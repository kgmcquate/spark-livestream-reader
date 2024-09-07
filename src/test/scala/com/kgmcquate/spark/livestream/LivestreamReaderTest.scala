package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.manifest.media.{PlaylistManager, VideoSegmentQueue}
import com.kgmcquate.livestream.{LivestreamParams, YouTubePage}
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.functions.{col, lit, struct}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StringType
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec

import java.io.FileOutputStream

final class LivestreamReaderTest extends AnyFlatSpec with BeforeAndAfterAll  {
  @transient var spark: SparkSession = _

//  private object testImplicits extends SQLImplicits {
//    protected override def _sqlContext: SQLContext = spark.sqlContext
//  }
//  import testImplicits._

//  override def beforeAll(): Unit = {
//    spark = SparkSession.builder()
////      .config(s"spark.broadcast.compress", s"false")
////      .config(s"spark.shuffle.compress", s"false")
////      .config(s"spark.shuffle.spill.compress", s"false")
////      .config(s"spark.master", s"local")
//      .master("local")
//      .getOrCreate()
//  }

  override def afterAll(): Unit = {
//    spark.stop()
  }

//  behavior of s"stuff"

  "YouTubeLiveReader" should "create a dataframe" in {
    spark = SparkSession.builder()
      //      .config(s"spark.broadcast.compress", s"false")
      //      .config(s"spark.shuffle.compress", s"false")
      //      .config(s"spark.shuffle.spill.compress", s"false")
      //      .config(s"spark.master", s"local")
      .master(s"local")
      .getOrCreate()

    spark
      .readStream
      .format(s"com.kgmcquate.spark.livestream.LivestreamReader")
      .option("url", "https://www.youtube.com/watch?v=ydYDqZQpim8")
      .option("resolution", "1920x1080")
      .load()
      .writeStream
      .trigger(Trigger.ProcessingTime((15L*1e3).toLong))
      .format(s"console")
      .start()
      .processAllAvailable()
//      .awaitTermination()

  }

  "LivestreamReader" should "write to kafka" in {
    spark = SparkSession.builder()
      //      .config(s"spark.broadcast.compress", s"false")
      //      .config(s"spark.shuffle.compress", s"false")
      //      .config(s"spark.shuffle.spill.compress", s"false")
      //      .config(s"spark.master", s"local")
      .master(s"local")
      .getOrCreate()

//    val confluent_cloud_api_key = sys.env("CONFLUENT_CLOUD_API_KEY")
//    val confluent_cloud_api_secret = sys.env("CONFLUENT_CLOUD_API_SECRET")

    val confluent_cloud_api_key="BDZDYKYZIDEF5ERV"
    val confluent_cloud_api_secret="uhZ2NbBf+02fcR4TAK4HZv7jcKBRROVrnLM+/XM9x1t65nFuR42tc4993PiT803S"
    val url = "https://www.youtube.com/watch?v=ydYDqZQpim8"
    spark
      .readStream
      .format(s"com.kgmcquate.spark.livestream.LivestreamReader")
      .option("url", "https://www.youtube.com/watch?v=ydYDqZQpim8")
      .option("resolution", "1920x1080")
      .load()
      .select(
        functions.to_json(
          struct(
            lit(url).as("url"),
            col("ts")
          )
        ).as("key"),
        col("frame_data").as("value")
      )
      .writeStream
      .trigger(Trigger.ProcessingTime((15L*1e3).toLong))
      .format("kafka")
      .option("kafka.bootstrap.servers", "pkc-p11xm.us-east-1.aws.confluent.cloud:9092")
      .option("kafka.security.protocol", "SASL_SSL")
      .option("kafka.sasl.jaas.config", s"org.apache.kafka.common.security.plain.PlainLoginModule required username='$confluent_cloud_api_key' password='$confluent_cloud_api_secret';")
      .option("kafka.sasl.mechanism", "PLAIN")
      .option("topic", "raw-video-frames")
      .option("checkpointLocation", "./checkpoints")
//      .option("startingOffsets", "earliest")
//      .option("maxOffsetsPerTrigger", 20)
      .start()
      .processAllAvailable()
    //      .awaitTermination()
  }

  "" should "" in {
    val page = YouTubePage("https://www.youtube.com/watch?v=ydYDqZQpim8")
    val manifestUrl = page.getCurrentManifestUrl
    println(manifestUrl)
    val params = LivestreamParams(
      url = "ydYDqZQpim8",
      resolution = "1920x1080",
      frameRate = 30,
      mode = "frames",
      tempPath = "/tmp/",
      framesPerSecond = 1,
      segmentsPerMinute = 12
    )

    val videoSegmentQueue: VideoSegmentQueue = VideoSegmentQueue()

    val playlistManagerThread = new Thread(PlaylistManager(page, params, videoSegmentQueue))
    playlistManagerThread.start()

    while (true) {
      val videoSegments = videoSegmentQueue.take(10).toList
      println(s"Test Got ${videoSegments.size} segments")

      videoSegments.foreach(segment => {
        val frames = segment.getFrames("/tmp/")
        val frameList = frames.getFramesEvery(0.5)
        var i = 0
        frameList.foreach(frame => {
          val filePath = s"data/test_${frame}.jpg"
          i += 1
          val outputStream = new FileOutputStream(filePath)
          outputStream.write(frame.frame)
          outputStream.close()
          println(s"Frame written to ${filePath}")
        })
      })

      Thread.sleep(3000)
    }

  }

}