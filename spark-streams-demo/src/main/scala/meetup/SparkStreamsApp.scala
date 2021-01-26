package meetup

import org.apache.spark.sql.streaming.Trigger

object SparkStreamsApp extends App {

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().getOrCreate()

  println(s">>> Spark version: ${spark.version}")

  import concurrent.duration._
  spark.readStream
    .format("rate")
    .load
    .writeStream
    .format("console")
    .trigger(Trigger.ProcessingTime(5.seconds))
    .option("truncate", false)
    .start
    .awaitTermination

}
