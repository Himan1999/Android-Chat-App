package com.binarycircle.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private EditText mCountryCode, mPhoneNumber;
    private TextView mLoginFeedback;
    private Button mGeneratebtn;
    private ProgressBar mLoginProgress;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mCountryCode = findViewById(R.id.countryCode);
        mPhoneNumber = findViewById(R.id.enterPhonenumber);
        mGeneratebtn = findViewById(R.id.generateOTP_btn);
        mLoginProgress = findViewById(R.id.loginProgressBar);
        mLoginFeedback = findViewById(R.id.login_feedback);
//        mLoginFeedback.setVisibility(View.INVISIBLE);

        mGeneratebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String country_code = mCountryCode.getText().toString();
                String phone_number = mPhoneNumber.getText().toString();
                String phoneNumber = country_code + phone_number;
                Log.d("Phone Number",phoneNumber);

                Toast.makeText(login.this,phoneNumber,Toast.LENGTH_LONG).show();

                if(country_code.isEmpty() || phone_number.isEmpty()){
                    mLoginFeedback.setText("Please fill the phone number to continue.");
                    mLoginFeedback.setVisibility(View.VISIBLE);
                }else{
                    mLoginProgress.setVisibility(View.VISIBLE);
                    mGeneratebtn.setEnabled(false);


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            login.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        }  );

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mLoginFeedback.setText("Verification Failed, Please Try Again.");
                mLoginFeedback.setVisibility(View.VISIBLE);
                mLoginProgress.setVisibility(View.INVISIBLE);
                mGeneratebtn.setEnabled(true);

            }

            @Override
            public void onCodeSent(final String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                mLoginFeedback.setText("Code is Sent, Please wait for a while.");
                mLoginFeedback.setVisibility(View.VISIBLE);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent otpIntent = new Intent(login.this,verify.class);
                                otpIntent.putExtra("VerificationId",verificationId);
                                startActivity(otpIntent);
                            }
                        },
                        10000
                );

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                            // ...
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mLoginFeedback.setText("There was an error verifying the OTP.");
                                mLoginFeedback.setVisibility(View.VISIBLE);
                            }
                        }
                        mLoginProgress.setVisibility(View.INVISIBLE);
                        mGeneratebtn.setEnabled(true);
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null){
            sendUserToHome();

        }
    }

    public void sendUserToHome(){
        Intent homeIntent = new Intent(login.this,MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}