package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams
import com.kgmcquate.livestream.frame.Frame
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader

class LivestreamPartitionReader(partition: LivestreamPartition, params: LivestreamParams) extends PartitionReader[InternalRow] {

  private lazy val iterator: Iterator[Frame] = {
    if(params.mode == "frames") {
      partition.videoSegments.flatMap(_.getFrames(params.tempPath)).iterator
    } else {
      throw new Exception(s"Unsupported mode ${params.mode}")
    }
  }

  override def next(): Boolean = {
    val hasNext = iterator.hasNext
//    throw new Exception("next() should not be called")
    println(s"Checking next frame: $hasNext")
    hasNext
  }

  override def get(): InternalRow = {
    val frame = iterator.next()
//    println(s"Getting next frame: ${frame.frameSequence.time}")
//    throw new Exception("get() should be called")
    InternalRow(
      frame.frameSequence.time.toEpochMilli * 1000L,
      frame.frame
    )
  }

  override def close(): Unit = {}
}
