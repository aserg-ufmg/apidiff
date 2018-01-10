# APIDiff
Tool to identify breaking changes and non-breaking changes between  versions of a Java library.

### Contributors
The current version is maintained by [Aline Brito](https://github.com/alinebrito) and the initial algorithm ([v0.0.1](https://github.com/alinebrito/apidiff/releases/tag/v0.0.1)) was developed by [Laerte Xavier](https://github.com/xavierlaerte). 

This project has been developed  with the orientation of the teachers [Marco Tulio Valente](https://github.com/mtov) and [Andre Hora](https://github.com/andrehora) ([Aserg](http://aserg.labsoft.dcc.ufmg.br/) [UFMG](https://www.ufmg.br/)).

The class [GitService](https://github.com/alinebrito/apidiff/blob/master/src/main/java/br/ufmg/dcc/labsoft/apidiff/detect/diff/service/git/GitService.java) was inspired in [RefDiff](https://github.com/aserg-ufmg/RefDiff.git).

### Filtering Packages

`Classifier.INTERNAL`: Contains the package `internal`.

`Classifier.TEST`: Contains the packages `test`|`tests`, or is in source file `src/test`, or ends with `test.java|tests.java`.

`Classifier.EXAMPLE`: Contains the packages `example`|`examples`|`sample`|`samples`|`demo`|`demos`

`Classifier.EXPERIMENTAL`: Contains the packages `experimental`.

`Classifier.NON_API`: Internal, test, example or experimental APIs.

`Classifier.API`: Other APIs.

### API usage guidelines

Detecting changes in version histories:

```java
```
Detecting changes in specific commit:

```java
```
Fetching new commits:

```java
```

Reading and writing a CSV file:

```java
```
