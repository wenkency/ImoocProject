package cn.carhouse.video;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 自定义宽高比缩放的FrameLayout
 */
public class RatioFrameLayout extends FrameLayout {
    public static final int RELATIVE_WIDTH = 0;
    public static final int RELATIVE_HEIGHT = 1;
    private float mRatio = 0f;
    private int mRelative = RELATIVE_WIDTH;
    protected int mOriginWidth, mOriginHeight;

    public RatioFrameLayout(Context context) {
        this(context, null);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
    }

    private void initAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
        // 获取自定义的属性值
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatioFrameLayout);
        mRatio = ta.getFloat(R.styleable.RatioFrameLayout_rlRatio, 0);
        mRelative = ta.getInt(R.styleable.RatioFrameLayout_rlRelative, RELATIVE_WIDTH);
        ta.recycle();
    }

    /**
     * 设置比例，如果为0就是用自己的
     *
     * @param ratio
     */
    public void setRatio(float ratio) {
        this.mRatio = ratio;
        postInvalidate();
    }

    public void setRelative(int relative) {
        if (relative != RELATIVE_WIDTH && relative != RELATIVE_HEIGHT) {
            throw new RuntimeException(
                    "relative 只能取值为0或1,see @RatioLayout#RELATIVE_WIDTH,@RatioLayout#RELATIVE_HEIGHT");
        }

        this.mRelative = relative;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && mRatio != 0 && mRelative == RELATIVE_WIDTH) {
            // 1.已知 宽度确定的值，宽高的一个比例,计算出高度，对孩子的宽高产生一个期望
            float height = widthSize / mRatio;
            int childWidth = widthSize - getPaddingLeft() - getPaddingRight();
            int childHeight = (int) (height - getPaddingTop() - getPaddingBottom() + 1f);
            //测量孩子
            int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            measureChildren(childWidthSpec, childHeightSpec);
            //设置自己
            mOriginWidth = widthSize;
            mOriginHeight = (int) (height + 1f);
            setMeasuredDimension(widthSize, (int) (height + 1f));

        } else if (heightMode == MeasureSpec.EXACTLY && mRatio != 0 && mRelative == RELATIVE_HEIGHT) {
            // 2.已知 高度确定的值，宽高的一个比例,计算出宽度，对孩子的宽高产生一个期望
            float width = heightSize * mRatio;
            // 测量孩子
            int childWidth = (int) (width - getPaddingLeft() - getPaddingRight() + 1f);
            int childHeight = heightSize - getPaddingTop() - getPaddingBottom();
            int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            measureChildren(childWidthSpec, childHeightSpec);
            //设置自己
            setMeasuredDimension((int) (width + 1f), heightSize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }


    }
}
