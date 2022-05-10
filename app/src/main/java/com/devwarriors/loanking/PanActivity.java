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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.ActivityPanBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PanActivity extends AppCompatActivity {

    ActivityPanBinding b;
    private final int REQ_CODE=1;
    private final int CAM_REQ_CODE=2;
    FirebaseDatabase database;
    FirebaseStorage firebaseStorage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("PAN Details");
        b=ActivityPanBinding.inflate(getLayoutInflater());

        setContentView(b.getRoot());
        Intent intent=getIntent();
        User thisUser=(User)intent.getSerializableExtra("UserInfo");
        firebaseStorage = FirebaseStorage.getInstance();

        progressDialog=new ProgressDialog(PanActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Data");
        Drawable d=b.inputPanPic.getDrawable();


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
            if(b.inputPanNumber.getText().toString().isEmpty()||b.inputPanPic.getDrawable()==d) {
                Toast.makeText(PanActivity.this, "Please enter both the fields", Toast.LENGTH_SHORT).show();
            } else if(!isPanOk(b.inputPanNumber.getText().toString())) {
                Toast.makeText(PanActivity.this, "Please enter a valid PAN card number", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();

                // Upload image
                b.inputPanPic.setDrawingCacheEnabled(true);
                b.inputPanPic.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) b.inputPanPic.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = firebaseStorage.getReference().child(thisUser.getId()).child("pan.jpg").putBytes(data);

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL
                    return firebaseStorage.getReference().child(thisUser.getId()).child("pan.jpg").getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        progressDialog.dismiss();

                        thisUser.setPanNo(b.inputPanNumber.getText().toString());
                        thisUser.setPanImage(downloadUri.toString());
                        database = FirebaseDatabase.getInstance();
                        database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);
                        Intent intent1 = new Intent(PanActivity.this, SalaryActivity.class);
                        intent1.putExtra("UserInfo", thisUser);
                        startActivity(intent1);
                    } else {
                        Toast.makeText(this, "Upload Failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean isPanOk(String panCardNo) {
        String regex = "[A-Z]{5}[0-9]{4}[A-Z]{1}";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the PAN Card number
        // is empty return false
        if (panCardNo == null)
        {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given
        // PAN Card number using regular expression.
        Matcher m = p.matcher(panCardNo);

        // Return if the PAN Card number
        // matched the ReGex
        return m.matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            if(requestCode==REQ_CODE) {
                assert data != null;
                b.inputPanPic.setImageURI(data.getData());
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setPanImage(data.getData().toString());
//                //Log.d("hello",thisUser.getAadharImage());
            }
            if(requestCode==CAM_REQ_CODE) {
                assert data != null;
                Bitmap img=(Bitmap)(data.getExtras().get("data"));
                b.inputPanPic.setImageBitmap(img);
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setPanImage(img.toString());
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
                    PanActivity.super.onBackPressed();
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