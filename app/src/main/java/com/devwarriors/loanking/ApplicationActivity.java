package com.devwarriors.loanking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.ActivityApplicationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagittariusk2.mailsend.CreateLoanMessage;
import com.sagittariusk2.mailsend.MailModule;
import com.sagittariusk2.mailsend.MessageBody;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class ApplicationActivity extends AppCompatActivity {

    private ActivityApplicationBinding b;
    private FirebaseDatabase database;
    private User parentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b=ActivityApplicationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle("Apply For Loan Application");
        Intent intent=getIntent();
        User thisUser=(User)intent.getSerializableExtra("UserInfo");
        database=FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        parentUser=new User();

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentUser=snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ApplicationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        b.button.setOnClickListener(view -> {
            int max=maxLoanAmount(thisUser.getCibilScore());
            if(b.inputLoan.getText().toString().isEmpty()||b.inputInterest.getText().toString().isEmpty()||b.inputTenure.toString().isEmpty()) {
                Toast.makeText(ApplicationActivity.this,"Please enter all the fields",Toast.LENGTH_SHORT).show();
            } else {
                long maxi=Long.parseLong(b.inputLoan.getText().toString());
                if(maxi>max) {
                    Toast toast=Toast.makeText(ApplicationActivity.this,"You are not eligible to apply for loans having amount greater than Rs "+max,Toast.LENGTH_SHORT);
                    toast.show();
                } else if(maxi==0) {
                    Toast toast=Toast.makeText(ApplicationActivity.this,"You are not eligible to apply for loans having zero amount",Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    thisUser.setLoanAmtReqSoFar(thisUser.getLoanAmtReqSoFar()+Integer.parseInt(b.inputLoan.getText().toString()));
                    thisUser.setLoanFreq(thisUser.getLoanFreq()+1);
                    database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);

                    UUID uuid=UUID.randomUUID();

                    // Creating new LoanRequest in "LoanRequests" field
                    database.getReference().child("LoanRequests").child(uuid.toString()).child("createdBy").setValue(thisUser.getId());
                    database.getReference().child("LoanRequests").child(uuid.toString()).child("createdDate").setValue(LocalDate.now().toString());
                    database.getReference().child("LoanRequests").child(uuid.toString()).child("amount").setValue(b.inputLoan.getText().toString());
                    database.getReference().child("LoanRequests").child(uuid.toString()).child("interest").setValue(b.inputInterest.getText().toString());
                    database.getReference().child("LoanRequests").child(uuid.toString()).child("tenure").setValue(b.inputTenure.getText().toString());

                    // adding this LoanRequest to "GlobalList"
                    database.getReference().child("GlobalList").child(uuid.toString()).setValue(uuid.toString());

                    // adding the LoanRequest in "list1" of parentUser
                    database.getReference().child("Users").child(thisUser.getId()).child("list1").child(uuid.toString()).setValue(uuid.toString());

                    // adding the LoanRequest in "list2" of all users except for parentUser and sending mails to all users except for the parentUser
                    database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Toast.makeText(ApplicationActivity.this, "Applied Successfully", Toast.LENGTH_SHORT).show();
                            for(DataSnapshot data:snapshot.getChildren()) {
                                if(!Objects.equals(data.getKey(), thisUser.getId())) {
                                    database.getReference().child("Users").child(data.getKey()).child("list2").child(uuid.toString()).setValue(uuid.toString());
                                    database.getReference().child("Users").child(data.getKey()).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            User thisUser1 =snapshot.getValue(User.class);
                                            assert thisUser1 != null;
                                            String email= thisUser1.getEmail();
                                            String name= thisUser1.getName();
                                            MailModule mailModule=new MailModule();
                                            MessageBody messageBody=new CreateLoanMessage(thisUser.getName(),thisUser.getEmail(),email,name,uuid.toString(),
                                                    b.inputLoan.getText().toString(),b.inputInterest.getText().toString(),b.inputTenure.getText().toString());
                                            mailModule.sendMail(email,messageBody);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(ApplicationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ApplicationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // jumping back to main Activity
                    Intent intent1=new Intent(ApplicationActivity.this,MainActivity.class);
                    intent1.putExtra("UserInfo",thisUser);
                    startActivity(intent1);
                }
            }
        });
    }

    protected int maxLoanAmount(int cibilScore) {
        if(cibilScore<=450)
            return 0;
        else if(cibilScore<=500)
            return cibilScore*100;
        else if(cibilScore<=550)
            return cibilScore*200;
        else if(cibilScore<=600)
            return cibilScore*400;
        else if(cibilScore<=650)
            return cibilScore*800;
        else
            return cibilScore*1600;
    }
}