package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;

public class Column {
	
	private String id;
	private String name;
	private String table_id;
	private String column_mapping_id;
	private boolean is_source;
	
	public Column(String id, String name, String table_id, String column_mapping_id, boolean is_source) {
		this.id = id;
		this.name = name;
		this.table_id = table_id;
		this.column_mapping_id = column_mapping_id;
		this.is_source = is_source;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getParentID() {
		return this.table_id;
	}
	
	public boolean isSource() {
		return this.is_source;
	}
	
	public String getColumnMappingID() {
		return this.column_mapping_id;
	}

	public String toFlowXML() {
		
		return MessageFormat.format("\t\t<asset class=\"database_column\" repr=\"{0}\" ID=\"{1}\"  matchByName=\"true\">\n" + 
				"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n" +
				"\t\t\t<reference name=\"database_table_or_view\" assetIDs=\"{2}\"/>\n\t\t</asset>\n", 
				new Object[] {StringEscapeUtils.escapeXml(this.getName()), this.getID(), this.getParentID()});
	}
}
