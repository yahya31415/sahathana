package co.cropbit.sahathanahomecare.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import co.cropbit.sahathanahomecare.R;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private String email;
    private String password;
    private String name;
    private String phno;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        setContentView(R.layout.activity_sign_up);
        context = this;
    }

    public void signUp(View view) {
        email = ((EditText) findViewById(R.id.sign_up_email)).getText().toString();
        password = ((EditText) findViewById(R.id.sign_up_password)).getText().toString();
        name = ((EditText) findViewById(R.id.sign_up_name)).getText().toString();
        phno = ((EditText) findViewById(R.id.sign_up_phno)).getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                mDatabase.getReference("user").child(authResult.getUser().getUid()).child("name").setValue(name);
                mDatabase.getReference("user").child(authResult.getUser().getUid()).child("phno").setValue(phno);
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
