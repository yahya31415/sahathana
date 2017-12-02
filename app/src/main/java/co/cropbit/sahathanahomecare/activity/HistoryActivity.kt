package co.cropbit.sahathanahomecare.activity

import android.content.Intent
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
import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.Hospital
import co.cropbit.sahathanahomecare.model.Location
import co.cropbit.sahathanahomecare.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_history.*
import java.text.DateFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity() {

    var requests = ArrayList<Request>()
    val mAuth = FirebaseAuth.getInstance()
    val adapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbarHistory)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        Request.get(mAuth.currentUser!!.uid, { r ->
            requests = r
            adapter.notifyDataSetChanged()
        })
        history.adapter = adapter
        history.layoutManager = LinearLayoutManager(this)
    }

    inner class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var type: TextView
            var hospital: TextView
            var approved: TextView
            var day: TextView
            var month: TextView
            var time: TextView

            init {
                type = itemView.findViewById<View>(R.id.treatment_type) as TextView
                hospital = itemView.findViewById<View>(R.id.treatment_hospital) as TextView
                approved = itemView.findViewById<View>(R.id.treatment_approved) as TextView
                day = itemView.findViewById<View>(R.id.treatment_day) as TextView
                month = itemView.findViewById<View>(R.id.treatment_month) as TextView
                time = itemView.findViewById<View>(R.id.treatment_time) as TextView
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val thisItemsView = LayoutInflater.from(this@HistoryActivity).inflate(R.layout.history_list_item, parent, false)
            return ViewHolder(thisItemsView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Set item views based on your views and data model
            val cal = Calendar.getInstance()
            cal.time = requests.get(position)?.datetime

            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = DateFormatSymbols().months[cal.get(Calendar.MONTH)]
            val time = StringBuilder().append(if(cal.get(Calendar.HOUR) == 0) "12" else cal.get(Calendar.HOUR)).append(":").append(cal.get(Calendar.MINUTE)).append(" ").append(if (cal.get(Calendar.AM_PM) == 0) "AM" else "PM")

            holder.type.text = requests.get(position)?.type
            holder.day.text = day.toString()
            holder.month.text = month.toString()
            holder.time.text = time.toString()
            holder.hospital.text = requests.get(position)?.hospital.displayName
            holder.approved.text = if (requests.get(position)?.status == 1) "Approved" else "Waiting for response"
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
