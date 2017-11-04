package co.cropbit.sahathanahomecare

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import co.cropbit.sahathanahomecare.model.Hospital
import co.cropbit.sahathanahomecare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbarProfile)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDatabase.getReference("users").child(mAuth.currentUser?.uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                user = p0?.getValue(User::class.java)
                render(user)
            }
            override fun onCancelled(p0: DatabaseError?) {
                user = null
                render(user)
            }
        })
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

    fun render(user: User?) {
        profileName.setText(user?.name)
        profilePhno.setText(user?.phoneNumber)
        getHospitals()
    }

    fun getHospitals() {
        mDatabase.getReference("hospitals").addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                var hospital = p0?.getValue(Hospital::class.java)
                Log.v("Sahathana", hospital?.address)
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCancelled(p0: DatabaseError?) {
                Log.v("Sahathana", p0?.message)
            }
        })
    }
}
