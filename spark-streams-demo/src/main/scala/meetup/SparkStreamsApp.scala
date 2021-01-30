package meetup

import org.apache.spark.sql.streaming.Trigger

import java.util.UUID

object SparkStreamsApp extends App {

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().getOrCreate()

  println(s">>> Spark version: ${spark.version}")

  val checkpointLocation = if (args.length > 0) args(0) else s"target/${UUID.randomUUID()}"
  println(s">>> Using checkpoint location: $checkpointLocation")

  import concurrent.duration._
  spark.readStream
    .format("rate")
    .load
    .writeStream
    .format("console")
    .trigger(Trigger.ProcessingTime(5.seconds))
    .option("truncate", false)
    .option("checkpointLocation", checkpointLocation)
    .start
    .awaitTermination

}
