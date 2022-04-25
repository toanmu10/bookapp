package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.example.bookapp.Constants;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PdfViewActivity extends AppCompatActivity {
    private ActivityPdfViewBinding binding;

    private String bookId;

    //test time
    TextView timerText;
    Button stopStartButton;

    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    boolean timerStarted = false;
    //

    //test zoom

//    private static final String TAG1 = PdfViewActivity.class.getSimpleName();
//    public static final String PDF_FILE = "TamCam.pdf";


    PDFView pdfView ;
//    Integer pageNumber;
//    String pdfFileName;
//
//    Integer currentPageNumber;
//    Integer savedPage;

    private Button btnZoomIn;
    private Button btnZoomOut ;
//    private PDFView pdfView ;
    private ZoomControls simpleZoomControls;
    //

    private static final String TAG = "PDF_VIEW_TAG";

    private int mCurrentPage = 0;
    private final static String KEY_CURRENT_PAGE = "current_page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        timerText = (TextView) findViewById(R.id.timerText);
        stopStartButton = (Button) findViewById(R.id.startStopButton);

        timer = new Timer();



//        binding.luuBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SharedPreferences mySharedPreferences = getPreferences(Context.MODE_PRIVATE);
//
//                savedPage = mySharedPreferences.getInt("retrievedPage",0);
//
//                pageNumber = savedPage;
//
//
//
//                pdfView = (PDFView)findViewById(R.id.pdfView);
//                displayFromAsset(PDF_FILE);
//
//            }
//        });

        //test


        simpleZoomControls = (ZoomControls) findViewById(R.id.simpleZoomControl); // initiate a ZoomControls
//        simpleZoomControls.hide(); // initially hide ZoomControls from the screen

        pdfView = (PDFView)findViewById(binding.pdfView.getId());


        simpleZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calculate current scale x and y value of ImageView
                float x = pdfView.getScaleX();
                float y = pdfView.getScaleY();
                // set increased value of scale x and y to perform zoom in functionality
                pdfView.setScaleX((float) (x + 1));
                pdfView.setScaleY((float) (y + 1));
                // display a toast to show Zoom In Message on Screen
                Toast.makeText(getApplicationContext(),"Zoom In",Toast.LENGTH_SHORT).show();
                // hide the ZoomControls from the screen
//                simpleZoomControls.hide();
            }
        });
        // perform setOnZoomOutClickListener event on ZoomControls
        simpleZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calculate current scale x and y value of ImageView
                float x = pdfView.getScaleX();
                float y = pdfView.getScaleY();
                // set decreased value of scale x and y to perform zoom out functionality
                pdfView.setScaleX((float) (x - 1));
                pdfView.setScaleY((float) (y - 1));
                // display a toast to show Zoom Out Message on Screen
                Toast.makeText(getApplicationContext(),"Zoom Out",Toast.LENGTH_SHORT).show();
                // hide the ZoomControls from the screen
//                simpleZoomControls.hide();
            }
        });
        //

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: BookId :"+bookId);

        loadBookDetails();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

//
    //}
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        SharedPreferences mySharedPreferences = getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor myEditor = mySharedPreferences.edit();
//
//
//        savedPage = pdfView.getCurrentPage();
//        myEditor.putInt("retrievedPage",savedPage);
//        myEditor.apply();
//
//
//    }




    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get PDF Url from database");
        //Step 1 : Get bookUrl using bookId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get bookUrl
                        String pdfUrl = ""+snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: PDF Url: "+pdfUrl);

                        //Step 2 : Load pdf using that url from Firebase storage
                        loadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage");
        
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //load pdf book using bytes
                        binding.pdfView.fromBytes(bytes)
                                .swipeHorizontal(false) // set false to scroll vertical
                                .defaultPage(mCurrentPage)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set current and total page in toolbar Text view subtitle
                                        int currentpage = page + 1;
                                        binding.toolbarSubtitleTv.setText(currentpage+"/"+pageCount);
                                        Log.d(TAG, "onPageChanged: "+currentpage+"/"+pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: "+t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mCurrentPage = Integer.parseInt(String.valueOf(binding.pdfView.getCurrentPage())) ;
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mCurrentPage = Integer.parseInt(String.valueOf(binding.pdfView.getCurrentPage()));
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        binding.pdfView.jumpTo(mCurrentPage);
//    }

    public void resetTapped(View view)
    {
        AlertDialog.Builder resetAlert = new AlertDialog.Builder(this);
        resetAlert.setTitle("Reset Timer");
        resetAlert.setMessage("Are you sure you want to reset the timer?");
        resetAlert.setPositiveButton("Reset", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(timerTask != null)
                {
                    timerTask.cancel();
                    setButtonUI("START", R.color.black);
                    time = 0.0;
                    timerStarted = false;
                    timerText.setText(formatTime(0,0,0));

                }
            }
        });

        resetAlert.setNeutralButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                //do nothing
            }
        });

        resetAlert.show();

    }

    public void startStopTapped(View view)
    {
        if(timerStarted == false)
        {
            timerStarted = true;
            setButtonUI("STOP", R.color.black);

            startTimer();
        }
        else
        {
            timerStarted = false;
            setButtonUI("START", R.color.black);

            timerTask.cancel();
        }
    }

    private void setButtonUI(String start, int color)
    {
        stopStartButton.setText(start);
        stopStartButton.setTextColor(ContextCompat.getColor(this, color));
    }

    private void startTimer()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        timerText.setText(getTimerText());
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }


    private String getTimerText()
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }


}