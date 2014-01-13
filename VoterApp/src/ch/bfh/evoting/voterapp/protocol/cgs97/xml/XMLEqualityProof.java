package ch.bfh.evoting.voterapp.protocol.cgs97.xml;

import org.simpleframework.xml.Element;

//Proof of equality
//1 ProductGroupTuple[
//                1.1 ProductGroupTuple[1.1.1 GStarModElement[447], 1.1.2 GStarModElement[546]],
//                1.2 ZModElement[99], 
//                1.3 ZModElement[391]]

public class XMLEqualityProof {

      @Element
      private XMLGqElement value111;
      @Element
      private XMLGqElement value112;
      @Element
      private XMLZqElement value12;
      @Element
      private XMLZqElement value13;
      
      public XMLEqualityProof(){}

      public XMLEqualityProof(XMLGqElement value111, XMLGqElement value112,
                      XMLZqElement value12, XMLZqElement value13) {
              super();
              this.value111 = value111;
              this.value112 = value112;
              this.value12 = value12;
              this.value13 = value13;
      }

      public XMLGqElement getValue111() {
              return value111;
      }

      public void setValue111(XMLGqElement value111) {
              this.value111 = value111;
      }

      public XMLGqElement getValue112() {
              return value112;
      }

      public void setValue112(XMLGqElement value112) {
              this.value112 = value112;
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
