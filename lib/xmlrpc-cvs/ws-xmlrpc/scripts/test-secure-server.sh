#!/bin/sh

CLASSPATH=./bin/classes

for i in $ANT_HOME/lib/*.jar
do  
  CLASSPATH=$CLASSPATH:$i
done  

DEBUG="-Djavax.net.debug=all"
#DEBUG="-Djava.security.debug=all"

SERVER="org.apache.xmlrpc.secure.SecureWebServer 10001"

$JAVA_HOME/bin/java $DEBUG \
     -Djavax.net.ssl.keyStore=keystore \
     -Djavax.net.ssl.trustStorePassword=password \
     -Djavax.net.ssl.trustStore=truststore \
     -Djavax.net.ssl.trustStorePassword=password \
     -cp $CLASSPATH $SERVER
