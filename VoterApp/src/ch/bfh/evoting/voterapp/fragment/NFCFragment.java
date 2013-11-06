package ch.bfh.evoting.voterapp.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

public class NFCFragment extends Fragment {

	private BroadcastReceiver nfcTagTappedReceiver;

	private SharedPreferences preferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_nfc, container, false);

		preferences = getActivity().getSharedPreferences(
				AndroidApplication.PREFS_NAME, 0);

		// broadcast receiving the poll review acceptations
		nfcTagTappedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

				Ndef ndef = Ndef.get(tag);

				NdefMessage msg;
				msg = ndef.getCachedNdefMessage();
				String[] config = new String(msg.getRecords()[0].getPayload())
						.split("\\|\\|");

				// saving the values that we got
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("SSID", config[0]);
				editor.commit();

				AndroidApplication.getInstance().getNetworkInterface()
						.setGroupName(config[1]);
				AndroidApplication.getInstance().getNetworkInterface()
						.setGroupPassword(config[2]);

				// connect to the network
				AndroidApplication.getInstance().connect(config, getActivity());
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
				nfcTagTappedReceiver,
				new IntentFilter(BroadcastIntentTypes.nfcTagTapped));

		return view;

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(nfcTagTappedReceiver);
	}

}
