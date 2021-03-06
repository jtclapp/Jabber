package com.modify.jabber;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    EditText username,email,password;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference,databaseReference;
    TextView show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);
        show = findViewById(R.id.showPassword);
        auth = FirebaseAuth.getInstance();
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = password.getText().toString();
                if(!pass.equals(""))
                {
                    if(show.getText().toString().equals("Show"))
                    {
                        show.setText("Hide");
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }
                    else
                    {
                        show.setText("Show");
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if(TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password))
                {
                    Toast.makeText(SignupActivity.this, "All fields need to be completed.",Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 8){
                    Toast.makeText(SignupActivity.this, "Password must be at least 8 characters long.",Toast.LENGTH_LONG).show();
                } else {

                    register(txt_username,txt_email,txt_password);
                }
            }
        });
    }
    private void register(String username, String email, String password)
    {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username",username);
                    hashMap.put("imageURL","default");
                    hashMap.put("status", "offline");
                    hashMap.put("bio","default bio");
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                databaseReference = FirebaseDatabase.getInstance().getReference("Settings").child("Settings:" + userid);
                                HashMap<String,String> hash = new HashMap<>();
                                hash.put("id",userid);
                                hash.put("sentColor","#FFA500");
                                hash.put("receivedColor","#0BDA51");
                                hash.put("sentTextColor","#FFFFFF");
                                hash.put("receivedTextColor","#FFFFFF");
                                databaseReference.setValue(hash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(SignupActivity.this,CreatingProfileActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignupActivity.this,"The email you entered is already in use.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}