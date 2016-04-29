package com.ibm.replication.iidr.igc.flow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ibm.replication.iidr.igc.assets.Assets;
import com.ibm.replication.iidr.igc.assets.ColumnMapping;
import com.ibm.replication.iidr.igc.assets.Datastore;
import com.ibm.replication.iidr.igc.assets.RSTableMapping;
import com.ibm.replication.iidr.igc.assets.RuleSet;
import com.ibm.replication.iidr.igc.assets.Subscription;
import com.ibm.replication.iidr.igc.assets.TableMapping;

public class Flows {

	private Assets assets;
	private ArrayList<Host> hosts;
	private ArrayList<Database> databases;
	private ArrayList<Schema> schemas;
	private ArrayList<Table> tables;
	private ArrayList<Column> columns;
	private ArrayList<DataFile> dataFiles;
	
	private int hostID;
	private int databaseID;
	private int schemaID;
	private int tableID;
	private int columnID;
	private int fileID;
	
	final static Logger logger = Logger.getLogger("com.ibm.replication.iidr.igc.flow.Flows");
	
	public Flows(Assets assets) {
		this.assets = assets;
		this.hostID = 0;
		this.databaseID = 0;
		this.schemaID = 0;
		this.tableID = 0;
		this.columnID = 0;
		this.fileID = 0;
		
		this.hosts = new ArrayList<Host>();
		this.databases = new ArrayList<Database>();
		this.schemas = new ArrayList<Schema>();
		this.tables = new ArrayList<Table>();
		this.columns = new ArrayList<Column>();
		this.dataFiles = new ArrayList<DataFile>();
		
		this.extractData();
	}
	
	private void extractData() {
		
		for (Datastore datastore : this.assets.getDatastores()) {
			
			String hostID; 
			Host foundHost = this.hostExists(datastore.getHost());
			if (foundHost == null) {
				hostID = this.getNextHostID();
				this.hosts.add(new Host(hostID, datastore.getHost()));
			} else {
				hostID = foundHost.getID();
			}
			
			if (!datastore.getDatabase().equalsIgnoreCase(Datastore.DATABASE_TYPE_DATASTAGE)) {
				if (this.databaseExists(datastore.getDatabaseName(),  datastore.getHost()) == null) {
					this.databases.add(new Database(this.getNextDatabaseID(), datastore.getDatabaseName(), datastore.getHost(), hostID));
				}
			} 
		}
		
		for (RSTableMapping rsTableMapping : this.assets.getRSTableMappings()) {
			
			Subscription currSubscription = this.assets.getSubscription(rsTableMapping.getParentID());
			
			if (currSubscription == null) {
				continue;
			}
			
			Datastore sourceDatastore = this.assets.getDatastore(currSubscription.getParentID());
			Datastore targetDatastore = this.assets.getDatastore(currSubscription.getTargetDatastoreID());
			
			if (sourceDatastore == null || targetDatastore == null) {
				continue;
			}
			
			Database sourceDatabase = this.databaseExists(sourceDatastore.getDatabaseName(), sourceDatastore.getHost());
			Database targetDatabase = this.databaseExists(targetDatastore.getDatabaseName(), targetDatastore.getHost());
			
			if (sourceDatabase == null || targetDatabase == null) {
				continue;
			}
			
			
			Schema sourceSchema = this.schemaExists(rsTableMapping.getSchema(), sourceDatastore.getHost(), sourceDatastore.getDatabaseName());
			
			if ( sourceSchema == null) {
				sourceSchema = new Schema(this.getNextSchemaID(), rsTableMapping.getSchema(), 
						sourceDatastore.getHost(), sourceDatastore.getDatabaseName(), sourceDatabase.getID());
				this.schemas.add(sourceSchema);
			}
			
			Schema targetSchema = this.schemaExists(rsTableMapping.getSchema(), targetDatastore.getHost(), targetDatastore.getDatabaseName());
			
			if (targetSchema == null) {
				targetSchema = new Schema(this.getNextSchemaID(), rsTableMapping.getSchema(), 
						targetDatastore.getHost(), targetDatastore.getDatabaseName(), targetDatabase.getID());
				this.schemas.add(targetSchema);
			}
			
			Table sourceTable = this.tableExists(rsTableMapping.getTable(), sourceDatastore.getHost(), sourceDatastore.getDatabaseName(), rsTableMapping.getSchema()); 
			
			if (sourceTable == null) {
				sourceTable = new Table(this.getNextTableID(), rsTableMapping.getTable(), sourceDatastore.getHost(), 
						sourceDatastore.getDatabaseName(), rsTableMapping.getSchema(), sourceSchema.getID(), rsTableMapping.getID(), true);
				this.tables.add(sourceTable);
			}
			
			Table targetTable = this.tableExists(rsTableMapping.getTable(), targetDatastore.getHost(), targetDatastore.getDatabaseName(), rsTableMapping.getSchema()); 
			
			if (targetTable == null) {
				targetTable = new Table(this.getNextTableID(), rsTableMapping.getTable(), targetDatastore.getHost(), 
						targetDatastore.getDatabaseName(), rsTableMapping.getSchema(), targetSchema.getID(), rsTableMapping.getID(), false);
				this.tables.add(targetTable);
			}
			
		}
		
		for (TableMapping tableMapping : this.assets.getTableMappings()) {
			Subscription currSubscription = this.assets.getSubscription(tableMapping.getParentID());
			
			if (currSubscription == null) {
				continue;
			}
			
			Datastore sourceDatastore = this.assets.getDatastore(currSubscription.getParentID());
			Datastore targetDatastore = this.assets.getDatastore(currSubscription.getTargetDatastoreID());
			
			if (sourceDatastore == null || targetDatastore == null) {
				continue;
			}
			
			Database sourceDatabase = this.databaseExists(sourceDatastore.getDatabaseName(), sourceDatastore.getHost());
			
			
			if (tableMapping.getTableMappingType() == TableMapping.MAPPING_TYPE_TABLE) {
				Database targetDatabase = this.databaseExists(targetDatastore.getDatabaseName(), targetDatastore.getHost());
				
				if (sourceDatabase == null || targetDatabase == null) {
					continue;
				}

				Schema sourceSchema = this.schemaExists(tableMapping.getSourceSchema(), sourceDatastore.getHost(), sourceDatastore.getDatabaseName());
				
				if ( sourceSchema == null) {
					sourceSchema = new Schema(this.getNextSchemaID(), tableMapping.getSourceSchema(), 
							sourceDatastore.getHost(), sourceDatastore.getDatabaseName(), sourceDatabase.getID());
					this.schemas.add(sourceSchema);
				}
				
				Schema targetSchema = this.schemaExists(tableMapping.getTargetSchema(), targetDatastore.getHost(), targetDatastore.getDatabaseName());
				
				if (targetSchema == null) {
					targetSchema = new Schema(this.getNextSchemaID(), tableMapping.getTargetSchema(), 
							targetDatastore.getHost(), targetDatastore.getDatabaseName(), targetDatabase.getID());
					this.schemas.add(targetSchema);
				}
				
				Table sourceTable = this.tableExists(tableMapping.getSourceTable(), sourceDatastore.getHost(), sourceDatastore.getDatabaseName(), tableMapping.getSourceSchema()); 
				
				if (sourceTable == null) {
					sourceTable = new Table(this.getNextTableID(), tableMapping.getSourceTable(), sourceDatastore.getHost(), 
							sourceDatastore.getDatabaseName(), tableMapping.getSourceSchema(), sourceSchema.getID(), tableMapping.getID());
					this.tables.add(sourceTable);
				}
				
				Table targetTable = this.tableExists(tableMapping.getTargetTable(), targetDatastore.getHost(), targetDatastore.getDatabaseName(), tableMapping.getTargetSchema()); 
				
				if (targetTable == null) {
					targetTable = new Table(this.getNextTableID(), tableMapping.getTargetTable(), targetDatastore.getHost(), 
							targetDatastore.getDatabaseName(), tableMapping.getTargetSchema(), targetSchema.getID(), tableMapping.getID());
					this.tables.add(targetTable);
				}
				
				for (ColumnMapping columnMapping : this.assets.getColumnMappingByParentID(tableMapping.getID())) {
					
					this.columns.add(new Column(this.getNextColumnID(), columnMapping.getTargetColumn(), targetTable.getID(), columnMapping.getID(), false));
					this.columns.add(new Column(this.getNextColumnID(), columnMapping.getSourceColumn(), sourceTable.getID(), columnMapping.getID(), true));
				}
				
			} else if (tableMapping.getTableMappingType() == TableMapping.MAPPING_TYPE_FLAT_FILE) {
				
				if (sourceDatabase == null) {
					continue;
				}

				Schema sourceSchema = this.schemaExists(tableMapping.getSourceSchema(), sourceDatastore.getHost(), sourceDatastore.getDatabaseName());
				
				if ( sourceSchema == null) {
					sourceSchema = new Schema(this.getNextSchemaID(), tableMapping.getSourceSchema(), 
							sourceDatastore.getHost(), sourceDatastore.getDatabaseName(), sourceDatabase.getID());
					this.schemas.add(sourceSchema);
				}
				
				Table sourceTable = this.tableExists(tableMapping.getSourceTable(), sourceDatastore.getHost(), sourceDatastore.getDatabaseName(), tableMapping.getSourceSchema()); 
				
				if (sourceTable == null) {
					sourceTable = new Table(this.getNextTableID(), tableMapping.getSourceTable(), sourceDatastore.getHost(), 
							sourceDatastore.getDatabaseName(), tableMapping.getSourceSchema(), sourceSchema.getID(), tableMapping.getID());
					this.tables.add(sourceTable);
				}
				
				DataFile targetFile = this.fileExists(tableMapping.getSourceTable(), tableMapping.getDirectory(), targetDatastore.getHost());
				
				if (targetFile == null) {
					targetFile = new DataFile(this.getNextFileID(), tableMapping.getSourceTable(), tableMapping.getDirectory(), 
							targetDatastore.getHost(), this.hostExists(targetDatastore.getHost()).getID(), tableMapping.getID());
					this.dataFiles.add(targetFile);
				}
			}
		}
	}
	
	private Host hostExists(String name) {
		for (Host host : this.hosts) {			
			if (host.getName().equalsIgnoreCase(name)) {
				return host;
			}
		}
		
		return null;
	}
	
	private DataFile fileExists(String name, String path, String host) {
		
		for (DataFile file : this.dataFiles) {			
			if (file.getName().equalsIgnoreCase(name) && file.getHost().equalsIgnoreCase(host) 
					&& file.getPath().equalsIgnoreCase(path)) {
				return file;
			}
		}
		
		return null;
	}
	
	private Database databaseExists(String name, String host) {
		for (Database database : this.databases) {			
			if (database.getName().equalsIgnoreCase(name) && database.getHost().equalsIgnoreCase(host)) {
				return database;
			}
		}
		
		return null;
	}
	
	private Schema schemaExists(String name, String host, String database) {
		for (Schema schema : this.schemas) {			
			if (schema.getName().equalsIgnoreCase(name) && schema.getHost().equalsIgnoreCase(host) && 
					schema.getDatabase().equalsIgnoreCase(database)) {
				return schema;
			}
		}
		
		return null;
	}
	
	private Table tableExists(String name, String host, String database, String schema) {
		for (Table table : this.tables) {			
			if (table.getName().equalsIgnoreCase(name) && table.getHost().equalsIgnoreCase(host) && 
					table.getDatabase().equalsIgnoreCase(database) && table.getSchema().equalsIgnoreCase(schema)) {
				return table;
			}
		}
		
		return null;
	}
		
	public String getNextHostID() {
		++this.hostID;
		return "host" + this.hostID;
	}
	
	public String getNextDatabaseID() {
		++this.databaseID;
		return "db" + this.databaseID;
	}

	public String getNextSchemaID() {
		++this.schemaID;
		return "schm" + this.schemaID;
	}
	
	public String getNextTableID() {
		++this.tableID;
		return "tbl" + this.tableID;
	}
	
	public String getNextColumnID() {
		++this.columnID;
		return "col" + this.columnID;
	}
	
	public String getNextFileID() {
		++this.fileID;
		return "file" + this.fileID;
	}
	
	
	private <T> String getJunctionChar(Iterator<T> iterator) {
		if (!iterator.hasNext()) {
			 return "\\";
		} else {
			return "+";
		}
	}
	
	private <T> String getTreeChar(Iterator<T> iterator) {
		if (!iterator.hasNext()) {
			 return " ";
		} else {
			return "|";
		}
	}
	
	private ArrayList<Database> getHostDatabases(String hostID) {
		ArrayList<Database> databases = new ArrayList<Database>();
		
		for (Database database : this.databases) {
			if (database.getParentID().equalsIgnoreCase(hostID)) {
				databases.add(database);
			}
		}
		
		return databases;
	}
	
	private ArrayList<DataFile> getHostFiles(String hostID) {
		ArrayList<DataFile> files = new ArrayList<DataFile>();
		
		for (DataFile file : this.dataFiles) {
			if (file.getParentID().equalsIgnoreCase(hostID)) {
				files.add(file);
			}
		}
		
		return files;
	}
	
	private ArrayList<Schema> getDatabaseSchemas(String databaseID) {
		ArrayList<Schema> schemas = new ArrayList<Schema>();
		
		for (Schema schema : this.schemas) {
			if (schema.getParentID().equalsIgnoreCase(databaseID)) {
				schemas.add(schema);
			}
		}
		
		return schemas;
	}
	
	private ArrayList<Table> getSchemaTables(String schemaID) {
		ArrayList<Table> tables = new ArrayList<Table>();
		
		for (Table table : this.tables) {
			if (table.getParentID().equalsIgnoreCase(schemaID)) {
				tables.add(table);
			}
		}
		
		return tables;
	}
	
	private ArrayList<Column> getTableColumns(String tableID) {
		ArrayList<Column> columns = new ArrayList<Column>();
		
		for (Column column : this.columns) {
			if (column.getParentID().equalsIgnoreCase(tableID)) {
				columns.add(column);
			}
		}
		
		return columns;
	}
	
	public String preview() {
		return this.previewIIDRAssets() + "\n\n" + this.previewIGCAssets();
	}
	
	
	private String previewIIDRAssets() {
		
		logger.info("Generating preview output");
		
		String preview = "";
		
		preview += "+----------------------------------------------------------------------------------------------------------------+\n" + 
				   "| IIDR tree legend:                                                                                              |\n" + 
		           "| d=Datastore,  s=Subscription, tm=Table Mapping, rs=Rules Set, rstm=Rules Set Table Mapping, cm=Column Mapping  |\n" +
		           "+----------------------------------------------------------------------------------------------------------------+\n\n";
		
		preview += ".\n";
		
		
		for (Iterator<Datastore> datastoreIterator = this.assets.getDatastores().iterator(); datastoreIterator.hasNext(); ) {
		    
			Datastore datastore = datastoreIterator.next();
			
			preview += MessageFormat.format("|\n{0}------(d){1}\n", new Object[] {this.getJunctionChar(datastoreIterator), datastore.getName()});
			
			for (Iterator<Subscription> subscriptionIterator = this.assets.getSubscriptionsByParentID(datastore.getID()).iterator(); subscriptionIterator.hasNext(); ) {
				Subscription subscription = subscriptionIterator.next();
				
				preview += MessageFormat.format("{0}       {1}------(s){2}\n", new Object[] {this.getTreeChar(datastoreIterator), 
																					  this.getJunctionChar(subscriptionIterator),
																					  subscription.getName()});
				
				for (Iterator<RuleSet> ruleSetIterator = this.assets.getRuleSetsByParentID(subscription.getID()).iterator(); ruleSetIterator.hasNext(); ) {
					RuleSet ruleSet  = ruleSetIterator.next();
					
					preview += MessageFormat.format("{0}       {1}       {2}------(rs){3}\n", new Object[] {this.getTreeChar(datastoreIterator), 
																							 this.getTreeChar(subscriptionIterator),
																							 "|",
																							 ruleSet.getName()});
				}
				
				for (Iterator<RSTableMapping> tableMappingIterator = this.assets.getRSTableMappingsByParentID(subscription.getID()).iterator(); tableMappingIterator.hasNext(); ) {
					RSTableMapping tableMapping  = tableMappingIterator.next();
					
					preview += MessageFormat.format("{0}       {1}       {2}------(rstm){3}\n", new Object[] {this.getTreeChar(datastoreIterator), 
																							 this.getTreeChar(subscriptionIterator),
																							 this.getJunctionChar(tableMappingIterator),
																							 tableMapping.getName()});
				}
				
				for (Iterator<TableMapping> tableMappingIterator = this.assets.getTableMappingsByParentID(subscription.getID()).iterator(); tableMappingIterator.hasNext(); ) {
					TableMapping tableMapping  = tableMappingIterator.next();
					
					preview += MessageFormat.format("{0}       {1}       {2}------(tm){3}\n", new Object[] {this.getTreeChar(datastoreIterator), 
																							 this.getTreeChar(subscriptionIterator),
																							 this.getJunctionChar(tableMappingIterator),
																							 tableMapping.getName()});
					
					for (Iterator<ColumnMapping> columnIterator = this.assets.getColumnMappingByParentID(tableMapping.getID()).iterator(); columnIterator.hasNext(); ) {
						ColumnMapping columnMapping = columnIterator.next();
						
						preview += MessageFormat.format("{0}       {1}       {2}       {3}------(cm){4}\n", 
								new Object[] {this.getTreeChar(datastoreIterator), 
											this.getTreeChar(subscriptionIterator),
											this.getTreeChar(tableMappingIterator),
											this.getJunctionChar(columnIterator),
											columnMapping.getName()});
					}
					
				}
			}
		}

		return preview;
	}
	
	private String previewIGCAssets() {
		
		String preview = "";
		
		preview += "+-----------------------------------------------------------+\n" + 
				   "| IGC tree legend:                                          |\n" +
		           "| h=Host,  d=Database,  s=Schema, t=Table, c=Column, f=File |\n" +
		           "+-----------------------------------------------------------+\n\n";
		
		preview += ".\n";
		
		for (Iterator<Host> hostIterator = this.hosts.iterator(); hostIterator.hasNext(); ) {
		
		    
			Host host = hostIterator.next();
			
			preview += MessageFormat.format("|\n{0}------(h){1}\n", new Object[] {this.getJunctionChar(hostIterator), host.getName()});
			
			for (Iterator<DataFile> fileIterator = this.getHostFiles(host.getID()).iterator(); fileIterator.hasNext();) {
				DataFile file = fileIterator.next();
				
				preview += MessageFormat.format("{0}       {1}------(f){2}\n", new Object[] {this.getTreeChar(hostIterator), 
						  "|",
						  file.getPath() + "/" + file.getName()});

			}
			
			
			for (Iterator<Database> databaseIterator = this.getHostDatabases(host.getID()).iterator(); databaseIterator.hasNext(); ) {
			    Database database = databaseIterator.next();
				
				preview += MessageFormat.format("{0}       {1}------(d){2}\n", new Object[] {this.getTreeChar(hostIterator), 
																					  this.getJunctionChar(databaseIterator),
																					  database.getName()});
					
				
				for (Iterator<Schema> schemaIterator = this.getDatabaseSchemas(database.getID()).iterator(); schemaIterator.hasNext(); ) {
					Schema schema  = schemaIterator.next();
					
					preview += MessageFormat.format("{0}       {1}       {2}------(s){3}\n", new Object[] {this.getTreeChar(hostIterator), 
																							 this.getTreeChar(databaseIterator),
																							 this.getJunctionChar(schemaIterator),
																							 schema.getName()});
					
					for (Iterator<Table> tableIterator = this.getSchemaTables(schema.getID()).iterator(); tableIterator.hasNext(); ) {
						Table table = tableIterator.next();
						
						preview += MessageFormat.format("{0}       {1}       {2}       {3}------(t){4}\n", 
								new Object[] {this.getTreeChar(hostIterator), 
											this.getTreeChar(databaseIterator),
											this.getTreeChar(schemaIterator),
											this.getJunctionChar(tableIterator),
											table.getName()});
						
						for (Iterator<Column> columnIterator = this.getTableColumns(table.getID()).iterator(); columnIterator.hasNext(); ) {
							Column column = columnIterator.next();
							
							preview += MessageFormat.format("{0}       {1}       {2}       {3}       {4}------(c){5}\n", 
									new Object[] {this.getTreeChar(hostIterator), 
												this.getTreeChar(databaseIterator),
												this.getTreeChar(schemaIterator),
												this.getTreeChar(tableIterator),
												this.getJunctionChar(columnIterator),
												column.getName()});
						}
						
					}
					
				}
			}
		}
		
		
		return preview;
		
	}
	
	public String toXML() {
		String flowXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
				   "<doc xmlns=\"http://www.ibm.com/iis/flow-doc\">\n" + 
				   "\t<assets>\n";
		
		for (Host host : this.hosts) {
			flowXML += host.toFlowXML();
		}
		
		for (Database database : this.databases) {
			flowXML += database.toFlowXML();
		}
		
		for (Schema schema : this.schemas) {
			flowXML += schema.toFlowXML();
		}
		
		for (Table table : this.tables) {
			flowXML += table.toFlowXML();
		}
		
		for (Column column : this.columns) {
			flowXML += column.toFlowXML();
		}
		
		for (DataFile dataFile : this.dataFiles) {
			flowXML += dataFile.toFlowXML();
		}
		
		for (Datastore datastore : this.assets.getDatastores()) {
			flowXML += datastore.toFlowXML();
		}
		
		for (Subscription subscription : this.assets.getSubscriptions()) {
			flowXML += subscription.toFlowXML();
		}
		
		for (TableMapping tableMapping : this.assets.getTableMappings()) {
			flowXML += tableMapping.toFlowXML();
		}
		
		for (ColumnMapping columnMapping : this.assets.getColumnMappings()) {
			flowXML += columnMapping.toFlowXML();
		}
		
		for (RSTableMapping rsTableMapping : this.assets.getRSTableMappings()) {
			flowXML += rsTableMapping.toFlowXML();
		}
		
		
		flowXML += "\t</assets>\n\t<flowUnits>\n";
		
		
		for (RSTableMapping rsTableMapping : this.assets.getRSTableMappings()) {
			flowXML += "\t\t<flowUnit assetID=\"" + rsTableMapping.getID() + "\" flowType=\"DESIGN\">\n"; 
			
			for (Table table : this.tables) {
				if (table.getTableMappingID().equalsIgnoreCase(rsTableMapping.getID())) {
					String source_id = "";
					String target_id = "";
					
					if (table.isSource()) {
						source_id = table.getID();
						target_id = rsTableMapping.getID();
					} else {
						source_id = rsTableMapping.getID();
						target_id = table.getID();
					}
					
					flowXML += MessageFormat.format("\t\t\t<flow sourceIDs=\"{0}\" targetIDs=\"{1}\"/>\n", 
							new Object[] {source_id, target_id});
					
				}
			}
			
			
			flowXML += "\t\t</flowUnit>\n";	
		}
		
		
		for (TableMapping tableMapping : this.assets.getTableMappingsByType(TableMapping.MAPPING_TYPE_FLAT_FILE)) {
			flowXML += "\t\t<flowUnit assetID=\"" + tableMapping.getID() + "\" flowType=\"DESIGN\">\n"; 
			
			for (Table table : this.tables) {
				if (table.getTableMappingID().equalsIgnoreCase(tableMapping.getID())) {
					flowXML += MessageFormat.format("\t\t\t<flow sourceIDs=\"{0}\" targetIDs=\"{1}\"/>\n", 
							new Object[] {table.getID(), tableMapping.getID()});
					break;
				}
			}
			
			for (DataFile dataFile : this.dataFiles) {
				if (dataFile.getTableMappingID().equalsIgnoreCase(tableMapping.getID())) {
					flowXML += MessageFormat.format("\t\t\t<flow sourceIDs=\"{0}\" targetIDs=\"{1}\"/>\n", 
							new Object[] {tableMapping.getID(), dataFile.getID()});
					break;
				}
			}
			
			flowXML += "\t\t</flowUnit>\n";	
		}
		
		for (ColumnMapping columnMapping : this.assets.getColumnMappings()) {
			flowXML += "\t\t<flowUnit assetID=\"" + columnMapping.getID() + "\" flowType=\"DESIGN\">\n"; 
			
			
			for (Column column : this.columns) {
				if (column.getColumnMappingID().equalsIgnoreCase(columnMapping.getID())) {
					
					String source_id = "";
					String target_id = "";
					
					if (column.isSource()) {
						source_id = column.getID();
						target_id = columnMapping.getID();
					} else {
						source_id = columnMapping.getID();
						target_id = column.getID();
					}
					
					flowXML += MessageFormat.format("\t\t\t<flow sourceIDs=\"{0}\" targetIDs=\"{1}\"/>\n", 
							new Object[] {source_id, target_id});
				}
			}
			
			
			flowXML += "\t\t</flowUnit>\n";		
		}
		
		
		flowXML += "\t</flowUnits>\n</doc>";
		
		return flowXML;
	}
	
	public static void main(String[] args) {
		
		
//		Assets assets = new Assets();
//		Datastore ds = new Datastore(assets.getNextDatastoreID(), "TESTDB", "TESTDB DB2 database on troia  DATABASE:TESTDB", "troia", 
//				"10901", "V11R3M3T0BCDD_BR_LSTYTHIK_96_7", "Java VM", "DB2 for LUW", "Dual");
//		Datastore ds2 = new Datastore(assets.getNextDatastoreID(), "ORCL", "TESTDB DB2 database on troia  DATABASE:ORCL", "serv1", 
//				"10901", "V11R3M3T0BCDD_BR_LSTYTHIK_96_7", "Java VM", "DB2 for LUW", "Dual");
//		Subscription sub = new Subscription(assets.getNextSubscriptionID(), "BLA", "Replicating customer data", "ORCL", "TESTDB", "BLA", "Auto Select", "", "No", "ds1");
//		sub.setTargetDatastoreID("ds2");
//		TableMapping tm = new TableMapping("tm1", "DISCOUNT", "TAB1", "NICE", "TAB2", "Standard", "Mirror", "No", "sub1");
//		TableMapping tm2 = new TableMapping("tm2", "HAPOALIM", "TAB1", "NICE", "TAB2", "Standard", "Mirror", "No", "sub1");
//		ColumnMapping cm = new ColumnMapping("cm1", "ALBUMID", "ALBUMID_TG", "", "tm1");
//		ColumnMapping cm2 = new ColumnMapping("cm2", "ALBUM_NAME", "ALBUM_NAME_TG", "", "tm1");
//		RuleSet rs = new RuleSet("rs1", "A_TABLES", "TESTS", "A*", "", "No", "", "sub1");
//		RSTableMapping rstm = new RSTableMapping("rstm1", "DISCOUNT", "TAB1", "No", "sub1");
//		
//		
//		assets.addDatastore(ds);
//		assets.addDatastore(ds2);
//		assets.addSubscription(sub);
//		assets.addTableMapping(tm);
//		assets.addTableMapping(tm2);
//		assets.addColumnMapping(cm);
//		assets.addColumnMapping(cm2);
//		assets.addRuleSet(rs);
//		assets.addRSTableMapping(rstm);
//		
//		Flows flows = new Flows(assets);
//		
//		//System.out.println(flows.toXML());
//		System.out.println(flows.preview());
		
	}

}
