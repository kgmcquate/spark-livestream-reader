package com.kgmcquate.livestream.manifest.media

import com.kgmcquate.livestream.{LivestreamParams, YouTubePage}
import com.kgmcquate.livestream.manifest.master.StreamOptionsManifest
import com.kgmcquate.livestream.video.VideoSegment

import java.lang.Math.round

case class PlaylistManager(page: YouTubePage, params: LivestreamParams, segmentQueue: VideoSegmentQueue) extends Runnable {

  private var playlistReader: PlaylistReader = _
  private val secondsPerSequence = 60 / params.segmentsPerMinute

  private def checkSegmentIsNeeded(segment: VideoSegment, segmentDuration: Double): Boolean = {
    if (segmentQueue.lastSequence.isEmpty) {
      return true
    }
    val lastSequence = segmentQueue.lastSequence

    val everyNSequence = (segmentDuration / secondsPerSequence)

    val nextSequence = lastSequence.map(_.mediaSequence + everyNSequence).getOrElse(0.0)

    val isNextSequence = segment.mediaSequence.mediaSequence > round(nextSequence)

    isNextSequence
  }

  def getPlaylistReader: PlaylistReader = {
    refreshReader()
    playlistReader
  }

  private def refreshReader(): PlaylistReader = {
    if (page.manifestIsExpired) {
      val streamOptions = StreamOptionsManifest.fromUrl(page.getCurrentManifestUrl)
      val mediaManifestInfo = streamOptions.getMediaManifestInfo(params.resolution)
      playlistReader = PlaylistReader(mediaManifestInfo)
    }
    playlistReader
  }

  private def monitorNewSegments(): Unit = {
    println("Monitoring new segments")
    while (true) {
      refreshReader()
      val segments = playlistReader.waitForNextSegments().getSegments().toList

      println(s"PlaylistManager got new video segments ${segments.size}")

      segments
        .foreach(segment => {
          val needed = checkSegmentIsNeeded(segment, segment.duration())
          println(s"Segment is needed: ${segment.mediaSequence.mediaSequence}")

          if (needed) segmentQueue.add(segment)
        })
    }
  }

  def run(): Unit = {
    monitorNewSegments()
  }

  def getSegmentQueue: VideoSegmentQueue = segmentQueue


}
