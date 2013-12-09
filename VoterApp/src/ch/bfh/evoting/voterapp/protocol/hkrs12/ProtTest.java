package ch.bfh.evoting.voterapp.protocol.hkrs12;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.interfaces.Function;


public class ProtTest {

	private GStarMod G_q;
	private ZMod Z_q;
	private Element generator;
	private SecureRandom random = new SecureRandom();
	private int[] voteResult;
	private Element[] possiblePlainTexts;
	//BigInteger.valueOf(23);//
	private BigInteger p = new BigInteger("139700725455163817218350367330248482081469054544178136562919376411664993761516359902466877562980091029644919043107300384411502932147505225644121231955823457305399024737939358832740762188317942235632506234568870323910594575297744132090375969205586671220221046438363046016679285179955869484584028329654326524819");



	public ProtTest(ProtocolPoll poll){
		G_q = GStarModSafePrime.getInstance(p);
		Z_q = G_q.getZModOrder();
		generator = G_q.getRandomGenerator(new SecureRandom());
	}

	public void runProtocol(){

		/* Setup */

		Element xi1 = Z_q.getRandomElement(random);
		Element ai1 = generator.selfApply(xi1);

		Element xi2 = Z_q.getRandomElement(random);
		Element ai2 = generator.selfApply(xi2);

		//		//Computation of the proof
		//
		//		//Function g^r
		//		Function f = new CompositeFunctionClass(new MultiIdentityFunctionClass(this.dm.getZ_q(), 1),
		//				new PartiallyAppliedFunctionClass(new SelfApplyFunctionClass(this.dm.getG_q(), this.dm.getZ_q()), this.dm.getGenerator(), 0));
		//		SigmaProofGenerator spg = new SigmaProofGeneratorClass(f);
		//
		//		//Generator and index of the participant has also to be hashed in the proof
		//		Element i = ZPlusClass.getInstance().createElement(BigInteger.valueOf(
		//				this.dm.getMe().getProtocolParticipantIndex()));
		//		TupleElement otherInput = ProductGroupClass.createTupleElement(this.dm.getGenerator(), i);
		//
		//		Element ts = spg.generate(xi, ai, otherInput, this.dm.getRandom());


		/* Commitment */
		Element productNumerator1 = G_q.getElement(BigInteger.valueOf(1));
		Element productDenominator1 = G_q.getElement(BigInteger.valueOf(1));

		productNumerator1 = productNumerator1;
		productDenominator1 = productDenominator1.apply(ai2);

		Element hi1 = productNumerator1.apply(productDenominator1.invert());

		Element productNumerator2 = G_q.getElement(BigInteger.valueOf(1));
		Element productDenominator2 = G_q.getElement(BigInteger.valueOf(1));

		productNumerator2 = productNumerator2.apply(ai1);
		productDenominator2 = productDenominator2;

		Element hi2 = productNumerator2.apply(productDenominator2.invert());

		//possiblePlainTexts are the possible vi
		possiblePlainTexts = new Element[3];
		possiblePlainTexts[0]= G_q.getElement(BigInteger.ZERO);
		possiblePlainTexts[1]= G_q.getElement(BigInteger.ONE);
		possiblePlainTexts[2]= G_q.getElement(BigInteger.TEN);

		//possibleVotes are the g^vi
		Element[] possibleVotes = new Element[3];
		for(int i=0;i<3;i++){
			possibleVotes[i]=generator.selfApply(possiblePlainTexts[i]);
		}

		Element vi1 = possiblePlainTexts[0];
		Element vi2 = possiblePlainTexts[1];

		Element bi1 = hi1.selfApply(xi1).apply(generator.selfApply(vi1));
		Element bi2 = hi2.selfApply(xi2).apply(generator.selfApply(vi2));

		//		//compute validity proof
		//		ElGamalEncryptionClass eec = new ElGamalEncryptionClass(this.dm.getG_q(), this.dm.getGenerator());
		//		Element proof = eec.createValididtyProof(this.dm.getMe().getXi(), this.dm.getMe().getHi(), index, this.dm.getRandom(), possibleVotes);
		//		me.setProofValidVote(proof);

		/* Voting round */
		//Send bi

		/* Tally */

		//verify proofs
		//		for(Participant p:this.dm.getProtocolParticipants().values()){
		//
		//			logger.debug("Verifying proofs for: "+p.getIdentification());
		//
		//			//Proof of knowledge of xi
		//
		//			Function f = new CompositeFunctionClass(new MultiIdentityFunctionClass(this.dm.getZ_q(), 1),
		//					new PartiallyAppliedFunctionClass(new SelfApplyFunctionClass(this.dm.getG_q(), this.dm.getZ_q()), this.dm.getGenerator(), 0));
		//			SigmaProofGenerator spg = new SigmaProofGeneratorClass(f);
		//
		//			Element i = ZPlusClass.getInstance().createElement(BigInteger.valueOf(
		//					p.getProtocolParticipantIndex()));
		//			TupleElement otherInput = ProductGroupClass.createTupleElement(this.dm.getGenerator(), i);
		//
		//			if(!spg.verify((TupleElement) p.getProofForXi(), p.getAi(), otherInput)){
		//				AlertDialog.Builder builder1 = new AlertDialog.Builder(this.dm.getEvotingMainActivity());
		//				builder1.setTitle("Error");
		//				builder1.setMessage("Knowledge of xi false for participant " + p.getIdentification());
		//				builder1.setCancelable(true);
		//				builder1.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		//					public void onClick(DialogInterface dialog, int id) {
		//						dialog.cancel();
		//					}
		//				});
		//				AlertDialog alert1 = builder1.create();
		//				alert1.show();
		//			}
		//			logger.debug("Proof for xi was correct for participant " + p.getIdentification());
		//
		//
		//			//verify validity proof
		//
		//			//possiblePlainTexts are the possible vi
		//			AtomicElement[] possiblePlainTexts = new AtomicElement[this.dm.getCandidates().size()];
		//			for(int j=0;j<this.dm.getCandidates().size();j++){
		//				possiblePlainTexts[j]=(AtomicElement)this.dm.getCandidates().get(j).getRepresentation();
		//			}
		//
		//			//possibleVotes are the g^vi
		//			AtomicElement[] possibleVotes = new AtomicElement[this.dm.getCandidates().size()];
		//			for(int j=0;j<this.dm.getCandidates().size();j++){
		//				possibleVotes[j]=this.dm.getGenerator().selfApply(possiblePlainTexts[j]);
		//			}
		//
		//			//compute validity proof
		//			ElGamalEncryptionClass eec = new ElGamalEncryptionClass(this.dm.getG_q(), this.dm.getGenerator());
		//			//cipher text
		//
		//			Element ciphertext = ProductGroupClass.createTupleElement(p.getAi(),p.getBi());
		//
		//			if(!eec.verifyValidityProof((TupleElement)p.getProofValidVote(), ciphertext, p.getHi(), possibleVotes)){
		//				AlertDialog.Builder builder1 = new AlertDialog.Builder(this.dm.getEvotingMainActivity());
		//				builder1.setTitle("Error");
		//				builder1.setMessage("Validity proof is false for participant " + p.getIdentification());
		//				builder1.setCancelable(true);
		//				builder1.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		//					public void onClick(DialogInterface dialog, int id) {
		//						dialog.cancel();
		//					}
		//				});
		//				AlertDialog alert1 = builder1.create();
		//				alert1.show();
		//			}
		//			logger.debug("Validity Proof was correct for participant " + p.getIdentification());
		//
		//			//TODO when a participant has been excluded, result is not found even if combinations are corrects: why?
		//			//maybe problem of the index if it is  = 2 and participant = 1 has been excluded ?
		//
		//			//Proof of equality between discrete logs
		//			if(this.recoveryRoundNeeded){
		//				//Function g^r
		//				Function f1 = new CompositeFunctionClass(new MultiIdentityFunctionClass(this.dm.getZ_q(), 1),
		//						new PartiallyAppliedFunctionClass(new SelfApplyFunctionClass(this.dm.getG_q(), this.dm.getZ_q()), this.dm.getGenerator(), 0));
		//
		//				//Function h_hat^r
		//				Function f2 = new CompositeFunctionClass(new MultiIdentityFunctionClass(this.dm.getZ_q(), 1),
		//						new PartiallyAppliedFunctionClass(new SelfApplyFunctionClass(this.dm.getG_q(), this.dm.getZ_q()), p.getHiHat(), 0));
		//
		//				SigmaEqualityProofGenerator sepg = new SigmaEqualityProofGeneratorClass(f1,f2);
		//
		//				Element publicInput = ProductGroupClass.createTupleElement(p.getAi(), p.getHiHatPowXi());
		//
		//				if(!sepg.verify((TupleElement) p.getProofForHiHat(), publicInput, null)){
		//					AlertDialog.Builder builder1 = new AlertDialog.Builder(this.dm.getEvotingMainActivity());
		//					builder1.setTitle("Error");
		//					builder1.setMessage("Equality between logs proof is false for participant " + p.getIdentification());
		//					builder1.setCancelable(true);
		//					builder1.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		//						public void onClick(DialogInterface dialog, int id) {
		//							dialog.cancel();
		//						}
		//					});
		//					AlertDialog alert1 = builder1.create();
		//					alert1.show();
		//				}
		//				logger.debug("Proof of Equality between logs was correct for participant " + p.getIdentification());
		//			}
		//		}


		//compute result
		Element product = G_q.getElement(BigInteger.valueOf(1));
		product=product.apply(bi1);
		product=product.apply(bi2);

		//try to find combination corresponding to the computed result

		//initialize array containing possible combination
		int[] voteForCandidates = new int[3];
		for(int i=0; i<voteForCandidates.length;i++){
			voteForCandidates[i]=0;
		}		

		computePossibleResults(voteForCandidates, 2, 2, 0, product);

	}

	/**
	 * Compute all combination of divide up MAX votes between Array.length candidate 
	 * @param array Array containing the possible combination
	 * @param resting Resting number of vote to divide up
	 * @param max Number of vote to divide up in total
	 * @param idx Index of the column (start with 0)
	 * @param result Obtained result of votes to find
	 */	
	private void computePossibleResults(int[] array, int resting, int max, int idx, Element result) {

		//stop condition for the recursion
		//we have reached the last column
		if (idx == array.length) {
			//if the number of votes attributed < max => not interesting for us
			if(arraySum(array)<max)return;
			Log.d("ProtTest","Possible combination "+Arrays.toString(array));
			//compare combination and result of tally
			Element tempResult = Z_q.getElement(BigInteger.ZERO);
			for(int j=0;j<array.length;j++){
				tempResult = tempResult.apply(possiblePlainTexts[j].selfApply(Z_q.getElement(BigInteger.valueOf(array[j]))));
			}
			if(G_q.areEqual(result, generator.selfApply(tempResult))){
				this.voteResult=array;
			}
			return;
		}
		//else put a value at the index and call recursion for the other columns
		for (int i = 0; i <= resting; i++) { 
			array[idx] = i;
			computePossibleResults(array, resting-i, max, idx+1, result);
			//if result was already found, we don't try other combinations
			if(this.voteResult!=null) return;
		}
	}

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
