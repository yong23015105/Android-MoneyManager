package com.example.moneymanager;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

public class RegisterActivity extends AppCompatActivity {
    EditText username, email, password, confirmPassword, question, answer;
    Button registerButton;
    RecordDatabaseHelper dbHelper;
    Button mBackLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.reg_username);
        email = findViewById(R.id.reg_email);
        password = findViewById(R.id.reg_password);
        confirmPassword = findViewById(R.id.reg_confirm_password);
        question = findViewById(R.id.editquestion);
        answer = findViewById(R.id.editanswer);
        registerButton = findViewById(R.id.registerButton);
        dbHelper = new RecordDatabaseHelper(this);

        mBackLoginButton = findViewById(R.id.registerBackButton);
        mBackLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString().trim();
                String mail = email.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String confirmPass = confirmPassword.getText().toString().trim();
                String ques = question.getText().toString().trim();
                String ans = answer.getText().toString().trim();

                if (user.isEmpty() || mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || ques.isEmpty() || ans.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass.equals(confirmPass)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.checkUserExists(user, mail)) {
                    Toast.makeText(RegisterActivity.this, "Username or email already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.insertUser(user, mail, pass, ques, ans)) {
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration Failed! Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.first_page_in, R.anim.second_page_out);
    }
}





