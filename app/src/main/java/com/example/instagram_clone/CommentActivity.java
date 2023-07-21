package com.example.instagram_clone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import Adapter.CommentAdapter;
import Model.Comments;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private CircleImageView imageProfile;
    private TextView post;
    private EditText addComment;

    FirebaseUser fUser;
    String postId;
    String authorId;
    private CommentAdapter commentAdapter;
    private List<Comments> commentList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        RecyclerView recyclerView = findViewById(R.id.recycle_view_comment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Intent intent=getIntent();

        postId=intent.getStringExtra("postId");
        authorId=intent.getStringExtra("authorId");

        commentList=new ArrayList<>();
        commentAdapter=new CommentAdapter(this,commentList,postId);

        recyclerView.setAdapter(commentAdapter);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addComment=findViewById(R.id.add_comment);
        imageProfile=findViewById(R.id.image_profile);
        post=findViewById(R.id.post);


        fUser= FirebaseAuth.getInstance().getCurrentUser();


        getUserImage();
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(addComment.getText())){
                    Toast.makeText(CommentActivity.this, "No Comments added", Toast.LENGTH_SHORT).show();
                }else{
                    putComment();
                }
                addComment.setText("");
            }
        });


        getComments();


    }

    private void getComments() {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comments comment=dataSnapshot.getValue(Comments.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void putComment() {
        HashMap<String,Object> map=new HashMap<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Comments")
                .child(postId);
        String id=ref.push().getKey();

        map.put("commentId",id);
        map.put("comment",addComment.getText().toString());
        map.put("publisher",fUser.getUid());

        ref.child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           Toast.makeText(CommentActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                       }else{
                           Toast.makeText(CommentActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                       }
                    }
                });

    }

    private void getUserImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user=snapshot.getValue(User.class);
                        assert user != null;
                        if(user.getImageurl().equals("default")){
                            imageProfile.setImageResource(R.drawable.default_image);
                        }else{
                            Picasso.get().load(user.getImageurl()).into(imageProfile);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}