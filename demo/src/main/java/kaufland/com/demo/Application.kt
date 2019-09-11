package kaufland.com.demo

import android.util.Log
import com.couchbase.lite.Blob
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException
import kaufland.com.couchbaseentityconnector.Couchbase2Connector
import kaufland.com.demo.entity.ProductEntity
import kaufland.com.demo.entity.UserCommentWrapper
import java.util.*

class Application : android.app.Application() {

    private var mDatabase: Database? = null

    val database: Database?
        get() {
            try {
                if (mDatabase == null) {
                    val config = DatabaseConfiguration(applicationContext)
                    mDatabase = Database(DB, config)
                }
                return mDatabase
            } catch (e: CouchbaseLiteException) {
                Log.e(TAG, "failed to get Database", e)
                return null
            }

        }

    override fun onCreate() {
        super.onCreate()

        deleteDbIfExists()
        PersistenceConfig.configure(object : Couchbase2Connector() {
            override fun getDatabase(name: String): Database {
                if (DB == name) {
                    return database!!
                }
                throw RuntimeException("wrong db name defined!!")
            }
        })
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
            ProductEntity.create().setName("Beer").setComments(listOf(UserCommentWrapper.create().setComment("very awesome"), UserCommentWrapper.create().setComment("tasty"))).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder))).save()
            ProductEntity.create().setName("Beer (no alcohol)").setComments(listOf(UserCommentWrapper.create().setComment("very bad"), UserCommentWrapper.create().setComment("not tasty").setAge(99))).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder))).save()
            ProductEntity.create().setName("Wodka").setComments(listOf(UserCommentWrapper.create().setComment("feeling like touch the sky"))).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder))).save()
            ProductEntity.create().setName("Gin").setComments(listOf(UserCommentWrapper.create().setComment("hipster drink but great"), UserCommentWrapper.create().setComment("tasty!!!"))).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder))).save()
            ProductEntity.create().setName("Apple").setComments(listOf(UserCommentWrapper.create().setComment("mhmhmh tasty!"), UserCommentWrapper.create().setComment("dont like it"))).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder))).save()
            ProductEntity.create().setName("Tomatoes").setComments(listOf(UserCommentWrapper.create().setComment("don't like there color"), UserCommentWrapper.create().setComment("worst experience ever!!"))).setImage(Blob("image/jpeg", resources.openRawResource(R.raw.ic_kaufland_placeholder))).save()
        } catch (e: PersistenceException) {
            e.printStackTrace()
        }

    }

    companion object {


        private val TAG = Application::class.java.name
        @JvmField val DB = "mydb_db"
    }


}
