package analytics.spark

import org.apache.spark.{SparkConf, SparkContext}

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
        try {
          Class.forName(className).getDeclaredConstructor(classOf[SparkContext]).newInstance(ctx)
        } finally {
          ctx.stop()
        }
    }
  }
}
