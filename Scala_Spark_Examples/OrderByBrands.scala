package org

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
// import org.apache.spark.mllib.evaluation.RegressionMetrics


// data present here
// https://drive.google.com/drive/folders/1TIdHXRvbMJHkSyix3q2sDOSwaUlP8isZ?usp=sharing

object task2{

    def task2_cal(args: Array[String]): Unit = {
        
        val spark = SparkSession.builder()
            .appName("Task1")
            .master("local[*]")
            .getOrCreate()

        val datapath = "src/main/resources/task2.csv"


        val df = spark.read
            .option("header", "true")
            .option("inferSchema", "true")
            .csv(datapath)
            .orderBy("product_rank")

        // df.show()
        /* data is like this
        +--------------------+------------+------------------+------+------+---------+------------------+--------+-------------------+------+-----+------------------+
        |           productid|product_rank|         brandname|l1code|l2code|  brickid|           revenue|quantity|distinct_purchasers|clicks|views|          discount|
        +--------------------+------------+------------------+------+------+---------+------------------+--------+-------------------+------+-----+------------------+
        |     469581866_black|           1|              nike|  8302|830207|830207008|45210.350000000006|      15|                 14|   337| 9909|24.000000000000004|
        |    6005407580_multi|           2|     whp-jewellers|  8303|830306|830306009|               0.0|       0|                  0|    12|  480| 9.999999999999996|
        |     465654605_white|           3|              puma|  8302|830207|830207010|24958.730000000003|      17|                 17|   295|11426|              70.0|
        |    4937167200_multi|           4|          cetaphil|  8303|830314|830314001|             923.0|       3|                  3|    31|  256|10.999999999999996|
        |     466195911_black|           5|              puma|  8302|830216|830216014|           4269.31|       7|                  7|   139| 5668| 56.99999999999999|
        |     465738251_black|           6|              puma|  8302|830207|830207008|23316.260000000002|      15|                 15|   308|11970|              70.0|
        |      442528942_navy|           7|               gap|  8302|830216|830216013|           7947.95|       5|                  5|   241| 7939|              50.0|
        |     442528942_white|           8|               gap|  8302|830216|830216013|            6298.0|       4|                  4|   109| 4958|              50.0|
        |     469551545_black|           9|              nike|  8302|830207|830207008|            9708.0|       4|                  4|   230| 8161|30.999999999999993|
        |      465738283_grey|          10|              puma|  8302|830207|830207008|           17434.0|      12|                 12|   382|10177|              70.0|
        |    6005289300_multi|          11|bangalore-refinery|  8302|830206|830206008|           23949.0|       3|                  3|    49|  512| 10.00003382274851|
        |     469581846_black|          12|              nike|  8302|830207|830207008|          11335.34|       5|                  4|    62| 3213|29.000000000000004|
        |   442306732_ltgreen|          13|               gap|  8302|830212|830212003|           3998.12|       6|                  6|    37| 2282|              70.0|
        |   4900556590_marble|          14|             lakme|  8303|830312|830312002|               0.0|       0|                  0|     2|   21|              10.0|
        |      467347490_pink|          15|        jack-jones|  8302|830216|830216014|            841.68|       1|                  1|    64| 3327|               0.0|
        |     469066375_black|          16|              puma|  8302|830216|830216003|2992.3599999999997|       3|                  2|    84| 1714| 60.00000000000001|
        |     469177708_black|          17|              puma|  8303|830316|830316018| 590.3999999999999|       1|                  1|    18| 2302| 55.00000000000001|
        |     466928517_cream|          18|           bullmer|  8302|830216|830216027|          11165.05|      11|                 11|   395| 5861|58.537512504168056|
        |     469615742_white|          19|              nike|  8302|830207|830207010|               0.0|       0|                  0|    77| 2529|29.000000000000004|
        |442295913_olivegreen|          20|               gap|  8302|830216|830216014| 8219.140000000001|      10|                 10|   201| 5763|              50.0|
        +--------------------+------------+------------------+------+------+---------+------------------+--------+-------------------+------+-----+------------------+
        */

        
         val level = args(0)
         val heuristics = args(1)
         val order = args(2)

        // performing reranking 

        var output = df


        // if order is desc then we will sort in descending order
        output = if(order == "desc"){
            output.withColumn("internal_rank_for_level", row_number().over(Window.partitionBy(level).orderBy(desc(heuristics),col("product_rank"))))
        }
        else{
            output.withColumn("internal_rank_for_level", row_number().over(Window.partitionBy(level).orderBy(heuristics,"product_rank")))
        }

        var selected_for_join = df.select(level)

        selected_for_join = selected_for_join.withColumn("prev_seen", row_number().over(Window.partitionBy(level).orderBy(lit(1))))
        

        output = selected_for_join.join(output,selected_for_join(level) === output(level) && selected_for_join("prev_seen") === output("internal_rank_for_level"),"left")
        .orderBy("internal_rank_for_level","product_rank")
        .drop("internal_rank_for_level")
        .drop("prev_seen")
        .drop(output(level))

        output = output.withColumn("final_rank", row_number().over(Window.partitionBy(level).orderBy(lit(1))))

        output.orderBy("product_rank").show()

    }
}