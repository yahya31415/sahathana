package co.cropbit.sahathanahomecare.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.Request
import co.cropbit.sahathanahomecare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DateFormatSymbols
import java.util.*

class MainActivity : AppCompatActivity() {

    var requests = ArrayList<Request>()
    val mAuth = FirebaseAuth.getInstance()
    val adapter = ActiveRequestAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth.addAuthStateListener { authState ->
            if (authState.currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        if(mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        } else {
            User.fromId(mAuth.currentUser!!.uid) { user ->
                welcome_text.text = "Welcome " + user.displayName
                user.addToken(FirebaseInstanceId.getInstance().token ?: "")
            }
        }

        Request.get(mAuth.currentUser!!.uid, { r ->
            requests = r.filter { request -> request.status < 2 } as ArrayList<Request>
            adapter.notifyDataSetChanged()

            if (requests.size == 0) {
                activeRequests.visibility = View.GONE
                noActiveRequestsMessage.visibility = View.VISIBLE
            } else {
                activeRequests.visibility = View.VISIBLE
                noActiveRequestsMessage.visibility = View.GONE
            }
        })
        activeRequests.adapter = adapter
        activeRequests.layoutManager = LinearLayoutManager(this)

    }

    fun goToProfile(view: View) {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    fun goToPastRequests(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    fun goToNewRequest(view: View) {
        val intent = Intent(this, TreatmentRequestActivity::class.java)
        startActivity(intent)
    }

    inner class ActiveRequestAdapter : RecyclerView.Adapter<ActiveRequestAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var type: TextView
            var hospital: TextView
            var approved: TextView
            var day: TextView
            var month: TextView
            var time: TextView

            init {
                type = itemView.findViewById<View>(R.id.ar_treatment_type) as TextView
                hospital = itemView.findViewById<View>(R.id.ar_treatment_hospital) as TextView
                approved = itemView.findViewById<View>(R.id.ar_treatment_approved) as TextView
                day = itemView.findViewById<View>(R.id.ar_treatment_day) as TextView
                month = itemView.findViewById<View>(R.id.ar_treatment_month) as TextView
                time = itemView.findViewById<View>(R.id.ar_treatment_time) as TextView
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val thisItemsView = LayoutInflater.from(this@MainActivity).inflate(R.layout.ar_list_item, parent, false)
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

            holder.itemView.setOnClickListener {
                val chatIntent = Intent(this@MainActivity, ChatActivity::class.java)
                chatIntent.putExtra("requestId", requests.get(position)?.id)
                startActivity(chatIntent)
            }
        }

        override fun getItemCount(): Int {
            return requests.size
        }
    }

}
