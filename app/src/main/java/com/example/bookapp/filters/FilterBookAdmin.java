package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterBookAdmin;
import com.example.bookapp.models.ModelBook;

import java.util.ArrayList;

public class FilterBookAdmin extends Filter {
    //arrayList in which we want to search
    ArrayList<ModelBook> filterList;
    //adapter in which filter need to be implemented
    AdapterBookAdmin adapterBookAdmin;
    //constructor
    public FilterBookAdmin(ArrayList<ModelBook> filterList, AdapterBookAdmin adapterBookAdmin) {
        this.filterList = filterList;
        this.adapterBookAdmin = adapterBookAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value shoul not be null and empty
        if (constraint != null && constraint.length() > 0){

            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelBook> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getTittle().toUpperCase().contains(constraint)){
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;

        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter change
        adapterBookAdmin.bookArrayList = (ArrayList<ModelBook>)results.values;

        //notify changes
        adapterBookAdmin.notifyDataSetChanged();

    }
}
