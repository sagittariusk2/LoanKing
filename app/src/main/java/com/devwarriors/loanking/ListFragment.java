package com.devwarriors.loanking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.devwarriors.loanking.Adapter.LoanAdapter;
import com.devwarriors.loanking.Adapter.OfferAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ListFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<String> loanRequests;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        SwipeRefreshLayout swipeContainer = view.findViewById(R.id.swipeContainer);

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        loanRequests=new ArrayList<>();

        String userID= Objects.requireNonNull(auth.getCurrentUser()).getUid();

        assert getArguments() != null;
        String title = getArguments().getString("title");

        loadData(title, userID, recyclerView);

        swipeContainer.setOnRefreshListener(() -> {
            loadData(title, userID, recyclerView);
            swipeContainer.setRefreshing(false);
        });

        return view;
    }

    private void loadData(String title, String userID, RecyclerView recyclerView) {
        if(title.equalsIgnoreCase("loans")) {
            database.getReference().child("Users").child(userID).child("list2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loanRequests.clear();
                    for(DataSnapshot data:dataSnapshot.getChildren()) {
                        loanRequests.add(Objects.requireNonNull(data.getValue()).toString());
                    }

                    LoanAdapter loanAdapter = new LoanAdapter(loanRequests,getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setItemViewCacheSize(200);
                    recyclerView.setAdapter(loanAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            database.getReference().child("Users").child(userID).child("list1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loanRequests.clear();
                    for(DataSnapshot data:dataSnapshot.getChildren()) {
                        loanRequests.add(Objects.requireNonNull(data.getValue()).toString());
                    }

                    OfferAdapter offerAdapter = new OfferAdapter(loanRequests, getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setItemViewCacheSize(200);
                    recyclerView.setAdapter(offerAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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