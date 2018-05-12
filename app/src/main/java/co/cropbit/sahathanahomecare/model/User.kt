package co.cropbit.sahathanahomecare.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by yahya on 01/08/17.
 */

class User {

    var id: String
    var displayName: String
    var gender: String
    var dob: Date
    var phoneNumber: String
    var androidToken: String? = null
    var defaultHospital: Hospital? = null

    constructor(uid: String, name: String, sex: String, date: Date, phone: String) {
        id = uid
        displayName = name
        gender = sex
        dob = date
        phoneNumber = phone
    }

    fun signUp(cb: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(id).set(toMap() as Map<String, Object>).addOnSuccessListener {
            FirebaseAuth.getInstance().currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
            )?.addOnCompleteListener {
                cb()
            }
        }
    }

    fun updateDefaultHospital(hospital: Hospital) {
        var map = HashMap<String, Any>()
        map.put("defaultHospital", hospital.id)
        FirebaseFirestore.getInstance().collection("users").document(id).set(map as Map<String, Any>, SetOptions.merge()).addOnSuccessListener {
            defaultHospital = hospital
        }
    }

    fun addToken(token: String) {
        var map = HashMap<String, Any>()
        map.put("androidToken", token)
        FirebaseFirestore.getInstance().collection("users").document(id).set(map as Map<String, Any>, SetOptions.merge())
                .addOnSuccessListener {
                    androidToken = token
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
                if(data.exists() && data.contains("displayName") && data.contains("gender") && data.contains("dob") && data.contains("phoneNumber")) {
                    val user = User(id, data.getString("displayName"), data.getString("gender"), Date(data.getString("dob").toLong()), data.getString("phoneNumber"))
                    if (data.contains("defaultHospital")) Hospital.fromId(data.getString("defaultHospital"), { hospital ->
                        user.defaultHospital = hospital
                        cb(user)
                    }) else cb(user)
                }
            }
        }
    }

}
