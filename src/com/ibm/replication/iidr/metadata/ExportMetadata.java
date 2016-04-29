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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ibm.replication.cdc.scripting.*;
import com.ibm.replication.iidr.igc.assets.Assets;
import com.ibm.replication.iidr.igc.assets.ColumnMapping;
import com.ibm.replication.iidr.igc.assets.Datastore;
import com.ibm.replication.iidr.igc.assets.RSTableMapping;
import com.ibm.replication.iidr.igc.assets.RuleSet;
import com.ibm.replication.iidr.igc.assets.Subscription;
import com.ibm.replication.iidr.igc.assets.TableMapping;
import com.ibm.replication.iidr.igc.flow.Flows;
import com.ibm.replication.iidr.igcrest.IGCRest;
import com.ibm.replication.iidr.igcrest.IGCRestException;

public class ExportMetadata {

	final static String VERSION = "1.0";
	final static String BUILD = "300";

	final static int ERR_DATASTORE_UNREACHABLE = -2225;
	final static int ERR_DATASTORE_NOT_SUPPORTED = -2202;
	final static int ERR_DATASTORES_NOT_SAME_TYPE = -2233;

	final private static String MAPPING_TYPE_DATASTAGE = "Flat File";
	final private static String DATASTORE_TYPE_DATASTAGE = "IBM InfoSphere DataStage";

	final static Logger logger = Logger.getLogger("com.ibm.replication.iidr.metadata.ExportMetadata");
	private Settings settings;
	private ExportMetadataParms parms;

	private EmbeddedScript script;
	private Assets assets;
	private Flows flows;

	public ExportMetadata(String[] commandLineArguments) throws ConfigurationException, ExportMetadataParmsException,
			EmbeddedScriptException, ExportMetadataException {
		settings = new Settings("conf" + File.separator + this.getClass().getSimpleName() + ".properties");
		parms = new ExportMetadataParms(commandLineArguments);

		assets = new Assets();

		if (parms.debug) {
			logger.setLevel(Level.DEBUG);
		}

		// Collect the metadata
		collectMetadata();

		Utils.writeContentToFile(settings.defaultDataPath + File.separator + "subscription.xml", assets.toXML());

		flows = new Flows(assets);
		Utils.writeContentToFile(settings.defaultDataPath + File.separator + "subscriptionflow.xml.xml", flows.toXML());

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
			String previewFileName = settings.defaultDataPath + File.separator + "ExportMetadata_Preview.txt";
			logger.debug("Writing output to " + previewFileName);
			Utils.writeContentToFile(previewFileName, flows.preview());
		}

	}

	private void collectMetadata() {

		script = new EmbeddedScript();

		try {
			// Opening CHCCLP script (mandatory for embedded CHCCLP)
			logger.debug("Opening CHCCLP script");
			script.open();

			// Connect to Access Server
			logger.info("Connecting to Access Server");
			script.execute(MessageFormat.format(
					"connect server hostname {0} port {1,number,#} username {2} password {3}",
					new Object[] { settings.asHostName, settings.asPort, settings.asUserName, settings.asPassword }));
			if (script.getResultCode() == 0) {
				// Connect to source datastore
				logger.debug("Connecting to source datastore " + parms.datastore);
				script.execute(MessageFormat.format("connect datastore name {0} context source",
						new Object[] { parms.datastore }));
				// Get source datastore parameters and store in asset object
				if (script.getResultCode() == 0) {
					logger.debug("Getting attributes for source datastore " + parms.datastore);
					Datastore sourceDatastore = addDatastoreToAssets(parms.datastore);
					// Now process the specified (or all) subscriptions
					collectSubscriptions(sourceDatastore);
				} else
					throw new ExportMetadataException("Error connecting to datastore " + parms.datastore);
			} else
				throw new ExportMetadataException("Error connecting to Access Server");

			for (int tableRow = 0; tableRow < datastoresTable.getRowCount(); tableRow++) {

				if (datastoresTable.getValueAt(tableRow, "TYPE").equals("Target")) {
					continue;
				}

				String currentSourceDatastore = datastoresTable.getValueAt(tableRow, "DATASTORE");

				if (!parms.datastore.isEmpty() && !parms.datastore.equalsIgnoreCase(currentSourceDatastore)) {
					continue;
				}

				try {

					// Fetch data on source datastore

					// Gather info about datastore and create an asset
					ResultStringKeyValues datastoreTable = (ResultStringKeyValues) script.getResult();

					String currentDatastoreID = "";

					Datastore currDatastore = assets.datastoreExists(currentSourceDatastore);

					if (currDatastore == null) {
						currDatastore = new Datastore(assets.getNextDatastoreID(), datastoreTable.getValue("Name"),
								datastoreTable.getValue("Description"), datastoreTable.getValue("Host Name"),
								datastoreTable.getValue("Port"), datastoreTable.getValue("Version"),
								datastoreTable.getValue("Platform"), datastoreTable.getValue("Database"),
								datastoreTable.getValue("Type"));

						assets.addDatastore(currDatastore);

						currentDatastoreID = currDatastore.getID();

					} else {
						currentDatastoreID = currDatastore.getID();
					}

					// Connect to source datastore
					logger.info("Listing subscriptions of the datastore " + currentSourceDatastore);
					// script.execute(MessageFormat.format("list subscriptions
					// filter datastore name {0}",
					// new Object[] {currentSourceDatastore}));
					script.execute("list subscriptions");

					ResultStringTable subscriptionsTable = (ResultStringTable) script.getResult();

					// For every directly mapped table, list the column mappings
					for (int subTableRow = 0; subTableRow < subscriptionsTable.getRowCount(); subTableRow++) {

						String subscriptionName = subscriptionsTable.getValueAt(subTableRow, "SUBSCRIPTION");

						if (!parms.datastore.isEmpty() && !parms.subscription.isEmpty()
								&& !parms.subscription.equalsIgnoreCase(subscriptionName)) {
							continue;
						}

						String subSourceDatastore = subscriptionsTable.getValueAt(subTableRow, "SOURCE DATASTORE");

						if (!subSourceDatastore.equalsIgnoreCase(currentSourceDatastore)) {
							continue;
						}

						if (!assets.subscriptionExists(subscriptionName, subSourceDatastore)) {
							logger.info("\tGetting attributes for subscription " + subscriptionName);
							script.execute(MessageFormat.format("show subscription name {0}",
									new Object[] { subscriptionName }));

							ResultStringKeyValues subscriptionTable = (ResultStringKeyValues) script.getResult();

							Subscription currSubscription = new Subscription(assets.getNextSubscriptionID(),
									subscriptionTable.getValue("Name"), subscriptionTable.getValue("Description"),
									subscriptionTable.getValue("Source Datastore"),
									subscriptionTable.getValue("Target Datastore"),
									subscriptionTable.getValue("Source ID"), subscriptionTable.getValue("TCP Host"),
									subscriptionTable.getValue("Firewall Port"),
									subscriptionTable.getValue("Persistency"), currentDatastoreID);

							String currTargetDatastore = subscriptionTable.getValue("Target Datastore");

							boolean targetConnected = true;

							String targetDatastoreID = currentDatastoreID;

							if (currTargetDatastore.startsWith("External")) {
								targetConnected = false;
							} else if (!currentSourceDatastore.equalsIgnoreCase(currTargetDatastore)) {

								try {
									// Connect to target datastore
									logger.debug("\t\tFetching info for target datastore " + currTargetDatastore);
									script.execute(MessageFormat.format("connect datastore name {0} context target",
											new Object[] { currTargetDatastore }));

									// Fetch data on target datastore
									script.execute(MessageFormat.format("show datastore name {0}",
											new Object[] { currTargetDatastore }));

									// Gather info about datastore and create an
									// asset
									datastoreTable = (ResultStringKeyValues) script.getResult();

									Datastore foundTargetDatastore = assets.datastoreExists(currTargetDatastore);

									if (foundTargetDatastore == null) {

										foundTargetDatastore = new Datastore(assets.getNextDatastoreID(),
												datastoreTable.getValue("Name"), datastoreTable.getValue("Description"),
												datastoreTable.getValue("Host Name"), datastoreTable.getValue("Port"),
												datastoreTable.getValue("Version"), datastoreTable.getValue("Platform"),
												datastoreTable.getValue("Database"), datastoreTable.getValue("Type"));

										assets.addDatastore(foundTargetDatastore);

									}

									targetDatastoreID = foundTargetDatastore.getID();

									targetConnected = true;

								} catch (EmbeddedScriptException e) {

									switch (e.getResultCode()) {
									case ERR_DATASTORE_UNREACHABLE:
										logger.info("The target datastore unreachable");
										targetConnected = false;
										break;
									case ERR_DATASTORE_NOT_SUPPORTED:
										logger.info("The target datastore not supported");
										targetConnected = false;
										break;
									default:
										throw e;
									}

								}

							}

							if (targetConnected) {

								// List the direct table mappings for the
								// selected subscription
								logger.info("\t\tGetting directly mapped tables");

								script.execute(MessageFormat.format("list table mappings name {0}",
										new Object[] { currSubscription.getName() }));
								ResultStringTable mappingsTables = (ResultStringTable) script.getResult();

								currSubscription.setTargetDatastoreID(targetDatastoreID);

								assets.addSubscription(currSubscription);

								script.execute(MessageFormat.format("select subscription name {0}",
										new Object[] { currSubscription.getName() }));

								for (int tmTableRow = 0; tmTableRow < mappingsTables.getRowCount(); tmTableRow++) {

									TableMapping currTableMapping;

									if (mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE")
											.equalsIgnoreCase(MAPPING_TYPE_DATASTAGE)) {

										logger.debug("\t\tSelecting table mapping of "
												+ mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE"));
										script.execute(MessageFormat
												.format("select table mapping sourceSchema {0} sourceTable {1}",
														new Object[] {
																Utils.getSchema(mappingsTables.getValueAt(tmTableRow,
																		"SOURCE TABLE")),
																Utils.getTable(mappingsTables.getValueAt(tmTableRow,
																		"SOURCE TABLE")) }));

										// Fetch data on target datastore
										logger.debug("\t\tFetching table mapping info for table "
												+ mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE"));

										script.execute("show table mapping");

										ResultStringKeyValues flatFileTarget = (ResultStringKeyValues) script
												.getResult();

										currTableMapping = new TableMapping(assets.getNextTableMappingID(),
												Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
												Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
												flatFileTarget.getValue("Directory"),
												mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE"),
												mappingsTables.getValueAt(tmTableRow, "METHOD"),
												mappingsTables.getValueAt(tmTableRow, "PREVENT RECURSION"),
												currSubscription.getID());
									} else {
										currTableMapping = new TableMapping(assets.getNextTableMappingID(),
												Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
												Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
												Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
												Utils.getTable(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
												mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE"),
												mappingsTables.getValueAt(tmTableRow, "METHOD"),
												mappingsTables.getValueAt(tmTableRow, "PREVENT RECURSION"),
												currSubscription.getID());

										logger.debug("\t\tSelecting table mapping of " + currTableMapping);
										script.execute(MessageFormat.format(
												"select table mapping sourceSchema {0} sourceTable {1} targetSchema {2} targetTable {3}",
												new Object[] { currTableMapping.getSourceSchema(),
														currTableMapping.getSourceTable(),
														currTableMapping.getTargetSchema(),
														currTableMapping.getTargetTable() }));

										logger.debug("\t\tFetching columns for table mapping of " + currTableMapping);
										script.execute("list column mappings");

										ResultStringTable tableColumns = (ResultStringTable) script.getResult();
										for (int columnRow = 0; columnRow < tableColumns.getRowCount(); columnRow++) {

											ColumnMapping columnMapping = new ColumnMapping(
													assets.getNextColumnMappingID(),
													tableColumns.getValueAt(columnRow, "SOURCE"),
													tableColumns.getValueAt(columnRow, "TARGET COLUMN"),
													tableColumns.getValueAt(columnRow, "INITIAL VALUE"),
													currTableMapping.getID());

											assets.addColumnMapping(columnMapping);
										}
									}

									assets.addTableMapping(currTableMapping);

								}

								try {

									// Listing rule sets
									logger.info("\t\tGetting rule sets");
									script.execute("list rule sets");
									ResultStringTable ruleSets = (ResultStringTable) script.getResult();

									for (int rsTableRow = 0; rsTableRow < ruleSets.getRowCount(); rsTableRow++) {

										RuleSet ruleSet = new RuleSet(assets.getNextRuleSetID(),
												ruleSets.getValueAt(rsTableRow, "RULE SET NAME"),
												ruleSets.getValueAt(rsTableRow, "SCHEMA"),
												ruleSets.getValueAt(rsTableRow, "INCLUDE TABLES"),
												ruleSets.getValueAt(rsTableRow, "EXCLUDE TABLES"),
												ruleSets.getValueAt(rsTableRow, "STRUCTURE ONLY"),
												ruleSets.getValueAt(rsTableRow, "CONTEXT"), currSubscription.getID());

										assets.addRuleSet(ruleSet);
									}

									// Listing rule set tables
									script.execute("list rule set tables");

									ResultStringTable ruleSetTables = (ResultStringTable) script.getResult();
									for (int rsTableRow = 0; rsTableRow < ruleSetTables.getRowCount(); rsTableRow++) {

										RSTableMapping rsTableMapping = new RSTableMapping(
												assets.getNextRSTableMappingID(),
												ruleSetTables.getValueAt(rsTableRow, "SCHEMA"),
												ruleSetTables.getValueAt(rsTableRow, "TABLE NAME"),
												ruleSetTables.getValueAt(rsTableRow, "STRUCTURE ONLY"),
												currSubscription.getID());

										assets.addRSTableMapping(rsTableMapping);
									}

								} catch (EmbeddedScriptException e) {

									switch (e.getResultCode()) {
									case ERR_DATASTORES_NOT_SAME_TYPE:
										logger.debug("\t\tRules set mappings not found");
										break;
									default:
										throw e;
									}

								}

								if (!currentSourceDatastore.equalsIgnoreCase(currTargetDatastore)) {
									logger.debug("\t\tDisconnecting from target datastore " + currTargetDatastore);
									script.execute(MessageFormat.format("disconnect datastore name {0}",
											new Object[] { currTargetDatastore }));
								}
							}
						}
					}

					logger.debug("Disconnecting from source datastore " + currentSourceDatastore);
					script.execute(MessageFormat.format("disconnect datastore name {0}",
							new Object[] { currentSourceDatastore }));

				} catch (EmbeddedScriptException e) {

					switch (e.getResultCode()) {
					case ERR_DATASTORE_UNREACHABLE:
						logger.info("The datastore unreachable");
						break;
					case ERR_DATASTORE_NOT_SUPPORTED:
						logger.info("The datastore not supported");
						break;
					default:
						throw e;
					}

				}

			}

		} catch (EmbeddedScriptException e) {
			logger.error(e.getResultCodeAndMessage());
		} finally {
			logger.info("Closing connection to Access Server");
			script.close();
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
			logger.info("Listing subscriptions for datastore " + sourceDatastore.getName());
			script.execute(MessageFormat.format("list subscriptions filter datastore name {0}",
					new Object[] { sourceDatastore.getName() }));
			ResultStringTable subscriptionsTable = (ResultStringTable) script.getResult();
			// For every directly mapped table, list the column mappings
			for (int tableRow = 0; tableRow < subscriptionsTable.getRowCount(); tableRow++) {
				subscriptionNames.add(subscriptionsTable.getValueAt(tableRow, "SUBSCRIPTION"));
			}
		} else {
			subscriptionNames = (ArrayList<String>) Arrays.asList(parms.subscription.split(","));
		}
		// Now get details for all selected subscriptions
		for (String subscriptionName : subscriptionNames) {
			logger.debug("Getting details for subscription " + subscriptionName);
			try {
				script.execute(MessageFormat.format("show subscription name {0}", new Object[] { subscriptionName }));
				ResultStringKeyValues subscriptionInfo = (ResultStringKeyValues) script.getResult();
				String targetDatastoreName = subscriptionInfo.getValue("Target Datastore");
				// If target datastore is not the same as source and if the
				// details have not been recorded yet
				if (!subscriptionInfo.getValue("Source Datastore").equals(targetDatastoreName)) {
					if (assets.datastoreExists(targetDatastoreName) == null) {
						logger.debug("Connecting to target datastore " + targetDatastoreName);
						script.execute(MessageFormat.format("connect datastore name {0} context target",
								new Object[] { targetDatastoreName }));
						addDatastoreToAssets(targetDatastoreName);
					}
				}
				// If the code gets here, it means that info from the target
				// datastore was retrieved
				assets.addSubscription(subscriptionInfo.getValue("Name"), subscriptionInfo.getValue("Description"),
						subscriptionInfo.getValue("Source Datastore"), subscriptionInfo.getValue("Target Datastore"),
						subscriptionInfo.getValue("Source ID"), subscriptionInfo.getValue("TCP Host"),
						subscriptionInfo.getValue("Firewall Port"), subscriptionInfo.getValue("Persistency"),
						sourceDatastore);
			} catch (EmbeddedScriptException e) {
				logger.warn("Error while retrieving details for subscription " + subscriptionName
						+ ". Subscription is ignored. Error: " + e.getMessage());
			}
		}
		// Once all subscriptions have been retrieved, list the table mappings
		for (Subscription subscription : assets.getSubscriptions()) {
			collectTableMappings(subscription);
		}
	}

	private void collectTableMappings(Subscription subscription) throws EmbeddedScriptException {
		script.execute(MessageFormat.format("select subscription name {0}", new Object[] { subscription.getName() }));
		// If the target datastore is InfoSphere DataStage
		if (assets.getDatastore(subscription.getTargetDatastoreID()).getDatabase().equals(DATASTORE_TYPE_DATASTAGE)) {
			logger.debug("Collecing DataStage table mappings for subscription " + subscription.getName());
			script.execute(
					MessageFormat.format("list table mappings name {0}", new Object[] { subscription.getName() }));
			ResultStringTable mappingsTables = (ResultStringTable) script.getResult();
			for (int tmTableRow = 0; tmTableRow < mappingsTables.getRowCount(); tmTableRow++) {
				script.execute(MessageFormat.format("select table mapping sourceSchema {0} sourceTable {1}",
						new Object[] { Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
								Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")) }));
				script.execute("show table mapping");
				ResultStringKeyValues flatFileTarget = (ResultStringKeyValues) script.getResult();
				assets.addTableToTableMapping(Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
						mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE"),
						mappingsTables.getValueAt(tmTableRow, "METHOD"),
						mappingsTables.getValueAt(tmTableRow, "PREVENT RECURSION"), subscription.getID());
			}
			// If the target datastore is a database
		} else {
			logger.debug("Collecing database table mappings for subscription " + subscription.getName());
			script.execute(
					MessageFormat.format("list table mappings name {0}", new Object[] { subscription.getName() }));
			ResultStringTable mappingsTables = (ResultStringTable) script.getResult();
			for (int tmTableRow = 0; tmTableRow < mappingsTables.getRowCount(); tmTableRow++) {
				script.execute(MessageFormat.format("select table mapping sourceSchema {0} sourceTable {1}",
						new Object[] { Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
								Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")) }));
				script.execute("show table mapping");
				ResultStringKeyValues flatFileTarget = (ResultStringKeyValues) script.getResult();
				assets.addTableToTableMapping(Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE")),
						Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
						Utils.getTable(mappingsTables.getValueAt(tmTableRow, "TARGET TABLE")),
						mappingsTables.getValueAt(tmTableRow, "MAPPING TYPE"),
						mappingsTables.getValueAt(tmTableRow, "METHOD"),
						mappingsTables.getValueAt(tmTableRow, "PREVENT RECURSION"), subscription.getID());
			}

		}
	}

	public static void main(String[] args) throws ConfigurationException, ExportMetadataParmsException,
			EmbeddedScriptException, ExportMetadataException {

		logger.info(MessageFormat.format("Staring metadata integration - v{0}.{1}",
				new Object[] { ExportMetadata.VERSION, ExportMetadata.BUILD }));

		// args = "-p preview.txt -ds TESTDB,ORCL".split(" ");
		// args = "-d -ub -p preview.txt -ds CDC_Oracle_cdcdemoa -sub
		// SARC".split(" ");
		args = "-d -ub -ds CDC_Oracle_cdcdemoa -s SARC -p preview.txt -x".split(" ");
		new ExportMetadata(args);
	}

}
