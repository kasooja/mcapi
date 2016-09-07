package org.insightcentre.mcapi.rough;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.insightcentre.mcapi.utils.BasicFileTools;

public class MassimoTest {

	public static String sendPostAN(String message) throws Exception {
		String url = "http://140.203.155.226:8080/mcapi/rest/demo/annotateAnPost";
		//String url = "http://localhost:8080/mcapi/rest/demo/annotateAnPost";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		String USER_AGENT = "";
		//add reuqest header
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		String urlParameters = "xmlTxt="+message;
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		System.out.println(response);
		in.close();
		//print result
		return response.toString();

	}
	
	public static void main(String[] args) {
		String xml = BasicFileTools.extractText("/Users/kat/git/mcapi/resources/Act_constitution_final-Kenya.xml");
	
		String sendPostAN;
		try {
			sendPostAN = sendPostAN(xml);
			//System.out.println(sendPostAN);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
