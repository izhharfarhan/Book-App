package com.example.bookapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapters.AdapterBookAdmin;
import com.example.bookapp.databinding.ActivityBookListAdminBinding;
import com.example.bookapp.models.ModelBook;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookListAdminActivity extends AppCompatActivity {
    //view binding
    private ActivityBookListAdminBinding binding;

//array list to hold list of data of type modelbook
    private ArrayList<ModelBook> bookArrayList;

    //adapter
    private AdapterBookAdmin adapterBookAdmin;

    private String categoryId, categoryTittle;

    private static final String TAG = "BOOK_LIST_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTittle = intent.getStringExtra("categoryTittle");

        // If category data is available, set the subtitle
        if (categoryTittle != null && !categoryTittle.isEmpty()) {
            binding.subtitleTv.setText(categoryTittle);
        }


        loadBookList();

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //search as an when user type each letter
                try {
                    adapterBookAdmin.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadBookList() {
        //init list before adding data
        bookArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelBook model = ds.getValue(ModelBook.class);
                            //add to list
                            bookArrayList.add(model);

                            Log.d(TAG, "onDataChange: "+model.getId()+" "+model.getTittle());
                        }
                        //set up adapter
                        adapterBookAdmin = new AdapterBookAdmin(BookListAdminActivity.this, bookArrayList);
                        binding.bookRv.setAdapter(adapterBookAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}