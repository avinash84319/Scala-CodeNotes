// Examples of different types of joins in Scala using Spark
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.broadcast
import org.apache.spark.sql.types._

object SpecialJoins {
    def main(args: Array[String]): Unit = {

        // Initialize Spark Session
        val spark = SparkSession.builder()
            .appName("Broadcast Joins Example")
            .master("local[*]") // Run locally with all available cores
            .getOrCreate()

        import spark.implicits._

        // Create a large DataFrame (simulates a large dataset)
        val largeTable = spark.range(1, 100000000) // "id" column from 1 to 100,000,000

        // Create a small DataFrame (simulates a small lookup table)
        val smallTableData = Seq(
            Row(1, "Gold"),
            Row(2, "Silver"),
            Row(3, "Bronze")
        )

        val smallTableSchema = StructType(Seq(
            StructField("id", IntegerType, nullable = false),
            StructField("medal", StringType, nullable = true)
        ))

        val smallTable = spark.createDataFrame(
            spark.sparkContext.parallelize(smallTableData),
            smallTableSchema
        )

        // Perform a regular join (no broadcast)
        val regularJoin = largeTable.join(smallTable, Seq("id"), "inner")

        // Show the query plan for the regular join
        println("Query Plan for Regular Join:")
        regularJoin.explain()

        /*
        Explanation:
        - Regular joins use a "SortMergeJoin" when both datasets are large.
        - Shuffles occur to redistribute data so matching keys are on the same executor.
        - This approach is expensive for large datasets because of the shuffle and sorting steps.
        */

        // Perform a broadcast join
        val broadcastJoin = largeTable.join(broadcast(smallTable), Seq("id"), "inner")

        // Show the query plan for the broadcast join
        println("Query Plan for Broadcast Join:")
        broadcastJoin.explain()

        /*
        Explanation:
        - Broadcast joins use "BroadcastHashJoin".
        - Spark broadcasts the small dataset to all executors, eliminating the need for shuffles.
        - Each executor can independently perform the join with the broadcasted data.
        - This is significantly faster for large-small joins compared to regular joins.
        */

        // Show the results of the broadcast join
        broadcastJoin.show()

        // Configuring Auto Broadcast Threshold
        // Set the threshold for automatic broadcasting to 100 MB
        spark.conf.set("spark.sql.autoBroadcastJoinThreshold", 104857600)

        // Disable automatic broadcasting
        spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)

        /*
        Configuration Explanation:
        - `spark.sql.autoBroadcastJoinThreshold` controls the table size threshold for automatic broadcasting.
        - Default value: 10 MB.
        - Setting it to -1 disables auto-broadcasting.
        */

        // Shuffle Hash Join Example
        spark.conf.set("spark.sql.join.preferSortMergeJoin", "false")
        val shuffleHashJoin = largeTable.join(smallTable.hint("shuffle"), Seq("id"), "inner")
        println("Query Plan for Shuffle Hash Join:")
        shuffleHashJoin.explain()

        /*
        Explanation:
        - Used when both datasets are large, and broadcasting isn't feasible.
        - Data is repartitioned based on the join key (hash partitioning).
        - A hash table is created on one side, and the join happens using this table.
        - This approach is more expensive than broadcast joins due to shuffling.
        */

        // Sort Merge Join Example
        spark.conf.set("spark.sql.join.preferSortMergeJoin", "true")
        val sortMergeJoin = largeTable.join(smallTable, Seq("id"), "inner")
        println("Query Plan for Sort Merge Join:")
        sortMergeJoin.explain()

        /*
        Explanation:
        - Default join type for large dataset joins.
        - Data is repartitioned and sorted on the join key.
        - Merging happens on sorted data, making it efficient for equi-joins.
        */

        // Cartesian Join Example
        val cartesianJoin = largeTable.crossJoin(smallTable)
        println("Query Plan for Cartesian Join:")
        cartesianJoin.explain()

        /*
        Explanation:
        - Cartesian join generates all possible combinations of rows from both datasets.
        - Expensive and often avoided unless necessary.
        - Used for non-equi joins or when no join condition is specified.
        */

        // Broadcast Nested Loop Join Example
        val nestedLoopJoin = largeTable.join(broadcast(smallTable), largeTable("id") < smallTable("id"), "inner")
        println("Query Plan for Broadcast Nested Loop Join:")
        nestedLoopJoin.explain()

        /*
        Explanation:
        - Useful for non-equi joins.
        - The smaller dataset is broadcast to all executors, and each executor processes the join independently.
        */

        // Show the results of various joins
        println("Results of Broadcast Join:")
        broadcastJoin.show()

        println("Results of Shuffle Hash Join:")
        shuffleHashJoin.show()

        println("Results of Sort Merge Join:")
        sortMergeJoin.show()

        println("Results of Cartesian Join:")
        cartesianJoin.show()

        println("Results of Broadcast Nested Loop Join:")
        nestedLoopJoin.show()

        // Stop the Spark session
        spark.stop()
    }
}
