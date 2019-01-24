package kaufland.com.demo;

import android.support.annotation.Nullable;
import android.util.Log;

import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.coachbasebinderapi.PersistenceException;
import kaufland.com.couchbaseentityconnector.Couchbase2Connector;
import kaufland.com.demo.entity.ProductEntity;
import kaufland.com.demo.entity.UserCommentWrapper;

public class Application extends android.app.Application {


    private static final String TAG = Application.class.getName();
    public static final String DB = "mydb_db";

    private Database mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        deleteDbIfExists();

        PersistenceConfig.configure(new Couchbase2Connector() {
            @Override
            protected Database getDatabase(String name) {
                if (DB.equals(name)) {
                    return Application.this.getDatabase();
                }
                throw new RuntimeException("wrong db name defined!!");
            }
        });
        createMockArticle();
    }

    private void deleteDbIfExists() {

        try {
            getDatabase().delete();
            mDatabase = null;
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "failed to clear Database", e);
        }
    }

    @Nullable
    public Database getDatabase() {
        try {
            if (mDatabase == null) {
                DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
                mDatabase = new Database(DB, config);
            }
            return mDatabase;
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "failed to get Database", e);
            return null;
        }
    }

    private void createMockArticle() {
        try {
            ProductEntity.create().setName("Beer").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentWrapper.create().setComment("very awesome"), UserCommentWrapper.create().setComment("tasty")))).
                    setImage(new Blob("image/jpeg", getResources().openRawResource(R.raw.ic_kaufland_placeholder))).
                    save();
            ProductEntity.create().setName("Beer (no alcohol)").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentWrapper.create().setComment("very bad"), UserCommentWrapper.create().setComment("not tasty").setAge(99)))).
                    setImage(new Blob("image/jpeg", getResources().openRawResource(R.raw.ic_kaufland_placeholder))).
                    save();
            ProductEntity.create().setName("Wodka").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentWrapper.create().setComment("feeling like touch the sky")))).
                    setImage(new Blob("image/jpeg", getResources().openRawResource(R.raw.ic_kaufland_placeholder))).
                    save();
            ProductEntity.create().setName("Gin").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentWrapper.create().setComment("hipster drink but great"), UserCommentWrapper.create().setComment("tasty!!!")))).
                    setImage(new Blob("image/jpeg", getResources().openRawResource(R.raw.ic_kaufland_placeholder))).
                    save();
            ProductEntity.create().setName("Apple").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentWrapper.create().setComment("mhmhmh tasty!"), UserCommentWrapper.create().setComment("dont like it")))).
                    setImage(new Blob("image/jpeg", getResources().openRawResource(R.raw.ic_kaufland_placeholder))).
                    save();
            ProductEntity.create().setName("Tomatoes").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentWrapper.create().setComment("don't like there color"), UserCommentWrapper.create().setComment("worst experience ever!!")))).
                    setImage(new Blob("image/jpeg", getResources().openRawResource(R.raw.ic_kaufland_placeholder))).
                    save();
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }


}
