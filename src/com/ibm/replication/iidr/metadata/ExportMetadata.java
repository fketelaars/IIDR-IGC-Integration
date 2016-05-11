/****************************************************************************
 ** Licensed Materials - Property of IBM 
 ** IBM InfoSphere Change Data Capture
 ** 5724-U70
 ** 
 ** (c) Copyright IBM Corp. 2001, 2016 All rights reserved.
 ** 
 ** The following sample of source code ("Sample") is owned by International 
 ** Business Machines Corporation or one of its subsidiaries ("IBM") and is 
 ** copyrighted and licensed, not sold. You may use, copy, modify, and 
 ** distribute the Sample for your own use in any form without payment to IBM.
 ** 
 ** The Sample code is provided to you on an "AS IS" basis, without warranty of 
 ** any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR 
 ** IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 ** MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do 
 ** not allow for the exclusion or limitation of implied warranties, so the above 
 ** limitations or exclusions may not apply to you. IBM shall not be liable for 
 ** any damages you suffer as a result of using, copying, modifying or 
 ** distributing the Sample, even if IBM has been advised of the possibility of 
 ** such damages.
 *****************************************************************************/

package com.ibm.replication.iidr.metadata;

import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ibm.replication.cdc.scripting.*;
import com.ibm.replication.iidr.igc.assets.Assets;
import com.ibm.replication.iidr.igc.assets.Datastore;
import com.ibm.replication.iidr.igc.assets.Subscription;
import com.ibm.replication.iidr.igc.assets.TableMapping;
import com.ibm.replication.iidr.igc.flow.Flows;
import com.ibm.replication.iidr.igcrest.IGCRest;
import com.ibm.replication.iidr.igcrest.IGCRestException;

public class ExportMetadata {

	final static String VERSION = "1.0";
	final static String BUILD = "500";

	final static int ERR_DATASTORE_UNREACHABLE = -2225;
	final static int ERR_DATASTORE_NOT_SUPPORTED = -2202;
	final static int ERR_DATASTORES_NOT_SAME_TYPE = -2233;

	final private static String DATASTORE_TYPE_DATASTAGE = "IBM InfoSphere DataStage";

	static Logger logger;

	private Settings settings;
	private ExportMetadataParms parms;

	private EmbeddedScript script;
	private Assets assets;
	private Flows flows;

	public ExportMetadata(String[] commandLineArguments) throws ConfigurationException, ExportMetadataParmsException,
			EmbeddedScriptException, ExportMetadataException, MalformedURLException {
		System.setProperty("log4j.configuration",
				new File(".", File.separatorChar + "conf" + File.separatorChar + "log4j.properties").toURI().toURL()
						.toString());
		// logger =
		// Logger.getLogger("com.ibm.replication.iidr.metadata.ExportMetadata");
		logger = Logger.getLogger(ExportMetadata.class.getName());

		logger.info(MessageFormat.format("Starting metadata integration - v{0}.{1}",
				new Object[] { ExportMetadata.VERSION, ExportMetadata.BUILD }));

		settings = new Settings("conf" + File.separator + this.getClass().getSimpleName() + ".properties");
		parms = new ExportMetadataParms(commandLineArguments);

		assets = new Assets();

		// If the debug option was set, make sure that all debug messages are
		// logged
		if (parms.debug) {
			Logger.getRootLogger().setLevel(Level.DEBUG);
		}

		// Collect the metadata
		collectMetadata();

		Utils.writeContentToFile(settings.defaultDataPath + File.separator + parms.datastore + "_subscription.xml",
				assets.toXML());

		flows = new Flows(assets);
		Utils.writeContentToFile(settings.defaultDataPath + File.separator + parms.datastore + "_subscription_flow.xml",
				flows.toXML());

		if (!parms.previewOnly) {
			try {

				IGCRest igcRest = new IGCRest(settings.isHostName, settings.isPort, settings.isUserName,
						settings.isPassword, settings.trustSelfSignedCertificates);

				igcRest.uploadBundleIfMissing(settings.bundleFilePath, parms.updateBundle);

				igcRest.postAssets(assets.toXML());

				igcRest.postFlows(flows.toXML());

			} catch (IGCRestException e) {
				logger.error(MessageFormat.format("REST API call for failed with code {0} and message: {1}",
						new Object[] { e.httpCode, e.message }));
				throw new ExportMetadataException(e);
			} catch (Exception e) {
				throw new ExportMetadataException(e);
			}
		} else {
			String previewFileName = settings.defaultDataPath + File.separator + parms.datastore
					+ "_ExportMetadata_Preview.txt";
			logger.debug(MessageFormat.format("Writing output to {0}", new Object[] { previewFileName }));
			Utils.writeContentToFile(previewFileName, flows.preview());
		}

		logger.info("Finished exporting the CDC metadata");

	}

	private void collectMetadata() throws EmbeddedScriptException, ExportMetadataException {

		script = new EmbeddedScript();

		try {
			// Opening CHCCLP script (mandatory for embedded CHCCLP)
			logger.debug("Opening CHCCLP script");
			script.open();

			// Connect to Access Server
			logger.info(MessageFormat.format("Connecting to Access Server at host name {0} and port {1,number,#}",
					new Object[] { settings.asHostName, settings.asPort }));
			try {
				script.execute(MessageFormat.format(
						"connect server hostname {0} port {1,number,#} username {2} password {3}", new Object[] {
								settings.asHostName, settings.asPort, settings.asUserName, settings.asPassword }));
				// Connect to source datastore
				logger.debug(
						MessageFormat.format("Connecting to source datastore {0}", new Object[] { parms.datastore }));
				try {
					script.execute(MessageFormat.format("connect datastore name {0} context source",
							new Object[] { parms.datastore }));
					// Get source datastore parameters and store in asset object
					logger.debug(MessageFormat.format("Getting attributes for source datastore {0}",
							new Object[] { parms.datastore }));
					Datastore sourceDatastore = addDatastoreToAssets(parms.datastore);
					// Now process the specified (or all) subscriptions
					collectSubscriptions(sourceDatastore);
				} catch (EmbeddedScriptException e) {
					throw new ExportMetadataException(MessageFormat.format("Error connecting to datastore {0}: {1}",
							new Object[] { parms.datastore, script.getResultMessage() }));
				}
			} catch (EmbeddedScriptException e) {
				throw new ExportMetadataException(MessageFormat.format("Error connecting to Access Server: {0}",
						new Object[] { script.getResultMessage() }));
			}

			logger.debug(
					MessageFormat.format("Disconnecting from source datastore {0}", new Object[] { parms.datastore }));
			script.execute(MessageFormat.format("disconnect datastore name {0}", new Object[] { parms.datastore }));

		} catch (EmbeddedScriptException e) {

			switch (e.getResultCode()) {
			case ERR_DATASTORE_UNREACHABLE:
				logger.warn(MessageFormat.format("Datastore {0} is unreachable", new Object[] { parms.datastore }));
				break;
			case ERR_DATASTORE_NOT_SUPPORTED:
				logger.warn(MessageFormat.format("Datastore {0} is not supported", new Object[] { parms.datastore }));
				break;
			default:
				throw e;
			}

		}

	}

	// Add a datastore to the assets
	private Datastore addDatastoreToAssets(String datastoreName) throws EmbeddedScriptException {
		script.execute(MessageFormat.format("show datastore name {0}", new Object[] { datastoreName }));
		ResultStringKeyValues datastoreInfo = (ResultStringKeyValues) script.getResult();
		Datastore datastore = assets.addDatastore(datastoreInfo.getValue("Name"), datastoreInfo.getValue("Description"),
				datastoreInfo.getValue("Host Name"), datastoreInfo.getValue("Port"), datastoreInfo.getValue("Version"),
				datastoreInfo.getValue("Platform"), datastoreInfo.getValue("Database"), datastoreInfo.getValue("Type"));
		return datastore;
	}

	// Get details for subsriptions of datastore
	private void collectSubscriptions(Datastore sourceDatastore) throws EmbeddedScriptException {
		ArrayList<String> subscriptionNames = new ArrayList<String>();
		// If no subscriptions were specified with the -s parameter, take all
		// subscriptions from the datastore
		if (parms.subscription == null) {
			logger.info(MessageFormat.format("Listing subscriptions for datastore {0}",
					new Object[] { sourceDatastore.getName() }));
			script.execute(MessageFormat.format("list subscriptions filter datastore name {0}",
					new Object[] { sourceDatastore.getName() }));
			ResultStringTable subscriptionsTable = (ResultStringTable) script.getResult();
			// For every directly mapped table, list the column mappings
			for (int tableRow = 0; tableRow < subscriptionsTable.getRowCount(); tableRow++) {
				subscriptionNames.add(subscriptionsTable.getValueAt(tableRow, "SUBSCRIPTION"));
			}
		} else {
			subscriptionNames = new ArrayList<String>(Arrays.asList(parms.subscription.split(",", -1)));
		}
		// Now get details for all selected subscriptions
		for (String subscriptionName : subscriptionNames) {
			logger.debug(
					MessageFormat.format("Getting details for subscription {0}", new Object[] { subscriptionName }));
			try {
				script.execute(MessageFormat.format("show subscription name {0}", new Object[] { subscriptionName }));
				ResultStringKeyValues subscriptionInfo = (ResultStringKeyValues) script.getResult();
				String targetDatastoreName = subscriptionInfo.getValue("Target Datastore");
				// If target datastore is not the same as source and if the
				// details have not been recorded yet
				if (!subscriptionInfo.getValue("Source Datastore").equals(targetDatastoreName)) {
					if (assets.datastoreExists(targetDatastoreName) == null) {
						logger.debug(MessageFormat.format("Connecting to target datastore {0}",
								new Object[] { targetDatastoreName }));
						script.execute(MessageFormat.format("connect datastore name {0} context target",
								new Object[] { targetDatastoreName }));
						addDatastoreToAssets(targetDatastoreName);
					}
				}
				// If the code gets here, it means that info from the target
				// datastore was retrieved
				Subscription subscription = assets.addSubscription(subscriptionInfo.getValue("Name"),
						subscriptionInfo.getValue("Description"), subscriptionInfo.getValue("Source Datastore"),
						subscriptionInfo.getValue("Target Datastore"), subscriptionInfo.getValue("Source ID"),
						subscriptionInfo.getValue("TCP Host"), subscriptionInfo.getValue("Firewall Port"),
						subscriptionInfo.getValue("Persistency"), sourceDatastore);
				// Now get table mappings
				collectTableMappings(subscription);
				if (!subscriptionInfo.getValue("Source Datastore").equals(targetDatastoreName)) {
					logger.debug(MessageFormat.format("Disconnecting from target datastore {0}",
							new Object[] { targetDatastoreName }));
					script.execute(MessageFormat.format("disconnect datastore name {0}",
							new Object[] { targetDatastoreName }));
				}
			} catch (EmbeddedScriptException e) {
				logger.warn(MessageFormat.format(
						"Error while retrieving details for subscription {0}; subscription is ignored. Error: {1}",
						new Object[] { subscriptionName, script.getResultMessage() }));
			}
		}
	}

	private void collectTableMappings(Subscription subscription) throws EmbeddedScriptException {
		logger.debug(MessageFormat.format("Collecting table mappings for subscription {0}",
				new Object[] { subscription.getName() }));
		script.execute(MessageFormat.format("select subscription name {0}", new Object[] { subscription.getName() }));
		// If the target datastore is InfoSphere DataStage, add table assets
		// targeting flat files

		logger.debug(MessageFormat.format("Target datastore for subscription {0} is {1}. Type of datastore is {2}",
				new Object[] { subscription.getName(), subscription.getTargetDatastoreName(),
						assets.getDatastoreByID(subscription.getTargetDatastoreID()).getDatabase() }));

		if (assets.getDatastoreByID(subscription.getTargetDatastoreID()).getDatabase()
				.equals(DATASTORE_TYPE_DATASTAGE)) {
			logger.debug(MessageFormat.format("Collecing DataStage table mappings for subscription {0}",
					new Object[] { subscription.getName() }));
			script.execute(
					MessageFormat.format("list table mappings name {0}", new Object[] { subscription.getName() }));
			ResultStringTable mappingsTables = (ResultStringTable) script.getResult();
			for (int tmTableRow = 0; tmTableRow < mappingsTables.getRowCount(); tmTableRow++) {
				script.execute(MessageFormat.format("select table mapping sourceSchema {0} sourceTable {1}",
						new Object[] { Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
								Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")) }));
				script.execute("show table mapping");
				ResultStringKeyValues flatFileTarget = (ResultStringKeyValues) script.getResult();
				assets.addTableToFlatFileMapping(Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						flatFileTarget.getValue("Directory"), mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE"),
						mappingsTables.getValueAt(tmTableRow, "METHOD"),
						mappingsTables.getValueAt(tmTableRow, "PREVENT RECURSION"), subscription.getID());
			}
			// If the target datastore is a database, add table to table assets
		} else {
			logger.debug(MessageFormat.format("Collecting database table mappings for subscription {0}",
					new Object[] { subscription.getName() }));
			script.execute(
					MessageFormat.format("list table mappings name {0}", new Object[] { subscription.getName() }));
			ResultStringTable mappingsTables = (ResultStringTable) script.getResult();
			for (int tmTableRow = 0; tmTableRow < mappingsTables.getRowCount(); tmTableRow++) {
				script.execute(MessageFormat.format("select table mapping sourceSchema {0} sourceTable {1}",
						new Object[] { Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
								Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")) }));
				TableMapping tableMapping = assets.addTableToTableMapping(
						Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
						mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE"),
						mappingsTables.getValueAt(tmTableRow, "METHOD"),
						mappingsTables.getValueAt(tmTableRow, "PREVENT RECURSION"), subscription.getID());
				// Now that the table asset has been added, also add the column
				// assets
				collectColumnMappings(tableMapping);
			}
			// Collect Rule Set mappings for subscriptions
			collectRSMappings(subscription);
		}
	}

	private void collectRSMappings(Subscription subscription) throws EmbeddedScriptException {
		script.execute(MessageFormat.format("select subscription name {0}", new Object[] { subscription.getName() }));
		// Listing rule sets
		try {
			logger.info(MessageFormat.format("Getting rule sets for subscription {0}",
					new Object[] { subscription.getName() }));
			script.execute("list rule sets");
			ResultStringTable ruleSets = (ResultStringTable) script.getResult();
			for (int rsTableRow = 0; rsTableRow < ruleSets.getRowCount(); rsTableRow++) {
				assets.addRuleSet(ruleSets.getValueAt(rsTableRow, "RULE SET NAME"),
						ruleSets.getValueAt(rsTableRow, "SCHEMA"), ruleSets.getValueAt(rsTableRow, "INCLUDE TABLES"),
						ruleSets.getValueAt(rsTableRow, "EXCLUDE TABLES"),
						ruleSets.getValueAt(rsTableRow, "STRUCTURE ONLY"), ruleSets.getValueAt(rsTableRow, "CONTEXT"),
						subscription.getID());
			}
			// Listing rule set tables
			logger.info(MessageFormat.format("Listing rule set tables for subscription {0}",
					new Object[] { subscription.getName() }));
			ResultStringTable ruleSetTables = (ResultStringTable) script.getResult();
			for (int rsTableRow = 0; rsTableRow < ruleSetTables.getRowCount(); rsTableRow++) {
				assets.addRSTableMapping(ruleSetTables.getValueAt(rsTableRow, "SCHEMA"),
						ruleSetTables.getValueAt(rsTableRow, "TABLE NAME"),
						ruleSetTables.getValueAt(rsTableRow, "STRUCTURE ONLY"), subscription.getID());
			}
		} catch (EmbeddedScriptException ese) {
			logger.debug(MessageFormat.format("Rule set mappings not applicable to subscription {0}",
					new Object[] { subscription.getName() }));
		}
	}

	private void collectColumnMappings(TableMapping tableMapping) throws EmbeddedScriptException {
		logger.debug(MessageFormat.format("Adding column mappings for table {0}.{1}",
				new Object[] { tableMapping.getSourceSchema(), tableMapping.getSourceTable() }));
		script.execute(MessageFormat.format(
				"select table mapping sourceSchema {0} sourceTable {1} targetSchema {2} targetTable {3}",
				new Object[] { tableMapping.getSourceSchema(), tableMapping.getSourceTable(),
						tableMapping.getTargetSchema(), tableMapping.getTargetTable() }));

		script.execute("list column mappings");

		ResultStringTable tableColumns = (ResultStringTable) script.getResult();
		for (int columnRow = 0; columnRow < tableColumns.getRowCount(); columnRow++) {
			assets.addColumnMapping(tableColumns.getValueAt(columnRow, "SOURCE"),
					tableColumns.getValueAt(columnRow, "TARGET COLUMN"),
					tableColumns.getValueAt(columnRow, "INITIAL VALUE"), tableMapping.getID());
		}
	}

	public static void main(String[] args) throws ConfigurationException {

		// Only set arguments when testing
		if (args.length == 1 && args[0].equalsIgnoreCase("*Testing*")) {
			// args = "-p preview.txt -ds TESTDB,ORCL".split(" ");
			// args = "-d -ub -p preview.txt -ds CDC_Oracle_cdcdemoa -sub
			// SARC".split(" ");
			args = "-d -ds CDC_DB2".split(" ");
			// args = " -ds TESTDB -p".split(" ");
		}
		try {
			new ExportMetadata(args);
		} catch (EmbeddedScriptException | ExportMetadataParmsException | ExportMetadataException
				| MalformedURLException ese) {
			System.err.println("Error while exporting the metadata: " + ese.getMessage());
		}

	}

}
