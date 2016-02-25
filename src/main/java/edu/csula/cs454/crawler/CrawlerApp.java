package edu.csula.cs454.crawler;

public class CrawlerApp {
	 public static void main(String[] args){
		 //String seed = "http://www.calstatela.edu/";
		 //String seed = "http://maplestory.nexon.net/";
		 //-d <depth> -u <url> -e
		 args = new String[]{"-d","0","-u","http://www.noholita.fr/wp-content/uploads/2014/10/KI-Bikini-Body-Training-Guide.pdf",/*"-e",/* "1"*/};
		 if(args.length > 3 && args[0] == "-d" && args[2] == "-u")
		 {
			 String seed = args[3];
			 int depth = Integer.parseInt(args[1]);
			 CrawlerController controller = CrawlerController.getInstance();
			 controller.setStorageFolder("CrawlerStorage");
			 controller.setDepth(depth);
			 controller.addSeed(seed);
			 controller.setMetaDataStorageDatabase("CrawledData");
			 if(args.length >= 5 && args[4] == "-e"){
				 controller.enableExtraction(true);
				 if(args.length == 6)controller.setNumberOfExtractors(Integer.parseInt(args[5]));
			 }
			 else controller.enableExtraction(false);			 
			 controller.start(); 			 
			 
		 }else{
			 System.out.println("Not Enough Command Line arguments. Or incorrect argumuments passed in...");
			 for(String s : args){
				 System.out.print(s);
			 }
		 }
		
	  }
}
