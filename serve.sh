#!/bin/bash -ex
mvn compile -Dmaven.compiler.showDeprecation=true -Dmaven.compiler.showWarnings=true
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
java -cp target/classes:`cat classpath.txt` org.example.WebServer index
