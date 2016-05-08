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

import java.util.Iterator;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;

import com.datamirror.common.util.EncryptedDataException;
import com.datamirror.common.util.Encryptor;

public class Settings {

	final static Logger logger = Logger.getLogger("com.ibm.replication.cdc.metadata.ExportMetadata");

	// Access Server connection parameters
	String asHostName = null;
	String asUserName = null;
	String asPassword = null;
	int asPort = 0;

	// Information Server metadata exchange settings
	String isHostName = null;
	String isUserName = null;
	String isPassword = null;
	int isPort = 0;

	String bundleFilePath = null;
	String defaultDataPath = "";

	boolean trustSelfSignedCertificates = false;

	/**
	 * Retrieve the settings from the given properties file.
	 * 
	 * @param propertiesFile
	 * @throws ConfigurationException
	 */
	public Settings(String propertiesFile) throws ConfigurationException {
		PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
		asHostName = config.getString("asHostName");
		asUserName = config.getString("asUserName");
		String encryptedAsPassword = config.getString("asPassword");
		asPort = config.getInt("asPort", 10101);

		isHostName = config.getString("isHostName");
		isUserName = config.getString("isUserName");
		String encryptedISPassword = config.getString("isPassword");
		isPort = config.getInt("isPort", 10101);

		bundleFilePath = config.getString("bundleFilePath");
		defaultDataPath = config.getString("defaultDataPath");

		trustSelfSignedCertificates = config.getBoolean("trustSelfSignedCertificates");

		// Check if the password has already been encrypted
		// If not, encrypt and save the properties
		try {
			asPassword = Encryptor.decodeAndDecrypt(encryptedAsPassword);
		} catch (EncryptedDataException e) {
			logger.info("Encrypting asPassword");
			asPassword = encryptedAsPassword;
			encryptedAsPassword = Encryptor.encryptAndEncode(encryptedAsPassword);
			config.setProperty("asPassword", encryptedAsPassword);
			config.save();
		}

		try {
			isPassword = Encryptor.decodeAndDecrypt(encryptedISPassword);
		} catch (EncryptedDataException e) {
			logger.info("Encrypting isPassword");
			isPassword = encryptedISPassword;
			encryptedISPassword = Encryptor.encryptAndEncode(encryptedISPassword);
			config.setProperty("isPassword", encryptedISPassword);
			config.save();
		}

		// Now log the settings
		logSettings(config);
	}

	/**
	 * Log the properties in the specified configuration file
	 * 
	 * @param config
	 */
	private void logSettings(PropertiesConfiguration config) {
		Iterator<String> configKeys = config.getKeys();
		while (configKeys.hasNext()) {
			String configKey = configKeys.next();
			logger.debug("Property: " + configKey + " = " + config.getProperty(configKey));
		}
	}

	public static void main(String[] args)
			throws ConfigurationException, IllegalArgumentException, IllegalAccessException {
		new Settings("conf/ExportMetadata.properties");
	}

}
