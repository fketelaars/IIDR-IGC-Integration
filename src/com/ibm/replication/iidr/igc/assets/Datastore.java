package com.ibm.replication.iidr.igc.assets;

import java.text.MessageFormat;

public class Datastore {

	public static final String IGC_CLASS_NAME = "$IIDR-Datastore";
	public static final String DATABASE_TYPE_DATASTAGE = "IBM InfoSphere DataStage";
	
	private String id;
	private String name;
	private String host;
	private String description;
	private String port;
	private String version;
	private String platform;
	private String database;
	private String type;
	private String database_name;
	
	public Datastore(String id, String name, String description, String host, String port, String version, 
				     String platform, String database, String type) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.host = host.toUpperCase();
		this.port = port;
		this.version = version;
		this.platform = platform;
		this.database = database;
		try {
			this.database_name = description.split("DATABASE:")[1].split(" ")[0];
		} catch (Exception ex) {
			this.database_name = this.name;
		}
		this.type = type;	
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getDatabaseName() {
		return this.database_name;
	}
	
	public String getDatabase() {
		return this.database;
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
       				 		this.host, this.port, this.version, this.platform, this.database, this.type,
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
