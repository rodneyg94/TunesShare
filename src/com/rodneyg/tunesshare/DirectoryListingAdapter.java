package com.rodneyg.tunesshare;

import java.util.ArrayList;
import java.util.List;

import com.rodneyg.tunesshare.Entry;
import com.rodneyg.tunesshare.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DirectoryListingAdapter extends BaseAdapter {

	private final List<Entry> mDirectoryListing = new ArrayList<Entry>();
	private final Context mContext;

	public DirectoryListingAdapter(Context context) {
		super();
		mContext = context;
	}

	public void updateDirectoryListing(Entry dirName, List<Entry> entries) {
		mDirectoryListing.clear();
		mDirectoryListing.addAll(entries);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mDirectoryListing.size();
	}

	@Override
	public Object getItem(int position) {
		return mDirectoryListing.get(position);
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
		Entry entry = mDirectoryListing.get(position);
		textView.setText((entry.isDirectory() ? "[D] " : "[F] ") + entry.getEntryName());
		return rowView;
	}
}
