package edu.csula.cs454.crawler;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.bson.types.ObjectId;

@Entity
public class DocumentMetadata {
	
	@Id private ObjectId id;
	private String url;
	private String path;
	private String[] content;
	private String ext;//file extension
	public String[] getContent() {
		return content;
	}

	public void setURL(String docURL) {
		url = docURL;
	}
	public ObjectId getID(){
		return id;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	public void setContent(String[] content) {
		this.content = content;		
	}
	public void setFileExtestion(String extension) {
		ext = extension;
	}
}
