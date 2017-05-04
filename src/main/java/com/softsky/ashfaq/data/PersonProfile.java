package com.softsky.ashfaq.data;

import java.util.Hashtable;
/**
  Person Profile contains some properties
 */
public class PersonProfile extends Hashtable<String, Object>{
	/**
	   Plain JSON string with profile data
	 */
	public String profileString;
	/**
	   Initialize new class with properties copied from @link copyable object
	   @param copyable object to copy properties from
	 */
	public PersonProfile(final Hashtable<String, Object> copyable){
		
	}

	/**
	   Initialize new class with single JSON string
	   @param jsonString string to copy from
	 */
	
	public PersonProfile(final String jsonString){
		profileString = jsonString;
	}

	/**
	   Default implementation overriden
	   @return JSON string object  
	 */
	public String toString(){
		return profileString;
	}
	
}
