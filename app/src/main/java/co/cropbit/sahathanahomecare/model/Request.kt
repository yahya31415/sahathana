package co.cropbit.sahathanahomecare.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by yahya on 02/08/17.
 */

class Request(val id: String, val uid: String, val location: Location, val datetime: Date, val status: Int, val type: String, val hospital: Hospital, var chats: ArrayList<HashMap<String, String>> = arrayListOf()) {

    fun toMap(): Map<String, Object> {
        var result = HashMap<String, Any>()
        result.put("uid", uid)
        result.put("location", location.toMap())
        result.put("datetime", datetime.time.toString())
        result.put("status", status.toString())
        result.put("type", type)
        result.put("hospital", hospital.id)
        result.put("chats", chats)
        return result as Map<String, Object>
    }

    fun push(cb: () -> Unit) {
        FirebaseFirestore.getInstance().collection("requests").add(toMap()).addOnSuccessListener {
            cb()
        }
    }

    fun sendMessage(m: String) {
        var msg = hashMapOf<String, String>()
        msg.put("author", FirebaseAuth.getInstance().currentUser!!.uid)
        msg.put("message", m)
        chats.add(msg)
        FirebaseFirestore.getInstance().collection("requests").document(id).update("chats", chats)
    }

    companion object {
        fun fromDoc(doc: DocumentSnapshot, cb: (Request) -> Unit) {
            if (doc.contains("uid") && doc.contains("location") && doc.contains("datetime") && doc.contains("status") && doc.contains("type") && doc.contains("hospital")) {
                val locationMap = doc.get("location") as Map<String, String>
                Hospital.fromId(doc.getString("hospital")) { hospital ->
                    cb(Request(doc.id,
                            doc.getString("uid"),
                            Location(locationMap["lat"]?.toDouble() ?: 0.0, locationMap["lng"]?.toDouble() ?: 0.0),
                            Date(doc.getString("datetime").toLong()),
                            doc.getString("status").toInt(),
                            doc.getString("type"),
                            hospital,
                            doc.get("chats") as ArrayList<HashMap<String, String>>))
                }
            }
        }
        fun fromId(id: String, cb: (request: Request) -> Unit) {
            FirebaseFirestore.getInstance().collection("requests").document(id).get().addOnSuccessListener { doc ->
                Request.fromDoc(doc, cb)
            }
        }

        fun get(uid: String, cb: (requests: ArrayList<Request>) -> Unit) {
            var requests = arrayListOf<Request>()
            FirebaseFirestore.getInstance().collection("requests").whereEqualTo("uid", uid)
                    .addSnapshotListener { snapshot, firebaseFirestoreException ->
                        if (snapshot.size() == 0) cb(requests)
                        snapshot.forEach { doc ->
                            Request.fromDoc(doc, { request ->
                                requests.add(request)
                                if(snapshot.size() == requests.size) {
                                    cb(requests)
                                }
                            })
                        }
                    }
        }

        fun subscribe(id: String, cb: (request: Request) -> Unit) {
            FirebaseFirestore.getInstance().collection("requests").document(id).addSnapshotListener { snapshot, exception ->
                Request.fromDoc(snapshot) { r ->
                    cb(r)
                }
            }
        }
    }
}
