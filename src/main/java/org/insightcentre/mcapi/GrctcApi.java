package org.insightcentre.mcapi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.insightcentre.aknt.xml.Law;
import org.insightcentre.aknt.xml.LawReader;
import org.insightcentre.aknt.xml.LawXmlHandler;
import org.insightcentre.mcapi.utils.AttributeReader;
import org.insightcentre.mcapi.utils.BasicFileTools;
import org.insightcentre.mcapi.utils.StanfordParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.jersey.multipart.FormDataParam;

import edu.stanford.nlp.pipeline.Annotation;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

@Path("/demo")
public class GrctcApi {

	private static final String TITLE = "Regulation Classification";
	public static Law law = new Law();
	private static DefaultHandler handler = new LawXmlHandler(law);
	private static String home = "/Users/kat/git/mcapi/resources/";
	//private static final String home = "/home/karaso/mcapi/resources/";

	private static final String modalityArff = "/Users/kat/git/mcapi/resources/USUKModality_updated_up.arff";
	//private static final String modalityArff = "/home/karaso/mcapi/resources/USUKModality_updated_up.arff";
	private static final ModalityClassifier modalityClassifierObj = new ModalityClassifier();	
	private static final Classifier modalityClassifier = modalityClassifierObj.getLearnedBinClassifier(modalityArff);
	private static Instances instances = new Instances(ModalityClassifier.trainingInstances, 0, 0);


	private static final String FACETS = "{" + 
			"\"Domain Specific Provisions\": {" + 
			"\"labels\": [\"Customer Due Diligence\","+ 
			"\"Enforcement\","+
			"\"Reporting\","+
			"\"Monitoring\""+ 
			"]" +
			"}," +  
			"\"Generic Provisions\":{" + 
			"\"labels\": [\"Prohibition\"," + 
			"\"Obligation\"," + 
			"\"Others\"]" +
			"}" + 	        
			"}";

	@GET
	@Path("/annotateGet")
	public Response annotateGet(@QueryParam("txt") String txt, @QueryParam("sent") String isSent) {
		boolean isSentence = true;

		if(isSent != null && !"".equals(isSent)){
			try {
				isSentence = Boolean.parseBoolean(isSent.toLowerCase().trim());	
			} catch(Exception e){
				isSentence = true;
			}
		}

		JSONObject jsonObj = null;

		if(isSentence){
			jsonObj = tagImpSentences(txt);
		} else {
			jsonObj = tagImpPhrases(txt);
		}				
		return Response.status(200).entity(jsonObj.toJSONString()).build();
	}

	@GET
	@Path("/annotateGetDemo")
	public Response annotateGetDemo(@QueryParam("txt") String txt, @QueryParam("sent") String isSent) {
		boolean isSentence = true;
		if(isSent != null && !"".equals(isSent)){
			try {
				isSentence = Boolean.parseBoolean(isSent.toLowerCase().trim());	
			} catch(Exception e){
				isSentence = true;
			}
		}

		JSONObject jsonObj = null;

		if(isSentence){
			jsonObj = tagImpSentencesDemo(txt);
		} else {
			jsonObj = tagImpSentencesDemo(txt);
		}				
		return Response.status(200).entity(jsonObj.toJSONString()).build();
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)	
	@Path("/annotatePost")
	public Response annotatePost(@FormParam("txt") String txt, @FormParam("sent") String isSent) {
		boolean isSentence = true;
		if(isSent != null && !"".equals(isSent)){
			try {
				isSentence = Boolean.parseBoolean(isSent.toLowerCase().trim());	
			} catch(Exception e){
				isSentence = true;
			}
		}

		JSONObject jsonObj = null;

		if(isSentence){
			jsonObj = tagImpSentences(txt);
		} else {
			jsonObj = tagImpPhrases(txt);
		}				
		return Response.status(200).entity(jsonObj.toJSONString()).build();
	}

	@POST		
	@Path("/annotateAnPost")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response annotateAnPost(@FormParam("xmlTxt") String xmlTxt) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlTxt.getBytes());  
		LawReader lReader = new LawReader(byteArrayInputStream);
		lReader.read(handler);
		String xml = tagImpSentencesAnuFileWithClassifier(law, xmlTxt);
		BasicFileTools.writeFile(home + "/akomaNtoso.xml", xml);
		return Response.ok(new File(home + "/akomaNtoso.xml"), MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + "akomaNtoso.xml" + "\"" ) //optional
				.build();		
	}

	@POST		
	@Path("/annotateAnPostFile")	
	@Consumes(MediaType.MULTIPART_FORM_DATA)	
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response annotateAnPostFile(@FormDataParam("xmlTxtFile") InputStream xmlTxtFileStream) {
		BufferedReader bfReader1 = BasicFileTools.getBufferedReader(xmlTxtFileStream);
		String xmlTxt = BasicFileTools.extractText(bfReader1, false);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlTxt.getBytes());		
		LawReader lReader = new LawReader(byteArrayInputStream);
		lReader.read(handler);
		String xml = tagImpSentencesAnuFileWithClassifier(law, xmlTxt);
		BasicFileTools.writeFile(home + "/akomaNtoso.xml", xml);
		return Response.ok(new File(home + "/akomaNtoso.xml"), MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + "akomaNtoso.xml" + "\"" ) //optional
				.build();		
	}

	@GET		
	@Path("/annotateAnGet")	
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response annotateAnGet(@QueryParam("xmlTxt") String xmlTxt) {		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlTxt.getBytes());  
		LawReader lReader = new LawReader(byteArrayInputStream);
		lReader.read(handler);
		String xml = tagImpSentencesAnuFileWithClassifier(law, xmlTxt);
		BasicFileTools.writeFile(home + "/akomaNtoso.xml", xml);
		return Response.ok(new File(home + "/akomaNtoso.xml"), MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + "akomaNtoso.xml" + "\"" ) //optional
				.build();		
	}


	private String tagImpSentencesAnuFile(Law law, String xmlText) {
		Set<String> impSentences = new HashSet<String>();
		int paraIndex = 0;
		for(paraIndex=0; paraIndex<law.paras.size(); paraIndex++){
			int pContIndex = 0;
			for(pContIndex=0; pContIndex<law.paras.get(paraIndex).content.pTextContent.size(); pContIndex++){
				String text = law.paras.get(paraIndex).content.pTextContent.get(pContIndex);

				Annotation annotation = StanfordParser.getAnnotation(text);
				List<String> sentences = StanfordParser.getSentences(annotation);

				for(String sentence : sentences){
					if(sentence.split("\\s+").length<10){
						continue;
					}
					String posTag = StanfordParser.getPosTag(sentence);
					System.out.println(posTag.toLowerCase());
					for(Pattern p : AttributeReader.posPatterns){
						Matcher matcher = p.matcher(posTag.toLowerCase());
						if(matcher.find()){
							Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
							StringBuffer bf = new StringBuffer();
							for(int o=1; o<numOfGroups+1; o++){
								String word = matcher.group(o);
								bf.append(word + " ");
							}
							impSentences.add(sentence.trim());
							break;
						}
					}
				}

			}
		}

		for(String sentence : impSentences){
			//System.out.println(sentence.toLowerCase());
			int from = xmlText.toLowerCase().indexOf(sentence.toLowerCase());
			int to = from + sentence.length();
			if(from==-1){
				continue;
			}
			xmlText = xmlText.substring(0, from) + "<ganeshamark>" + xmlText.substring(from, to) + "</ganeshamark>" + xmlText.substring(to, xmlText.length());
		}
		return xmlText;
	}	

	private String tagImpSentencesAnuFileWithClassifier(Law law, String xmlText) {
		Set<String> impSentences = new HashSet<String>();
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
					//String posTag = StanfordParser.getPosTag(sentence);
					if(sentence.split("\\s+").length<8){
						continue;
					}
					Instance instance = modalityClassifierObj.getInstance(sentence, instances);

					instances.add(instance);

					Instances filteredInstances;
					boolean imp = false;

					try {
						filteredInstances = Filter.useFilter(instances, ModalityClassifier.multiFilter);
						//double classifyInstance = modalityClassifier.classifyInstance(filteredInstances.firstInstance());
						double[] distributionForInstance = modalityClassifier.distributionForInstance(filteredInstances.firstInstance());

						System.out.println(distributionForInstance[0] + " " + distributionForInstance[1] + " " + distributionForInstance[2]);
						if(distributionForInstance[0] > 0.50 || distributionForInstance[1] > 0.50){
							imp = true;
						}
						instances.remove(0);
					} catch (Exception e) {						
						e.printStackTrace();
					}

					if(imp){
						impSentences.add(sentence.trim());
					}
				}

			}
		}

		for(String sentence : impSentences){
			//System.out.println(sentence.toLowerCase());
			int from = xmlText.toLowerCase().indexOf(sentence.toLowerCase());
			int to = from + sentence.length();
			if(from==-1){
				continue;
			}
			xmlText = xmlText.substring(0, from) + "<ganeshamark>" + xmlText.substring(from, to) + "</ganeshamark>" + xmlText.substring(to, xmlText.length());
		}
		return xmlText;
	}	


	private JSONObject tagImpSentences(String text) {
		JSONObject obj = new JSONObject();
		obj.put("text", text);	
		JSONArray annotations = new JSONArray();

		Set<String> impSentences = new HashSet<String>();
		Annotation annotation = StanfordParser.getAnnotation(text);
		List<String> sentences = StanfordParser.getSentences(annotation);



		for(String sentence : sentences){
			String posTag = StanfordParser.getPosTag(sentence);
			System.out.println(posTag.toLowerCase());
			for(Pattern p : AttributeReader.posPatterns){
				Matcher matcher = p.matcher(posTag.toLowerCase());
				if(matcher.find()){
					Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
					StringBuffer bf = new StringBuffer();
					for(int o=1; o<numOfGroups+1; o++){
						String word = matcher.group(o);
						bf.append(word + " ");
					}
					impSentences.add(sentence.trim());
					break;
				}
			}
		}

		for(String sentence : impSentences){
			System.out.println(sentence.toLowerCase());
			int from = text.toLowerCase().indexOf(sentence.toLowerCase());
			int to = from + sentence.length();
			if(from==-1){
				continue;
			}
			//	System.out.println("from: " +  indexOf + " to: " + endIndex);
			JSONObject anno = getJsonAnno(from, to);
			annotations.add(anno);		
		}
		obj.put("annotations", annotations);
		return obj;
	}	


	private JSONObject tagImpPhrases(String text) {
		JSONObject obj = new JSONObject();
		obj.put("text", text);	
		JSONArray annotations = new JSONArray();

		Set<String> impPhrases = new HashSet<String>();
		Annotation annotation = StanfordParser.getAnnotation(text);
		List<String> sentences = StanfordParser.getSentences(annotation);

		for(String sentence : sentences){
			String posTag = StanfordParser.getPosTag(sentence);
			System.out.println(posTag.toLowerCase());
			for(Pattern p : AttributeReader.posPatterns){
				Matcher matcher = p.matcher(posTag.toLowerCase());
				if(matcher.find()){
					Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
					StringBuffer bf = new StringBuffer();
					for(int o=1; o<numOfGroups+1; o++){
						String word = matcher.group(o);
						bf.append(word + " ");
					}
					impPhrases.add(bf.toString().trim());
				}
			}
		}

		for(String phrase : impPhrases){
			System.out.println(phrase);
			int from = text.toLowerCase().indexOf(phrase);
			int to = from + phrase.length();
			if(from==-1){
				continue;
			}
			//	System.out.println("from: " +  indexOf + " to: " + endIndex);
			JSONObject anno = getJsonAnno(from, to);
			annotations.add(anno);		
		}
		obj.put("annotations", annotations);
		return obj;
	}	


	private JSONObject tagImpSentencesDemo(String text) {
		JSONObject obj = new JSONObject();

		JSONArray annotations = new JSONArray();

		Set<String> impSentences = new HashSet<String>();
		Annotation annotation = StanfordParser.getAnnotation(text);
		List<String> sentences = StanfordParser.getSentences(annotation);

		for(String sentence : sentences){
			String posTag = StanfordParser.getPosTag(sentence);
			System.out.println(posTag.toLowerCase());
			for(Pattern p : AttributeReader.posPatterns){
				Matcher matcher = p.matcher(posTag.toLowerCase());
				if(matcher.find()){
					Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
					StringBuffer bf = new StringBuffer();
					for(int o=1; o<numOfGroups+1; o++){
						String word = matcher.group(o);
						bf.append(word + " ");
					}
					impSentences.add(sentence.trim());
					break;
				}
			}
		}

		boolean lastOneObligation = true;
		for(String sentence : impSentences){
			System.out.println(sentence.toLowerCase());
			int from = text.toLowerCase().indexOf(sentence.toLowerCase());
			int to = from + sentence.length();
			if(from==-1){
				continue;
			}			
			if(lastOneObligation){
				text = text.substring(0, from) + "<em title=\"Obligation\" data-toggle=\"tooltip\" class=\"obligation customer-due-diligence\">" + text.substring(from, to) + "</em>" + text.substring(to, text.length());
				lastOneObligation = false;
			} else {
				text = text.substring(0, from) + "<em title=\"Prohibition\" data-toggle=\"tooltip\" class=\"prohibition customer-due-diligence\">" + text.substring(from, to) + "</em>" + text.substring(to, text.length());
				lastOneObligation = true;
			}
			JSONObject anno = getJsonAnno(from, to);
			annotations.add(anno);		
		}
		obj.put("text", text.trim());
		obj.put("annotations", annotations);
		return obj;
	}	

	private JSONObject getJsonAnno(int from, int to){
		JSONObject anno = new JSONObject();
		anno.put("from", from);
		anno.put("to", to);
		return anno;
	}

	@GET
	@Path("/title")
	public Response getTitle() {
		return Response.status(200).entity(TITLE).build();
	}

	@GET
	@Path("/facets")
	public Response getSchema() {
		return Response.status(200).entity(FACETS).build();
	}

	public static void main(String[] args) {
		String totalText = "The bank should not register the customers. I do not like you.";
		Set<String> impPhrases = new HashSet<String>();
		Annotation annotation = StanfordParser.getAnnotation(totalText);
		List<String> sentences = StanfordParser.getSentences(annotation);
		for(String sentence : sentences){
			String posTag = StanfordParser.getPosTag(sentence);
			System.out.println(posTag.toLowerCase());
			for(Pattern p : AttributeReader.posPatterns){
				Matcher matcher = p.matcher(posTag.toLowerCase());
				if(matcher.find()){
					Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
					StringBuffer bf = new StringBuffer();
					for(int o=1; o<numOfGroups+1; o++){
						String word = matcher.group(o);
						bf.append(word + " ");
						//System.out.print(word + " ");
					}
					impPhrases.add(bf.toString().trim());
				}
			}
		}

		for(String phrase : impPhrases){
			System.out.println(phrase);
			int indexOf = totalText.toLowerCase().indexOf(phrase);
			int endIndex = indexOf + phrase.length();
			System.out.println("from: " +  indexOf + " to: " + endIndex);
		}
	}

}


//private JSONObject tagImpSentencesAnu(Law law, String xmlText) {
//	JSONObject obj = new JSONObject();		
//	JSONArray annotations = new JSONArray();
//	Set<String> impSentences = new HashSet<String>();
//	int paraIndex = 0;
//	for(paraIndex=0; paraIndex<law.paras.size(); paraIndex++){
//		int pContIndex = 0;
//		for(pContIndex=0; pContIndex<law.paras.get(paraIndex).content.pTextContent.size(); pContIndex++){
//			String text = law.paras.get(paraIndex).content.pTextContent.get(pContIndex);
//
//			Annotation annotation = StanfordParser.getAnnotation(text);
//			List<String> sentences = StanfordParser.getSentences(annotation);
//
//			for(String sentence : sentences){
//				String posTag = StanfordParser.getPosTag(sentence);
//				System.out.println(posTag.toLowerCase());
//				for(Pattern p : AttributeReader.posPatterns){
//					Matcher matcher = p.matcher(posTag.toLowerCase());
//					if(matcher.find()){
//						Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
//						StringBuffer bf = new StringBuffer();
//						for(int o=1; o<numOfGroups+1; o++){
//							String word = matcher.group(o);
//							bf.append(word + " ");
//						}
//						impSentences.add(sentence.trim());
//						break;
//					}
//				}
//			}
//
//		}
//	}
//
//	for(String sentence : impSentences){
//		//System.out.println(sentence.toLowerCase());
//		int from = xmlText.toLowerCase().indexOf(sentence.toLowerCase());
//		int to = from + sentence.length();
//		if(from==-1){
//			continue;
//		}
//		xmlText = xmlText.substring(0, from) + "<mark>" + xmlText.substring(from, to) + "<mark>" + xmlText.substring(to, xmlText.length());
//		
//		//	System.out.println("from: " +  indexOf + " to: " + endIndex);
//		JSONObject anno = getJsonAnno(from, to);
//		annotations.add(anno);		
//	}
//	
//	obj.put("text", StringEscapeUtils.unescapeJava(xmlText));
//
//	obj.put("annotations", annotations);
//	return obj;
//}



//private JSONObject tagImpSentencesAn(Law law, String xmlText) {
//JSONObject obj = new JSONObject();
//obj.put("text", xmlText);	
//
//JSONArray annotations = new JSONArray();
//
//Set<String> impSentences = new HashSet<String>();
//
//int paraIndex = 0;
//for(paraIndex=0; paraIndex<law.paras.size(); paraIndex++){
//	int pContIndex = 0;
//	for(pContIndex=0; pContIndex<law.paras.get(paraIndex).content.pTextContent.size(); pContIndex++){
//		String text = law.paras.get(paraIndex).content.pTextContent.get(pContIndex);
//
//		Annotation annotation = StanfordParser.getAnnotation(text);
//		List<String> sentences = StanfordParser.getSentences(annotation);
//
//		for(String sentence : sentences){
//			String posTag = StanfordParser.getPosTag(sentence);
//			System.out.println(posTag.toLowerCase());
//			for(Pattern p : AttributeReader.posPatterns){
//				Matcher matcher = p.matcher(posTag.toLowerCase());
//				if(matcher.find()){
//					Integer numOfGroups = AttributeReader.patternVsGroups.get(p);
//					StringBuffer bf = new StringBuffer();
//					for(int o=1; o<numOfGroups+1; o++){
//						String word = matcher.group(o);
//						bf.append(word + " ");
//					}
//					impSentences.add(sentence.trim());
//					break;
//				}
//			}
//		}
//
//	}
//}
//
//for(String sentence : impSentences){
//	System.out.println(sentence.toLowerCase());
//	int from = xmlText.toLowerCase().indexOf(sentence.toLowerCase());
//	int to = from + sentence.length();
//	if(from==-1){
//		continue;
//	}
//	//	System.out.println("from: " +  indexOf + " to: " + endIndex);
//	JSONObject anno = getJsonAnno(from, to);
//	annotations.add(anno);		
//}
//obj.put("annotations", annotations);
//return obj;
//}	
