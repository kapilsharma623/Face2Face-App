package com.example.face2face.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.face2face.databinding.ActivitySignUpBinding;
import com.example.face2face.utilities.Constants;
import com.example.face2face.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager=new PreferenceManager(getApplicationContext());

        binding.imageback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.inputFirstName.getText().toString().trim().isEmpty())
                {
                    Toast.makeText(SignUpActivity.this,"Enter first name",Toast.LENGTH_SHORT).show();
                }
                else if (binding.inputLastName.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Enter last name",Toast.LENGTH_SHORT).show();
                }
                else if (binding.inputEmail.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Enter email",Toast.LENGTH_SHORT).show();
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
                    Toast.makeText(SignUpActivity.this,"Enter valid email",Toast.LENGTH_SHORT).show();
                }
                else if (binding.inputPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Enter password",Toast.LENGTH_SHORT).show();
                }
                else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Confirm your password",Toast.LENGTH_SHORT).show();
                }
                else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
                    Toast.makeText(SignUpActivity.this,"Password and confirm password must be same",Toast.LENGTH_SHORT).show();
                }
                else {
                    signup();
                }
            }
        });
    }

    private void signup()
    {
        binding.buttonSignUp.setVisibility(View.INVISIBLE);
        binding.signUpProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,binding.inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME,binding.inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS).add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_FIRST_NAME,documentReference.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, binding.inputFirstName.getText().toString());
                        preferenceManager.putString(Constants.KEY_LAST_NAME, binding.inputLastName.getText().toString());
                        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.signUpProgressBar.setVisibility(View.INVISIBLE);
                        binding.buttonSignUp.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUpActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }
}