package kaufland.com.demo

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import kaufland.com.demo.entity.ProductEntity

class ProductAdapter(context: Context) : ArrayAdapter<ProductEntity>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.product_item, null)
        }

        convertView!!.tag = position
        val list = getItem(position)
        val text = convertView.findViewById<TextView>(R.id.text)
        text.text = list!!.name
        val img = convertView.findViewById<ImageView>(R.id.image)

        img.setImageBitmap(BitmapFactory.decodeStream(list.image!!.contentStream))
        return convertView
    }
}
