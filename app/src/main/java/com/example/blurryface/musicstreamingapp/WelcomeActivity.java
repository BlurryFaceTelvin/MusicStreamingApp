package com.example.blurryface.musicstreamingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    EditText emailText,passwordText;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        emailText = findViewById(R.id.usertext);
        passwordText = findViewById(R.id.passtext);
        //firebase authentication initialise
        firebaseAuth = FirebaseAuth.getInstance();

    }
    public void onLogIn(View view){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        if(TextUtils.isEmpty(email)){
            emailText.setError("Email field is required");
            return;
        }
        if(TextUtils.isEmpty(password)){
            passwordText.setError("Password field is required");
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //makes sure when you press back button you cant go back to LogInActivity
                Intent intent = new Intent(WelcomeActivity.this,MusicListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WelcomeActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });

    }
    public void onSignUp(View view){
        Intent in = new Intent(WelcomeActivity.this,SignUpActivity.class);
        startActivity(in);
    }
}
