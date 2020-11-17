package com.justin.myapplication;

import android.os.Bundle;

import com.justin.annotationprocessor.ARouter;

import androidx.appcompat.app.AppCompatActivity;


@ARouter(path = "main/MainActivity2")
public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}