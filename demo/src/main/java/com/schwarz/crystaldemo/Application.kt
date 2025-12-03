package com.schwarz.crystaldemo

import android.util.Log
import com.couchbase.lite.Blob
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.PersistenceException
import com.schwarz.crystalapi.TypeConversionErrorWrapper
import com.schwarz.crystalcouchbaseconnector.Couchbase2Connector
import com.schwarz.crystaldemo.entity.ProductEntity
import com.schwarz.crystaldemo.entity.UserCommentWrapper

class Application : android.app.Application() {
    private var mDatabase: Database? = null

    val database: Database?
        get() {
            return try {
                if (mDatabase == null) {
                    val config = DatabaseConfiguration(applicationContext)
                    mDatabase = Database(DB, config)
                }
                mDatabase
            } catch (e: CouchbaseLiteException) {
                Log.e(TAG, "failed to get Database", e)
                null
            }
        }

    override fun onCreate() {
        super.onCreate()

        deleteDbIfExists()
        PersistenceConfig.configure(
            object : Couchbase2Connector() {
                override fun getDatabase(name: String): Database {
                    if (DB == name) {
                        return database!!
                    }
                    throw RuntimeException("wrong db name defined!!")
                }

                override fun invokeOnError(errorWrapper: TypeConversionErrorWrapper) {
                    if (errorWrapper.exception is java.lang.ClassCastException) {
                        Log.e(
                            TAG,
                            "Data type manipulated: Tried to cast ${errorWrapper.value} into ${errorWrapper.`class`}"
                        )
                    } else {
                        throw errorWrapper.exception
                    }
                }
            }
        )
        createMockArticle()
    }

    private fun deleteDbIfExists() {
        try {
            database!!.delete()
            mDatabase = null
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, "failed to clear Database", e)
        }
    }

    private fun createMockArticle() {
        try {
            ProductEntity.create().builder().setName("Beer").setComments(
                listOf(
                    UserCommentWrapper.create().builder().setComment("very awesome").exit(),
                    UserCommentWrapper.create().builder().setComment("tasty").exit()
                )
            ).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit().save()
            ProductEntity.create().builder().setName("Beer (no alcohol)").setComments(
                listOf(
                    UserCommentWrapper.create().builder().setComment("very bad").exit(),
                    UserCommentWrapper.create().builder().setComment("not tasty").setAge(99).exit()
                )
            ).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit().save()
            ProductEntity.create().builder().setName("Wodka").setComments(
                listOf(
                    UserCommentWrapper.create().builder().setComment("feeling like touch the sky")
                        .exit()
                )
            ).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit().save()
            ProductEntity.create().builder().setName("Gin").setComments(
                listOf(
                    UserCommentWrapper.create().builder().setComment("hipster drink but great")
                        .exit(),
                    UserCommentWrapper.create().builder().setComment("tasty!!!").exit()
                )
            ).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit().save()
            ProductEntity.create().builder().setName("Apple").setComments(
                listOf(
                    UserCommentWrapper.create().builder().setComment("mhmhmh tasty!").exit(),
                    UserCommentWrapper.create().builder().setComment("dont like it").exit()
                )
            ).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit().save()
            ProductEntity.create().builder().setName("Tomatoes").setComments(
                listOf(
                    UserCommentWrapper.create().builder().setComment("don't like there color")
                        .exit(),
                    UserCommentWrapper.create().builder().setComment("worst experience ever!!")
                        .exit()
                )
            ).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder)))
                .exit().save()
        } catch (e: PersistenceException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = Application::class.java.name
        const val DB = "mydb_db"
    }
}
