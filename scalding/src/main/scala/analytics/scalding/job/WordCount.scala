package analytics.scalding.job

import com.twitter.scalding._
import com.twitter.scalding.typed.TypedSink

class WordCount(args: Args) extends Job(args) {

  val file = getClass.getResource("/foo.txt").getFile

  TypedPipe.from(TextLine(file))
    .flatMap(tokenize)
    .groupBy(identity)
    .size
    .debug
    .write(TypedSink(NullSource))

  def tokenize(text: String): Array[String] = {
    text.toLowerCase.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+")
  }
}
