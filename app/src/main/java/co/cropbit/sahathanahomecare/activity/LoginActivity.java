package co.cropbit.sahathanahomecare.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import co.cropbit.sahathanahomecare.R;

public class LoginActivity extends AppCompatActivity {

    private String value;
    private PhoneAuthProvider phoneAuthProvider;
    private FirebaseAuth mAuth;
    private Context mContext;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private boolean OTPSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mContext = this;
        phoneAuthProvider = PhoneAuthProvider.getInstance();
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        value = ((EditText) findViewById(R.id.login_phno)).getText().toString();
        if(!OTPSent) {
            Toast.makeText(this, "Sending OTP", Toast.LENGTH_SHORT).show();
            phoneAuthProvider.verifyPhoneNumber(value, 60, TimeUnit.SECONDS, (Activity) mContext, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            FirebaseUser user = task.getResult().getUser();

                            if(user.getDisplayName() == null) {
                                Intent intent = new Intent(mContext, SignUpActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(mContext, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.v("Error", e.getMessage());
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId;
                    mResendToken = token;

                    OTPSent = true;

                    ((Button) findViewById(R.id.login_button)).setText("Verify OTP");
                    ((EditText) findViewById(R.id.login_phno)).setText("");
                }
            });
        } else {
            Toast.makeText(this, "Verifying OTP", Toast.LENGTH_SHORT).show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, value);
            mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user = task.getResult().getUser();

                    if(user.getDisplayName() == null) {
                        Intent intent = new Intent(mContext, SignUpActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public void signup(View view) {

    }
}
