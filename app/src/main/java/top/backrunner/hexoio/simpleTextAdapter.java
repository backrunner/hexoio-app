package top.backrunner.hexoio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class simpleTextAdapter extends RecyclerView.Adapter<simpleTextAdapter.mViewHolder> implements View.OnClickListener{

    private mViewHolder holder;
    private final LayoutInflater inflater;

    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    private OnItemClickListener mOnItemClickListener = null;

    Context context;
    View view;

    private ArrayList<String> titles = new ArrayList<String>();
    private ArrayList<String> descs = new ArrayList<>();

    public simpleTextAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view=inflater.inflate(R.layout.simple_text_row, parent, false);
        holder=new mViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(simpleTextAdapter.mViewHolder holder, int position) {
        holder.titleView.setText(titles.get(position));
        holder.descView.setText(descs.get(position));
        holder.itemView.setTag(position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(v, (int)v.getTag());
        }
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public void setContents(ArrayList<String> titles,ArrayList<String> descs){
        this.titles = titles;
        this.descs = descs;
        notifyItemRangeChanged(0,titles.size());
    }

    public void removeAt(int position){
        titles.remove(position);
        descs.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0,titles.size());
    }

    class mViewHolder extends RecyclerView.ViewHolder{
        TextView titleView;
        TextView descView;
        public mViewHolder(View itemView){
            super (itemView);
            titleView = (TextView)itemView.findViewById(R.id.itemTitle);
            descView = (TextView)itemView.findViewById(R.id.itemDesc);
        }
    }
}

