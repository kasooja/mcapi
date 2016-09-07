package org.insightcentre.aknt.xml;


import java.io.ByteArrayInputStream;
import java.util.List;

import org.insightcentre.mcapi.utils.BasicFileTools;
import org.insightcentre.mcapi.utils.StanfordParser;
import org.xml.sax.helpers.DefaultHandler;

import edu.stanford.nlp.pipeline.Annotation;

public class AkntLawXMLReaderTest {	
	public static Law law = new Law();
	public static String dataPath = "/Users/kat/git/mcapi/resources/Act_constitution_final-Kenya.xml";
	//'may conduct investigations on its own initiative or'
	public static void main(String[] args) {		
		DefaultHandler handler = new LawXmlHandler(law);
		String xmlText = BasicFileTools.extractText(dataPath);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlText.getBytes());  
		LawReader lReader = new LawReader(byteArrayInputStream);
		lReader.read(handler);

		int paraIndex = 0;
		for(paraIndex=0; paraIndex<law.paras.size(); paraIndex++){
			int pContIndex = 0;
			for(pContIndex=0; pContIndex<law.paras.get(paraIndex).content.pTextContent.size(); pContIndex++){
				String text = law.paras.get(paraIndex).content.pTextContent.get(pContIndex);

				Annotation annotation = StanfordParser.getAnnotation(text);
				List<String> sentences = StanfordParser.getSentences(annotation);

				for(String sentence : sentences){
					if(sentence.contains("may conduct investigations on its own initiative")){
						System.out.println();
					}
					int length = sentence.split("\\s+").length;
					if(length<10){						
						continue;
					}
					System.out.println(sentence);
				}
			}
			//	System.out.println(law.paras.get(0).content.pTextContent.get(0));		
		}	

	}
}
