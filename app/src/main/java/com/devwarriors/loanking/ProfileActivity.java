package com.devwarriors.loanking;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.devwarriors.loanking.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding b;
    private final int REQ_CODE=1;
    private final int CAM_REQ_CODE=2;
    private DatePickerDialog datePickerDialog;
    FirebaseDatabase database;
    FirebaseStorage firebaseStorage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        b=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        firebaseStorage = FirebaseStorage.getInstance();

        progressDialog=new ProgressDialog(ProfileActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Data");

        Drawable d=b.inputProfilePic.getDrawable();

        initDatePicker();
        b.inputDob.setText(getTodaysDate());

        Intent intent=getIntent();
        User thisUser=(User)intent.getSerializableExtra("UserInfo");

        b.inputDob.setOnClickListener(view -> datePickerDialog.show());

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
            if(b.inputPhone.getText().toString().isEmpty()||b.inputProfilePic.getDrawable()==d) {
                Toast.makeText(ProfileActivity.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
            } else if(b.inputPhone.getText().toString().length()!=10) {
                Toast.makeText(ProfileActivity.this, "Please enter a valid phone number(of 10 digits)", Toast.LENGTH_SHORT).show();
            } else {
                thisUser.setDob(b.inputDob.getText().toString());
                thisUser.setPhone(b.inputPhone.getText().toString());

                progressDialog.show();

                // Upload image
                b.inputProfilePic.setDrawingCacheEnabled(true);
                b.inputProfilePic.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) b.inputProfilePic.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = firebaseStorage.getReference().child(thisUser.getId()).child("profile.jpg").putBytes(data);

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL
                    return firebaseStorage.getReference().child(thisUser.getId()).child("profile.jpg").getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        progressDialog.dismiss();
                        thisUser.setProfileImage(downloadUri.toString());

                        database = FirebaseDatabase.getInstance();
                        database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);
                        Intent intent1 = new Intent(ProfileActivity.this, BankActivity.class);
                        intent1.putExtra("UserInfo", thisUser);
                        startActivity(intent1);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Upload Failure", Toast.LENGTH_SHORT).show();
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
                b.inputProfilePic.setImageURI(data.getData());
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setProfileImage((data.getData().toString()));
//                //Log.d("hello",thisUser.getAadharImage());
            }
            if(requestCode==CAM_REQ_CODE) {
                assert data != null;
                Bitmap img=(Bitmap)(data.getExtras().get("data"));
                b.inputProfilePic.setImageBitmap(img);
//                Intent intent=getIntent();
//                User thisUser=(User)intent.getSerializableExtra("UserInfo");
//                thisUser.setProfileImage(img.toString());
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
                    ProfileActivity.super.onBackPressed();
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

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = makeDateString(day, month, year);
            b.inputDob.setText(date);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        //default should never happen
        return "JAN";
    }
}