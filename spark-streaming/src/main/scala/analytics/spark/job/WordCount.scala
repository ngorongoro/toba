package analytics.spark.job

import org.apache.spark.streaming.StreamingContext

class WordCount(streamingCtx: StreamingContext) {
  val dir = getClass.getResource("/").getFile
  val lines = streamingCtx.textFileStream(dir)
  val words = lines.flatMap(_.split(" "))
  val counts = words.map(x => (x, 1)).reduceByKey(_ + _)
  counts.print()
}
