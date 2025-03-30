package com.example.easysplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private TextView settlementSummaryTextView;
    private LinearLayout memberListContainer;
    private Button addMemberButton;
    private DBHelper dbHelper;

    private int groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = findViewById(R.id.groupToolbar);
        setSupportActionBar(toolbar);

        // Nastavení titulu toolbaru z dat z Intentu
        groupId = getIntent().getIntExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");
        if (groupName != null && !groupName.isEmpty()) {
            getSupportActionBar().setTitle(groupName);
        }

        settlementSummaryTextView = findViewById(R.id.settlementSummaryTextView);
        memberListContainer = findViewById(R.id.memberListContainer);
        addMemberButton = findViewById(R.id.addMemberButton);
        dbHelper = new DBHelper(this);

        // Kliknutí na toolbar (titulek) spustí dialog pro úpravu názvu skupiny
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditGroupNameDialog();
            }
        });

        // Tlačítko pro přidání člena
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMemberDialog();
            }
        });

        loadGroupMembers();
        updateSettlement();
    }

    private void showEditGroupNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upravit název skupiny");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(groupName);
        builder.setView(input);

        builder.setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    groupName = newName;
                    getSupportActionBar().setTitle(newName);
                    // Aktualizaci názvu skupiny v DB lze doplnit, pokud je potřeba.
                } else {
                    Toast.makeText(GroupActivity.this, "Název nesmí být prázdný", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Zrušit", null);
        builder.show();
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Přidat člena");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Jméno člena");
        layout.addView(nameInput);

        final EditText multiplierInput = new EditText(this);
        multiplierInput.setHint("Násobitel (např. 1)");
        multiplierInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(multiplierInput);

        builder.setView(layout);

        builder.setPositiveButton("Přidat", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString().trim();
                String multStr = multiplierInput.getText().toString().trim();
                if (name.isEmpty() || multStr.isEmpty()) {
                    Toast.makeText(GroupActivity.this, "Jméno a násobitel jsou povinné", Toast.LENGTH_SHORT).show();
                    return;
                }
                int multiplier = Integer.parseInt(multStr);
                dbHelper.insertGroupMember(groupId, name, multiplier);
                Toast.makeText(GroupActivity.this, "Člen přidán", Toast.LENGTH_SHORT).show();
                loadGroupMembers();
                updateSettlement();
            }
        });
        builder.setNegativeButton("Zrušit", null);
        builder.show();
    }

    private void loadGroupMembers() {
        memberListContainer.removeAllViews();
        List<DBHelper.GroupMember> members = dbHelper.getGroupMembers(groupId);
        for (final DBHelper.GroupMember member : members) {
            LinearLayout memberRow = new LinearLayout(this);
            memberRow.setOrientation(LinearLayout.VERTICAL);
            memberRow.setPadding(16, 16, 16, 16);
            memberRow.setBackgroundColor(0xFFCCCCCC);

            // Jméno člena
            TextView nameTextView = new TextView(this);
            nameTextView.setText(member.name);
            nameTextView.setTextSize(20);
            nameTextView.setTextColor(0xFF000000);
            memberRow.addView(nameTextView);

            // Násobitel
            TextView multiplierTextView = new TextView(this);
            multiplierTextView.setText("Násobitel: " + member.multiplier);
            multiplierTextView.setTextSize(14);
            multiplierTextView.setTextColor(0xFF555555);
            memberRow.addView(multiplierTextView);

            // Celková útrata
            double totalExpense = computeTotalExpenseForMember(member.gmId);
            TextView totalTextView = new TextView(this);
            totalTextView.setText("Celková útrata: " + totalExpense + " Kč");
            totalTextView.setTextSize(14);
            totalTextView.setTextColor(0xFF555555);
            memberRow.addView(totalTextView);

            // Řádek s tlačítky Edit a Smazat
            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);

            Button editButton = new Button(this);
            editButton.setText("Edit");
            buttonRow.addView(editButton);

            Button deleteButton = new Button(this);
            deleteButton.setText("Smazat");
            buttonRow.addView(deleteButton);

            memberRow.addView(buttonRow);

            // Kliknutí na tlačítko Edit vyvolá dialog pro úpravu člena
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditMemberDialog(member);
                }
            });

            // Kliknutí na tlačítko Smazat odstraní člena a aktualizuje seznam
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.deleteGroupMember(member.gmId);
                    Toast.makeText(GroupActivity.this, "Člen smazán", Toast.LENGTH_SHORT).show();
                    loadGroupMembers();
                    updateSettlement();
                }
            });

            // Kliknutí na zbytek řádku otevře detail člena
            memberRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GroupActivity.this, MemberDetailActivity.class);
                    intent.putExtra("memberId", member.gmId);
                    intent.putExtra("memberName", member.name);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                }
            });

            memberListContainer.addView(memberRow);
        }
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

    private void updateSettlement() {
        List<DBHelper.GroupMember> members = dbHelper.getGroupMembers(groupId);
        String settlementText = computeSettlements(members);
        settlementSummaryTextView.setText(settlementText);
    }

    private String computeSettlements(List<DBHelper.GroupMember> members) {
        class Settlement {
            String name;
            double diff;
            int multiplier;
            Settlement(String name, double diff, int multiplier) {
                this.name = name;
                this.diff = diff;
                this.multiplier = multiplier;
            }
        }
        List<Settlement> settlements = new ArrayList<>();
        double totalExpense = 0;
        int totalWeight = 0;
        for (DBHelper.GroupMember m : members) {
            double paid = computeTotalExpenseForMember(m.gmId);
            totalExpense += paid;
            totalWeight += m.multiplier;
            settlements.add(new Settlement(m.name, paid, m.multiplier));
        }
        if (totalWeight == 0) return "Žádní členové";
        double averageShare = totalExpense / totalWeight;
        for (Settlement s : settlements) {
            s.diff = s.diff - (s.multiplier * averageShare);
        }
        List<Settlement> debtors = new ArrayList<>();
        List<Settlement> creditors = new ArrayList<>();
        for (Settlement s : settlements) {
            if (s.diff < -0.01) debtors.add(s);
            else if (s.diff > 0.01) creditors.add(s);
        }
        StringBuilder result = new StringBuilder();
        for (Settlement debtor : debtors) {
            double owe = -debtor.diff;
            for (Settlement creditor : creditors) {
                if (owe <= 0) break;
                if (creditor.diff <= 0) continue;
                double pay = Math.min(owe, creditor.diff);
                result.append(debtor.name)
                        .append(" má zaplatit ")
                        .append(creditor.name)
                        .append(": ")
                        .append(String.format("%.2f", pay))
                        .append(" Kč\n");
                debtor.diff += pay;
                creditor.diff -= pay;
                owe -= pay;
            }
        }
        if (result.length() == 0) result.append("Všichni utratili stejně.");
        return result.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupMembers();
        updateSettlement();
    }


    private void showEditMemberDialog(final DBHelper.GroupMember member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upravit údaje o členu");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Jméno člena");
        nameInput.setText(member.name);
        layout.addView(nameInput);

        final EditText multiplierInput = new EditText(this);
        multiplierInput.setHint("Násobitel");
        multiplierInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        multiplierInput.setText(String.valueOf(member.multiplier));
        layout.addView(multiplierInput);

        builder.setView(layout);

        builder.setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = nameInput.getText().toString().trim();
                String newMultStr = multiplierInput.getText().toString().trim();
                if(newName.isEmpty() || newMultStr.isEmpty()){
                    Toast.makeText(GroupActivity.this, "Jméno a násobitel jsou povinné", Toast.LENGTH_SHORT).show();
                    return;
                }
                int newMultiplier = Integer.parseInt(newMultStr);
                dbHelper.updateGroupMember(member.gmId, newName, newMultiplier);
                Toast.makeText(GroupActivity.this, "Člen upraven", Toast.LENGTH_SHORT).show();
                loadGroupMembers();
                updateSettlement();
            }
        });

        builder.setNegativeButton("Zrušit", null);
        builder.show();
    }


}
