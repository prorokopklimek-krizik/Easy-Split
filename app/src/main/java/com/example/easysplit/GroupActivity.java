package com.example.easysplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_NEW_MEMBER = 2;

    private Toolbar groupToolbar;
    private EditText groupNameEditText;
    private Spinner availableMembersSpinner;
    private Button addNewMemberButton;
    private Button addMemberToGroupButton;
    private LinearLayout groupMembersContainer;

    private DBHelper dbHelper;
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> availableMembersList;  // Jména dostupných členů z DB
    private List<DBHelper.PermanentMember> groupMembers; // Členové aktuální skupiny

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupToolbar = findViewById(R.id.groupToolbar);
        groupNameEditText = findViewById(R.id.groupNameEditText);
        availableMembersSpinner = findViewById(R.id.availableMembersSpinner);
        addNewMemberButton = findViewById(R.id.addNewMemberButton);
        addMemberToGroupButton = findViewById(R.id.addMemberToGroupButton);
        groupMembersContainer = findViewById(R.id.groupMembersContainer);

        dbHelper = new DBHelper(this);
        groupMembers = new ArrayList<>();

        // Načtení názvu skupiny z Intentu a nastavení do toolbaru a editovatelného pole
        String initialGroupName = getIntent().getStringExtra("groupName");
        if (initialGroupName != null && !initialGroupName.isEmpty()) {
            groupNameEditText.setText(initialGroupName);
            groupToolbar.setTitle(initialGroupName);
        } else {
            groupToolbar.setTitle("Skupina");
        }
        setSupportActionBar(groupToolbar);

        loadAvailableMembers();

        addNewMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Otevře AddMemberActivity pro přidání nového člena do DB
                Intent intent = new Intent(GroupActivity.this, AddMemberActivity.class);
                startActivityForResult(intent, REQUEST_ADD_NEW_MEMBER);
            }
        });

        addMemberToGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = availableMembersSpinner.getSelectedItemPosition();
                if (selectedPosition < 0 || availableMembersList == null || availableMembersList.size() <= selectedPosition) {
                    Toast.makeText(GroupActivity.this, "Vyberte člena", Toast.LENGTH_SHORT).show();
                    return;
                }
                String memberName = availableMembersList.get(selectedPosition);
                // Kontrola, zda člen již ve skupině není
                for (DBHelper.PermanentMember member : groupMembers) {
                    if (member.name.equals(memberName)) {
                        Toast.makeText(GroupActivity.this, "Člen je již přidán", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // Vyhledání detailu člena z DB
                DBHelper.PermanentMember newMember = getPermanentMemberByName(memberName);
                if (newMember != null) {
                    groupMembers.add(newMember);
                    addMemberToGroupList(newMember);
                }
            }
        });
    }

    /**
     * Načte seznam dostupných permanentních členů z DB a nastaví data pro Spinner.
     */
    private void loadAvailableMembers() {
        List<DBHelper.PermanentMember> permanentMembers = dbHelper.getPermanentMembers();
        availableMembersList = new ArrayList<>();
        for (DBHelper.PermanentMember member : permanentMembers) {
            availableMembersList.add(member.name);
        }
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableMembersList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availableMembersSpinner.setAdapter(spinnerAdapter);
    }

    /**
     * Pomocná metoda, která najde permanentního člena podle jména.
     */
    private DBHelper.PermanentMember getPermanentMemberByName(String name) {
        List<DBHelper.PermanentMember> permanentMembers = dbHelper.getPermanentMembers();
        for (DBHelper.PermanentMember member : permanentMembers) {
            if (member.name.equals(name)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Přidá zobrazení člena do kontejneru členů skupiny.
     * Každý řádek obsahuje informace o členu a tlačítka pro editaci a smazání.
     */
    private void addMemberToGroupList(final DBHelper.PermanentMember member) {
        // Vytvoření horizontálního layoutu pro řádek člena
        final LinearLayout memberRow = new LinearLayout(this);
        memberRow.setOrientation(LinearLayout.HORIZONTAL);
        memberRow.setPadding(16, 16, 16, 16);

        // TextView pro zobrazení jména a násobitele
        final TextView memberInfo = new TextView(this);
        memberInfo.setText(member.name + " - násobitel: " + member.multiplier);
        memberInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        memberRow.addView(memberInfo);

        // Tlačítko pro editaci člena
        Button editButton = new Button(this);
        editButton.setText("Edit");
        memberRow.addView(editButton);

        // Tlačítko pro smazání člena
        Button deleteButton = new Button(this);
        deleteButton.setText("Smazat");
        memberRow.addView(deleteButton);

        groupMembersContainer.addView(memberRow);

        // Listener pro editaci – otevře dialog pro úpravu údajů
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setTitle("Editace člena");

                LinearLayout layout = new LinearLayout(GroupActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameInput = new EditText(GroupActivity.this);
                nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                nameInput.setHint("Jméno");
                nameInput.setText(member.name);
                layout.addView(nameInput);

                final EditText multiplierInput = new EditText(GroupActivity.this);
                multiplierInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                multiplierInput.setHint("Násobitel");
                multiplierInput.setText(String.valueOf(member.multiplier));
                layout.addView(multiplierInput);

                builder.setView(layout);

                builder.setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = nameInput.getText().toString().trim();
                        String multiplierStr = multiplierInput.getText().toString().trim();
                        if (newName.isEmpty() || multiplierStr.isEmpty()) {
                            Toast.makeText(GroupActivity.this, "Vyplňte všechna pole", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int newMultiplier = Integer.parseInt(multiplierStr);
                        // Aktualizace údajů člena
                        member.name = newName;
                        member.multiplier = newMultiplier;
                        memberInfo.setText(newName + " - násobitel: " + newMultiplier);
                    }
                });
                builder.setNegativeButton("Zrušit", null);
                builder.show();
            }
        });

        // Listener pro smazání – odstraní člena ze seznamu a z UI
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupMembers.remove(member);
                groupMembersContainer.removeView(memberRow);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_NEW_MEMBER && resultCode == RESULT_OK && data != null) {
            // Po návratu z AddMemberActivity obnovíme seznam dostupných členů
            loadAvailableMembers();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
