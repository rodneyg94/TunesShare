package com.rodneyg.tunesshare;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.InvalidInterfaceException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordManager;

public class ChannelActivity extends Activity {
	
	SchordManager mChordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        
        // Create an instance of Schord
        Schord chord = new Schord();
        try {
        	// Initialize an instance of Schord
        	chord.initialize(this);
        } catch (SsdkUnsupportedException e) {
        	// Error handling
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

}
