package com.rodneyg.tunesshare;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rodneyg.tunesshare.R;

public class NodeListAdapter extends BaseAdapter {

	private final List<String> mNodesList = new ArrayList<String>();
	private final Context mContext;

	public NodeListAdapter(Context context) {
		super();
		mContext = context;
	}

	@Override
	public int getCount() {
		return mNodesList.size();
	}

	@Override
	public Object getItem(int position) {
		return mNodesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.row_element_name);
		textView.setText(mNodesList.get(position));
		return rowView;
	}

	public void addNode(String nodeName) {
		mNodesList.add(nodeName);
		notifyDataSetChanged();
	}

	public void removeNode(String nodeName) {
		mNodesList.remove(nodeName);
		notifyDataSetChanged();
	}

	public void clear() {
		mNodesList.clear();
		notifyDataSetChanged();
	}
}
