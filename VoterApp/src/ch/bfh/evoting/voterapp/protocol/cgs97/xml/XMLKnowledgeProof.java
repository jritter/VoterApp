package ch.bfh.evoting.voterapp.protocol.cgs97.xml;

import org.simpleframework.xml.Element;

public class XMLKnowledgeProof {

    @Element
    private XMLGqElement value11;
    @Element
    private XMLZqElement value12;
    @Element
    private XMLZqElement value13;
    
    public XMLKnowledgeProof(){}

    public XMLKnowledgeProof(XMLGqElement value11, XMLZqElement value12,
                    XMLZqElement value13) {
            super();
            this.value11 = value11;
            this.value12 = value12;
            this.value13 = value13;
    }

    public XMLGqElement getValue11() {
            return value11;
    }

    public void setValue11(XMLGqElement value11) {
            this.value11 = value11;
    }

    public XMLZqElement getValue12() {
            return value12;
    }

    public void setValue12(XMLZqElement value12) {
            this.value12 = value12;
    }

    public XMLZqElement getValue13() {
            return value13;
    }

    public void setValue13(XMLZqElement value13) {
            this.value13 = value13;
    }
    
}
