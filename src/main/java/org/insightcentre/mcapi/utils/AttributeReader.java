package org.insightcentre.mcapi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeReader {

	public static Set<String> posSet = new HashSet<String>();
	public static LinkedHashSet<Pattern> posPatterns = new LinkedHashSet<Pattern>();
	public static Map<Pattern, Integer> patternVsGroups = new HashMap<Pattern, Integer>();
	public static Set<String> txtSet = new HashSet<String>();
	
	static {
		String regexStr = "(\\d\\.\\d+)\\s+\\d+\\s+(txt|pos)_(.+)";
		Pattern pattern = Pattern.compile(regexStr);

		String attFile = "/Users/kat/git/mcapi/resources/InfoGainAtts";
		//String attFile = "/home/karaso/mcapi/resources/InfoGainAtts";
		BufferedReader br = BasicFileTools.getBufferedReader(attFile);
		String line = "";
		try {
			while((line=br.readLine()) != null){
				Matcher matcher = pattern.matcher(line.trim());
				boolean isPos = true;
				String posOrTxt = null;
				if(matcher.find()){
				//	double sigScore = Double.parseDouble(matcher.group(1).trim());					
					if("txt".equalsIgnoreCase(matcher.group(2).trim())){
						isPos = false;
					}
					posOrTxt = matcher.group(3).trim();
				}
				if(isPos){
					posSet.add(posOrTxt);
					String[] split = posOrTxt.split("\\s+");					
					String constantReg = "([^\\s]+)_";
					StringBuffer bf = new StringBuffer();
					int t = 0;
					for(String s : split){
						bf.append(constantReg + s);
						if(t<split.length-1){
							 bf.append("\\s+");
							 t++;
						}							
					}
					Pattern compiledPattern = Pattern.compile(bf.toString().trim());
					patternVsGroups.put(compiledPattern, split.length);					
					posPatterns.add(compiledPattern);
				//	System.out.println(posOrTxt);
				//	System.out.println(bf.toString().trim());
				//	System.out.println(patternVsGroups.get(compiledPattern));
				} else {
					//System.out.println(posOrTxt);
			//		txtSet.add(posOrTxt);
				}				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
