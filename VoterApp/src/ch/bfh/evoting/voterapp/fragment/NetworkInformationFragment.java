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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
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
	private PendingIntent pendingIntent;
	private IntentFilter nfcIntentFilter;
	private IntentFilter[] intentFiltersArray;

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
		View v = inflater.inflate(R.layout.fragment_network_information,
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

								params = NetworkInformationFragment.this
										.getView().getRootView()
										.getLayoutParams();
								params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								NetworkInformationFragment.this.getView()
										.getRootView().setLayoutParams(params);

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

					// Setting up a pending intent that is invoked when an NFC
					// tag
					// is tapped on the back
					pendingIntent = PendingIntent.getActivity(getActivity(), 0,
							new Intent(getActivity(), getClass())
									.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
							0);

					nfcIntentFilter = new IntentFilter();
					nfcIntentFilter
							.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
					nfcIntentFilter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
					intentFiltersArray = new IntentFilter[] { nfcIntentFilter };
				} else {
					nfcAvailable = false;
				}
			}
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
			SharedPreferences preferences = this.getActivity()
					.getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
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

	

	@Override
	public void onClick(View view) {
		if (view == btnWriteNfcTag) {
			if (!nfcAdapter.isEnabled()) {

				// if nfc is available but deactivated ask the user whether he
				// wants to enable it. If yes, redirect to the settings.
				alertDialog = new AlertDialog.Builder(getActivity()).create();
				alertDialog.setTitle(getResources().getString(
						R.string.enable_nfc));
				alertDialog.setMessage(getResources().getString(
						R.string.enable_nfc_question));
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
						R.string.tap_nfc_tag));
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

}
