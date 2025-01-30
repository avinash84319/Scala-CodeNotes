package org

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
// import org.apache.spark.mllib.evaluation.RegressionMetrics

// data present here
// https://drive.google.com/drive/folders/1TIdHXRvbMJHkSyix3q2sDOSwaUlP8isZ?usp=sharing


object task1{

    def task1_cal(args: Array[String]): Unit = {
        
        val spark = SparkSession.builder()
            .appName("Task1")
            .master("local[*]")
            .getOrCreate()

        val datapath = "/home/avinash/ScalaSpark/ScalaPractice/app/src/main/resources/task1-updated.csv"


        var df = spark.read
            .option("header", "true")
            .option("inferSchema", "true")
            .csv(datapath)

        // df.show()
        /*  data is like this 
        +---------+----+--------------------+---------------+-------------------+-----+------+--------------+------------+-------+------------+--------+
        |  article|  dc|               class|          brick|article_description|brand|region|         state|        city|pincode|forecast_qty|sold_qty|
        +---------+----+--------------------+---------------+-------------------+-----+------+--------------+------------+-------+------------+--------+
        |494410491|R696|MEDIUM SCREEN 40 ...|LED TV 40 TO 54|TCL 50 4K TV 50P755|  TCL|  West|   Maharashtra|    Bhiwandi| 421302|         1.0|     0.0|
        |494410491|SJ48|MEDIUM SCREEN 40 ...|LED TV 40 TO 54|TCL 50 4K TV 50P755|  TCL| South|     Karnataka|     Dharwad| 580011|         1.0|     0.0|
        |494410491|SLOR|MEDIUM SCREEN 40 ...|LED TV 40 TO 54|TCL 50 4K TV 50P755|  TCL| North|   Uttarakhand|    Dehradun| 248001|         1.0|     0.0|
        |494410497|SC28|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| North| Uttar Pradesh|     Lucknow| 226401|         7.0|     0.0|
        |494410497|S597|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  East|         Assam|    Guwahati| 781031|         1.0|     0.0|
        |494410497|R396|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| South|     Karnataka|   Bangalore| 562123|        20.0|     0.0|
        |494410497|SE40|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| South|     Telangana|   Hyderabad| 501401|        27.0|     0.0|
        |494410497|SLGC|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  West|   Maharashtra|        Pune| 412216|        12.0|     0.0|
        |494410497|R201|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  East|        Orissa|     Cuttack| 754002|         3.0|     0.0|
        |494410497|S0UG|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| South|Andhra Pradesh|  Gannavaram| 521107|         4.0|     0.0|
        |494410497|SACU|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  West|   Maharashtra|      Nagpur| 441122|         6.0|     0.0|
        |494410497|SJ48|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| South|     Karnataka|     Dharwad| 580011|         6.0|     0.0|
        |494410497|SLKO|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  East|   West Bengal|      Howrah| 711310|         6.0|     0.0|
        |494410497|R696|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  West|   Maharashtra|    Bhiwandi| 421302|        39.0|     0.0|
        |494410497|S0GZ|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  West|Madhya Pradesh|       Sawer| 453771|         1.0|     0.0|
        |494410497|S573|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  West|   Chattisgarh|      Raipur| 493111|         1.0|     0.0|
        |494410497|SJ99|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| South|    Tamil Nadu|Kancheepuram| 602105|        14.0|     0.0|
        |494410497|S0IC|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| South|     Karnataka|    Hosakote| 562114|        15.0|     0.0|
        |494410497|R810|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL| North|     Rajasthan|      Jaipur| 302022|        12.0|     0.0|
        |494410497|SLLN|MEDIUM SCREEN 40 ...|LED TV 40 TO 54| TCL 50 QLED 50C655|  TCL|  East|   West Bengal|    Siliguri| 735135|         3.0|     0.0|
        +---------+----+--------------------+---------------+-------------------+-----+------+--------------+------------+-------+------------+--------+
        */

        // random values in the column sold_qty as everything was zero
        // df = df.withColumn("sold_qty", rand()*20)

        // df.show()

        val levels = args
        
        val output = df.groupBy(col("article")+:levels.map(col):_*)
            .agg(
                sum("forecast_qty").as("forecast_qty"),
                sum("sold_qty").as("sold_qty")
            )
            .withColumn("error", col("forecast_qty") - col("sold_qty"))
            .groupBy(levels.map(col):_*)
            .agg(
                sqrt(sum(pow(col("error"), 2))).as("rms"),
                sum(abs(col("error"))).as("mae"),
                sum(abs(col("error")/col("sold_qty"))).as("mape")
            )

        output.show()


    }
}