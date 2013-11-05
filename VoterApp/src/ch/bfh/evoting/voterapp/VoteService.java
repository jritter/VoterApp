package ch.bfh.evoting.voterapp;

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
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

public class VoteService extends Service{

	boolean doWork = true;
	BroadcastReceiver voteReceiver;
	AsyncTask<Object, Object, Object> sendVotesTask;
	private int votesReceived = 0;
	private Poll poll;
	private static VoteService instance;

	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.e("VoteService", "Destroyed");
		reset();
		super.onDestroy();
	}

	private void reset(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(voteReceiver);
		votesReceived = 0;
		doWork=false;
		if(sendVotesTask!=null)
			sendVotesTask.cancel(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		this.poll = (Poll) intent.getSerializableExtra("poll");

		voteReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent intent) {
				Option vote = (Option)intent.getSerializableExtra("vote");
				for(Option op : poll.getOptions()){
					if(op.equals(vote)){
						op.setVotes(op.getVotes()+1);
					}
				}
				String voter = intent.getStringExtra("voter");
				if(poll.getParticipants().containsKey(voter)){
					votesReceived++;
					poll.getParticipants().get(voter).setHasVoted(true);
				}

				sendVotesTask = new AsyncTask<Object, Object, Object>(){

					@Override
					protected Object doInBackground(Object... arg0) {
						while(doWork){
							Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
							i.putExtra("votes", votesReceived);
							i.putExtra("options", (Serializable)poll.getOptions());
							i.putExtra("participants", (Serializable)poll.getParticipants());
							LocalBroadcastManager.getInstance(VoteService.this).sendBroadcast(i);
							SystemClock.sleep(1000);
						}
						return null;
					}

				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(voteReceiver, new IntentFilter(BroadcastIntentTypes.newVote));
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static VoteService getInstance(){
		return instance;
	}

	public int getVotes(){
		return this.votesReceived;
	}

}