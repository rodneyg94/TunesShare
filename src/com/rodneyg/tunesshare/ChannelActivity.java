package com.rodneyg.tunesshare;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.InvalidInterfaceException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;
import com.rodneyg.tunesshare.ChannelActivity;
import com.rodneyg.tunesshare.Entry;
import com.rodneyg.tunesshare.FileEntry;
import com.rodneyg.tunesshare.DirectoryListingFragment.DirectoryListingFragmentListener;
import com.rodneyg.tunesshare.ExplorerChordMessage;
import com.rodneyg.tunesshare.ExplorerChordMessage.MessageType;
import com.rodneyg.tunesshare.DirEntry;
import com.rodneyg.tunesshare.NodesListFragment;
import com.rodneyg.tunesshare.NodesListFragment.NodesListFragmentListener;

public class ChannelActivity extends Activity implements NodesListFragmentListener, DirectoryListingFragmentListener {
	
	SchordManager mChordManager;
	SchordChannel channel;
	
	private TextView mFileTransferStatus;
	private ProgressBar mFileTransferProgressBar;
	private String mExploredNodeName;
	
	private NodesListFragment mNodesListFragment;
	private DirectoryListingFragment mDirectoryListingFragment;
	private FragmentTransaction mFragmentTransaction;
	private boolean mIsDirectoryListingFragmentVisible = false;
	
	public static final String TAG = "TunesShare";
	public static final String CHANNEL_NAME = "TUNESSHARE_CHANNEL";
	public static final String PAYLOAD_TYPE = "TUNESSHARE_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        
        mFileTransferStatus = (TextView) findViewById(R.id.file_transfer_status);
		mFileTransferProgressBar = (ProgressBar) findViewById(R.id.file_transfer_progress_bar);
		
        mNodesListFragment = (NodesListFragment) getFragmentManager().findFragmentById(R.id.nodeslist_fragment);
		mNodesListFragment.setListener(this);
		mDirectoryListingFragment = (DirectoryListingFragment) getFragmentManager().findFragmentById(
				R.id.directorylisting_fragment);
		mDirectoryListingFragment.setListener(this);
		
		mFragmentTransaction = getFragmentManager().beginTransaction();
		mFragmentTransaction.show(mNodesListFragment);
		mFragmentTransaction.hide(mDirectoryListingFragment);
		mFragmentTransaction.commit();
		
		initChord();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (mChordManager == null) {
            initChord();
        }
    }
    
    private void initChord() {
    	
    	 // Create an instance of Schord
        Schord chord = new Schord();
        try {
        	// Initialize an instance of Schord
        	chord.initialize(this);
        } catch (SsdkUnsupportedException e) {
        	// Error handling
        	if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED) {
        		AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
            	messageBox.setTitle("Chord Channel");
            	messageBox.setMessage("Unsupported SDK");
            	messageBox.setCancelable(false);
            	messageBox.setNeutralButton("OK", null);
            	messageBox.show();
            	return;
            }
        }
        mChordManager = new SchordManager(this);
		mChordManager.setTempDirectory(Environment.getExternalStorageDirectory() + "/tstemp");

        List<Integer> interfaceList = mChordManager.getAvailableInterfaceTypes();
        if (interfaceList.isEmpty()) {
        	// There is no connection
        	return;
        }
        try {
        	mChordManager.start(interfaceList.get(0).intValue(), mManagerListener);
        } catch (InvalidInterfaceException e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_channel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onDestroy() {
    	mChordManager.stop();
    	super.onDestroy();
    }
    
    // Listener for Chord manager events
    private SchordManager.StatusListener mManagerListener =
    		new SchordManager.StatusListener () {
    	@Override
    	public void onStarted(String name, int reason) {
    		if (STARTED_BY_USER == reason) {
    			// Called when Chord is started successfully
    			channel = mChordManager.joinChannel(CHANNEL_NAME, mChannelListener);
    		}
    	}
    	@Override
    	public void onStopped(int reason) {
    		if (STOPPED_BY_USER == reason) {
    			// Called when Chord is stopped
    			mChordManager.close();
    		}
    	}
    	
    };
    
    // Listener for Chord channel events
    private SchordChannel.StatusListener mChannelListener =
    		new SchordChannel.StatusListener() {
    	
    	// Called when a node leave event is raised on the channel.
        @Override
        public void onNodeLeft(String fromNode, String fromChannel) {
        	mNodesListFragment.removeNode(fromNode);
        }

        // Called when a node join event is raised on the channel
		@Override
		public void onNodeJoined(String fromNode, String fromChannel) {
			mNodesListFragment.addNode(fromNode);
		}
        
		@Override
		public void onDataReceived(String nodeName, String channelName, String payloadType, byte[][] payload) {
			if (payloadType.equals(PAYLOAD_TYPE)) {
				final ExplorerChordMessage message = ExplorerChordMessage.obtainChordMessage(payload[0]);
				final Entry directory;
				mExploredNodeName = nodeName;
				switch (message.getType()) {
				case LISTING:
					Log.w(TAG, "Handling LISTING message");
					directory = (Entry) message.getObject(ExplorerChordMessage.DIRECTORY);
					@SuppressWarnings("unchecked")
					List<Entry> entries = (List<Entry>) message.getObject(ExplorerChordMessage.LISTING);
					mDirectoryListingFragment.setListing(directory, entries);
					mFragmentTransaction = getFragmentManager().beginTransaction();
					mFragmentTransaction.show(mDirectoryListingFragment);
					mFragmentTransaction.hide(mNodesListFragment);
					mFragmentTransaction.commit();
					mIsDirectoryListingFragmentVisible = true;
					break;
				case LISTING_REQUEST:
					Log.w(TAG, "Handling LISTING_REQUEST message");
					directory = (Entry) message.getObject(ExplorerChordMessage.DIRECTORY);
					List<Entry> listing = fetchDirectoryListing(directory);
					if (listing != null) {
						final ExplorerChordMessage listingMessage = new ExplorerChordMessage(MessageType.LISTING);
						listingMessage.putObject(ExplorerChordMessage.DIRECTORY, directory);
						listingMessage.putObject(ExplorerChordMessage.LISTING, listing);
						sendMessage(nodeName, listingMessage);
					} else {
						final ExplorerChordMessage errorMessage = new ExplorerChordMessage(MessageType.ERROR);
						errorMessage.putObject(
								ExplorerChordMessage.ERROR_MSG,
								"Error occured during listing contents of the following directory: "
										+ directory.getEntryName());
						sendMessage(nodeName, errorMessage);
					}
					break;
				case ERROR:
					new AlertDialog.Builder(ChannelActivity.this).setTitle("Error occured")
							.setMessage("Error: " + message.getString(ExplorerChordMessage.ERROR_MSG))
							.setNeutralButton("Close", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create().show();
					Log.w(TAG, "Handling ERROR message");
					break;
				case FILE_REQUEST:
					Log.w(TAG, "Handling FILE_REQUEST message");
					Entry requestedFile = (Entry) message.getObject(ExplorerChordMessage.REQUESTED_FILE);
					try {
						channel.sendFile(mExploredNodeName, "EXPLORER_FILE", requestedFile.getPath(), 5 * 1000);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					mFileTransferProgressBar.setVisibility(View.VISIBLE);
					mFileTransferProgressBar.setProgress(0);
					mFileTransferStatus.setText("Sending file...");
					mFileTransferStatus.setVisibility(View.VISIBLE);
					break;
				default:
					throw new IllegalArgumentException("Unknown message type");
				}

			}
		}

		@Override
		public void onFileWillReceive(String nodeName, String channelName, String fileName, String hash,
				String fileType, String exchangeId, long fileSize) {
			channel.acceptFile(exchangeId, 30 * 1000, 2, 10 * 1024);
			mFileTransferProgressBar.setVisibility(View.VISIBLE);
			mFileTransferProgressBar.setProgress(0);
			mFileTransferStatus.setText("Downloading file...");
			mFileTransferStatus.setVisibility(View.VISIBLE);
		}

		@Override
		public void onFileSent(String nodeName, String channelName, String fileName, String hash, String fileType,
				String exchangeId) {
			Toast.makeText(ChannelActivity.this, "File :" + fileName + " successfully sent", Toast.LENGTH_LONG).show();
			mFileTransferProgressBar.setVisibility(View.INVISIBLE);
			mFileTransferStatus.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onFileReceived(String nodeName, String channelName, String fileName, String hash, String fileType,
				String exchangeId, long fileSize, String tmpFilePath) {
			// At this point file is received. It is stored in temporary directory and can be copied somewhere else.
			Toast.makeText(ChannelActivity.this, "File :" + fileName + " successfully received", Toast.LENGTH_LONG)
					.show();
			mFileTransferProgressBar.setVisibility(View.INVISIBLE);
			mFileTransferStatus.setVisibility(View.INVISIBLE);
			File temp = new File(tmpFilePath);
			temp.renameTo(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) ,fileName));
		}

		@Override
		public void onFileFailed(String nodeName, String channelName, String hash, String fileType, String exchangeId,
				int reason) {
			Toast.makeText(ChannelActivity.this, "File transfer failed.", Toast.LENGTH_LONG).show();
			mFileTransferProgressBar.setVisibility(View.INVISIBLE);
			mFileTransferStatus.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onFileChunkSent(String nodeName, String channelName, String fileName, String hash, String fileType,
				String exchangeId, long fileSize, long offset, long chunkSize) {
			Log.e(TAG, "onFileChunkSent - progress: " + (int) (offset * 100 / fileSize));
			mFileTransferProgressBar.setProgress((int) (offset * 100 / fileSize));
		}

		@Override
		public void onFileChunkReceived(String nodeName, String channelName, String fileName, String hash,
				String fileType, String exchangeId, long fileSize, long offset) {
			Log.e(TAG, "onFileChunkReceived - progress: " + (int) (offset * 100 / fileSize));
			mFileTransferProgressBar.setProgress((int) (offset * 100 / fileSize));
		}

		@Override
		public void onMultiFilesChunkReceived(String arg0, String arg1,
				String arg2, String arg3, int arg4, String arg5, long arg6,
				long arg7) {
		}

		@Override
		public void onMultiFilesChunkSent(String arg0, String arg1,
				String arg2, String arg3, int arg4, String arg5, long arg6,
				long arg7, long arg8) {
		}

		@Override
		public void onMultiFilesFailed(String arg0, String arg1, String arg2,
				String arg3, int arg4, int arg5) {
		}

		@Override
		public void onMultiFilesFinished(String arg0, String arg1, String arg2,
				int arg3) {
		}

		@Override
		public void onMultiFilesReceived(String arg0, String arg1, String arg2,
				String arg3, int arg4, String arg5, long arg6, String arg7) {
		}

		@Override
		public void onMultiFilesSent(String arg0, String arg1, String arg2,
				String arg3, int arg4, String arg5) {
		}

		@Override
		public void onMultiFilesWillReceive(String arg0, String arg1,
				String arg2, String arg3, int arg4, String arg5, long arg6) {
		}


		@Override
		public void onUdpDataDelivered(String arg0, String arg1, String arg2) {
		}

		@Override
		public void onUdpDataReceived(String arg0, String arg1, String arg2,
				byte[][] arg3, String arg4) {
		}
        
    };


	@Override
	public void onNodeSelected(String nodeName) {
		Log.w(TAG, "onNodeSelected: " + nodeName);
		final ExplorerChordMessage message = new ExplorerChordMessage(MessageType.LISTING_REQUEST);
		message.putObject(ExplorerChordMessage.DIRECTORY, new DirEntry("", "."));
		sendMessage(nodeName, message);
		
	}
	
	private void sendMessage(String nodeName, ExplorerChordMessage message) {
		channel.sendData(nodeName, PAYLOAD_TYPE, new byte[][] { message.getBytes() });
	}

	@Override
	public void downloadFile(Entry file) {
		final ExplorerChordMessage fileRequestMessage = new ExplorerChordMessage(MessageType.FILE_REQUEST);
		fileRequestMessage.putObject(ExplorerChordMessage.REQUESTED_FILE, file);
		sendMessage(mExploredNodeName, fileRequestMessage);
	}

	@Override
	public void sendDirectoryListingRequest(Entry directory) {
		final ExplorerChordMessage message = new ExplorerChordMessage(MessageType.LISTING_REQUEST);
		message.putObject(ExplorerChordMessage.DIRECTORY, directory);
		sendMessage(mExploredNodeName, message);
	}

	@Override
	public List<Entry> fetchDirectoryListing(Entry directory) {
		List<Entry> entries = null;
		String directoryName;
		if (directory.getEntryName().equals("")) {
			directoryName = "/";
		} else {
			directoryName = directory.getPath();
		}

		File[] dirEntries = new File(directoryName).listFiles();
		if (dirEntries != null) {
			entries = new ArrayList<Entry>();
			for (File file : dirEntries) {
				if (file.isDirectory()) {
					entries.add(new DirEntry(file.getName(), file.getAbsolutePath()));
				} else {
					entries.add(new FileEntry(file.getName(), file.getAbsolutePath()));
				}
			}
		}

		return entries;
	}
	
	@Override
	public void onBackPressed() {
		if (mIsDirectoryListingFragmentVisible) {
			mFragmentTransaction = getFragmentManager().beginTransaction();
			mFragmentTransaction.show(mNodesListFragment);
			mFragmentTransaction.hide(mDirectoryListingFragment);
			mFragmentTransaction.commit();
			mIsDirectoryListingFragmentVisible = false;
		} else {
			mChordManager.stop();
			mExploredNodeName = null;
			super.onBackPressed();
		}
	}


}
