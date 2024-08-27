package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.frame.FrameOffset
import com.kgmcquate.livestream.manifest.media.MediaSequence
import org.apache.spark.sql.connector.read.streaming.Offset

import java.time.{Duration, Instant}

case class LivestreamOffset(frameSequence: FrameOffset) extends Offset {
  override def json(): String = {
    s"""{\"epoch_ts\": ${frameSequence.mediaSequence.time}
       |
       |}""".stripMargin
  }
}

object LivestreamOffset {
  def fromJson(the_json: String): LivestreamOffset = {
    LivestreamOffset(FrameOffset(MediaSequence(0, Duration.ZERO, Instant.now), 0, 30))
  }
}