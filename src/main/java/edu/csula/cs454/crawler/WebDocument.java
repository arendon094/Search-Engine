package edu.csula.cs454.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;


public class WebDocument {
	
	private Document doc= null;
	private Response response = null;
	private String documentId = null;
	private boolean isHtml;
	private String fileExtesion = null;
	private String path = "";
	//private static HashSet<String> stopwords = null;
	//private static StandardFilter standardFilter = null;
	private static CharArraySet stopSet = null;

	public WebDocument(Document doc){
		//This assumes the document passed in is an html document 
		this.doc = doc;
		isHtml = true;
		fileExtesion="html";
	}
	
	public WebDocument(Response resp){
		//This assues that the docuemnt passed in is something other than an html document 
		this.response = resp;
		isHtml = false;
	}
	
	public String getUrl(){
		if(isHtml)return doc.location().toLowerCase().trim();
		else return clean(response.url().toString().toLowerCase().trim()); 				
	}
	
	private String clean(String dirtyUrl){
		
		//remove query String
		for(int i = 0, length = dirtyUrl.length();i < length;i++)
		{
			if(dirtyUrl.charAt(i) == '?')return dirtyUrl.substring(0,i);	
		}
		return dirtyUrl;
	}
	
	public static String[] extractMeaningfulContent(String content){
		//TODO this can be optamized 
		  Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(content));
		  //CharArraySet stopSet = CharArraySet.copy(Version.LUCENE_36, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		  //TODO: maybe add some
		  if(stopSet == null){
			//  stopwords = new HashSet<String>();
			  stopSet = CharArraySet.copy(Version.LUCENE_36, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			  try{
				  BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("stopwords.txt")));
				  String word;
			      while((word = bufferedReader.readLine()) != null) 
			      {
			    	 stopSet.add(word);
			    	  //stopwords.add(word);
			      }   
				  
				  
			  }catch(Exception e){
				  e.printStackTrace();
			  }
			  
		  }
		  //stopSet.add("for");
		  //stopSet.add("the");
		  StandardFilter standardFilter = new StandardFilter(Version.LUCENE_36, tokenizer);
		  StopFilter stopFilter = new StopFilter(Version.LUCENE_36, standardFilter, stopSet);
		  CharTermAttribute charTermAttribute = (CharTermAttribute) tokenizer.addAttribute(CharTermAttribute.class);
		  ArrayList<String> words = new ArrayList<String>();
		    try { 	
				 while(stopFilter.incrementToken()){
			        String token = charTermAttribute.toString().toString().toLowerCase();
			        //TODO may want to do some stemming here 
			        if(isLikelyAWord(token))words.add(token);
				 }
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		    int length = words.size();
		    String[] finalWords = new String[length];
		    
		    for(int i = 0; i < length; i++)
		    {
		    	finalWords[i] = words.get(i);
		    }
		  return finalWords; 
		//return content.split(" ");
	}
		
	private static boolean isLikelyAWord(String word){
		int length = word.length();
		if(length == 1)return false;
		for(int i = 0; i  < length; i++)
		{
			int c = word.charAt(i);
			if((c >= 65 && c < 91))continue;
			if((c >= 97 && c < 123))continue;
			return false;
		}
		return true;
	}
	
	public String[] getContent(){
		
		if(isHtml)return extractMeaningfulContent(doc.select("body").text());
		else{
			
			if(getExtension()== "pdf"){
				  BodyContentHandler handler = new BodyContentHandler();
			      Metadata metadata = new Metadata();
			      FileInputStream inputstream;
				try {
				  inputstream = new FileInputStream(getPath());
				  ParseContext pcontext = new ParseContext();
			      //parsing the document using PDF parser
			      PDFParser pdfparser = new PDFParser(); 
			      pdfparser.parse(inputstream, handler, metadata,pcontext);
			      return extractMeaningfulContent(handler.toString());
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TikaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			     
			}
			
			return new String[]{};//TODO this is temparary  should check based on file type 
		}
	}
	
	public String getExtension(){
		if(fileExtesion != null)return fileExtesion;
		fileExtesion = FilenameUtils.getExtension(getUrl());
		if(fileExtesion == null) fileExtesion ="";
		//TODO if file extension is empty use tika to resolve;
		return fileExtesion;
	}
	
	public String getPath(){
		if(documentId == null)generateDocID();
		return path+"/"+documentId+"."+getExtension();
	}
	
	private void generateDocID(){
		documentId = MD5(getUrl()); 
	}
	
	private String MD5(String md5) {
		   try {
		        MessageDigest md = MessageDigest.getInstance("MD5");
		        byte[] array = md.digest(md5.getBytes());
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < array.length; ++i) {
		          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		        return sb.toString();
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
	}
	
	public void saveToDisk(String storagePath){
		path = storagePath;
		String fileName = getPath();
		//System.out.println("Saving to file:"+fileName);
		try {
				new FileOutputStream(fileName, false).close();
				if(isHtml){
					PrintWriter docFile = new PrintWriter (fileName, "UTF-8");
					docFile.print(doc.toString());
					docFile.close();					
				}else{
					OutputStream out = new FileOutputStream(fileName);
					out.write(response.bodyAsBytes());
					out.close();
				}			
				
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}			
	}
}
