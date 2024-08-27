package com.kgmcquate.livestream

case class LivestreamParams(
                             url: String,
                             resolution: String,
                             frameRate: Double,
                             mode: String,
                             tempPath: String,
                             framesPerSecond: Double,
                             segmentsPerMinute: Double
                            )

object LivestreamParams {
  def fromProperties(properties: java.util.Map[String, String]): LivestreamParams = {
    LivestreamParams(
      if (properties.containsKey("video_id")) properties.get("video_id") else throw new Exception("video_id is required"),
      if (properties.containsKey("resolution")) properties.get("resolution") else throw new Exception("resolution is required"),
      properties.getOrDefault("frame_rate", "30.0").toDouble,
      properties.getOrDefault("mode", "frames"),
      properties.getOrDefault("temp_path", "/tmp"),
      properties.getOrDefault("frames_per_second", "1.0").toDouble,
      properties.getOrDefault("segments_per_minute", "12").toDouble
    )
  }
}