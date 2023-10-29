package com.example.tmdb;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tmdb.fragment.HomeFragment;
import com.example.tmdb.fragment.NotivigationFragment;
import com.example.tmdb.fragment.ProfileFragment;
import com.example.tmdb.fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottom_navigation;
    Fragment selectedfragment ;
    ImageView chat_icon;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);


       Bundle intent = getIntent().getExtras();
       if (intent != null){
           String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
           editor.putString("profileid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                   new ProfileFragment()).commit();
        } else {
           getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                   new HomeFragment()).commit();
   }
        chat_icon=findViewById(R.id.chat_icon);
        chat_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Chat.class);
                startActivity(intent);
            }
        });


    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        selectedfragment = new HomeFragment();
                    } else if (itemId == R.id.nav_search) {
                        selectedfragment = new SearchFragment();
                    } else if (itemId == R.id.nav_add) {
                       // selectedfragment=new PostDetailFragment();
                        selectedfragment = null;
                        Intent intent=new Intent(MainActivity.this, PostActivity.class);
                        startActivity(intent);

                    } else if (itemId == R.id.nav_heart) {
                        selectedfragment = new NotivigationFragment();
                    } else if (itemId == R.id.nav_profile) {
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selectedfragment = new ProfileFragment();
                    }
                    if (selectedfragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedfragment).commit();

                    }


                    return true;
                }
            };
}