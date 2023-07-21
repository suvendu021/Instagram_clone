package Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone.OptionActivity;
import com.example.instagram_clone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapter.PhotoAdapter;
import Model.Post;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private CircleImageView imageProfile;
    private ImageView option,userPost,savePost;
    private TextView userName,post,followers,following,fullName,about;

    private FirebaseUser fUser;
    private String profileId;
    private Button editProfile;
    private RecyclerView recyclerViewSavePost;
    private PhotoAdapter postAdapterSaves;
    private List<Post> mySavedPost;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_profile, container, false);

       fUser= FirebaseAuth.getInstance().getCurrentUser();

       String data=getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId","none");
       if(data.equals("none")){
           profileId=fUser.getUid();
       }else{
           profileId=data;

       }

       imageProfile=view.findViewById(R.id.imageProfile);
       userName=view.findViewById(R.id.user_name);
       fullName=view.findViewById(R.id.fullname);
       post=view.findViewById(R.id.post);
       followers=view.findViewById(R.id.followers);
       following=view.findViewById(R.id.following);
       about=view.findViewById(R.id.about);
       userPost=view.findViewById(R.id.user_post);
       savePost=view.findViewById(R.id.save_post);
       option=view.findViewById(R.id.option);
       editProfile=view.findViewById(R.id.edit_profile);

       RecyclerView recyclerView = view.findViewById(R.id.recycle_view_uploadImage);
       recyclerView.setHasFixedSize(true);
       recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));

       myPhotoList=new ArrayList<>();
       photoAdapter=new PhotoAdapter(getContext(),myPhotoList);

       recyclerView.setAdapter(photoAdapter);

       recyclerViewSavePost = view.findViewById(R.id.recycle_view_savePost);
       recyclerViewSavePost.setHasFixedSize(true);
       recyclerViewSavePost.setLayoutManager(new GridLayoutManager(getContext(),3));

       mySavedPost=new ArrayList<>();
       postAdapterSaves=new PhotoAdapter(getContext(),mySavedPost);

       recyclerViewSavePost.setAdapter(postAdapterSaves);



       userInfo();
       getFollowersAndFollowingCount();
       getPostCount();
       myPhotos();
       getSavePosts();

       if(profileId.equals(fUser.getUid())){
           editProfile.setText("EDIT PROFILE");
       }else {
           checkFollowStatus();
       }
       editProfile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String btnText=editProfile.getText().toString();
               if(btnText.equals("EDIT PROFILE")){
                   //start edit profile activity
               }else {
                   if(btnText.equals("FOLLOW")){
                       FirebaseDatabase.getInstance().getReference().child("follow").child(fUser.getUid())
                               .child("following").child(profileId).setValue(true);
                       FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                               .child("follower").child(fUser.getUid()).setValue(true);
                   }else {
                       FirebaseDatabase.getInstance().getReference().child("follow").child(fUser.getUid())
                               .child("following").child(profileId).removeValue();
                       FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                               .child("follower").child(fUser.getUid()).removeValue();
                   }
               }


           }
       });

       option.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(getContext(), OptionActivity.class));
           }
       });

       recyclerView.setVisibility(View.VISIBLE);
       recyclerViewSavePost.setVisibility(View.GONE);
       userPost.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               recyclerView.setVisibility(View.VISIBLE);
               recyclerViewSavePost.setVisibility(View.GONE);
           }
       });
        savePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerViewSavePost.setVisibility(View.VISIBLE);
            }
        });



       return view;
    }

    private void getSavePosts() {
        final List<String> saveIds=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Saves").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    saveIds.add(dataSnapshot.getKey());
                }
                FirebaseDatabase.getInstance().getReference().child("Posts").child("userPost")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                mySavedPost.clear();
                                for(DataSnapshot dataSnapshot1:snapshot1.getChildren()){
                                    Post post=dataSnapshot1.getValue(Post.class);
                                    for(String id:saveIds){
                                        if(post.getPostId().equals(id)){
                                            mySavedPost.add(post);
                                        }
                                    }
                                }
                                postAdapterSaves.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos() {
        FirebaseDatabase.getInstance().getReference().child("Posts").child("userPost")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myPhotoList.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Post post=dataSnapshot.getValue(Post.class);
                            if(post != null && post.getPublisher() != null && post.getPublisher().equals(profileId)){
                                myPhotoList.add(post);
                            }
                        }
                        Collections.reverse(myPhotoList);
                        photoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkFollowStatus() {
        FirebaseDatabase.getInstance().getReference().child("follow")
                .child(fUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(profileId).exists()){
                            editProfile.setText("FOLLOWING");
                        }else {
                            editProfile.setText("FOLLOW");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPostCount() {
        FirebaseDatabase.getInstance().getReference().child("Posts").child("userPost")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count=0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Post post=dataSnapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileId)){
                        count++;
                    }
                }
                post.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowersAndFollowingCount() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("follow")
                .child(profileId);
        ref.child("follower").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                userName.setText(user.getUserName());
                if(user.getImageurl().equals("default")){
                    imageProfile.setImageResource(R.drawable.default_image);
                }else {
                    Picasso.get().load(user.getImageurl()).into(imageProfile);
                }
                fullName.setText(user.getName());
                about.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}