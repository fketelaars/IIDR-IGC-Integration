package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;

public class Database {
	
	private String id;
	private String name;
	private String host;
	private String host_id;
	
	
	public Database(String id, String name, String host, String host_id) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.host_id = host_id;
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
	
	public String getParentID() {
		return this.host_id;
	}
	
	public String toFlowXML() {
		return MessageFormat.format("\t\t<asset class=\"database\" repr=\"{0}\" ID=\"{1}\">\n" + 
				"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n" +
				"\t\t\t<reference name=\"host\" assetIDs=\"{2}\"/>\n\t\t</asset>\n", 
					new Object[] {this.getName(), this.getID(), this.getParentID()});
	}
}
