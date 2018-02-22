# APIDiff

A tool to identify API breaking and non-breaking changes between two versions of a Java library. APIDiff analyses libraries hosted on the distributed version control system _git_.

### Contributors
The project has been maintained by [Aline Brito](https://github.com/alinebrito) and [Laerte Xavier](https://github.com/xavierlaerte) under supervision of Professor [Marco Tulio Valente](https://github.com/mtov) ([Aserg](http://aserg.labsoft.dcc.ufmg.br/) [UFMG](https://www.ufmg.br/)) and Professor [Andre Hora](https://github.com/andrehora) ([UFMS](https://www.ufms.br/) [FACOM](https://www.facom.ufms.br/)).

### Catalog

The following _Breaking Changes_ (BC) are supported: 

| Element  | Breaking Changes (BC) |
| ------------- | ------------- |
| Type  | Rename Type, Move Type, Move and Rename Type, Remove Type, Lost Visibility, Add Final Modifier,  Remove Static Modifier, Change in Supertype, Remove Supertype |
| Method  | Move Method, Rename Method, Remove Method, Push Down Method, Inline Method, Change in Parameter List, Change in Exception List, Change in Return Type Method, Lost Visibility, Add Final Modifier, Remove Static Modifier  | 
| Field  |  Remove Field, Move Field, Push Down Field, Change in Default Value, Change in Type Field,  Lost Visibility, Add Final Modifier | 

The following _Non-breaking Changes_ (NBC) are supported: 

| Element  | Non-breaking Changes (NBC) |
| ------------- | ------------- |
| Type  | Add Type, Extract Supertype, Gain Visibility, Remove Final Modifier, Add Static Modifier, Add Supertype, Depreciate Type |
| Method  | Pull Up Method, Gain Visibility, Remove Final Modifier, Add Static Modifier, Depreciate Method, Add Method, Extract Method| 
| Field  | Pull Up Field, Add Field, Depreciate Field, Gain Visibility, Remove Final Modifier|


The refactorings catalog is reused from [RefDiff](https://github.com/aserg-ufmg/RefDiff).

### Filtering Packages

It is possible to filter in or filter out packages according to their names. 

```java 
Classifier.INTERNAL: Elements that are in packages with the term "internal".

Classifier.TEST: Elements that are in packages with the terms "test"|"tests", or is in source file "src/test", or ends with "test.java"|"tests.java".

Classifier.EXAMPLE: Elements that are in packages with the terms "example"|"examples"|"sample"|"samples"|"demo"|"demos"

Classifier.EXPERIMENTAL: Elements that are in packages with the term "experimental".

Classifier.NON_API: Internal, test, example or experimental elements.

Classifier.API: Elements that are not non-APIs.
``` 

### Usage Scenarios

Detecting changes in version histories:

```java
APIDiff diff = new APIDiff("bumptech/glide", "https://github.com/bumptech/glide.git");
diff.setPath("/home/projects/github");

Result result = diff.detectChangeAllHistory("master", Classifier.API);
for(Change changeMethod : result.getChangeMethod()){
    System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
}
```
Detecting changes in specific commit:

```java
APIDiff diff = new APIDiff("mockito/mockito", "https://github.com/mockito/mockito.git");
diff.setPath("/home/projects/github");

Result result = diff.detectChangeAtCommit("4ad5fdc14ca4b979155d10dcea0182c82380aefa", Classifier.API);
for(Change changeMethod : result.getChangeMethod()){
    System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
}
```
Fetching new commits:

```java
APIDiff diff = new APIDiff("bumptech/glide", "https://github.com/bumptech/glide.git");
diff.setPath("/home/projects/github");
    
Result result = diff.fetchAndDetectChange(Classifier.API);
for(Change changeMethod : result.getChangeMethod()){
    System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
}
```

Writing a CSV file:

```java
APIDiff diff = new APIDiff("mockito/mockito", "https://github.com/mockito/mockito.git");
diff.setPath("/home/projects/github");
Result result = diff.detectChangeAtCommit("4ad5fdc14ca4b979155d10dcea0182c82380aefa", Classifier.API);
		
List<String> listChanges = new ArrayList<String>();
listChanges.add("Category;isDepreciated;containsJavadoc");
for(Change changeMethod : result.getChangeMethod()){
    String change = changeMethod.getCategory().getDisplayName() + ";" + changeMethod.getDepreciated()  + ";" + changeMethod.getJavadoc() ;
    listChanges.add(change);
}
UtilFile.writeFile("output.csv", listChanges);
```

### Publications

Aline Brito, Laerte Xavier, Andre Hora, Marco Tulio Valente. [APIDiff: Detecting API Breaking Changes](http://homepages.dcc.ufmg.br/~mtov/pub/2018-saner-apidiff.pdf). In 25th International Conference on Software Analysis, Evolution and Reengineering (SANER), Tool Track, pages 1-5, 2018.

Learn more about our research group at http://aserg.labsoft.dcc.ufmg.br/
