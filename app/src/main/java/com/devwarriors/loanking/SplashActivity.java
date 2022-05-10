package com.devwarriors.loanking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.devwarriors.loanking.databinding.ActivitySplashBinding;

import java.util.Objects;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Objects.requireNonNull(getSupportActionBar()).hide();
        b=ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Thread thread= new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Intent intent=new Intent(SplashActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
        thread.start();
    }
}