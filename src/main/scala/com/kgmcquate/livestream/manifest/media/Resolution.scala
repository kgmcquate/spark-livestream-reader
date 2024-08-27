package com.kgmcquate.livestream.manifest.media

case class Resolution(width: Int, height: Int)
object Resolution {
  def fromResolution(resolution: io.lindstrom.m3u8.model.Resolution): Resolution = {
    Resolution(resolution.width(), resolution.height())
  }
}
