package com.example.bookapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.BookDetailActivity;
import com.example.bookapp.BookEditActivity;
import com.example.bookapp.MyApplication;
import com.example.bookapp.databinding.RowBookAdminBinding;
import com.example.bookapp.filters.FilterBookAdmin;
import com.example.bookapp.models.ModelBook;
import com.joanzapata.pdfview.PDFView;

import java.util.ArrayList;

public class AdapterBookAdmin extends RecyclerView.Adapter<AdapterBookAdmin.HolderBookAdmin> implements Filterable {
    //context
    private Context context;
    //array list to hold list of data of model book
    public ArrayList<ModelBook> bookArrayList, filterList;

    //view binding row_book_admin.xml
    private RowBookAdminBinding binding;
    
    private FilterBookAdmin filter;

    private static final String TAG = "BOOK_ADAPTER_TAG";

    //progress
    private ProgressDialog progressDialog;

    //constructor
    public AdapterBookAdmin(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderBookAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind layout using view binding
        binding = RowBookAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBookAdmin(binding.getRoot());
    }

    //fill data to viewHolder
    @Override
    public void onBindViewHolder(@NonNull AdapterBookAdmin.HolderBookAdmin holder, int position) {
        //Get data, Set data, handle clicks etc

        //get data
        ModelBook model = bookArrayList.get(position);
        String bookId = model.getId();
        String categoryId = model.getCategoryId();
        String tittle = model.getTittle();
        String description = model.getDescription();
        String bookUrl = model.getUrl();
        long timestamp = model.getTimestamp();
        //we need to convert timestamp to dd/MM/yyyy format
        String formattedDate = MyApplication.formatTimeStamp(timestamp);

        //set data
        holder.tittleTv.setText(tittle);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //we will needthese functions many time,so insteady of writing again and again move them to MyApplication class and make static to use later
        //load furniture details like category, pdf from url, pdf size in separate function
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv);
        MyApplication.loadBookFromUrlSinglePage(
                ""+bookUrl,
                ""+tittle,
                holder.pdfView,
                holder.progressBar
                );
        MyApplication.loadBookSize(
                ""+bookUrl,
                ""+tittle,
                holder.sizeTv
        );

        //handle click, show dialog with options 1. edit 2. delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        //handle click, open pdf details page, pass pdf/book id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelBook model, HolderBookAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTittle = model.getTittle();

        // options to show in dialog
        String[] options = {"Edit", "Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options!")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog options click
                        if (which == 0){
                            //edit clicked, open new activity to edit the book info
                            Intent intent = new Intent(context, BookEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        } else if (which == 1) {
                            //delete clicked
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTittle
                            );
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size(); //return number of records | list size
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterBookAdmin(filterList,this);
        }
        return filter;
    }

    //view holder class for row book admin xml
    class HolderBookAdmin extends RecyclerView.ViewHolder{

        //ui views of row_book_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView tittleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderBookAdmin(@NonNull View itemView) {
            super(itemView);

            //init ui views
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            tittleTv = binding.tittleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
