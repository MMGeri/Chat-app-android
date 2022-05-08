package com.geri.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import com.geri.chat.data.DAO;
import com.geri.chat.ui.UserMain.UserMainActivity;
import com.geri.chat.ui.login.LoginActivity;
import com.geri.chat.ui.register.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String SECRET_KEY = "secret";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, UserMainActivity.class);
            intent.putExtra("SECRET_KEY", SECRET_KEY);
            Log.i("KozosDebug", "Auto log in " + user.getEmail() +" "+ user.getDisplayName());
            startActivity(intent);
        }
    }



    @Override
    protected void onRestart() {
        super.onRestart();

    }

    public void login(View view) {
        if(checkForInternet())
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void register(View view) {
        if(checkForInternet())
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private boolean checkForInternet() {
        //TODO display no internet message
        return true;
    }


}