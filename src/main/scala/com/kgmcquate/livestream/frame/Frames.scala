package com.kgmcquate.livestream.frame

import com.kgmcquate.livestream.manifest.media.MediaSequence

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID

case class Frames(
                  videoFilename: String,
                  lengthSeconds: Double,
                  framesPerSecond: Double,
                  mediaSequence: MediaSequence,
                  tempPath: String
                ) extends Iterable[Frame] {

  lazy val framePeriod = 1.0 / framesPerSecond
  lazy val numFrames = (lengthSeconds / framePeriod).toInt
  private var index = -1

  val tempPrefix = Paths.get(tempPath, s"yt_frame_${UUID.randomUUID()}_").toString

  def runFFMpeg: Iterator[String] = {
    val expectedFilenames = (1 until numFrames + 1).map(i => s"${tempPrefix}${"%03d".format(i)}.png")
    val command = s"ffmpeg -i $videoFilename -c:v png -vf fps=$framesPerSecond ${tempPrefix}%03d.png"
    import scala.sys.process.Process
    Process(command).!!
//    val proc = Process(command).run()
//    println(proc.)
//    val exitCode = proc.exitValue()

    expectedFilenames.iterator
  }

  override def iterator: Iterator[Frame] = new Iterator[Frame] {
    val imageFilenames = runFFMpeg

    def hasNext: Boolean = imageFilenames.hasNext

    def next(): Frame = {
      val filename = imageFilenames.next()
      import java.nio.file.{Files, Paths}

      val imageBytes: Array[Byte] = Files.readAllBytes(Paths.get(filename))

      Frame(imageBytes, FrameOffset(mediaSequence, index, framesPerSecond))
    }
  }

  def getFramesAt(secondsFromStart: Iterable[Double]): Iterable[Frame] = {
    val frameTimes = (0 until numFrames).map(i => (i, i * framePeriod))
    val indexesToGet =
      secondsFromStart
        .flatMap(desiredSeconds => {
          frameTimes
            .filter { case (_, seconds) =>
              (seconds - 0.5 * framePeriod) <= desiredSeconds && desiredSeconds < (seconds + 0.5 * framePeriod)
            }
            .map { case (frameIndex, _) => frameIndex }
        })
        .toArray

    this
      .zipWithIndex
      .filter { case (_, index) => indexesToGet.contains(index) }
      .map { case (frame, _) => frame }
  }

  def getFramesEvery(seconds: Double, start: Double = 0.0): Iterable[Frame] = {
    val desiredFrameTimes =
      (BigDecimal(0.0) until BigDecimal(lengthSeconds) by seconds)
        .map(_ + start)
        .map(_.toDouble)

    getFramesAt(desiredFrameTimes)
  }
}
//
//case class FramesOpenCV(
//                   videoFilename: String,
//                   lengthSeconds: Double,
//                   framesPerSecond: Double,
//                   mediaSequence: MediaSequence,
//                  tempPath: String
//                 ) extends Iterable[Frame]  {
//
//  lazy val framePeriod = 1.0 / framesPerSecond
//  lazy val numFrames = (lengthSeconds / framePeriod).toInt
//  private var index = -1
//
//
//  def iterator: Iterator[Frame] = new Iterator[Frame] {
//    val frame = new Mat()
//    val output = new MatOfByte()
//    val camera = new VideoCapture(videoFilename)
//
//    println(camera.getBackendName)
////    camera.setExceptionMode(true)
//
//    def hasNext: Boolean = {
//      val _hasNext = camera.read(frame)
//      if (!_hasNext) {
//        camera.release()
//        if(index == -1) {
//          throw new Exception("No frames found in video")
//        }
//        new File(videoFilename).delete()
//      }
//      _hasNext
//    }
//
//    def next(): Frame = {
//      imencode(".png", frame, output)
//      index += 1
//      Frame(output.toArray, FrameOffset(mediaSequence, index, framesPerSecond))
//    }
//  }
//
//  def getFramesAt(secondsFromStart: Iterable[Double]): Iterable[Frame] = {
//    val frameTimes = (0 until numFrames).map(i => (i, i * framePeriod))
//    val indexesToGet =
//      secondsFromStart
//        .flatMap(desiredSeconds => {
//          frameTimes
//            .filter { case (_, seconds) =>
//              (seconds - 0.5 * framePeriod) <= desiredSeconds && desiredSeconds < (seconds + 0.5 * framePeriod)
//            }
//            .map { case (frameIndex, _) => frameIndex }
//        })
//        .toArray
//
//    this
//      .zipWithIndex
//      .filter { case (_, index) => indexesToGet.contains(index) }
//      .map { case (frame, _) => frame }
//  }
//
//  def getFramesEvery(seconds: Double, start: Double = 0.0): Iterable[Frame] = {
//    val desiredFrameTimes =
//      (BigDecimal(0.0) until BigDecimal(lengthSeconds) by seconds)
//        .map(_ + start)
//        .map(_.toDouble)
//
//    getFramesAt(desiredFrameTimes)
//  }
//
//  def getFrameNumbers(numbers: Array[Int]): Iterable[Frame] = {
//    this
//      .zipWithIndex
//      .filter {
//        case (_, index) => numbers.contains(index)
//      }
//      .map { case (frame, _) => frame }
//  }
//}