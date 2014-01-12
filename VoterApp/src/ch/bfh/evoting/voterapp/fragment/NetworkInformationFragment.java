package ch.bfh.evoting.voterapp.fragment;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Fragment displaying the review of a poll
 * 
 */
public class NetworkInformationFragment extends Fragment implements
		OnClickListener {

	private boolean paramsAvailable = false;
	private String ssid;
	private String groupPassword;
	private String groupName;
	private boolean nfcAvailable;

	private NfcAdapter nfcAdapter;
	private boolean writeNfcEnabled;
	private BroadcastReceiver nfcTagTappedReceiver;

	private ProgressDialog writeNfcTagDialog;
	private AlertDialog alertDialog;
	private Button btnWriteNfcTag;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View v = inflater.inflate(R.layout.fragment_network_information,
				container, false);

		btnWriteNfcTag = (Button) v.findViewById(R.id.button_write_nfc_tag);

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
			groupPassword = AndroidApplication.getInstance()
					.getNetworkInterface().getGroupPassword()
					+ AndroidApplication.getInstance().getNetworkInterface()
							.getSaltShortDigest();
		}

		if (paramsAvailable) {

			final ImageView ivQrCode = (ImageView) v
					.findViewById(R.id.imageview_qrcode);

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
										+ groupName + "||" + groupPassword,
										BarcodeFormat.QR_CODE, size, size);
								ivQrCode.setImageBitmap(qrCode2Bitmap(qrcode));
								android.view.ViewGroup.LayoutParams params = ivQrCode
										.getLayoutParams();
								params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								ivQrCode.setLayoutParams(params);

								params = /*NetworkInformationFragment.this
										.getView()*/v.getRootView()
										.getLayoutParams();
								params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								/*NetworkInformationFragment.this.getView()*/
										v.getRootView().setLayoutParams(params);

							} catch (WriterException e) {
								Log.d(this.getClass().getSimpleName(),
										e.getMessage());
							}
							return true;
						}

					});
		}

		if (nfcAvailable && paramsAvailable) {
			nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
			btnWriteNfcTag.setOnClickListener(this);
		} else {
			btnWriteNfcTag.setVisibility(View.GONE);
		}

		TextView tv_network_name = (TextView) v
				.findViewById(R.id.textview_network_name);
		tv_network_name.setText(ssid);

		TextView tv_group_name = (TextView) v
				.findViewById(R.id.textview_group_name);
		tv_group_name.setText(groupName.replace("group", ""));

		TextView tv_group_password = (TextView) v
				.findViewById(R.id.textview_group_password);
		tv_group_password.setText(groupPassword);
		
		TextView tv_identification = (TextView) v
				.findViewById(R.id.textview_identification);
		SharedPreferences preferences = getActivity().getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
		tv_identification.setText(preferences.getString("identification", ""));

		WifiAPManager wifiapman = new WifiAPManager();
		WifiManager wifiman = (WifiManager) this.getActivity()
				.getSystemService(Context.WIFI_SERVICE);
		if (!wifiapman.isWifiAPEnabled(wifiman)) {
			LinearLayout view = (LinearLayout) v
					.findViewById(R.id.view_wlan_key);
			view.removeAllViews();
		} else {
			TextView tv_network_key = (TextView) v
					.findViewById(R.id.textview_network_key);
			tv_network_key.setText(preferences.getString("wlan_key", ""));
		}

		// broadcast receiving the poll review acceptations
		nfcTagTappedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (writeNfcEnabled) {

					String content = ssid + "||" + groupName + "||"
							+ groupPassword;

					// create a new NdefRecord
					NdefRecord record = createMimeRecord(
							"application/ch.bfh.evoting.voterapp",
							content.getBytes());

					// create a new Android Application Record
					NdefRecord aar = NdefRecord.createApplicationRecord(context
							.getPackageName());

					// create a ndef message
					NdefMessage message = new NdefMessage(new NdefRecord[] {
							record, aar });

					// extract tag from the intent
					Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

					// write the tag
					writeTag(tag, message);

					// close the dialog
					writeNfcEnabled = false;
					writeNfcTagDialog.dismiss();

					writeNfcTagDialog.dismiss();
					writeNfcEnabled = false;
				}
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
				nfcTagTappedReceiver,
				new IntentFilter(BroadcastIntentTypes.nfcTagTapped));

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		if (view == btnWriteNfcTag) {
			if (!nfcAdapter.isEnabled()) {

				// if nfc is available but deactivated ask the user whether he
				// wants to enable it. If yes, redirect to the settings.
				alertDialog = new AlertDialog.Builder(getActivity()).create();
				alertDialog.setTitle(getResources().getString(
						R.string.nfc_enable_nfc));
				alertDialog.setMessage(getResources().getString(
						R.string.nfc_enable_nfc_question));
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
						getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								startActivity(new Intent(
										android.provider.Settings.ACTION_WIRELESS_SETTINGS));
							}
						});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
						getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

				alertDialog
						.setOnShowListener(new DialogInterface.OnShowListener() {
							@Override
							public void onShow(DialogInterface dialog) {
								Utility.setTextColor(dialog, getResources()
										.getColor(R.color.theme_color));
								alertDialog
										.getButton(AlertDialog.BUTTON_POSITIVE)
										.setBackgroundResource(
												R.drawable.selectable_background_votebartheme);
								alertDialog
										.getButton(AlertDialog.BUTTON_NEGATIVE)
										.setBackgroundResource(
												R.drawable.selectable_background_votebartheme);
							}
						});
				alertDialog.show();

			} else {
				// display a progress dialog waiting for the NFC tag to be
				// tapped
				writeNfcEnabled = true;
				writeNfcTagDialog = new ProgressDialog(getActivity());
				writeNfcTagDialog.setMessage(getResources().getString(
						R.string.put_nfc));
				writeNfcTagDialog.setCancelable(false);
				writeNfcTagDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								writeNfcEnabled = false;
								dialog.dismiss();

							}
						});
				writeNfcTagDialog
						.setOnShowListener(new DialogInterface.OnShowListener() {
							@Override
							public void onShow(DialogInterface dialog) {
								Utility.setTextColor(dialog, getResources()
										.getColor(R.color.theme_color));
								writeNfcTagDialog
										.getButton(AlertDialog.BUTTON_NEGATIVE)
										.setBackgroundResource(
												R.drawable.selectable_background_votebartheme);
							}
						});

				writeNfcTagDialog.show();
			}
		}

	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(nfcTagTappedReceiver);
	}
	
	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/
	
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
	 * Creates a custom MIME type encapsulated in an NDEF record
	 * 
	 * @param mimeType
	 *            The string with the mime type name
	 * @param payload
	 * 			  Content to write 
	 * @return 	NDEF record
	 */
	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload);
		return mimeRecord;
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

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle(getResources().getString(R.string.dialog_nfc_write_failed));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok),
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
					alertDialog.setMessage(getResources().getString(R.string.dialog_nfc_readonly));
					alertDialog.show();
					return false;
				}

				// work out how much space we need for the data
				int size = message.toByteArray().length;
				if (ndef.getMaxSize() < size) {
					alertDialog
							.setMessage(getResources().getString(R.string.dialog_nfc_not_enough_space));
					alertDialog.show();
					return false;
				}

				ndef.writeNdefMessage(message);

			} else {
				// attempt to format tag
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
					} catch (IOException e) {
						alertDialog.setMessage(getResources().getString(R.string.dialog_nfc_unable_format_ndef));
						alertDialog.show();
						return false;

					}
				} else {
					alertDialog
							.setMessage(getResources().getString(R.string.dialog_nfc_no_ndef_support));
					alertDialog.show();
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
//		alertDialog.setTitle(R.string.nfc_success);
//		alertDialog.setMessage(getResources().getString(R.string.nfc_write_success));
		
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);

			}
		});
		
		for(int i=0; i<2; i++)
			Toast.makeText(this.getActivity(), R.string.toast_nfc_write_success, Toast.LENGTH_SHORT).show();
//		alertDialog.show();
		return true;
	}

}
