package kaufland.com.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.demo.entity.ProductEntity;
import kaufland.com.demo.entity.UserCommentEntity;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = CommentActivity.class.getName();
    private ArrayAdapter<String> mAdapter;


    public static Intent buildIntent(MainActivity activity, String id) {
        Intent intent = new Intent(activity, CommentActivity.class);
        intent.putExtra("id", id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ArrayList<UserCommentEntity> data = getParentEntity().getComments();

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, map(data));

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        findViewById(R.id.btn_post).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                ArrayList<UserCommentEntity> mComments = getParentEntity().getComments();
                mComments.add(UserCommentEntity.create().
                        setComment(((EditText) findViewById(R.id.edit_text)).getText().toString()).
                        setUserName("you"));
                try {
                    getParentEntity().setComments(mComments).save();
                    ((EditText) findViewById(R.id.edit_text)).setText("");
                    recreate();
                } catch (CouchbaseLiteException e) {
                    Log.e(TAG, "failed to save", e);
                }
            }
        });
    }

    private List<String> map(ArrayList<UserCommentEntity> userCommentEntities) {
        List<String> result = new ArrayList<>();

        for (UserCommentEntity entity : userCommentEntities) {
            result.add(entity.getComment() + "\n[" + entity.getUserName() + "]");
        }

        return result;
    }

    private ProductEntity getParentEntity() {
        return ProductEntity.create(getIntent().getStringExtra("id"));
    }
}
