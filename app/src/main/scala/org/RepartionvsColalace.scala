package org

import org.apache.spark.sql.SparkSession

object RepartionvsColalace {
    def main(args: Array[String]): Unit = {
        // Start a Spark session configured for local execution
        val spark = SparkSession.builder()
            .appName("Repartition and Coalesce Demo")
            .master("local[*]") // Use all available cores
            .getOrCreate()

        // Access the SparkContext from the SparkSession
        val sc = spark.sparkContext

        // Create an RDD with numbers from 1 to 10 million
        val numbers = sc.parallelize(1 to 10000000)

        // Check the number of partitions initially
        println(s"Initial number of partitions: ${numbers.partitions.length}")

        // Repartition the RDD to 2 partitions
        val repartitionedNumbers = numbers.repartition(2)

        // Perform an action (e.g., count) to trigger the computation
        println("Counting repartitioned RDD:")
        val repartitionedCount = repartitionedNumbers.count() // This will incur a shuffle
        println(s"Repartitioned count: $repartitionedCount")

        // Coalesce the RDD to 2 partitions
        val coalescedNumbers = numbers.coalesce(2)

        // Perform an action (e.g., count) to trigger the computation
        println("Counting coalesced RDD:")
        val coalescedCount = coalescedNumbers.count() // This avoids a shuffle
        println(s"Coalesced count: $coalescedCount")

        // Observations:
        // 1. Repartition redistributes data evenly, which involves a shuffle and can be expensive.
        // 2. Coalesce reduces the number of partitions by combining them without a shuffle.
        // 3. Coalesce is faster for reducing partitions but does not guarantee evenly sized partitions.
        // 4. Coalesce behaves like a repartition when increasing the number of partitions.

        // Example: Check partitions for both RDDs
        println(s"Partitions after repartition: ${repartitionedNumbers.partitions.length}")
        println(s"Partitions after coalesce: ${coalescedNumbers.partitions.length}")

        // Sleep to allow inspection of Spark UI (if needed)
        Thread.sleep(10000)

        // Stop the Spark session
        spark.stop()
    }
}
