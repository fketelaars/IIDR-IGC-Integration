package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;

public class Table {
	
	private String id;
	private String name;
	private String host;
	private String database;
	private String schema;
	private String schema_id;
	private String table_mapping_id;
	private boolean is_source;
	
	
	public Table(String id, String name, String host, String database, String schema, String schema_id, String table_mapping_id) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.database = database;
		this.schema = schema;
		this.schema_id = schema_id;
		this.table_mapping_id = table_mapping_id;
		this.is_source = true;
		
	}
	
	public Table(String id, String name, String host, String database, String schema, String schema_id, String table_mapping_id, boolean is_source) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.database = database;
		this.schema = schema;
		this.schema_id = schema_id;
		this.table_mapping_id = table_mapping_id;
		this.is_source = is_source;
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

	public String getSchema() {
		return this.schema;
	}
	
	public String getParentID() {
		return this.schema_id;
	}

	public String getTableMappingID() {
		return this.table_mapping_id;
	}

	public boolean isSource() {
		return this.is_source;
	}
	
	public String toFlowXML() {
		
		return MessageFormat.format("\t\t<asset class=\"database_table\" repr=\"{0}\" ID=\"{1}\"  matchByName=\"true\">\n" + 
				"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n" +
				"\t\t\t<reference name=\"database_schema\" assetIDs=\"{2}\"/>\n\t\t</asset>\n", 
				new Object[] {this.getName(), this.getID(), this.getParentID()});
	}
	
}
