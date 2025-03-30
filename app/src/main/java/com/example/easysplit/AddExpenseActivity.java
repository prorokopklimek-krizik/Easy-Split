package com.example.easysplit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_LOCATION_PICK = 102;

    private EditText expenseNameEditText, expenseAmountEditText, expenseDescriptionEditText, locationEditText;
    private Button addPhotoButton, setLocationButton, submitExpenseButton;
    private ImageView receiptImageView;
    private DBHelper dbHelper;
    private int memberId;
    private String photoUri = ""; // Uložená cesta/URI k fotografii
    private String currentPhotoPath; // Pro uložení cesty vytvořeného souboru

    // Proměnné pro režim úprav
    private boolean isEditing = false;
    private int expenseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        expenseNameEditText = findViewById(R.id.expenseNameEditText);
        expenseAmountEditText = findViewById(R.id.expenseAmountEditText);
        expenseDescriptionEditText = findViewById(R.id.expenseDescriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        setLocationButton = findViewById(R.id.setLocationButton);
        submitExpenseButton = findViewById(R.id.submitExpenseButton);
        receiptImageView = findViewById(R.id.receiptImageView);

        expenseAmountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        dbHelper = new DBHelper(this);

        // Získání memberId z Intentu
        memberId = getIntent().getIntExtra("memberId", -1);
        if(memberId == -1) {
            Toast.makeText(this, "Error: Invalid member ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Zjistíme, zda jde o režim úprav
        isEditing = getIntent().getBooleanExtra("isEditing", false);
        if(isEditing) {
            expenseId = getIntent().getIntExtra("expenseId", -1);
            String expName = getIntent().getStringExtra("expenseName");
            double expAmount = getIntent().getDoubleExtra("expenseAmount", 0);
            String expDesc = getIntent().getStringExtra("expenseDescription");
            String expPhoto = getIntent().getStringExtra("expensePhoto");
            if(expName != null)
                expenseNameEditText.setText(expName);
            expenseAmountEditText.setText(String.valueOf(expAmount));
            if(expDesc != null)
                expenseDescriptionEditText.setText(expDesc);
            if(expPhoto != null && !expPhoto.isEmpty()){
                photoUri = expPhoto;
                receiptImageView.setImageURI(Uri.parse(photoUri));
            }
        }

        addPhotoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        setLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Spustíme aktivitu pro výběr lokace – předpokládejme, že tato aktivita vrací souřadnice
                Intent intent = new Intent(AddExpenseActivity.this, LocationPickerActivity.class);
                startActivityForResult(intent, REQUEST_LOCATION_PICK);
            }
        });

        submitExpenseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String expenseName = expenseNameEditText.getText().toString().trim();
                String amountStr = expenseAmountEditText.getText().toString().trim();
                String expenseDescription = expenseDescriptionEditText.getText().toString().trim();
                String locationInput = locationEditText.getText().toString().trim();
                if(expenseName.isEmpty() || amountStr.isEmpty()){
                    Toast.makeText(AddExpenseActivity.this, "Please enter expense name and amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                double expenseAmount;
                try {
                    expenseAmount = Double.parseDouble(amountStr);
                } catch(NumberFormatException e){
                    Toast.makeText(AddExpenseActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Přidáme lokaci do popisu
                String fullDescription = expenseDescription + "\nAdres: " + locationInput;
                if(isEditing) {
                    if(expenseId == -1) {
                        Toast.makeText(AddExpenseActivity.this, "Error: Invalid expense ID", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbHelper.updateExpense(expenseId, expenseName, expenseAmount, fullDescription, photoUri);
                    Toast.makeText(AddExpenseActivity.this, "Expense updated", Toast.LENGTH_SHORT).show();
                } else {
                    long result = dbHelper.insertExpense(memberId, expenseName, expenseAmount, fullDescription, photoUri);
                    if(result != -1) {
                        Toast.makeText(AddExpenseActivity.this, "Expense saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddExpenseActivity.this, "Error saving expense", Toast.LENGTH_SHORT).show();
                    }
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch(IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.easysplit.fileprovider",
                        photoFile);
                photoUri = photoURI.toString();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(photoUri));
                receiptImageView.setImageBitmap(imageBitmap);
            } catch(IOException e){
                e.printStackTrace();
            }
        } else if(requestCode == REQUEST_LOCATION_PICK && resultCode == RESULT_OK) {
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if(addresses != null && !addresses.isEmpty()){
                    String address = addresses.get(0).getAddressLine(0);
                    locationEditText.setText(address);
                } else {
                    locationEditText.setText("Address not found");
                }
            } catch(IOException e){
                e.printStackTrace();
                Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
