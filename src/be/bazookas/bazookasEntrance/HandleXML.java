package be.bazookas.bazookasEntrance;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class HandleXML {
	
	private String VideoName;
	private String VideoURL;
	private String Naam;
	private String Company;
	private ArrayList<Person> persons = new ArrayList<Person>();
	private ArrayList<Video> videos = new ArrayList<Video>();
	 private XmlPullParserFactory xmlFactoryObject;
	   public volatile boolean parsingComplete = true;
	   private String urlString;
	   public HandleXML(String url) {
		urlString = url;
	}
	public String getVideoName() {
		return VideoName;
	}
	public String getVideoURL() {
		return VideoURL;
	}
	public String getNaam() {
		return Naam;
	}
	public ArrayList<Person> getPersons() {
		return persons;
	}
	public ArrayList<Video> getVideos() {
		return videos;
	}
	public String getCompany() {
		return Company;
	}
	public void parseXMLAndStoreIt(XmlPullParser myParser) {
		int event;
	    String text=null;
	    Video v = null;
	    Person p = null;
	    try {
	    	event = myParser.getEventType();
	    	while (event != XmlPullParser.END_DOCUMENT) {
				String Name = myParser.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					if (Name.equals("video")){
						v=new Video();
					}
					else if (Name.equals("Person")) {
						p=new Person();
					}
					break;
				case XmlPullParser.TEXT:
					text = myParser.getText();
					break;
				case XmlPullParser.END_TAG:
					if (Name.equals("videoName")){
						VideoName = text;
						v.set_videoName(VideoName);
					}
					else if (Name.equals("videoUrl")) {
						VideoURL= text;
						v.set_videoURL(VideoURL);
					}
					else if (Name.equals("Naam")) {
						Naam = text;
						p.set_naam(Naam);
					}
					else if (Name.equals("Company")) {
						Company = text;
						p.set_company(Company);
					}
					else if (Name.equals("video")){
						videos.add(v);
					}
					else if (Name.equals("Person")) {
						persons.add(p);
					}
					break;
				}
				event = myParser.next();
				
			}
	    	parsingComplete = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void fetchXML(){
	      Thread thread = new Thread(new Runnable(){
	         @Override
	         public void run() {
	            try {
	               URL url = new URL(urlString);
	               HttpURLConnection conn = (HttpURLConnection) 
	               url.openConnection();
	                  conn.setReadTimeout(10000 /* milliseconds */);
	                  conn.setConnectTimeout(15000 /* milliseconds */);
	                  conn.setRequestMethod("GET");
	                  conn.setDoInput(true);
	                  conn.connect();
	            InputStream stream = conn.getInputStream();

	            xmlFactoryObject = XmlPullParserFactory.newInstance();
	            XmlPullParser myparser = xmlFactoryObject.newPullParser();

	            myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES
	            , false);
	            myparser.setInput(stream, null);
	            parseXMLAndStoreIt(myparser);
	            stream.close();
	            } catch (Exception e) {
	               e.printStackTrace();
	            }
	        }
	    });

	    thread.start(); 


	   }

}
