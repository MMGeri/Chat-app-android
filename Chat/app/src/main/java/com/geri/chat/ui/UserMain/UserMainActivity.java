package com.geri.chat.ui.UserMain;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;


import com.geri.chat.R;

import com.geri.chat.alarms.AlarmReceiver;
import com.geri.chat.data.DAO;
import com.geri.chat.services.NotificationJobService;
import com.geri.chat.services.NotificationService;
import com.geri.chat.ui.UserMain.navigation.profile.ProfileActivity;
import com.geri.chat.ui.UserMain.navigation.settings.SettingsActivity;
import com.geri.chat.ui.convo.ConvoActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


public class UserMainActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private DrawerLayout mDrawer;

    private Toolbar toolbar;

    private AlarmManager mAlarmManager;
    private PendingIntent pendingIntent;

    private boolean loggingOut = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_draver);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        BottomNavigationView navView = findViewById(R.id.bottom_navigation_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(navView, navController);
        //Setup the drawer
        setupDrawer();
        
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();

        //Check if user is logged in
        FirebaseUser user = currentUser;
        if (user == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
        //TODO display Offline message
    }

    private void setAlarmManager() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("userId",currentUser.getUid());
        pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_IMMUTABLE);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,0,60000,pendingIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!loggingOut ) {
            setAlarmManager();
        }
    }





    @Override
    public void onResume() {
        super.onResume();
        mAlarmManager.cancel(pendingIntent);
        if(NotificationJobService.listenerRegistration != null)
        NotificationJobService.listenerRegistration.remove();
    }

    public void logout() {

        FirebaseAuth.getInstance().signOut();
        DAO.destroyInstance();

        mAlarmManager.cancel(pendingIntent);
        if(NotificationJobService.listenerRegistration != null)
        NotificationJobService.listenerRegistration.remove();

        loggingOut=true;
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void setupDrawer() {

        mDrawer = findViewById(R.id.drawer_layout);  //activity user main draver
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true); //hamburger
        drawerToggle.syncState();
        mDrawer.addDrawerListener(drawerToggle);


        NavigationView nvDrawer = findViewById(R.id.nav_view);
        setupDrawerContent(nvDrawer);

        //modify the UserEmail TextView in nav_header_main.xml
        TextView userEmail = nvDrawer.getHeaderView(0).findViewById(R.id.UserEmail);
        TextView userName = nvDrawer.getHeaderView(0).findViewById(R.id.UserName);
        userEmail.setText(currentUser.getEmail());
        userName.setText(currentUser.getDisplayName());
    }


    private void setupDrawerContent(NavigationView nvDrawer) {
        //ha menuitemre kattintunk, akkor a megfelelo activity-t inditjuk
        nvDrawer.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("uid", currentUser.getUid());
                intent.putExtra("username", currentUser.getDisplayName());
                intent.putExtra("email", currentUser.getEmail());
                startActivity(intent);
                break;
            case R.id.logout_button:
                logout();
                break;
        }
        mDrawer.closeDrawers();
    }

}

