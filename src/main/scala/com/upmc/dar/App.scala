package com.upmc.dar

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.evaluation.RegressionMetrics


object App {
  val NB_lines = 50

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  def readCsv(sparkSession: SparkSession, path: String): DataFrame = {
    sparkSession.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
  }

  def runTrainingAndEstimation(data: DataFrame, labelCol: String, numFeatCols: Array[String],
                               categFeatCols: Array[String] = Array()): DataFrame = {

    val lr = new LinearRegression().setLabelCol(labelCol)
    val assembler = new VectorAssembler()
    val pipeline = new Pipeline()

    if (categFeatCols.length == 0) {
      assembler
        .setInputCols(numFeatCols)
        .setOutputCol("features")
      pipeline.setStages(Array(assembler, lr))

    } else {

      var featureCols = numFeatCols
      val indexers = categFeatCols.map(c =>
        new StringIndexer().setInputCol(c).setOutputCol(s"${c}_idx")
      )
      val encoders = categFeatCols.map(c => {
        val outputCol = s"${c}_enc"
        featureCols = featureCols :+ outputCol
        new OneHotEncoder().setInputCol(s"${c}_idx").setOutputCol(outputCol)
      })
      assembler
        .setInputCols(featureCols)
        .setOutputCol("features")
      pipeline.setStages(indexers ++ encoders ++ Array(assembler, lr))
    }

    val Array(trainSet, testSet) = data.randomSplit(Array(0.9, 0.1), seed = 12345)

    // Entrainement du modèle sur trainSet
    val modelLR = pipeline.fit(trainSet)

    // Prédiction sur testSet
    val predictions = modelLR.transform(testSet)
    predictions.select("prediction", labelCol, "year_of_sale", "neighborhood").show()

    val predictionsAndObservations = predictions
      .select("prediction", labelCol)
      .rdd
      .map(row => (row.getDouble(0), row.getInt(1).toDouble))
    val metrics = new RegressionMetrics(predictionsAndObservations)
    val rmse = metrics.rootMeanSquaredError
    println("RMSE: " + rmse)

    predictions
  }

  println("Hello World!")

  val sparkSession: SparkSession = SparkSession.builder
    .master("local")
    .appName("BrooklynHousePricingML")
    .getOrCreate()

  import sparkSession.implicits._


  def main(args: Array[String]): Unit = {
    // Example 1 (RDD replaced by Dataset API of Spark 2)
    val dataFolder = "/Users/cb_mac/Desktop/UPMC/M2/AAGA/TME/BrooklynHousePricing/files/"
    val brooklynPath = dataFolder + "brooklyn_sales_map.csv"
    //val brooklynHouses: Dataset[String] = sparkSession.read.textFile(brooklynPath)

    val brooklyn_sales: DataFrame = readCsv(sparkSession, brooklynPath)

    //filter data set with some pertinent column name
    val numericCols = Array(
      "year_built",
      "year_of_sale",
      "land_sqft",
      "gross_sqft",
      "total_units",
      "residential_units",
      "commercial_units"
    )

    val categCols = Array("neighborhood", "sale_date","zip_code")
    //val numericCols  = Array("year_of_sale")
    val labelCol = "sale_price"
    // entrainement avec uniquement des features numériques
    val filtered_data = brooklyn_sales.filter($"year_of_sale" =!= 2017)
    runTrainingAndEstimation(filtered_data, labelCol, numericCols)
    // entrainement avec features numériques et catégorielles
    runTrainingAndEstimation(filtered_data, labelCol, numericCols, categCols)
  }

  /**
    * prediction Linear Regression
    */


}
