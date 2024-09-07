package com.kgmcquate.livestream.video

import com.kgmcquate.livestream.frame
import com.kgmcquate.livestream.frame.Frames
import com.kgmcquate.livestream.manifest.media.{MediaManifestInfo, MediaSegmentWrapper, MediaSequence}

import java.io.FileOutputStream
import java.net.{HttpURLConnection, URL}
import java.nio.file.Paths
import java.util.UUID

case class VideoSegment(
                         private val mediaSegment: MediaSegmentWrapper,
                         mediaSequence: MediaSequence,
                         mediaManifestInfo: MediaManifestInfo
                       ) {
  def duration(): Double = mediaSegment.duration()

  def uri(): String = mediaSegment.uri()

  def getVideo(): Array[Byte] = {
    val videoConnection = new URL(uri()).openConnection().asInstanceOf[HttpURLConnection]
    videoConnection.setRequestMethod("GET")
    val videoInputStream = videoConnection.getInputStream
    val videoBytes = videoInputStream.readAllBytes()
    videoBytes
  }

  def getVideoAsStream(): java.io.InputStream = {
    val videoConnection = new URL(uri()).openConnection().asInstanceOf[HttpURLConnection]
    videoConnection.setRequestMethod("GET")
    videoConnection.getInputStream
  }

  def writeVideoToFile(filePath: String): Unit = {
    println(s"Writing video to file: $filePath")
    val videoStream = getVideoAsStream()
    val outputStream = new FileOutputStream(filePath)
    val buffer = new Array[Byte](1024)
    var bytesRead = videoStream.read(buffer)
    while (bytesRead != -1) {
      outputStream.write(buffer, 0, bytesRead)
      bytesRead = videoStream.read(buffer)
    }
    videoStream.close()
    outputStream.close()
  }

  def getFrames(tempPath: String): Frames = {
    val videoFilename = Paths.get(tempPath, s"yt_segment_${UUID.randomUUID()}.ts").toString

    writeVideoToFile(videoFilename)
    frame.Frames(videoFilename, duration(), mediaManifestInfo.frameRate, mediaSequence, tempPath)
  }

}