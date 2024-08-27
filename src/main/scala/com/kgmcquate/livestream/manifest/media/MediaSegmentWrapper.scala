package com.kgmcquate.livestream.manifest.media

import io.lindstrom.m3u8

class MediaSegmentWrapper(
                         uri: String,
                         duration: Double
                         ) extends Serializable {

  def uri(): String = uri
  def duration(): Double = duration
}
object MediaSegmentWrapper {
  def fromMediaSegment(mediaSegment: m3u8.model.MediaSegment): MediaSegmentWrapper = {
    new MediaSegmentWrapper(mediaSegment.uri(), mediaSegment.duration())
  }
}