package com.example.bluetransport;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;  
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {
  
  TextView myLabel, loading;
  EditText myTextbox;
  ListView listView;
  ArrayAdapter<String> listAdapter;
  BluetoothAdapter mBluetoothAdapter;
  BluetoothSocket mmSocket;
  BluetoothDevice mmDevice;
  BroadcastReceiver receiver;
  IntentFilter filter; 				
  OutputStream mmOutputStream;
  InputStream mmInputStream;
  Thread blueThread;			//Bluetooth transfer thread
  byte[] readBuffer;
  int readBufferPosition;
  int counter;
  volatile boolean stopBlue;
  final DataStore ds = new DataStore();		//instance of DataStore class. stores data from bluetooth
  ResultRecord record;						//instance of ResultRecord class. processes data into required format.
  Meter meter;
  
    @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  
    Button receiveButton = (Button)findViewById(R.id.receive);
    Button postButton = (Button)findViewById(R.id.post);
    Button closeButton = (Button)findViewById(R.id.close);
    myLabel = (TextView)findViewById(R.id.label);
    loading = (TextView)findViewById(R.id.loading);
    
    //Getting bluetooth Adapter 
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if(mBluetoothAdapter == null) {
      myLabel.setText("No bluetooth adapter available");
    }
    
    //Check if bluetooth is available
    if(!mBluetoothAdapter.isEnabled()) {
      Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBluetooth, 0);
    }
    
   bR();	//Broadcast receiver
    
    //Receive Button
    receiveButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
          findBT();
          openBT();
        }
        catch (IOException ex) { }
      }
    });
    
    //Close button closes connection
    closeButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
          if(mmInputStream != null && mmOutputStream != null){
          closeBT();
          }
          else{
        	Toast.makeText(getBaseContext(), "Bluetooth is not connected!", Toast.LENGTH_LONG).show();
          }
        }catch (IOException ex) { }
      }
    });
  
  //Post button
    postButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
    	  if (!ds.isEmpty()){
          new HttpAsyncTask().execute("https://red.shef.ac.uk/devices.json");
    	  }
    	  else{
    			Toast.makeText(getBaseContext(), "No data to post!", Toast.LENGTH_LONG).show();
    		}
      }
    });
    
  }
  
  
void findBT() {							//Finds the Edison bluetooth device with name "tase_ed" 
	  
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    if(pairedDevices.size() > 0) {
      for(BluetoothDevice device : pairedDevices) {
        if(device.getName().equals("tase_ed")) {
          mmDevice = device;
          myLabel.setText("NO ACTIVE BLUETOOTH CONNECTION");
          break;
        }
      }
    }
    
  }

//Establishing bluetooth connection to bluetoth device
  void openBT() throws IOException {
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);    
    mmSocket.connect();
    mmOutputStream = mmSocket.getOutputStream();
    mmInputStream = mmSocket.getInputStream(); 
    myLabel.setText("Bluetooth Connection opened");
    beginListenForData();
    myLabel.setText("DATA RECEIVED!!");
    
  }

 
private void  beginListenForData() {
	final Handler handler = new Handler();
    final byte delimiter = 10; //This is the ASCII code for a newline character
    listView = (ListView)findViewById(R.id.listView);
    listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
    listView.setAdapter(listAdapter);
    
    stopBlue = false;
    readBufferPosition = 0;
    readBuffer = new byte[1024];  										// Bufffer size for incomming bytes
    
    blueThread = new Thread(new Runnable() {							//An instance of Thread to receive data 
      public void run() {
         while(!Thread.currentThread().isInterrupted() && !stopBlue) {		 
          try {
            int bytesAvailable = mmInputStream.available();            
            if(bytesAvailable > 0) {
              byte[] packetBytes = new byte[bytesAvailable];
              mmInputStream.read(packetBytes);					//read available bytes
              for(int i=0;i<bytesAvailable;i++) {				
                byte b = packetBytes[i];
                if(b == delimiter) {
                  byte[] encodedBytes = new byte[readBufferPosition];
                  System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                  final String data = new String(encodedBytes, "US-ASCII");
                  readBufferPosition = 0;
                  
                  //Use a handler to send data to main UI thread
                  handler.post(new Runnable() {
                    public void run() {
                    
                    	listAdapter.add(data); //Finally adds read data to a ListView //myLabel.setText(data);
                    	ds.setData(data);		//Adds data to the DataStore for later use!
                    }  
                  });
                }
                else {
                  readBuffer[readBufferPosition++] = b;
                }
              }
              
            }
          } 
          catch (IOException ex) {
            stopBlue = true;
          }
         }
      }
    });
    
    //Start Thread
    blueThread.start();
  }

//Close method
void closeBT() throws IOException {
    stopBlue = true;
    mmOutputStream.close();
    mmInputStream.close();
    mmSocket.close();
    myLabel.setText("Bluetooth Closed");
  }

//Post method to send data to server
private String POST(String url, ResultRecord record){
	InputStream inputStream = null;
	String result = "";
	String json = "";
	meter = new Meter(ds.getData(0));		//Meter object to read serial number on first line 
	
	JSONArray jarr = new JSONArray();
	JSONObject j ;
	
	for(int i = 1; i<ds.getCount(); i++){
	record = new ResultRecord(ds.getData(i));
	
	 //build the jsonObject
    JSONObject jsonObject = new JSONObject();
    	try {
    		jsonObject.put("count", null);
    		jsonObject.put("readings", getReadings(record)); 
    		jsonObject.put("sn", meter.getSN());
    		jsonObject.put("type", meter.getMeter());
	    
    		jarr.put(jsonObject);		//Put all objects into an array
    	} 
    		catch (JSONException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
		}
	
	try{
		//create HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        
        //make POST request to the given URL
        HttpPost httpPost = new HttpPost(url); 
        
        for(int i = 0; i<jarr.length(); i++){
        	j = jarr.getJSONObject(i);
        
        //headers to inform server about the type of the content   
        httpPost.setHeader("Authorization", "Basic emhlbmcuaHVpQGdtYWlsLmNvbTp1c2Vy");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
          
		
        //convert JSONObject to JSON to String
        json = j.toString();
		
        //set json to StringEntity
        StringEntity se = new StringEntity(json);
		
        //set httpPost Entity
        httpPost.setEntity(se);
   
        
        //Execute POST request URL
        HttpResponse httpResponse = httpclient.execute(httpPost);
        
        //receive response as inputStream
        inputStream = httpResponse.getEntity().getContent();
		
        //convert inputstream to string
        if(inputStream != null)
            result = convertInputStreamToString(inputStream);
        else
            result = "Post failed... Did not work!";
		
	}}
        catch(Exception e){
		Log.d("InputStream", e.getLocalizedMessage());
	}
	
	return result;
}

//Method transfers input to string
private String convertInputStreamToString(InputStream inputStream) throws IOException{				
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
            result += line;
		
		inputStream.close();
        return result;
	}

private class HttpAsyncTask extends AsyncTask<String, Void, String> {
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		loading.setText("Sending Data to server...");
	}
	
	@Override
    protected String doInBackground(String... urls) {
		
        return POST(urls[0],record);
    }
	// onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(getBaseContext(), "Data Sent! "+ result, Toast.LENGTH_LONG).show();
        loading.setText("Data Sent!");
        System.out.println(result);
   }
}

private JSONArray getReadings(ResultRecord record){
	JSONArray jarr = new JSONArray();
	JSONObject job = new JSONObject();
	try {
		
		job.put("readingDate", record.getdate());
		job.put("type", record.gettype());
		job.put("result", record.getresult());
		
		jarr.put(job);
		
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return jarr;
}


private void bR(){
	receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
				
				//run some code in case someone changes  bluetooth adapter state!!
				if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetooth, 0);
				}
			}	
		}
	};
	filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);  //initializing filter for change of adapter state, on/off!
	registerReceiver(receiver, filter);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);
	if(resultCode == RESULT_CANCELED){
		Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue!!", Toast.LENGTH_LONG).show();
		finish();
	}
	
}
}


