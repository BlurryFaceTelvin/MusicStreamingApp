package com.example.blurryface.musicstreamingapp;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.africastalking.AfricasTalking;
import com.africastalking.models.payment.checkout.CheckoutResponse;
import com.africastalking.models.payment.checkout.MobileCheckoutRequest;
import com.africastalking.services.PaymentService;
import com.africastalking.utils.Callback;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicListActivity extends AppCompatActivity {
    RecyclerView musicList;
    DatabaseReference musicDatabase;
    ProgressBar musicProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        //initialise the recycler view
        musicList = findViewById(R.id.musicRecyclerView);
        musicProgressBar = findViewById(R.id.musicProgress);
        //make sure our music list has a fixed size
        musicList.setHasFixedSize(true);
        musicList.setLayoutManager(new LinearLayoutManager(this));
        musicDatabase = FirebaseDatabase.getInstance().getReference().child("Music");
        //initialize the Africa's Talking API ip address of my machine

        try {
            AfricasTalking.initialize("192.168.1.130",35897, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter <Music, MusicViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Music, MusicViewHolder>(
                        Music.class,
                        R.layout.music_data_layout,
                        MusicViewHolder.class,
                        musicDatabase
                ) {
                    @Override
                    protected void populateViewHolder(MusicViewHolder viewHolder, Music model, int position) {
                        viewHolder.setArtistName(model.getArtist());
                        viewHolder.setSongName(model.getTitle());
                        viewHolder.setImage(model.getImage(),getApplicationContext());
                        musicProgressBar.setVisibility(View.GONE);

                    }
                };

        musicList.setAdapter(firebaseRecyclerAdapter);

    }


    public void onBuy(View view){
        new Paying().execute();
        Log.e("Error","Here");
        //Intent mainIntent = new Intent(MusicListActivity.this,MainActivity.class);
        //startActivity(mainIntent);
    }


    public static class MusicViewHolder extends RecyclerView.ViewHolder{
        View view;
        public MusicViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
        public void setImage(String thumbImageUrl, Context context){
            CircleImageView thumb = view.findViewById(R.id.user_single_photo);
            Picasso.with(context).load(thumbImageUrl).placeholder(R.drawable.defaultimage).into(thumb);
        }
        public void setArtistName(String artistName){
            TextView name = view.findViewById(R.id.single_artist_name);
            name.setText(artistName);
        }
        public void setSongName(String title){
            TextView songName = view.findViewById(R.id.single_song_title);
            songName.setText(title);
        }

    }

    public class Paying extends AsyncTask<Void, String, Void>{
        PaymentService paymentService;
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                paymentService = AfricasTalking.getPaymentService();
                MobileCheckoutRequest checkoutRequest = new MobileCheckoutRequest("MusicProduct","KES 10","0703280748");
                paymentService.checkout(checkoutRequest, new Callback<CheckoutResponse>() {
                    @Override
                    public void onSuccess(CheckoutResponse data) {
                        if(data.status.equals("Success")){
                            Toast.makeText(MusicListActivity.this,"Successful",Toast.LENGTH_LONG).show();
                        }
                        //Toast.makeText(MusicListActivity.this,data.description.toString(),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });

            }catch (Exception e){
                e.printStackTrace();
                //Toast.makeText(MusicListActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }

}
