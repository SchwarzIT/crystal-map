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
    compile 'com.github.Kaufland.andcouchbaseentity:couchbase-entity-api:1.2.2'
    apt 'com.github.Kaufland.andcouchbaseentity:couchbase-entity:1.2.2'
    ```

3. Configure library 

* Add the following Code in your Application.class

``` java
    @Override
    public void onCreate() {
        super.onCreate();
        PersistenceConfig.configure(new PersistenceConfig.DatabaseGet() {
            @Override
            public Database getDatabase(String name) {
                if (DB.equals(name)) {
                    return Application.this.getDatabase();
                }
                throw new RuntimeException("wrong db name defined!!");
            }
        });
    }
```
  
 * Annotate classes to generate entities (All generated Classes have the suffix Entity)
  
  ``` java
@CblEntity(database = Application.DB)
public class Product {

    @CblConstant(value = "type", constant = "product")
    private String type;

    @CblField("name")
    private String name;

    @CblField("comments")
    private ArrayList<UserComment> comments;

    @CblField(value = "image", attachmentType = "image/jpg")
    private InputStream inputStream;
}
   ```
   * Use CblChild to define ChildEntities
   ``` java
@CblChild
public class UserComment {

    @CblField(value = "comment")
    private String comment;

    @CblField("user")
    @CblDefault("anonymous")
    private String userName;
}
   ```

 * Use generated classes and be happy :-)

 ``` java
   ProductEntity.create().setName("Beer").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("very awesome"), UserCommentEntity.create().setComment("tasty")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
                
 ```
  * hint: to modify childEntities its neccessary to invoke setter before save the parent entity
  
 ``` java
        ArrayList<UserCommentEntity> data = getParentEntity().getComments();
        data.remove(0);
        try {
            getParentEntity().setComments(data).save();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "failed to save Entity", e);
        }
                
 ```  
  
