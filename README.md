line-parser  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/line-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/line-parser) [![Javadocs](http://www.javadoc.io/badge/com.github.marschall/line-parser.svg)](http://www.javadoc.io/doc/com.github.marschall/line-parser) [![Build Status](https://travis-ci.org/marschall/line-parser.svg?branch=master)](https://travis-ci.org/marschall/line-parser)
===========

```xml
<dependency>
    <groupId>com.github.marschall</groupId>
    <artifactId>line-parser</artifactId>
    <version>0.4.2</version>
</dependency>
```

An `mmap()` based line parser for cases when:

 * the start byte position of a line in the file is required
 * the length in bytes of a line is required
 * only a few character of every line are required

In these cases this library can theoretically be more efficient than `BufferedReader` because:

 * the copy operations of buffered IO are avoided
 * the allocation and resizing of an intermediate `StringBuffer` is avoided
 * the allocation of the final `String` is avoided, only the required substrings
   are allocated

The performance may still be slower than a than `BufferedReader` based approach but it should consume much less memory bandwidth and produce only a fraction of the garbage.

As this project gives you a `CharSequence` instead of a `String` you may want to have a look at the [charsequences](https://github.com/marschall/charsequences) which gives you some the `String` convenience methods while avoiding allocation.

Misc
----

 * the main parsing loop is likely to benefit from [on-stack replacement (OSR)](http://openjdk.java.net/groups/hotspot/docs/HotSpotGlossary.html#onStackReplacement)
 * if you're using UTF-8 with a [BOM](https://en.wikipedia.org/wiki/Byte_order_mark) then the BOM is returned as well
 * if you're using UTF-16 with a [BOM](https://en.wikipedia.org/wiki/Byte_order_mark) then the BOM is returned as well
 * the library runs on Java 8 but is also a Java 9 module that only requires the `jdk.unsupported` module besides the `java.base` module

Usage
-----

```java
LineParser parser = new LineParser();
parser.forEach(path, cs, (line) -> {
  System.out.printf("[%d,%d]%s%n", line.getOffset(), line.getLength(), line.getContent());
});
```


