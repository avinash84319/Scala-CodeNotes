// Examples of different types of joins in Scala using Spark
import org.apache.spark.sql.SparkSession


object Joins {
    def main(args: Array[String]): Unit = {

        // Create a Spark session
        val spark = SparkSession.builder.appName("JoinExamples").getOrCreate()
        import spark.implicits._

        // Create the first dataset (kids)
        val kids = Seq(
            ("Alice", 10),
            ("Bob", 12),
            ("Charlie", 11)
        ).toDF("name", "age")

        // Create the second dataset (teams)
        val teams = Seq(
            ("Alice", "Soccer"),
            ("Bob", "Basketball"),
            ("David", "Baseball")
        ).toDF("name", "team")

        // Inner Join
        val innerJoin = kids.join(teams, kids("name") === teams("name"), "inner")
        innerJoin.show()
        /*
        Output:
        +-----+---+----------+
        | name|age|      team|
        +-----+---+----------+
        |Alice| 10|    Soccer|
        |  Bob| 12|Basketball|
        +-----+---+----------+
        */

        // Left Outer Join
        val leftOuterJoin = kids.join(teams, kids("name") === teams("name"), "left_outer")
        leftOuterJoin.show()
        /*
        Output:
        +-----+---+----------+
        | name|age|      team|
        +-----+---+----------+
        |Alice| 10|    Soccer|
        |  Bob| 12|Basketball|
        |Charlie| 11|      null|
        +-----+---+----------+
        */

        // Right Outer Join
        val rightOuterJoin = kids.join(teams, kids("name") === teams("name"), "right_outer")
        rightOuterJoin.show()
        /*
        Output:
        +-----+----+----------+
        | name| age|      team|
        +-----+----+----------+
        |Alice|  10|    Soccer|
        |  Bob|  12|Basketball|
        |David|null|  Baseball|
        +-----+----+----------+
        */

        // Full Outer Join
        val fullOuterJoin = kids.join(teams, kids("name") === teams("name"), "outer")
        fullOuterJoin.show()
        /*
        Output:
        +-----+----+----------+
        | name| age|      team|
        +-----+----+----------+
        |Alice|  10|    Soccer|
        |  Bob|  12|Basketball|
        |Charlie| 11|      null|
        |David|null|  Baseball|
        +-----+----+----------+
        */

        // Cross Join
        val crossJoin = kids.crossJoin(teams)
        crossJoin.show()
        /*
        Output:
        +-----+---+-----+----------+
        | name|age| name|      team|
        +-----+---+-----+----------+
        |Alice| 10|Alice|    Soccer|
        |Alice| 10|  Bob|Basketball|
        |Alice| 10|David|  Baseball|
        |  Bob| 12|Alice|    Soccer|
        |  Bob| 12|  Bob|Basketball|
        |  Bob| 12|David|  Baseball|
        |Charlie| 11|Alice|    Soccer|
        |Charlie| 11|  Bob|Basketball|
        |Charlie| 11|David|  Baseball|
        +-----+---+-----+----------+
        */

        // Left Anti Join
        val leftAntiJoin = kids.join(teams, kids("name") === teams("name"), "left_anti")
        leftAntiJoin.show()
        /*
        Output:
        +-----+---+
        | name|age|
        +-----+---+
        |Charlie| 11|
        +-----+---+
        */

        // Left Semi Join
        val leftSemiJoin = kids.join(teams, kids("name") === teams("name"), "left_semi")
        leftSemiJoin.show()
        /*
        Output:
        +-----+---+
        | name|age|
        +-----+---+
        |Alice| 10|
        |  Bob| 12|
        +-----+---+
        */

        // Self Join
        val selfJoin = kids.as("k1").join(kids.as("k2"), $"k1.age" === $"k2.age")
        selfJoin.show()
        /*
        Output:
        +-----+---+-----+---+
        | name|age| name|age|
        +-----+---+-----+---+
        |Alice| 10|Alice| 10|
        |  Bob| 12|  Bob| 12|
        |Charlie| 11|Charlie| 11|
        +-----+---+-----+---+
        */

        // Close the Spark session
        spark.stop()
    }
}
