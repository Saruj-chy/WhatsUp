package com.sd.saruj.whatsup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private String receiveUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiveUserID = getIntent().getExtras().get("visit_user_id").toString();

        Toast.makeText(this, "User Id: "+receiveUserID, Toast.LENGTH_SHORT).show();
    }
}
