package analytics.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{StreamingContext, Durations}

object Tool {

  def main(args: Array[String]): Unit = {
    args.lift(0) match {
      case None => throw new RuntimeException("Please provide a fully-qualified class name")
      case Some(className) =>
        val conf = new SparkConf()
          .setMaster("local[2]")
          .setJars(SparkContext.jarOfObject(this).toSeq)
          .setAppName(className)
        val ctx = new SparkContext(conf)
        val streamingCtx = new StreamingContext(ctx, Durations.minutes(1))
        try {
          Class.forName(className).getDeclaredConstructor(classOf[StreamingContext]).newInstance(streamingCtx)
          streamingCtx.start()
          streamingCtx.awaitTermination()
        } finally {
          ctx.stop()
        }
    }
  }
}
