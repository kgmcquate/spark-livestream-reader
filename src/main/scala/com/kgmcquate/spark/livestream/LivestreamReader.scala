package com.kgmcquate.spark.livestream

import com.kgmcquate.livestream.LivestreamParams
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.{BinaryType, StructField, StructType, TimestampType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util


class LivestreamReader extends TableProvider {

  //  override def schema(): StructType = {StructType(Seq())}
  override def inferSchema(options: CaseInsensitiveStringMap): StructType = LivestreamReader.frameSchema

  override def getTable(
                         schema: StructType,
                         partitioning: Array[Transform],
                         properties: util.Map[String, String]
                       ): Table = {

    val params = LivestreamSparkParams.fromProperties(properties)

    LivestreamTable(schema, partitioning, params)
  }
  //  override def columns(): Array[Column] = super.columns()

}

object LivestreamReader {
  val frameSchema: StructType = StructType(Seq(
    StructField(s"ts", TimestampType, nullable = false),
    StructField(s"frame_data", BinaryType, nullable = true)
  ))
}
