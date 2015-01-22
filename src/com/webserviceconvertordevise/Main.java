package com.webserviceconvertordevise;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.formation.Webservice.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener{
	
	//the site whose convert devise
	 private static String 		URL_WS="http://www.webservicex.com/CurrencyConvertor.asmx/ConversionRate";
	 
	 //defined the elements of our GUI
	 private Spinner 			spinnerFrom;
	 private Spinner 			spinnerTo;
	 private ProgressDialog 	dialog;
	 private Handler 			mHandler;
	 private String 			resultat;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //put a devise array in ours spinners
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.devises, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spinnerFrom = (Spinner) findViewById(R.id.spinner_from);
       
        spinnerFrom.setAdapter(adapter);
        
        spinnerTo = (Spinner) findViewById(R.id.spinner_to);
     
        spinnerTo.setAdapter(adapter);
        
        ((Button)findViewById(R.id.btn_convertir)).setOnClickListener(this);
        
        //progress dialog to do wait users 
         dialog  = new ProgressDialog(this);

		 dialog.setMessage("wait to convert devise...");
		 dialog.setCancelable(true);
		 
		 //we initialise this handler to give result of our webservice
		 mHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch(msg.what){
						case 1:
						dialog.dismiss();
						((TextView)findViewById(R.id.resultat)).setText(resultat);
						break;
						
					}
				}
			};
		 
    }

	@Override
	public void onClick(View v) {
		//we call our weservice when we click in a button
		switch(v.getId())
		{
			case R.id.btn_convertir:
				 dialog.show();
				 new Thread(new Runnable(){
						public void run() {
							resultat=convertViaWebService(spinnerFrom.getSelectedItem().toString(),spinnerTo.getSelectedItem().toString(),Double.parseDouble(((TextView)findViewById(R.id.input_montant)).getText().toString()));
							mHandler.sendEmptyMessage(1);
						}; 
					}).start();
			
			break;
		}
		
	}
	
	//function which call webservice and resturn the result of convertion
	private String convertViaWebService(String devise_1, String devise_2, double montant)
	{
        try {
        	//http parameters are use to define timeout of connection and declanche an error if our user don't have the connection
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		
    	HttpConnectionParams.setSoTimeout(httpParameters, 5000);
    	
    	//we declare our http client 
    	HttpClient httpclient = new DefaultHttpClient(httpParameters);
    	
    	//we declare a httpPost variable to call webservice
    	HttpPost httppost = new HttpPost(URL_WS);  
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        
        //we give all the parameters to use webservice for convert devise
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);  
        nameValuePairs.add(new BasicNameValuePair("FromCurrency", devise_1));  
        nameValuePairs.add(new BasicNameValuePair("ToCurrency", devise_2));   
    	
        //we assign this parameters on our httpPost
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        
        //we execute our call webservice
        HttpResponse response = httpclient.execute(httppost); 
        
        //we put the result in a inputStream
        InputStream is=response.getEntity().getContent();
        
        InputStreamReader reader = new InputStreamReader(is,HTTP.UTF_8);
        
        //put the result in a String
        char[] buf = new char [4096];
        
        int   count;
		StringBuilder sb=new StringBuilder();
		    		
		while ((count = reader.read (buf, 0, buf.length)) != -1)
			sb.append(buf, 0, count);
		is.close();
		
		String res=sb.toString();
		
		return String.valueOf(Math.round(Double.parseDouble(sb.toString().substring(res.indexOf("/\">")+3,res.lastIndexOf("<")))*montant)+ " "+devise_2);
        
		} catch (Exception e) {
			return "Error to call webservice convertor devise\n please verify your connection internet";
		}  
		
	}
}