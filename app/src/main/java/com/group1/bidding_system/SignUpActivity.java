package com.group1.bidding_system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.group1.bidding_system.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class SignUpActivity extends AppCompatActivity {

    EditText firstName, lastName, email, password, reEnterPassword;
    Button signUp, cancel;

    public FirebaseAuth mAuth;
    //public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseFunctions mFunctions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.signup_email);
        firstName = findViewById(R.id.signup_firstName);
        lastName = findViewById(R.id.signup_lastName);
        password = findViewById(R.id.signup_password);
        reEnterPassword = findViewById(R.id.signup_reEnterPassword);
        signUp = findViewById(R.id.signup_signupButton);
        cancel = findViewById(R.id.signup_cancelButton);

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailInput = email.getText().toString();
                final String passwordInput = password.getText().toString();
                final String firstNameInput = firstName.getText().toString();
                final String lastNameInput = lastName.getText().toString();
                String reEnterPasswordInput = reEnterPassword.getText().toString();

                boolean isErrorThrown = false;

                if (emailInput.equals("") || emailInput == null) {
                    email.setError("Please enter an email address");
                    isErrorThrown = true;
                }
                if (passwordInput.equals("") || passwordInput == null) {
                    password.setError("Please enter an password");
                    isErrorThrown = true;
                }
                if (!passwordInput.equals(reEnterPasswordInput)) {
                    password.setError("Passwords don't match");
                    reEnterPassword.setError("Passwords don't match");
                    isErrorThrown = true;
                }
                if(firstNameInput.equals("") || firstNameInput == null) {
                    firstName.setError("Please enter a first name");
                    isErrorThrown = true;
                }
                if(lastNameInput.equals("") || lastNameInput == null){
                    lastName.setError("Please enter a last name");
                    isErrorThrown = true;
                }

                if(!isErrorThrown){
                    mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        FirebaseUser user = mAuth.getCurrentUser();

                                        assert user != null;
                                        String uid = user.getUid();

                                        Toast.makeText(SignUpActivity.this, "Sign up was successful.",
                                                Toast.LENGTH_SHORT).show();

                                        User newUser = new User(uid, firstNameInput, lastNameInput, emailInput, 200.00);


                                        createUserOnCall(newUser).addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if(task.isSuccessful()){
                                                    Log.d("Response", task.getResult());

                                                    Toast.makeText(SignUpActivity.this, "Sign up was successful.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Toast.makeText(SignUpActivity.this, "Sign up failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });



                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                        Log.d("Failed", task.getException().toString());
                                    }

                                }
                            });

                }

            }
        });
    }

    /*
      This function calls the deployed Firebase Cloud function, createUser
      On Call (functions.https.onCall) calls the Cloud function without the URL
    */
    private Task<String> createUserOnCall(User user) {
        Map<String, String> data = new HashMap<>();

        data.put("firstname", user.firstName);
        data.put("lastname", user.lastName);
        data.put("email", user.email);
        data.put("uid", user.userId);
        data.put("balance", String.valueOf(user.balance));

        return mFunctions
                .getHttpsCallable("createUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Object result = task.getResult().getData();
                        Log.d("Result", result.toString());
                        return task.getResult().getData().toString();
                    }
                });
    }

   /* *//*
     This function requests the deployed Firebase Cloud function, createUser
     On Request (functions.https.onRequest) calls the Cloud function using the generated URL for the Cloud function
   *//*
    private void createUserOnRequest(final User user){
        String createUserUrl = "https://us-central1-auction-a09bd.cloudfunctions.net/createUser";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("firstname", user.firstName);
        params.put("lastname", user.lastName);
        params.put("email", user.email);
        params.put("uid", user.userId);
        params.put("balance", String.valueOf(user.balance));

        client.post(createUserUrl, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        try {
                            String str = new String(responseBody, "UTF-8");
                            Log.d("Response", str);
                            Toast.makeText(SignUpActivity.this, "Profile data stored successfully", Toast.LENGTH_SHORT).show();

                            Intent intentToMain = new Intent(SignUpActivity.this, MainActivity.class);
                            intentToMain.putExtra("userId", user.userId);
                            startActivity(intentToMain);
                            finish();

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(SignUpActivity.this, "Unable to load actors", Toast.LENGTH_SHORT).show();
                        String str = "";

                        try {
                            str = new String(responseBody, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        Log.d("demo", str);
                    }
                }
        );
    }*/
}