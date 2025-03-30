package com.example.easysplit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private Button addGroupButton;
    private LinearLayout groupListContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nastavení toolbaru s tlačítkem nastavení
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        groupNameEditText = findViewById(R.id.groupNameEditText);
        addGroupButton = findViewById(R.id.addGroupButton);
        groupListContainer = findViewById(R.id.groupListContainer);
        dbHelper = new DBHelper(this);

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = groupNameEditText.getText().toString().trim();
                if (groupName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Zadejte název skupiny", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.insertGroup(groupName);
                groupNameEditText.setText("");
                loadGroups();
            }
        });

        loadGroups();
    }

    private void loadGroups() {
        groupListContainer.removeAllViews();
        ArrayList<DBHelper.Group> groups = dbHelper.getGroups();
        for (final DBHelper.Group group : groups) {
            // Dynamicky spočítáme celkovou útratu pro danou skupinu
            double groupTotal = computeGroupTotalExpense(group.id);

            // Vytvoříme kontejner pro jednu skupinu
            LinearLayout groupItemLayout = new LinearLayout(this);
            groupItemLayout.setOrientation(LinearLayout.VERTICAL);
            groupItemLayout.setPadding(16, 16, 16, 16);
            groupItemLayout.setBackgroundColor(0xFF808080); // šedé pozadí

            // Název skupiny – velký, bílý text
            TextView groupNameTextView = new TextView(this);
            groupNameTextView.setText(group.name);
            groupNameTextView.setTextSize(20);
            groupNameTextView.setTextColor(0xFFFFFFFF);
            groupItemLayout.addView(groupNameTextView);

            // Celková útrata – menší, světle šedý text
            TextView groupTotalTextView = new TextView(this);
            groupTotalTextView.setText("Celková útrata: " + groupTotal + " Kč");
            groupTotalTextView.setTextSize(14);
            groupTotalTextView.setTextColor(0xFFDDDDDD);
            groupItemLayout.addView(groupTotalTextView);

            // Kliknutí na blok otevře detail skupiny (např. GroupActivity)
            groupItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                    intent.putExtra("groupId", group.id);
                    intent.putExtra("groupName", group.name);
                    startActivity(intent);
                }
            });

            groupListContainer.addView(groupItemLayout);
        }
    }

    /**
     * Vypočítá celkovou útratu pro danou skupinu tím, že sečte utraty všech členů, kteří patří do této skupiny.
     * @param groupId ID skupiny
     * @return součet útrat
     */
    private double computeGroupTotalExpense(int groupId) {
        double total = 0;
        List<DBHelper.GroupMember> members = dbHelper.getGroupMembers(groupId);
        for (DBHelper.GroupMember member : members) {
            ArrayList<DBHelper.Expense> expenses = dbHelper.getExpensesForMember(member.gmId);
            if (expenses != null) {
                for (DBHelper.Expense expense : expenses) {
                    total += expense.amount;
                }
            }
        }
        return total;
    }
}
