package ch.bfh.evoting.voterapp.protocol.hkrs12;

import java.math.BigInteger;
import java.util.Map;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.ObservableTreeMap;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;

public class ProtocolPoll extends Poll {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Crypto
	//BigInteger.valueOf(1187);//
	private BigInteger p = new BigInteger("139700725455163817218350367330248482081469054544178136562919376411664993761516359902466877562980091029644919043107300384411502932147505225644121231955823457305399024737939358832740762188317942235632506234568870323910594575297744132090375969205586671220221046438363046016679285179955869484584028329654326524819");
	private GStarModSafePrime G_q;
	private ZMod Z_q;
	private GStarModElement generator;
	
	private Map<String, Participant> excludedParticipants = new ObservableTreeMap<String,Participant>();
	private Map<String, Participant> completelyExcludedParticipants = new ObservableTreeMap<String,Participant>();
	
	
	public ProtocolPoll(Poll poll){
		super(poll.getId(), poll.getQuestion(), poll.getStartTime(), poll.getOptions(), poll.getParticipants(), poll.isTerminated());
		G_q = GStarModSafePrime.getInstance(p);
		Z_q = G_q.getZModOrder();
		generator = G_q.getDefaultGenerator();
	}
	
	/**
	 * Get the safe prime group used in the protocol
	 * @return the safe prime group used in the protocol
	 */
	public GStarMod getG_q() {
		return G_q;
	}

	/**
	 * Get the additive group used in the protocol
	 * @return the additive group used in the protocol
	 */
	public ZMod getZ_q() {
		return Z_q;
	}

	/**
	 * Get the generator of the group Gq used in the protocol
	 * @return the generator of the group Gq used in the protocol
	 */
	public Element getGenerator() {
		return generator;
	}

	/**
	 * Get the map containing all participant excluded during the protocol
	 * The recovery round will allow to recover them
	 * @return the map containing all participant excluded during the protocol
	 */
	public Map<String, Participant> getExcludedParticipants() {
		return excludedParticipants;
	}

	
	/**
	 * Get the map containing all participant excluded before the protocol
	 * The recovery round will NOT allow to recover them
	 * @return the map containing all participant excluded before the protocol
	 */
	public Map<String, Participant> getCompletelyExcludedParticipants() {
		return completelyExcludedParticipants;
	}

	
	
	
}
