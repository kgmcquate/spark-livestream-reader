package com.kgmcquate.livestream.frame

import com.kgmcquate.livestream.manifest.media.MediaSequence

import java.time.{Duration, Instant}

case class FrameOffset(mediaSequence: MediaSequence, frameNumber: Int, fps: Double) {
  def time: Instant = mediaSequence.time
    .plus(
      Duration.ofNanos(
        ((frameNumber.toLong * fps) * 1e9 ).toLong
      )
    )
}
