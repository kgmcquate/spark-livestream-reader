package com.kgmcquate.livestream.manifest.media

import java.time.{Duration, Instant}

case class MediaSequence(mediaSequence: Long, segmentStartOffset: Duration, time: Instant)
