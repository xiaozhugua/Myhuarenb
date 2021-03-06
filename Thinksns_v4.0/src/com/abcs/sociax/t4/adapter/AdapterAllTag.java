package com.abcs.sociax.t4.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abcs.sociax.t4.android.Thinksns;
import com.abcs.sociax.t4.component.HolderSociax;
import com.abcs.sociax.t4.model.ModelAllTag;
import com.abcs.sociax.android.R;

/**
 * 类说明：
 * 
 * @author Zoey
 * @date 2015年9月9日
 * @version 1.0
 */
public class AdapterAllTag extends BaseAdapter {

	Context context;
	ArrayList<ModelAllTag> tag_list = new ArrayList<ModelAllTag>();
	private Thinksns application;
	
	public AdapterAllTag(Context context,
			ArrayList<ModelAllTag> list_all) {
		super();
		this.context = context;
		this.tag_list = list_all;
		application = (Thinksns) context.getApplicationContext();
	}

	@Override
	public int getCount() {
		return tag_list.size();
	}

	@Override
	public ModelAllTag getItem(int position) {
		return tag_list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		HolderSociax holder;
		if (convertView == null) {
			holder = new HolderSociax();
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_tag_cloud, null);

			holder.tv_tag_cloud = (TextView) convertView.findViewById(R.id.tv_tag_cloud);
			holder.tv_del = (TextView) convertView.findViewById(R.id.tv_del);
			
			holder.tv_del.setVisibility(View.GONE);
			convertView.setTag(R.id.tag_viewholder, holder);
		} else {
			holder = (HolderSociax) convertView.getTag(R.id.tag_viewholder);
		}

		ModelAllTag modelTag = (ModelAllTag) getItem(position);
		holder.tv_tag_cloud.setText(modelTag.getTitle());
		
		return convertView;
	}
}
