#!/bin/sh

echo "Creating private/public key pair ..."

keytool -genkey \
        -dname "cn=localhost, ou=Tambora, o=Zenplex, c=US" \
        -alias tambora \
        -keypass password \
        -keystore keystore \
        -storepass password \
        -validity 180                

echo "Creating certificate ..."

keytool -export \
        -alias tambora \
        -keystore keystore \
        -keypass password \
        -storepass password \
        -rfc \
        -file testkeys.cer

echo "Import cert into truststore ..."

keytool -import \
        -alias tambora \
        -file testkeys.cer \
        -keystore truststore \
        -storepass password
