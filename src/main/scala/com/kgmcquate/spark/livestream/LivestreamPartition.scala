package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.video.VideoSegment
import org.apache.spark.sql.connector.read.InputPartition

case class LivestreamPartition(videoSegments: Seq[VideoSegment]) extends InputPartition {
  override def preferredLocations: Array[String] = Array.empty
}
