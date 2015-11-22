line-parser
===========

An `mmap()` based line parser for cases when:

 * the start position in the file of a line is required
 * the length in bytes of a line is required
 * only a few character of every line is required

In these cases it can be more efficient than `BufferedReader` because:

 * the copy operations of buffered IO are avoided
 * the allocation and resizing of an intermediate StringBuffer is avoided
 * the allocation of the final String is avoided, only the required substrings
   are allocated


Limitations
-----------

 * doesn't handle files larger than 2 GB
 * doesn't handle file encodings with a BOM

