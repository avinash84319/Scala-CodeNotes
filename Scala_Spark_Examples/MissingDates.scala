package org

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ArrayBuffer

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Main {
    def main(args: Array[String]): Unit = {


        val spark = SparkSession.builder()
            .appName("Spark Dataframe Example")
            .config("spark.master", "local")
            .getOrCreate()
        
        val data = Seq(
            ("2023-07-04", "Order1"), 
            ("2023-07-10", "Order2"), 
            ("2023-06-28", "Order3"), 
            ("2023-08-15", "Order4"), 
            ("2023-07-04", "Order5")
        )
        
        var df = spark.createDataFrame(data).toDF("Date","order")

        df = df.withColumn("Date",col("Date").cast(DateType))

        // creating columns like year,month,date seperatly
        df = df.withColumn("Year",substring(col("Date"),1,4))
                        .withColumn("Month",substring(col("Date"),6,2))
                        .withColumn("Day",substring(col("Date"),9,2))
                        .orderBy("Date")

        // converting Dates to different format
        df = df.withColumn("Date",concat(col("Day"),lit("-"),col("Month"),lit("-"),col("Year")))

        df = df.withColumn("Date",col("Date").cast(StringType)).select("Date").distinct()

        // finding missing dates
        val minDate = df.select(min("Date")).first().getAs[String](0)
        val maxDate = df.select(max("Date")).first().getAs[String](0)
        
        println("\n"*5)
        println(minDate,maxDate)
        println("\n"*5)

        def rangeDates(minDate: String, maxDate: String): Array[String] = {
                val dates = ArrayBuffer[String]()

                // Parse input dates
                val min = LocalDate.parse(minDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                val max = LocalDate.parse(maxDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))

                // Generate the date range
                var currentDate = min
                while (!currentDate.isAfter(max)) {
                    dates.append(currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    currentDate = currentDate.plusDays(1)
                }
    
                dates.toArray
        }

        var allDates = rangeDates(maxDate,minDate)

        println(allDates)
        println("\n"*5)

        // allDates = Seq(
        //         (1,allDates)
        // )

        var nammametro = Seq(        // take new variable names as scala is shit enough that it
            (1,allDates)            // doesnt allow to put some other type in same variabl name
        )

        var allDatesDF = spark.createDataFrame(nammametro).toDF("shit","Dates")

        // expload the array
        allDatesDF = allDatesDF.withColumn("Dates",explode(col("Dates"))).select("Dates")

        // df.show()
        // allDatesDF.show()

        // using leftanti join to get missing dates
        var missingDates = allDatesDF.join(df,allDatesDF("Dates") === df("Date"),"leftanti")

        missingDates.show()
        
    }
}