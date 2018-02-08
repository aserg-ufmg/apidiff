# APIDiff

A tool to identify API breaking and non-breaking changes between two versions of a Java library. APIDiff analyses libraries hosted on the distributed version control system _git_.

### Contributors
The project has been maintained by [Aline Brito](https://github.com/alinebrito) and [Laerte Xavier](https://github.com/xavierlaerte) with the orientation of the professor [Marco Tulio Valente](https://github.com/mtov) ([Aserg](http://aserg.labsoft.dcc.ufmg.br/) [UFMG](https://www.ufmg.br/)) and professor [Andre Hora](https://github.com/andrehora) ([UFMS](https://www.ufms.br/) [FACOM](https://www.facom.ufms.br/)).

### Filtering Packages

It is possible to filter in or filter out packages according to their names. 

```java 
Classifier.INTERNAL: Contains the package "internal".

Classifier.TEST: Contains the packages "test"|"tests", or is in source file "src/test", or ends with "test.java"|"tests.java".

Classifier.EXAMPLE: Contains the packages "example"|"examples"|"sample"|"samples"|"demo"|"demos"

Classifier.EXPERIMENTAL: Contains the packages "experimental".

Classifier.NON_API: Internal, test, example or experimental APIs.

Classifier.API: Elements that are not non-APIs.
``` 

### Usage Scenarios

Detecting changes in version histories:

```java
    APIDiff diff = new APIDiff("bumptech/glide", "https://github.com/bumptech/glide.git");
    diff.setPath("/home/aline/Downloads/test");

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
    diff.setPath("/home/aline/Downloads/test");
    Result result = diff.detectChangeAtCommit("4ad5fdc14ca4b979155d10dcea0182c82380aefa", Classifier.API);
		
    List<String> listChanges = new ArrayList<String>();
    listChanges.add("Category;isDepreciated;containsJavadoc");
    for(Change changeMethod : result.getChangeMethod()){
        String change = changeMethod.getCategory().getDisplayName() + ";" + changeMethod.getDepreciated()  + ";" + changeMethod.getJavadoc() ;
        listChanges.add(change);
    }
    UtilFile.writeFile("output.csv", listChanges);
```
