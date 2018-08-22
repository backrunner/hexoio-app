package top.backrunner.hexoio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.graphics.Path;

public class RoundRectImageView extends ImageView {
    private int cornerSize;//圆角大小

    public RoundRectImageView(Context context){
        this(context,null);
    }

    public RoundRectImageView(Context context, AttributeSet attrs){
        this(context,attrs,0);
    }

    public RoundRectImageView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundRectImageView,defStyle,0);
        cornerSize = a.getInt(R.styleable.RoundRectImageView_corner_size,5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = getWidth();
        int h = getHeight();
        //这里对path添加一个圆角区域，这里一般需要将dp转换为pixel
        path.addRoundRect(new RectF(0,0,w,h), dip2px(this.getContext(),cornerSize), dip2px(this.getContext(),cornerSize), Path.Direction.CW);
        canvas.clipPath(path);//将Canvas按照上面的圆角区域截取
        super.onDraw(canvas);
    }

    /**
     * 设置圆角的大小
     * @param size
     */
    public void setCornerSize(int size){
        cornerSize = size;
    }

    public int dip2px(Context context, float dipValue)

    {
        float m=context.getResources().getDisplayMetrics().density ;
        return (int)(dipValue * m + 0.5f) ;

    }
}