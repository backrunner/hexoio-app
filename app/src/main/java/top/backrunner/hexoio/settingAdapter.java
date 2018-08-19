package top.backrunner.hexoio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class settingAdapter extends RecyclerView.Adapter<settingAdapter.mViewHolder> implements View.OnClickListener{

    private mViewHolder holder;
    private final LayoutInflater inflater;

    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    private OnItemClickListener mOnItemClickListener = null;

    Context context;
    View view;

    private ArrayList<String> settingNames = new ArrayList<String>();

    public settingAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view=inflater.inflate(R.layout.setting_row, parent, false);
        holder=new mViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(settingAdapter.mViewHolder holder, int position) {
        holder.settingName.setText(settingNames.get(position));
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
        return settingNames.size();
    }

    public void setContents(ArrayList<String> names){
        this.settingNames = names;
        notifyItemRangeChanged(0,settingNames.size());
    }

    public void removeAt(int position){
        settingNames.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0,settingNames.size());
    }

    class mViewHolder extends RecyclerView.ViewHolder{
        TextView settingName;
        public mViewHolder(View itemView){
            super (itemView);
            settingName = (TextView)itemView.findViewById(R.id.settingName);
        }
    }
}

