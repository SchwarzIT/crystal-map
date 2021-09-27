package kaufland.com.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import kaufland.com.demo.CommentActivity.Companion.buildIntent
import kaufland.com.demo.entity.ProductEntity.Companion.findByType

class MainActivity : AppCompatActivity() {
    private var mAdapter: ProductAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAdapter = ProductAdapter(this)
        mAdapter!!.addAll(findByType())
        mAdapter!!.notifyDataSetChanged()
        val listView = findViewById<ListView>(R.id.list)
        listView.adapter = mAdapter
        listView.onItemClickListener = OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val mItem = mAdapter!!.getItem(position)
            startActivity(buildIntent(this@MainActivity, mItem!!.getId()))
        }
    }
}
