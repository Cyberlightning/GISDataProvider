package com.cyberlightning.webserver.services;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public abstract class TranslationService {
	
	
	public static Properties properties =  new Properties();
	public static JSONObject jsonObject = null;


	public void parse() {

		    try {

		    	JSONParser jsonParser = new JSONParser();
		        File file = new File("/home/tomi/git/Cyber-WeX/RealVirtualIntegration/WebServer/miserables.json");

		        Object object = jsonParser.parse(new FileReader(file));

		        jsonObject = (JSONObject) object;

		        parseJson(jsonObject);

		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
		}
		public static JSONObject parseJSON (String _unparsed) {
			 JSONParser jsonParser = new JSONParser();
			try {
				jsonObject = (JSONObject) jsonParser.parse(_unparsed);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return jsonObject;
			
		}
		public static void getArray(Object object2) throws ParseException {

		    JSONArray jsonArr = (JSONArray) object2;

		    for (int k = 0; k < jsonArr.size(); k++) {

		        if (jsonArr.get(k) instanceof JSONObject) {
		            parseJson((JSONObject) jsonArr.get(k));
		        } else {
		            System.out.println(jsonArr.get(k));
		        }

		    }
		}

		public static void parseJson(JSONObject jsonObject) throws ParseException {

		    Set<Object> set = jsonObject.keySet();

		    Iterator<Object> iterator = set.iterator();

		    while (iterator.hasNext()) {
		        Object obj = iterator.next();
		        if (jsonObject.get(obj) instanceof JSONArray) {
		            System.out.println(obj.toString());
		            getArray(jsonObject.get(obj));
		        } else {
		            if (jsonObject.get(obj) instanceof JSONObject) {
		                parseJson((JSONObject) jsonObject.get(obj));
		            } else {
		                System.out.println(obj.toString() + "\t" + jsonObject.get(obj));
		            }
		        }
		    }

		}
		
		public static JSONObject getJson() {
			Object object = null;			
			try {

			        JSONParser jsonParser = new JSONParser();
			        
			        File file = new File("/home/tomi/git/Cyber-WeX/RealVirtualIntegration/WebServer/miserables.json");

			         object = jsonParser.parse(new FileReader(file));

			        jsonObject = (JSONObject) object;

			        parseJson(jsonObject);

			    } catch (Exception ex) {
			        ex.printStackTrace();
			    }
			 return (JSONObject) object;
		}

}
