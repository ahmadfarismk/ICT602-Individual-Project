package com.example.individualassignmentproject;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * Screen 3: Detail / Record Page
 * Displays full details of a bill record with options to edit or delete.
 */
public class DetailActivity extends AppCompatActivity {

    // View mode
    private TextView tvDetailMonth, tvDetailUnits, tvDetailRebate;
    private TextView tvDetailTotalCharges, tvDetailFinalCost;
    private View layoutViewButtons, layoutEditButtons;
    private MaterialButton btnEdit, btnDelete, btnSave, btnCancel;

    // Edit mode
    private Spinner spinnerDetailMonth;
    private TextInputLayout tilDetailUnits;
    private TextInputEditText etDetailUnits;
    private RadioGroup radioGroupDetailRebate;

    private FirebaseHelper firebaseHelper;
    private String billId;
    private BillRecord currentRecord;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase
        firebaseHelper = new FirebaseHelper();

        // Initialize view-mode views
        tvDetailMonth = findViewById(R.id.tvDetailMonth);
        tvDetailUnits = findViewById(R.id.tvDetailUnits);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailTotalCharges = findViewById(R.id.tvDetailTotalCharges);
        tvDetailFinalCost = findViewById(R.id.tvDetailFinalCost);
        layoutViewButtons = findViewById(R.id.layoutViewButtons);
        layoutEditButtons = findViewById(R.id.layoutEditButtons);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize edit-mode views
        spinnerDetailMonth = findViewById(R.id.spinnerDetailMonth);
        tilDetailUnits = findViewById(R.id.tilDetailUnits);
        etDetailUnits = findViewById(R.id.etDetailUnits);
        radioGroupDetailRebate = findViewById(R.id.radioGroupDetailRebate);

        // Get bill ID from intent
        billId = getIntent().getStringExtra("bill_id");
        if (billId == null) {
            Toast.makeText(this, "Error: No bill ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load record
        loadRecord();

        // Button listeners
        btnEdit.setOnClickListener(v -> enterEditMode());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> exitEditMode());
    }

    /**
     * Loads the bill record from Firebase and displays it.
     */
    private void loadRecord() {
        firebaseHelper.getBillById(billId, new FirebaseHelper.DataCallback<BillRecord>() {
            @Override
            public void onSuccess(BillRecord data) {
                currentRecord = data;
                displayRecord();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailActivity.this,
                        "Error loading record: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Displays the record data in view mode.
     */
    private void displayRecord() {
        tvDetailMonth.setText(currentRecord.getMonth());
        tvDetailUnits.setText(String.format(Locale.getDefault(), "%d kWh", currentRecord.getUnits()));
        tvDetailRebate.setText(String.format(Locale.getDefault(), "%d%%", currentRecord.getRebate()));
        tvDetailTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", currentRecord.getTotalCharges()));
        tvDetailFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", currentRecord.getFinalCost()));
    }

    /**
     * Switches to edit mode — show input widgets, hide text views.
     */
    private void enterEditMode() {
        isEditMode = true;

        // Show edit inputs
        tvDetailMonth.setVisibility(View.GONE);
        spinnerDetailMonth.setVisibility(View.VISIBLE);

        tvDetailUnits.setVisibility(View.GONE);
        tilDetailUnits.setVisibility(View.VISIBLE);

        tvDetailRebate.setVisibility(View.GONE);
        radioGroupDetailRebate.setVisibility(View.VISIBLE);

        layoutViewButtons.setVisibility(View.GONE);
        layoutEditButtons.setVisibility(View.VISIBLE);

        // Pre-fill with current values
        setSpinnerToMonth(currentRecord.getMonth());
        etDetailUnits.setText(String.valueOf(currentRecord.getUnits()));
        setRebateRadioButton(currentRecord.getRebate());
    }

    /**
     * Switches back to view mode.
     */
    private void exitEditMode() {
        isEditMode = false;

        tvDetailMonth.setVisibility(View.VISIBLE);
        spinnerDetailMonth.setVisibility(View.GONE);

        tvDetailUnits.setVisibility(View.VISIBLE);
        tilDetailUnits.setVisibility(View.GONE);

        tvDetailRebate.setVisibility(View.VISIBLE);
        radioGroupDetailRebate.setVisibility(View.GONE);

        layoutViewButtons.setVisibility(View.VISIBLE);
        layoutEditButtons.setVisibility(View.GONE);

        tilDetailUnits.setError(null);
    }

    /**
     * Validates inputs, recalculates, and saves the updated record to Firebase.
     */
    private void saveChanges() {
        tilDetailUnits.setError(null);

        // Validate units
        String unitsText = etDetailUnits.getText() != null ? etDetailUnits.getText().toString().trim() : "";
        if (unitsText.isEmpty()) {
            tilDetailUnits.setError(getString(R.string.error_empty_units));
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsText);
        } catch (NumberFormatException e) {
            tilDetailUnits.setError(getString(R.string.error_units_range));
            return;
        }

        if (units < 1 || units > 1000) {
            tilDetailUnits.setError(getString(R.string.error_units_range));
            return;
        }

        String month = spinnerDetailMonth.getSelectedItem().toString();
        int rebate = getSelectedEditRebate();

        // Recalculate
        double totalCharges = calculateTieredCharges(units);
        double finalCost = totalCharges - (totalCharges * rebate / 100.0);
        totalCharges = Math.round(totalCharges * 100.0) / 100.0;
        finalCost = Math.round(finalCost * 100.0) / 100.0;

        // Update record
        BillRecord updated = new BillRecord(month, units, rebate, totalCharges, finalCost);
        firebaseHelper.updateBill(billId, updated, new FirebaseHelper.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                currentRecord = updated;
                currentRecord.setId(billId);
                displayRecord();
                exitEditMode();
                Toast.makeText(DetailActivity.this,
                        getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailActivity.this,
                        "Error updating: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation dialog before deleting.
     */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_message))
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    firebaseHelper.deleteBill(billId, new FirebaseHelper.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Toast.makeText(DetailActivity.this,
                                    getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(DetailActivity.this,
                                    "Error deleting: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    /**
     * Sets the month spinner to the given month name.
     */
    private void setSpinnerToMonth(String month) {
        String[] months = getResources().getStringArray(R.array.months_array);
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                spinnerDetailMonth.setSelection(i);
                break;
            }
        }
    }

    /**
     * Sets the rebate radio button to the given percentage.
     */
    private void setRebateRadioButton(int rebate) {
        switch (rebate) {
            case 0: radioGroupDetailRebate.check(R.id.rbDetailRebate0); break;
            case 1: radioGroupDetailRebate.check(R.id.rbDetailRebate1); break;
            case 2: radioGroupDetailRebate.check(R.id.rbDetailRebate2); break;
            case 3: radioGroupDetailRebate.check(R.id.rbDetailRebate3); break;
            case 4: radioGroupDetailRebate.check(R.id.rbDetailRebate4); break;
            case 5: radioGroupDetailRebate.check(R.id.rbDetailRebate5); break;
            default: radioGroupDetailRebate.check(R.id.rbDetailRebate0); break;
        }
    }

    /**
     * Gets the selected rebate from the edit-mode RadioGroup.
     */
    private int getSelectedEditRebate() {
        int selectedId = radioGroupDetailRebate.getCheckedRadioButtonId();
        if (selectedId == R.id.rbDetailRebate0) return 0;
        if (selectedId == R.id.rbDetailRebate1) return 1;
        if (selectedId == R.id.rbDetailRebate2) return 2;
        if (selectedId == R.id.rbDetailRebate3) return 3;
        if (selectedId == R.id.rbDetailRebate4) return 4;
        if (selectedId == R.id.rbDetailRebate5) return 5;
        return 0;
    }

    /**
     * Tiered pricing calculation (same logic as MainActivity).
     */
    private double calculateTieredCharges(int units) {
        double total = 0.0;
        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }
        return total;
    }
}
