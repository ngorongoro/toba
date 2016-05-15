package analytics.spark.job

import org.apache.spark.streaming.StreamingContext

/** Simple word count stream processing job using common implementation of algorithm */
class CommonWordCount(streamingCtx: StreamingContext) {
  val dir = getClass.getResource("/").getFile
  val words = streamingCtx.textFileStream(dir).flatMap(_.split(" "))
  // Use common WordCount implementation
  words.foreachRDD { rdd =>
    analytics.spark.impl.WordCount(rdd)
  }
}
