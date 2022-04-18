package com.example.bookapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapter.AdapterComment;
import com.example.bookapp.adapter.AdapterPdfFavortie;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.DialogCommentAddBinding;
import com.example.bookapp.models.ModelComment;
import com.example.bookapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    //bookId get from Intent
    String bookId,bookTitle,bookUrl;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;

    private AdapterComment adapterComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //We need bookUrl before download
        binding.downloadBookBtn.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadBookDetail();
        loadComments();

        //increment book views count , downloads count
        MyApplication.incrementBookViewCount(bookId);


        //handle back Btn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Xu ly khi read book btn
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);
            }
        });

        //Xu ly khi download book btn
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);

                }else{
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                }

            }
        });

        //Xu ly them/xoa khoi danh sach yeu thich
        binding.favoriteBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }else{
                    if(isInMyFavorite){
                        //in favorite , remove from favorite list
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,bookId);
                    }else {
                        // not in favorite , add to favorite list
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                    }
                }
            }
        });

        //Xu ly add comment
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not log in", Toast.LENGTH_SHORT).show();
                }else{
                    addCommentDialog();
                }
            }
        });
    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelComment modelComment = ds.getValue(ModelComment.class);
                            commentArrayList.add(modelComment);
                        }
                        adapterComment = new AdapterComment(PdfDetailActivity.this,commentArrayList);
                        binding.commentRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String comment = "";
    private void addCommentDialog() {
        //inflate bind view for dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));


        //set up alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //create and show dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //handle click dismiss dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //handle click add comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data
                comment = commentAddBinding.commentEt.getText().toString().trim();
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Enter your comment", Toast.LENGTH_SHORT).show();
                }else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        progressDialog.setMessage("Adding comment ...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //Add to database
        //Books > bookId > Comments > commentId > commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //request permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
                if(isGranted){
                    MyApplication.downloadBook(this,""+bookId,""+bookTitle,""+bookUrl);
                }else{
                    Toast.makeText(PdfDetailActivity.this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String dowloadsCount = ""+snapshot.child("dowloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //requied data is loaded , show downloadBtn
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(""+categoryId,binding.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,binding.pdfView,binding.progressBar,binding.pagesTv);
                        MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,binding.sizeTv);

                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsTv.setText(dowloadsCount.replace("null","N/A"));
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if(isInMyFavorite){
                            //exists in favorite
                            binding.favoriteBookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                            binding.favoriteBookBtn.setText("Remove Favorite");
                        }else{
                            //not exists in favorite
                            binding.favoriteBookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0);
                            binding.favoriteBookBtn.setText("Add Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}