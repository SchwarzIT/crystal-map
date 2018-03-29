package kaufland.com.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;

import kaufland.com.demo.entity.ProductEntity;
import kaufland.com.demo.util.LiveQueryAdapter;

public class ProductAdapter extends LiveQueryAdapter<ProductEntity> {

    public ProductAdapter(Context context, LiveQuery query) {
        super(context, query);
    }

    @Override
    protected ProductEntity docToEntity(Document doc) {
        return ProductEntity.create(doc.getId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.product_item, null);
        }

        convertView.setTag(position);
        final ProductEntity list = (ProductEntity) getItem(position);
        TextView text = convertView.findViewById(R.id.text);
        text.setText(list.getName());
        return convertView;
    }
}