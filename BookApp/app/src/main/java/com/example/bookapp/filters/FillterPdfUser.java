package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterPdfUser;
import com.example.bookapp.models.ModelPdf;

import java.util.ArrayList;
import java.util.Locale;

public class FillterPdfUser extends Filter {
    //array list in which we want to search
    ArrayList<ModelPdf> filterList;

    //adapter in which filter need to be implementd
    AdapterPdfUser adapterPdfUser;

    public FillterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //gia tri tim kiem khong duoc null
        if(charSequence != null || charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for(int i=0; i < filterList.size(); i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filteredModels.add(filterList.get(i));
                }

            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        //apply filter changes
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        adapterPdfUser.notifyDataSetChanged();
    }
}
