package com.example.hyu13.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText mEmailLogin, mPasswordLogin, mEmailRegistration, mPasswordRegistration
            ,mNameRegistration, mAgeRegistration, mSexRegistration, mUsernameRegistration;
    private Button mButtonLogin, mButtonRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailLogin = (EditText) findViewById(R.id.emailLogin);
        mPasswordLogin = (EditText) findViewById(R.id.passwordLogin);
        mEmailRegistration = (EditText) findViewById(R.id.emailRegistration);
        mUsernameRegistration = (EditText) findViewById(R.id.usernameRegistration);
        mPasswordRegistration = (EditText) findViewById(R.id.passwordRegistration);
        mNameRegistration = (EditText) findViewById(R.id.nameRegistration);
        mAgeRegistration = (EditText) findViewById(R.id.ageRegistration);
        mSexRegistration = (EditText) findViewById(R.id.sexRegistration);

        mButtonLogin = (Button) findViewById(R.id.buttonLogin);
        mButtonRegistration = (Button) findViewById(R.id.buttonRegistration);

        mAuth = FirebaseAuth.getInstance(); //change in status of login/logout
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mButtonRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailRegistration.getText().toString();
                final String password = mPasswordRegistration.getText().toString();
                final String username = mUsernameRegistration.getText().toString();

                //checks for more than one of the same username
                Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(username);
                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount()>0){
                            Toast.makeText(LoginActivity.this, "username already exists", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        String user_id = mAuth.getCurrentUser().getUid();
                                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                                        String name = mNameRegistration.getText().toString();
                                        String age = mAgeRegistration.getText().toString();
                                        String sex = mSexRegistration.getText().toString();

                                        Map newPost = new HashMap();
                                        newPost.put("username", username);
                                        newPost.put("name", name);
                                        newPost.put("age", age);
                                        newPost.put("sex", sex);

                                        current_user_db.setValue(newPost);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailLogin.getText().toString();
                String password = mPasswordLogin.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
