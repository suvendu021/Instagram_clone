package com.example.instagram_clone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ImageView imageView;
    private EditText username,name,email,password;
    private Button register_user;
    private TextView login;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username=findViewById(R.id.username);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        register_user=findViewById(R.id.register_user);
        pd=new ProgressDialog(this);

        mRootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        register_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtUsername=username.getText().toString();
                String txtName=name.getText().toString();
                String txtEmail=email.getText().toString();
                String txtPassword=password.getText().toString();

                if(TextUtils.isEmpty(txtEmail) ||TextUtils.isEmpty(txtName)|| TextUtils.isEmpty(txtPassword)||TextUtils.isEmpty(txtUsername) ){
                    Toast.makeText(RegisterActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                } else if (txtPassword.length()<6) {
                    Toast.makeText(RegisterActivity.this, "Password is too Short", Toast.LENGTH_SHORT).show();
                }else {
                    registeruser(txtUsername,txtName,txtEmail,txtPassword);
                }
            }
        });
    }

    private void registeruser(String txtUsername, String txtName, String txtEmail, String txtPassword) {
        pd.setMessage("please wait!");
        pd.show();

        mAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                HashMap<String,Object> map=new HashMap<>();
                map.put("name",txtName);
                map.put("userName",txtUsername);
                map.put("eMail",txtEmail);
                map.put("id",mAuth.getCurrentUser().getUid());
                map.put("bio","");
                map.put("imageurl","default");

                mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "Update the Profile for better experience", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}