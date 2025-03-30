package com.example.easysplit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner colorSpinner;
    private Button saveSettingsButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        colorSpinner = findViewById(R.id.colorSpinner);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);
        dbHelper = new DBHelper(this);

        // Nastavení spinneru s možnostmi barev (definované v res/values/strings.xml)
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.color_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);

        // Načtení uložené barvy z DB a nastavení spinneru
        String savedColor = dbHelper.getAppColor();
        if (savedColor != null && !savedColor.isEmpty()) {
            int pos = adapter.getPosition(savedColor);
            if (pos >= 0) {
                colorSpinner.setSelection(pos);
            }
            updateBackgroundColor(savedColor);
        }

        // Listener spinneru – při změně okamžitě aktualizuje barvu
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedColor = parent.getItemAtPosition(position).toString();
                dbHelper.saveUserSettings("", selectedColor);
                updateBackgroundColor(selectedColor);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "Nastavení uloženo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Aktualizuje pozadí a textovou barvu dle zvolené barvy.
     *
     * @param colorName Například "Černá", "Tmavě modrá", "Tmavě červená", "Tmavě zelená", "Tmavě žlutá"
     */
    private void updateBackgroundColor(String colorName) {
        int bgColor = Color.WHITE;
        switch (colorName) {
            case "Černá":
                bgColor = Color.BLACK;
                break;
            case "Tmavě modrá":
                bgColor = Color.rgb(0, 0, 139);
                break;
            case "Tmavě červená":
                bgColor = Color.rgb(139, 0, 0);
                break;
            case "Tmavě zelená":
                bgColor = Color.rgb(0, 100, 0);
                break;
            case "Tmavě žlutá":
                bgColor = Color.rgb(153, 153, 0);
                break;
        }
        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(bgColor);
        int textColor = (bgColor == Color.BLACK || bgColor == Color.rgb(0, 0, 139) ||
                bgColor == Color.rgb(139, 0, 0) || bgColor == Color.rgb(0, 100, 0) ||
                bgColor == Color.rgb(153, 153, 0)) ? Color.WHITE : Color.BLACK;
        updateTextColorRecursive(rootView, textColor);
    }

    private void updateTextColorRecursive(View view, int textColor) {
        if (view instanceof TextView) {
            ((TextView)view).setTextColor(textColor);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                updateTextColorRecursive(group.getChildAt(i), textColor);
            }
        }
    }
}
