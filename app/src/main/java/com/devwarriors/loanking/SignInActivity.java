package com.devwarriors.loanking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.ActivitySiginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    ActivitySiginBinding b;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    User oneUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b=ActivitySiginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        progressDialog=new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading");


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

        b.button.setOnClickListener(view -> signIn());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        User thisUser=new User();
                        FirebaseUser user = mAuth.getCurrentUser();
                        database= FirebaseDatabase.getInstance();

                        assert user != null;
                        thisUser.setId(user.getUid());
                        thisUser.setName(user.getDisplayName());
                        thisUser.setEmail(user.getEmail());

                        database.getReference().child("GlobalList").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot data:snapshot.getChildren())
                                {
                                    database.getReference().child("Users").child(thisUser.getId()).child("list2").child(data.getKey()).setValue(data.getKey());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        if(Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {
                            database.getReference().child("Users").child(thisUser.getId()).child("Info").setValue(thisUser);
                        }
                        reDirect();
                    } else {
                        // If sign in fails, display a message to the user.
                        Snackbar.make(b.getRoot(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void reDirect(){
        oneUser=new User();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            progressDialog.show();
            database.getReference().child("Users").child(currentUser.getUid()).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    oneUser=snapshot.getValue(User.class);
                    if(oneUser!=null) {
                        if(oneUser.getBankAccountNo().length()!=0&&oneUser.getBankBalance().length()!=0&&oneUser.getBankIFSC().length()!=0&&oneUser.getCtc().length()!=0) {
                            Intent intent=new Intent(SignInActivity.this,MainActivity.class);
                            intent.putExtra("UserInfo",oneUser);
                            startActivity(intent);
                        } else if(oneUser.getProfileImage().length()!=0&&oneUser.getDob().length()!=0&&oneUser.getPhone().length()!=0) {
                            Intent intent=new Intent(SignInActivity.this,BankActivity.class);
                            intent.putExtra("UserInfo",oneUser);
                            startActivity(intent);
                        } else if(oneUser.getSalary().length()!=0&&oneUser.getSalaryImage().length()!=0) {
                            Intent intent=new Intent(SignInActivity.this, ProfileActivity.class);
                            intent.putExtra("UserInfo",oneUser);
                            startActivity(intent);
                        } else if(oneUser.getPanImage().length()!=0&&oneUser.getPanNo().length()!=0) {
                            Intent intent=new Intent(SignInActivity.this,SalaryActivity.class);
                            intent.putExtra("UserInfo",oneUser);
                            startActivity(intent);
                        } else if(oneUser.getAadharImage().length()!=0&&oneUser.getAadharNo().length()!=0) {
                            Intent intent=new Intent(SignInActivity.this,PanActivity.class);
                            intent.putExtra("UserInfo",oneUser);
                            startActivity(intent);
                        } else {
                            Intent intent=new Intent(SignInActivity.this,AadhaarActivity.class);
                            intent.putExtra("UserInfo",oneUser);
                            startActivity(intent);
                        }
                    } else {
                        Intent intent=new Intent(SignInActivity.this,AadhaarActivity.class);
                        intent.putExtra("UserInfo",oneUser);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SignInActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        reDirect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SignInActivity.super.onBackPressed();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }
}