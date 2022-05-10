package com.devwarriors.loanking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.ActivitySalaryBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class SalaryActivity extends AppCompatActivity {

    ActivitySalaryBinding b;
    private final int REQ_CODE=1;
    private final int CAM_REQ_CODE=2;
    FirebaseDatabase database;
    FirebaseStorage firebaseStorage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Salary Details");
        b= ActivitySalaryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        Intent intent=getIntent();
        User thisUser=(User)intent.getSerializableExtra("UserInfo");

        firebaseStorage = FirebaseStorage.getInstance();

        progressDialog=new ProgressDialog(SalaryActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Data");

        Drawable d=b.inputSalaryPic.getDrawable();


        b.galleryButton.setOnClickListener(view -> {
            Intent gallery=new Intent(Intent.ACTION_PICK);
            gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery,REQ_CODE);
        });

        b.cameraButton.setOnClickListener(view -> {
            Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera,CAM_REQ_CODE);
        });

        b.next.setOnClickListener(view -> {
            if(b.inputSalary.getText().toString().isEmpty()||b.inputSalaryPic.getDrawable()==d) {
                Toast.makeText(SalaryActivity.this, "Please enter both the fields", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();
                thisUser.setSalary(b.inputSalary.getText().toString());

                // Upload image
                b.inputSalaryPic.setDrawingCacheEnabled(true);
                b.inputSalaryPic.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) b.inputSalaryPic.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = firebaseStorage.getReference().child(thisUser.getId()).child("salary.jpg").putBytes(data);

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL
                    return firebaseStorage.getReference().child(thisUser.getId()).child("salary.jpg").getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        progressDialog.dismiss();

                        thisUser.setSalaryImage(downloadUri.toString());

                        database = FirebaseDatabase.getInstance();
                        database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);
                        Intent intent1 = new Intent(SalaryActivity.this, ProfileActivity.class);
                        intent1.putExtra("UserInfo", thisUser);
                        startActivity(intent1);
                    } else {
                        Toast.makeText(this, "Upload Failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            if(requestCode==REQ_CODE) {
                assert data != null;
                b.inputSalaryPic.setImageURI(data.getData());
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setSalaryImage((data.getData().toString()));
//                //Log.d("hello",thisUser.getAadharImage());
            }if(requestCode==CAM_REQ_CODE) {
                assert data != null;
                Bitmap img=(Bitmap)(data.getExtras().get("data"));
                b.inputSalaryPic.setImageBitmap(img);
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setSalaryImage(img.toString());
//                //Log.d("hello",thisUser.getAadharImage());
            }
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SalaryActivity.super.onBackPressed();
                    quit();
                }).create().show();
    }

    public void quit() {
        Intent start = new Intent(Intent.ACTION_MAIN);
        start.addCategory(Intent.CATEGORY_HOME);
        start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(start);
    }
}