# APIDiff
Tool to identify breaking changes and non-breaking changes between  versions of a Java library.

### Contributors
The code has been developed by [Laerte Xavier](https://github.com/xavierlaerte) and [Aline Brito](https://github.com/alinebrito), inspired by the APIChangeTypeFinderSanner project developed by  [Laerte Xavier](https://github.com/xavierlaerte).

### Requirements

* [Java 1.7+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

* [Maven 3.1+](https://maven.apache.org/download.cgi)

### Build aplication.

For Linux, run the script buil.sh. Example:

```
sh buil.sh
```
For Windows, run the script buit.bat. Example:

```
./buil.bat
```
The file APIDiff.jar is built  in target. Copy APIDiff.jar for a path with permission write.

### Execute Aplication Command Line

Run APIDiff using the following syntax:

```
java -jar APIDiff.jar <path version library old> <path version library new>
```

The output is a CSV file (output.csv).
