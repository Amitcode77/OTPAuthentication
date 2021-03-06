package com.example.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Registration extends AppCompatActivity
{

    private CountryCodePicker ccp; //
    private EditText phoneText;       //
    private EditText codeText;   //
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber = "";  //
    private RelativeLayout relativeLayout;   //

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;      //
    private FirebaseAuth mAuth;  //
    private String mVerificationId; //
    private PhoneAuthProvider.ForceResendingToken mResendToken;   //
    private ProgressDialog loadingBar;  //

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_registration);
        ccp = (CountryCodePicker) findViewById (R.id.ccp);
        phoneText = (EditText) findViewById (R.id.phoneText);

        ccp.registerCarrierNumberEditText (phoneText);

        codeText = (EditText) findViewById (R.id.codeText);
        continueAndNextBtn = (Button) findViewById (R.id.continueNextButton);
        relativeLayout = (RelativeLayout) findViewById (R.id.phoneAuth);
        mAuth = FirebaseAuth.getInstance ();

        loadingBar = new ProgressDialog (this);


        continueAndNextBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v)
            {
                if (continueAndNextBtn.getText ().equals ("Submit")  || checker.equals ("Code Sent"))
                {
                        String verificationCode = codeText.getText ().toString ();

                        if (verificationCode.equals (""))
                        {

                        }
                        else
                        {
                            loadingBar.setTitle ("Code Verification");
                            loadingBar.setMessage ("Please wait, while we are verifying your otp");
                            loadingBar.setCanceledOnTouchOutside (false);
                            loadingBar.show ();

                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential (mVerificationId, verificationCode);
                            signInWithPhoneAuthCredential (credential);

                        }
                }
                else
                {
                    phoneNumber = ccp.getFullNumberWithPlus ();
                    if (!phoneNumber.equals (""))
                    {
                        loadingBar.setTitle ("Phone Number Verification");
                        loadingBar.setMessage ("Please wait, while we are verifying");
                        loadingBar.setCanceledOnTouchOutside (false);
                        loadingBar.show ();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS,Registration.this,mCallbacks);        // OnVerificationStateChangedCallbacks
                    }
                    else
                    {
                        Toast.makeText (Registration.this, "Please write valid phone Number", Toast.LENGTH_SHORT).show ();
                    }
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks ()
        {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential (phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                Toast.makeText (Registration.this, "Invalid Phone Number..."+e, Toast.LENGTH_LONG).show ();
                loadingBar.dismiss ();
                relativeLayout.setVisibility (View.VISIBLE);

                continueAndNextBtn.setText ("Continue");
                codeText.setVisibility (View.GONE);

             }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent (s, forceResendingToken);
                mVerificationId = s;
                mResendToken = forceResendingToken;

                relativeLayout.setVisibility (View.GONE);
                checker = "Code Sent";
                continueAndNextBtn.setText ("Submit");
                codeText.setVisibility (View.VISIBLE);

                loadingBar.dismiss ();
                Toast.makeText (Registration.this, "Code has been sent to you, please write here", Toast.LENGTH_SHORT).show ();
            }
        };



    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult> () {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        { 
                            loadingBar.dismiss ();
                            Toast.makeText (Registration.this, "Congratulation,your account is created successfully", Toast.LENGTH_SHORT).show ();
                            sendUserToMainActivity ();

                        } 
                        else 
                         {
                            loadingBar.dismiss ();
                            String e = task.getException ().toString ();
                            Toast.makeText (Registration.this, "Error: "+e, Toast.LENGTH_SHORT).show ();
                         }
                        }

                });
    }

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent (this,MainActivity.class);
        startActivity (intent);
        finish ();
    }
}
