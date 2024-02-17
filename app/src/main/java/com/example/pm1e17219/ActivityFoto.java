package com.example.pm1e17219;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityFoto extends AppCompatActivity {
    ImageView imgGuardada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vfoto);
        imgGuardada = (ImageView) findViewById(R.id.ImgGuardada);
    }

}