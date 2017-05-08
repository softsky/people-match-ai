package com.localblox.ashfaq.data;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Arrays;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ServerAddress;
import org.bson.Document;

/**
   IMPORTANT:
   For even better object model this class should be abstract
   and implementation moved to com.softsky.ashfaq.data.impl and named ProfileEnumeratorMongoDBImpl or something like that
 */
public final class ProfileEnumerator implements Enumeration<PersonProfile> {

	final MongoClient mongoClient = new MongoClient(
	                                                /**
	                                                   IMPORTANT: We will apparently need many replicas for speed
	                                                   so used this constructor here, but only single host
	                                                   is used
	                                                */
	                                                Arrays.asList(new ServerAddress("localhost", 27017),
	                                                              new ServerAddress("localhost", 27017),
	                                                              new ServerAddress("localhost", 27017)));
	// get handle to "mydb" database
        private final MongoDatabase database = mongoClient.getDatabase("ashfaq");

        // get a handle to the "test" collection
        private final MongoCollection<Document> collection = database.getCollection("People");

	private MongoCursor<Document> cursor;
	
	public ProfileEnumerator(){
		// do nothing
	}

	/**
	   Fetches all records from database
	 */
	public void fetchAll() {
		System.out.println(collection);
	       cursor = collection.find().iterator();
	}
	/**
	   Tests if more elements comes from database.

	   @return true if more elements could be retrieved with request
	 */
	public boolean hasMoreElements() {
		return cursor.hasNext();
	}

	/**
	   Returns the next element of this enumeration if this enumeration object has at least one more element to provide.

	   @return instance of next PersonProfile element
	 */
	public PersonProfile nextElement(){
		return new PersonProfile(cursor.next().toJson());
	}
}
