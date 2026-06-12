package com.example.individualassignmentproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 2: History / List View Page
 * Displays a scrollable list of all saved bill records.
 * Each item shows Month + Final Cost and is clickable to open the Detail page.
 */
public class HistoryActivity extends AppCompatActivity {

    private ListView listViewHistory;
    private TextView tvEmptyHistory;
    private HistoryAdapter adapter;
    private List<BillRecord> billList;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Setup toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        listViewHistory = findViewById(R.id.listViewHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        // Initialize data
        firebaseHelper = new FirebaseHelper();
        billList = new ArrayList<>();
        adapter = new HistoryAdapter(this, billList);
        listViewHistory.setAdapter(adapter);

        // Handle item click → open Detail page
        listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
            BillRecord record = billList.get(position);
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("bill_id", record.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }

    /**
     * Loads all bills from Firebase and updates the list.
     */
    private void loadBills() {
        firebaseHelper.getAllBills(new FirebaseHelper.DataCallback<List<BillRecord>>() {
            @Override
            public void onSuccess(List<BillRecord> data) {
                billList.clear();
                billList.addAll(data);
                adapter.notifyDataSetChanged();

                // Show/hide empty state
                if (billList.isEmpty()) {
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                    listViewHistory.setVisibility(View.GONE);
                } else {
                    tvEmptyHistory.setVisibility(View.GONE);
                    listViewHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HistoryActivity.this,
                        "Error loading bills: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
