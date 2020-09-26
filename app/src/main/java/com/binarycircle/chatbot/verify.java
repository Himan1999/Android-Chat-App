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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class verify extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private String mVerificationId;

    private EditText mOtp;
    private TextView mVerifyFeedback;
    private Button mVerifybtn;
    private ProgressBar mVerifyProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mVerificationId = getIntent().getStringExtra("VerificationId");

        mOtp = findViewById(R.id.enterOTP);
        mVerifybtn = findViewById(R.id.verifyOTP_btn);
        mVerifyProgress = findViewById(R.id.verifyprogressBar);
        mVerifyFeedback = findViewById(R.id.verify_feedback);

        mVerifybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String otp = mOtp.getText().toString();
                if(otp.isEmpty()){
                    mVerifyFeedback.setText("Please fill the OTP Received to continue.");
                    mVerifyFeedback.setVisibility(View.VISIBLE);
                }else{
                    mVerifyProgress.setVisibility(View.VISIBLE);
                    mVerifybtn.setEnabled(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(verify.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                            // ...
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mVerifyFeedback.setText("There was an error verifying the OTP.");
                                mVerifyFeedback.setVisibility(View.VISIBLE);
                            }
                        }
                        mVerifyProgress.setVisibility(View.INVISIBLE);
                        mVerifybtn.setEnabled(true);
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
        Intent homeIntent = new Intent(verify.this,MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}