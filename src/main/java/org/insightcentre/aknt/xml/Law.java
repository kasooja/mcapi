package org.insightcentre.aknt.xml;


import java.util.ArrayList;
import java.util.List;

public class Law {

	public List<Paragraph> paras = new ArrayList<Paragraph>();
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(paras.get(0));
		return buffer.toString().trim();
	}

}
