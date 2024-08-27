This is a library for streaming video livestreams from YouTube with Apache Spark.

## Installation
Add the following to your `build.sbt` file:
```scala
libraryDependencies += "com.kgmcquate" %% "spark-livestream-reader" % "0.2.0"
```

Or add the Maven coordinates to your PySpark job:
```bash
spark-submit --packages com.kgmcquate::spark-livestream-reader:0.2.0
```

## Usage
```scala
 spark
     .readStream
     .format(s"com.kgmcquate.spark.livestream.LivestreamReader")
     .option("url", "https://www.youtube.com/watch?v=ydYDqZQpim8")
     .option("resolution", "1920x1080")
     .load()
```

## Reader options
```scala
spark.readStream
   .format(s"com.kgmcquate.spark.livestream.LivestreamReader")
   // The URL of the YouTube video
   .option("url", "https://www.youtube.com/watch?v=ydYDqZQpim8")
   // The resolution of the livestream
   .option("resolution", "1920x1080")
   // The frame rate of the livestream
   .option("frameRate", "30")
   // Temp path to store video chunks
   .option("tempPath", "/tmp")
   // The number of frames per second to output in each video segment
   .option("framesPerSecond", "30")
   // The number of segments per minute to output
   // Reducing the number of segments per minute will help avoid rate limiting, since each segment is an API call.
   .option("segmentsPerMinute", "2")
   // The max number of segments per partition
   .option("segmentsPerPartition", "10")
   // The number of partitions to use
   .option("numPartitions", "10")
```

## How it works
1. Get the link of the YouTube livestream
   - This is the link to the YouTube video that is currently being livestreamed
   - The link is usually in the format `https://www.youtube.com/watch?v=VIDEO_ID`
2. Get the master manifest file
   - Scrape the YouTube video page for the master manifest file URL
   - This is the `.m3u8` file that contains the links to the video streams
   - The master manifest expires after a few hours
3. Choose a stream variant from the master manifest
    - Choose the stream variant with the desired resolution and frame rate
4. Get the stream variant manifest file
   - This is the `.m3u8` file that contains the links to the video chunks
   - Each chunk is a small video file, usually 2-10 seconds long
   - The entire manifest is usually for the next 30 seconds, it's refreshed before it expires
5. Get the video segments
   - Download video segments from the stream variant manifest file
6. Extract frames from video segments
   - Using OpenCV, extract frames from the video segments
   - Each frame is a re-encoded as a PNG image