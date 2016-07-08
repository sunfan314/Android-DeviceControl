package com.jr.devicecontrol.util;

import java.util.List;
import com.jr.devicecontrol.R;
import com.jr.devicecontrol.model.Status;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BaseStatusAdapter extends ArrayAdapter<Status> {
	private int resourceId;

	public BaseStatusAdapter(Context context, int textViewResourceId,
			List<Status> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		resourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Status status = getItem(position);
		// Log.d(status.getKey(), status.getValue());
		View view;
		ViewHolder holder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			holder = new ViewHolder();
			holder.keyTextView = (TextView) view.findViewById(R.id.status_key);
			holder.valueTextView = (TextView) view
					.findViewById(R.id.status_value);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		holder.keyTextView.setText(status.getKey());
		holder.valueTextView.setText(status.getValue());
		return view;
	}

	private class ViewHolder {
		TextView keyTextView;
		TextView valueTextView;
	}
}
