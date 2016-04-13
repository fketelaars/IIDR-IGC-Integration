package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;

public class Host {
	
	private String id;
	private String name;
	
	public Host(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toFlowXML() {
		
		return MessageFormat.format("\t\t<asset class=\"host\" repr=\"{0}\" ID=\"{1}\">\n" + 
				"\t\t\t<attribute name=\"name\" value=\"{0}\"/>\n\t\t</asset>\n", 
				new Object[] {this.getName(), this.getID()});
	}
}
