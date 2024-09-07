package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams
import com.kgmcquate.livestream.frame.FrameOffset
import com.kgmcquate.livestream.manifest.media.{MediaSequence, VideoSegmentQueue}
import org.apache.spark.sql.connector.read.streaming.{MicroBatchStream, Offset}
import org.apache.spark.sql.connector.read.{InputPartition, Scan}
import org.apache.spark.sql.types.StructType

import java.time.{Duration, Instant}

class LivestreamScan(segmentQueue: VideoSegmentQueue, params: LivestreamSparkParams) extends Scan with MicroBatchStream {

  override def readSchema(): StructType = {
    LivestreamReader.frameSchema
  }

  override def commit(end: Offset): Unit = {
//    end match {
//      case e: LivestreamOffset => segmentQueue //.commit(e.frameSequence.mediaSequence)
//      case _ => throw new IllegalArgumentException("Invalid offset type")
//    }
  }

  override def createReaderFactory(): LivestreamPartitionReaderFactory = {
    new LivestreamPartitionReaderFactory(params)
  }

  override def latestOffset(): LivestreamOffset = {
    LivestreamOffset(
      FrameOffset(
        segmentQueue.lastSequence.getOrElse(MediaSequence(0, Duration.ZERO, Instant.now)),
        0,
        params.frameRate
      )
    )
  }

  override def planInputPartitions(start: Offset, end: Offset): Array[InputPartition] = {
    (start, end) match {
      case (s: LivestreamOffset, e: LivestreamOffset) => planInputPartitions(s, e)
      case _ => throw new IllegalArgumentException("Invalid offset type")
    }
  }

  private def planInputPartitions(start: LivestreamOffset, end: LivestreamOffset): Array[InputPartition] = {
    val startFrame = start.frameSequence
    val endFrame = end.frameSequence
    // Read from the segments queue
    val segments = segmentQueue.take(params.numPartitions * params.segmentsPerPartition)
    val partitionedSegments = segments.grouped(params.segmentsPerPartition)
    partitionedSegments.map(segmentGroup => {
      LivestreamPartition(
        segmentGroup
      )
    }).toArray
  }

  override def initialOffset(): Offset = {
    LivestreamOffset(
      FrameOffset(
        MediaSequence(0, Duration.ZERO, Instant.now),
        0,
        params.frameRate
      )
    )
  }

  override def deserializeOffset(json: String): LivestreamOffset = LivestreamOffset.fromJson(json)

  override def stop(): Unit = {}

  override def toMicroBatchStream(checkpointLocation: String): MicroBatchStream = {
    this
  }
}
object LivestreamScan {
  def builder(): LivestreamScanBuilder = {
    new LivestreamScanBuilder
  }
}