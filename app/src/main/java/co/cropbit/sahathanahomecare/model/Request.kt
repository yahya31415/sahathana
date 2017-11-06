package co.cropbit.sahathanahomecare.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by yahya on 02/08/17.
 */

class Request {

    var id = ""
    var uid = ""
    var location = Location(0.0,0.0)
    var datetime: Long = 0
    var status: Int = 0
    var type = ""
    var hospital = ""
    var approvedBy: String? = null

    fun toMap(): Map<String, Object> {
        var result = HashMap<String, Any>()
        result.put("uid", uid)
        result.put("location", location.toMap())
        result.put("datetime", datetime.toString())
        result.put("status", status.toString())
        result.put("type", type)
        result.put("hospital", hospital)
        if(approvedBy != null) result.put("approvedBy", approvedBy!!)
        return result as Map<String, Object>
    }

    fun push(cb: () -> Unit) {
        FirebaseFirestore.getInstance().collection("requests").add(toMap()).addOnSuccessListener {
            cb()
        }
    }

    fun statusString(): String? {
        when (status) {
            SENT -> return "Sent"
            PROCESSING -> return "Processing"
            APPROVED -> return "Approved"
        }
        return null
    }

    companion object {
        val SENT = 0
        val PROCESSING = 1
        val APPROVED = 2
        fun fromId(id: String, cb: (request: Request) -> Unit) {
            FirebaseFirestore.getInstance().collection("requests").document(id).get().addOnSuccessListener { doc ->
                val request = Request()
                request.id = id
                request.uid = doc.getString("uid")

                var l = doc.get("location") as Map<String, String>
                request.location = Location(l["lat"]!!.toDouble(), l["lng"]!!.toDouble())

                request.datetime = doc.getString("datetime").toLong()
                request.status = doc.getString("status").toInt()
                request.type = doc.getString("type")
                request.hospital = doc.getString("hospital")
                request.approvedBy = doc.getString("approvedBy")
                cb(request)
            }
        }

        fun get(uid: String, cb: (requests: ArrayList<Request>) -> Unit) {
            var requests = arrayListOf<Request>()
            FirebaseFirestore.getInstance().collection("requests").whereEqualTo("uid", uid).get().addOnSuccessListener { snapshot ->
                snapshot.forEach { doc ->
                    val request = Request()
                    request.uid = doc.getString("uid")

                    var l = doc.get("location") as Map<String, String>
                    request.location = Location(l["lat"]!!.toDouble(), l["lng"]!!.toDouble())

                    request.datetime = doc.getString("datetime").toLong()
                    request.status = doc.getString("status").toInt()
                    request.type = doc.getString("type")
                    request.hospital = doc.getString("hospital")
                    request.approvedBy = doc.getString("approvedBy")

                    requests.add(request)
                }
                cb(requests)
            }
        }
    }

    fun getApprovedStringAsync(cb: (String) -> Unit) {
        if(approvedBy == null) {
            cb("Waiting for approval")
        } else {
            Hospital.fromId(approvedBy!!, { hospital ->
                cb(hospital.displayName)
            })
        }
    }
}
