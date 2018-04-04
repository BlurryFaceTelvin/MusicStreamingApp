package com.example.blurryface.musicstreamingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    FirebaseAuth auth;
    EditText emailText,passwordText;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailText = findViewById(R.id.userText);
        passwordText = findViewById(R.id.passText);
        toolbar = findViewById(R.id.signUpToolbar);
        auth = FirebaseAuth.getInstance();
        //setSupportActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public void onRegister(View view){
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
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                welcomePage();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }
    public void onExit(View view){
        welcomePage();
    }
    public void welcomePage(){
        Intent intent = new Intent(SignUpActivity.this,WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}
