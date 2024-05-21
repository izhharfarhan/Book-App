package com.example.bookapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityBookAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class BookAddActivity extends AppCompatActivity {

    //setup view binding
    private ActivityBookAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //arraylist to hold book categories
    private ArrayList<String> categoryTittleArrayList, categoryIdArrayList;

    private static final int BOOK_PICK_CODE = 1000;

    //uri of picked book
    private Uri bookUri = null;

    //TAG for debugging
    private static final String TAG = "ADD_BOOK_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadBookCategories();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //handler click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click attach book
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookPickIntent();
            }
        });

        //handle click pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        //handle click upload book
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                validateData();
            }
        });

    }


    private String tittle= "", description = "", author = "", isbn = "", yearPublished = "";
    private void validateData() {
        //step 1 validate data
        Log.d(TAG, "validateData: Validating data...");

        //getdata
        tittle = binding.tittleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        author = binding.authorEt.getText().toString().trim();
        isbn = binding.isbnEt.getText().toString().trim();
        yearPublished = binding.yearPublishedEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(tittle)){
            Toast.makeText(this, "Enter Tittle...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(author)) {
            Toast.makeText(this, "Enter Author Name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(isbn)) {
            Toast.makeText(this, "Enter ISBN Number...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(yearPublished)) {
            Toast.makeText(this, "Enter Year Published...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTittle)) {
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        } else if (bookUri==null) {
            Toast.makeText(this, "Pick Book...", Toast.LENGTH_SHORT).show();
        }else {
            //all data is valid can upload now
            uploadBookToStorage();
        }
    }

    private void uploadBookToStorage() {
        //step 2: upload book to firebase storage
        Log.d(TAG, "uploadBookToStorage: Uploading to storage...");

        //show progress dialog
        progressDialog.setMessage("uploading book...");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //path of book in firebase storage
        String filePathAndName = "Books/" + timestamp;
        //storage references
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(bookUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Book Uploaded tp storage...");
                        Log.d(TAG, "onSuccess: Getting Book Url");

                        //get book url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedBookUrl = ""+uriTask.getResult();

                        //uploaded to firebase db
                        uploadBookInfoToDb(uploadedBookUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Book upload failed due to "+e.getMessage());
                        Toast.makeText(BookAddActivity.this, "Book upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadBookInfoToDb(String uploadedBookUrl, long timestamp) {
        //step 3: upload book info to firebase db
        Log.d(TAG, "uploadBookToStorage: Uploading book info to firebase db...");

        progressDialog.setMessage("Uploading book info...");

        String uid = firebaseAuth.getUid();

        //setup data to upload, also add view count
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("tittle", ""+tittle);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("author", ""+author);
        hashMap.put("isbn", ""+isbn);
        hashMap.put("yearPublished", ""+yearPublished);
        hashMap.put("url", ""+uploadedBookUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        //db reference: DB > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded...");
                        Toast.makeText(BookAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to "+e.getMessage());
                        Toast.makeText(BookAddActivity.this, "Failed to upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookCategories(){
        Log.d(TAG, "loadBookCategories: loading book categories...");
        categoryTittleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db reference to load categories...db > categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTittleArrayList.clear(); //clear before adding data
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    //get id and tittle of category
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTittle = ""+ds.child("category").getValue();

                    //add to respective arraylist
                    categoryTittleArrayList.add(categoryTittle);
                    categoryIdArrayList.add(categoryId);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //selected category id and category tittle
    private String  selectedCategoryId, selectedCategoryTittle;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get string array of categories from array list
        String[] categoriesArray = new String[categoryTittleArrayList.size()];
        for (int i = 0; i < categoryTittleArrayList.size(); i++){
            categoriesArray[i] = categoryTittleArrayList.get(i);
            //categoryTittleArrayList.get(i) retrieves the category title from position i in the ArrayList and assigns it to the corresponding element in the array.
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        //get clicked item from list
                        selectedCategoryTittle = categoryTittleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        //set to category text view
                        binding.categoryTv.setText(selectedCategoryTittle);

                        Log.d(TAG, "onClick: Selected Category: "+selectedCategoryId+" "+selectedCategoryTittle);
                    }
                })
                .show();
    }

    private void bookPickIntent() {
        Log.d(TAG, "bookPickIntent: starting book pick intent");

        Intent intent = new Intent();
        //Specify the desired content type
        intent.setType("application/pdf");
        //Define intent action, to select content
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //Initiates the content selection activity with the application selection dialog
        startActivityForResult(Intent.createChooser(intent, "Select Book"), BOOK_PICK_CODE);
    }

    //This method is called when the activity requested for the result (startActivityForResult) is completed.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            //It is used to ensure that results are obtained from book selection activities.
            if (requestCode == BOOK_PICK_CODE){
                Log.d(TAG, "onActivityResult: BOOK Picked");

                bookUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: "+bookUri);

            }
        }else {
            Log.d(TAG, "onActivityResult: cancelled picking book");
            Toast.makeText(this, "cancelled picking book", Toast.LENGTH_SHORT).show();
        }
    }
}