package com.kgmcquate.livestream.frame

import com.kgmcquate.livestream.manifest.media.MediaSequence
import org.opencv.core.{Mat, MatOfByte}
import org.opencv.imgcodecs.Imgcodecs.imencode
import org.opencv.videoio.VideoCapture

import java.io.File
import java.nio.file.Path

case class Frames(
                   videoFilename: String,
                   lengthSeconds: Double,
                   framesPerSecond: Double,
                    mediaSequence: MediaSequence
                 ) extends Iterable[Frame]  {
  lazy val framePeriod = 1.0 / framesPerSecond
  lazy val numFrames = (lengthSeconds / framePeriod).toInt
  private var index = -1

  def iterator: Iterator[Frame] = new Iterator[Frame] {
    val f = new java.io.File(videoFilename)
    if (f.exists && !f.isDirectory) {
      println(s"Video file exists: $videoFilename")
    } else {
      throw new Exception(s"Video file does not exist: $videoFilename")
    }
    val frame = new Mat()
    val output = new MatOfByte()
    val camera = new VideoCapture(videoFilename)

    def hasNext: Boolean = {
      val _hasNext = camera.read(frame)
      if (!_hasNext) {
        camera.release()
        new File(videoFilename).delete()
      }
      _hasNext
    }

    def next(): Frame = {
      imencode(".png", frame, output)
      index += 1
      Frame(output.toArray, FrameOffset(mediaSequence, index, framesPerSecond))
    }
  }

  def getFramesAt(secondsFromStart: Array[Double]): Array[Frame] = {
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
      .toArray
  }

  def getFramesEvery(seconds: Double, start: Double = 0.0): Array[Frame] = {
    val desiredFrameTimes =
      (BigDecimal(0.0) until BigDecimal(lengthSeconds) by seconds)
        .map(_ + start)
        .map(_.toDouble)
        .toArray

    getFramesAt(desiredFrameTimes)
  }

  def getFrameNumbers(numbers: Array[Int]): Array[Frame] = {
    this
      .zipWithIndex
      .filter {
        case (_, index) => numbers.contains(index)
      }
      .map { case (frame, _) => frame }
      .toArray
  }
}
