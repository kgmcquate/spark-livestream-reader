package com.kgmcquate.livestream.video

import com.kgmcquate.livestream.frame
import com.kgmcquate.livestream.frame.Frames
import com.kgmcquate.livestream.manifest.media.{MediaManifestInfo, MediaSegmentWrapper, MediaSequence}
import io.lindstrom.m3u8.model.MediaSegment
import nu.pattern.OpenCV

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

  private def initializeOpenCV(): Unit = {
    OpenCV.loadLocally()
  }

  def getFrames(tempPath: String): Frames = {
    initializeOpenCV()
    val videoFilename = Paths.get(tempPath, s"yt_segment_${UUID.randomUUID()}.mpeg").toString

    writeVideoToFile(videoFilename)
    frame.Frames(videoFilename, duration(), mediaManifestInfo.frameRate, mediaSequence)
  }
}