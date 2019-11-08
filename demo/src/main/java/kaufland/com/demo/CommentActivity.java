package kaufland.com.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kaufland.com.coachbasebinderapi.PersistenceException;
import kaufland.com.demo.entity.ProductEntity;
import kaufland.com.demo.entity.UserCommentWrapper;
import kaufland.com.demo.entity.UserCommentWrapper.Companion.*;

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
        final List<UserCommentWrapper> data = getParentEntity().getComments();

        mAdapter = new ArrayAdapter<String>(this, R.layout.comment_item_view, R.id.txt_comment, map(data)) {
            @NonNull
            @Override
            public android.view.View getView(final int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                view.findViewById(R.id.btn_delete).setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        data.remove(position);
                        try {
                            getParentEntity().builder().setComments(data).exit().save();
                        } catch (PersistenceException e) {
                            Log.e(TAG, "failed to save Entity", e);
                        }
                        recreate();
                    }
                });
                return view;
            }
        };

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        findViewById(R.id.btn_post).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                List<UserCommentWrapper> mComments = getParentEntity().getComments();
                mComments.add(UserCommentWrapper.create().builder().
                        setComment(((EditText) findViewById(R.id.edit_text)).getText().toString()).
                        setUser("you").exit());
                try {
                    getParentEntity().builder().setComments(mComments).exit().save();
                    ((EditText) findViewById(R.id.edit_text)).setText("");
                    recreate();
                } catch (PersistenceException e) {
                    Log.e(TAG, "failed to save", e);
                }
            }
        });
    }

    private List<String> map(List<UserCommentWrapper> userCommentEntities) {
        List<String> result = new ArrayList<>();

        for (UserCommentWrapper entity : userCommentEntities) {
            result.add(entity.getComment() + "\n[" + entity.getUser() + "(" + entity.getAge() + ")]");
        }

        return result;
    }

    private ProductEntity getParentEntity() {
        return ProductEntity.create(getIntent().getStringExtra("id"));
    }
}
