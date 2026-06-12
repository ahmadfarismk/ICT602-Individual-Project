package com.example.individualassignmentproject;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that wraps Firebase Realtime Database CRUD operations
 * for electricity bill records.
 */
public class FirebaseHelper {

    private static final String NODE_BILLS = "bills";
    private final DatabaseReference billsRef;

    public FirebaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        billsRef = database.getReference(NODE_BILLS);
    }

    /**
     * Callback interface for asynchronous Firebase operations.
     */
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    /**
     * Insert a new bill record. The Firebase push key becomes the record ID.
     */
    public void insertBill(BillRecord record, DataCallback<String> callback) {
        String key = billsRef.push().getKey();
        if (key != null) {
            record.setId(key);
            billsRef.child(key).setValue(record)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(key))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("Failed to generate key");
        }
    }

    /**
     * Fetch all bill records from Firebase.
     */
    public void getAllBills(DataCallback<List<BillRecord>> callback) {
        billsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BillRecord> bills = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    BillRecord record = child.getValue(BillRecord.class);
                    if (record != null) {
                        record.setId(child.getKey());
                        bills.add(record);
                    }
                }
                callback.onSuccess(bills);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Fetch a single bill record by its Firebase key.
     */
    public void getBillById(String id, DataCallback<BillRecord> callback) {
        billsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BillRecord record = snapshot.getValue(BillRecord.class);
                if (record != null) {
                    record.setId(snapshot.getKey());
                    callback.onSuccess(record);
                } else {
                    callback.onError("Record not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Update an existing bill record.
     */
    public void updateBill(String id, BillRecord record, DataCallback<Void> callback) {
        record.setId(id);
        billsRef.child(id).setValue(record)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Delete a bill record by its Firebase key.
     */
    public void deleteBill(String id, DataCallback<Void> callback) {
        billsRef.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
