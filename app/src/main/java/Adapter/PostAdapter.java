package Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone.CommentActivity;
import com.example.instagram_clone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import Model.Post;
import Model.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Viewholder> {
    private Context mContext;
    private List<Post> mPost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false);
        return new PostAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        Post post=mPost.get(position);

        Picasso.get().load(post.getImageUrl()).into(holder.postImage);
        holder.description.setText(post.getDescription());

        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);

                if (user != null && user.getImageurl() != null && user.getImageurl().equals("default")) {
                    holder.profileImage.setImageResource(R.drawable.default_image);
                } else {
                    Picasso.get().load(user.getImageurl()).into(holder.profileImage);
                }


                holder.author.setText(user.getName());
                holder.userName.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        isLiked(post.getPostId(), holder.like);
        noOfLikes(post.getPostId(), holder.likeNo);
        getCommentNumber(post.getPostId(),holder.noOfComment);
        isSaved(post.getPostId(),holder.save);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId",post.getPostId());
                intent.putExtra("authorId",post.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.noOfComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId",post.getPostId());
                intent.putExtra("authorId",post.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.save.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).removeValue();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        public ImageView profileImage;
        public ImageView postImage;
        public ImageView like;
        public ImageView comment;
        public ImageView more;
        public ImageView save;

        public TextView userName;
        public TextView likeNo;
        public TextView author;
        public TextView noOfComment;
        public TextView description;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            postImage=itemView.findViewById(R.id.post_image);
            profileImage=itemView.findViewById(R.id.profile_image);
            like=itemView.findViewById(R.id.like);
            comment=itemView.findViewById(R.id.comment);
            save=itemView.findViewById(R.id.save);
            more=itemView.findViewById(R.id.more);

            userName=itemView.findViewById(R.id.username);
            likeNo=itemView.findViewById(R.id.like_no);
            noOfComment=itemView.findViewById(R.id.no_of_comment);
            author=itemView.findViewById(R.id.author);
            description=itemView.findViewById(R.id.description);



        }
    }
    private void isSaved(String postId,ImageView image){
        FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).exists()){
                    image.setImageResource(R.drawable.ic_save_black);
                    image.setTag("saved");
                }else {
                    image.setImageResource(R.drawable.ic_save);
                    image.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void isLiked(String postId,ImageView imageView){
        FirebaseDatabase.getInstance().getReference().child("likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void noOfLikes(String postId,TextView textView){
        FirebaseDatabase.getInstance().getReference().child("likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textView.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getCommentNumber(String postId,TextView text){
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                text.setText("View all "+snapshot.getChildrenCount()+" Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
