package com.example.moneymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddTransactionActivity extends AppCompatActivity {
    private Spinner spinnerType;
    private RecordDatabaseHelper dbHelper;
    private Integer mUserID;
    private Button mButtonReturn;
    private Button mChooseDateButton;
    private EditText mEditTextDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction_page);

        Intent getUserID = getIntent();
        String userIdString = getUserID.getStringExtra("User_ID");
        mUserID = Integer.parseInt(userIdString);

        spinnerType = findViewById(R.id.spinnerType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        dbHelper = new RecordDatabaseHelper(this);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });

        mButtonReturn = findViewById(R.id.buttonReturn);
        mButtonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mChooseDateButton = findViewById(R.id.chooseDateButton);
        mChooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calenderUI.showCalendar(AddTransactionActivity.this,mEditTextDate);
            }
        });

        mEditTextDate = findViewById(R.id.editTextDate);
        mEditTextDate.setFocusable(false);
        mEditTextDate.setFocusableInTouchMode(false);

    }

    private void saveTransaction() {
        // put these three inside function make sure function can fetch current input data
        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextAmount = findViewById(R.id.editTextAmount);
        EditText editTextDescription = findViewById(R.id.editTextDescription);

        //use trim() to delete 'space' from the start and the end
        String name = editTextName.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String amountString = editTextAmount.getText().toString();
        String date = mEditTextDate.getText().toString();
        String description = editTextDescription.getText().toString();

        if (name.isEmpty() || amountString.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all the blanks!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must more than 0!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Not a Valid Amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()){
            description = "No Description";
        }

        try {
            dbHelper.AddTransaction(mUserID, name, type, amount, date, description);
            Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
            clearAllInput();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "error occurred while saving Transaction ", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllInput() {
        ((EditText) findViewById(R.id.editTextName)).setText("");
        ((EditText) findViewById(R.id.editTextAmount)).setText("");
        ((EditText) findViewById(R.id.editTextDate)).setText("");
        ((EditText) findViewById(R.id.editTextDescription)).setText("");
        spinnerType.setSelection(0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.first_page_in, R.anim.second_page_out);
    }
}
