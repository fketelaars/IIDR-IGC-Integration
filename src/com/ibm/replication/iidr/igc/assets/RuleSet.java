package com.ibm.replication.iidr.igc.assets;


import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

public class RuleSet {

	public static final String IGC_CLASS_NAME = "$IIDR-RuleSet";
	
	final static Logger logger = Logger.getLogger(RuleSet.class.getName());
	
	private String id;
	private String name;
	private String schema;
	private String include_tables;
	private String exclude_tables;
	private Boolean structure_only;
	private String context;
	private String parent_id;
	
	public RuleSet (String id, String name, String schema, String include_tables, String exclude_tables, 
			String structure_only, String context, String parent_id) {
		this.id = id;
		this.name = name;
		this.schema = schema;
		this.include_tables = include_tables;
		this.exclude_tables = exclude_tables;
		this.structure_only = new Boolean(structure_only);
		this.context = context;
		this.parent_id = parent_id;
	}
	
	public String toXML() {
		return MessageFormat.format("\t\t<asset class=\"{8}\" repr=\"{0}\" ID=\"{1}\">\n" +
			"\t\t\t<attribute name=\"name\" value=\"{0}\" />\n" +
			"\t\t\t<attribute name=\"short_description\" value=\"Rule set mapping\" />\n" +
			"\t\t\t<attribute name=\"long_description\" value=\"Rule set mapping\" />\n" +
			"\t\t\t<attribute name=\"$schema\" value=\"{2}\" />\n" +
			"\t\t\t<attribute name=\"$include_tables\" value=\"{3}\" />\n" + 
			"\t\t\t<attribute name=\"$exclude_tables\" value=\"{4}\" />\n" +
			"\t\t\t<attribute name=\"$structure_only\" value=\"{5}\" />\n" +
			"\t\t\t<attribute name=\"$context\" value=\"{6}\" />\n" +
			"\t\t\t<reference name=\"$Subscription\" assetIDs=\"{7}\" />\n" +
			"\t\t</asset>\n", 
       		 new Object[] {this.name, this.id, this.schema, StringEscapeUtils.escapeXml(this.include_tables), 
       				 StringEscapeUtils.escapeXml(this.exclude_tables), this.structure_only, this.context, this.parent_id, IGC_CLASS_NAME});
	}
	
	public String getID() {
		return this.id;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getParentID() {
		return this.parent_id;
	}
	
	public static void main(String[] args) {
		
		RuleSet ruleSet = new RuleSet("rs1", "A_TABLES", "TESTS", "A*", "", "No", "", "sub1");
		
		System.out.println(ruleSet.toXML());
		System.out.println(ruleSet.toString());
	}

}
