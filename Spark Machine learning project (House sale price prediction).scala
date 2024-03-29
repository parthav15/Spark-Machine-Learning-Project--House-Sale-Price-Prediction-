// Databricks notebook source
// MAGIC %md
// MAGIC # Creating a Regression Model
// MAGIC
// MAGIC In this project, I am going to implement a regression model that will predict the House Sales Prices based on many attributes in Data.
// MAGIC
// MAGIC # Importing Spark SQL and ML libraries

// COMMAND ----------

// MAGIC %scala
// MAGIC
// MAGIC import org.apache.spark.sql.functions._
// MAGIC import org.apache.spark.sql.Row
// MAGIC import org.apache.spark.sql.types._
// MAGIC
// MAGIC import org.apache.spark.ml.regression.LinearRegression
// MAGIC import org.apache.spark.ml.feature.VectorAssembler

// COMMAND ----------

// MAGIC %md
// MAGIC # First step is to load the source data
// MAGIC
// MAGIC ##### The data for this project is provided as a CSV file containing details of House details we need to Predict the SalePrice.
// MAGIC
// MAGIC ##### You will load this data into a DataFrame and display it.

// COMMAND ----------

// MAGIC %scala
// MAGIC
// MAGIC val data = spark.read.option("inferSchema","true").option("header","true").csv("/FileStore/tables/train.csv")
// MAGIC
// MAGIC display(data)

// COMMAND ----------

// MAGIC %scala
// MAGIC data.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC # We need to prepare the data
// MAGIC ###### Now the next step is to train the regression model. We will use VectorAssembler class to transformt he feature columns into a vector, and then rename the SalePrice column to label.

// COMMAND ----------

// MAGIC %md
// MAGIC #VectorAssembler()
// MAGIC ###### VectorAssembler() is a transformer that combines a given list of columns into a single vector columns. it is useful for combining raw features and features generated by different feature transformers into a single feature vector, in order to train ML models like logistic regression and decision trees.

// COMMAND ----------

// MAGIC %scala
// MAGIC
// MAGIC var StringfeatureCol = Array("MSZoning", "LotFrontage", "Street", "Alley", "LotShape", "LandContour", "Utilities", "LotConfig", "LandSlope", "Neighborhood", "Condition1", "Condition2", "BldgType", "HouseStyle", "RoofStyle", "RoofMatl", "Exterior1st", "Exterior2nd", "MasVnrType", "MasVnrArea", "ExterQual", "ExterCond", "Foundation", "BsmtQual", "BsmtCond", "BsmtExposure", "BsmtFinType1", "BsmtFinType2", "Heating", "HeatingQC", "CentralAir", "Electrical", "KitchenQual", "Functional", "FireplaceQu", "GarageType", "GarageYrBlt", "GarageFinish", "GarageQual", "GarageCond", "PavedDrive", "PoolQC", "Fence", "MiscFeature")

// COMMAND ----------

// MAGIC %md
// MAGIC #StringIndexer
// MAGIC
// MAGIC ######StringIndexer encodes a string column of labels into a columns of label indices.

// COMMAND ----------

import org.apache.spark.ml.feature.StringIndexer

val df = spark.createDataFrame(
  Seq((0, "a"), (1, "b"), (2, "c"), (3, "a"), (4, "a"), (5, "c"))
).toDF("id", "category")

val indexer = new StringIndexer()
  .setInputCol("category")
  .setOutputCol("categoryIndex")

val indexed = indexer.fit(df).transform(df)

display(indexed)

// COMMAND ----------

// MAGIC %md
// MAGIC # Define the Pipeline
// MAGIC
// MAGIC ###### A predictive model often requires multiple stages of feature preparation.
// MAGIC
// MAGIC ###### A pipeline consists of a series of transformers and estimator stages that typically prepare a DataFrame for modelling and then train a predictive model.
// MAGIC
// MAGIC ###### in this case, you will create a pipeline with stages:
// MAGIC * A StringIndexer estimator that converts string values to indexes for categorical features.
// MAGIC * A VectorAssembler that combines categorical features into a single vector.

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.ml.attribute.Attribute
// MAGIC import org.apache.spark.ml.feature.{IndexToString, StringIndexer}
// MAGIC import org.apache.spark.ml.{Pipeline, PipelineModel}
// MAGIC
// MAGIC val indexers = StringfeatureCol.map{
// MAGIC   colName =>
// MAGIC   new StringIndexer().setInputCol(colName).setOutputCol(colName + "_indexed")
// MAGIC }
// MAGIC
// MAGIC val pipeline = new Pipeline()
// MAGIC                     .setStages(indexers)
// MAGIC
// MAGIC val HouseDF = pipeline.fit(data).transform(data)

// COMMAND ----------

HouseDF.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC
// MAGIC All the string values have been converted into numeric values.

// COMMAND ----------

HouseDF.show()

// COMMAND ----------

// MAGIC %md
// MAGIC
// MAGIC # Split the Data
// MAGIC
// MAGIC It is common practice when building supervised machine learning models to split the source data, using some of it to train the model and reserving some to test the trained model. In this project, we will use 70% of the data for training, and reserve 30% for testing purposes. In the testing data, the label columns is renamed to trueLabel so you can use it later to compare predicted labels with known actual values.

// COMMAND ----------

// MAGIC %scala
// MAGIC
// MAGIC val splits = HouseDF.randomSplit(Array(0.7,0.3))
// MAGIC val train = splits(0)
// MAGIC val test = splits(1)
// MAGIC val train_rows = train.count()
// MAGIC val test_rows = test.count()
// MAGIC println("Training Rows: " + train_rows + "Tesing Rows: " + test_rows)

// COMMAND ----------

// MAGIC %md
// MAGIC # VectorAssembler() that combines categorical features into a single vector

// COMMAND ----------

// MAGIC %scala
// MAGIC
// MAGIC val assembler = new VectorAssembler().setInputCols(Array("Id", "MSSubClass", "LotArea", "OverallQual", "OverallCond", "YearBuilt", "YearRemodAdd", "BsmtFinSF1", "BsmtFinSF2", "BsmtUnfSF", "TotalBsmtSF", "1stFlrSF", "2ndFlrSF", "LowQualFinSF", "GrLivArea", "BsmtFullBath","BsmtHalfBath", "FullBath", "HalfBath", "BedroomAbvGr", "KitchenAbvGr", "TotRmsAbvGrd", "Fireplaces", "GarageCars", "GarageArea", "WoodDeckSF", "OpenPorchSF", "EnclosedPorch", "3SsnPorch", "ScreenPorch", "PoolArea", "MiscVal", "MoSold", "YrSold", "MSZoning_indexed", "LotFrontage_indexed", "Street_indexed", "Alley_indexed", "LotShape_indexed","LandContour_indexed", "Utilities_indexed", "LotConfig_indexed", "LandSlope_indexed", "Neighborhood_indexed", "Condition1_indexed", "Condition2_indexed", "BldgType_indexed", "HouseStyle_indexed", "RoofStyle_indexed", "RoofMatl_indexed", "Exterior1st_indexed", "Exterior2nd_indexed", "MasVnrType_indexed", "MasVnrArea_indexed", "ExterQual_indexed", "ExterCond_indexed", "Foundation_indexed", "BsmtQual_indexed", "BsmtCond_indexed", "BsmtExposure_indexed", "BsmtFinType1_indexed", "BsmtFinType2_indexed", "Heating_indexed", "HeatingQC_indexed", "CentralAir_indexed", "Electrical_indexed", "KitchenQual_indexed", "Functional_indexed", "FireplaceQu_indexed", "GarageType_indexed", "GarageYrBlt_indexed", "GarageFinish_indexed", "GarageQual_indexed", "GarageCond_indexed", "PavedDrive_indexed", "PoolQC_indexed", "Fence_indexed", "MiscFeature_indexed" )).setOutputCol("features")
// MAGIC val training = assembler.transform(train).select($"features", $"SalePrice".alias("label"))
// MAGIC training.show()

// COMMAND ----------

// MAGIC %md
// MAGIC # Now train a regression model
// MAGIC
// MAGIC Next, you need to train a regresssion model using the training data. To do this, create an instance of the regression algorithm you want to use and use its fit method to train a model based on the training DataFrame. In this project, we will use a LinearRegression algorithm.

// COMMAND ----------

val lr = new LinearRegression().setLabelCol("label").setFeaturesCol("features").setMaxIter(10).setRegParam(0.3)
val model = lr.fit(training)
println("Model Trained!")

// COMMAND ----------

// MAGIC %md
// MAGIC # Now I am going to Prepare the testing data.
// MAGIC
// MAGIC Now that you have a trained model, you can test it using the testing data you reserved previously. First, you need to prepare the testing data in the same way as you did the training data by transforming the feature columns into vector. This time you'll rename the SalePrice column to trueLabel.

// COMMAND ----------

val testing = assembler.transform(test).select($"features",$"SalePrice".alias("trueLabel"))
testing.show()

// COMMAND ----------

// MAGIC %md
// MAGIC # Now I am going to test the model.
// MAGIC
// MAGIC Now I am ready to use the Transform method of the model to generate some predictions. But in this case you are using the test data which includes a known true label, so you can compare the predicted Sale Price.

// COMMAND ----------

val prediction = model.transform(testing)
val predicted = prediction.select("features","prediction","trueLabel")
predicted.show()

// COMMAND ----------

// MAGIC %md
// MAGIC So, as you can see the results, the prediction column contains the predicted values, and the trueLabel column contains the actual known value from the testing data. It looks like there is some variance between the predictions and the actual values.

// COMMAND ----------

// MAGIC %md
// MAGIC # Evaluating the Regression Model
// MAGIC
// MAGIC In this project, we have created pipeline for a linear regression model, and then test and evaluate the model.
// MAGIC
// MAGIC # Prepare the Data
// MAGIC
// MAGIC First, import the libraries you will need and prepare the training and test data.

// COMMAND ----------

// MAGIC %md
// MAGIC # Examine the Predicted and Actual Vales
// MAGIC
// MAGIC You can plot the predicted values against the actual values to see how accurately the model has predicted. In a perfect model, the resulting scatter plot shoudl form a perfect diagnot line with each predicted value being identical to the actual value - in practice, some variance is to expected.

// COMMAND ----------

predicted.createOrReplaceTempView("HousePrice")

// COMMAND ----------

// MAGIC %sql
// MAGIC
// MAGIC select prediction, trueLabel from HousePrice

// COMMAND ----------

// MAGIC %python
// MAGIC
// MAGIC # Fetch the result of the SQL query
// MAGIC result = spark.sql("SELECT prediction, trueLabel FROM HousePrice").toPandas()
// MAGIC
// MAGIC # Plot the scatter plot using matplotlib
// MAGIC import matplotlib.pyplot as plt
// MAGIC
// MAGIC plt.scatter(result['prediction'], result['trueLabel'])
// MAGIC plt.xlabel('Predicted Values')
// MAGIC plt.ylabel('True Values')
// MAGIC plt.title('Scatter Plot of Predicted vs True Values')
// MAGIC plt.show()

// COMMAND ----------

// MAGIC %md
// MAGIC # Retrieve the Root Mean Square Error (RMSE)
// MAGIC
// MAGIC There are a number of metrics used to measure the variance between predicted and actual values. Of these, the root mean square error (RMSE) is a commonly used value that is measured in the same units as the predicted and actual values - so in this case, the RMSE indicates the average number of Price between predicted and actual Sale Price Vales. You can use the RegressionEvaluator class to retrieve the RMSE.

// COMMAND ----------

import org.apache.spark.ml.evaluation.RegressionEvaluator

val evaluator = new RegressionEvaluator().setLabelCol("trueLabel").setPredictionCol("prediction").setMetricName("rmse")
val rmse = evaluator.evaluate(prediction)
println("Root Mean Square Error (RMSE): " + (rmse))

// COMMAND ----------

// MAGIC %md
// MAGIC # THANK YOU