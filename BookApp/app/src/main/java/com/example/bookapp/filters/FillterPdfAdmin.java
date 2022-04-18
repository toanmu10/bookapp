package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterCategory;
import com.example.bookapp.adapter.AdapterPdfAdmin;
import com.example.bookapp.models.ModelCategory;
import com.example.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FillterPdfAdmin extends Filter {
    ArrayList<ModelPdf> filterList;

    AdapterPdfAdmin adapterPdfAdmin;

    public FillterPdfAdmin(ArrayList<ModelPdf> fillterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = fillterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filterModel = new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filterModel.add(filterList.get(i));
                }
            }
            results.count = filterModel.size();
            results.values = filterModel;
        }else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)filterResults.values;

        adapterPdfAdmin.notifyDataSetChanged();
    }
}
