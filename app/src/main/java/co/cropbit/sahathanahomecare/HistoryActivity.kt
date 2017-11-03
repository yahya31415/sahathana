package co.cropbit.sahathanahomecare

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.cropbit.sahathanahomecare.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_history.*
import java.text.DateFormatSymbols
import java.util.*

class HistoryActivity : AppCompatActivity() {

    val historyActivityContext = this
    val requests = arrayListOf<Request>()

    val mAuth = FirebaseAuth.getInstance()
    val mDatabase = FirebaseDatabase.getInstance()

    internal val adapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbarHistory)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mDatabase.getReference("requests").child(mAuth.currentUser?.uid).ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val request = dataSnapshot.getValue(Request::class.java)
                request?.key = dataSnapshot.key
                request?.setApproved(mDatabase!!.getReference("users"), Runnable {
                    requests.add(request)
                    Log.v("Sahathana",  if (request.approved != null) request.approved else "UNSET")
                    adapter.notifyDataSetChanged()
                })
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val request = dataSnapshot.getValue(Request::class.java)
                request!!.key = dataSnapshot.key

                requests.forEach { req ->
                    if(req.key == request.key) {
                        requests.remove(req)
                    }
                }

                requests.add(request)
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                for (i in requests.indices) {
                    if (requests[i].key == dataSnapshot.key) {
                        requests.removeAt(i)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {
                requests.clear()
                adapter.notifyDataSetChanged()
            }
        })
        history.adapter = adapter
        history.layoutManager = LinearLayoutManager(this)
    }

    internal inner class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var type: TextView
            var hospital: TextView
            var day: TextView
            var month: TextView
            var time: TextView

            init {
                type = itemView.findViewById<View>(R.id.treatment_type) as TextView
                hospital = itemView.findViewById<View>(R.id.treatment_hospital) as TextView
                day = itemView.findViewById<View>(R.id.treatment_day) as TextView
                month = itemView.findViewById<View>(R.id.treatment_month) as TextView
                time = itemView.findViewById<View>(R.id.treatment_time) as TextView
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {
            val thisItemsView = LayoutInflater.from(historyActivityContext).inflate(R.layout.history_list_item, parent, false)
            return ViewHolder(thisItemsView)
        }

        override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
            // Set item views based on your views and data model
            val cal = Calendar.getInstance()
            cal.time = Date(requests.get(position).datetime!!)

            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = DateFormatSymbols().months[cal.get(Calendar.MONTH)]
            val time = StringBuilder().append(cal.get(Calendar.HOUR)).append(":").append(cal.get(Calendar.MINUTE)).append(" ").append(if (cal.get(Calendar.AM_PM) == 0) "AM" else "PM")

            holder.type.text = requests.get(position).type
            holder.day.text = day.toString()
            holder.month.text = month.toString()
            holder.time.text = time.toString()
            holder.hospital.text = requests.get(position).approved
        }

        override fun getItemCount(): Int {
            return requests.size
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
