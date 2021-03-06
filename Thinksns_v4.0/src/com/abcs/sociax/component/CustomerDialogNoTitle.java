package com.abcs.sociax.component;

import com.abcs.sociax.android.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class CustomerDialogNoTitle extends Dialog {

	public interface OnCloseListener {
		public boolean onClose(CustomerDialogNoTitle dialog);
	}

	protected OnCloseListener onCloseListener;
	protected View closeButton;
	private View content;
	protected TextView contentsView;

	public void setContent(String text) {
		contentsView.setText(text);
	}

	public CustomerDialogNoTitle(Context context, int layoutId, float margin,
			String content) {
		super(context);
		this.setNotTitle(margin);
		init(context, layoutId, content);
	}

	public CustomerDialogNoTitle(Context context, int theme, int layoutId,
			float margin, String content) {
		super(context, theme);
		this.setNotTitle(margin);
		init(context, layoutId, content);
	}

	public CustomerDialogNoTitle(Context context, int layoutId, String content) {
		super(context);
		this.setNotTitle();
		init(context, layoutId, content);
	}

	public CustomerDialogNoTitle(Context context, int theme, int layoutId,
			String content) {
		super(context, theme);
		this.setNotTitle();
		init(context, layoutId, content);
	}

	private void setNotTitle() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setCanceledOnTouchOutside(true);
	}

	private void setNotTitle(float x) {
		this.setNotTitle();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.verticalMargin = x;
		getWindow().setAttributes(lp);
	}

	private void init(Context context, int layoutId, String contents) {
		LinearLayout dialogLayout = (LinearLayout) View.inflate(context,
				com.abcs.sociax.android.R.layout.toast_dialog, null);
		this.setContentView(dialogLayout);
//		content = View.inflate(context, layoutId, null);
		contentsView = (TextView) (dialogLayout.findViewById(R.id.dialog_content));
		contentsView.setText(contents);
		dialogLayout.findViewById(R.id.dialog_background).setAlpha(0.5f);
	}

	public void setBackGroudColor(int color) {
		content.setBackgroundColor(color);
	}

	public void setBackgroundResource(int res) {
		content.setBackgroundResource(res);
	}

}
