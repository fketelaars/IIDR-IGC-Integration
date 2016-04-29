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

import org.apache.commons.cli.*;

public class ExportMetadataParms {

	private Options options;
	private HelpFormatter formatter;
	private CommandLineParser parser;
	private CommandLine commandLine;

	public boolean debug;
	public String datastore;
	public boolean previewOnly;
	public boolean xmlFileOutput;
	public String subscription;
	public boolean updateBundle;

	public ExportMetadataParms(String[] commandLineArguments) throws ExportMetadataParmsException {
		// Initialize parameters
		this.debug = false;
		this.datastore = "";
		this.formatter = new HelpFormatter();
		this.parser = new DefaultParser();
		this.options = new Options();
		this.previewOnly = false;
		this.xmlFileOutput = false;
		this.subscription = "";
		this.updateBundle = false;

		this.options.addOption("d", false, "");
		this.options.addOption("ub", false, "");
		this.options.addOption("p", false, "");
		this.options.addOption("xp", false, "");
		this.options.addOption(Option.builder("ds").hasArg().build());
		this.options.addOption(Option.builder("s").hasArg().build());

		try {
			commandLine = parser.parse(options, commandLineArguments);
		} catch (ParseException e) {
			sendInvalidParameterException("");
		}

		this.debug = commandLine.hasOption("d");
		this.updateBundle = commandLine.hasOption("ub");
		this.previewOnly = commandLine.hasOption("p");
		this.xmlFileOutput = commandLine.hasOption("xf");

		// Datastore parameter is mandatory
		if (commandLine.getOptionValue("ds") != null) {
			this.datastore = commandLine.getOptionValue("ds");
			if (commandLine.getOptionValue("s") != null) {
				this.subscription = commandLine.getOptionValue("s");
			}

		} else
			sendInvalidParameterException("Datastore (ds parameter) must be specified");

	}

	// Method to send exception
	private void sendInvalidParameterException(String message) throws ExportMetadataParmsException {
		formatter.printHelp("ExportMetadata", message, this.options, "", true);
		throw new ExportMetadataParmsException("Error while validating parameters");
	}

}
