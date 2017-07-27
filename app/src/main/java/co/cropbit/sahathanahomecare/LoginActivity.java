package co.cropbit.sahathanahomecare;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private String email;
    private String password;
    private FirebaseAuth mAuth;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mContext = this;
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        email = ((EditText) findViewById(R.id.login_email)).getText().toString();
        password = ((EditText) findViewById(R.id.login_password)).getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                mContext.stopService(getIntent());
            }
        });
    }
}
