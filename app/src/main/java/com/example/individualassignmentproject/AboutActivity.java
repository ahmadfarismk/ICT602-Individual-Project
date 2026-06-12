package com.example.individualassignmentproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

/**
 * Screen 4: About Page
 * Displays student information, copyright, GitHub link, and app usage instructions.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Setup toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // GitHub button → opens URL in browser
        MaterialButton btnGithub = findViewById(R.id.btnGithub);
        btnGithub.setOnClickListener(v -> {
            String url = getString(R.string.about_github_url);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open browser", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
