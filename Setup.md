# Download JVM
 - [JAVA ARCHIVE](https://www.oracle.com/java/technologies/downloads/archive/)
 - make sure you get the right version based on the version of scala you need

# Download Gradle
 - [Gradle](https://gradle.org/install/)
 - follow the steps in this page you should get it

# Scala-Spark
 - [Spark](https://spark.apache.org/downloads.html)
 - from this page you will get tgz file extract it somewhere then add the /bin/spark-shell which will be in the extracted to your path or just add this line to bashrc
   alias spark-shell="path_to_that_file"

   ```
   alias spark-shell="/home/avinash/spark-3.5.4-bin-hadoop3-scala2.13/bin/spark-shell"
   alias spark-submit="/home/avinash/spark-3.5.4-bin-hadoop3-scala2.13/bin/spark-submit"

 it should be like this,versions might change so check out what version you have downloaded

# Gradle,Scala
 - [Guid for scala with gradle](https://docs.gradle.org/current/samples/sample_building_scala_applications.html)
 - follow this page, and setup a project , make sure to learn how to run , build from this page
 - to add spark go to build.gradle file and near implementations in dependencies add the code from this page
 - [code to add in build.gradle dependencies](https://mvnrepository.com/artifact/org.apache.spark/spark-core)
 - make sure to add both spark core and spark sql from maven repositories to the build file
 - to build a jar file just build using *./gradlew build* then the jar will be in build folder
 - then you can submit this to spark-submit with classpath