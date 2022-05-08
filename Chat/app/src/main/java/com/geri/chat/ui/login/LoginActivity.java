package com.geri.chat.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.geri.chat.R;
import com.geri.chat.ui.UserMain.UserMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String SECRET_KEY = "secret";
    private FirebaseAuth mAuth;

    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();


        username = findViewById(R.id.loginUsername);
        password = findViewById(R.id.loginPassword);
    }

    @Override
    public void onStart() {
        super.onStart();
       FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseAuth.getInstance().signOut();
        }
    }



    public void login(View view) {
        if (username.getText().toString().equals("") || password.getText().toString().equals(""))
            Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
        else {
            createSpinner();
            mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(com.google.android.gms.tasks.Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        saveToken(getSharedPreferences("TOKEN", MODE_PRIVATE).getString("token", ""));

                        Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                        intent.putExtra("SECRET_KEY", SECRET_KEY);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    destroySpinner();
                }

                private void saveToken(String token) {
                    if(FirebaseAuth.getInstance().getCurrentUser() != null && token != null && !token.equals("")) {
                        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("token", token);
                    }
                }
            });
        }
    }
    private void createSpinner() {
        findViewById(R.id.loginProgress).setVisibility(View.VISIBLE);
    }

    private void destroySpinner() {
        findViewById(R.id.loginProgress).setVisibility(View.GONE);
    }



}