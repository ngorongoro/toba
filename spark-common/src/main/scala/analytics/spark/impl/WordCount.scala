package analytics.spark.impl

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext

/** Simple word count algorithm using Dataset API */
object WordCount {

  def apply(rdd: RDD[String]): Unit = {
    val sqlCtx = SQLContext.getOrCreate(rdd.sparkContext)

    import sqlCtx.implicits._

    val lines = rdd.toDF.as[String]

    val words = lines
      .flatMap(_.split(" "))
      .filter(_ != "")

    val counts = words
      .groupBy(_.toLowerCase)
      .count()

    counts.show()
  }
}
