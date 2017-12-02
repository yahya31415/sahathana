package co.cropbit.sahathanahomecare.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by yahya on 01/08/17.
 */

class Hospital(val address: String, val displayName: String, val email: String, val location: Location, val phoneNumber: String, val photoURL: String, val id: String) {

    fun toMap(): Map<String, Object> {
        var result = HashMap<String, Any>()
        result.put("address", address)
        result.put("displayName", displayName)
        result.put("email", email)
        result.put("location", location.toMap())
        result.put("phoneNumber", phoneNumber)
        result.put("photoURL", photoURL)
        return result as Map<String, Object>
    }

    companion object {
        fun fromDoc(doc: DocumentSnapshot): Hospital? {
            if(doc.contains("address") && doc.contains("location") && doc.contains("displayName") && doc.contains("email") && doc.contains("phoneNumber") && doc.contains("photoURL")) {
                val locationMap = doc.get("location") as Map<String, Double>
                val hospital = Hospital(
                        doc.getString("address"),
                        doc.getString("displayName"),
                        doc.getString("email"),
                        Location(locationMap["lat"] ?: 0.0, locationMap["lng"] ?: 0.0),
                        doc.getString("phoneNumber"),
                        doc.getString("photoURL"),
                        doc.id)
                return hospital
            } else {
                return null
            }
        }

        fun fromId(id: String, cb: (Hospital) -> Unit) {
            FirebaseFirestore.getInstance().collection("hospitals").document(id).get().addOnSuccessListener { doc ->
                val hospital = Hospital.fromDoc(doc)
                if (hospital != null) cb(hospital)
            }
        }
        fun nearbyHospitals(cb: (ArrayList<Hospital>) -> Unit) {
            var result = ArrayList<Hospital>()
            FirebaseFirestore.getInstance().collection("hospitals").get().addOnSuccessListener { snapshot ->
                snapshot.forEach { doc ->
                    val hospital = Hospital.fromDoc(doc)
                    if (hospital != null) result.add(hospital)
                }
                cb(result)
            }
        }
    }

}
