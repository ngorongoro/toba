# toba

An example project structure that allows the authoring and execution of jobs written for various analytics engines.
 
## Spark (RDDs, DataFrames, and Datasets)
```
sbt> project spark-batch
sbt> run analytics.spark.job.WordCount
```

## Spark Streaming
```
sbt> project spark-streaming
sbt> run analytics.spark.job.WordCount
```

Once the application is started, files added to `./spark-streaming/target/scala-2.10/classes` will automatically be processed.

## Scalding
```
sbt> project scalding
sbt> run analytics.scalding.job.WordCount --local
```
