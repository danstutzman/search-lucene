#!/bin/bash -ex
if [ ! -e classpath.txt ]; then
  mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
fi
java -cp target/classes:`cat classpath.txt` org.example.HelloWorld
#java -cp target/classes:`cat classpath.txt` org.example.LuceneExample
