package com.rodneyg.tunesshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    // Called when user clicks the "Play Music" button
    public void showMusic(View view) {
        // Create intent to deliver to MusicActivity class
        Intent intent = new Intent(this, MusicActivity.class);
        // send intent to start the activity
        startActivity(intent);
    }

    // Called when user clicks the "Join Channel" button
    public void joinChannel(View view) {
        // Create intent to deliver to ChannelActivity class
        Intent intent = new Intent(this, ChannelActivity.class);
        // send intent to start the activity
        startActivity(intent);
    }
}
