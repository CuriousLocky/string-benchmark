#!/bin/bash

test_cmd="/mnt/sdb/hexiang/openjdk-oracle-23.0.1/bin/java -javaagent:/mnt/sdb/hexiang/.cache/JetBrains/RemoteDev/dist/a3c4f5cf4aad7_ideaIU-243.19420.21/lib/idea_rt.jar=40087:/mnt/sdb/hexiang/.cache/JetBrains/RemoteDev/dist/a3c4f5cf4aad7_ideaIU-243.19420.21/bin -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /mnt/sdb/hexiang/string-benchmark/out/production/string-benchmark:/mnt/sdb/hexiang/.m2/repository/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37.jar:/mnt/sdb/hexiang/.m2/repository/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar:/mnt/sdb/hexiang/.m2/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar:/mnt/sdb/hexiang/.m2/repository/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar org.openjdk.jmh.Main"
individuals=("Concat" "Compare" "Match")
combines=("ConcatSub" "ConcatMatch")

for test in "${individuals[@]}"
do
  $test_cmd "IndividualMicros.$test.*" -rf json -rff "/mnt/sdb/hexiang/string-benchmark/src/IndividualMicros/logs/$test-results.json"
done

for test in "${combines[@]}"
do
  $test_cmd "CombinedMicros.$test.*" -rf json -rff "/mnt/sdb/hexiang/string-benchmark/src/CombinedMicros/logs/$test-results.json"
done