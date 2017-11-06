package co.cropbit.sahathanahomecare.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.text.SimpleDateFormat

class SignUpActivity : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.login_genders, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender_spinner.adapter = spinnerAdapter
    }

    fun login(view: View) {
        var user = User()
        user.id = mAuth.currentUser!!.uid
        user.phoneNumber = mAuth.currentUser!!.phoneNumber!!
        user.displayName = login_name.text.toString()
        user.gender = gender_spinner.selectedItem.toString()
        user.dob = SimpleDateFormat("yyyy-mm-dd").parse("${yyyy.text.toString()}-${mm.text.toString()}-${dd.text.toString()}")
        user.signUp {
            mAuth.currentUser!!.updateProfile(
                    UserProfileChangeRequest.Builder()
                            .setDisplayName(login_name.text.toString())
                            .build()
            ).addOnCompleteListener {
                val intent = Intent(this, TreatmentRequestActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}
