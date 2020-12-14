package meetup

object SparkApp extends App {

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().getOrCreate()

  println(s">>> Spark version: ${spark.version}")

  spark.sparkContext.stop()

}
