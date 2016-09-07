package org.insightcentre.mcapi;


import java.util.Enumeration;

import org.insightcentre.mcapi.utils.Commons;
import org.insightcentre.mcapi.utils.StanfordParser;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ModalityClassifier {

	public static Instances trainingInstances;

	private static String bsaChapterxNoisePattern1String = "VerDate(?s).*?Regulations";
	private static String crimeActNoisePattern1String = "Crime and Courts Act 2013 \\(c\\. 22\\)";
	private static String seriousCrimeActNoisePattern1String = "Serious Organised Crime and Police Act 2005 \\(c\\. 15\\)";
	private static String terrorismActNoisePattern1String = "24-07-00 23:00:.*ch.*\\s*ACT\\s*.*\\s\\d+.*Terrorism\\s*Act\\s*2000";
	private static String doubleSpacesPatternString = "\\s\\s+"; 
	private static String weirdCharacterPatternString = "œ|æ|ß|ð|ø|å|ł|þ|�|ï|¿|½|â|€|“|”|™|˜";
	private static String weirdPatternString = "\\W\\W\\w+;";
	private static String slashNs = "\n";
	private static String singleCharAtLineStarts = "\\b\\w\\s+(.*)";
	private static String unnecessaryQuote = "\\s+”\\s+";
	public static MultiFilter multiFilter;

	//	private void setAttributesWithNominalClass(List<String> annotationTypeList) {
	//		textAttribute = new Attribute("text", (ArrayList<String>) null);		
	//		arkFramesAttribute = new Attribute("arkFrames", (ArrayList<String>) null);
	//		posTagSeqAttribute = new Attribute("posTagSeq", (ArrayList<String>) null);
	//		atts.add(new Attribute("Label", annotationTypeList));
	//		atts.add(textAttribute);		
	//		atts.add(arkFramesAttribute);
	//		atts.add(posTagSeqAttribute);
	//	}


	public String removeNoise(String content){
		content = content.replaceAll(bsaChapterxNoisePattern1String, " ").trim();
		content = content.replaceAll(crimeActNoisePattern1String, " ").trim();
		content = content.replaceAll(seriousCrimeActNoisePattern1String, " ").trim();
		content = content.replaceAll(terrorismActNoisePattern1String, " ").trim();
		content = content.replaceAll(weirdCharacterPatternString, " ").trim();
		content = content.replaceAll(weirdPatternString, " ").trim();
		content = content.replaceAll(singleCharAtLineStarts, " $1 ").trim();
		content = content.replaceAll(unnecessaryQuote, " ").trim();
		content = content.replaceAll(doubleSpacesPatternString, " ").trim();				
		return content;
	}

	public Instance getInstance(String sentence, Instances trainingInstances){		
		sentence  = sentence.replaceAll(slashNs, " ").trim();
		sentence = removeNoise(sentence);
		StringBuilder posTagSeq = new StringBuilder();
		String taggedSentence = StanfordParser.getPosTag(sentence);
		String[] split = taggedSentence.split("\\s+");
		for(String s : split){
			String pos = s.split("_")[1];
			posTagSeq.append(pos + " ");				
		}		
		double[] vals = new double[trainingInstances.numAttributes()];
		Enumeration<Attribute> enumerateAttributes = trainingInstances.enumerateAttributes();
		while(enumerateAttributes.hasMoreElements()){
			Attribute attribute = enumerateAttributes.nextElement();
			String attributeName = attribute.name().toLowerCase();
			int attributeIndex = attribute.index();			
			if("text".equalsIgnoreCase(attributeName)){				
				vals[attributeIndex] = trainingInstances.attribute("text").addStringValue(sentence.replace("class", "classwekaattribute").trim());					
			}
			if("posTagSeq".equalsIgnoreCase(attributeName)){				
				vals[attributeIndex] = trainingInstances.attribute("posTagSeq").addStringValue(posTagSeq.toString().replace("class", "classwekaattribute").trim());						
			}
		}
		Instance instance = new DenseInstance(1.0, vals);
		instance.setDataset(trainingInstances);
		return instance;
	}

	public Classifier getLearnedBinClassifier(String arffFileNameNonFilt) {
		//trainingInstances = loadInstances(arffFileNameNonFilt);
		DataSource source;
		try {
			source = new DataSource(arffFileNameNonFilt);
			trainingInstances = source.getDataSet();
			Attribute classAttr = trainingInstances.attribute("Label");
			trainingInstances.setClass(classAttr);
			AttributeSelection attrSel = Commons.getAttributeSelectionFilter();
			Reorder reorder = Commons.getReorderFilter();
			StringToWordVector wordVector = Commons.getStringToWordVectorFilter();
			multiFilter = new MultiFilter();			
			multiFilter.setFilters(new Filter[]{wordVector, reorder, attrSel});		
			LibSVM svm = new LibSVM();
			//String[] options = "-S 0 -K 2 -D 3 -G 0.1 -R 0.1 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -Z -B -seed 1".split("\\s+");
			svm.setCoef0(0.1);
			svm.setGamma(0.1);
			svm.setNormalize(true); 
			svm.setProbabilityEstimates(true);
			svm.setWeights("11 4 1");
			//svm.setOptions(options);
			multiFilter.setInputFormat(trainingInstances);
			Instances trainingInstancesFiltered = Filter.useFilter(trainingInstances, multiFilter);
			try {
				svm.buildClassifier(trainingInstancesFiltered);				
				//				//filteredClassifier.buildClassifier(trainingInstances);			
				//				Evaluation eval = new Evaluation(trainingInstances);
				//				//eval.crossValidateModel(svm, filteredClassifier, trainingInstances, 10, new Random(2));
				//				eval.crossValidateModel(svm, trainingInstances, 10, new Random(2));
				//				double[][] confusionMatrix = eval.confusionMatrix();
				//				System.out.println("Confusion Matrix");
				//				for(int i=0; i<confusionMatrix.length; i++){
				//					for(int j=0; j<confusionMatrix[i].length; j++){
				//						System.out.print(confusionMatrix[i][j] + " ");
				//					}
				//					System.out.println();
				//				}
				//				System.out.println(eval.toSummaryString("\nResults\n======\n", true));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return svm;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static Instances getTrainingInstances() {
		return trainingInstances;
	}

	public void setTrainingInstances(Instances trainingInstances) {
		this.trainingInstances = trainingInstances;
	}

	public static void main(String[] args) {
		String modalityArff = "/Users/kat/Work/API/USUKModality_updated_up.arff";
		ModalityClassifier modalityClassifierObj = new ModalityClassifier();	
		Classifier modalityClassifier = modalityClassifierObj.getLearnedBinClassifier(modalityArff);
		String sentence0 = "to clean and safe water in adequate quantities;";
		String sentence1 = "may conduct investigations on its own initiative or on a complaint made by a member of the public;";
	
		String sentence2 = "may conduct investigations on its own initiative or on a \n                                complaint made by a member of the public;";
		String sentence3 = "The bank should not register the customers without passport.";
		String sentence4 = "the nature of the right or fundamental freedom";
		String sentence5 = "Each person who receives in the U.S. currency or other monetary instruments in an aggregate amount exceeding $ 10 , 000 at one time which have been transported , mailed , or shipped to such person from any place outside the United States with respect to which report has not been filed under paragraph -LRB- a -RRB- of this section";
		String[] sentences = new String[5];
		sentences[0] = sentence1; sentences[1] = sentence2; sentences[2] = sentence3; sentences[3] = sentence4; sentences[4] = sentence5;		


		Instances instances = new Instances(trainingInstances, 0, 0);

		for(String sentence : sentences){
			System.out.println(sentence);
			if(sentence.contains("KENYA")){
				System.out.println("this");
			}
			Instance instance = modalityClassifierObj.getInstance(sentence, trainingInstances);
			try {
				instances.add(instance);

				Instances filteredInstances = Filter.useFilter(instances, multiFilter);

				//double classifyInstance = modalityClassifier.classifyInstance(filteredInstances.firstInstance());
				double[] distributionForInstance = modalityClassifier.distributionForInstance(filteredInstances.firstInstance());
				System.out.println(distributionForInstance[0] + " " + distributionForInstance[1] + " " + distributionForInstance[2]);
				instances.remove(0);

				//System.out.println(classifyInstance);
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}


}
