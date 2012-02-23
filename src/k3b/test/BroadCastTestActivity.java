package k3b.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class BroadCastTestActivity extends Activity implements View.OnClickListener {
	
	private static final String ACTION_COMMAND = "com.zettsett.timetracker.action.COMMAND";
	class _RemoteTimeTrackerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive (Context context, Intent intent) {
			String data = intent.getDataString();
			if (data != null)
			{
				String[] parts = data.split(":");
				
		        TextView text = (TextView) findViewById(R.id.text1);

		        text.append("a " + data + "\n");
//				message
//				.append(parts[1]).append(" ").append((parts.length > 2) ? parts[2] : "").append("\n");
				;
			}
		}
	}

	BroadcastReceiver myReceiver = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TextView text = (TextView) findViewById(R.id.text1);
        
        ((Button) findViewById(R.id.button1)).setOnClickListener(this);
        ((Button) findViewById(R.id.button2)).setOnClickListener(this);
        ((Button) findViewById(R.id.button3)).setOnClickListener(this);
        

    }

	@Override
    public void onResume() {
		super.onResume();
		
		if (myReceiver == null)
		{
			myReceiver = new _RemoteTimeTrackerReceiver();
		    IntentFilter filter = new IntentFilter(ACTION_COMMAND);
		    filter.addDataScheme("cmd");
			registerReceiver(myReceiver, filter);
		}
	}
    
	@Override
    public void onPause()
    {
		
		if (myReceiver != null)
		{
			unregisterReceiver(myReceiver);
			myReceiver = null;
		}
    	super.onPause();
    }
    
	@Override
	public void onClick(View paramView) {
		Intent intent = new Intent();
		intent
			.setAction(ACTION_COMMAND)
			.setData(Uri.parse(((Button) paramView).getText().toString()))
			;
		
		sendBroadcast(intent);

        TextView text = (TextView) findViewById(R.id.text1);
        
        // text.setText(RemoteTimeTrackerReceiver.message.toString());
	}
}