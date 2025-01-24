package org

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Main {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Spark Dataframe Example")
      .config("spark.master", "local")
      .getOrCreate()
    

    val write_path = args.head
    val file_paths = args.tail

    println("File paths are: ")
    file_paths.foreach(println)

    println("File path to write is: ")
    println(write_path)

    val dfs = file_paths.map(spark.read.option("header", "true").csv(_))

    // dfs.foreach(_.show())

    // checking the schema
    // dfs.map(_.printSchema())

    //checing if both date and club and Match date and HomeTeam are in schema
    val df1 = dfs(0)
    val df2 = dfs(1)
    
    if(df1.columns.contains("date") && df1.columns.contains("club") && df2.columns.contains("MatchDate") && df2.columns.contains("HomeTeam")){

        df1.join(df2,df2("HomeTeam") === df1("club") && df2("MatchDate") === df1("date"),"inner").write.parquet(write_path)
        println("Dataframes joined and written to the path")

    }else{
        println("Required columns are not present in the dataframes")
    }

  }
}