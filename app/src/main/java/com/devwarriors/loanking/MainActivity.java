package com.devwarriors.loanking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    ActivityMainBinding b;
    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    Handler handler;
    User thisUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();

        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Calculating Your Cibil Score");

        handler=new Handler();

        Intent intent=getIntent();
        thisUser=(User)intent.getSerializableExtra("UserInfo");

        thisUser.setCibilScore(calCibilScore(Integer.parseInt(thisUser.getSalary()),
                Integer.parseInt(thisUser.getBankBalance()),
                Integer.parseInt(thisUser.getCtc()),
                thisUser.getLoanAmtReqSoFar(),
                thisUser.getLoanFreq(),
                thisUser.getDob()));


        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Deals");
        arrayList.add("Loans");

        prepareViewPager(b.viewPager, arrayList);

        b.tabLayout.setupWithViewPager(b.viewPager);

        String id= Objects.requireNonNull(auth.getCurrentUser()).getUid();

        database.getReference().child("Users").child(id).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                thisUser=snapshot.getValue(User.class);
                assert thisUser != null;
                thisUser.setCibilScore(calCibilScore(Integer.parseInt(thisUser.getSalary()),
                        Integer.parseInt(thisUser.getBankBalance()),
                        Integer.parseInt(thisUser.getCtc()),
                        thisUser.getLoanAmtReqSoFar(),
                        thisUser.getLoanFreq(),
                        thisUser.getDob()));
                database.getReference().child("Users").child(id).child("Info").setValue(thisUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        b.displayName.setText(thisUser.getName());
        b.displayName.setOnClickListener(view -> {
            Intent intent1=new Intent(MainActivity.this,DetailsActivity.class);
            startActivity(intent1);
        });
        new DownloadImageFromInternet(b.profileImage).execute(thisUser.getProfileImage());
        b.profileImage.setOnClickListener(view -> {
            Intent intent1=new Intent(MainActivity.this,DetailsActivity.class);
            startActivity(intent1);
        });

        b.check.setOnClickListener(view -> {
            progressDialog.show();
            handler.postDelayed(() -> {
                progressDialog.dismiss();
                Toast toast=Toast.makeText(MainActivity.this,"Your Cibil Score is "+thisUser.getCibilScore(),Toast.LENGTH_SHORT);
                toast.show();
            },2000);
        });

        b.floatingButton.setOnClickListener(view -> {
            Intent intent1=new Intent(MainActivity.this,ApplicationActivity.class);
            intent1.putExtra("UserInfo",thisUser);
            startActivity(intent1);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Logout: {
                auth.signOut();
                GoogleSignInOptions gso = new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
                mGoogleSignInClient.signOut();

                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    MainActivity.super.onBackPressed();
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

    public int calCibilScore(int salary,int bankBalance,int ctc,int loanAmtReqSoFar,int loanFreq,String dob) {
        double d=(((0.2)*(12*salary+bankBalance)+(0.8)*(ctc-12*salary))-2*loanAmtReqSoFar)/(ctc);
        int cibilScore=(int)((700-300+1)*d)+300;
        cibilScore-=10*loanFreq;
        int age=calAge(dob);
        if(age<18)
            cibilScore=300;
        else if(age<=25)
            cibilScore+=5;
        else if(age<=45)
            cibilScore+=10;
        else if(age<=60)
            cibilScore+=5;
        if(cibilScore<300)
            cibilScore=300;
        else if(cibilScore>700)
            cibilScore=700;
        return cibilScore;
    }

    public int calAge(String dob) {
        String[] s=dob.split(" ");
        int birthYear=Integer.parseInt(s[2]);
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        return currentYear-birthYear;
    }

    private void prepareViewPager(ViewPager viewPager, ArrayList<String> arrayList) {
        MainAdapter adapter = new MainAdapter(getSupportFragmentManager());
        ListFragment listFragment = new ListFragment();

        for(int i=0; i<arrayList.size(); i++) {
            Bundle bundle = new Bundle();
            bundle.putString("title", arrayList.get(i));
            //bundle.putString("appUserID",auth.getCurrentUser().getUid());
            listFragment.setArguments(bundle);
            adapter.addFragment(listFragment, arrayList.get(i));
            listFragment = new ListFragment();
        }

        viewPager.setAdapter(adapter);
    }

    private class MainAdapter extends FragmentPagerAdapter {
        ArrayList<String> arrayList = new ArrayList<>();
        List<Fragment> fragmentList = new ArrayList<>();

        public void addFragment(Fragment fragment, String title) {
            arrayList.add(title);
            fragmentList.add(fragment);
        }

        public MainAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return arrayList.get(position);
        }
    }

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