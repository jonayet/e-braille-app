package com.makeathon2014.e_braille;
import com.makeathon2014.e_braille.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;


public class MainActivity extends Activity {

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private static TextView mTitle;

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// Message types sent from the BluetoothReadService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;	

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private BluetoothAdapter mBluetoothAdapter = null;
	private static BluetoothSerialService mSerialService = null;
	private boolean mEnablingBT;
	private MenuItem mMenuItemConnect;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			finishDialogNoBluetooth();
			return;
		}

		setContentView(R.layout.main_activity);
		mSerialService = new BluetoothSerialService(this, mHandlerBT);
		mSerialService.setAllowInsecureConnections( true );


		final TextView inputTextView = (TextView) findViewById(R.id.inputTextEditText);
		final TextView selectedTextView = (TextView) findViewById(R.id.selectedCharacterTextView);
		inputTextView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				try
				{
					if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
						int x = (int)event.getX();
						int y = (int)event.getY();
						Layout layout = ((TextView) v).getLayout();
						if (layout!=null){
							int line = layout.getLineForVertical(y);
							int offset = layout.getOffsetForHorizontal(line, x - 20);
							char selecteedChar = inputTextView.getText().toString().toCharArray()[offset];
							selectedTextView.setText("" + selecteedChar);
							mSerialService.write(new byte[] { (byte)selecteedChar } );
						}
					}
					else if (event.getAction() == MotionEvent.ACTION_UP){
						selectedTextView.setText("");
					}
				}
				catch(Exception ex) {}
				return true;
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		mEnablingBT = false;
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (!mEnablingBT) { // If we are turning on the BT we cannot check if it's enable
			if ( (mBluetoothAdapter != null)  && (!mBluetoothAdapter.isEnabled()) ) {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.alert_dialog_turn_on_bt)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.alert_dialog_warning_title)
				.setCancelable( false )
				.setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mEnablingBT = true;
						Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableIntent, REQUEST_ENABLE_BT);			
					}
				})
				.setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finishDialogNoBluetooth();            	
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}		

			if (mSerialService != null) {
				// Only if the state is STATE_NONE, do we know that we haven't started already
				if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
					// Start the Bluetooth chat services
					mSerialService.start();
				}
			}

			if (mBluetoothAdapter != null) {

			}
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mSerialService != null)
			mSerialService.stop();
	}

	public int getConnectionState() {
		return mSerialService.getState();
	}

	public void send(byte[] out) {
		if ( out.length > 0 ) {
			mSerialService.write( out );
		}
	}

	public int getTitleHeight() {
		return mTitle.getHeight();
	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandlerBT = new Handler() {

		@Override
		public void handleMessage(Message msg) {        	
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothSerialService.STATE_CONNECTED:
					if (mMenuItemConnect != null) {
						mMenuItemConnect.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
						mMenuItemConnect.setTitle(R.string.disconnect);
					}

					mTitle.setText( R.string.title_connected_to );
					mTitle.append(" " + mConnectedDeviceName);
					break;

				case BluetoothSerialService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;

				case BluetoothSerialService.STATE_LISTEN:
				case BluetoothSerialService.STATE_NONE:
					if (mMenuItemConnect != null) {
						mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
						mMenuItemConnect.setTitle(R.string.connect);
					}

					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
				/*                
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;              
                mEmulatorView.write(readBuf, msg.arg1);

                break;
				 */                
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), getString(R.string.toast_connected_to) + " "
						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};    

	public void finishDialogNoBluetooth() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.alert_dialog_no_bt)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(R.string.app_name)
		.setCancelable( false )
		.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();            	
			}
		});
		AlertDialog alert = builder.create();
		alert.show(); 
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:

			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				// Attempt to connect to the device
				mSerialService.connect(device);                
			}
			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {

				finishDialogNoBluetooth();                
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		mMenuItemConnect = menu.getItem(0);      
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect:

			if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
				// Launch the DeviceListActivity to see devices and do scan
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
			else
				if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
					mSerialService.stop();
					mSerialService.start();
				}
			return true;
		}
		return false;
	}
}