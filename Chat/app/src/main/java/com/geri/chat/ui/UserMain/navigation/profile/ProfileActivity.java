package com.geri.chat.ui.UserMain.navigation.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.geri.chat.R;

public class ProfileActivity extends AppCompatActivity {
    private TextView uid;
    private TextView username;
    private TextView email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uid= findViewById(R.id.id_value);
        username= findViewById(R.id.username_value);
        email= findViewById(R.id.email_value);

        uid.setText(getIntent().getStringExtra("uid"));
        username.setText(getIntent().getStringExtra("username"));
        email.setText(getIntent().getStringExtra("email"));
    }

    public void toggle_id(View view) {
        //toggle visibility of id
        if(uid.getVisibility()==View.VISIBLE){
            uid.setVisibility(View.INVISIBLE);
            return;
        }
        uid.setVisibility(View.VISIBLE);
    }
}