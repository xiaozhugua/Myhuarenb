package com.abcs.haiwaigou.view.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.abcs.sociax.android.R;


/**
 * Created by zjz on 2015/10/9.
 * <p/>
 * RecyclerView的FooterView，简单的展示一个TextView
 */
public class SampleFooter extends RelativeLayout {

    public SampleFooter(Context context) {
        super(context);
        init(context);
    }

    public SampleFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SampleFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {

        inflate(context, R.layout.hwg_sample_footer, this);
    }
}