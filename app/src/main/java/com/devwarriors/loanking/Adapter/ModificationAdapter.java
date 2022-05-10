package com.devwarriors.loanking.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devwarriors.loanking.Data.User;
import com.devwarriors.loanking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagittariusk2.mailsend.AcceptedMessage;
import com.sagittariusk2.mailsend.MailModule;
import com.sagittariusk2.mailsend.MessageBody;
import com.sagittariusk2.mailsend.RejectedMessage;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ModificationAdapter extends RecyclerView.Adapter<ModificationAdapter.ViewHolder> {

    FirebaseAuth auth;
    FirebaseDatabase database;
    String loanRequest;
    ArrayList<String> modifiedRequests;
    User parentUser;
    MailModule mailModule;
    Context context;
    String thisId;

    public ModificationAdapter(Context context, String loanRequest, ArrayList<String> modifiedRequests,String thisId) {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        this.loanRequest = loanRequest;
        this.modifiedRequests = modifiedRequests;
        this.context = context;
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
        mailModule = new MailModule();
        this.thisId=thisId;
    }

    @NonNull
    @Override
    public ModificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.modification_container, parent, false);
        return new ViewHolder(view, this);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ModificationAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final String[] acceptId = new String[4];
        database.getReference().child("LoanRequests").child(loanRequest).child("modifiedVariants").child(modifiedRequests.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.rateTV.setText("Interest Rate: "+ Objects.requireNonNull(snapshot.child("interest").getValue())+" %");
                holder.durationTV.setText("Tenure: "+Objects.requireNonNull(snapshot.child("tenure").getValue())+" months");

                database.getReference().child("Users").child(Objects.requireNonNull(snapshot.child("modifiedBy").getValue()).toString()).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.nameTV.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                        acceptId[0]= Objects.requireNonNull(snapshot.child("id").getValue()).toString();
                        acceptId[1]= Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                        acceptId[2]= Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                        acceptId[3]= snapshot.child("phone").getKey();
                        String imageLink = Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();
                        new DownloadImageFromInternet(holder.profile_image).execute(imageLink);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if(parentUser.getId().equals(thisId)) {
            // parentUser Loan
            holder.acceptBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
            database.getReference().child("LoanRequests").child(loanRequest).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.child("acceptedBy").exists()) {
                        holder.acceptBtn.setVisibility(View.VISIBLE);
                        holder.rejectBtn.setVisibility(View.VISIBLE);
                    } else {
                        holder.acceptBtn.setVisibility(View.VISIBLE);
                        holder.acceptBtn.setEnabled(false);
                        holder.acceptBtn.setText("ACCEPTED");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.rejectBtn.setVisibility(View.GONE);
            database.getReference().child("LoanRequests").child(loanRequest).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child("acceptedBy").exists()) {
                        holder.acceptBtn.setText("ACCEPTED");
                    } else {
                        holder.acceptBtn.setText("MODIFIED");
                        holder.acceptBtn.setBackgroundColor(Color.YELLOW);
                    }
                    holder.acceptBtn.setEnabled(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        holder.acceptBtn.setOnClickListener(view -> {
            String tId=modifiedRequests.get(position);
            database.getReference().child("LoanRequests").child(loanRequest).child("modifiedVariants").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot data:snapshot.getChildren()) {
                        if(!Objects.equals(data.getKey(), tId)) {
                            database.getReference().child("LoanRequests").child(loanRequest).child("modifiedVariants").child(data.getKey()).removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // adding the acceptance information
            database.getReference().child("LoanRequests").child(loanRequest).child("acceptedBy").setValue(acceptId[0]);
            database.getReference().child("LoanRequests").child(loanRequest).child("acceptedDate").setValue(LocalDate.now().toString());

            // removing from global list
            database.getReference().child("GlobalList").child(loanRequest).removeValue();

            // sending mail to both the parties
            database.getReference().child("LoanRequests").child(loanRequest).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String amt= Objects.requireNonNull(snapshot.child("amount").getValue()).toString();
                    String interest= Objects.requireNonNull(snapshot.child("modifiedVariants").child(modifiedRequests.get(position)).child("interest").getValue()).toString();
                    String tenure= Objects.requireNonNull(snapshot.child("modifiedVariants").child(modifiedRequests.get(position)).child("tenure").getValue()).toString();
                    MessageBody messageBody=new AcceptedMessage(parentUser.getName(),parentUser.getEmail(),acceptId[2],acceptId[1],loanRequest,amt,
                            interest,tenure,acceptId[3]);
                    mailModule.sendMail(acceptId[2],messageBody);
                    messageBody=new AcceptedMessage(acceptId[1],acceptId[2],parentUser.getEmail(),parentUser.getName(),loanRequest,amt,
                            interest,tenure,acceptId[3]);
                    mailModule.sendMail(parentUser.getEmail(),messageBody);
                    Toast.makeText(context, "Accepted Successfully", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Please refresh screen by scrolling down", Toast.LENGTH_SHORT).show();

//                    String x = modifiedRequests.get(position);
//                    modifiedRequests.clear();
//                    modifiedRequests.add(x);
//                    holder.adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        holder.rejectBtn.setOnClickListener(view -> {
            // send mail
            database.getReference().child("LoanRequests").child(loanRequest).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String amt= Objects.requireNonNull(snapshot.child("amount").getValue()).toString();
                    String interest= Objects.requireNonNull(snapshot.child("modifiedVariants").child(modifiedRequests.get(position)).child("interest").getValue()).toString();
                    String tenure= Objects.requireNonNull(snapshot.child("modifiedVariants").child(modifiedRequests.get(position)).child("tenure").getValue()).toString();
                    MessageBody messageBody = new RejectedMessage(parentUser.getName(), parentUser.getEmail(), acceptId[2], acceptId[1],
                            loanRequest, amt, interest, tenure);
                    mailModule.sendMail(acceptId[2],messageBody);

                    // remove from ModificationList
                    database.getReference().child("LoanRequests").child(loanRequest).child("modifiedVariants").child(modifiedRequests.get(position)).removeValue();
                    Toast.makeText(context, "Rejected Successfully", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Please refresh screen by scrolling down", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

//        database.getReference().child("LoanRequests").child(loanRequest).child("modifiedVariants").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                modifiedRequests.clear();
//                for(DataSnapshot data:snapshot.getChildren()) {
//                    modifiedRequests.add(data.getKey());
//                }
//                holder.adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return modifiedRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV, rateTV, durationTV;
        private final Button acceptBtn,rejectBtn;
        private final CircleImageView profile_image;
        private final ModificationAdapter adapter;

        public ViewHolder(@NonNull View itemView, ModificationAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            nameTV = itemView.findViewById(R.id.nameTV);
            rateTV = itemView.findViewById(R.id.rateTV);
            durationTV = itemView.findViewById(R.id.durationTV);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn= itemView.findViewById(R.id.rejectBtn);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    private static class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
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
