package com.yorkyu.weathervision.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ClearEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {
    private Drawable mClearDrawable;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 获取 EditText 的DrawableRight. 假如设置没有设置我们就使用默认的图片
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
//            mClearDrawable = getResources()
//                    .getDrawable(R.drawable.);
            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
            setClearIconVisible(false);
            setOnFocusChangeListener(this);
            addTextChangedListener(this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setClearIconVisible(s.length() > 0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 当
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

//    public void setShakeAnimation() {
//        this.setAnimation(shake);
//    }

    /**
     * 设置清除图标的显示与隐藏，调用 setCompoundDrawables 为 EditText 绘制上去
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    /**
     * 根据输入框中的值来过滤数据并更新 ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean touchable = event.getX() > (getWidth() - getPaddingRight() - mClearDrawable.getIntrinsicWidth())
                        && (event.getX() < (getWidth() - getPaddingRight()));
                if (touchable) {
                    this.setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
