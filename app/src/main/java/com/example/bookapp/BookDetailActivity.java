package com.example.bookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityBookDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class BookDetailActivity extends AppCompatActivity {

    //view binding
    private ActivityBookDetailBinding binding;

    //pdf id, get from intent
    String bookId, bookTittle, bookUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent e.g. bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        loadBookDetails();
        //increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId);

        //handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click open to view book/pdf
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(BookDetailActivity.this, BookViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });
    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String tittle = ""+snapshot.child("tittle").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String author = ""+snapshot.child("author").getValue();
                        String isbn = ""+snapshot.child("isbn").getValue();
                        String yearPublished = ""+snapshot.child("yearPublished").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        String url = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //formatdate
                        String date = MyApplication.formatTimeStamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadBookFromUrlSinglePage(
                                ""+url,
                                ""+tittle,
                                binding.pdfView,
                                binding.progressBar
                        );
                        MyApplication.loadBookSize(
                                ""+url,
                                ""+tittle,
                                binding.sizeTv
                        );

                        //set data
                        binding.tittleTv.setText(tittle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.authorTv.setText(author.replace("null", "N/A"));
                        binding.isbnTv.setText(isbn.replace("null", "N/A"));
                        binding.yearPublishedTv.setText(yearPublished.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}