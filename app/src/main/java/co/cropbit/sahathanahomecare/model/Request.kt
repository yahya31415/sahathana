package co.cropbit.sahathanahomecare.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log

/**
 * Created by yahya on 02/08/17.
 */

class Request {

    var uid = ""
    var location = Location()
    var datetime: Long? = null
    var status: Int = 0
    var key: String? = null
    var type = ""
    var isEmergency: Boolean = false
    var approvedBy: String? = null
    var approved: String? = "Waiting for approval"

    constructor() {

    }

    constructor(u: String, l: Location, dt: Long?, st: Int, tp: String, isE: Boolean, apb: String) {
        uid = u
        location = l
        datetime = dt
        status = st
        type = tp
        isEmergency = isE
        approvedBy = apb
    }

    fun statusString(): String? {
        when (status) {
            SENT -> return "Sent"
            PROCESSING -> return "Processing"
            APPROVED -> return "Approved"
        }
        return null
    }

    fun setApproved(ref: DatabaseReference, runnable: Runnable) {
        if (approvedBy == null) {
            runnable.run()
            return
        }
        var r = this
        Log.v("Sahathana Deep", ref.child(approvedBy!!).child("displayName").toString())
        ref.child(approvedBy!!).child("displayName").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.getValue(String::class.java)
                r.approved = name
                runnable.run()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    companion object {
        val SENT = 0
        val PROCESSING = 1
        val APPROVED = 2
    }
}
