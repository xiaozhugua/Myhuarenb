package com.thinksns.tschat.widget;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.thinksns.tschat.constant.TSConfig;
import com.thinksns.tschat.unit.ImageUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 类说明：点击图片选择本地照片
 * 
 * @author wz
 * @date 2014-9-9
 * @version 1.0
 */
public class SelectImageListener implements DialogInterface.OnClickListener {
	private String imagePath = "";
	private Activity activity;
	private TextView image;


	public SelectImageListener(Activity activity, TextView img) {
		this.activity = activity;
		this.image = img;
	}

	public SelectImageListener(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			cameraImage();
			break;
		case 1:
			locationImage();
			break;
		default:
			dialog.dismiss();
		}

	}

	// 本地图片
	public void locationImage() {
		Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
		getImage.addCategory(Intent.CATEGORY_OPENABLE);
		getImage.setType("image/*");
		activity.startActivityForResult(Intent.createChooser(getImage, "选择照片"),
				TSConfig.LOCAL_IMAGE);
	}

	// 获取相机拍摄图片
	public void cameraImage() {
		if (!ImageUtil.isHasSdcard()) {
			Toast.makeText(activity, "请检查存储卡", Toast.LENGTH_LONG).show();
			return;
		}

		// 启动相机
		Intent myIntent = new Intent(
				MediaStore.ACTION_IMAGE_CAPTURE);
		String picName = System.currentTimeMillis() + ".jpg";
		try {
			String path = ImageUtil.saveFilePaht(picName);
			File file = new File(path);
			Uri uri = Uri.fromFile(file);
			setImagePath(path);
			myIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			myIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);         
			myIntent.putExtra("return-data", true);
		} catch (FileNotFoundException e) {
			Log.d("headImageChangeListener", "wztest err" + e.toString());
		}

		activity.startActivityForResult(myIntent, TSConfig.CAMERA_IMAGE);
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	/**
	 * 裁剪图片
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		// 裁剪图片
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		activity.startActivityForResult(intent, TSConfig.ZOOM_IMAGE);

	}
}
