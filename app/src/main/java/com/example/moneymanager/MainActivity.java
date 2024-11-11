package com.example.moneymanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText username, password;
    Button loginButton;
    TextView signupText, forgotPasswordText;
    RecordDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        dbHelper = new RecordDatabaseHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String user_ID = dbHelper.checkUserCredentials(user, pass);

                if (user_ID != null) {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Main_Page.class);
                    intent.putExtra("User_ID", user_ID);
                    startActivity(intent);
                    overridePendingTransition(R.anim.second_page_in, R.anim.first_page_out);
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.second_page_in, R.anim.first_page_out);
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View askEmailDialog = inflater.inflate(R.layout.ask_email_layout, null);
        builder.setView(askEmailDialog);

        EditText askEmailText = askEmailDialog.findViewById(R.id.input_email);
        Button confirmEmailButton = askEmailDialog.findViewById(R.id.askEmail_ConfirmButton);
        Button backToLoginButton = askEmailDialog.findViewById(R.id.askEmail_to_mainButton);

        AlertDialog Email_dialog = builder.create();
        Email_dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corner_dialog);

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your registered email");

        confirmEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = askEmailText.getText().toString().trim();
                if (email.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter email!", Toast.LENGTH_SHORT).show();
                }else{
                    String question = dbHelper.checkUserExistsByEmail(email);
                    if (question != null) {
                        showQuestionDialog(question,email);
                        Email_dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email_dialog.dismiss();
            }
        });

        Email_dialog.show();
    }

    private void showQuestionDialog(final String question, final String email){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View askQuestionDialog = inflater.inflate(R.layout.ask_question_layout, null);
        builder.setView(askQuestionDialog);

        TextView showQuestion = askQuestionDialog.findViewById(R.id.show_questionTextView);
        EditText answerQuestion = askQuestionDialog.findViewById(R.id.answerEditText);
        Button answerConfirm = askQuestionDialog.findViewById(R.id.Answer_ConfirmButton);
        Button answerBack = askQuestionDialog.findViewById(R.id.Answer_to_mainButton);

        AlertDialog Question_dialog = builder.create();
        Question_dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corner_dialog);

        showQuestion.setText(question);
        answerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputAnswer = answerQuestion.getText().toString();
                if(inputAnswer.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter answer!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(dbHelper.checkUserAnswer(email,inputAnswer)){
                        Toast.makeText(MainActivity.this, "answer correct", Toast.LENGTH_SHORT).show();
                        showNewPasswordDialog(email);
                        Question_dialog.dismiss();
                    } else{
                        Toast.makeText(MainActivity.this, "Incorrect Answer!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        answerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Question_dialog.dismiss();
            }
        });

        Question_dialog.show();
    }

    private void showNewPasswordDialog(final String email) {
        Log.d("PasswordReset", "showNewPasswordDialog called with email: " + email); // Add this line for debugging
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View resetPasswordDialog = inflater.inflate(R.layout.reset_password_layout, null);
        builder.setView(resetPasswordDialog);

        EditText resetPassword = resetPasswordDialog.findViewById(R.id.resetPassword);
        Button confirmPassword = resetPasswordDialog.findViewById(R.id.resetPassword_ConfirmButton);
        Button BackToLogin = resetPasswordDialog.findViewById(R.id.resetPassword_to_mainButton);

        AlertDialog Password_dialog = builder.create();
        Password_dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corner_dialog);

        confirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = resetPassword.getText().toString().trim();
                if(newPassword.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a password!", Toast.LENGTH_SHORT).show();
                }else{
                    boolean isUpdated = dbHelper.updatePassword(email, newPassword);
                    if (isUpdated) {
                        Toast.makeText(MainActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                        Password_dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "Password reset failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        BackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Password_dialog.dismiss();
            }
        });

        Password_dialog.show();
    }
}



