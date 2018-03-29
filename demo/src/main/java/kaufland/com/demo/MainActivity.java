package kaufland.com.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.View;

import java.util.Map;

import kaufland.com.demo.entity.ProductEntity;

public class MainActivity extends AppCompatActivity {

    private ProductAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new ProductAdapter(this, getQuery().toLiveQuery());

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                ProductEntity mItem = (ProductEntity) mAdapter.getItem(position);
                startActivity(CommentActivity.buildIntent(MainActivity.this, mItem.getId()));
            }
        });
    }

    private Query getQuery() {
        Application application = (Application) getApplication();
        View view = application.getDatabase().getView("products");
        if (view.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String)document.get(ProductEntity.TYPE);
                    if (ProductEntity.DOC_TYPE.equals(type))
                        emitter.emit(document.get(ProductEntity.NAME), null);
                }
            };
            view.setMap(mapper, "1.0");
        }
        return view.createQuery();
    }
}
