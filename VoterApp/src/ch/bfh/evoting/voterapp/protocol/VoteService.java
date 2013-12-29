package ch.bfh.evoting.voterapp.protocol;

import java.io.Serializable;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.protocol.cgs97.CGS97Protocol;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class VoteService extends Service {

	private static final String TAG = VoteService.class.getSimpleName();
	private boolean doWork = true;
	private BroadcastReceiver voteReceiver;
	private AsyncTask<Object, Object, Object> sendVotesTask;
	private int votesReceived = 0;
	private Poll poll;
	private BroadcastReceiver stopReceiver;
	private static VoteService instance;

	@Override
	public void onCreate() {
		Log.e(TAG, "VoteService started" + votesReceived);
		if (instance != null)
			instance.stopSelf();
		instance = this;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service destroyed");
		reset();
		super.onDestroy();
	}

	private void reset() {
		LocalBroadcastManager.getInstance(this)
				.unregisterReceiver(voteReceiver);
		votesReceived = 0;
		doWork = false;
		if (sendVotesTask != null)
			sendVotesTask.cancel(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			this.stopSelf();
			return 0;
		}

		this.poll = (Poll) intent.getSerializableExtra("poll");

		voteReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
				Log.e("VoteService", "called");

				String voter = intent.getStringExtra("voter");
				if (poll.getParticipants().containsKey(voter)
						&& !poll.getParticipants().get(voter).hasVoted()) {
					Log.e("VoteService", "vote++");
					votesReceived++;
//					Option vote = (Option) intent.getSerializableExtra("vote");
//					for (Option op : poll.getOptions()) {
//						if (op.equals(vote)) {
//							op.setVotes(op.getVotes() + 1);
//						}
//					}
					poll.getParticipants().get(voter).setHasVoted(true);
				}

				if (votesReceived >= poll.getNumberOfParticipants()) {
					((CGS97Protocol) AndroidApplication.getInstance()
							.getProtocolInterface()).computeResult(poll);
				}

				sendVotesTask = new AsyncTask<Object, Object, Object>() {

					@Override
					protected Object doInBackground(Object... arg0) {
						while (doWork) {
							Intent i = new Intent(
									BroadcastIntentTypes.newIncomingVote);
							i.putExtra("votes", votesReceived);
							i.putExtra("options",
									(Serializable) poll.getOptions());
							i.putExtra("participants",
									(Serializable) poll.getParticipants());
							LocalBroadcastManager.getInstance(VoteService.this)
									.sendBroadcast(i);
							SystemClock.sleep(1000);
						}
						return null;
					}

				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(voteReceiver,
				new IntentFilter(BroadcastIntentTypes.newVote));

		// Register a BroadcastReceiver on stop poll order events
		stopReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				((CGS97Protocol) AndroidApplication.getInstance()
						.getProtocolInterface()).computeResult(poll);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver,
				new IntentFilter(BroadcastIntentTypes.stopVote));

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static VoteService getInstance() {
		return instance;
	}

}