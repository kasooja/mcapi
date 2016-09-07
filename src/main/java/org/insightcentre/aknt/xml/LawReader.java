package org.insightcentre.aknt.xml;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class LawReader {
	
	public String path;
	public InputStream stream;
		
	public LawReader(String dataPath) {
		this.path = dataPath;
		try {
			stream = new FileInputStream(dataPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public LawReader(InputStream stream) {
		this.stream = stream;
	}

	public void read(DefaultHandler handler){
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();		
			saxParser.parse(stream, handler);			
		} catch (Exception e){
			e.printStackTrace();
		}
	} 
}







