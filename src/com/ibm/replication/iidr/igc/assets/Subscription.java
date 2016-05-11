package com.ibm.replication.iidr.igc.assets;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

public class Subscription {

	public static final String IGC_CLASS_NAME = "$IIDR-Subscription";
	
	final static Logger logger = Logger.getLogger(Subscription.class.getName());

	private String id;
	private String name;
	private String description;
	private String source_datastore;
	private String target_datastore;
	private String source_id;
	private String tcp_host;
	private String firewall_port;
	private Boolean persistency;
	private String parent_id;
	private String target_datastore_id;

	public Subscription(String id, String name, String description, String source_datastore, String target_datastore,
			String source_id, String tcp_host, String firewall_port, String persistency, String parent_id,
			String target_datastore_id) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.source_datastore = source_datastore.toUpperCase();
		this.target_datastore = target_datastore.toUpperCase();
		this.source_id = source_id;
		this.tcp_host = tcp_host.toUpperCase();
		this.firewall_port = firewall_port;
		this.persistency = new Boolean(persistency);
		this.parent_id = parent_id;
		this.target_datastore_id = target_datastore_id;
	}

	public void setTargetDatastoreID(String target_datastore_id) {
		this.target_datastore_id = target_datastore_id;
	}

	public String getTargetDatastoreID() {
		return this.target_datastore_id;
	}

	public String getParentID() {
		return this.parent_id;
	}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getSourceDatastore() {
		return this.source_datastore;
	}

	public String getTargetDatastoreName() {
		return this.target_datastore;
	}

	public String toString() {
		return this.name;
	}

	public String toAssetXML() {
		return MessageFormat.format(
				"\t\t<asset class=\"{12}\" repr=\"{0}\" ID=\"{1}\">\n"
						+ "\t\t\t<attribute name=\"name\" value=\"{2}\" />\n"
						+ "\t\t\t<attribute name=\"short_description\" value=\"{3}\" />\n"
						+ "\t\t\t<attribute name=\"long_description\" value=\"{4}\" />\n"
						+ "\t\t\t<attribute name=\"$source_datastore\" value=\"{5}\" />\n"
						+ "\t\t\t<attribute name=\"$target_datastore\" value=\"{6}\" />\n"
						+ "\t\t\t<attribute name=\"$source_id\" value=\"{7}\" />\n"
						+ "\t\t\t<attribute name=\"$tcp_host\" value=\"{8}\" />\n"
						+ "\t\t\t<attribute name=\"$firewall_port\" value=\"{9}\" />\n"
						+ "\t\t\t<attribute name=\"$persistency\" value=\"{10}\" />\n"
						+ "\t\t\t<reference name=\"$Datastore\" assetIDs=\"{11}\" />\n" + "\t\t</asset>\n",
				new Object[] { this.name, this.id, this.name, this.description, this.description, this.source_datastore,
						this.target_datastore, this.source_id, this.tcp_host, this.firewall_port, this.persistency,
						this.parent_id, IGC_CLASS_NAME });
	}

	public String toFlowXML() {
		return MessageFormat.format(
				"\t\t<asset class=\"{0}\" repr=\"{1}\" ID=\"{2}\">\n"
						+ "\t\t\t<attribute name=\"name\" value=\"{1}\" />\n"
						+ "\t\t\t<reference name=\"$Datastore\" assetIDs=\"{3}\" />\n" + "\t\t</asset>\n",
				new Object[] { IGC_CLASS_NAME, this.name, this.id, this.parent_id });
	}

	public static void main(String[] args) {

		Subscription sub = new Subscription("sub1", "BLA", "Replicating customer data", "ORCL", "TESTDB", "BLA",
				"Auto Select", "", "No", "ds1", "ds2");

		System.out.println(sub.toAssetXML());
	}
}
