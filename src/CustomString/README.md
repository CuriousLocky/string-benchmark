# Custom String Representation

This directory contains the customized string representations optimized for certain operations.

Not all representations support all String methods (at least not efficiently).

## Array Rope
A rope-like structure that combines different character sequences in an array. 
A sequence can be a subsequence from another to skip copying.  
### Pros
- Fast concatenation 
- Fast subsequence
### Cons
- Difficult to perform search 
- May not be efficient for small string concat