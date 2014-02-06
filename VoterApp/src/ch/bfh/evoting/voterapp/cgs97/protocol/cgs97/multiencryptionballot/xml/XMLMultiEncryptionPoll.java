package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml.XMLOption;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;

@Root(name="MultiEncryptionBallotVote")
public class XMLMultiEncryptionPoll {

        @Element
        private String question;
        @ElementList
        private List<XMLOption> options;
        @ElementList
        private List<XMLParticipant> participants;
        @Element
        private String p;
        @Element
        private String q;
        @Element
        private XMLGqElement generator;
        @Element
        private XMLGqElement publicKey;
        @Element
        private int threshold;
        
        public XMLMultiEncryptionPoll(){}
        
        public XMLMultiEncryptionPoll(String question, List<XMLOption> options,
                        List<XMLParticipant> participants, String p, String q, XMLGqElement generator, XMLGqElement publicKey, int threshold) {
                super();
                this.question = question;
                this.options = options;
                this.participants = participants;
                this.p = p;
                this.q = q;
                this.generator = generator;
                this.publicKey = publicKey;
                this.threshold = threshold;
        }
        
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
        public String getQ() {
            return q;
	    }
	    public void setQ(String q) {
	            this.q = q;
	    }
        public XMLGqElement getGenerator() {
                return generator;
        }
        public void setGenerator(XMLGqElement generator) {
                this.generator = generator;
        }

		public XMLGqElement getPublicKey() {
			return publicKey;
		}

		public void setPublicKey(XMLGqElement publicKey) {
			this.publicKey = publicKey;
		}

		public int getThreshold() {
			return threshold;
		}

		public void setThreshold(int threshold) {
			this.threshold = threshold;
		}        
        
}