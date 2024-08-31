package com.kgmcquate.livestream.manifest.media

import com.kgmcquate.livestream.LivestreamParams
import com.kgmcquate.livestream.frame.Frame
import com.kgmcquate.livestream.video.VideoSegment
import io.lindstrom.m3u8.model.MediaSegment

import java.time.Duration
import scala.jdk.CollectionConverters.asScalaIteratorConverter



case class VideoSegments(
                          private val mediaSegments: java.util.Iterator[MediaSegmentWrapper],
                          private val startingMediaSequence: MediaSequence,
                          private val endingMediaSequence: MediaSequence,
                          mediaManifestInfo: MediaManifestInfo
                        ) {

  private val startTime = startingMediaSequence.time

  private val segments = {
    var timeOffset: Duration = Duration.ZERO
    var mediaSequence = startingMediaSequence.mediaSequence

    val mediaSegmentsList = mediaSegments.asScala.toList

    println(s"Creating new VideoSegments from mediaSegments: ${mediaSegmentsList.length}")
    println(s"Creating new VideoSegments from mediaSegments: ${mediaSegmentsList.length}")

    val segments =
      mediaSegmentsList
      .map(mediaSegment  => {
        val segment =
          VideoSegment(
            mediaSegment,
            MediaSequence(
              mediaSequence,
              timeOffset,
              startTime.plus(timeOffset)
            ),
            mediaManifestInfo
          )

        println(s"VideoSegments Created segment ${segment.mediaSequence.mediaSequence} at ${segment.mediaSequence.time}")
        timeOffset = timeOffset.plusNanos((mediaSegment.duration() * 1e9).toLong)
        mediaSequence += 1
        segment
      })

    println(s"VideoSegments Created ${segments.size} segments")

    segments.iterator
  }

  def getSegments(): Iterator[VideoSegment] = {
    segments
  }

  def getSegmentFromStart(timeFromStart: Duration): VideoSegment = {
    segments.find(segment => {
      timeFromStart.compareTo(segment.mediaSequence.segmentStartOffset) >= 0
    }).getOrElse(throw new Exception(s"No segment found at time $timeFromStart"))
  }

}
