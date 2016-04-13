package com.ibm.replication.iidr.igc.assets;

import java.util.ArrayList;

public class Assets {

	
	private ArrayList<Datastore> datastores;
	private ArrayList<Subscription> subscriptions;
	private ArrayList<TableMapping> tableMappings;
	private ArrayList<ColumnMapping> columnMappings;
	private ArrayList<RuleSet> ruleSets;
	private ArrayList<RSTableMapping> rsTableMappings;
	
	private int datastoresID;
	private int subscriptionsID;
	private int tableMappingsID;
	private int columnMappingsID;
	private int ruleSetsID;
	private int rsTableMappingsID;
	
	
	public Assets() {
		this.datastores = new ArrayList<Datastore>();
		this.subscriptions = new ArrayList<Subscription>();
		this.tableMappings = new ArrayList<TableMapping>();
		this.columnMappings = new ArrayList<ColumnMapping>();
		this.ruleSets = new ArrayList<RuleSet>();
		this.rsTableMappings = new ArrayList<RSTableMapping>();
		
		this.datastoresID = 0;
		this.subscriptionsID = 0;
		this.tableMappingsID = 0;
		this.columnMappingsID = 0;
		this.ruleSetsID = 0;
		this.rsTableMappingsID = 0;
		
	}
	
	
	public String getNextDatastoreID() {
		++this.datastoresID;
		return "ds" + this.datastoresID;
	}
	
	public String getNextSubscriptionID() {
		++this.subscriptionsID;
		return "sub" + this.subscriptionsID;
	}
	
	public String getNextTableMappingID() {
		++this.tableMappingsID;
		return "tm" + this.tableMappingsID;
	}
	
	public String getNextColumnMappingID() {
		++this.columnMappingsID;
		return "cm" + this.columnMappingsID;
	}
	
	public String getNextRuleSetID() {
		++this.ruleSetsID;
		return "rs" + this.ruleSetsID;
	}
	
	public String getNextRSTableMappingID() {
		++this.rsTableMappingsID;
		return "rstm" + this.rsTableMappingsID;
	}
	
	public void addDatastore(Datastore datastore) {
		this.datastores.add(datastore);
	}
	
	public void addSubscription(Subscription subscription) {
		this.subscriptions.add(subscription);
	}
	
	public void addTableMapping(TableMapping tableMapping) {
		this.tableMappings.add(tableMapping);
	}
	
	public void addColumnMapping(ColumnMapping columnMapping) {
		this.columnMappings.add(columnMapping);
	}
	
	public void addRuleSet(RuleSet ruleSet) {
		this.ruleSets.add(ruleSet);
	}
	
	public void addRSTableMapping(RSTableMapping rsTableMapping) {
		this.rsTableMappings.add(rsTableMapping);
	}
	
	public Datastore datastoreExists(String name) {
		for (Datastore datastore : this.datastores) {
			if (datastore.getName().equalsIgnoreCase(name)) {
				return datastore;
			}
		}
		
		return null;
	}
	
	public boolean subscriptionExists(String name, String sourceDatastore) {
		for (Subscription subscription : this.subscriptions) {
			if (subscription.getName().equalsIgnoreCase(name) && subscription.getSourceDatastore().equalsIgnoreCase(sourceDatastore)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public ArrayList<Subscription> getSubscriptionsByParentID(String parentID) {
		
		ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
		
		for (Subscription subscription : this.subscriptions) {
			if (subscription.getParentID().equalsIgnoreCase(parentID)) {
				subscriptions.add(subscription);
			}
		}
		
		return subscriptions;
	}
	
	public ArrayList<TableMapping> getTableMappingsByParentID(String parentID) {
		
		ArrayList<TableMapping> tableMappings = new ArrayList<TableMapping>();
		
		for (TableMapping tableMapping : this.tableMappings) {
			if (tableMapping.getParentID().equalsIgnoreCase(parentID)) {
				tableMappings.add(tableMapping);
			}
		}
		
		return tableMappings;
	}
	
	public ArrayList<RSTableMapping> getRSTableMappingsByParentID(String parentID) {
		
		ArrayList<RSTableMapping> tableMappings = new ArrayList<RSTableMapping>();
		
		for (RSTableMapping tableMapping : this.rsTableMappings) {
			if (tableMapping.getParentID().equalsIgnoreCase(parentID)) {
				tableMappings.add(tableMapping);
			}
		}
		
		return tableMappings;
	}
	
	public ArrayList<RuleSet> getRuleSetsByParentID(String parentID) {
		
		ArrayList<RuleSet> ruleSets = new ArrayList<RuleSet>();
		
		for (RuleSet ruleSet : this.ruleSets) {
			if (ruleSet.getParentID().equalsIgnoreCase(parentID)) {
				ruleSets.add(ruleSet);
			}
		}
		
		return ruleSets;
	}
	
	
	public ArrayList<ColumnMapping> getColumnMappingsByParentID(String parentID) {
		
		ArrayList<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();
		
		for (ColumnMapping columnMapping : this.columnMappings) {
			if (columnMapping.getParentID().equalsIgnoreCase(parentID)) {
				columnMappings.add(columnMapping);
			}
		}
		
		return columnMappings;
	}
	
	
	public ArrayList<ColumnMapping> getColumnMappingByParentID(String parentID) {
		
		ArrayList<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();
		
		for (ColumnMapping columnMapping : this.columnMappings) {
			if (columnMapping.getParentID().equalsIgnoreCase(parentID)) {
				columnMappings.add(columnMapping);
			}
		}
		
		return columnMappings;
	}
	
	public String toXML() {
		
		String assetsXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
						   "<doc xmlns=\"http://www.ibm.com/iis/flow-doc\">\n" + 
						   "\t<assets>\n";
		
		String completeAssetIDs = "";
		
		for (Datastore datastore : this.datastores) {
			assetsXML += datastore.toAssetXML();
			completeAssetIDs += datastore.getID() + " ";
		}
		
		for (Subscription subscription : this.subscriptions) {
			assetsXML += subscription.toAssetXML();
		}
		
		for (TableMapping tableMapping : this.tableMappings) {
			assetsXML += tableMapping.toAssetXML();
		}
		
		for (RuleSet ruleSet : this.ruleSets) {
			assetsXML += ruleSet.toXML();
		}
		
		for (ColumnMapping columnMapping : this.columnMappings) {
			assetsXML += columnMapping.toXML();
		}
		
		for (RSTableMapping rsTableMapping : this.rsTableMappings) {
			assetsXML += rsTableMapping.toXML();
		}
		
		assetsXML += "\t</assets>\n";
		
		assetsXML += "\t<importAction partialAssetIDs=\"" + completeAssetIDs + "\" />\n";
		
		assetsXML += "</doc>";
		
		return assetsXML;
	}
	
	public ArrayList<String> listDatastores() {
		ArrayList<String> datastoresList = new ArrayList<String>();
		
		for (Datastore datastore : this.datastores) {
			datastoresList.add(datastore.toString());
		}
		
		return datastoresList;
	}
	
	public ArrayList<String> listSubscriptions() {
		ArrayList<String> subscriptionList = new ArrayList<String>();
		
		for (Subscription subscription : this.subscriptions) {
			subscriptionList.add(subscription.toString());
		}
		
		return subscriptionList;
	}
	
	public ArrayList<String> listTableMappings() {
		ArrayList<String> tableMAppingList = new ArrayList<String>();
		
		for (TableMapping tableMapping : this.tableMappings) {
			tableMAppingList.add(tableMapping.toString());
		}
		
		return tableMAppingList;
	}
	
	public ArrayList<String> listRuleSets() {
		ArrayList<String> ruleSetList = new ArrayList<String>();
		
		for (RuleSet ruleSet : this.ruleSets) {
			ruleSetList.add(ruleSet.toString());
		}
		
		return ruleSetList;
	}
	
	public ArrayList<String> listColumnMappings() {
		ArrayList<String> columnMappingList = new ArrayList<String>();
		
		for (ColumnMapping columnMapping : this.columnMappings) {
			columnMappingList.add(columnMapping.toString());
		}
		
		return columnMappingList;
	}
	
	public ArrayList<String> listRSTableMappings() {
		ArrayList<String> rsTableMAppingList = new ArrayList<String>();
		
		for (RSTableMapping rsTableMapping : this.rsTableMappings) {
			rsTableMAppingList.add(rsTableMapping.toString());
		}
		
		return rsTableMAppingList;
	}
	
	public ArrayList<Datastore> getDatastores() {
		return this.datastores;
	}
	
	public ArrayList<Subscription> getSubscriptions() {
		return this.subscriptions;
	}
	
	
	public ArrayList<TableMapping> getTableMappings() {
		return this.tableMappings;
	}
	
	public ArrayList<RSTableMapping> getRSTableMappings() {
		return this.rsTableMappings;
	}
	
	public ArrayList<TableMapping> getTableMappingsByType(int type) {
		
		ArrayList<TableMapping> foundTableMappings = new ArrayList<TableMapping>();
		
		for (TableMapping tableMapping : this.tableMappings) {
			if (tableMapping.getTableMappingType() == type) {
				foundTableMappings.add(tableMapping);
			}
		}
		
		return foundTableMappings;
	}
	
	public ArrayList<ColumnMapping> getColumnMappings() {
		return this.columnMappings;
	}
	
	
	public Subscription getSubscription(String id) {
		
		for (Subscription subscription : this.subscriptions) {
			if (subscription.getID().equalsIgnoreCase(id)) {
				return subscription;
			}
		}
		
		return null;
	}
	
	public Datastore getDatastore(String id) {
		for (Datastore datastore : this.datastores) {
			if (datastore.getID().equalsIgnoreCase(id)) {
				return datastore;
			}
		}
		
		return null;
	}
	
	
	public static void main(String[] args) {

		
		Assets assets = new Assets();
		Datastore ds = new Datastore(assets.getNextDatastoreID(), "TESTDB", "TESTDB DB2 database on troia", "troia", 
				"10901", "V11R3M3T0BCDD_BR_LSTYTHIK_96_7", "Java VM", "DB2 for LUW", "Dual");
		Subscription sub = new Subscription(assets.getNextSubscriptionID(), "BLA", "Replicating customer data", "ORCL", "TESTDB", "BLA", "Auto Select", "", "No", "ds1");
		TableMapping tm = new TableMapping("tm1", "DISCOUNT", "TAB1", "NICE", "TAB2", "Standard", "Mirror", "No", "sub1");
		ColumnMapping cm = new ColumnMapping("cm1", "ALBUMID", "ALBUMID", "", "tm1");
		RuleSet rs = new RuleSet("rs1", "A_TABLES", "TESTS", "A*", "", "No", "", "sub1");
		RSTableMapping rstm = new RSTableMapping("rstm1", "DISCOUNT", "TAB1", "No", "sub1");
		
		assets.addDatastore(ds);
		assets.addSubscription(sub);
		assets.addTableMapping(tm);
		assets.addColumnMapping(cm);
		assets.addRuleSet(rs);
		assets.addRSTableMapping(rstm);
		
		System.out.println(assets.toXML());
		System.out.println(assets.listDatastores());
		System.out.println(assets.listSubscriptions());
		System.out.println(assets.listTableMappings());
		System.out.println(assets.listRuleSets());
		System.out.println(assets.listColumnMappings());
		System.out.println(assets.listRSTableMappings());
	}

}
