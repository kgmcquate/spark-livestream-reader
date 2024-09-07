package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader

class LivestreamPartitionReader(partition: LivestreamPartition, params: LivestreamParams) extends PartitionReader[InternalRow] {
  private val frameIterator = partition.videoSegments.flatMap(_.getFrames(params.tempPath)).iterator

  override def next(): Boolean = {
    frameIterator.hasNext
  }

  override def get(): InternalRow = {
    val frame = frameIterator.next()
    println(s"Getting next frame: ${frame.frameSequence.time}")
    InternalRow(
      frame.frameSequence.time.toEpochMilli * 1000L,
      frame.frame
    )
  }

  override def close(): Unit = {}
}
