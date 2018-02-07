# APIDiff

A tool to identify API breaking and non-breaking changes between two versions of a Java library. APIDiff analyses libraries hosted on the distributed version control system _git_.

### Contributors
The current version is maintained by [Aline Brito](https://github.com/alinebrito) and the initial algorithm ([v0.0.1](https://github.com/alinebrito/apidiff/releases/tag/v0.0.1)) was developed by [Laerte Xavier](https://github.com/xavierlaerte). 

This project has been developed  with the orientation of the professor [Marco Tulio Valente](https://github.com/mtov) ([Aserg](http://aserg.labsoft.dcc.ufmg.br/) [UFMG](https://www.ufmg.br/)) and professor [Andre Hora](https://github.com/andrehora) ([UFMS](https://www.ufms.br/) [FACOM](https://www.facom.ufms.br/)).

### Filtering Packages

It is possible to filter in or filter out packages according to their names. 

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
