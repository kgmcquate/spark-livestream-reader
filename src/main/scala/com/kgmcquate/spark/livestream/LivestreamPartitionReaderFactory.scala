package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}

class LivestreamPartitionReaderFactory(params: LivestreamParams) extends PartitionReaderFactory {

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    partition match {
      case p: LivestreamPartition =>
        new LivestreamPartitionReader(p, params)
      case _ => throw new IllegalArgumentException("Expected LivestreamPartition")
    }
  }

}
