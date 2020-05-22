#!/bin/bash

./gradlew customFatJar
JAVA_JAR=$( find . -name lograter-exec-*.jar )

java -jar $JAVA_JAR --help

#commands=( iis access pc application alloc gc accessToCsv jmeter )
#
#for i in "${commands[@]}"
#do
#    java -jar $JAVA_JAR --help $i
#done



