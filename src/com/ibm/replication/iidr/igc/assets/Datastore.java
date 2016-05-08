package com.ibm.replication.iidr.igc.assets;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Datastore {

	public static final String IGC_CLASS_NAME = "$IIDR-Datastore";
	public static final String DATABASE_TYPE_DATASTAGE = "IBM InfoSphere DataStage";
	
	final static Logger logger = Logger.getLogger("com.ibm.replication.cdc.metadata.ExportMetadata");
	
	private String id;
	private String name;
	private String host;
	private String description;
	private String port;
	private String version;
	private String platform;
	private String engine_type;
	private String type;
	private String database_name;
	
	public Datastore(String id, String name, String description, String host, String port, String version, 
				     String platform, String engine_type, String type) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.host = host.toUpperCase();
		this.port = port;
		this.version = version;
		this.platform = platform;
		this.engine_type = engine_type;
		this.type = type;
		
		// Try to make out the database name from the datastore description
		Pattern dbPattern = Pattern.compile("(DATABASE:|DB:)\\s*(\\w+)");
		Matcher dbMatcher = dbPattern.matcher(description);
		if (dbMatcher.find()) {
			database_name = dbMatcher.group(2);
			logger.debug(MessageFormat.format(
					"Database name {0} for table has been retrieved from the datastore description",
					new Object[] { database_name }));
		} else {
			database_name = name;
			logger.debug(MessageFormat.format("Database name {0} for table has been set to the name of the datastore",
					new Object[] { database_name }));
		}
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getDatabaseName() {
		return this.database_name;
	}
	
	public String getDatabase() {
		return this.engine_type;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String toAssetXML() {
		return MessageFormat.format("\t\t<asset class=\"{11}\" repr=\"{0}\" ID=\"{1}\">\n" +
			"\t\t\t<attribute name=\"name\" value=\"{2}\" />\n" +
			"\t\t\t<attribute name=\"short_description\" value=\"{3}\" />\n" +
			"\t\t\t<attribute name=\"long_description\" value=\"{4}\" />\n" +
			"\t\t\t<attribute name=\"$host\" value=\"{5}\" />\n" + 
			"\t\t\t<attribute name=\"$port\" value=\"{6}\" />\n" + 
			"\t\t\t<attribute name=\"$version\" value=\"{7}\" />\n" + 
			"\t\t\t<attribute name=\"$platform\" value=\"{8}\" />\n" + 
			"\t\t\t<attribute name=\"$database\" value=\"{9}\" />\n" + 
			"\t\t\t<attribute name=\"$type\" value=\"{10}\" />\n" + 
			"\t\t</asset>\n", 
       		 new Object[] {this.name, this.id, this.name, this.description, this.description,
       				 		this.host, this.port, this.version, this.platform, this.engine_type, this.type,
       				 	    IGC_CLASS_NAME});
	}
	
	public String toFlowXML() {
		return MessageFormat.format("\t\t<asset class=\"{0}\" repr=\"{1}\" ID=\"{2}\">\n" +
				"\t\t\t<attribute name=\"name\" value=\"{1}\" />\n" +
				"\t\t</asset>\n", 
				new Object[] {IGC_CLASS_NAME, this.name, this.id});
	}
	
	public static void main(String[] args) {
		
		Datastore ds = new Datastore("ds1", "TESTDB", "TESTDB DB2 database on troia", "troia", "10901", 
				"V11R3M3T0BCDD_BR_LSTYTHIK_96_7", "Java VM", "DB2 for LUW", "Dual");

		System.out.println(ds.toAssetXML());
	}
}
