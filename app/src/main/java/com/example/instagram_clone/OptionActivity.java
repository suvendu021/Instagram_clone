package com.example.instagram_clone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class OptionActivity extends AppCompatActivity {
    private TextView setting;
    private TextView logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        setting=findViewById(R.id.setting);
        logOut=findViewById(R.id.log_out);

        Toolbar toolbar=findViewById(R.id.bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Option");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(OptionActivity.this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

    }
}