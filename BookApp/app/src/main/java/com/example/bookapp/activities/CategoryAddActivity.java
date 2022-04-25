package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {
    private ActivityCategoryAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Pelase Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        //Xu ly mut quay lai
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }




//    private String category="";
//    private void validateData() {
//        category = binding.categoryEt.getText().toString().trim();
//        if(TextUtils.isEmpty(category)){
//            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
//        }
//
//        else{
//            addCategoryFirebase();
//        }
//    }
private String category="";
    private void validateData() {
        category = binding.categoryEt.getText().toString().trim();
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
        }

        else{
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = rootRef.child("categories");

//            String EditText_category = binding.categoryEt.getText().toString();
            categoryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String server_data_Cate = snapshot.getValue(String.class);
                        if (category.equals(server_data_Cate))
                            Toast.makeText(CategoryAddActivity.this, "Successfully uploaded ...", Toast.LENGTH_SHORT).show();
                        addCategoryFirebase();

                    } else {
                        Toast.makeText(CategoryAddActivity.this, "Trung roi ...", Toast.LENGTH_SHORT).show();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException(); // never ignore errors
                }
            });


        }
    }





    private void addCategoryFirebase() {
        progressDialog.setMessage("Adding category ...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Category add successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}