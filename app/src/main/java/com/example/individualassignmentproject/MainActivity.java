package com.example.individualassignmentproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private TextInputLayout tilUnits;
    private TextInputEditText etUnits;
    private RadioGroup radioGroupRebate;
    private MaterialButton btnCalculate, btnHistory, btnAbout;
    private MaterialCardView cardResult;
    private TextView tvTotalCharges, tvFinalCost;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // Initialize Firebase helper
        firebaseHelper = new FirebaseHelper();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        tilUnits = findViewById(R.id.tilUnits);
        etUnits = findViewById(R.id.etUnits);
        radioGroupRebate = findViewById(R.id.radioGroupRebate);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnHistory = findViewById(R.id.btnHistory);
        btnAbout = findViewById(R.id.btnAbout);
        cardResult = findViewById(R.id.cardResult);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);

        // Calculate button click
        btnCalculate.setOnClickListener(v -> calculateBill());

        // Navigate to History
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Navigate to About
        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Validates inputs and calculates the electricity bill using tiered pricing.
     */
    private void calculateBill() {
        // Clear previous errors
        tilUnits.setError(null);

        // Validate units input
        String unitsText = etUnits.getText() != null ? etUnits.getText().toString().trim() : "";
        if (unitsText.isEmpty()) {
            tilUnits.setError(getString(R.string.error_empty_units));
            etUnits.requestFocus();
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsText);
        } catch (NumberFormatException e) {
            tilUnits.setError(getString(R.string.error_units_range));
            etUnits.requestFocus();
            return;
        }

        if (units < 1 || units > 1000) {
            tilUnits.setError(getString(R.string.error_units_range));
            etUnits.requestFocus();
            return;
        }

        // Get selected month
        String month = spinnerMonth.getSelectedItem().toString();

        // Get selected rebate percentage
        int rebatePercent = getSelectedRebate();

        // Calculate total charges using tiered pricing
        double totalCharges = calculateTieredCharges(units);

        // Calculate final cost after rebate
        double finalCost = totalCharges - (totalCharges * rebatePercent / 100.0);

        // Round to 2 decimal places
        totalCharges = Math.round(totalCharges * 100.0) / 100.0;
        finalCost = Math.round(finalCost * 100.0) / 100.0;

        // Display results
        tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", totalCharges));
        tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", finalCost));
        cardResult.setVisibility(View.VISIBLE);

        // Save to Firebase
        double finalTotalCharges = totalCharges;
        double finalFinalCost = finalCost;
        BillRecord record = new BillRecord(month, units, rebatePercent, finalTotalCharges, finalFinalCost);
        firebaseHelper.insertBill(record, new FirebaseHelper.DataCallback<String>() {
            @Override
            public void onSuccess(String key) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error saving: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Computes total charges based on tiered (block) pricing:
     * Block 1: 1-200 kWh   @ RM 0.218/kWh
     * Block 2: 201-300 kWh  @ RM 0.334/kWh
     * Block 3: 301-600 kWh  @ RM 0.516/kWh
     * Block 4: 601-1000 kWh @ RM 0.546/kWh
     */
    private double calculateTieredCharges(int units) {
        double total = 0.0;

        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else { // units <= 1000
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }

        return total;
    }

    /**
     * Gets the selected rebate percentage from the RadioGroup.
     */
    private int getSelectedRebate() {
        int selectedId = radioGroupRebate.getCheckedRadioButtonId();
        if (selectedId == R.id.rbRebate0) return 0;
        if (selectedId == R.id.rbRebate1) return 1;
        if (selectedId == R.id.rbRebate2) return 2;
        if (selectedId == R.id.rbRebate3) return 3;
        if (selectedId == R.id.rbRebate4) return 4;
        if (selectedId == R.id.rbRebate5) return 5;
        return 0; // Default
    }
}