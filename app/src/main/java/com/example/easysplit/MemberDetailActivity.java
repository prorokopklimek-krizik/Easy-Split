package com.example.easysplit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class MemberDetailActivity extends AppCompatActivity {

    // Toolbar slouží jako záhlaví s názvem člena
    private Toolbar toolbar;
    // Kontejner pro seznam výdajů
    private LinearLayout expenseListContainer;
    // Tlačítko pro přidání nové utraty
    private Button addExpenseButton;
    // Pomocný objekt pro databázi
    private DBHelper dbHelper;
    // Identifikátor člena (gm_id v tabulce group_members)
    private int memberId;
    // Jméno člena
    private String memberName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);

        toolbar = findViewById(R.id.memberToolbar);
        setSupportActionBar(toolbar);

        // Nastavíme titulek toolbaru na jméno člena
        memberId = getIntent().getIntExtra("memberId", -1);
        memberName = getIntent().getStringExtra("memberName");
        if(memberId == -1 || memberName == null) {
            Toast.makeText(this, "Chyba: Neplatný identifikátor člena", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getSupportActionBar().setTitle(memberName);

        addExpenseButton = findViewById(R.id.addExpenseButton);
        expenseListContainer = findViewById(R.id.expenseListContainer);
        dbHelper = new DBHelper(this);

        // Tlačítko pro přidání nové utraty
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberDetailActivity.this, AddExpenseActivity.class);
                intent.putExtra("isEditing", false);
                intent.putExtra("memberId", memberId);
                startActivityForResult(intent, 200);
            }
        });

        loadExpenses();
    }

    private void loadExpenses() {
        expenseListContainer.removeAllViews();
        ArrayList<DBHelper.Expense> expenses = dbHelper.getExpensesForMember(memberId);
        if(expenses == null || expenses.isEmpty()){
            TextView emptyText = new TextView(this);
            emptyText.setText("Žádné výdaje");
            emptyText.setTextColor(getResources().getColor(android.R.color.white));
            expenseListContainer.addView(emptyText);
            return;
        }
        for(final DBHelper.Expense expense : expenses) {
            // Kontejner pro jednu utratu
            LinearLayout expenseRow = new LinearLayout(this);
            expenseRow.setOrientation(LinearLayout.VERTICAL);
            expenseRow.setPadding(16, 16, 16, 16);
            expenseRow.setBackgroundResource(R.drawable.rounded_list_item);

            // Název utraty – velké písmo
            TextView expenseNameTextView = new TextView(this);
            expenseNameTextView.setText(expense.name);
            expenseNameTextView.setTextSize(18);
            expenseNameTextView.setTextColor(getResources().getColor(android.R.color.white));
            expenseRow.addView(expenseNameTextView);

            // Částka utraty
            TextView expenseAmountTextView = new TextView(this);
            expenseAmountTextView.setText("Částka: " + expense.amount + " Kč");
            expenseAmountTextView.setTextSize(14);
            expenseAmountTextView.setTextColor(getResources().getColor(android.R.color.white));
            expenseRow.addView(expenseAmountTextView);

            // Popis, pokud existuje
            if(expense.description != null && !expense.description.isEmpty()){
                TextView expenseDescTextView = new TextView(this);
                expenseDescTextView.setText("Popis: " + expense.description);
                expenseDescTextView.setTextSize(14);
                expenseDescTextView.setTextColor(getResources().getColor(android.R.color.white));
                expenseRow.addView(expenseDescTextView);
            }

            // Zobrazení fotografie účtenky, pokud je k dispozici
            if(expense.photo != null && !expense.photo.isEmpty()){
                ImageView photoView = new ImageView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 400);
                photoView.setLayoutParams(lp);
                photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photoView.setImageURI(Uri.parse(expense.photo));
                expenseRow.addView(photoView);
            }

            // Řádek s tlačítky Edit a Smazat
            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);

            Button editButton = new Button(this);
            editButton.setText("Edit");
            buttonRow.addView(editButton);

            Button deleteButton = new Button(this);
            deleteButton.setText("Smazat");
            buttonRow.addView(deleteButton);

            expenseRow.addView(buttonRow);

            // Kliknutí na tlačítko Edit otevře AddExpenseActivity v režimu úprav
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MemberDetailActivity.this, AddExpenseActivity.class);
                    intent.putExtra("isEditing", true);
                    intent.putExtra("expenseId", expense.id);
                    intent.putExtra("expenseName", expense.name);
                    intent.putExtra("expenseAmount", expense.amount);
                    intent.putExtra("expenseDescription", expense.description);
                    intent.putExtra("expensePhoto", expense.photo);
                    intent.putExtra("memberId", memberId);
                    startActivityForResult(intent, 200);
                }
            });

            // Kliknutí na tlačítko Smazat odstraní výdaj
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.deleteExpense(expense.id);
                    loadExpenses();
                    Toast.makeText(MemberDetailActivity.this, "Výdaj smazán", Toast.LENGTH_SHORT).show();
                }
            });

            // Kliknutí na celý řádek také otevře AddExpenseActivity pro úpravu
            expenseRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MemberDetailActivity.this, AddExpenseActivity.class);
                    intent.putExtra("isEditing", true);
                    intent.putExtra("expenseId", expense.id);
                    intent.putExtra("expenseName", expense.name);
                    intent.putExtra("expenseAmount", expense.amount);
                    intent.putExtra("expenseDescription", expense.description);
                    intent.putExtra("expensePhoto", expense.photo);
                    intent.putExtra("memberId", memberId);
                    startActivityForResult(intent, 200);
                }
            });

            expenseListContainer.addView(expenseRow);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 200 && resultCode == RESULT_OK) {
            loadExpenses();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }
}
