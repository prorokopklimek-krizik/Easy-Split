package com.example.easysplit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private Button addGroupButton;
    private LinearLayout groupListContainer;
    private DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        groupNameEditText = findViewById(R.id.groupNameEditText);
        addGroupButton = findViewById(R.id.addGroupButton);
        groupListContainer = findViewById(R.id.groupListContainer);
        dbHelper = new DBHelper(this);

        addGroupButton.setOnClickListener(view -> {
            String groupName = groupNameEditText.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Zadejte název skupiny", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.insertGroup(groupName);
            groupNameEditText.setText("");
            loadGroups();
        });

        loadGroups();
    }

    private void loadGroups() {
        groupListContainer.removeAllViews();
        ArrayList<DBHelper.Group> groups = dbHelper.getGroups();
        for (final DBHelper.Group group : groups) {
            // Každá skupina bude zobrazena v kulatém "card" stylu
            LinearLayout groupItemLayout = new LinearLayout(this);
            groupItemLayout.setOrientation(LinearLayout.VERTICAL);
            groupItemLayout.setPadding(16, 16, 16, 16);
            groupItemLayout.setBackgroundResource(R.drawable.rounded_list_item);

            TextView groupNameTextView = new TextView(this);
            groupNameTextView.setText(group.name);
            groupNameTextView.setTextSize(20);
            groupNameTextView.setTextColor(getResources().getColor(android.R.color.white));
            groupItemLayout.addView(groupNameTextView);

            double groupTotal = computeGroupTotalExpense(group.id);
            TextView groupTotalTextView = new TextView(this);
            groupTotalTextView.setText("Celková útrata: " + groupTotal + " Kč");
            groupTotalTextView.setTextSize(14);
            groupTotalTextView.setTextColor(getResources().getColor(android.R.color.white));
            groupItemLayout.addView(groupTotalTextView);

            groupItemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                intent.putExtra("groupId", group.id);
                intent.putExtra("groupName", group.name);
                startActivity(intent);
            });

            groupListContainer.addView(groupItemLayout);
        }
    }

    private double computeGroupTotalExpense(int groupId) {
        double total = 0;
        for (DBHelper.GroupMember member : dbHelper.getGroupMembers(groupId)) {
            total += computeTotalExpenseForMember(member.gmId);
        }
        return total;
    }

    private double computeTotalExpenseForMember(int memberId) {
        double total = 0;
        ArrayList<DBHelper.Expense> expenses = dbHelper.getExpensesForMember(memberId);
        if (expenses != null) {
            for (DBHelper.Expense exp : expenses) {
                total += exp.amount;
            }
        }
        return total;
    }
}
