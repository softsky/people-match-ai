package com.localblox.ashfaq.csv;

import static org.apache.spark.sql.functions.lit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
class CSVImporter {
	public static void main(String... args){
		SparkSession spark = SparkSession
			.builder()
			.appName("Java Spark SQL basic example")
			.config("spark.master", "local")
			.getOrCreate();
		
		List<String> files = new ArrayList<>();
	     Path dir = Paths.get("target/in");
	     try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.csv")) {
	    	  
	         Iterator<Path> iterator = stream.iterator();	         
	         while(iterator.hasNext()){
	        	 files.add(iterator.next().toString());
	         }
	     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	    String [] arrFiles = new String[files.size()];
	    files.toArray(arrFiles);
		SQLContext sqlContext = new SQLContext(spark);
			Dataset<Row> df = sqlContext.read()
			.format("com.databricks.spark.csv")
			.option("header", "true") // Use first line of all files as header
			.option("inferSchema", "true") // Automatically infer data types
			.load(arrFiles.length > 0?arrFiles[0]:null); // processing only first item, otherwise Hadoop crashes

			Dataset<Row> selectedData = df.select("Address", "City Name", "State Code", "County Name", "Zip Code", 
					"Contact Person Name", "Contact Person Position", "Gender Contact Person 2", 
					"Employee Count", "Employee Range", 
					"Annual Revenues", "Sales Range", 
					"SIC Name / Category", "Category","Full Category Set","Latitude", "Longitude","Physical Neighborhood",
					"Love Score", "Freshness Score","Holistic Score",
					"Hours of Operation",
					"Reviews Scanned", "Good Reviews Scanned","Average Review Rating",
					"Likes Count","Social Media Profiles Count", 
					"Facebook Profile", "Twitter Profile","Foursquare Profile",
					"HQ_Followers", "HQ_Type", "HQ_Employees", 
					"HQ_Name", "HQ_YearFounded", "HQ_Categories", "HQ_Specialties", 
					"HQ_Revenue", "HQ_Ticker", "HQ_Exchange", "HQ_Acquisitions", "HQ_GrowthScore", 
					"HQ_EstMonthlyUniques", "HQ_EstInboundLinks", "HQ_TwitterFollowers","HQ_FacebookLikes", "HQ_FacebookTalkingAbout", "HQ_LinkedInFollowerCount");
			
			selectedData = selectedData
					.withColumn("PurchaseComplete", lit(1));

			selectedData.write()
			.format("com.databricks.spark.csv")
			.option("header", "true")
			.save("out");		
	}
}
