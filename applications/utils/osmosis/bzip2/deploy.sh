#! /bin/sh -x

VERSION=20090327

mvn -e deploy:deploy-file \
    -Durl=http://oss.sonatype.org/content/repositories/openstreetmap-releases \
    -DrepositoryId=sonatype-openstreetmap-releases \
    -DgroupId=org.openstreetmap.osmosis.org.apache.tools \
    -DartifactId=bzip2 \
    -Dversion=${VERSION} \
    -DpomFile=$(dirname $0)/pom.xml \
    -Dfile=$(dirname $0)/org.apache.tools/bzip2/${VERSION}/jars/bzip2.jar

