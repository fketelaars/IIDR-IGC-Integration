package com.ibm.replication.iidr.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

	/**
	 * Gets the schema name from the qualified name. If a qualified name is not
	 * specified, return an empty string as the schema name
	 * 
	 * @param qualifiedName
	 * @return The schema name
	 */
	public static String getSchema(String qualifiedName) {
		String schemaName = "";
		if (qualifiedName.contains("."))
			schemaName = qualifiedName.split("\\.")[0];
		return schemaName;
	}

	/**
	 * Gets the table name from the qualified name. If a qualified name is not
	 * specified, return an empty string as the table name
	 * 
	 * @param qualifiedName
	 * @return The table name
	 */
	public static String getTable(String qualifiedName) {
		String tableName = "";
		if (qualifiedName.contains("."))
			tableName = qualifiedName.split("\\.")[1];
		return tableName;
	}
	
	public static void writeContentToFile(String fileName, String content) {

		File assetsFile = new File(fileName);
		
		try {
			BufferedWriter writer  = new BufferedWriter(new FileWriter(assetsFile));
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
