package com.upmc.dar

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.evaluation.RegressionMetrics


object App {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  def readCsv(sparkSession: SparkSession, path: String): DataFrame = {
    sparkSession.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
  }

  def runTrainingAndEstimation(data: DataFrame, labelCol: String, numFeatCols: Array[String], out: String,
                               categFeatCols: Array[String] = Array()): DataFrame = {

    val lr = new LinearRegression().setLabelCol(labelCol).setMaxIter(10)
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
        new StringIndexer().setInputCol(c).setOutputCol(s"${c}_idx").setHandleInvalid("keep")
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
      .map(row => (row.getDouble(0), row.getDecimal(1).doubleValue()))
    val metrics = new RegressionMetrics(predictionsAndObservations)
    val rmse = metrics.rootMeanSquaredError
    println("RMSE: " + rmse)
    predictions.select("prediction", "sale_price", "year_of_sale", "neighborhood").coalesce(1).
      write.format("com.databricks.spark.csv").option("header", "true").save(out)
    predictions
  }


  val sparkSession: SparkSession = SparkSession.builder
    .master("local")
    .appName("BrooklynHousePricingML")
    .getOrCreate()

  import sparkSession.implicits._


  def main(args: Array[String]): Unit = {
    println("Welcome to Brooklyn House Pricing ML App...")

    val input: String = args(0)
    val limit: Float = args(1).toFloat

    if (limit < 1 || limit > 100) {
      println(s"sorry! $limit is out of bound ")
      System.exit(1)
    }
    println(s"Evaluate $limit % of the dataset ...")

    val brooklynPath = input
    val lines = readCsv(sparkSession,brooklynPath).count() // count total number of lines

    val percentage: Int = (lines * (limit / 100)).toInt
    val brooklyn_sales: DataFrame = readCsv(sparkSession, brooklynPath).limit(percentage)// limit data to compute

    val filtered_data = brooklyn_sales.select("borough1", "neighborhood", "building_class_category",
      "tax_class", "block", "lot", "building_class", "address9", "zip_code", "residential_units",
      "commercial_units", "total_units", "land_sqft", "gross_sqft",
      "year_built", "tax_class_at_sale", "building_class_at_sale",
      "sale_price", "sale_date", "year_of_sale", "BldgClass", "BldgArea", "NumBldgs",
      "NumFloors", "YearBuilt", "YearAlter1", "YearAlter2", "HistDist", "XCoord",
      "YCoord", "ZoneMap", "TaxMap", "Version", "SHAPE_Leng", "SHAPE_Area").filter($"year_of_sale" =!= 2017)

    val df = filtered_data.na.drop.filter($"sale_price" =!= 0)

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

    val categCols = Array("neighborhood", "zip_code")
    //val numericCols  = Array("year_of_sale")

    val labelCol = "sale_price"
    // entrainement avec uniquement des features numériques

    // entrainement avec features numériques et catégorielles
    runTrainingAndEstimation(df, labelCol, numericCols, "predWithNumCols")
    runTrainingAndEstimation(df, labelCol, numericCols, "predWithAll", categCols)
  }

}