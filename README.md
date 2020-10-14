# APIDiff

A tool to identify API breaking and non-breaking changes between two versions of a Java library. APIDiff analyses libraries hosted on the distributed version control system _git_.

## Catalog

_Breaking Changes_ are modifications performed in API elements such as types, methods, and fields that may break client applications:

| Element  | Breaking Changes (BC) |
| ------------- | ------------- |
| Type  | rename, move, move and rename, remove, lost visibility, add final modifier,  remove static modifier, change in supertype, remove supertype |
| Method  | move, rename, remove, push down, inline, change in parameter list, change in exception list, change in return type, lost visibility, add final modifier, remove static modifier  | 
| Field  |  remove, move, push down field, change in default value, change in type field,  lost visibility, add final modifier | 

_Non-breaking Changes_ are modifications that do not break clients:

| Element  | Non-breaking Changes (NBC) |
| ------------- | ------------- |
| Type  | add, extract supertype, gain visibility, remove final modifier, add static modifier, add supertype, deprecated type|
| Method  | pull up, vain visibility, remove final modifier, add static modifier, deprecated method, add, extract| 
| Field  | pull up, add, deprecated field, gain visibility, remove final modifier|


The refactorings catalog is reused from [RefDiff](https://github.com/aserg-ufmg/RefDiff).

## Examples

* Detecting changes in version histories:

```java
APIDiff diff = new APIDiff("bumptech/glide", "https://github.com/bumptech/glide.git");
diff.setPath("/home/projects/github");

Result result = diff.detectChangeAllHistory("master", Classifier.API);
for(Change changeMethod : result.getChangeMethod()){
    System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
}
```
* Detecting changes in specific commit:

```java
APIDiff diff = new APIDiff("mockito/mockito", "https://github.com/mockito/mockito.git");
diff.setPath("/home/projects/github");

Result result = diff.detectChangeAtCommit("4ad5fdc14ca4b979155d10dcea0182c82380aefa", Classifier.API);
for(Change changeMethod : result.getChangeMethod()){
    System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
}
```
* Fetching new commits:

```java
APIDiff diff = new APIDiff("bumptech/glide", "https://github.com/bumptech/glide.git");
diff.setPath("/home/projects/github");
    
Result result = diff.fetchAndDetectChange(Classifier.API);
for(Change changeMethod : result.getChangeMethod()){
    System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
}
```

* Writing a CSV file:

```java
APIDiff diff = new APIDiff("mockito/mockito", "https://github.com/mockito/mockito.git");
diff.setPath("/home/projects/github");
Result result = diff.detectChangeAtCommit("4ad5fdc14ca4b979155d10dcea0182c82380aefa", Classifier.API);
		
List<String> listChanges = new ArrayList<String>();
listChanges.add("Category;isDeprecated;containsJavadoc");
for(Change changeMethod : result.getChangeMethod()){
    String change = changeMethod.getCategory().getDisplayName() + ";" + changeMethod.isDeprecated()  + ";" + changeMethod.containsJavadoc() ;
    listChanges.add(change);
}
UtilFile.writeFile("output.csv", listChanges);
```

* Filtering Packages according to their names:

```java 
Classifier.INTERNAL: Elements that are in packages with the term "internal".

Classifier.TEST: Elements that are in packages with the terms "test"|"tests", or is in source file "src/test", or ends with "test.java"|"tests.java".

Classifier.EXAMPLE: Elements that are in packages with the terms "example"|"examples"|"sample"|"samples"|"demo"|"demos"

Classifier.EXPERIMENTAL: Elements that are in packages with the term "experimental".

Classifier.NON_API: Internal, test, example or experimental elements.

Classifier.API: Elements that are not non-APIs.
``` 

## Usage

APIDiff is available in the [Maven Central Repository](https://mvnrepository.com/artifact/com.github.aserg-ufmg/apidiff/2.0.0):

```xml
<dependency>
    <groupId>com.github.aserg-ufmg</groupId>
    <artifactId>apidiff</artifactId>
    <version>2.0.0</version>
</dependency>
```
## Publications

Aline Brito, Laerte Xavier, Andre Hora, Marco Tulio Valente. [APIDiff: Detecting API Breaking Changes](http://homepages.dcc.ufmg.br/~mtov/pub/2018-saner-apidiff.pdf). In 25th International Conference on Software Analysis, Evolution and Reengineering (SANER), Tool Track, pages 1-5, 2018.

Learn more about our research group at http://aserg.labsoft.dcc.ufmg.br/
