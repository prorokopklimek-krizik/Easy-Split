package com.example.easysplit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_MEMBER = 1;

    private EditText userNameEditText;
    private Spinner colorSpinner;
    private LinearLayout memberListContainer;
    private Button addMemberButton;
    private Button saveSettingsButton;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userNameEditText = findViewById(R.id.userNameEditText);
        colorSpinner = findViewById(R.id.colorSpinner);
        memberListContainer = findViewById(R.id.memberListContainer);
        addMemberButton = findViewById(R.id.addMemberButton);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        // Nastavení Spinneru s možnostmi barev (definováno v strings.xml)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.color_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);

        dbHelper = new DBHelper(this);

        // Načtení uložených nastavení a permanentních členů
        loadSettings();
        loadPermanentMembers();

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Spuštění aktivity pro přidání člena
                Intent intent = new Intent(SettingsActivity.this, AddMemberActivity.class);
                startActivityForResult(intent, REQUEST_ADD_MEMBER);
            }
        });

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    private void loadSettings() {
        String storedUserName = dbHelper.getUserName();
        String storedColor = dbHelper.getAppColor();

        if (!storedUserName.isEmpty()) {
            userNameEditText.setText(storedUserName);
        }
        if (!storedColor.isEmpty()) {
            // Nastavení výběru spinneru podle uložené barvy
            ArrayAdapter adapter = (ArrayAdapter) colorSpinner.getAdapter();
            int position = adapter.getPosition(storedColor);
            if (position >= 0) {
                colorSpinner.setSelection(position);
            }
            updateBackgroundColor(storedColor);
        }
    }

    private void loadPermanentMembers() {
        memberListContainer.removeAllViews();
        ArrayList<DBHelper.PermanentMember> members = dbHelper.getPermanentMembers();
        for (DBHelper.PermanentMember member : members) {
            TextView memberTextView = new TextView(this);
            memberTextView.setText(member.name + " - násobitel: " + member.multiplier);
            memberTextView.setPadding(16, 16, 16, 16);
            memberTextView.setTextSize(16);
            memberListContainer.addView(memberTextView);
        }
    }

    private void saveSettings() {
        String userName = userNameEditText.getText().toString().trim();
        String selectedColor = colorSpinner.getSelectedItem().toString();
        if (userName.isEmpty()) {
            Toast.makeText(this, "Vyplňte prosím jméno uživatele", Toast.LENGTH_SHORT).show();
            return;
        }
        dbHelper.saveUserSettings(userName, selectedColor);
        Toast.makeText(this, "Nastavení uloženo", Toast.LENGTH_SHORT).show();
        updateBackgroundColor(selectedColor);
    }

    /**
     * Aktualizuje pozadí obrazovky podle názvu barvy.
     * Např.: "Modrá", "Červená", "Zelená", "Žlutá"
     */
    private void updateBackgroundColor(String colorName) {
        int color = Color.WHITE;
        switch (colorName) {
            case "Černá":
                color = Color.BLACK;
                break;
            case "Tmavě modrá":
                color = Color.rgb(0, 0, 139);  // tmavě modrá
                break;
            case "Tmavě červená":
                color = Color.rgb(139, 0, 0);  // tmavě červená
                break;
            case "Tmavě zelená":
                color = Color.rgb(0, 100, 0);  // tmavě zelená
                break;
            case "Tmavě žlutá":
                color = Color.rgb(153, 153, 0);  // tmavě žlutá
                break;
        }
        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(color);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_MEMBER && resultCode == RESULT_OK && data != null) {
            String memberName = data.getStringExtra("memberName");
            int multiplier = data.getIntExtra("multiplier", 1);
            dbHelper.insertPermanentMember(memberName, multiplier);
            loadPermanentMembers();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
