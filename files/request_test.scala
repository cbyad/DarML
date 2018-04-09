//set context
val sqlContext = new org.apache.spark.sql.SQLContext(sc)

//load dataset
val brooklyn_sales = sqlContext.read.format("com.databricks.spark.csv").option("header","true").option("inferSchema","true").load("/home/dat/Fac/DAR/brooklyn_sales_map.csv")

//export dataframe 
brooklyn_sales.select("neighborhood").distinct.coalesce(1).write.format("com.databricks.spark.csv").option("header","true").save("test.csv")

//filter data set with some pertinent column names
var filtered_data = brooklyn_sales.select(
"borough1",
"neighborhood",
"building_class_category",
"tax_class",
"block",
"lot",
"building_class",
"address9",
"zip_code",
"residential_units",
"commercial_units",
"total_units" ,
"land_sqft",
"gross_sqft",
"year_built",
"tax_class_at_sale",
"building_class_at_sale",
"sale_price",
"sale_date",
"year_of_sale",
"BldgClass",
"BldgArea",
"NumBldgs",
"NumFloors",
"YearBuilt",
"YearAlter1",
"YearAlter2",
"HistDist",
"XCoord",
"YCoord",
"ZoneMap",
"TaxMap",
"Version",
"SHAPE_Leng",
"SHAPE_Area")

//rename column 
filtered_data = filtered_data.withColumnRenamed("borough1","borough")
filtered_data = filtered_data.withColumnRenamed("address9","address")

//save filtered data
filtered_data.coalesce(1).write.format("com.databricks.spark.csv").option("header","true").save("filtered_data")

//Sum a selected column and cast it to long
val sumSteps: Long = filtered_data.agg(sum("sale_price").cast("long")).first.getLong(0)

//Average price for each neighborhood
filtered_data.groupBy("neighborhood")
    .agg(count("*").alias("sales_count"),
         sum("sale_price").cast("long").alias("total_sum_price"), 
        avg("sale_price").alias("average_price")).show

//Average price for each borough
filtered_data.groupBy("borough")
    .agg(count("*").alias("sales_count"),
         sum("sale_price").cast("long").alias("total_sum_price"), 
        avg("sale_price").alias("average_price")).show

//Average price for each zip code
filtered_data.groupBy("zip_code")
    .agg(count("*").alias("sales_count"),
         sum("sale_price").cast("long").alias("total_sum_price"), 
        avg("sale_price").alias("average_price")).show

//Average price for each year
filtered_data.groupBy("year_of_sale")
    .agg(count("*").alias("sales_count"),
         sum("sale_price").cast("long").alias("total_sum_price"), 
        avg("sale_price").alias("average_price"))
    .orderBy(asc("year_of_sale")).show(false)

//Average price for each neighborhood and each year
val sale_per_year_neigh = filtered_data.groupBy("neighborhood", "year_of_sale")
    .agg(count("*").alias("sales_count"),
         sum("sale_price").cast("long").alias("total_sum_price"), 
        avg("sale_price").alias("average_price")).orderBy(desc("year_of_sale")).show

