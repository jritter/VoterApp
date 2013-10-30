package ch.bfh.evoting.voterapp.fragment;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.AlertDialog;
import android.app.Fragment;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Fragment displaying the review of a poll
 * 
 */
public class NetworkInformationFragment extends Fragment {

	private boolean paramsAvailable = false;
	private String ssid;
	private String groupPassword;
	private String groupName;
	private boolean nfcAvailable;


	private NfcAdapter nfcAdapter;
	private boolean writeNfcEnabled;
	private PendingIntent pendingIntent;
	private IntentFilter nfcIntentFilter;
	private IntentFilter[] intentFiltersArray;

	private ProgressDialog writeNfcTagDialog;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_network_information, container, false);
		
		nfcAvailable = getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_NFC);



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
			groupPassword = AndroidApplication.getInstance().getNetworkInterface().getGroupPassword()
					+ AndroidApplication.getInstance().getNetworkInterface().getSaltShortDigest();
		}

		

		if (paramsAvailable) {

			final ImageView ivQrCode = (ImageView) v.findViewById(R.id.imageview_qrcode);

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
										+ groupName +"||" + groupPassword, BarcodeFormat.QR_CODE,
										size, size);
								ivQrCode.setImageBitmap(qrCode2Bitmap(qrcode));
								android.view.ViewGroup.LayoutParams params = ivQrCode.getLayoutParams();
								params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								ivQrCode.setLayoutParams(params);
								
								
								params = NetworkInformationFragment.this.getView().getRootView().getLayoutParams();
								params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								NetworkInformationFragment.this.getView().getRootView().setLayoutParams(params);

							} catch (WriterException e) {
								Log.d(this.getClass().getSimpleName(),
										e.getMessage());
							}
							return true;
						}

					});

			// only set up the NFC stuff if NFC is also available
			if (nfcAvailable) {
				nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
				if (nfcAdapter.isEnabled()) {

					// Setting up a pending intent that is invoked when an NFC tag
					// is tapped on the back
					pendingIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(
							getActivity(), getClass())
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
		
		//TODO can we remove this
		//v.findViewById(R.id.layout_bottom_action_bar).setVisibility(View.VISIBLE);
		//btnWriteNfcTag = (Button) v.findViewById(R.id.button_write_nfc_tag);

		/*if (nfcAvailable && paramsAvailable){

			nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

			btnWriteNfcTag.setOnClickListener(this);
		} else {
			((LinearLayout)btnWriteNfcTag.getParent()).removeView(btnWriteNfcTag);
		}*/


		TextView tv_network_name = (TextView) v.findViewById(R.id.textview_network_name);
		tv_network_name.setText(ssid);

		TextView tv_group_name = (TextView) v.findViewById(R.id.textview_group_name);
		tv_group_name.setText(groupName.replace("group", ""));
		
		TextView tv_group_password = (TextView) v.findViewById(R.id.textview_group_password);
		tv_group_password.setText(groupPassword);
		
		WifiAPManager wifiapman = new WifiAPManager();
		WifiManager wifiman = (WifiManager) this.getActivity().getSystemService(Context.WIFI_SERVICE);
		if (!wifiapman.isWifiAPEnabled(wifiman)) {
			LinearLayout view = (LinearLayout)v.findViewById(R.id.view_wlan_key);
			view.removeAllViews();
		} else {
			TextView tv_network_key = (TextView) v.findViewById(R.id.textview_network_key);
			SharedPreferences preferences = this.getActivity().getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
			tv_network_key.setText(preferences.getString("wlan_key", ""));
		}

		return v;
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
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
	
	/**
	 * Writes an NFC Tag
	 * 
	 * @param tag
	 *            The reference to the tag
	 * @param message
	 *            the message which should be written on the message
	 * @return true if successful, false otherwise
	 */
	public boolean writeTag(Tag tag, NdefMessage message) {

		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
	 * @param payload content to put in record
	 * @return a record containing the payload
	 */
	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload);
		return mimeRecord;
	}
	

}
