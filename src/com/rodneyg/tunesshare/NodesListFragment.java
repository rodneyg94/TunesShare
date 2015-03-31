package com.rodneyg.tunesshare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import com.rodneyg.tunesshare.NodeListAdapter;

public class NodesListFragment extends Fragment {

	private NodesListFragmentListener mListener;
	private NodeListAdapter mNodeListAdapter;
	private ListView mNodesList;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mNodeListAdapter = new NodeListAdapter(getActivity());
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.nodes_list_fragment, null);
		mNodesList = (ListView) view.findViewById(R.id.devices_list);
		mNodesList.setAdapter(mNodeListAdapter);
		mNodesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onNodeSelected((String) mNodeListAdapter.getItem(position));
			}
		});

		return view;
	}

	public void setListener(NodesListFragmentListener listener) {
		mListener = listener;
	}

	public void removeNode(String nodeName) {
		mNodeListAdapter.removeNode(nodeName);
	}

	public void addNode(String nodeName) {
		mNodeListAdapter.addNode(nodeName);
	}

	public void clear() {
		mNodeListAdapter.clear();
	}

	public interface NodesListFragmentListener {
		void onNodeSelected(String nodeName);
	}
}
