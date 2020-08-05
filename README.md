[![](https://jitpack.io/v/Kaufland/andcouchbaseentity.svg)](https://jitpack.io/#Kaufland/andcouchbaseentity)
[![Build Status](https://travis-ci.org/SchwarzIT/andcouchbaseentity.svg?branch=master)](https://travis-ci.org/SchwarzIT/andcouchbaseentity)
[![codecov](https://codecov.io/gh/SchwarzIT/andcouchbaseentity/branch/master/graph/badge.svg)](https://codecov.io/gh/Kaufland/andcouchbaseentity)



Is a library that generates entities and methods to easily modify data. 

This Library can be used if your database represents its data in maps


## Features

* Easy to use - just annotate class

* Also supports nested maps or lists

* No performance loss - all entities are being generated during compile time.

* No more string-based map modifications - the framework also generates constants for usage in views or queries.

* Entities updates can be performed with a fluent API

* NEW generate easy find queries based on Entity fields

* NEW generate accessors to static functions/properties in annotated base class

## Quick View

Add Annotations

```kotlin
@Entity(database = "mydb_db")
@Fields(
        Field(name = "type", type = String::class, defaultValue = "product", readonly = true),
        Field(name = "name", type = String::class),
        Field(name = "comments", type = UserComment::class, list = true),
        Field(name = "image", type = Blob::class),
        Field(name = "identifiers", type = String::class, list = true)
)
@Queries(
        Query(fields = ["type"])
)
open class Product{

    companion object{

        @GenerateAccessor
        fun someComplexQuery(param1 : String){
            //do some heavy logic here
        }
    }
}
```
Use it

```kotlin
        ProductEntity
                .create()
                .builder()
                .setName("Beer")
                .setComments(listOf(UserCommentWrapper
                        .create()
                        .builder()
                        .setComment("very awesome")
                        .exit()))
                .setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit()
                .save()

        val allEntitiesOfType = ProductEntity.findByType()
        val resultOfAComplexQuery = ProductEntity.someComplexQuery("foo")
```

### Optional Features

 - Generate Model documentation based declared Entities
 - Generate suspend functions for Database interaction

```kotlin
kapt {
    arguments {
        arg("useSuspend", "false")
        arg("entityframework.documentation.generated", "${buildDir.absolutePath}/entity") //path to generate documentation
        arg("entityframework.documentation.fileName", "demo.html") //optional name for the generated html file
    }
}
```



## Implementation

### [**Guide for Couchbase 2.x.x and other Databases**](https://github.com/SchwarzIT/andcouchbaseentity/wiki/Implementation-Guide-2.x.x)

### [**Guide for Couchbase 3.x.x and other Databases**](https://github.com/SchwarzIT/andcouchbaseentity/wiki/Implementation-Guide-3.x.x)
  
