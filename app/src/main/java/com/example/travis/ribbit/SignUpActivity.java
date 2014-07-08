package com.example.travis.ribbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected EditText mEmail;
    protected Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUsername = (EditText) findViewById(R.id.editTextUsername);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mEmail = (EditText) findViewById(R.id.editTextEmail);
        mSignUpButton = (Button) findViewById(R.id.buttonSignUp);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String email = mEmail.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    // Error with user inputted fields
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setTitle(R.string.error_title_sign_up)
                            .setMessage(R.string.error_message_sign_up)
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else {
                    // User inputted fields are valid
                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setEmail(email);

                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // Success!
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                // Error!
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setTitle(R.string.error_title_sign_up)
                                        .setMessage(e.getMessage())
                                        .setPositiveButton(android.R.string.ok, null);

                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
