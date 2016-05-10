package com.ibm.replication.iidr.igc.assets;


import java.text.MessageFormat;

import org.apache.log4j.Logger;

public class TableMapping {

	
	public static final String IGC_CLASS_NAME = "$IIDR-TableMapping";
	public static final int MAPPING_TYPE_TABLE = 1;
	public static final int MAPPING_TYPE_FLAT_FILE = 2;
	
	final static Logger logger = Logger.getLogger(TableMapping.class.getName());
	
	
	private String id;
	private String source_schema;
	private String source_table;
	private String target_schema;
	private String target_table;
	private String mapping_type;
	private String method;
	private Boolean prevent_recursion;
	private String parent_id;
	private String directory;
	private int tm_mapping_type;

	
	public TableMapping (String id, String source_schema, String source_table, String target_schema, 
			String target_table, String mapping_type, String method, String prevent_recursion, String parent_id) {
		
		this.id = id;
		this.source_schema = source_schema;
		this.source_table = source_table;
		this.directory = "";
		this.target_schema = target_schema;
		this.target_table = target_table;
		this.mapping_type = mapping_type;
		this.method = method;
		this.prevent_recursion = new Boolean(prevent_recursion);
		this.parent_id = parent_id;
		this.tm_mapping_type = TableMapping.MAPPING_TYPE_TABLE;

	}
	
	public TableMapping (String id, String source_schema, String source_table, String directory,
			String mapping_type, String method, String prevent_recursion, String parent_id) {
		
		this.id = id;
		this.source_schema = source_schema;
		this.source_table = source_table;
		this.directory = directory;
		this.target_schema = "";
		this.target_table = "";
		this.mapping_type = mapping_type;
		this.method = method;
		this.prevent_recursion = new Boolean(prevent_recursion);
		this.parent_id = parent_id;
		this.tm_mapping_type = TableMapping.MAPPING_TYPE_FLAT_FILE;

	}
	
	public String getParentID() {
		return this.parent_id;
	}
	
	private String getTarget() {
		
		String target = "";
		
		
		if (this.tm_mapping_type == TableMapping.MAPPING_TYPE_TABLE) {
			target = this.target_schema + "." + this.target_table;
		} else if (this.tm_mapping_type == TableMapping.MAPPING_TYPE_FLAT_FILE){
			target = this.directory;
		}
		
		return target;
	}
	
	public String toAssetXML() {
		
		
		return MessageFormat.format("\t\t<asset class=\"{9}\" repr=\"{0}.{1} - {3}\" ID=\"{2}\">\n" +
			"\t\t\t<attribute name=\"name\" value=\"{0}.{1} - {3}\" />\n" +
			"\t\t\t<attribute name=\"short_description\" value=\"Table mapping from {0}.{1} to {3}\" />\n" +
			"\t\t\t<attribute name=\"long_description\" value=\"Table mapping from {0}.{1} to {3}\" />\n" +
			"\t\t\t<attribute name=\"$source\" value=\"{0}.{1}\" />\n" +
			"\t\t\t<attribute name=\"$target\" value=\"{3}\" />\n" + 
			"\t\t\t<attribute name=\"$mapping_type\" value=\"{5}\" />\n" +
			"\t\t\t<attribute name=\"$method\" value=\"{6}\" />\n" +
			"\t\t\t<attribute name=\"$prevent_recursion\" value=\"{7}\" />\n" + 
			"\t\t\t<reference name=\"$Subscription\" assetIDs=\"{8}\" />\n" +
			"\t\t</asset>\n", 
       		 new Object[] {this.source_schema, this.source_table, this.id, this.getTarget(), null,
       				 this.mapping_type, this.method, this.prevent_recursion, this.parent_id, IGC_CLASS_NAME});
	}
	
	public String toFlowXML() {
		return MessageFormat.format("\t\t<asset class=\"{6}\" repr=\"{0}.{1} - {3}\" ID=\"{2}\">\n" +
				"\t\t\t<attribute name=\"name\" value=\"{0}.{1} - {3}\" />\n" +
				"\t\t\t<reference name=\"$Subscription\" assetIDs=\"{5}\" />\n" +
				"\t\t</asset>\n", 
				new Object[] {this.source_schema, this.source_table, this.id, this.getTarget(), null, this.parent_id, IGC_CLASS_NAME});
	}

	public String getID() {
		return this.id;
	}
	
	public String getDirectory() {
		return this.directory;
	}
	
	public String toString() {
		return MessageFormat.format("{0}.{1} - {2}", new Object[] {this.source_schema, this.source_table, 
				this.getTarget()});
		
	}
	
	public String getName() {
		return MessageFormat.format("{0}.{1} - {2}", new Object[] {this.source_schema, this.source_table, 
				this.getTarget()});
	}
	
	public String getSourceSchema() {
		return this.source_schema;
	}

	public String getSourceTable() {
		return this.source_table;
	}
	
	public String getTargetSchema() {
		return this.target_schema;
	}
	
	public String getTargetTable() {
		return this.target_table;
	}
	
	public int getTableMappingType() {
		return this.tm_mapping_type;
	}
	
	public static void main(String[] args) {
		
		TableMapping tableMapping = new TableMapping("tm1", "DISCOUNT", "TAB1", "NICE", "TAB2", "Standard", "Mirror", 
				"No", "sub1");
		
		System.out.println(tableMapping.toAssetXML());
		System.out.println(tableMapping.toString());
		
		
		tableMapping = new TableMapping("tm1", "DISCOUNT", "TAB1", "/tmp/cdd", "Flat File", "Mirror", 
				"No", "sub2");
		
		System.out.println(tableMapping.toAssetXML());
		System.out.println(tableMapping.toString());
		
	}

}
