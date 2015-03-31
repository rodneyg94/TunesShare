package com.rodneyg.tunesshare;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.rodneyg.tunesshare.Entry;

public class DirectoryListingFragment extends Fragment {

	private DirectoryListingFragmentListener mListener;
	private DirectoryListingAdapter mListingAdapter;
	private ListView mDirectoryListing;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListingAdapter = new DirectoryListingAdapter(activity);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.directory_listing_fragment, null);
		mDirectoryListing = (ListView) view.findViewById(R.id.directory_listing);
		mDirectoryListing.setAdapter(mListingAdapter);
		mDirectoryListing.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Entry entry = (Entry) mListingAdapter.getItem(position);
				if (entry.isDirectory()) {
					mListener.sendDirectoryListingRequest(entry);
				} else {
					new AlertDialog.Builder(getActivity()).setTitle("Download file")
							.setMessage("Do you want to download: " + entry.getEntryName() + "?")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									mListener.downloadFile(entry);
								}
							}).setNeutralButton("No", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create().show();
				}
			}
		});
		return view;
	}

	public void setListing(Entry dirName, List<Entry> entries) {
		mListingAdapter.updateDirectoryListing(dirName, entries);
	}

	public void setListener(DirectoryListingFragmentListener listener) {
		mListener = listener;
	}

	public interface DirectoryListingFragmentListener {
		void downloadFile(Entry file);

		List<Entry> fetchDirectoryListing(Entry directory);

		void sendDirectoryListingRequest(Entry directory);
	}
}
