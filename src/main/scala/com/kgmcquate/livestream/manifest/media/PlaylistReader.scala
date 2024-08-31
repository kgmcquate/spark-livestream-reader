package com.kgmcquate.livestream.manifest.media

import io.lindstrom.m3u8.model.MediaPlaylist
import io.lindstrom.m3u8.parser.{MediaPlaylistParser, ParsingMode}

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.time.{Duration, Instant}
import scala.jdk.CollectionConverters.{asJavaIteratorConverter, asScalaBufferConverter}


case class PlaylistReader(mediaManifestInfo: MediaManifestInfo) {
  private var currentMediaManifest: MediaPlaylist = _

  private var currentStartSequence: MediaSequence = _
  private var currentEndSequence: MediaSequence = _

  private def currentManifestDuration: Duration = Duration.between(currentStartSequence.time, currentEndSequence.time)
  //  private var approxStartTime: Instant = _
//  private var approxEndTime: Instant = _


  private def currentManifestIsExpired(): Boolean = {
    val currentTime = Instant.now
    val timePadding = currentManifestDuration.dividedBy(10).minusSeconds(1)
    currentTime.isAfter(currentEndSequence.time.minus(timePadding))
  }

//  def getSegments(): VideoSegments = {
//    val segmentsPlaylist = SegmentsPlaylist(mediaManifestInfo)
//    segmentsPlaylist.getSegments()
//  }

  def waitForNextSegments(): VideoSegments = {
    if (currentMediaManifest == null) {
      return getSegments()
    }
    val currentTime = Instant.now
    val waitTime = Duration.between(currentTime, currentEndSequence.time).toMillis
    println(s"Waiting for $waitTime milliseconds for new playlist segments")
    if (waitTime > 0) {
      Thread.sleep(waitTime)
    }
    getSegments()
  }

  def getSegments(forceManifestRefresh: Boolean = false): VideoSegments = {
    println("Getting new playlist segments")
    if (forceManifestRefresh || currentMediaManifest == null || currentManifestIsExpired()) {
      println("Getting new media manifest")
      currentMediaManifest = getMediaManifest
      currentStartSequence = MediaSequence(
        currentMediaManifest.mediaSequence(),
        Duration.ZERO,
        Instant.now
      )

      val duration = Duration.ofNanos(
        (currentMediaManifest.mediaSegments().asScala.map(_.duration()).sum * 1e9).toLong
      )
      println(s"Duration of new media manifest is ${duration.toSeconds} seconds")

      currentEndSequence = MediaSequence(
        currentMediaManifest.mediaSequence() + currentMediaManifest.mediaSegments().size() - 1,
        duration,
        currentStartSequence.time.plus(duration)
      )
    }

    println(s"Creating new VideoSegments from mediaSegments: ${currentMediaManifest.mediaSegments().size()}")

    VideoSegments(
      currentMediaManifest.mediaSegments().asScala.map(MediaSegmentWrapper.fromMediaSegment).iterator.asJava,
      currentStartSequence,
      currentEndSequence,
      mediaManifestInfo
    )
  }


  private def getMediaManifest: MediaPlaylist = {
    val manifestUrl = mediaManifestInfo.manifestUrl
    println(s"Getting media manifest from ${manifestUrl.take(50)}")
    val manifestConnection = new URL(manifestUrl).openConnection().asInstanceOf[HttpURLConnection]
    manifestConnection.setRequestMethod("GET")
    val manifestInputStream = manifestConnection.getInputStream

    val mediaParser = new MediaPlaylistParser(ParsingMode.LENIENT)
    val playlist = mediaParser.readPlaylist(new BufferedReader(new InputStreamReader(manifestInputStream)))
    println(s"Got media playlist with ${playlist.mediaSegments().size()} segments")
    playlist
  }
}
