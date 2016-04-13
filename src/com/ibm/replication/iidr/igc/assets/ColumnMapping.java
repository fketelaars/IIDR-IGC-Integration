package com.ibm.replication.iidr.igc.assets;


import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;

public class ColumnMapping {

	public static final String IGC_CLASS_NAME = "$IIDR-ColumnMapping";
	
	private String id;
	private String source_column;
	private String target_column;
	private String initial_value;
	private String parent_id;
	
	public ColumnMapping (String id, String source_column, String target_column, String initial_value, String parent_id) {
		this.id = id;
		this.source_column = source_column;
		this.target_column = target_column;
		this.initial_value = initial_value;
		this.parent_id = parent_id;
	}
	
	public String toXML() {
		
		StringEscapeUtils.escapeXml("sdfsdf");
		
		return MessageFormat.format("\t\t<asset class=\"{5}\" repr=\"{0} - {1}\" ID=\"{2}\">\n" +
			"\t\t\t<attribute name=\"name\" value=\"{0} - {1}\" />\n" +
			"\t\t\t<attribute name=\"short_description\" value=\"Column mapping from {0} {1}\" />\n" +
			"\t\t\t<attribute name=\"long_description\" value=\"Column mapping from {0} {1}\" />\n" +
			"\t\t\t<attribute name=\"$source_column\" value=\"{0}\" />\n" +
			"\t\t\t<attribute name=\"$target_column\" value=\"{1}\" />\n" + 
			"\t\t\t<attribute name=\"$initial_value\" value=\"{3}\" />\n" +
			"\t\t\t<reference name=\"$TableMapping\" assetIDs=\"{4}\" />\n" +
			"\t\t</asset>\n", 
       		 new Object[] {StringEscapeUtils.escapeXml(this.source_column), StringEscapeUtils.escapeXml(this.target_column), this.id, this.initial_value, this.parent_id, IGC_CLASS_NAME});
		
		
	}
	
	public String toFlowXML() {
		return MessageFormat.format("\t\t<asset class=\"{4}\" repr=\"{0} - {1}\" ID=\"{2}\">\n" +
				"\t\t\t<attribute name=\"name\" value=\"{0} - {1}\" />\n" +
				"\t\t\t<reference name=\"$TableMapping\" assetIDs=\"{3}\" />\n" +
				"\t\t</asset>\n", 
				new Object[] {StringEscapeUtils.escapeXml(this.source_column), StringEscapeUtils.escapeXml(this.target_column), this.id, this.parent_id, IGC_CLASS_NAME});
	}
	
	public String getSourceColumn() {
		return this.source_column;
	}
	
	public String getTargetColumn() {
		return this.target_column;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String toString() {
		return MessageFormat.format("{0} - {1}", new Object[] {this.source_column, this.target_column});
		
	}
	
	public String getName() {
		return MessageFormat.format("{0} - {1}", new Object[] {this.source_column, this.target_column});
	}
	public String getParentID() {
		return this.parent_id;
	}
	
	public static void main(String[] args) {
		
		ColumnMapping columnMapping = new ColumnMapping("cm1", "ALBUMID", "ALBUMID", "", "tm1");
		
		System.out.println(columnMapping.toXML());
		System.out.println(columnMapping.toString());
	}

}
