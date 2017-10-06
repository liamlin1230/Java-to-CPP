#!/bin/bash

sbt assembly
mv target/scala-2.11/translator-assembly-1.0.jar .

for i in {0..20}; do
    java -cp translator-assembly-1.0.jar edu.nyu.oop.Boot -translateJavaCode \
        src/test/java/inputs/test$(printf %03d $i)/Test$(printf %03d $i).java
    g++ -o output/output.out output/java_lang.cpp output/output.cpp

    echo "Testing input $i"
    echo "Java output:"
    java -cp target/scala-2.11/test-classes/ inputs.test$(printf %03d $i).Test$(printf %03d $i)
    echo "C++ output:"
    ./output/output.out
done
