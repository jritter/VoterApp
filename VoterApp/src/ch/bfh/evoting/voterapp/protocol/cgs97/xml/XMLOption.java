package ch.bfh.evoting.voterapp.protocol.cgs97.xml;

import org.simpleframework.xml.Element;


public class XMLOption {

        @Element
        private String text;
        @Element
        private int votes;
        
        public XMLOption(){}
        
        public XMLOption(String text, int votes) {
                super();
                this.text = text;
                this.votes = votes;
        }
        
        public String getText() {
                return text;
        }
        public void setText(String text) {
                this.text = text;
        }
        public int getVotes() {
                return votes;
        }
        public void setVotes(int votes) {
                this.votes = votes;
        }
}