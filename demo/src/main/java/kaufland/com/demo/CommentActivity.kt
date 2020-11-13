package kaufland.com.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import kaufland.com.coachbasebinderapi.PersistenceException
import kaufland.com.demo.entity.ProductEntity
import kaufland.com.demo.entity.ProductEntity.Companion.create
import kaufland.com.demo.entity.UserCommentWrapper
import java.util.*

class CommentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val data = parentEntity.comments?.toMutableList()
        val adapter =  object :ArrayAdapter<String?>(this, R.layout.comment_item_view, R.id.txt_comment, map(data)) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.findViewById<View>(R.id.btn_delete).setOnClickListener { v: View? ->
                    data?.removeAt(position)
                    try {
                        val entity = parentEntity
                        entity.comments = data
                        entity.save()
                    } catch (e: PersistenceException) {
                        Log.e(TAG, "failed to save Entity", e)
                    }
                    recreate()
                }
                return view
            }
        }
        val listView = findViewById<ListView>(R.id.list)
        listView.adapter = adapter
        findViewById<View>(R.id.btn_post).setOnClickListener { v: View? ->
            val mComments = parentEntity.comments?.toMutableList()
            mComments?.add(UserCommentWrapper.create().builder().setComment((findViewById<View>(R.id.edit_text) as EditText).text.toString()).setUser("you").exit())
            try {
                val entity = parentEntity
                entity.comments = mComments
                entity.save()
                (findViewById<View>(R.id.edit_text) as EditText).setText("")
                recreate()
            } catch (e: PersistenceException) {
                Log.e(TAG, "failed to save", e)
            }
        }
    }

    private fun map(userCommentEntities: List<UserCommentWrapper>?): List<String> {
        val result: MutableList<String> = ArrayList()
        for (entity in userCommentEntities!!) {
            result.add("""
    ${entity.comment}
    [${entity.user}(${entity.age})]
    """.trimIndent())
        }
        return result
    }

    private val parentEntity: ProductEntity
        private get() = create(intent.getStringExtra("id"))

    companion object {
        private val TAG = CommentActivity::class.java.name
        @JvmStatic
        fun buildIntent(activity: MainActivity?, id: String?): Intent {
            val intent = Intent(activity, CommentActivity::class.java)
            intent.putExtra("id", id)
            return intent
        }
    }
}