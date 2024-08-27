package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams
import com.kgmcquate.livestream.manifest.media.VideoSegmentQueue
import org.apache.spark.sql.connector.read.ScanBuilder

class LivestreamScanBuilder extends ScanBuilder {
  private var params: LivestreamSparkParams = _
  private var manifestUrl: String = _
  private var segmentQueue: VideoSegmentQueue = _

  def withParams(params: LivestreamSparkParams): LivestreamScanBuilder = {
    this.params = params
    this
  }

  def withManifestUrl(manifestUrl: String): LivestreamScanBuilder = {
    this.manifestUrl = manifestUrl
    this
  }

  def withSegmentQueue(segmentQueue: VideoSegmentQueue): LivestreamScanBuilder = {
    this.segmentQueue = segmentQueue
    this
  }

  override def build(): LivestreamScan = {
    new LivestreamScan(segmentQueue, params)
  }
}
