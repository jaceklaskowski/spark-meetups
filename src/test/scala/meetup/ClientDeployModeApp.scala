package meetup

import org.apache.spark.sql.SparkSession

object ClientDeployModeApp extends App {

  val appName = this.getClass.getSimpleName.replaceAll("\\$", "")
  SparkSession.builder()
    .master("k8s://https://fakeHost")
    .config("spark.submit.deployMode", "client")
    .appName(appName)
    .getOrCreate()
  SparkApp.main(args)

}
