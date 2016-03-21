package edu.csula.cs454.indexer;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import edu.csula.cs454.crawler.DocumentMetadata;
import edu.csula.cs454.ranker.ToastRanker;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.io.File;
import java.util.*;

/**
 * Created by esauceda on 2/26/16.
 */
public class IndexDocuments {
    public static void main(String args[]){
        final Morphia morphia = new Morphia();
        morphia.map(Index.class);
        morphia.map(DocumentMetadata.class);        
        final Datastore ds = morphia.createDatastore(new MongoClient(), "CrawledData");
        morphia.setUseBulkWriteOperations(true);
        String APP_ID = args[0];
        String APP_SECRET = args[1];
        ds.delete(ds.createQuery(Index.class));
        ds.delete(ds.createQuery(ImgIndex.class));
        ToastRanker ranker = new ToastRanker();
        Query<DocumentMetadata> documents = ds.find(DocumentMetadata.class);
        ClarifaiClient clarifai = new ClarifaiClient(APP_ID, APP_SECRET);
        int counter = 0;
        int size = documents.asList().size();
        HashMap<String, Index> indexMap = new HashMap<String, Index>();
        HashMap<String, ImgIndex> imgIndexMap = new HashMap<String, ImgIndex>();
        HashSet<String> files = new HashSet<String>();

        for (DocumentMetadata doc : documents){

            //Image recognition
            ranker.addDocument(doc);
            if (doc.getContent().length == 0 && !files.contains(doc.getURL())){
                files.add(doc.getURL());
                File img = new File(doc.getPath());
                List<RecognitionResult> result = clarifai.recognize(new RecognitionRequest(img));
                    if (result.get(0).getStatusMessage().equals("OK")){
                        for (Tag tag : result.get(0).getTags()) {
                            if (!termExistsInImgIndex(ds, tag.getName(), doc.getID(), tag.getProbability())) {
                                Map<ObjectId, Double> locations = new HashMap<ObjectId, Double>();
                                locations.put(doc.getID(), tag.getProbability());
                                ImgIndex newImg = new ImgIndex();
                                newImg.setTag(tag.getName());
                                newImg.setLocations(locations);
                                ds.save(newImg);
                            }
                        }
                    }
            }
            for (String term : doc.getContent()) {
                //if term has more than 10 locations, add it to the masterIndexList
                    Index mappedIndex = indexMap.get(term);
                    if (mappedIndex == null){
                        //check to make sure index is not in db
                        Query<Index> query = ds.find(Index.class, "term ==", term);
                        if (query.asList().size() > 0){
                            //use this index, add location, add to hashmap
                            Index queriedIndex = query.asList().get(0);
                            queriedIndex.addLocation(doc.getID());
                            indexMap.put(term, queriedIndex);
                        } else {
                            Map<ObjectId, Integer> locations = new HashMap<ObjectId, Integer>();
                            locations.put(doc.getID(), 1);
                            Index newIndex = new Index();
                            newIndex.setTerm(term);
                            newIndex.setLocations(locations);
                            indexMap.put(term, newIndex);
                            //create new index + location, add to hashmap
                        }
                    } else {
                        mappedIndex.addLocation(doc.getID());
                        //use this index, add location, save to hashmap
                    }
                //Check to make sure term hasn't been found in document yet
                //if term hasn't been found:
                //check to make sure it isn't in the db
                //if it isn't in the db, new index + term in hashmap
                //else: find index + term in hashmap
                //else: add location to index in hashmap
                //once done, insert to db
//                if (!termExistsInIndex(ds, term, doc.getID(), terms, indexes)) {
//                    Map<ObjectId, Integer> locations = new HashMap<ObjectId, Integer>();
//                    locations.put(doc.getID(), 1);
//                    Index newTerm = new Index();
//                    newTerm.setTerm(term);
//                    newTerm.setLocations(locations);
//                    terms.add(term);
//                    indexes.add(newTerm);
//                }
            }

            counter++;
            if (counter % 100 == 0) {
                System.out.println(counter);
            }
//            if (counter % 1000 == 0){
//                System.out.println("Processed " + counter + " Documents out of " + size);
//                ds.save(indexMap.values(), WriteConcern.UNACKNOWLEDGED);
//                System.out.println("Saved");
//                indexMap.clear();
//                System.out.println("Cleared");
//            }

        }
        ds.save(indexMap.values());
        System.out.println("Done indexing");
        System.out.println("Let the ranking begin!!!!");
        DocumentMetadata[] rankedDocs = ranker.rankDocumentsUsingSecretToastMethod();
        System.out.println("Saving Ranks");
        ds.save(rankedDocs);
        System.out.println();
        System.out.print("Ranking is complete!!");
        //calculateTfIdf(ds, totalDocs);
    }

    /**
     * Similar to termExistsInIndex, this function checks to see if a term (tag) already exists in our Index ImgIndex.
     * If it does, append a location to that term document and save.
     * @param ds: The datastore we are saving to. "CrawledData".
     * @param tag: The term (tag) we are searching for.
     * @param id: An ObjectId referencing the document that the term was found in.
     * @return Boolean; True if the term exists, otherwise False.
     */
    public static boolean termExistsInImgIndex(Datastore ds, String tag, ObjectId id, Double prob){
        boolean existence = false;
        Query<ImgIndex> query = ds.find(ImgIndex.class, "tag ==", tag);
        if (query.asList().size() > 0){
            existence = true;
            ImgIndex index = query.asList().get(0);
            index.addLocation(id, prob);
            ds.save(index);
        }
        return existence;
    }
}