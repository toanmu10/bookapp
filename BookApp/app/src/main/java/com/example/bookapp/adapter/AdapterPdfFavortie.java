package com.example.bookapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfFavoriteBinding;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AdapterPdfFavortie extends RecyclerView.Adapter<AdapterPdfFavortie.HolderPdfFavorite>{
    private Context context;

    private ArrayList<ModelPdf> pdfArrayList;

    private RowPdfFavoriteBinding binding;

    public AdapterPdfFavortie(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        ModelPdf model = pdfArrayList.get(position);

        loadBookDetails(model , holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",model.getId());
                context.startActivity(intent);
            }
        });

        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context,model.getId());
            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book Info
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String bookDescription = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();


                        //set to model
                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(bookDescription);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                        MyApplication.loadCategory(categoryId,holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,holder.pdfView,holder.progressBar,null);
                        MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,holder.sizeTv);

                        //set data to view
                        holder.titleTv.setText(bookTitle);
                        holder.desciptionTv.setText(bookDescription);
                        holder.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView sizeTv,titleTv,desciptionTv,categoryTv,dateTv;
        ImageButton removeFavBtn;

        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            sizeTv = binding.sizeTv;
            titleTv = binding.titleTv;
            desciptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            dateTv = binding.dateTv;
            removeFavBtn = binding.removeFavBtn;
        }
    }

}
