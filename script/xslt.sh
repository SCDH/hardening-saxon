#!/bin/sh

JARS=${project.build.directory}/${project.artifactId}-${project.version}.jar
JARS=$JARS:${project.build.directory}/lib/Saxon-HE-${saxon.version}.jar
JARS=$JARS:${project.build.directory}/lib/xmlresolver-${xmlresolver.version}.jar

java $JAVAOPTS -cp $JARS net.sf.saxon.Transform $@
