package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;

public class Schema {
	
	private String id;
	private String name;
	private String host;
	private String database;
	private String database_id;
	
	
	public Schema(String id, String name, String host, String database, String database_id) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.database = database;
		this.database_id = database_id;
		
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getDatabase() {
		return this.database;
	}

	public String getParentID() {
		return this.database_id;
	}

	public String toFlowXML() {
		
		return MessageFormat.format("\t\t<asset class=\"database_schema\" repr=\"{0}\" ID=\"{1}\"  matchByName=\"true\">\n" + 
				"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n" +
				"\t\t\t<reference name=\"database\" assetIDs=\"{2}\"/>\n\t\t</asset>\n", 
					new Object[] {this.getName(), this.getID(), this.getParentID()});
	}
	
}
