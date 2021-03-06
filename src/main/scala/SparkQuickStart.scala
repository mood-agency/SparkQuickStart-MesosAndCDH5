import org.apache.hadoop.yarn.api.protocolrecords.ApplicationsRequestScope
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.log4j.Logger
import org.apache.log4j.Level

/**
 * Created by Favio on 14/05/15.
 */
object SparkQuickStart {

//  Note that applications should define a main() method instead of extending
//  scala.App. Subclasses of scala.App may not work correctly

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val conf = new SparkConf()
      //      .setMaster("local")
      .setMaster("mesos://master.mcbo.mood.com.ve:5050")
      .setAppName("Jaccard Similarity")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

//   1. BASICS

//    Spark’s primary abstraction is a distributed collection of items called a
//    Resilient Distributed Dataset (RDD). RDDs can be created from Hadoop InputFormats
//    (such as HDFS files) or by transforming other RDDs. Let’s make a new RDD from the
//    text of the README file in the Spark source directory:

    val textFile = sc.textFile("hdfs://master.mcbo.mood.com.ve:8020/README.md")

//    RDDs have actions, which return values, and transformations, which return
//    pointers to new RDDs. Let’s start with a few actions:

//    Because we are not running this on the spark-shell we'll have to create some
//    momentary variables in order to see the output

    val res1 = textFile.count()
    val res2 = textFile.first()

    println(res1,res2)

//    Now let’s use a transformation. We will use the filter transformation to return a
//    new RDD with a subset of the items in the file.

    val linesWithSpark = textFile.filter(line => line.contains("Spark"))

//    We can chain together transformations and actions:

    val res4 = textFile.filter(line => line.contains("Spark")).count()
    println(res4)

//    This is the same as
    val res5 = linesWithSpark.count()
    println(res5)

//    2. More on RDD Operations

//    RDD actions and transformations can be used for more complex computations. Let’s
//    say we want to find the line with the most words:

    val res6 = textFile.map(line => line.split(" ").size).reduce((a,b) => if (a > b) a else b)
    println(res6)

//    This first maps a line to an integer value, creating a new RDD. reduce is called on
//    that RDD to find the largest line count. The arguments to map and reduce are Scala
//    function literals (closures), and can use any language feature or Scala/Java
//    library. For example, we can easily call functions declared elsewhere.
//    We’ll use Math.max() function to make this code easier to understand:

    val res7 =textFile.map(line => line.split(" ").size).reduce((a, b) => Math.max(a, b))
    println(res7)

//    One common data flow pattern is MapReduce, as popularized by Hadoop. Spark can
//    implement MapReduce flows easily:

    val wordCounts = textFile.flatMap(line => line.split(" ")).map(word => (word, 1))
                    .reduceByKey((a, b) => a + b)

//    Here, we combined the flatMap, map and reduceByKey transformations to compute the
//    per-word counts in the file as an RDD of (String, Int) pairs. To collect the word
//    counts in our shell, we can use the collect action:

    val res8 = wordCounts.collect()
    println(res8)

//    3. Caching

//    Spark also supports pulling data sets into a cluster-wide in-memory cache. This is
//    very useful when data is accessed repeatedly, such as when querying a small “hot”
//    dataset or when running an iterative algorithm like PageRank. As a simple example,
//    let’s mark our linesWithSpark dataset to be cached:

//    Cache the val
    linesWithSpark.cache()

//    First call to count ("normal time")
    val res9 =linesWithSpark.count()
    println(res9)

//    Now that it was cached this process should be faster (you won't notice until you're
//    running this with a big file)
    val res10 = linesWithSpark.count()
    println(res10)

//    It may seem silly to use Spark to explore and cache a very short text file. The
//    interesting part is that these same functions can be used on very large data sets,
//    even when they are striped across tens or hundreds of nodes.

//    4. Self-Contained Applications

//    We've been doing a self contain application from the beginning so we'll just
//    copy the code that was used in the tutorial here to complete the QuickStart

    val logData = textFile.cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()

    println(s"Line with a: $numAs, Lines with b: $numBs")

    sc.stop()

  }
}
