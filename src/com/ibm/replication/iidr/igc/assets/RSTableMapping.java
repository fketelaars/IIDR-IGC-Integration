package com.ibm.replication.iidr.igc.assets;


import java.text.MessageFormat;

import org.apache.log4j.Logger;

public class RSTableMapping {

	
	public static final String IGC_CLASS_NAME = "$IIDR-RSTableMapping";
	
	final static Logger logger = Logger.getLogger(RSTableMapping.class.getName());
	
	private String id;
	private String schema;
	private String table;
	private Boolean structure_only;
	private String parent_id;
	
	public RSTableMapping (String id, String schema, String table, String structure_only, String parent_id) {
		
		this.id = id;
		this.schema = schema;
		this.table = table;
		this.structure_only = new Boolean(structure_only);
		this.parent_id = parent_id;
		
	}
	
	public String toXML() {
		return MessageFormat.format("\t\t<asset class=\"{5}\" repr=\"{0}.{1} - {0}.{1}\" ID=\"{2}\">\n" +
			"\t\t\t<attribute name=\"name\" value=\"{0}.{1} - {0}.{1}\" />\n" +
			"\t\t\t<attribute name=\"short_description\" value=\"Rule Set Table mapping for {0}.{1}\" />\n" +
			"\t\t\t<attribute name=\"long_description\" value=\"Rule Set Table mapping for {0}.{1}\" />\n" +
			"\t\t\t<attribute name=\"$schema\" value=\"{0}\" />\n" +
			"\t\t\t<attribute name=\"$table\" value=\"{1}\" />\n" + 
			"\t\t\t<attribute name=\"$structure_only\" value=\"{3}\" />\n" + 
			"\t\t\t<reference name=\"$Subscription\" assetIDs=\"{4}\" />\n" +
			"\t\t</asset>\n", 
       		 new Object[] {this.schema, this.table, this.id, this.structure_only, this.parent_id, IGC_CLASS_NAME});
	}

	public String toFlowXML() {
		return MessageFormat.format("\t\t<asset class=\"{4}\" repr=\"{0}.{1} - {0}.{1}\" ID=\"{2}\">\n" +
				"\t\t\t<attribute name=\"name\" value=\"{0}.{1} - {0}.{1}\" />\n" +
				"\t\t\t<reference name=\"$Subscription\" assetIDs=\"{3}\" />\n" +
				"\t\t</asset>\n", 
				new Object[] {this.schema, this.table, this.id, this.parent_id, IGC_CLASS_NAME});
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getParentID() {
		return this.parent_id;
	}
	
	public String getSchema() {
		return this.schema;
	}
	
	public String getTable() {
		return this.table;
	}
	
	public String getName() {
		return MessageFormat.format("{0}.{1} - {0}.{1}", new Object[] {this.schema, this.table});
	}
	
	public String toString() {
		return MessageFormat.format("{0}.{1}", new Object[] {this.schema, this.table});
		
	}
		public static void main(String[] args) {
		
		RSTableMapping tableMapping = new RSTableMapping("rstm1", "DISCOUNT", "TAB1", "No", "sub1");
		
		System.out.println(tableMapping.toXML());
		System.out.println(tableMapping.toString());
	}

}
