[![](https://jitpack.io/v/Kaufland/andcouchbaseentity.svg)](https://jitpack.io/#Kaufland/andcouchbaseentity)
[![KIS](https://img.shields.io/badge/KIS-awesome-red.svg)](http://www.spannende-it.de)


Is a library that generates Entities and methods to modify data easily for [couchbase-lite-android](https://github.com/couchbase/couchbase-lite-android).



## Features

* Easy to use just annotate class and fields

* No performance loose all Entities are generated on compile time.

* No more String based Map modifications are needed. Framework also generates Constants to use them in Views.

* Entities updates can be performed with a fluent-Api

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
    compile 'com.github.Kaufland.andcouchbaseentity:couchbase-entity-api:0.0.3'
    apt 'com.github.Kaufland.andcouchbaseentity:couchbase-entity:0.0.3'
    ```

3. Configure Library 

* Add following Code in your Application.class

``` java
@Override
    public void onCreate() {
        super.onCreate();
        enableLogging();
        PersistenceConfig.configure(new PersistenceConfig.DatabaseGet() {
            @Override
            public Database getDatabase() {
                return Application.this.getDatabase();
            }
        });
    }
```
  
 * Annotate classes to generate Entities
  
  ``` java
@CblEntity
public class List {

    @CblField
    String type;

    @CblField("title")
    String title;
    
    @CblField(value = "image", attachmentType = "image/jpg")
    protected InputStream image;
   ```

 * Annotate classes to generate Entities (All generated Classes has the suffix Entity)

 ``` java
  ListEntity mList = ListEntity.create().
                setType("list").
                setCreatedAt(currentTimeString).
                setMembers(new ArrayList<String>()).
                setTitle(title).
                save();
                
 ```
