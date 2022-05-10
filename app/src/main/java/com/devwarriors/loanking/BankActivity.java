package com.devwarriors.loanking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.ActivityBankBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankActivity extends AppCompatActivity {

    private ActivityBankBinding b;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Bank Details");
        b=ActivityBankBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        progressDialog=new ProgressDialog(BankActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Data");

        Intent intent=getIntent();
        User thisUser=(User)intent.getSerializableExtra("UserInfo");

        b.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(b.inputAcNo.getText().toString().isEmpty()||b.inputBalance.getText().toString().isEmpty()||
                        b.inputCtc.getText().toString().isEmpty()||b.inputCtc.getText().toString().isEmpty()) {
                    Toast.makeText(BankActivity.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                } else if(!isBankAcOk(b.inputAcNo.getText().toString())) {
                    Toast.makeText(BankActivity.this, "Please enter a valid bank account number", Toast.LENGTH_SHORT).show();
                } else if(!isIFSCok(b.inputIFSC.getText().toString())) {
                    Toast.makeText(BankActivity.this, "Please enter a valid IFSC code", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.show();
                    thisUser.setBankAccountNo(b.inputAcNo.getText().toString());
                    thisUser.setBankIFSC(b.inputIFSC.getText().toString());
                    thisUser.setBankBalance(b.inputBalance.getText().toString());
                    thisUser.setCtc(b.inputCtc.getText().toString());
                    database=FirebaseDatabase.getInstance();
                    database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);
                    progressDialog.dismiss();
                    Intent intent1=new Intent(BankActivity.this,MainActivity.class);
                    intent1.putExtra("UserInfo",thisUser);
                    startActivity(intent1);
                }
            }

            private boolean isIFSCok(String str) {
                String regex = "^[A-Z]{4}0[A-Z0-9]{6}$";

                // Compile the ReGex
                Pattern p = Pattern.compile(regex);

                // If the string is empty
                // return false
                if (str == null) {
                    return false;
                }

                // Pattern class contains matcher()
                // method to find matching between
                // the given string and
                // the regular expression.
                Matcher m = p.matcher(str);

                // Return if the string
                // matched the ReGex
                return m.matches();
            }

            private boolean isBankAcOk(String str) {
                String regex = "[0-9]{9,18}";

                // Compile the ReGex
                Pattern p = Pattern.compile(regex);

                // If the string is empty
                // return false
                if (str == null) {
                    return false;
                }

                // Pattern class contains matcher()
                // method to find matching between
                // the given string and
                // the regular expression.
                Matcher m = p.matcher(str);

                // Return if the string
                // matched the ReGex
                return m.matches();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    BankActivity.super.onBackPressed();
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