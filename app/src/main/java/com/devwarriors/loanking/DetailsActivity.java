package com.devwarriors.loanking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.LayoutProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {

    LayoutProfileBinding b;
    FirebaseAuth auth;
    FirebaseDatabase database;
    User parentUser;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b=LayoutProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        parentUser=new User();

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentUser=snapshot.getValue(User.class);
                assert parentUser != null;
                b.profileID.setText(parentUser.getId());
                b.profileName.setText(parentUser.getName());
                b.profileBOD.setText(parentUser.getDob());
                b.profilePhone.setText(parentUser.getPhone());
                b.profileEmail.setText(parentUser.getEmail());
                b.profileCIBIL.setText(String.valueOf(parentUser.getCibilScore()));
                b.profileMaximumCredit.setText(String.valueOf(maxLoanAmount(parentUser.getCibilScore())));
                b.profileAadhar.setText(parentUser.getAadharNo());
                b.profilePAN.setText(parentUser.getPanNo());
                b.profileBankNumber.setText(parentUser.getBankAccountNo());
                b.profileIFSC.setText(parentUser.getBankIFSC());
                b.profileCTC.setText(parentUser.getCtc());

                new DownloadImageFromInternet(b.profileImage).execute(parentUser.getProfileImage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        b.logoutBtn.setOnClickListener(view ->{
            auth.signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
            mGoogleSignInClient.signOut();

            Toast.makeText(DetailsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(DetailsActivity.this, SignInActivity.class);
            startActivity(intent);
        });

    }
    protected int maxLoanAmount(int cibilScore)
    {
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

    @SuppressLint("StaticFieldLeak")
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
