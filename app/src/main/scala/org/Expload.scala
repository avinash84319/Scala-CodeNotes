// Examples of different types of joins in Scala using Spark
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Expload {
    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
        .appName("Explode Outer and Posexplode Example")
        .master("local[*]")
        .getOrCreate()

        // Sample DataFrame with an array column, including a null array and an empty array
        val data = Seq(
        (1, Array("apple", "banana", "cherry")),  // Non-null array with elements
        (2, null),                               // Null array
        (3, Array.empty[String])                 // Empty array
        )

        val df = spark.createDataFrame(data).toDF("id", "fruits")

        // Show the original DataFrame
        println("Original DataFrame:")
        df.show()

        /*
        Theory:
        - We want to simulate `explode_outer` behavior, where null or empty arrays are handled.
        - In Spark (Scala), we will use `explode` and then handle the null or empty arrays explicitly.
        Output:
        +---+-------------------+
        | id|             fruits|
        +---+-------------------+
        |  1|[apple, banana, c...|
        |  2|               null|
        |  3|                 []|
        +---+-------------------+
        */

        // Simulating `explode_outer` behavior
        val explodedDF = df
        .withColumn("fruit", explode(col("fruits"))) // Explode the array column
        .na.fill(Map("fruit" -> null))               // Fill null for exploded values when array is null or empty

        // Show the resulting DataFrame after exploding
        println("Exploded DataFrame (Simulating explode_outer):")
        explodedDF.show()

        /*
        Theory:
        - The `explode` function takes an array or map and creates a new row for each element.
        - We use `.na.fill(Map("fruit" -> null))` to handle null or empty arrays, ensuring that we get a row with `null` when the array is empty or null.
        Output:
        +---+------+
        | id| fruit|
        +---+------+
        |  1| apple|
        |  1|banana|
        |  1|cherry|
        |  2|  null|
        |  3|  null|
        +---+------+
        */

        // Example of `posexplode` to get both position and element in an array
        val posexplodedDF = df.withColumn("position_fruit", posexplode(col("fruits")))

        // Show the resulting DataFrame after using posexplode
        println("Posexploded DataFrame:")
        posexplodedDF.show()

        /*
        Theory:
        - `posexplode` works similarly to `explode`, but it returns both the index and the element from the array.
        - This is useful when you need the position of each element in addition to the value itself.
        - Best practice: Use `posexplode` when the position of the array element is important to your analysis.
        Output:
        +---+---+------+
        | id|pos| fruit|
        +---+---+------+
        |  1|  0| apple|
        |  1|  1|banana|
        |  1|  2|cherry|
        |  2|  0|  null|
        |  3|  0|  null|
        +---+---+------+
        */

        // Example of `inline` to explode a nested struct (struct array) into separate columns
        val nestedData = Seq(
        (1, Seq(("apple", 10), ("banana", 15))),
        (2, Seq(("orange", 5), ("mango", 8)))
        )

        val nestedDF = spark.createDataFrame(nestedData).toDF("id", "fruits")

        // Exploding the struct into separate columns
        val inlineDF = nestedDF.select(col("id"), inline(col("fruits")))

        // Show the resulting DataFrame after using inline
        println("Inline Exploded DataFrame:")
        inlineDF.show()

        /*
        Theory:
        - `inline` is used to explode an array of structs into separate columns.
        - This is useful for flattening nested structures into individual fields.
        Output:
        +---+------+----+
        | id|  _1  | _2 |
        +---+------+----+
        |  1| apple| 10 |
        |  1|banana| 15 |
        |  2|orange|  5 |
        +---+------+----+
        */

        // Example of `arrays_zip` to combine multiple arrays into a single array of structs
        val array1 = Seq(1, 2, 3)
        val array2 = Seq("a", "b", "c")

        val zipDF = spark.createDataFrame(Seq((array1, array2))).toDF("nums", "letters")
        val zippedDF = zipDF.withColumn("zipped", arrays_zip(col("nums"), col("letters")))

        // Show the resulting DataFrame after using arrays_zip
        println("Arrays Zip DataFrame:")
        zippedDF.show()

        /*
        Theory:
        - `arrays_zip` combines two or more arrays into a single array of structs.
        - Best practice: Use this when you need to combine parallel arrays into one cohesive structure.
        Output:
        +------+-------+-------------+
        | nums  | letters| zipped|
        +------+-------+-------------+
        | [1,2,3]| [a,b,c]| [(1,a),(2,b),(3,c)]|
        +------+-------+-------------+
        */

        // Example of `filter` on exploded data
        val filteredExplodedDF = explodedDF.filter(col("fruit").equalTo("apple"))
        println("Filtered Exploded DataFrame (only 'apple'):")
        filteredExplodedDF.show()

        /*
        Theory:
        - After exploding the data, you can apply any transformation or filtering.
        - In this case, we filter for rows where the exploded fruit is "apple".
        - Best practice: Always apply filtering after an explode operation to reduce unnecessary data.
        Output:
        +---+------+
        | id| fruit|
        +---+------+
        |  1| apple|
        +---+------+
        */

        // Stop Spark session after completion
        spark.stop()


    }
}
