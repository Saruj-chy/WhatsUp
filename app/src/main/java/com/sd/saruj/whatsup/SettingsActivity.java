package com.sd.saruj.whatsup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private static final int galleryPick = 1;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        initializefields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();
//        userName.setVisibility(View.INVISIBLE);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("Image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, Your Profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d("TAG", "Yes, Upload");
                            Toast.makeText(SettingsActivity.this, "Profile image Uploaded successfully", Toast.LENGTH_SHORT).show();


//                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
//                            final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                            final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                            RootRef.child("Users").child(currentUserId).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this, "Image save in database, successfully..", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else{
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error :"+ message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });
                        }
                        else{
                            Log.d("TAG", "No");
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
//                Toast.makeText(this, "result: " +resultUri +"FilePath : "+ filePath , Toast.LENGTH_LONG).show();
                Log.d("TAG", "result: " +resultUri +" \n  FilePath : "+ filePath);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error " +error, Toast.LENGTH_SHORT).show();

            }

        }

    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")))
                        {
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveStatus =dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage =dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);
//                            Picasso.get().load(retriveProfileImage).into(userProfileImage);
//                            Picasso.get().load("http://i.imgur.com/DvpvklR.png").into(userProfileImage);

                            Log.d("TAG","image retrive");
                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveStatus =dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);
                            Log.d("TAG","image not retrive");

                        }
                        else{
//                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your Profile Information.. ", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(getApplicationContext(), "Please write your UserName first...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(getApplicationContext(), "Please write your status...", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            RootRef.child("Users").child(currentUserId).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private void initializefields(){
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
