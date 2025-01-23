package org

import org.apache.spark.sql.SparkSession

object QueryPlan {
    def main(args: Array[String]): Unit = {
        // Create a SparkSession configured for local execution
        val spark = SparkSession.builder()
            .appName("Reading and Interpreting Query Plans")
            .master("local[*]") // Use all available CPU cores
            .getOrCreate()

        // Create the first dataset: a range of numbers from 1 to 10 million
        val ds1 = spark.range(1, 10000000) 
        // `ds1` is a DataFrame that represents a range of numbers from 1 to 10 million (step = 1).
        // Query Plan:
        // *(1) Range (1, 10000000, step=1, splits=6)
        // Spark creates a range of numbers divided into 6 partitions (default based on the environment).

        // Create the second dataset: a range of numbers from 1 to 10 million with a step of 2
        val ds2 = spark.range(1, 10000000, 2)
        // `ds2` is another DataFrame, similar to `ds1`, but it contains only odd numbers (step = 2).
        // Query Plan:
        // *(1) Range (1, 10000000, step=2, splits=6)
        // Spark creates a range of numbers with a step of 2, also divided into 6 partitions.

        // Repartition ds1 into 7 partitions
        val ds3 = ds1.repartition(7)
        // `repartition` involves a shuffle to redistribute data into 7 partitions evenly.
        // Query Plan:
        // Exchange RoundRobinPartitioning(7)
        // +- *(1) Range (1, 10000000, step=1, splits=6)
        // A shuffle occurs to evenly redistribute data into 7 partitions.

        // Repartition ds2 into 9 partitions
        val ds4 = ds2.repartition(9)
        // Similar to above, this involves a shuffle to redistribute data into 9 partitions.
        // Query Plan:
        // Exchange RoundRobinPartitioning(9)
        // +- *(1) Range (1, 10000000, step=2, splits=6)
        // This shuffle rearranges data into 9 partitions.

        // Transform ds3 by multiplying the `id` column by 5
        val ds5 = ds3.selectExpr("id * 5 as id")
        // Applies a transformation to create a new column `id`, which is the original `id` multiplied by 5.
        // Query Plan:
        // *(2) Project [(id#30L * 5) AS id#36L]
        // +- Exchange RoundRobinPartitioning(7)
        //    +- *(1) Range (1, 10000000, step=1, splits=6)
        // The data is transformed after repartitioning into 7 partitions.

        // Join ds5 and ds4 on the `id` column
        val joined = ds5.join(ds4, "id")
        // Performs an inner join between `ds5` and `ds4` on the `id` column.
        // Query Plan:
        // *(6) SortMergeJoin [id#36L], [id#32L], Inner
        // :- *(3) Sort [id#36L ASC NULLS FIRST], false, 0
        // :  +- Exchange hashpartitioning(id#36L, 200)
        // :     +- *(2) Project [(id#30L * 5) AS id#36L]
        // :        +- Exchange RoundRobinPartitioning(7)
        // :           +- *(1) Range (1, 10000000, step=1, splits=6)
        // +- *(5) Sort [id#32L ASC NULLS FIRST], false, 0
        //    +- Exchange hashpartitioning(id#32L, 200)
        //       +- Exchange RoundRobinPartitioning(9)
        //          +- *(4) Range (1, 10000000, step=2, splits=6)
        // - Both datasets are repartitioned and sorted by the join key (`id`) before performing the join.
        // - Multiple shuffles occur, including hash partitioning and sorting, to align the data for the join.

        // Select the sum of the `id` column from the joined dataset
        val sum = joined.selectExpr("sum(id)")
        // Computes the sum of the `id` column across all partitions.
        // Query Plan:
        // *(7) HashAggregate(keys=[], functions=[sum(id#36L)])
        // +- Exchange SinglePartition
        //    +- *(6) HashAggregate(keys=[], functions=[partial_sum(id#36L)])
        //       +- *(6) Project [id#36L]
        //          +- *(6) SortMergeJoin [id#36L], [id#32L], Inner
        // - Partial sums are calculated within each partition first.
        // - The partial sums are then aggregated into a single partition to compute the final result.

        sum.explain()
        // Explains the physical plan for the computation.
        // Use this to analyze Spark's execution and identify performance bottlenecks.
    }
}
