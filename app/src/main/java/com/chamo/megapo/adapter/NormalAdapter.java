//package com.chamo.megapo.adapter;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import com.bumptech.glide.Glide;
//import com.chamo.megapo.R;
//import com.chamo.megapo.listener.RequestListener;
//
//import java.util.List;
//
//public class NormalAdapter extends RecyclerView.Adapter<NormalAdapter.VH> implements RequestListener {
//    private final AppCompatActivity activity;
//    private Context context;
////    private List<Media> list;
//    private OnItemClickListener listener;
//
//    @Override
//    public void onResourceReady(Bitmap bitmap) {
//    }
//
//    @Override
//    public void onException() {
//    }
//    public
//    static class VH extends RecyclerView.ViewHolder{
//        public final TextView text;
//        public final ImageView imageView,imageLock;
//        public VH(View v) {
//            super(v);
//            text = (TextView) v.findViewById(R.id.text);
//            imageView = (ImageView) v.findViewById(R.id.image);
//            imageLock = (ImageView) v.findViewById(R.id.imageLock);
//        }
//    }
//
//    public NormalAdapter(List<Media> data, Context context, AppCompatActivity activity) {
//        this.list = data;
//        this.context = context;
//        this.activity = activity;
//    }
//
//    @Override
//    public void onBindViewHolder(VH holder, int position) {
//        Glide.with(context).load(list.get(position).getVideoImage()).into(holder.imageView);
//        if (!list.get(position).getUnlock().equals("0")) {
//            holder.imageLock.setVisibility(View.GONE);
//            holder.text.setText("完成度： "+list.get(position).getPercentage()+"%");
//        }else {
//            holder.text.setText("");
//            holder.imageLock.setVisibility(View.VISIBLE);
//        }
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!list.get(position).getUnlock().equals("0")) {
//                    listener.onClick(position);
//                }
//            }
//        });
//    }
//
//    public interface OnItemClickListener {
//        void onClick(int position);
//    }
//
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        this.listener = listener;
//    }
//
//    @Override
//    public int getItemCount() {
//        if (list.size() != 0) {
//            return list.size();
//        }else {
//            return 0;
//        }
//    }
//
//    @Override
//    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
//        return new VH(v);
//    }
//}