package fr.stl.dar

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession}



object TfiRevenueEstimator {

  def readCsv(sparkSession: SparkSession, path: String): DataFrame = {
    sparkSession.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
  }

  def main(arg: Array[String]): Unit = {
    val spark = SparkSession.builder
      .master("local")
      .appName("TfiRestaurants")
      .getOrCreate()

    val inputPath = "/home/denys/dar/data/tfi/train.csv"
    val data = readCsv(spark, inputPath)
    val numericCols = data.columns.filter(_.startsWith("P"))

    val assembler = new VectorAssembler()
      .setInputCols(numericCols)
      .setOutputCol("features")

    val lr = new LinearRegression().setLabelCol("revenue")
    val pipeline = new Pipeline()
      .setStages(Array(assembler, lr))

    val Array(trainSet, testSet) = data.randomSplit(Array(0.9, 0.1))
    // Entrainement du modèle sur trainSet
    val modelLR = pipeline.fit(trainSet)
    // Prédiction sur testSet
    val predictions = modelLR.transform(testSet)
    predictions.select("revenue", "prediction").show()

  }

}
