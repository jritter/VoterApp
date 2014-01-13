package ch.bfh.evoting.voterapp.protocol.cgs97.xml;

import org.simpleframework.xml.Element;

public class XMLGqPair {
        
        @Element
        private XMLGqElement value1;
        @Element
        private XMLGqElement value2;

        public XMLGqPair(){}
        
        public XMLGqPair(XMLGqElement value1, XMLGqElement value2){
                this.value1 = value1;
                this.value2 = value2;
        }

        public XMLGqElement getValue1() {
                return value1;
        }

        public void setValue1(XMLGqElement value1) {
                this.value1 = value1;
        }

        public XMLGqElement getValue2() {
                return value2;
        }

        public void setValue2(XMLGqElement value2) {
                this.value2 = value2;
        }
        
        
        
}
