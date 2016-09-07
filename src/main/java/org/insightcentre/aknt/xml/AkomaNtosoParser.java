package org.insightcentre.aknt.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBException;

import org.akomantoso.api.AnDocType;
import org.akomantoso.api.AnVersion;
import org.akomantoso.schema.v2.HierarchicalStructure;
import org.insightcentre.mcapi.utils.BasicFileTools;
import org.xml.sax.SAXException;

public class AkomaNtosoParser {
	
	public static void main(String[] args) throws IOException {
		//String dataPath = "/Users/kat/git/mcapi/resources/Act_constitution_final-Kenya.xml";
		String dataPath = "/Users/kat/git/mcapi/resources/akomantoso_act_csd07.xml";
		try {
			AnVersion v = new AnVersion(3, "CSD07");
			InputStream schemaForVersion = v.getSchemaForVersion();
			BufferedReader reader = new BufferedReader(new InputStreamReader(schemaForVersion));
			String line = null;
			while((line=reader.readLine())!=null){
				System.out.println(line);
			}
			String text = BasicFileTools.extractText(dataPath);
			System.out.println(text);
			AnDocType docType = AnParserKat.parse(v, new File(dataPath));
			Object objType = (HierarchicalStructure) docType.objType;
			
			System.out.println(docType.typeName);
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
