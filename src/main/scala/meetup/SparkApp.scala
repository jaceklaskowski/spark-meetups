package meetup

object SparkApp extends App {

  // The underlying SparkContext throws an exception
  // when --files or --jars could not be found

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().getOrCreate()

  println(s">>> Spark version: ${spark.version}")

  try {
    import org.apache.spark.SparkFiles
    val testme = SparkFiles.get("test.me")
    println(s">>> test.me: $testme")
    val body = scala.io.Source.fromFile(testme).mkString
    println(
      s"""
         |>>> Content of test.me:
         |-----
         |$body
         |-----
         |""".stripMargin)
  } catch {
    case t: Throwable =>
      println(">>> Swallowing an exception for demo purposes")
      t.printStackTrace()
  }

  // Commented out to let the driver pod be up and running
  // That allows accessing the container and the file system
  // For debugging purposes only

  // That assumes SparkContext has been created
  // esp. in spark-submit --deploy client

  // spark.sparkContext.stop()
}
