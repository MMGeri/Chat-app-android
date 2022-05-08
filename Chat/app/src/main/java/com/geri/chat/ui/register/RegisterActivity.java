package com.geri.chat.ui.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.geri.chat.MainActivity;
import com.geri.chat.R;
import com.geri.chat.data.console;
import com.geri.chat.data.DAO;
import com.geri.chat.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDateTime;


public class RegisterActivity extends AppCompatActivity {
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private FirebaseAuth mAuth;

    EditText username;
    EditText password;
    EditText confirmPassword;
    EditText email;
    Button registerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        email = findViewById(R.id.email);
        registerButton = findViewById(R.id.registerButton);


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isEmailCorrect(email.getText().toString())) {
                    registerFormState.setValue(new RegisterFormState(null, null, null, R.string.invalid_email, false));
                    return;
                }
                if (!isPasswordCorrect(password.getText().toString())) {
                    registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password, null, null, false));
                    return;
                }
                if (!isConfirmPasswordCorrect(password.getText().toString(), confirmPassword.getText().toString())) {
                    registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_confirm_password, null, false));
                    return;
                }
                registerFormState.setValue(new RegisterFormState(true));

            }
        };

        password.addTextChangedListener(afterTextChangedListener);
        confirmPassword.addTextChangedListener(afterTextChangedListener);
        email.addTextChangedListener(afterTextChangedListener);

        registerFormState.observe(this, new Observer<RegisterFormState>() {
            @Override
            public void onChanged(RegisterFormState registerFormState) {
                if (registerFormState == null) {
                    return;
                }
                registerButton.setEnabled(registerFormState.isDataValid());

                if (registerFormState.getUsernameError() != null) {
                    username.setError(getString(registerFormState.getUsernameError()));
                }
                if (registerFormState.getPasswordError() != null) {
                    password.setError(getString(registerFormState.getPasswordError()));
                }
                if (registerFormState.getConfirmPasswordError() != null) {
                    confirmPassword.setError(getString(registerFormState.getConfirmPasswordError()));
                }
                if (registerFormState.getEmailError() != null) {
                    email.setError(getString(registerFormState.getEmailError()));
                }

            }
        });

    }

    public void register(View view) {
        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();
        String emailString = email.getText().toString();

        mAuth.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    //set current user DisplayName
                    FirebaseUser user = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(usernameString)
                            .build();
                    user.updateProfile(profileUpdates);

                    DAO.getInstance().addUserToFirestore(new User(user.getUid(), usernameString, "", LocalDateTime.now()), RegisterActivity.this)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RegisterActivity.this, "Failed to add user to firestore", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private boolean isConfirmPasswordCorrect(String toString, String toString1) {
        return toString.equals(toString1);
    }

    private boolean isPasswordCorrect(String toString) {
        return toString.length() >= 6;
    }

    private boolean isEmailCorrect(String toString) {
        return toString.contains("@");
    }
}