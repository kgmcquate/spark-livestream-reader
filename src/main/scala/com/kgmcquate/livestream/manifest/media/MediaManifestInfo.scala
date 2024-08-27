package com.kgmcquate.livestream.manifest.media

import io.lindstrom.m3u8.model.Variant

case class MediaManifestInfo(
                              manifestUrl: String,
                              resolution: Resolution,
                              frameRate: Double
                            )
object MediaManifestInfo {
  def fromVariant(variant: Variant): MediaManifestInfo = {
    MediaManifestInfo(
      variant.uri(),
      Resolution.fromResolution(variant.resolution().orElse(null)),
      variant.frameRate().orElse(null)
    )
  }
}
