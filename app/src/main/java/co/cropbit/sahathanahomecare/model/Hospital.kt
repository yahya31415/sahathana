package co.cropbit.sahathanahomecare.model

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by yahya on 01/08/17.
 */

class Hospital {

    var address = ""
    var displayName = ""
    var email = ""
    var location = Location(0.0,0.0)
    var phoneNumber = ""
    var photoURL = ""
    var id = ""

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
        fun fromId(id: String, cb: (Hospital) -> Unit) {
            FirebaseFirestore.getInstance().collection("hospitals").document(id).get().addOnSuccessListener { doc ->
                if(doc.contains("address") && doc.contains("location") && doc.contains("displayName") && doc.contains("email") && doc.contains("phoneNumber") && doc.contains("photoURL")) {
                    val hospital = Hospital()
                    hospital.address = doc.getString("address")

                    val locationMap = doc.get("location") as Map<String, Double>
                    hospital.location = Location(locationMap.get("lat")!!, locationMap.get("lng")!!)

                    hospital.displayName = doc.getString("displayName")
                    hospital.email = doc.getString("email")
                    hospital.phoneNumber = doc.getString("phoneNumber")
                    hospital.photoURL = doc.getString("photoURL")

                    hospital.id = doc.id
                    cb(hospital)
                }
            }
        }
        fun nearbyHospitals(cb: (ArrayList<Hospital>) -> Unit) {
            var result = ArrayList<Hospital>()
            FirebaseFirestore.getInstance().collection("hospitals").get().addOnSuccessListener { snapshot ->
                snapshot.forEach { doc ->
                    val hospital = Hospital()
                    hospital.address = doc.getString("address")

                    val locationMap = doc.get("location") as Map<String, Double>
                    hospital.location = Location(locationMap.get("lat")!!, locationMap.get("lng")!!)

                    hospital.displayName = doc.getString("displayName")
                    hospital.email = doc.getString("email")
                    hospital.phoneNumber = doc.getString("phoneNumber")
                    hospital.photoURL = doc.getString("photoURL")

                    hospital.id = doc.id
                    result.add(hospital)
                }
                cb(result)
            }
        }
    }

}
