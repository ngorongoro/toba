package analytics.spark.job

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/** Simple word count job using common implementation of algorithm */
class CommonWordCount(ctx: SparkContext) {
  val file = getClass.getResource("/foo.txt").getFile
  val rdd = ctx.textFile(file)
  // Use common WordCount implementation
  analytics.spark.impl.WordCount(rdd)
}
