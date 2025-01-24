# Spark Optimizations Explained

## 1. Coalesce vs Repartition
### Detailed Explanation
- **Coalesce**: This operation reduces the number of partitions in a dataset by combining adjacent partitions without causing a full shuffle. It minimizes data movement and is more efficient when downsizing partitions. However, it doesn’t guarantee balanced partition sizes.
  - **Advantages**: Coalesce avoids a full shuffle, making it faster and less resource-intensive when reducing partitions.
  - **Limitations**: It may result in uneven partition sizes if the original partitions are not balanced.
  - **Use case**: Ideal for reducing partitions after a computation that results in fewer records, such as filtering.

- **Repartition**: This operation performs a full shuffle to increase or decrease the number of partitions, ensuring data is evenly distributed across all partitions. It is more resource-intensive but necessary for achieving balanced workloads.
  - **Advantages**: Guarantees balanced partition sizes and parallelism across executors.
  - **Limitations**: The full shuffle can be computationally expensive and time-consuming.
  - **Use case**: Useful when the dataset has uneven partition sizes or when more partitions are needed for parallelism.

### Code Example
```scala
// Coalesce example
val coalescedDF = df.coalesce(2)

// Repartition example
val repartitionedDF = df.repartition(4)
```

### Why Repartition is Better in Some Cases
If you are working with unbalanced partitions (e.g., one partition has significantly more data than others), coalesce may exacerbate the imbalance. Repartition ensures even distribution and is better for subsequent operations that require parallelism.

---

## 2. Avoiding GroupBy Using ReduceBy
### Detailed Explanation
- **GroupBy**: Groups data by key and performs computations on the grouped data. It triggers a shuffle, which can be expensive for large datasets.
- **ReduceBy**: Performs local aggregation within each partition before shuffling the data, reducing the overall amount of data transferred between nodes.

### Key Difference
- **GroupBy**: Processes all data before aggregation, leading to larger shuffles.
- **ReduceBy**: Aggregates data locally within each partition first, significantly reducing the shuffle size.

### Code Example
```scala
// GroupBy example
val grouped = rdd.groupBy(_._1).mapValues(_.map(_._2).sum)

// ReduceBy example
val reduced = rdd.reduceByKey(_ + _)
```

### Why ReduceBy is Better
When working with large datasets, shuffles are one of the most time-consuming operations. By reducing data size before the shuffle, ReduceBy improves efficiency and minimizes resource usage.

---

## 3. Handling Data Skewness
### Techniques
1. **Repartition**: Redistributes data evenly across partitions to address imbalance.
   ```scala
   val repartitioned = rdd.repartition(10)
   ```
   **Why it helps**: Ensures that no single executor is overloaded, improving parallelism and execution time.

2. **Salting**: Introduces a random prefix ("salt") to skewed keys to distribute data more evenly.
   ```scala
   val salted = rdd.map { case (key, value) =>
     val salt = new Random().nextInt(10)
     (s"$key#$salt", value)
   }
   ```
   **Why it helps**: Breaks up skewed keys into smaller chunks, reducing the load on any single partition.

3. **Isolated Salting**: Applies salting only to identified skewed keys, leaving others unchanged.
   **Why it helps**: Targets skewed keys without affecting the entire dataset, maintaining efficiency.

4. **Map Join**: Converts a shuffle join into a broadcast join by broadcasting the smaller dataset to all executors.
   ```scala
   val broadcasted = sparkContext.broadcast(smallDataset)
   val result = largeDataset.map { record =>
     val value = broadcasted.value.get(record.key)
     (record.key, value)
   }
   ```
   **Why it helps**: Eliminates the need for shuffling large datasets during joins, improving performance.

5. **Iterative Broadcast Join**: For iterative algorithms that repeatedly use a small dataset, broadcasting it ensures efficient reuse.

---

## 4. Executor Tuning
### Detailed Explanation
- **Fat Executors**: Allocate a large amount of memory and fewer executors. This can lead to high garbage collection (GC) overhead.
- **Tiny Executors**: Allocate minimal memory and increase the number of executors. This may underutilize resources.
- **Balanced Executors**: Strikes a balance by allocating optimal memory and cores for each executor.

### Best Practices
1. Leave 1 core per node for OS processes.
2. Assign ~5 tasks per executor for optimal parallelism.
3. Reserve 1 executor and 1 GB of memory for the Yarn Application Master.
4. Allocate extra memory for JVM overhead (~10% of executor memory).

### Example Comparison
| Configuration       | Pros                                | Cons                         |
|---------------------|-------------------------------------|------------------------------|
| Fat Executors       | Fewer executors to manage          | High GC times                |
| Tiny Executors      | Reduced GC times                   | Underutilized resources      |
| Balanced Executors  | Optimized resource utilization      | Requires proper configuration |

### Code Example
```bash
--executor-cores 5 --executor-memory 10G --num-executors 50 --driver-memory 2G
```

---

## 5. Persistent and Broadcast
### Detailed Explanation
- **Persistence**: Stores RDDs or DataFrames in memory or disk for reuse. Persistence levels include MEMORY_ONLY, MEMORY_AND_DISK, and DISK_ONLY.
  ```scala
  val cachedData = data.persist(StorageLevel.MEMORY_AND_DISK)
  ```
  **Why it helps**: Reduces recomputation costs for iterative algorithms or workflows that reuse the same data.

- **Broadcast**: Distributes small datasets to all executors to avoid shuffling during joins.
  ```scala
  val broadcastVar = sparkContext.broadcast(Map("key1" -> 1, "key2" -> 2))
  rdd.map { case (k, v) => (k, broadcastVar.value.getOrElse(k, 0)) }
  ```
  **Why it helps**: Improves performance by eliminating the need to shuffle small datasets across the cluster.

---

## 6. Cost-Based Optimizations (CBO)
### Detailed Explanation
CBO uses table statistics (e.g., row counts, column cardinality) to optimize query execution plans. This ensures better join order and partition pruning.

### Requirements
- Statistics must be computed for tables.
- Enabled by setting `spark.sql.cbo.enabled` to `true`.

### Code Example
```sql
ANALYZE TABLE tableName COMPUTE STATISTICS;
EXPLAIN COST SELECT * FROM tableName WHERE id = 1;
```

**Why it helps**: Ensures queries are executed in the most efficient manner based on the data’s characteristics.

---

## 7. Bucketing
### Detailed Explanation
- Bucketing organizes data into fixed buckets based on a specified column. It reduces shuffling during joins and aggregations.

### Code Example
```scala
val bucketedDF = df.write
  .format("parquet")
  .bucketBy(4, "columnName")
  .saveAsTable("bucketedTable")
```

**Why it helps**: Ensures that data with the same bucket key is colocated, reducing shuffle during operations such as joins.

---

## 8. Union vs OR in Joins
### Detailed Explanation
- **Union**: Combines results from multiple queries into a single dataset. Ideal for appending datasets.
  ```scala
  val combined = df1.union(df2)
  ```

- **OR in Joins**: Filters data using multiple conditions within a single query.
  ```scala
  val joined = df1.join(df2, df1("col1") === df2("col1") || df1("col2") === df2("col2"))
  ```

**Why Union is Better**: When combining datasets that don’t require complex join logic, union is faster and simpler to implement.

---

## 9. Dynamic Partition Pruning
### Detailed Explanation
Dynamic Partition Pruning (DPP) optimizes query execution by pruning unnecessary partitions at runtime based on query conditions. By analyzing the query plan and runtime data, Spark decides which partitions to scan, reducing the data processed.

### Types of Partition Pruning
1. **Static Partition Pruning**:
   - Filters partitions at query compilation time.
   - Works when partition filter values are known before execution.
   - **Example**:
     ```sql
     SELECT * FROM sales WHERE region = 'US';
     ```
     Here, Spark knows the partition value (`region = 'US'`) during query planning, and only the `US` partition is scanned.

   **Advantages**:
   - Simple and efficient for queries with constant partition filters.
   
   **Limitations**:
   - Doesn’t work when partition filters depend on runtime data.

2. **Dynamic Partition Pruning**:
   - Filters partitions at runtime when filter values are derived from a subquery or join.
   - Enables Spark to decide which partitions to scan based on runtime data.
   - **Example**:
     ```sql
     SET spark.sql.optimizer.dynamicPartitionPruning.enabled=true;
     SELECT * FROM largeTable
     WHERE partitionKey IN (SELECT key FROM smallTable);
     ```
     Here, the `partitionKey` values are dynamically computed from `smallTable` at runtime

**Advantages of Dynamic Partition Pruning**:
- Reduces the volume of data scanned, minimizing I/O and improving query performance.
- Automatically adapts to runtime data, making it effective for queries involving subqueries or joins.
  
**Limitations of Dynamic Partition Pruning**:
- Can introduce slight overhead due to runtime computation of partition filters.
- May not be effective for queries with highly distributed or skewed data.

### How Dynamic Partition Pruning Works
1. **Query Plan Analysis**: During query planning, Spark identifies opportunities for DPP by analyzing query filters.
2. **Runtime Filtering**: At runtime, Spark collects the necessary filter values from dependent subqueries or joins.
3. **Partition Scanning**: Spark prunes unnecessary partitions based on the runtime filter values and scans only relevant partitions.

### Code Example of DPP in Action
```sql
-- Enable Dynamic Partition Pruning
SET spark.sql.optimizer.dynamicPartitionPruning.enabled=true;

-- Query Example
SELECT largeTable.*
FROM largeTable
JOIN smallTable
ON largeTable.partitionKey = smallTable.key
WHERE smallTable.filterColumn = 'value';
```
- **Explanation**:
  - The `partitionKey` values are derived from `smallTable.key` at runtime.
  - Only partitions in `largeTable` matching the runtime-computed keys are scanned.

### Static Partition Pruning vs. Dynamic Partition Pruning
| Feature                       | Static Partition Pruning                | Dynamic Partition Pruning                 |
|-------------------------------|-----------------------------------------|-------------------------------------------|
| **When pruning occurs**       | During query compilation               | At runtime                                |
| **Filter type**               | Constants or literals                  | Derived from runtime subqueries or joins |
| **Performance impact**        | Fast, as no runtime computation needed | Slight overhead but significantly reduces scanned data |
| **Use case**                  | Queries with fixed partition filters   | Queries with dynamic partition filters   |

### Why Dynamic Partition Pruning is Better
- **Flexibility**: Handles cases where partition filters depend on runtime values, which static pruning cannot address.
- **Performance Gains**: Reduces I/O and computation by scanning only relevant partitions at runtime, especially for large datasets with many partitions.

refered from  [Youtube Playlist](https://www.youtube.com/watch?v=R3wVjyePRno&list=PLtfmIPhU2DkNqAEPWpm9PeQ71qPCYdQgN)
