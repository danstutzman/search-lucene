#!/bin/bash -ex
mvn compile
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
mkdir -p index
java -cp target/classes:`cat classpath.txt` org.example.LuceneExample a2.txt index
