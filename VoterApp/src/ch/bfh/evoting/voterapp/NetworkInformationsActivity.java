package ch.bfh.evoting.voterapp;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.network.wifi.AdhocWifiManager;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class NetworkInformationsActivity extends Activity implements OnClickListener {

	private boolean paramsAvailable = false;
	private String ssid;
	private String groupName;
	private String groupPassword;
	private boolean nfcAvailable;
	private Button btnWriteNfcTag;
	private Button btnNext;
	private Button btnRecreateNetwork;

	private NfcAdapter nfcAdapter;
	private boolean writeNfcEnabled;
	private PendingIntent pendingIntent;
	private IntentFilter nfcIntentFilter;
	private IntentFilter[] intentFiltersArray;

	private ProgressDialog writeNfcTagDialog;
	private Poll poll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplication.getInstance().setCurrentActivity(this);


		// Is NFC available on this device?
		nfcAvailable = this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_NFC);

		Poll serializedPoll = (Poll)this.getIntent().getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}

		setContentView(R.layout.activity_network_informations);
		setupActionBar();

		ssid = AndroidApplication.getInstance().getNetworkInterface()
				.getNetworkName();
		groupName = AndroidApplication.getInstance().getNetworkInterface()
				.getGroupName();
		if (groupName == null) {
			ssid = getString(R.string.not_connected);
			groupName = getString(R.string.not_connected);
			groupPassword = getString(R.string.not_connected);
			paramsAvailable = false;
		} else {
			paramsAvailable = true;
			SharedPreferences preferences = this.getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
			groupPassword = preferences.getString("group_password", null);
		}

		findViewById(R.id.layout_bottom_action_bar).setVisibility(View.VISIBLE);
		btnWriteNfcTag = (Button) findViewById(R.id.button_write_nfc_tag);

		if (nfcAvailable && paramsAvailable){

			nfcAdapter = NfcAdapter.getDefaultAdapter(this);

			btnWriteNfcTag.setOnClickListener(this);
		} else {
			((LinearLayout)btnWriteNfcTag.getParent()).removeView(btnWriteNfcTag);
		}

		btnRecreateNetwork = (Button) findViewById(R.id.button_recreate_network);
		btnRecreateNetwork.setOnClickListener(this);

		btnNext = (Button) findViewById(R.id.button_next);
		btnNext.setOnClickListener(this);

		TextView tv_network_name = (TextView) findViewById(R.id.textview_network_name);
		tv_network_name.setText(ssid);

		TextView tv_group_name = (TextView) findViewById(R.id.textview_group_name);
		tv_group_name.setText(groupName.replace("group", ""));
		
		TextView tv_group_password = (TextView) findViewById(R.id.textview_group_password);
		tv_group_password.setText(groupPassword);
		
		WifiAPManager wifiapman = new WifiAPManager();
		WifiManager wifiman = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifiapman.isWifiAPEnabled(wifiman)) {
			LinearLayout v = (LinearLayout)findViewById(R.id.view_wlan_key);
			((LinearLayout)v.getParent()).removeView(v);
		} else {
			TextView tv_network_key = (TextView) findViewById(R.id.textview_network_key);
			SharedPreferences preferences = this.getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
			tv_network_key.setText(preferences.getString("wlan_key", ""));
		}

		if (paramsAvailable) {

			final ImageView ivQrCode = (ImageView) findViewById(R.id.imageview_qrcode);

			ivQrCode.getViewTreeObserver().addOnPreDrawListener(
					new ViewTreeObserver.OnPreDrawListener() {
						public boolean onPreDraw() {
							int width = ivQrCode.getMeasuredHeight();
							int height = ivQrCode.getMeasuredWidth();
							Log.d(this.getClass().getSimpleName(), "Width: "
									+ width);
							Log.d(this.getClass().getSimpleName(), "Height: "
									+ height);

							int size;

							if (height > width) {
								size = width;
							} else {
								size = height;
							}

							try {
								QRCodeWriter writer = new QRCodeWriter();
								BitMatrix qrcode = writer.encode(ssid + "||"
										+ groupName + "||" + groupPassword, BarcodeFormat.QR_CODE,
										size, size);
								ivQrCode.setImageBitmap(qrCode2Bitmap(qrcode));

							} catch (WriterException e) {
								Log.d(this.getClass().getSimpleName(),
										e.getMessage());
							}
							return true;
						}

					});

			// only set up the NFC stuff if NFC is also available
			if (nfcAvailable) {
				nfcAdapter = NfcAdapter.getDefaultAdapter(this);
				if (nfcAdapter.isEnabled()) {

					// Setting up a pending intent that is invoked when an NFC tag
					// is tapped on the back
					pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
							this, getClass())
					.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

					nfcIntentFilter = new IntentFilter();
					nfcIntentFilter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
					nfcIntentFilter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
					intentFiltersArray = new IntentFilter[] { nfcIntentFilter };
				} else {
					nfcAvailable = false;
				}
			}
		}
	}


	//	@Override
	//	public void onBackPressed() {
	//		// do nothing because we don't want that people access to an anterior
	//		// activity
	//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.network_informations, menu);
		return true;
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			if (this.getIntent().getBooleanExtra("goToMain", false)) {
				NavUtils.navigateUpFromSameTask(this);
			} else {
				super.onBackPressed();
			}
			return true;
		} else if (item.getItemId() == R.id.help) {
			HelpDialogFragment hdf = HelpDialogFragment.newInstance(
					getString(R.string.help_title_network_info),
					getString(R.string.help_text_network_info));
			hdf.show(getFragmentManager(), "help");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private Bitmap qrCode2Bitmap(BitMatrix qrcode) {

		final int WHITE = 0x00EAEAEA;
		final int BLACK = 0xFF000000;

		int width = qrcode.getWidth();
		int height = qrcode.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = qrcode.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	@Override
	public void onClick(View view) {

		if (view == btnWriteNfcTag){
			if (!nfcAdapter.isEnabled()) {

				// if nfc is available but deactivated ask the user whether he
				// wants to enable it. If yes, redirect to the settings.
				AlertDialog alertDialog = new AlertDialog.Builder(this)
				.create();
				alertDialog.setTitle("InstaCircle - NFC needs to be enabled");
				alertDialog
				.setMessage("In order to use this feature, NFC must be enabled. Enable now?");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
						startActivity(new Intent(
								android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				});
				alertDialog.show();
			} else {
				// display a progress dialog waiting for the NFC tag to be
				// tapped
				writeNfcEnabled = true;
				writeNfcTagDialog = new ProgressDialog(this);
				writeNfcTagDialog
				.setTitle("InstaCircle - Share Networkconfiguration with NFC Tag");
				writeNfcTagDialog
				.setMessage("Please tap a writeable NFC Tag on the back of your device...");
				writeNfcTagDialog.setCancelable(false);
				writeNfcTagDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						writeNfcEnabled = false;
						dialog.dismiss();
					}
				});

				writeNfcTagDialog.show();
			}
		} else if(view == btnRecreateNetwork){
			Intent i = new Intent(this, NetworkConfigActivity.class);
			i.putExtra("poll", poll);
			startActivity(i);
		} else if(view == btnNext) {
			if(AndroidApplication.getInstance().isAdmin()){
				Intent i = new Intent(this, ElectorateActivity.class);
				i.putExtra("poll", poll);
				startActivity(i);
			} else {
				Intent i = new Intent(this, CheckElectorateActivity.class);
				startActivity(i);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		poll = (Poll)savedInstanceState.getSerializable("poll");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	public void onNewIntent(Intent intent) {
		//if extra is present, it has priority on the saved poll
		Poll serializedPoll = (Poll)intent.getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}

		if (writeNfcEnabled) {
			// Handle the NFC part...

			String text = ssid + "||" + groupName + "||" + groupPassword;

			// create a new NdefRecord
			NdefRecord record = createMimeRecord(
					"application/ch.bfh.evoting.voterapp", text.getBytes());

			// create a new Android Application Record
			NdefRecord aar = NdefRecord
					.createApplicationRecord(getPackageName());

			// create a ndef message
			NdefMessage message = new NdefMessage(new NdefRecord[] { record,
					aar });

			// extract tag from the intent
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			// write the tag
			writeTag(tag, message);

			// close the dialog
			writeNfcEnabled = false;
			writeNfcTagDialog.dismiss();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		AndroidApplication.getInstance().setCurrentActivity(null);
		if (nfcAvailable) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {

		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);

		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
			nfcAvailable = true;
		}

		// make sure that this activity is the first one which can handle the
		// NFC tags
		if (nfcAvailable) {
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					intentFiltersArray, null);
		}

	}

	/**
	 * Writes an NFC Tag
	 * 
	 * @param tag
	 *            The reference to the tag
	 * @param message
	 *            the message which should be writen on the message
	 * @return true if successful, false otherwise
	 */
	public boolean writeTag(Tag tag, NdefMessage message) {

		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("InstaCircle - write NFC Tag failed");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		try {
			// see if tag is already NDEF formatted
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					Log.d(this.getClass().getSimpleName(), "This tag is read only.");
					alertDialog.setMessage("This tag is read only.");
					alertDialog.show();
					return false;
				}

				// work out how much space we need for the data
				int size = message.toByteArray().length;
				if (ndef.getMaxSize() < size) {
					Log.d(this.getClass().getSimpleName(), "Tag doesn't have enough free space.");
					alertDialog
					.setMessage("Tag doesn't have enough free space.");
					alertDialog.show();
					return false;
				}

				ndef.writeNdefMessage(message);
				Log.d(this.getClass().getSimpleName(), "Tag written successfully.");

			} else {
				// attempt to format tag
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						Log.d(this.getClass().getSimpleName(), "Tag written successfully!");
					} catch (IOException e) {
						alertDialog.setMessage("Unable to format tag to NDEF.");
						alertDialog.show();
						Log.d(this.getClass().getSimpleName(), "Unable to format tag to NDEF.");
						return false;

					}
				} else {
					Log.d(this.getClass().getSimpleName(), "Tag doesn't appear to support NDEF format.");
					alertDialog
					.setMessage("Tag doesn't appear to support NDEF format.");
					alertDialog.show();
					return false;
				}
			}
		} catch (Exception e) {
			Log.d(this.getClass().getSimpleName(), "Failed to write tag");
			return false;
		}
		alertDialog.setTitle("InstaCircle");
		alertDialog.setMessage("NFC Tag written successfully.");
		alertDialog.show();
		return true;
	}

	/**
	 * Creates a custom MIME type encapsulated in an NDEF record
	 * 
	 * @param mimeType
	 *            The string with the mime type name
	 */
	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload);
		return mimeRecord;
	}

	protected void onDestroy() {        
		AndroidApplication.getInstance().setCurrentActivity(null);
		super.onDestroy();
	}
}
