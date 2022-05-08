package com.geri.chat.ui.UserMain.navigation.settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.geri.chat.R;

public class SettingsActivity extends AppCompatActivity {
    private boolean changes =false;
    private boolean isNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        isNightMode = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("nightMode", false);

        SwitchCompat switchCompat = findViewById(R.id.night_mode_switch);
        switchCompat.setChecked(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("nightMode", false));
    }

    public void toggleNightMode(View view) {
        isNightMode = !isNightMode;
        changes = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (changes) {
            getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("nightMode", isNightMode).apply();
            //TODO: change from shared preferences to database ?
            //TODO change theme
        }
    }

//    public void enableNotifications(View view) {
//        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);
//    }
//  //TODO: stuff
//    public void disableNotifications(View view) {
//        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);
//    }
}