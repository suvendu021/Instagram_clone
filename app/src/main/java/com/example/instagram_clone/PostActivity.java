package com.example.instagram_clone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {
    private ImageView close,imageAdded;
    private TextView post;
    ActivityResultLauncher<String> mGetContent;
    private Uri resultUri;
    private String imageUrl;
    AutoCompleteTextView description;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        close=findViewById(R.id.close);
        imageAdded=findViewById(R.id.image_added);
        post=findViewById(R.id.post);
        description=findViewById(R.id.description);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this,MainActivity.class));
                finish();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        imageAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

        mGetContent=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Intent intent=new Intent(PostActivity.this,CropperActivity.class);
                intent.putExtra("DATA",result.toString());
                startActivityForResult(intent,101);
            }
        });

    }

    private void upload() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("uploading");
        pd.show();
        if(resultUri!=null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference("posts").child(System.currentTimeMillis() + "." + getFileExtension(resultUri));
            StorageTask<UploadTask.TaskSnapshot> uploadTask = filepath.putFile(resultUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri=task.getResult();
                    imageUrl=downloadUri.toString();

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                    String postId=ref.push().getKey();

                    HashMap<String , Object>map=new HashMap<>();
                    map.put("postId",postId);
                    map.put("imageUrl",imageUrl);
                    map.put("description",description.getText().toString());
                    map.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                    assert postId != null;
                    ref.child("userPost").child(postId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            Toast.makeText(PostActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PostActivity.this,MainActivity.class));
                            finish();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("UploadTask", "Image upload failed: " + e.getMessage());
                    pd.dismiss();
                    Toast.makeText(PostActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();

                }
            });
        }else {
            Toast.makeText(this, "Image is not Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1 && requestCode==101){
            assert data != null;
            String result= data.getStringExtra("RESULT");

            if(result!=null){
                resultUri=Uri.parse(result);
            }
            imageAdded.setImageURI(resultUri);
        }
    }
}