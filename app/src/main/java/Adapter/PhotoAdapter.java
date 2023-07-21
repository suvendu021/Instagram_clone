package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import Model.Post;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private Context mContext;

    private List<Post> mPost;


    public PhotoAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.photo_item,parent,false);
        return new PhotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post=mPost.get(position);
        Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.default_image).into(holder.imagePost);
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imagePost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePost=itemView.findViewById(R.id.image_post);
        }
    }
}
