ScalikeJDBC Mapper Generator Gradle Plugin
============================================

[![Build Status](https://travis-ci.org/grimrose/scalikejdbc-mapper-generator-gradle-plugin.svg?branch=master)](https://travis-ci.org/grimrose/scalikejdbc-mapper-generator-gradle-plugin)

Gradle Plugin for ScalikeJDBC Mapper Generator http://scalikejdbc.org/

*NOTE*: This is an **Experimental** version. please wait for release version.

## Usage

### How to Setup

#### local Install

```
git clone git@github.com:grimrose/scalikejdbc-mapper-generator-gradle-plugin.git
cd scalikejdbc-mapper-generator-gradle-plugin
./gradlew clean install
```

#### build.gradle

```
buildscript {
    repositories {
        mavenLocal()
        jcenter()
    }
    dependencies {
        classpath "org.grimrose.gradle:scalikejdbc-mapper-generator-gradle-plugin:0.1-SNAPSHOT"
        // JDBC Driver
        classpath "com.h2database:h2:1.4.177"
    }
}
```

#### project/scalikejdbc.properties

```
# ---
# jdbc settings

jdbc.driver=org.h2.Driver
jdbc.url=jdbc:h2:file:db/hello
jdbc.username=sa
jdbc.password=
jdbc.schema=

# ---
# source code generator settings

generator.packageName=models
# generator.lineBreak: LF/CRLF
geneartor.lineBreak=LF
# generator.template: interpolation/queryDsl
generator.template=queryDsl
# generator.testTemplate: specs2unit/specs2acceptance/ScalaTestFlatSpec
generator.testTemplate=specs2unit
generator.encoding=UTF-8
# When you're using Scala 2.11 or higher, you can use case classes for 22+ columns tables
generator.caseClassOnly=true
```

### Available Task

```
ScalikeJDBC Mapper Generator tasks
----------------------------------
scalikejdbcGen - Generates a model for a specified table
scalikejdbcGenAll - Generates models for all tables
scalikejdbcGenAllForce - Generates and overwrites models for all tables
scalikejdbcGenEcho - Prints a model for a specified table
scalikejdbcGenForce - Generates and overwrites a model for a specified table
```

```
gradle scalikejdbcGen -PtableName=$tableName -PclassName=$className
```

e.g.

```
gradle scalikejdbcGen -PtableName=company
gradle scalikejdbcGen -PtableName=companies -PclassName=Company
```

### Output Example

See [Reverse Engineering](http://scalikejdbc.org/documentation/reverse-engineering.html)


## License

Published binary files have the following copyright:

```
Copyright 2012 Kazuhiro Sera
Copyright 2014 grimrose

Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0.html
```

