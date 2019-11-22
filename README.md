[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.tigris.jsapar/jsapar/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.tigris.jsapar/jsapar)
[![Javadocs](https://javadoc.io/badge/org.tigris.jsapar/jsapar.svg)](https://javadoc.io/doc/org.tigris.jsapar/jsapar)
[![Build Status](https://travis-ci.org/org-tigris-jsapar/jsapar.png?branch=master)](https://travis-ci.org/org-tigris-jsapar/jsapar)

[![Java 8](https://img.shields.io/badge/java-8-brightgreen.svg)](#java-8)
[![Java 9-ea](https://img.shields.io/badge/java-9-brightgreen.svg)](#java-9)
[![Java 10-ea](https://img.shields.io/badge/java-10-brightgreen.svg)](#java-10)
[![Java 11-ea](https://img.shields.io/badge/java-11-brightgreen.svg)](#java-11)
[![Java 12-ea](https://img.shields.io/badge/java-12-brightgreen.svg)](#java-12)
[![Java 13-ea](https://img.shields.io/badge/java-13-brightgreen.svg)](#java-13)
# jsapar
**JSaPar** stands for  **J**ava **S**chem**a** based **Par**ser

JSaPar is a Java library providing a schema based parser and composer of almost all sorts of delimited and fixed 
width files.

It is an open source java library created with the purpose of
making it easy to process delimited and fixed width data sources.
By separating the description of the data format into a schema that can be loaded from XML it makes the code
easier to maintain and increases flexibility.

* [Documentation](https://org-tigris-jsapar.github.io/jsapar/)
* [Release notes](https://org-tigris-jsapar.github.io/jsapar/release_notes)
* [Javadocs API documentation](https://javadoc.io/doc/org.tigris.jsapar/jsapar)
* [Examples](https://github.com/org-tigris-jsapar/jsapar-examples)

## News
### Version 2.1.0 is released 
*2019-11-05*

The focus of this fix-release has been to add some missing functionality such as enums and to build using Java 11. See [release notes](https://org-tigris-jsapar.github.io/jsapar/release_notes#2.1.0) for details
### Version 2.0.1 is released 
*2019-03-04*

The focus of this fix-release has been to improve performance while parsing. See [release notes](https://org-tigris-jsapar.github.io/jsapar/release_notes#2.0.1) for details
### Version 2.0.0 is now officially released 
*2018-11-24*

Version 2.0.0 is now officially released and is available in <a href="http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.tigris.jsapar%22%20AND%20a%3A%22jsapar%22">maven repository</a>.
## Mission
The goal of this project is a java library that removes the burden of parsing and composing flat files and CSV files from the developer.

The library should
* Be easy to use for both simple and complex situations.
* Be possible to extend.
* Have a low memory impact and good performance.
* Be flexible to use in different situations.
* Be independent of other (third party) libraries.
* Use schemas in order to distinctly separate the description of the format of the data source from the code.
* Unburden the tremendous tasks of a developer dealing with fixed width and delimited data sources.

## Features
* Support for flat files with fixed positions.
* Support for CSV and all other delimited files such as TAB-separated or multi character separated.
* Configurable line separator character sequence.
* Support for quoted CSV cells.
* Support for multi line quoted CSV cells. Line breaks are allowed within quoted cells.
* Support for type conversion while parsing and composing.
* Can handle internationalization of numbers and dates both while parsing and composing.
* Support for different type of lines where line type is determined by the value of defined "condition cells". 
* Support converting Java objects to or from any of the other supported input or output formats.
* The schema can be expressed with xml notation or created directly within the java code.
* The parser can either produce a Document class, representing the content of the file, or you can choose to receive
 events for each line that has been successfully parsed.
* Can handle huge files without loading everything into memory.
* The output Document class contains a list of lines which contains a list of cells.
* The input and outputs are given by java.io.Reader and java.io.Writer which means that it is not necessarily files
that are parsed or generated.
* The schema contains information about the format of each cell regarding data type and syntax.
* Parsing errors can either be handled by exceptions thrown at first error or the errors can be collected during
parsing to be able to deal with them later.
* Support for consuming or producing an internal xml format which can be used to transform any of the supported formats 
into any markup language by the use of xslt.

## Quality goals
* All features fully documented, discussed and demonstrated.
* Unit tests for (almost) all classes within the library.
* Examples demonstrating all features.

We are not quite there yet, but we are working on it...
## Community
* Bugs and suggestions can be submitted [here on Github](https://github.com/org-tigris-jsapar/jsapar/issues). 
* For other type of questions, use the [`[jsapar]` tag in Stack exchange](https://stackoverflow.com/questions/tagged/jsapar). Remember to add the tag to new questions.
