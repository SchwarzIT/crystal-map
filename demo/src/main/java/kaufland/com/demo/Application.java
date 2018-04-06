package kaufland.com.demo;

import android.support.annotation.Nullable;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.demo.entity.ProductEntity;
import kaufland.com.demo.entity.UserCommentEntity;

public class Application extends android.app.Application {


    private static final String TAG = Application.class.getName();
    public static final String DB = "mydb_db";
    private Manager mManager;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mManager == null) {
            try {
                AndroidContext context = new AndroidContext(getApplicationContext());
                mManager = new Manager(context, Manager.DEFAULT_OPTIONS);
            } catch (Exception e) {
                Log.e(TAG, "Cannot create Manager object", e);
            }
        }

        deleteDbIfExists();

        PersistenceConfig.configure(new PersistenceConfig.DatabaseGet() {
            @Override
            public Database getDatabase(String name) {
                if (DB.equals(name)) {
                    return Application.this.getDatabase();
                }
                throw new RuntimeException("wrong db name defined!!");
            }
        });
        createMockArticle();
    }

    private void deleteDbIfExists() {
        if (getDatabase().exists()) {
            try {
                getDatabase().delete();
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "failed to clear Database", e);
            }
        }
    }

    @Nullable
    public Database getDatabase() {
        try {
            return mManager.getDatabase(DB);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "failed to get Database", e);
            return null;
        }
    }

    private void createMockArticle() {
        try {
            ProductEntity.create().setName("Beer").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("very awesome"), UserCommentEntity.create().setComment("tasty")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
            ProductEntity.create().setName("Beer (no alcohol)").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("very bad"), UserCommentEntity.create().setComment("not tasty")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
            ProductEntity.create().setName("Wodka").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("feeling like touch the sky")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
            ProductEntity.create().setName("Gin").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("hipster drink but great"), UserCommentEntity.create().setComment("tasty!!!")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
            ProductEntity.create().setName("Apple").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("mhmhmh tasty!"), UserCommentEntity.create().setComment("dont like it")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
            ProductEntity.create().setName("Tomatoes").
                    setComments(new ArrayList<>(Arrays.asList(UserCommentEntity.create().setComment("don't like there color"), UserCommentEntity.create().setComment("worst experience ever!!")))).
                    setInputStream(getResources().openRawResource(R.raw.ic_kaufland_placeholder)).
                    save();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }


}
