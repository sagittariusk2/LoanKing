package com.devwarriors.loanking.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<String> loanRequests;
    User parentUser;
    Context context;
    String thisUserId;

    public OfferAdapter(ArrayList<String> loanRequests, Context context) {
        this.loanRequests=loanRequests;
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        parentUser=new User();
        this.context=context;

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentUser=snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public OfferAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.personal_loan_container, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OfferAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ArrayList<String> modifiedRequests = new ArrayList<>();

        database.getReference("LoanRequests").child(loanRequests.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modifiedRequests.clear();
                thisUserId= Objects.requireNonNull(snapshot.child("createdBy").getValue()).toString();
                for(DataSnapshot data:snapshot.child("modifiedVariants").getChildren()) {
                    if(parentUser.getId().equals(thisUserId))
                        modifiedRequests.add(data.getKey());
                    else
                    {
                        if(Objects.requireNonNull(data.child("modifiedBy").getValue()).toString().equals(parentUser.getId()))
                            modifiedRequests.add(data.getKey());
                    }
                }
                ModificationAdapter modificationAdapter = new ModificationAdapter(context, loanRequests.get(position), modifiedRequests, thisUserId);
                holder.modificationRecyclerViewID.setLayoutManager(new LinearLayoutManager(context));
                holder.modificationRecyclerViewID.setItemViewCacheSize(100);
                holder.modificationRecyclerViewID.setAdapter(modificationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setting fields in View
        database.getReference().child("LoanRequests").child(loanRequests.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id= Objects.requireNonNull(dataSnapshot.child("createdBy").getValue()).toString();
                database.getReference().child("Users").child(id).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.nameTV.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                        new DownloadImageFromInternet(holder.profile_image).execute(Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                holder.amtTV.setText("Amount: Rs. "+ Objects.requireNonNull(dataSnapshot.child("amount").getValue()));
                holder.rateTV.setText("Interest: "+ Objects.requireNonNull(dataSnapshot.child("interest").getValue())+" %");
                holder.durationTV.setText("Tenure: "+ Objects.requireNonNull(dataSnapshot.child("tenure").getValue())+" months");
                holder.loanIDTV.setText("Loan ID: "+loanRequests.get(position).substring(0, 8)+"..."+loanRequests.get(position).substring(25));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.arrowIM.setOnClickListener(view -> {
            if(view.getContentDescription().toString().equalsIgnoreCase("closed")) {
                // open
                if(modifiedRequests.size()==0) {
                    Toast.makeText(context, "There are no deals right now for this loan request", Toast.LENGTH_SHORT).show();
                } else {
                    holder.modificationRecyclerViewID.setVisibility(View.VISIBLE);
                    holder.arrowIM.setImageResource(R.drawable.up_arrow);
                    view.setContentDescription("opened");
                }
            } else {
                // close
                holder.modificationRecyclerViewID.setVisibility(View.GONE);
                holder.arrowIM.setImageResource(R.drawable.down_arrow);
                view.setContentDescription("closed");
            }
        });
    }

    @Override
    public int getItemCount() {
        return loanRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV, amtTV, rateTV, durationTV, loanIDTV;
        private final CircleImageView profile_image;
        private final ImageButton arrowIM;
        private final RecyclerView modificationRecyclerViewID;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            amtTV = itemView.findViewById(R.id.amtTV);
            rateTV = itemView.findViewById(R.id.rateTV);
            durationTV = itemView.findViewById(R.id.durationTV);
            loanIDTV = itemView.findViewById(R.id.loanIDTV);
            arrowIM = itemView.findViewById(R.id.arrowIM);
            profile_image = itemView.findViewById(R.id.profile_image);
            modificationRecyclerViewID = itemView.findViewById(R.id.modificationRecyclerViewID);
        }
    }

    private static class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
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
