@echo off

set JARS=${project.build.directory}\${project.artifactId}-${project.version}.jar
set JARS=%JARS%;${project.build.directory}\lib\Saxon-HE-${saxon.version}.jar
set JARS=%JARS%;${project.build.directory}\lib\xmlresolver-${xmlresolver.version}.jar

java %JAVAOPTS% -cp %JARS% %*
