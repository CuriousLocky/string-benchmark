# Combined Micro Benchmarks

This directory contains micro benchmarks that combines multiple operations.

All benchmark is run on the following platform unless specified.

- CPU: Xeon 4210R
- OS: Ubuntu 20.04.5
- JDK: OpenJDK-23.0.1 Oracle
- Unit: us/op (shorter is better)

## Concat-Sub
This benchmark tests String concatenation + substring operations.

In each run, a string `str1` is constructed by concatenating `1000` random strings.
Then a string `str2` is constructed by concatenating `1000` substrings of size `100` from `str1`. 
The time taken to construct `str2` is recorded.

| Benchmark                      | Time       |
|--------------------------------|------------|
| ConcatSub.arrayRope            | 147.759    |
| ConcatSub.arrayRopeImmutable   | 2086.833   |
| ConcatSub.arrayRopeNoMat       | 64.849     |
| ConcatSub.stringBuilder        | 1411.454   |
| ConcatSub.stringBuilderNoMat   | 1090.161   |
| ConcatSub.stringNaive          | 15741.802  |

