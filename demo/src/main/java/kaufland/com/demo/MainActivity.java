package kaufland.com.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.SelectResult;

import kaufland.com.demo.entity.ProductEntity;

public class MainActivity extends AppCompatActivity {

    private ProductAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new ProductAdapter(this);

        Query query = getQuery();
        query.addChangeListener(new QueryChangeListener() {
            @Override
            public void changed(QueryChange change) {
                mAdapter.setNotifyOnChange(false);
                mAdapter.clear();
                if (change != null) {
                    for (Result item : change.getResults()) {
//                    mAdapter.add(ProductEntity.create(item.getDictionary(Application.DB).getString(ProductEntity._ID)));
                        mAdapter.add(new ProductEntity(item.getDictionary(Application.DB).toMap()));
                    }
                }
                mAdapter.setNotifyOnChange(true);
                mAdapter.notifyDataSetChanged();
            }
        });

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                ProductEntity mItem = mAdapter.getItem(position);
                startActivity(CommentActivity.buildIntent(MainActivity.this, mItem.getId()));
            }
        });
    }

    private Query getQuery() {

        Application application = (Application) getApplication();
        Database database = application.getDatabase();

        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type").equalTo(Expression.string(ProductEntity.DOC_TYPE)));

        return query;
    }
}
