package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.manifest.media.{PlaylistManager, VideoSegmentQueue}
import com.kgmcquate.livestream.{LivestreamParams, YouTubePage}
import org.apache.spark.sql.connector.catalog.{SupportsRead, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import scala.jdk.CollectionConverters.setAsJavaSetConverter

case class LivestreamTable(override val schema: StructType,
                           override val partitioning: Array[Transform],
                           params: LivestreamSparkParams)
  extends SupportsRead {

  val page: YouTubePage = YouTubePage(params.url)

  val videoSegmentQueue: VideoSegmentQueue = VideoSegmentQueue()
  // Keep the media manifests and new segments up to date
  private val playlistManager = PlaylistManager(page, params, videoSegmentQueue)
  private val playlistManagerThread = new Thread(playlistManager)
  playlistManagerThread.start()

  override def name(): String = params.url

  override def capabilities(): util.Set[TableCapability] = {
    Set[TableCapability](TableCapability.MICRO_BATCH_READ).asJava
  }

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = {
//    println(partitioning.mkString(","))

    LivestreamScan.builder()
      .withParams(params)
      .withManifestUrl(page.getCurrentManifestUrl)
      .withSegmentQueue(videoSegmentQueue)
  }

//  override def partitioning(): Array[Transform] = {
//    Array()
//  }

}
