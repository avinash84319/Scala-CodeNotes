package org
import org.apache.spark.sql.SparkSession
import org.RepartionvsColalace

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Spark SQL basic example")
      .config("spark.master", "local")
      .getOrCreate()

    val df = spark.read.option("header", "true").csv("/home/avinash/ScalaSpark/JsonToReport/app/src/main/resources/Matches.csv")

    println(s"Scala version: ${util.Properties.versionString}")

    // exploring sql methods of spark in scala

    //display the schema
    df.printSchema()
    //display the data
    df.show()
    //display the data in tabular format
    df.show(10, false)

    // for row operations

    //selecting a row
    df.head(1).foreach(println)
    //filtering rows
    df.filter("HomeTeam = 'Arsenal'").show()
    //filtering rows with multiple conditions
    df.filter("HomeTeam = 'Arsenal' AND AwayTeam = 'Chelsea'").show()
    //where clause
    df.where("HomeTeam = 'Arsenal' AND AwayTeam = 'Chelsea'").show()
    //droping rows with null values
    df.na.drop().show()
    //filling null values
    df.na.fill("No Value").show()
    //removing duplicate rows
    df.dropDuplicates().show()
    //sorting rows
    df.orderBy("HomeTeam").show()
    //limiting rows
    df.limit(5).show()
    //adding rows to the dataframe
    df.union(df).show()
    //selecting rows at index
    df.collect()(100)


    // for columns

    //selecting a column
    df.select("MatchDate").show()
    //selecting multiple columns
    df.select("MatchDate","HomeTeam","AwayTeam").show()
    //reaname columns
    df.withColumnRenamed("HomeTeam","Home").show()
    //droping columns
    df.drop("HomeTeam").show()
    //adding columns
    df.withColumn("NewColumn",df("HomeTeam")).show()
    //adding columns with expression
    df.withColumn("NewColumn",df("HomeTeam") + df("AwayTeam")).show()
    //casting columns
    df.withColumn("NewColumn",df("HomeTeam").cast("Int")).show()
    //filtering columns
    df.filter(df("HomeTeam") === "Arsenal").show()
    //apply transformations on columns
    df.withColumn("NewColumn",df("HomeTeam") + " vs " + df("AwayTeam")).show()
    //adding columns with constant value
    df.withColumn("NewColumn",org.apache.spark.sql.functions.lit("Arsenal")).show()
    //checking null values in columns
    df.na.fill("No Value",Array("HomeTeam")).show()
    //updating column values based on condition
    df.withColumn("HomeTeam",org.apache.spark.sql.functions.when(df("HomeTeam") === "Arsenal","Arsenal FC").otherwise(df("HomeTeam"))).show()
    //updating column values based on multiple conditions
    df.withColumn("HomeTeam",org.apache.spark.sql.functions.when(df("HomeTeam") === "Arsenal","Arsenal FC").when(df("HomeTeam") === "Chelsea","Chelsea FC").otherwise(df("HomeTeam"))).show()
    //Column-wise Aggregation
    df.select(org.apache.spark.sql.functions.sum("FTHome").alias("Total Goals")).show()
    //Grouping
    df.groupBy("HomeTeam").count().show()
    //Grouping with Aggregation
    df.groupBy("HomeTeam").agg(org.apache.spark.sql.functions.sum("FTHome").alias("Total Goals")).show()
    //Grouping with multiple Aggregations
    df.groupBy("HomeTeam").agg(org.apache.spark.sql.functions.sum("FTHome").alias("Total Goals"),org.apache.spark.sql.functions.avg("FTHome").alias("Average Goals")).show()
    //applying user defined functions
    val addOne = org.apache.spark.sql.functions.udf((x:Int) => x+1)

    //dividing the data into 2 partitions to practice joins
    val df1 = df.limit(10)
    val df2 = df.limit(10)

    //join df1 and df2
    df1.join(df2,df1("HomeTeam") === df2("HomeTeam")).show()

    //left join
    df1.join(df2,df1("HomeTeam") === df2("HomeTeam"),"left").show()

    //right join
    df1.join(df2,df1("HomeTeam") === df2("HomeTeam"),"right").show()

    //outer join
    df1.join(df2,df1("HomeTeam") === df2("HomeTeam"),"outer").show()

    //cross join
    df1.crossJoin(df2).show()

    spark.stop()

  }
}
