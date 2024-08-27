package com.kgmcquate.livestream.manifest.media

import com.kgmcquate.livestream.video.VideoSegment

import scala.jdk.CollectionConverters.IterableHasAsJava

/**
 * A queue of video segments. This is used to keep track of the video segments that have yet to be downloaded.
 * New segments are added to the end of the queue.
 * Segments are removed from the front of the queue.
 */
case class VideoSegmentQueue() {

  private val queue = new java.util.concurrent.LinkedBlockingQueue[VideoSegment]

  def add(videoSegment: VideoSegment): Unit = {
    println(s"Queue Adding video segment: ${videoSegment.mediaSequence.mediaSequence}")
    queue.add(videoSegment)
//    queue.enqueue(videoSegment)
  }

  def add(videoSegments: Iterable[VideoSegment]): Unit = {
    videoSegments.map(queue.add)
  }

  def take(): Option[VideoSegment] = {
    Option(queue.poll())
  }

  def take(n: Int): Iterator[VideoSegment] = {
    (1 to n).iterator.flatMap(_ => Option(queue.poll())).map(s => {
      println(s"Queue Taking video segment: ${s.mediaSequence.mediaSequence}")
      s
    })
  }

  def lastSequence: Option[MediaSequence] = {
    Option(queue.peek()).map(_.mediaSequence)
  }

}
