package edu.csula.cs454.crawler;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.MongoClient;
import com.mongodb.client.*;

public class DataExtractor extends Thread implements Runnable {
	
	private static CrawlerController controller = null;
	private static String storageFolder = null;
	private static String database = null;
	public void run(){
		if(controller == null)
		{
			controller = CrawlerController.getInstance();
			storageFolder = controller.getStorageFolder();
			database = controller.getMetaDataStorageDatabase();
		}
		MongoClient mongoClient =  new MongoClient();
		MongoCollection collection = mongoClient.getDatabase(database).getCollection("Docs");		
		while(controller.isCrawling() || controller.hasDocuments())
		{			
			while(controller.hasDocuments())
			{
				/*org.jsoup.nodes.Document doc = controller.getNextDocument();
				String docURL = doc.location();
				System.out.println("Absolute URL: "+docURL);	
				//Create object that will store metadata 
				org.bson.Document docMetaData = new org.bson.Document( "url", docURL);
				collection.insertOne(docMetaData);
				//create a string reference file (<storagefolder>/<mongoid>.html)
				String fileName = storageFolder+"/"+((ObjectId)docMetaData.get( "_id" )).toHexString()+".html";
				//write document to disk 
				PrintWriter docFile;
				try {
					docFile = new PrintWriter (fileName, "UTF-8");
					docFile.print(doc.toString());
					docFile.close();
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					System.out.print("Something Went Wrong when saving crawled file locally:(");
					e.printStackTrace();
					System.exit(0);
				}
				
				//get time stamp 
				//LocalDateTime localDateTime = LocalDateTime.now();
				
				//docMetaData.put();
				//String urlHash = 
				
				//System.out.println("TimeStamp: "+ localDateTime.toString());*/
	
			}			
		}	
		mongoClient.close();
	}
}
