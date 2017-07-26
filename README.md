[![](https://jitpack.io/v/Kaufland/andcouchbaseentity.svg)](https://jitpack.io/#Kaufland/andcouchbaseentity)
[![Build Status](https://travis-ci.org/Kaufland/andcouchbaseentity.svg?branch=master)](https://travis-ci.org/Kaufland/andcouchbaseentity)
[![codecov](https://codecov.io/gh/Kaufland/andcouchbaseentity/branch/master/graph/badge.svg)](https://codecov.io/gh/Kaufland/andcouchbaseentity)
[![KIS](https://img.shields.io/badge/KIS-awesome-red.svg)](http://www.spannende-it.de)



Is a library that generates entities and methods to easily modify data for [couchbase-lite-android](https://github.com/couchbase/couchbase-lite-android).



## Features

* Easy to use - just annotate class and fields

* Also supports child entities or lists of child entities

* No performance loss - all entities are being generated during compile time.

* No more string-based map modifications - the framework also generates constants for usage in views.

* Entities updates can be performed with a fluent API

## Implementation


1. Add it in your root build.gradle at the end of repositories:

```
 buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
  }
  ...
  allprojects {
    repositories {
	maven { url 'https://jitpack.io' }
    }
  }
```

2. Add gradle dependency

    ```
    compile 'com.github.Kaufland.andcouchbaseentity:couchbase-entity-api:0.9.5'
    apt 'com.github.Kaufland.andcouchbaseentity:couchbase-entity:0.9.5'
    ```

3. Configure library 

* Add the following Code in your Application.class

``` java
@Override
    public void onCreate() {
        super.onCreate();
        PersistenceConfig.configure(new PersistenceConfig.DatabaseGet() {
            @Override
            public Database getDatabase() {
                return Application.this.getDatabase();
            }
        });
    }
```
  
 * Annotate classes to generate entities
  
  ``` java
@CblEntity
public class List {

    @CblConstant(value = "type", constant = "List")
    private String type;

    @CblField("title")
    String title;
    
    @CblField(value = "image", attachmentType = "image/jpg")
    protected InputStream image;
    
    @CblField("sub")
    Sub sub;

    @CblField("list_sub")
    java.util.List<Sub> listSub;
   ```

 * Annotate classes to generate entities (All generated Classes have the suffix Entity)

 ``` java
  ListEntity mList = ListEntity.create().           
                setCreatedAt(currentTimeString).
                setMembers(new ArrayList<String>()).
                setTitle(title).
                setSub(SubEntity.create().setTest("test")).
                save();
                
 ```
