package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterCategory;
import com.example.bookapp.models.ModelCategory;

import java.util.ArrayList;

public class FillterCategory extends Filter {
    ArrayList<ModelCategory> filterList;

    AdapterCategory adapterCategory;

    public FillterCategory(ArrayList<ModelCategory> fillterList, AdapterCategory adapterCategory) {
        this.filterList = fillterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelCategory> filterModel = new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getCategory().toUpperCase().contains(charSequence)){
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

        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)filterResults.values;

        adapterCategory.notifyDataSetChanged();
    }
}
