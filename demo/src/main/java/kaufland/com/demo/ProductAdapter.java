package kaufland.com.demo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kaufland.com.demo.entity.ProductEntity;


public class ProductAdapter extends ArrayAdapter<ProductEntity> {

    public ProductAdapter(Context context) {
        super(context, 0);
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
        ImageView img = convertView.findViewById(R.id.image);

        img.setImageBitmap(BitmapFactory.decodeStream(list.getImage().getContentStream()));
        return convertView;
    }
}