package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.DisplayResultActivity;
import ch.bfh.evoting.voterapp.MainActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager.Round;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.ObservableTreeMap;
import ch.bfh.evoting.voterapp.util.Utility;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.FiniteStateException;
import com.continuent.tungsten.fsm.core.Transition;
import com.continuent.tungsten.fsm.core.TransitionFailureException;
import com.continuent.tungsten.fsm.core.TransitionRollbackException;

/**
 * Action executed in Tally step
 * @author Philémon von Bergen
 * 
 */
public class TallyingAction extends AbstractAction {


	private Map<Element, int[]> resultMap = new ConcurrentHashMap<Element, int[]>();
	private boolean allResultsComputed = false;
	private AsyncTask<Object, Object, Object> precomputationTask;
	private AlertDialog dialogResultComputed = null;
	private AlertDialog dialogResultNotShown = null;

	public TallyingAction(Context context, String messageTypeToListenTo,
			final ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 0);

		//start to compute possible results
		precomputationTask = createPrecomputationTask(poll.getNumberOfParticipants());

		((ObservableTreeMap<String, Participant>)poll.getExcludedParticipants()).addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				precomputationTask.cancel(true);
				resultMap.clear();
				precomputationTask = createPrecomputationTask(poll.getNumberOfParticipants()-poll.getExcludedParticipants().size());
			}
		});
	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException {

		Log.d(TAG,"Tally started");

		long time0 = System.currentTimeMillis();

		//compute result
		Element product = poll.getG_q().getElement(BigInteger.valueOf(1));
		Collection<Participant> activeParticipants = new ArrayList<Participant>();
		for(Participant p : poll.getParticipants().values()){
			activeParticipants.add(p);
		}
		activeParticipants.removeAll(poll.getExcludedParticipants().values());
		for(Participant p : activeParticipants){

			ProtocolParticipant p2 = (ProtocolParticipant)p;
			if(poll.getExcludedParticipants().isEmpty()){
				product=product.apply(p2.getBi());
			} else {
				product=product.apply(p2.getBi());
				product=product.apply(p2.getHiHatPowXi());
			}
		}

		//try to find combination corresponding to the computed result
		int[] result = compareResult(product);
		precomputationTask.cancel(true);
		if(result!=null){
			Log.d(TAG, "Result is "+Arrays.toString(result));
			float sum = arraySum(result);
			if(sum>2){
				int i=0;
				for(Option op : poll.getOptions()){
					op.setVotes(result[i]);
					if(sum!=0){
						op.setPercentage(result[i]/sum*100);
					}
					i++;
				}
			} else {
				Log.w(TAG, "Number of voters <= 2, so didn't display result to keep vote secrecy");

				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						while(!(AndroidApplication.getInstance().getCurrentActivity() instanceof DisplayResultActivity)){
							SystemClock.sleep(300);
						}

						AndroidApplication.getInstance().getCurrentActivity().runOnUiThread(new Runnable(){

							public void run(){
								AlertDialog.Builder builder = new AlertDialog.Builder(AndroidApplication.getInstance().getCurrentActivity());
								// Add the buttons
								builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialogResultNotShown.dismiss();
									}
								});

								builder.setTitle(R.string.dialog_title_result_not_shown);
								builder.setMessage(R.string.dialog_result_not_shown);


								dialogResultNotShown = builder.create();
								dialogResultNotShown.setOnShowListener(new DialogInterface.OnShowListener() {
									@Override
									public void onShow(DialogInterface dialog) {
										Utility.setTextColor(dialog, AndroidApplication.getInstance().getResources().getColor(R.color.theme_color));
										dialogResultNotShown.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundResource(
												R.drawable.selectable_background_votebartheme);
									}
								});

								// Create the AlertDialog
								dialogResultNotShown.show();
							}
						});
						return null;
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			}
		} else {
			Log.e(TAG, "Result not found");
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {

					while(!(AndroidApplication.getInstance().getCurrentActivity() instanceof DisplayResultActivity)){
						SystemClock.sleep(300);
					}

					AndroidApplication.getInstance().getCurrentActivity().runOnUiThread(new Runnable(){

						public void run(){
							AlertDialog.Builder builder = new AlertDialog.Builder(AndroidApplication.getInstance().getCurrentActivity());
							// Add the buttons
							builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialogResultComputed.dismiss();
								}
							});

							builder.setTitle(R.string.dialog_title_result_not_computed);
							builder.setMessage(R.string.dialog_result_not_computed);


							dialogResultComputed = builder.create();
							dialogResultComputed.setOnShowListener(new DialogInterface.OnShowListener() {
								@Override
								public void onShow(DialogInterface dialog) {
									Utility.setTextColor(dialog, AndroidApplication.getInstance().getResources().getColor(R.color.theme_color));
									dialogResultComputed.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundResource(
											R.drawable.selectable_background_votebartheme);
								}
							});

							// Create the AlertDialog
							dialogResultComputed.show();
						}
					});


					return null;
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		}

		long time1 = System.currentTimeMillis();
		Log.e(TAG,"Time for tally round: "+(time1-time0)+" ms");

		//send broadcast to dismiss the wait dialog
		Intent intent2 = new Intent(BroadcastIntentTypes.dismissWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);

		goToNextState();
	}

	@Override
	public void savedProcessedMessage(Round round, String sender, ProtocolMessageContainer message, boolean exclude){
		//no message processed at this state
	}

	@Override
	protected void goToNextState() {
		poll.setTerminated(true);

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showResultActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		try {
			((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new ResultComputedEvent(null));
		} catch (FiniteStateException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Compute all combination of divide up MAX votes between Array.length candidate 
	 * Optimized for most of the votes in first columns
	 * @param array Array containing the possible combination
	 * @param resting Resting number of vote to divide up
	 * @param max Number of vote to divide up in total
	 * @param idx Index of the column (start with 0)
	 * @param result Obtained result of votes to find
	 */	
	private boolean computePossibleResults(int numberOfVotes, boolean interrupt) {

		//initialize array containing possible combination and the array receiving the result
		int[] voteForCandidates = new int[poll.getOptions().size()];
		for(int i=0; i<voteForCandidates.length;i++){
			voteForCandidates[i]=0;
		}

		computePossibleResultsRecursion(voteForCandidates,numberOfVotes,numberOfVotes,0, interrupt);
		return true;
	}

	/**
	 * Compute all combination of divide up MAX votes between Array.length candidate 
	 * Optimized for most of the votes in first columns
	 * @param array Array containing the possible combination
	 * @param resting Resting number of vote to divide up
	 * @param max Number of vote to divide up in total
	 * @param idx Index of the column (start with 0)
	 * @param result Obtained result of votes to find
	 */	
	private void computePossibleResultsRecursion(int[] array, int resting, int max, int idx, boolean interrupt) {
		if(interrupt) return;

		//stop condition for the recursion
		//we have reached the last column
		if (idx == array.length) {
			//if the number of votes attributed < max => not interesting for us
			if(arraySum(array)<max)return;
			//System.out.println("Possible combination "+Arrays.toString(array));
			//compare combination and result of tally
			Element tempResult = poll.getZ_q().getElement(BigInteger.ZERO);
			for(int j=0;j<array.length;j++){
				tempResult = tempResult.apply(((ProtocolOption)poll.getOptions().get(j)).getRepresentation().selfApply(poll.getZ_q().getElement(BigInteger.valueOf(array[j]))));
			}
			resultMap.put(poll.getGenerator().selfApply(tempResult), array.clone());
			return;
		}
		//else put a value at the index and call recursion for the other columns
		for (int i = resting; i >= 0; i--) { 
			if(interrupt) return;
			array[idx] = i;
			computePossibleResultsRecursion(array, resting-i, max, idx+1, interrupt);
		}
		return;
	}

	private int[] compareResult(Element searchedResult){
		do{
			int[] result = resultMap.get(searchedResult);
			if(result!=null){
				precomputationTask.cancel(true);
				return result;
			}
			SystemClock.sleep(1000);
		}while(!allResultsComputed);
		return null;
	}

	private AsyncTask<Object, Object, Object> createPrecomputationTask(final int numberOfParticipants){
		return new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {
				long startTime = SystemClock.currentThreadTimeMillis();
				computePossibleResults(numberOfParticipants, this.isCancelled());
				allResultsComputed = true;
				Log.e(TAG, "***** All possible results computed in "+(SystemClock.currentThreadTimeMillis()-startTime)+" ms *****");
				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	//	/**
	//	 * Compute all combination of divide up MAX votes between Array.length candidate 
	//	 * Optimized for most of the votes in first columns
	//	 * @param array Array containing the possible combination
	//	 * @param resting Resting number of vote to divide up
	//	 * @param max Number of vote to divide up in total
	//	 * @param idx Index of the column (start with 0)
	//	 * @param result Obtained result of votes to find
	//	 */	
	//	private int[] computePossibleResults(int numberOfVotes, Element searchedResult) {
	//
	//		//initialize array containing possible combination and the array receiving the result
	//		int[] voteForCandidates = new int[poll.getOptions().size()];
	//		int[] result = null;
	//		for(int i=0; i<voteForCandidates.length;i++){
	//			voteForCandidates[i]=0;
	//		}
	//
	//		return computePossibleResultsRecursion(voteForCandidates,numberOfVotes,numberOfVotes,0, searchedResult, result);
	//
	//	}
	//	
	//	/**
	//	 * Compute all combination of divide up MAX votes between Array.length candidate 
	//	 * Optimized for most of the votes in first columns
	//	 * @param array Array containing the possible combination
	//	 * @param resting Resting number of vote to divide up
	//	 * @param max Number of vote to divide up in total
	//	 * @param idx Index of the column (start with 0)
	//	 * @param result Obtained result of votes to find
	//	 */	
	//	private int[] computePossibleResultsRecursion(int[] array, int resting, int max, int idx, Element searchedResult, int[] result) {
	//
	//		//stop condition for the recursion
	//		//we have reached the last column
	//		if (idx == array.length) {
	//			//if the number of votes attributed < max => not interesting for us
	//			if(arraySum(array)<max)return result;
	//			System.out.println("Possible combination "+Arrays.toString(array));
	//			//compare combination and result of tally
	//			Element tempResult = poll.getZ_q().getElement(BigInteger.ZERO);
	//			for(int j=0;j<array.length;j++){
	//				tempResult = tempResult.apply(((ProtocolOption)poll.getOptions().get(j)).getRepresentation().selfApply(poll.getZ_q().getElement(BigInteger.valueOf(array[j]))));
	//			}
	//			if(poll.getG_q().areEqual(searchedResult, poll.getGenerator().selfApply(tempResult))){
	//				result=array;
	//				System.out.println("Result is "+Arrays.toString(array));
	//			}
	//			return result;
	//		}
	//		//else put a value at the index and call recursion for the other columns
	//		for (int i = resting; i >= 0; i--) { 
	//			array[idx] = i;
	//			result = computePossibleResultsRecursion(array, resting-i, max, idx+1, searchedResult, result);
	//			//if result was already found, we don't try other combinations
	//			if(result!=null) return result;
	//		}
	//		return result;
	//	}

	/**
	 * Make the sum of each element of the array
	 * Warning: this method is sensible to integer overflow if the number a great enough
	 * @param array the array to sum up
	 * @return the sum
	 */
	private int arraySum(int[] array){
		int sum=0;
		for(int i=0;i<array.length;i++){
			sum+=array[i];
		}
		return sum;
	}


}
