package meetup

import org.apache.spark.sql.SparkSession

object SparkStreamsAppTest extends App {

  SparkSession.builder().master("local[*]").getOrCreate()
  SparkStreamsApp.main(args = Array.empty)

}
