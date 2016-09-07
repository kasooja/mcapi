package org.insightcentre.mcapi.rough;

import java.io.File;
import java.io.IOException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class ApiTest {


	public static void callApiUsingUnirest(){
		try {
			HttpResponse<String> jsonResponse = Unirest.post("http://140.203.155.226:8080/mcapi/rest/demo/annotateAnPostFile")			
					  .field("xmlTxtFile", new File("/Users/kat/git/mcapi/resources/Act_constitution_final-Kenya.xml"))
					  .asString();
			System.out.println(jsonResponse.getBody());
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public static void okRequest(){
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("multipart/form-data; boundary=---011000010111000001101001");
		RequestBody body = RequestBody.create(mediaType, "-----011000010111000001101001\r\nContent-Disposition: form-data; name=\"xmlTxtFile\"; filename=\"[object Object]\"\r\nContent-Type: false\r\n\r\n\r\n-----011000010111000001101001--");
		Request request = new Request.Builder()
				.url("http://140.203.155.226:8080/mcapi/rest/demo/annotateAnPostFile")
				.post(body)
				.addHeader("content-type", "multipart/form-data; boundary=---011000010111000001101001")
				.addHeader("cache-control", "no-cache")
				.addHeader("postman-token", "c00ac606-81c0-0a2b-fb9a-25fcc6ef24c4")
				.build();
		try {
			Response response = client.newCall(request).execute();
			System.out.println(response.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//okRequest();
		callApiUsingUnirest();
	}
}
