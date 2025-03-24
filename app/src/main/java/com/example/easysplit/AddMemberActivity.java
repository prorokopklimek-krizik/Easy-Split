package com.example.easysplit;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddMemberActivity extends AppCompatActivity {

    private EditText memberNameEditText;
    private EditText multiplierEditText;
    private Button submitMemberButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        memberNameEditText = findViewById(R.id.memberNameEditText);
        multiplierEditText = findViewById(R.id.multiplierEditText);
        submitMemberButton = findViewById(R.id.submitMemberButton);

        multiplierEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        submitMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String memberName = memberNameEditText.getText().toString().trim();
                String multiplierStr = multiplierEditText.getText().toString().trim();
                if (memberName.isEmpty() || multiplierStr.isEmpty()) {
                    Toast.makeText(AddMemberActivity.this, "Vyplňte prosím všechna pole", Toast.LENGTH_SHORT).show();
                    return;
                }
                int multiplier = Integer.parseInt(multiplierStr);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("memberName", memberName);
                resultIntent.putExtra("multiplier", multiplier);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
