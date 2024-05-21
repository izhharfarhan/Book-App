package com.example.bookapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityBookEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BookEditActivity extends AppCompatActivity {

    //view binding
    private ActivityBookEditBinding binding;

    //bookId get from intent started from adapterBookAdmin
    private String bookId;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTittleArrayList, categoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //bookId get from intent started from AdapterBookAdmin
        bookId = getIntent().getStringExtra("bookId");

        //set up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        //handle click pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        //handle click go to previous screen
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click begin upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }



    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading info...");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectedCategoryId = ""+snapshot.child("categpryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String tittle = ""+snapshot.child("tittle").getValue();
                        String author = ""+snapshot.child("author").getValue();
                        String isbn = ""+snapshot.child("isbn").getValue();
                        String yearPublished = ""+snapshot.child("yearPublished").getValue();
                        //set to view
                        binding.tittleEt.setText(tittle);
                        binding.descriptionEt.setText(description);
                        binding.authorEt.setText(author);
                        binding.isbnEt.setText(isbn);
                        binding.yearPublishedEt.setText(yearPublished);

                        Log.d(TAG, "onDataChange: Loading book category info...");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get category
                                        String category = ""+snapshot.child("category").getValue();
                                        //set to category text view
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String tittle = "", description = "", author = "", isbn = "", yearPublished = "";

    private void validateData() {
        //GET DATA
        tittle = binding.tittleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        author = binding.authorEt.getText().toString().trim();
        isbn = binding.isbnEt.getText().toString().trim();
        yearPublished = binding.yearPublishedEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(tittle)){
            Toast.makeText(this, "Enter Tittle...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(author)) {
            Toast.makeText(this, "Enter Author Name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(isbn)) {
            Toast.makeText(this, "Enter ISBN...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(yearPublished)){
            Toast.makeText(this, "Enter Year Published...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show();
        }else {
            // Check if category is not changed
            if (selectedCategoryId.equals("null")) {
                Toast.makeText(this, "Change the category", Toast.LENGTH_SHORT).show();
            } else {
                updateBook();
            }
        }
    }

    private void updateBook() {
        Log.d(TAG, "updateBook: Starting updating book info to db...");

        //show progress
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("tittle", ""+tittle);
        hashMap.put("description", ""+description);
        hashMap.put("author", ""+author);
        hashMap.put("isbn", ""+isbn);
        hashMap.put("yearPublished", ""+yearPublished);
        hashMap.put("categoryId", ""+selectedCategoryId);


        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book Updated...");
                        progressDialog.dismiss();
                        Toast.makeText(BookEditActivity.this, "Book Info Updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(BookEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String selectedCategoryId = "", selectedCategoryTittle = "";

    private void categoryDialog(){
        //make string array from arraylist of string
        String[] categoriesArray = new String[categoryTittleArrayList.size()];
        for (int i = 0; i < categoryTittleArrayList.size(); i++){
            categoriesArray[i] = categoryTittleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTittle = categoryTittleArrayList.get(which);

                        //set to textview
                        binding.categoryTv.setText(selectedCategoryTittle);
                    }
                })
                .show();

    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...");

        categoryIdArrayList = new ArrayList<>();
        categoryTittleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTittleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTittleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: "+id);
                    Log.d(TAG, "onDataChange: Category: "+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}