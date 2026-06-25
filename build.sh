#!/bin/bash
set -e

echo "Compiling..."
mkdir -p bin
javac -d bin src/*.java
cp src/*.jpg bin/ 2>/dev/null || true

echo "Packaging JAR..."
jar cfe ImageConverter.jar Main -C bin .

rm -rf bin
echo "Done! Created ImageConverter.jar"
