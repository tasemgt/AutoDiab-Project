package com.example.bluetransport;
/**
 * Created by Michael Tase: July, 2015.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ResultRecord {
	private String type, readingDate, result;
	
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK);
	
	public ResultRecord(String data){
		String s = " ";
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.readingDate = data.substring(data.indexOf(s)+1, data.indexOf(s)+13);
		this.result = data.substring(data.lastIndexOf(s));
		this.type = data.substring(data.indexOf(s)+14, data.lastIndexOf(s));
	}
	
	public String gettype() {
		return Character.toString(this.type.charAt(0));
	} 

	@SuppressWarnings("null")
	public long getdate() {
		try {
			return (df.parse(this.readingDate.concat("00")).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (Long) null;
		}
	}

	public Double getresult() {
		return Double.parseDouble(this.result);
	}
}
