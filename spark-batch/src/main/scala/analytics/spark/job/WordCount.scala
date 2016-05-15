package analytics.spark.job

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/** Simple word count job using Dataset API */
class WordCount(ctx: SparkContext) {

  val sqlCtx = SQLContext.getOrCreate(ctx)

  import sqlCtx.implicits._

  val file = getClass.getResource("/foo.txt").getFile

  val lines = sqlCtx.read.text(file).as[String]

  val words = lines
    .flatMap(_.split(" "))
    .filter(_ != "")

  val counts = words
    .groupBy(_.toLowerCase)
    .count()

  counts.show()
}
