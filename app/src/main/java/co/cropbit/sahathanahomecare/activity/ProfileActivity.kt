package co.cropbit.sahathanahomecare.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.Hospital
import co.cropbit.sahathanahomecare.model.Location
import co.cropbit.sahathanahomecare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.DateFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var user: User? = null

    var nearbyHospitals: ArrayList<Hospital>? = null
    val nearbyHospitalNames = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null

    var firstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbarProfile)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        User.fromId(mAuth.currentUser!!.uid, { u ->
            user = u
            render()
        })

        Hospital.nearbyHospitals { nh ->
            nearbyHospitals = nh
            nh.forEach({ hospital ->
                nearbyHospitalNames.add(hospital.displayName)
                adapter?.notifyDataSetChanged()
            })
        }

        adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nearbyHospitalNames)
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (!firstTime) {
                    user?.updateDefaultHospital(nearbyHospitals!!.get(nearbyHospitalNames.indexOf(spinner.selectedItem.toString())))
                } else {
                    firstTime = false
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun logOut(view: View?) {
        mAuth.signOut()
    }

    fun gotoHistory(view: View?) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    fun render() {
        profileName.text = user?.displayName
        profilePhno.text = user?.phoneNumber
        profileGender.text = user?.gender

        val cal = Calendar.getInstance()
        cal.time = user?.dob
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = DateFormatSymbols().months[cal.get(Calendar.MONTH)+1]
        val year = cal.get(Calendar.YEAR)
        profileDob.text = "$day $month $year"

        if(user?.defaultHospital != null) {
            spinner.setSelection(nearbyHospitalNames.indexOf(user?.defaultHospital!!.displayName))
        }
    }

}
