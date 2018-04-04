package com.example.blurryface.musicstreamingapp;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.africastalking.AfricasTalking;
import com.africastalking.models.payment.checkout.CheckoutResponse;
import com.africastalking.models.payment.checkout.CheckoutValidationResponse;
import com.africastalking.models.payment.checkout.MobileCheckoutRequest;
import com.africastalking.services.PaymentService;
import com.africastalking.utils.Callback;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;



public class MusicListActivity extends AppCompatActivity {
    RecyclerView musicList;
    DatabaseReference musicDatabase;
    ProgressBar musicProgressBar;
    FirebaseUser currentUser;
    SpotsDialog buyDialog,dialog;
    Toolbar toolbar;
    Intent mainIntent;
    OkHttpClient client;
    Request request;
    int status;
    PaymentService paymentService;
    boolean mResume;
    int buyplayBtn = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        //initialise the recycler view
        musicList = findViewById(R.id.musicRecyclerView);
        musicProgressBar = findViewById(R.id.musicProgress);
        //initialise spot dialog
        buyDialog = new SpotsDialog(this,"Wait a Moment");
        dialog = new SpotsDialog(this,"Processing");
        //initialise the toolbar
        toolbar = findViewById(R.id.musicListToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Music App");
        //make sure our music list has a fixed size
        musicList.setHasFixedSize(true);
        musicList.setLayoutManager(new LinearLayoutManager(this));
        musicDatabase = FirebaseDatabase.getInstance().getReference().child("Music");
        mainIntent = new Intent(MusicListActivity.this,MainActivity.class);
        //initialize current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //initialise africastalking gateway
        //initialize the Africa's Talking API ip address of my machine
        try {
            AfricasTalking.initialize("192.168.137.107",35897, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (savedInstanceState != null){
            buyplayBtn = savedInstanceState.getInt("currentBuy");
        }
        mResume = true;
        status = 0;
    }


    @Override
    protected void onStart() {
        super.onStart();
        //check if user is not logged in go to the WelcomeActivity
        if(currentUser==null){
            Intent intent = new Intent(MusicListActivity.this,WelcomeActivity.class);
            //makes sure when you press back button you cant go back to LogInActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        //fill in our Activity with a list of music from Firebase
        FirebaseRecyclerAdapter <Music, MusicViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Music, MusicViewHolder>(
                        Music.class,
                        R.layout.music_data_layout,
                        MusicViewHolder.class,
                        musicDatabase
                ) {
                    @Override
                    protected void populateViewHolder(MusicViewHolder viewHolder, Music model, int position) {
                        viewHolder.setPosition(position);
                        viewHolder.setArtistName(model.getArtist());
                        viewHolder.setSongName(model.getTitle());
                        viewHolder.setImage(model.getImage(),getApplicationContext());
                        mainIntent.putExtra("name",model.getArtist());
                        mainIntent.putExtra("title",model.getTitle());
                        mainIntent.putExtra("image",model.getImage());
                        mainIntent.putExtra("song",model.getSong());
                        musicProgressBar.setVisibility(View.GONE);
                        if (buyplayBtn == position) {
                            viewHolder.setButtonText("Play");
                        }
                    }
                };
        musicList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class MusicViewHolder extends RecyclerView.ViewHolder{
        View view;
        int position = -1;
        public MusicViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            final Button btn = (Button) view.findViewById(R.id.buyBtn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    buyDialog.show();
//                    buyplayBtn = position;
//                    if(btn.getText().equals("Buy")){
//                        //we are buying the song
//                        new Paying().execute();
//
//                        status=5;
//                    }else {
//                        //we play the song that is already bought
//                        startActivity(mainIntent);
//                    }
                }
            });

        }

        public void setPosition(int position) {
            this.position = position;
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
        public void setButtonText(String title){
            Button button = view.findViewById(R.id.buyBtn);

            button.setText(title);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentBuy", buyplayBtn);

    }

    public class Paying extends AsyncTask<Void, String, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                //initialise the payment service
                paymentService = AfricasTalking.getPaymentService();

                //start the check out request and pass in as arguements the productName, amount and phone number
                MobileCheckoutRequest checkoutRequest = new MobileCheckoutRequest("MusicApp","KES 10","0703280748");

                paymentService.checkout(checkoutRequest, new Callback<CheckoutResponse>() {
                    @Override
                    public void onSuccess(CheckoutResponse data) {
                        buyDialog.dismiss();
                        Toast.makeText(MusicListActivity.this,data.status,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        buyDialog.dismiss();
                        Toast.makeText(MusicListActivity.this,throwable.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }



    //create a menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.list_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.logout_list:
                Intent intent = new Intent(MusicListActivity.this,WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }
    //confirm the payment and determine whether it is successful or not
    public void confirmPayment(){
        client = new OkHttpClient();
        request = new Request.Builder().url("http://192.168.137.107:30001/transaction/status").build();
        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                dialog.dismiss();
                Log.e("failure",e.getMessage());
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                dialog.dismiss();
                String status = response.body().string();
                //if user either cancels or has insufficient funds we go to game over
                if(status.equals("Success")){
                    //if it fails to pay show the message below
                    showMessage("failed");
                }else if(status.equals("Failed")){
                    //if successful add the time and player gets another chance to continue
                    showMessage("successful");

                }
            }
        });
    }
    public void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicListActivity.this,"Your payment has "+message,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //when user first gets to the activity
        if(mResume){
            mResume = false;
            Log.e("resume",String.valueOf(status));
        }else if(!mResume&&status==5) {
            //after mpesa pop up
            status = 3;
            Log.e("resume",String.valueOf(status));

            dialog.show();
            //wait for ten seconds to confirm
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmPayment();
                }
            }, 10000);
        }else{
            Log.e("resume","normal");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(status==5){
            //pause by the checkout
            Log.e("pause",String.valueOf(status));
        }
        else {
            //normal pause
            status=3;
            Log.e("pause",String.valueOf(status));
        }
    }
}
