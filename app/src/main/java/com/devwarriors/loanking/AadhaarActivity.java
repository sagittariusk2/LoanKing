package com.devwarriors.loanking;

import android.annotation.SuppressLint;
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
import com.devwarriors.loanking.databinding.ActivityAadhaarBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AadhaarActivity extends AppCompatActivity {

    private ActivityAadhaarBinding b;
    private final int REQ_CODE=1;
    private final int CAM_REQ_CODE=2;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Aadhaar Details");
        b=ActivityAadhaarBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Intent intent=getIntent();
        User thisUser=(User)intent.getSerializableExtra("UserInfo");
        firebaseStorage = FirebaseStorage.getInstance();

        progressDialog=new ProgressDialog(AadhaarActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Data");
        Drawable d=b.inputAadhaarPic.getDrawable();

        b.galleryButton.setOnClickListener(view ->  {
            Intent gallery=new Intent(Intent.ACTION_PICK);
            gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery,REQ_CODE);
        });

        b.cameraButton.setOnClickListener(view ->  {
            Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera,CAM_REQ_CODE);
        });

        b.next.setOnClickListener(view ->  {
            if(b.inputAadhaar.getText().toString().isEmpty()||b.inputAadhaarPic.getDrawable()==d) {
                Toast.makeText(AadhaarActivity.this, "Please enter both the fields", Toast.LENGTH_SHORT).show();
            } else if(!isAadhaarOk(b.inputAadhaar.getText().toString())) {
                Toast.makeText(AadhaarActivity.this, "Please enter a valid aadhaar number(with spaces)", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();

                // Upload image
                b.inputAadhaarPic.setDrawingCacheEnabled(true);
                b.inputAadhaarPic.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) b.inputAadhaarPic.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = firebaseStorage.getReference().child(thisUser.getId()).child("aadhar.jpg").putBytes(data);

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL
                    return firebaseStorage.getReference().child(thisUser.getId()).child("aadhar.jpg").getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        progressDialog.dismiss();

                        thisUser.setAadharNo(b.inputAadhaar.getText().toString());
                        thisUser.setAadharImage(downloadUri.toString());
                        database= FirebaseDatabase.getInstance();
                        database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);
                        Intent intent1=new Intent(AadhaarActivity.this,PanActivity.class);
                        intent1.putExtra("UserInfo",thisUser);
                        startActivity(intent1);
                    } else {
                        // Handle failures
                        // ...
                        Toast.makeText(this, "Please Upload Once Again", Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnFailureListener(e -> Toast.makeText(this, "Unsuccessful Upload!! Please Upload Once again", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {

                });
            }
        });

        b.inputAadhaar.addTextChangedListener(new TextWatcher() {
            int x=0;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                x = charSequence.length();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(x<editable.length()) {
                    if (b.inputAadhaar.getText().toString().length() == 4) {
                        b.inputAadhaar.setText(b.inputAadhaar.getText().toString() + " ");
                        b.inputAadhaar.setSelection(b.inputAadhaar.getText().length());
                    } else if (b.inputAadhaar.getText().toString().length() == 9) {
                        b.inputAadhaar.setText(b.inputAadhaar.getText().toString() + " ");
                        b.inputAadhaar.setSelection(b.inputAadhaar.getText().length());
                    } else if (b.inputAadhaar.getText().toString().length() == 15) {
                        b.inputAadhaar.setText(b.inputAadhaar.getText().toString().substring(0, b.inputAadhaar.getText().toString().length() - 1));
                        b.inputAadhaar.setSelection(b.inputAadhaar.getText().length());
                    }
                }
            }
        });
    }

    private boolean isAadhaarOk(String str) {
        String regex = "^[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (str == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given string
        // and regular expression.
        Matcher m = p.matcher(str);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            if(requestCode==REQ_CODE) {
                assert data != null;
                b.inputAadhaarPic.setImageURI(data.getData());
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setAadharImage(data.getData().toString());
//                Log.d("hello",thisUser.getAadharImage());
            }
            if(requestCode==CAM_REQ_CODE) {
                assert data != null;
                Bitmap img=(Bitmap)(data.getExtras().get("data"));
                b.inputAadhaarPic.setImageBitmap(img);
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setAadharImage(img.toString());
//                Log.d("hello",thisUser.getAadharImage());
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
                    AadhaarActivity.super.onBackPressed();
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