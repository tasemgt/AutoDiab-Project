/**
 * Created by Michael Tase: July, 2015.
 */

package com.example.bluetransport;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
	List<String> Storage = new ArrayList<String>();
	
	public void setData(String string){
		Storage.add(string);
	}
	
	public String getData(int i){
		return Storage.get(i);
	}
	
	public int getCount(){
		return Storage.size();
	}
	
	public String[] getArray(){
		return (String[]) Storage.toArray();
	}
	
	public boolean isEmpty(){
		return Storage.isEmpty();
	}

} 
