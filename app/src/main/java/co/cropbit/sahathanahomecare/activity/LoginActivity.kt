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
import com.vexigon.libraries.onboarding.obj.Page
import com.vexigon.libraries.onboarding.ui.models.TopUserBenefitsModel

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var countryCodeAdapter: ArrayAdapter<CharSequence>? = null

    val phoneAuthProvider = PhoneAuthProvider.getInstance()
    val mAuth = FirebaseAuth.getInstance()
    private var mContext: Context? = null

    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var OTPSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TopUserBenefitsModel(this)
                .setupSlides(
                        Page(resources.getStringArray(R.array.onboarding_titles)[0], resources.getStringArray(R.array.onboarding_contents)[0], R.drawable.home_image),
                        Page(resources.getStringArray(R.array.onboarding_titles)[1], resources.getStringArray(R.array.onboarding_contents)[1], R.drawable.hospital_image),
                        Page(resources.getStringArray(R.array.onboarding_titles)[2], resources.getStringArray(R.array.onboarding_contents)[2], R.drawable.ambulance_image)
                        )
                .launch()
        mContext = this
        setContentView(R.layout.activity_login)
        countryCodeAdapter = ArrayAdapter.createFromResource(this, R.array.country_codes, R.layout.spinner_item_black)
        countryCodeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        country_code?.adapter = countryCodeAdapter
    }

    fun login(view: View) {
        var text = login_phno.text.toString()
        if(text.isEmpty() || country_code.selectedItem.toString().isEmpty()) {
            Toast.makeText(this, "Error: Blank Field", Toast.LENGTH_SHORT).show()
            return
        }
        if (!OTPSent) {
            Toast.makeText(this, "Sending OTP", Toast.LENGTH_SHORT).show()
            phoneAuthProvider.verifyPhoneNumber(country_code.selectedItem.toString() + text, 60, TimeUnit.SECONDS, this, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            val user = task.result.user

                            if (user.displayName == null) {
                                val intent = Intent(mContext, SignUpActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                val intent = Intent(mContext, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        } else {
                            val exception = task.exception
                            Toast.makeText(this@LoginActivity, exception?.message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LoginActivity, e.message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
                    login_phno.text.clear()
                }

                override fun onCodeSent(verificationId: String?,
                                        token: PhoneAuthProvider.ForceResendingToken?) {

                    Toast.makeText(this@LoginActivity, "OTP Sent", Toast.LENGTH_SHORT).show()

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
            val credential = PhoneAuthProvider.getCredential(mVerificationId!!, text)
            mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val user = task.result.user

                    if (user.displayName == null) {
                        val intent = Intent(mContext, SignUpActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val intent = Intent(mContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    val exception = task.exception
                    Toast.makeText(this@LoginActivity, exception?.message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
