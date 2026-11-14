#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac -d out $(find src -name "*.java")
echo "=== Tests ==="
java -cp out ahocorasick.AhoCorasickTest
echo
echo "=== Benchmark ==="
java -cp out ahocorasick.Benchmark results
