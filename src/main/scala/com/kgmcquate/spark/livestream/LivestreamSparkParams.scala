package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams

class LivestreamSparkParams(
                             override val url: String,
                             override val resolution: String,
                             override val frameRate: Double,
                             override val mode: String,
                             override val tempPath: String,
                             override val framesPerSecond: Double,
                             override val segmentsPerMinute: Double,
                             val segmentsPerPartition: Int,
                             val numPartitions: Int
  ) extends LivestreamParams(url, resolution, frameRate, mode, tempPath, framesPerSecond, segmentsPerMinute)

object LivestreamSparkParams {
  def fromProperties(properties: java.util.Map[String, String]): LivestreamSparkParams = {
    new LivestreamSparkParams(
      if (properties.containsKey("url")) properties.get("url") else throw new Exception("url is required"),
      if (properties.containsKey("resolution")) properties.get("resolution") else throw new Exception("resolution is required"),
      properties.getOrDefault("frame_rate", "30.0").toDouble,
      properties.getOrDefault("mode", "frames"),
      properties.getOrDefault("temp_path", "/tmp"),
      properties.getOrDefault("frequency", "1.0").toDouble,
      properties.getOrDefault("segments_per_minute", "12").toDouble,
      properties.getOrDefault("segments_per_partition", "1").toInt,
      properties.getOrDefault("num_partitions", "1").toInt
    )
  }
}

