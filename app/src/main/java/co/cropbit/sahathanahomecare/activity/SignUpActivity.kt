package co.cropbit.sahathanahomecare.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.text.SimpleDateFormat

class SignUpActivity : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    var spinnerAdapter: ArrayAdapter<CharSequence>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.login_genders, android.R.layout.simple_spinner_item)
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender_spinner.adapter = spinnerAdapter
    }

    fun login(view: View) {
        var name = login_name.text.toString()
        var gender = gender_spinner.selectedItem.toString()
        var year = yyyy.text.toString()
        var month = mm.text.toString()
        var day = dd.text.toString()

        if(name.isEmpty() || gender.isEmpty() || year.isEmpty() || month.isEmpty() || day.isEmpty()) {
            Toast.makeText(this, "Error: Blank Fields", Toast.LENGTH_SHORT).show()
            return
        }

        var user = User(mAuth.currentUser!!.uid, name, gender, SimpleDateFormat("yyyy-mm-dd").parse("$year-$month-$day"), mAuth.currentUser!!.phoneNumber!!)

        user.signUp {
            val intent = Intent(this, TreatmentRequestActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
