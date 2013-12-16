package ch.bfh.evoting.voterapp.util.xml;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class XMLPoll {

	@Element
	private String question;
	@ElementList
	private List<XMLOption> options;
	@ElementList
	private List<XMLParticipant> participants;

	@Element
	private String p = "139700725455163817218350367330248482081469054544178136562919376411664993761516359902466877562980091029644919043107300384411502932147505225644121231955823457305399024737939358832740762188317942235632506234568870323910594575297744132090375969205586671220221046438363046016679285179955869484584028329654326524819";
	@Element
	private String generator;
	@Element
	private String tempGenerator;
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<XMLOption> getOptions() {
		return options;
	}
	public void setOptions(List<XMLOption> options) {
		this.options = options;
	}
	public List<XMLParticipant> getParticipants() {
		return participants;
	}
	public void setParticipants(List<XMLParticipant> participants) {
		this.participants = participants;
	}
	public String getP() {
		return p;
	}
	public void setP(String p) {
		this.p = p;
	}
	public String getGenerator() {
		return generator;
	}
	public void setGenerator(String generator) {
		this.generator = generator;
	}
	public String getTempGenerator() {
		return tempGenerator;
	}
	public void setTempGenerator(String tempGenerator) {
		this.tempGenerator = tempGenerator;
	}
	

	
	
}
