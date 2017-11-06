package co.cropbit.sahathanahomecare.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by yahya on 01/08/17.
 */

class User {

    var id = ""
    var displayName = ""
    var gender = ""
    var dob = Date()
    var phoneNumber = ""
    var defaultHospital: Hospital? = null

    fun signUp(cb: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(id).set(toMap() as Map<String, Object>).addOnSuccessListener {
            cb()
        }
    }

    fun updateDefaultHospital(hospital: Hospital) {
        var map = HashMap<String, Any>()
        map.put("defaultHospital", hospital.id)
        FirebaseFirestore.getInstance().collection("users").document(id).set(map as Map<String, Any>, SetOptions.merge()).addOnSuccessListener {
            defaultHospital = hospital
        }
    }

    fun toMap(): Map<String, Object> {
        var result = HashMap<String, Any>()
        result.put("displayName", displayName)
        result.put("gender", gender)
        result.put("dob", dob.time.toString())
        result.put("phoneNumber", phoneNumber)
        if (defaultHospital != null) result.put("defaultHospital", defaultHospital!!.toMap())
        return result as Map<String, Object>
    }

    companion object {
        fun fromId(id: String, cb: (user: User) -> Unit) {
            FirebaseFirestore.getInstance().collection("users").document(id).get().addOnSuccessListener { data ->
                if(data.exists()) {
                    val user = User()
                    user.id = id
                    user.displayName = data.getString("displayName")
                    user.phoneNumber = data.getString("phoneNumber")
                    user.dob = Date(data.getString("dob").toLong())
                    user.gender = data.getString("gender")
                    if (data.contains("defaultHospital")) Hospital.fromId(data.getString("defaultHospital"), { hospital ->
                        user.defaultHospital = hospital
                        cb(user)
                    }) else cb(user)
                } else {
                    FirebaseAuth.getInstance().signOut()
                }
            }
        }
    }

}
