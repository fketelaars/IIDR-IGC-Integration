package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;

public class DataFile {
	
	private String id;
	private String record_id;
	private String name;
	private String record_name;
	private String path;
	private String host;
	private String host_id;
	private String table_mapping_id;
	
	public DataFile(String id, String name, String path, String host, String host_id, String table_mapping_id) {
		this.id = id;
		this.record_id = id + "fr";
		this.name = name + ".txt";
		this.record_name = name;
		this.path = path;
		this.host = host;
		this.host_id = host_id;
		this.table_mapping_id = table_mapping_id;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getParentID() {
		return this.host_id;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getRecordID() {
		return this.record_id;
	}
	
	public String getTableMappingID() {
		return this.table_mapping_id;
	}
	
	public String toFlowXML() {
		
		String flowXML = MessageFormat.format("\t\t<asset class=\"data_file\" repr=\"{0}\" ID=\"{1}\">\n" + 
									"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n" +  	
									"\t\t\t<attribute name=\"path\" tag=\"true\" value=\"{2}\"/>\n" + 
									"\t\t\t<reference name=\"parent_folder_or_host\" assetIDs=\"{3}\"/>\n\t\t</asset>\n",
									new Object[] {this.getName(), this.getID(), this.getPath(), this.getParentID()});
		
		flowXML += MessageFormat.format("\t\t<asset class=\"data_file_record\" repr=\"{0}\" ID=\"{1}\">\n" + 
				"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n" + 
		"\t\t\t<reference name=\"data_file\" assetIDs=\"{2}\"/>\n" + 
		"\t\t</asset>\n",
		new Object[] {this.record_name, this.getRecordID(), this.getID()});
			
		return flowXML;
		
	}
}
