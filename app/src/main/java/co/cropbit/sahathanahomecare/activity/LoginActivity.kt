package co.cropbit.sahathanahomecare.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import co.cropbit.sahathanahomecare.R

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var countryCodeAdapter: ArrayAdapter<CharSequence>? = null

    private var phoneAuthProvider: PhoneAuthProvider? = null
    private var mAuth: FirebaseAuth? = null
    private var mContext: Context? = null

    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var OTPSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mContext = this
        phoneAuthProvider = PhoneAuthProvider.getInstance()
        setContentView(R.layout.activity_login)
        countryCodeAdapter = ArrayAdapter.createFromResource(this, R.array.country_codes, android.R.layout.simple_spinner_item)
        countryCodeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        country_code?.adapter = countryCodeAdapter
    }

    fun login(view: View) {
        var text = country_code.selectedItem.toString() + login_phno.text.toString()
        if (!OTPSent) {
            Toast.makeText(this, "Sending OTP", Toast.LENGTH_SHORT).show()
            phoneAuthProvider!!.verifyPhoneNumber(text, 60, TimeUnit.SECONDS, (mContext as Activity?)!!, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    mAuth!!.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
                        val user = task.result.user

                        if (user.displayName == null) {
                            val intent = Intent(mContext, SignUpActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(mContext, TreatmentRequestActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.v("Error", e.message)
                }

                override fun onCodeSent(verificationId: String?,
                                        token: PhoneAuthProvider.ForceResendingToken?) {

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId
                    mResendToken = token

                    OTPSent = true

                    login_phno.hint = "One Time Password"
                    login_phno.text.clear()
                    login_button.text = getString(R.string.login_button_otp)
                    country_code.visibility = View.INVISIBLE
                }
            })
        } else {
            Toast.makeText(this, "Verifying OTP", Toast.LENGTH_SHORT).show()
            val credential = PhoneAuthProvider.getCredential(mVerificationId!!, login_phno.text!!.toString())
            mAuth!!.signInWithCredential(credential).addOnCompleteListener { task ->
                val user = task.result.user

                if (user.displayName == null) {
                    val intent = Intent(mContext, SignUpActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(mContext, TreatmentRequestActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }
}
