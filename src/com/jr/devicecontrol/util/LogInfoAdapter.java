package com.jr.devicecontrol.util;

import java.util.List;

import com.jr.devicecontrol.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LogInfoAdapter extends ArrayAdapter<String>{
	
	private int resourceId;
	

	public LogInfoAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		resourceId=resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		String str=getItem(position);
		View view;
		ViewHolder holder;
		if(convertView == null){
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			holder=new ViewHolder();
			holder.logInfoTextView=(TextView)view.findViewById(R.id.log_info_textview);
			view.setTag(holder);
		}else{
			view=convertView;
			holder=(ViewHolder)view.getTag();
		}
		holder.logInfoTextView.setText(str);
		return view;
		
	}
	
	private class ViewHolder{
		TextView logInfoTextView;
	}
}
