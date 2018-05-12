package co.cropbit.sahathanahomecare.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.Request
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.DateFormatSymbols
import java.util.*

class ChatActivity : AppCompatActivity() {

    var request: Request? = null
    var adapter: ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Request.fromId(intent.getStringExtra("requestId")) { r ->
            request = r

            title = r.hospital.displayName

            adapter = ChatAdapter()
            chats.adapter = adapter
            val layoutManager = LinearLayoutManager(this@ChatActivity)
            layoutManager.stackFromEnd = true
            chats.layoutManager = layoutManager
            adapter?.notifyDataSetChanged()


            Request.subscribe(request!!.id) { rr ->
                request = rr
                adapter?.notifyDataSetChanged()
                chats.scrollToPosition(request!!.chats.size - 1)
            }
        }
    }

    fun send(view: View) {
        request?.sendMessage(chat_msg_et.text.toString())
        chat_msg_et.text.clear()
    }

    inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var msg: TextView
            var author: TextView
            // var hospital: TextView

            init {
                msg = itemView.findViewById<View>(R.id.msg) as TextView
                author = itemView.findViewById<View>(R.id.author) as TextView
                // time = itemView.findViewById<View>(R.id.ar_treatment_time) as TextView
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val thisItemsView = LayoutInflater.from(this@ChatActivity).inflate(R.layout.chat_list_item, parent, false)
            return ViewHolder(thisItemsView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Set item views based on your views and data model
//            val cal = Calendar.getInstance()
//            cal.time = requests.get(position)?.datetime
//
//            val day = cal.get(Calendar.DAY_OF_MONTH)
//            val month = DateFormatSymbols().months[cal.get(Calendar.MONTH)]
//            val time = StringBuilder().append(if(cal.get(Calendar.HOUR) == 0) "12" else cal.get(Calendar.HOUR)).append(":").append(cal.get(Calendar.MINUTE)).append(" ").append(if (cal.get(Calendar.AM_PM) == 0) "AM" else "PM")

            holder.msg.text = request?.chats?.get(position)?.get("message")
            if (request?.chats?.get(position)?.get("author").equals(FirebaseAuth.getInstance().currentUser?.uid)) {
                holder.author.text = "You"
            } else {
                holder.author.text = request?.hospital?.displayName
            }
            // holder.day.text = day.toString()

//            holder.itemView.setOnClickListener {
//                val chatIntent = Intent(this@MainActivity, ChatActivity::class.java)
//                chatIntent.putExtra("requestId", requests.get(position)?.id)
//                startActivity(chatIntent)
//            }
        }

        override fun getItemCount(): Int {
            return request!!.chats.size
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
