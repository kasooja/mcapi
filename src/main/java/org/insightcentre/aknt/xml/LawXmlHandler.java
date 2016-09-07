package org.insightcentre.aknt.xml;



import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LawXmlHandler extends DefaultHandler {

	private Paragraph currentPara = null;	
	private Law law = new Law();
	private boolean takeText = false;
	private boolean inThePara = false;

	private String tagStringValue = "";

	public LawXmlHandler(Law law) {
		this.law = law;		
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		if (qName.equalsIgnoreCase("paragraph")) {			
			//id="sct1-par001"
			currentPara = new Paragraph();
			String id = attributes.getValue("id");
			currentPara.id = id;
			currentPara.content = new Content();
			inThePara = true;
			law.paras.add(currentPara);
		}

//		if (qName.equalsIgnoreCase("content")) {
//			//takeText = true;
//		}

		//		if (qName.equalsIgnoreCase("listIntroduction")) {
		//			takeText = true;
		//		}
		//		
		//		if (qName.equalsIgnoreCase("item")) {
		//			takeText = true;
		//		}

		if (qName.equalsIgnoreCase("p") && inThePara) {
			takeText = true;
		}

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
//		if(qName.equalsIgnoreCase("content") ){			
//			//currentPara.content = tagStringValue.trim();
//			tagStringValue = "";
//			takeText = false;
//		}
		if (qName.equalsIgnoreCase("p") && inThePara) {
			currentPara.content.pTextContent.add(tagStringValue.trim());
			tagStringValue = "";
			takeText = false;
		}
		if (qName.equalsIgnoreCase("paragraph")) {
			inThePara = false;
		}
	}


	public void characters(char ch[], int start, int length) throws SAXException {
		if (takeText) 
			tagStringValue = tagStringValue + " " + new String(ch, start, length);		
	}

}