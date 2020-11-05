#!/bin/bash

./gradlew customFatJar
JAVA_JAR=$( find . -name lograter-exec-*.jar )

java -jar $JAVA_JAR --help

#commands=( iis latency access pc application alloc gc accessToCsv jmeter )
commands=( latency )

for i in "${commands[@]}"
do
    java -jar $JAVA_JAR --help $i
done



