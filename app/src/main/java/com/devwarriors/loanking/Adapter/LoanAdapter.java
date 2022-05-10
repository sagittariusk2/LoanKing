package com.devwarriors.loanking.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.sagittariusk2.mailsend.ModifiedMessage;
import com.sagittariusk2.mailsend.RejectedMessage;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder> {

    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<String> loanRequests;
    User parentUser;
    MailModule mailModule;
    Context context;

    public LoanAdapter(ArrayList<String> loanRequests,Context context) {
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

        mailModule=new MailModule();
    }

    @NonNull
    @Override
    public LoanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list1_container, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LoanAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
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

        holder.acceptBtn.setOnClickListener(view -> {
            String id=loanRequests.get(position);

            // removing from list2 of all users
            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot data:snapshot.getChildren()) {
                        database.getReference().child("Users").child(Objects.requireNonNull(data.getKey())).child("list2").child(id).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // adding in list1 of parentUser
            database.getReference().child("Users").child(parentUser.getId()).child("list1").child(id).setValue(id);

            // removing from global list
            database.getReference().child("GlobalList").child(id).removeValue();

            // sending mail to that User
            database.getReference().child("LoanRequests").child(loanRequests.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String id1= Objects.requireNonNull(dataSnapshot.child("createdBy").getValue()).toString();
                    database.getReference().child("Users").child(id1).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            MessageBody messageBody=new AcceptedMessage(
                                    parentUser.getName(),
                                    parentUser.getEmail(),
                                    Objects.requireNonNull(snapshot.child("email").getValue()).toString(),
                                    Objects.requireNonNull(snapshot.child("name").getValue()).toString(),
                                    id,
                                    Objects.requireNonNull(dataSnapshot.child("amount").getValue()).toString(),
                                    Objects.requireNonNull(dataSnapshot.child("interest").getValue()).toString(),
                                    Objects.requireNonNull(dataSnapshot.child("tenure").getValue()).toString(),
                                    parentUser.getPhone());
                            mailModule.sendMail(Objects.requireNonNull(snapshot.child("email").getValue()).toString(),messageBody);
                            Toast.makeText(context, "Accepted Successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // adding a field "acceptedBy"
            database.getReference().child("LoanRequests").child(loanRequests.get(position)).child("acceptedBy").setValue(parentUser.getId());

            // adding a field "acceptedDate"
            database.getReference().child("LoanRequests").child(loanRequests.get(position)).child("acceptedDate").setValue(LocalDate.now().toString());

            // adding a field "modifiedVariants"
            String tempId=UUID.randomUUID().toString();
            String intRate=holder.rateTV.getText().toString();
            intRate=intRate.substring(10,intRate.length()-2);
            String tenre=holder.durationTV.getText().toString();
            tenre=tenre.substring(8,tenre.length()-7);

            database.getReference().child("LoanRequests").child(loanRequests.get(position)).child("modifiedVariants").child(tempId).child("interest").setValue(intRate);
            database.getReference().child("LoanRequests").child(loanRequests.get(position)).child("modifiedVariants").child(tempId).child("tenure").setValue(tenre);
            database.getReference().child("LoanRequests").child(loanRequests.get(position)).child("modifiedVariants").child(tempId).child("modifiedBy").setValue(parentUser.getId());
            database.getReference().child("LoanRequests").child(loanRequests.get(position)).child("modifiedVariants").child(tempId).child("modifiedDate").setValue(LocalDate.now().toString());
        });

        holder.modifyBtn.setOnClickListener(view -> {
            // creating dialog box
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Modify Loan Request");

            // set the custom layout
            final View customLayout =LayoutInflater.from(context).inflate(R.layout.activity_modified,null);
            builder.setView(customLayout);

            EditText inputTenure = customLayout.findViewById(R.id.inputTenure);
            EditText inputInterest=customLayout.findViewById(R.id.inputInterest);

            // setting the old fields in the layout
            String intRate=holder.rateTV.getText().toString();
            String tenre=holder.durationTV.getText().toString();
            inputInterest.setText(intRate.substring(10,intRate.length()-2));
            inputTenure.setText(tenre.substring(8,tenre.length()-7));

            String id=loanRequests.get(position);

            // adding a positive button
            builder.setPositiveButton("Modify By Tenure",(dialogInterface, which) ->{
                // sending mail to that user and doing operations too
                database.getReference().child("LoanRequests").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String id2= Objects.requireNonNull(dataSnapshot.child("createdBy").getValue()).toString();
                        String amount= Objects.requireNonNull(dataSnapshot.child("amount").getValue()).toString();
                        String interest= Objects.requireNonNull(dataSnapshot.child("interest").getValue()).toString();
                        String tenure= Objects.requireNonNull(dataSnapshot.child("tenure").getValue()).toString();

                        if(inputTenure.getText().toString().isEmpty()||inputInterest.getText().toString().isEmpty()) {
                            Toast.makeText( context, "Error!! Please enter both the fields to continue", Toast.LENGTH_SHORT).show();
                        } else if(interest.equals(inputInterest.getText().toString())&&tenure.equals(inputTenure.getText().toString())) {
                            Toast.makeText( context, "Error!! You have not modified any of the fields", Toast.LENGTH_SHORT).show();
                        } else if(!interest.equals(inputInterest.getText().toString())) {
                            Toast.makeText( context, "Error!! You can only modify tenure", Toast.LENGTH_SHORT).show();
                        } else if(tenure.equals(inputTenure.getText().toString())) {
                            Toast.makeText( context, "Error!! You have not modified tenure", Toast.LENGTH_SHORT).show();
                        } else {
                            database.getReference().child("Users").child(id2).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    MessageBody messageBody=new ModifiedMessage(
                                            parentUser.getName(),
                                            parentUser.getEmail(),
                                            Objects.requireNonNull(snapshot.child("email").getValue()).toString(),
                                            Objects.requireNonNull(snapshot.child("name").getValue()).toString(),
                                            id,
                                            amount,
                                            amount,
                                            interest,
                                            inputInterest.getText().toString(),
                                            tenure,
                                            inputTenure.getText().toString(),
                                            parentUser.getPhone());
                                    mailModule.sendMail(Objects.requireNonNull(snapshot.child("email").getValue()).toString(),messageBody);
                                    Toast.makeText(context, "Modified Successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            // adding to list1 of parentUser
                            database.getReference().child("Users").child(parentUser.getId()).child("list1").child(id).setValue(id);

                            // removing from list2 of parentUser
                            database.getReference().child("Users").child(parentUser.getId()).child("list2").child(id).removeValue();

                            // adding data to the LoanRequest(basically adding a loanRequestId in modified field in the LoanRequest)
                            String id1=UUID.randomUUID().toString();
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("modifiedBy").setValue(parentUser.getId());
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("modifiedDate").setValue(LocalDate.now().toString());
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("interest").setValue(inputInterest.getText().toString());
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("tenure").setValue(inputTenure.getText().toString());
                            //Toast.makeText( context, "Loan Request Modified Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } );

            // adding a negative button
            builder.setNegativeButton("Modify By Interest",(dialogInterface, which) ->{
                // sending mail to that user and doing other operations too
                database.getReference().child("LoanRequests").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String id2= Objects.requireNonNull(dataSnapshot.child("createdBy").getValue()).toString();
                        String amount= Objects.requireNonNull(dataSnapshot.child("amount").getValue()).toString();
                        String interest= Objects.requireNonNull(dataSnapshot.child("interest").getValue()).toString();
                        String tenure= Objects.requireNonNull(dataSnapshot.child("tenure").getValue()).toString();

                        if(inputTenure.getText().toString().isEmpty()||inputInterest.getText().toString().isEmpty()) {
                            Toast.makeText( context, "Error!! Please enter both the fields to continue", Toast.LENGTH_SHORT).show();
                        } else if(interest.equals(inputInterest.getText().toString())&&tenure.equals(inputTenure.getText().toString())) {
                            Toast.makeText( context, "Error!! You have not modified any of the fields", Toast.LENGTH_SHORT).show();
                        } else if(!tenure.equals(inputTenure.getText().toString())) {
                            Toast.makeText( context, "Error!! You can only modify interest rate", Toast.LENGTH_SHORT).show();
                        } else if(interest.equals(inputInterest.getText().toString())) {
                            Toast.makeText( context, "Error!! You have not modified interest rate", Toast.LENGTH_SHORT).show();
                        } else {
                            database.getReference().child("Users").child(id2).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    MessageBody messageBody=new ModifiedMessage(
                                            parentUser.getName(),
                                            parentUser.getEmail(),
                                            Objects.requireNonNull(snapshot.child("email").getValue()).toString(),
                                            Objects.requireNonNull(snapshot.child("name").getValue()).toString(),
                                            id,
                                            amount,
                                            amount,
                                            interest,
                                            inputInterest.getText().toString(),
                                            tenure,
                                            inputTenure.getText().toString(),
                                            parentUser.getPhone());
                                    mailModule.sendMail(Objects.requireNonNull(snapshot.child("email").getValue()).toString(),messageBody);
                                    Toast.makeText(context, "Modified Successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            // adding to list1 of parentUser
                            database.getReference().child("Users").child(parentUser.getId()).child("list1").child(id).setValue(id);

                            // removing from list2 of parentUser
                            database.getReference().child("Users").child(parentUser.getId()).child("list2").child(id).removeValue();

                            // adding data to the LoanRequest(basically adding a loanRequestId in modified field in the LoanRequest)
                            String id1=UUID.randomUUID().toString();
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("modifiedBy").setValue(parentUser.getId());
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("modifiedDate").setValue(LocalDate.now().toString());
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("interest").setValue(inputInterest.getText().toString());
                            database.getReference().child("LoanRequests").child(id).child("modifiedVariants").child(id1).child("tenure").setValue(inputTenure.getText().toString());
                            //Toast.makeText( context, "Loan Request Modified Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } );

            // adding a neutral button
            builder.setNeutralButton("Cancel",((dialogInterface, which) ->{

            } ));

            // create and show
            // the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        holder.rejectBtn.setOnClickListener(view -> {
            String id=loanRequests.get(position);
            database.getReference().child("Users").child(parentUser.getId()).child("list2").child(id).removeValue();

            database.getReference().child("LoanRequests").child(loanRequests.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String id1= Objects.requireNonNull(dataSnapshot.child("createdBy").getValue()).toString();
                    database.getReference().child("Users").child(id1).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            MessageBody messageBody=new RejectedMessage(
                                    parentUser.getName(),
                                    parentUser.getEmail(),
                                    Objects.requireNonNull(snapshot.child("email").getValue()).toString(),
                                    Objects.requireNonNull(snapshot.child("name").getValue()).toString(),
                                    id,
                                    Objects.requireNonNull(dataSnapshot.child("amount").getValue()).toString(),
                                    Objects.requireNonNull(dataSnapshot.child("interest").getValue()).toString(),
                                    Objects.requireNonNull(dataSnapshot.child("tenure").getValue()).toString());
                            mailModule.sendMail(Objects.requireNonNull(snapshot.child("email").getValue()).toString(),messageBody);
                            Toast.makeText(context, "Rejected Successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return loanRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV, amtTV, rateTV, durationTV, loanIDTV;
        private final Button acceptBtn, modifyBtn, rejectBtn;
        private final CircleImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            amtTV = itemView.findViewById(R.id.amtTV);
            rateTV = itemView.findViewById(R.id.rateTV);
            durationTV = itemView.findViewById(R.id.durationTV);
            loanIDTV = itemView.findViewById(R.id.loanIDTV);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            modifyBtn = itemView.findViewById(R.id.modifyBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
            profile_image = itemView.findViewById(R.id.profile_image);

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
