#!/bin/sh

CLASSPATH=./bin/classes

for i in $ANT_HOME/lib/*.jar
do  
  CLASSPATH=$CLASSPATH:$i
done  

DEBUG="-Djavax.net.debug=all"
#DEBUG="-Djava.security.debug=all"
CLIENT="org.apache.xmlrpc.XmlRpcClient https://localhost:10001/RPC2 string testing"

$JAVA_HOME/bin/java $DEBUG \
     -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol \
     -Djavax.net.ssl.keyStore=keystore \
     -Djavax.net.ssl.trustStorePassword=password \
     -Djavax.net.ssl.trustStore=truststore \
     -Djavax.net.ssl.trustStorePassword=password \
     -cp $CLASSPATH $CLIENT
