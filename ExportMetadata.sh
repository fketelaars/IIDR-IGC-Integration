#!/bin/bash
SCRIPT_DIR=$( dirname $( readlink -f $0 ) )
source ${SCRIPT_DIR}/conf/ExportMetadata.properties
JAVA=${CDC_AS_HOME}/jre64/jre/bin/java

$JAVA -cp "${SCRIPT_DIR}/opt/downloaded/*.jar:${SCRIPT_DIR}/lib/*.jar" com.ibm.replication.iidr.metadata.ExportMetadata "$@"

