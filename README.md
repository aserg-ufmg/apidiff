# APIDiff
Tool to identify breaking changes and non-breaking changes between  versions of a Java library.

### Contributors
The code has been developed by [Laerte Xavier](https://github.com/xavierlaerte) and [Aline Brito](https://github.com/alinebrito).

### Requirements

* [Java 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3.1+](https://maven.apache.org/download.cgi)

### Build aplication.

Set the path of the projetc or the path to clone the projects in file `config.properties`.

For Linux, run the script buil.sh. Example:

```
sh buil.sh
```
For Windows, run the script buit.bat. Example:

```
./buil.bat
```
The file APIDiff.jar is built  in target. Copy APIDiff.jar for a path with permission write.

### Type of APIs

`ClassifierAPI.NON_API_INTERNAL`: Contains the package `internal`.

`ClassifierAPI.NON_API_TEST`: Contains the packages `test`|`tests`, or is in source file `src/test`, or ends with test.java|tests.java.

`ClassifierAPI.NON_API_EXAMPLE`: Contains the packages `example`|`examples`|`sample`|`samples`|`demo`|`demos`

`ClassifierAPI.NON_API_EXPERIMENTAL`: Contains the packages `experimental`.

`ClassifierAPI.NON_API`: Internal, test, example or experimental APIs.

`ClassifierAPI.API`: Other APIs.

### API usage guidelines

Comparing the source code from two folders that contain the code before and after the breaking changes:

```java
  APIDiff diff = new APIDiff("alinebrito/breaking-changes-toy-example");
  diff.calculateDiffProject("v1/alinebrito/breaking-changes-toy-example", "v2/alinebrito/breaking-changes-toy-example", ClassifierAPI.API);
```
Detecting breaking changes in new commits of git repositories:
```java
  APIDiff diff = new APIDiff("alinebrito/breaking-changes-toy-example", "https://github.com/alinebrito/breaking-changes-toy-example.git");
  diff.calculateDiffCommit();
```
The output is a CSV file (output.csv).
