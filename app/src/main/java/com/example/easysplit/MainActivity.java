package com.example.easysplit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private TextView greetingTextView;
    private EditText groupNameEditText;
    private Button addGroupButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        // Nastavení toolbaru
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Tlačítko nastavení
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        greetingTextView = findViewById(R.id.greetingTextView);
        groupNameEditText = findViewById(R.id.groupNameEditText);
        addGroupButton = findViewById(R.id.addGroupButton);

        // Načtení uživatelského jména z DB a nastavení pozdravu
        String userName = dbHelper.getUserName();
        if (!userName.isEmpty()) {
            greetingTextView.setText("Vítejte, " + userName + "!");
        } else {
            greetingTextView.setText("Vítejte!");
        }

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = groupNameEditText.getText().toString().trim();
                if (groupName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Zadejte název skupiny", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Přesměruj do GroupActivity s předaným názvem skupiny
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            }
        });
    }
}
