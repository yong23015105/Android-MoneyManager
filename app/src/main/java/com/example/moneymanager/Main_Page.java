package com.example.moneymanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Main_Page extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecordRecycleviewAdapter mRecordRecycleviewAdapter;
    private RecordDatabaseHelper mRecordDatabaseHelper;
    private Button mAddTrasactionButton;
    private String User_ID;
    private Button mLogOutButton;
    private TextView mUsernameTitle;
    private String mUsername;
    private String mWelcomeMessage;
    private Button mThemeButton;
    private Button mTransEdit, mTransDelete, mTransCancel;
    // tool that turn layout into view for dialog
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //let ui fill the whole page
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mainpage);

        Intent getUserID = getIntent();
        User_ID = getUserID.getStringExtra("User_ID");

        /*Toast.makeText(Main_Page.this, "User_ID " + User_ID, Toast.LENGTH_SHORT).show();*/

        mRecordDatabaseHelper = new RecordDatabaseHelper(this);

        // set the layout of recycle view
        mRecyclerView = findViewById(R.id.expense_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Cursor cursor = mRecordDatabaseHelper.getAllData(User_ID);

        // set adapter, pass cursor and user id to adapter and bind adapter to the recycle view
        mRecordRecycleviewAdapter = new RecordRecycleviewAdapter(this,cursor,User_ID);
        mRecyclerView.setAdapter(mRecordRecycleviewAdapter);

        // add line between transaction record
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        mAddTrasactionButton = findViewById(R.id.create_button);
        mAddTrasactionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Main_Page.this, AddTransactionActivity.class);
            intent.putExtra("User_ID", User_ID);
            startActivity(intent);
            //animation when changing page
            overridePendingTransition(R.anim.second_page_in, R.anim.first_page_out);
        });

        mLogOutButton = findViewById(R.id.logOut_button);
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mUsernameTitle = findViewById(R.id.UsernameTitle);
        mUsername = mRecordDatabaseHelper.findUsername(Integer.parseInt(User_ID));
        mWelcomeMessage = getString(R.string.Welcome_Message,mUsername);
        mUsernameTitle.setText(mWelcomeMessage);

        mThemeButton = findViewById(R.id.btn_toggle_theme);
        mThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = mRecordDatabaseHelper.getAllData(User_ID);
        //change new cursor everytime back to main page
        mRecordRecycleviewAdapter.changeCursor(cursor);
    }

    public void showDialog(String name, String type, double amount, String date, String state, String description, long id) {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);

        mInflater = getLayoutInflater();
        View TransactionDialog = mInflater.inflate(R.layout.information_dialog, null);
        mbuilder.setView(TransactionDialog);

        TextView mTransName = TransactionDialog.findViewById(R.id.trans_name);
        TextView mTransType = TransactionDialog.findViewById(R.id.trans_type);
        TextView mTransAmount = TransactionDialog.findViewById(R.id.trans_amount);
        TextView mTransDate = TransactionDialog.findViewById(R.id.trans_date);
        TextView mTransState = TransactionDialog.findViewById(R.id.trans_state);
        TextView mTransDescription = TransactionDialog.findViewById(R.id.trans_description);

        mTransEdit = TransactionDialog.findViewById(R.id.Trans_edit);
        mTransDelete = TransactionDialog.findViewById(R.id.Trans_delete);
        mTransCancel = TransactionDialog.findViewById(R.id.Trans_cancel);

        mTransName.setText(createSpannableText("Name: " + name));
        mTransType.setText(createSpannableText("Type: " + type));
        mTransAmount.setText(createSpannableText("Amount: RM " + amount));
        mTransDate.setText(createSpannableText("Date: " + date));
        mTransState.setText(createSpannableText("State: " + state));
        mTransDescription.setText(createSpannableText("Description: " + description));

        AlertDialog trans_dialog = mbuilder.create();
        trans_dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corner_dialog);

        mTransEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog(name, type, amount, date, description, id);
                trans_dialog.dismiss();
            }
        });

        mTransCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trans_dialog.dismiss();
            }
        });

        mTransDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog(id);
                trans_dialog.dismiss();
            }
        });

        trans_dialog.show();
    }

    public void showEditDialog(String name, String type, double amount, String date, String description, long id) {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);

        mInflater = getLayoutInflater();
        View EditionDialog = mInflater.inflate(R.layout.edit_transaction_layout, null);
        mbuilder.setView(EditionDialog);

        Spinner spinnerType;
        Button mButtonSave, mChooseDateButton, mButtonReturn;
        EditText mEditTextDate, editTextName, editTextAmount, editTextDescription;

        spinnerType = EditionDialog.findViewById(R.id.spinnerType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(type);
        spinnerType.setSelection(spinnerPosition);

        mButtonSave = EditionDialog.findViewById(R.id.buttonSave);
        mButtonReturn = EditionDialog.findViewById(R.id.buttonReturn);
        mChooseDateButton = EditionDialog.findViewById(R.id.chooseDateButton);
        mEditTextDate = EditionDialog.findViewById(R.id.editTextDate);
        editTextName = EditionDialog.findViewById(R.id.editTextName);
        editTextAmount = EditionDialog.findViewById(R.id.editTextAmount);
        editTextDescription = EditionDialog.findViewById(R.id.editTextDescription);

        editTextName.setText(name);
        editTextAmount.setText(String.valueOf(amount));
        mEditTextDate.setText(date);
        editTextDescription.setText(description);

        mEditTextDate.setFocusable(false);
        mEditTextDate.setFocusableInTouchMode(false);

        AlertDialog Edit_dialog = mbuilder.create();
        Edit_dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corner_dialog);

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String type = spinnerType.getSelectedItem().toString();
                String amount = editTextAmount.getText().toString();
                String date = mEditTextDate.getText().toString();
                String description = editTextDescription.getText().toString();

                double double_amount = Double.parseDouble(amount);

                boolean isUpdated = mRecordDatabaseHelper.updateTransaction(id, name, type, double_amount, date, description);

                if (isUpdated) {
                    Toast.makeText(Main_Page.this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
                    Cursor newCursor = mRecordDatabaseHelper.getAllData(User_ID);
                    mRecordRecycleviewAdapter.changeCursor(newCursor);
                    Edit_dialog.dismiss();
                } else {
                    Toast.makeText(Main_Page.this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mButtonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Edit_dialog.dismiss();
            }
        });

        mChooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calenderUI.showCalendar(Main_Page.this,mEditTextDate);
            }
        });

        Edit_dialog.show();
    }


    public void showDeleteDialog(long id) {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);

        mInflater = getLayoutInflater();
        View DeletionDialog = mInflater.inflate(R.layout.delete_confirmation_dialog, null);
        mbuilder.setView(DeletionDialog);

        Button mDeletionConfirmButton = DeletionDialog.findViewById(R.id.askDeletion_ConfirmButton);
        Button mDeletionCancelButton = DeletionDialog.findViewById(R.id.askDeletion_to_mainButton);

        AlertDialog Deletion_Dialog = mbuilder.create();
        Deletion_Dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corner_dialog);

        mDeletionCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Deletion_Dialog.dismiss();
            }
        });

        mDeletionConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecordDatabaseHelper.deleteTransaction(id);
                Cursor cursor = mRecordDatabaseHelper.getAllData(User_ID);
                mRecordRecycleviewAdapter.changeCursor(cursor);
                Deletion_Dialog.dismiss();
            }
        });

        Deletion_Dialog.show();
    }

    private SpannableString createSpannableText(String text) {
        SpannableString spannableText = new SpannableString(text);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.first_page_in, R.anim.second_page_out);
    }
}


