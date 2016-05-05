package it.acsys.downloadmanager.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import org.apache.http.util.EntityUtils;

public class MainCLI {
	
	public static String downloadURL = "http://localhost:8080/DownloadManager/DownloadsMonitorServlet";

	public static String usage() {
		String help = "Usage:\n";
		help +=  "-listAll  : show the downloads list with history (select in database)\n";
		help +=  "-download [URI]  : starts URI download\n";
		help +=  "-info [URI_id]  : show information related to the given product URI id\n";
		help +=  "-pause [URI_id]  : pause the given URI id\n";
		help +=  "-resume [URI_id]  : resume the given URI id\n";
		help +=  "-remove [URI_id]  : remove the given URI id\n";
		help +=  "-changePriority [URI_id]  : put the given URI id at the top of the waiting list\n";
		help +=  "-addDAR [DAR URL]   : Read DAR and starts downloading contained Product URIs\n";
		help +=  "-cancelDAR [DAR URL] : Cancel downloading all Product URIs contained in DAR\n";
		help +=  "-pauseDAR [DAR URL]  : Pause downloading all Product URIs contained in DAR\n";
		help +=  "-resumeDAR [DAR URL]  : Resume downloading all Product URIs contained in DAR\n";
		help += "-showConfig: show server configuration parameters\n";
		help += "-testConfig: tests if server configuration file is well formed\n";
		help += "-setConfig [key] [value]: sets the specified key with the specified value in the server configuration file\n";
		return help;
	}
	
	private static boolean authenticateUser(String username) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
		    HttpPost httpPost = new HttpPost(downloadURL);
	
		    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
		    params.add(new BasicNameValuePair("commandType", "getUserCLI"));
	
		    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		    
		    // Make the request
		    response = httpclient.execute(httpPost);
		    HttpEntity responseEntity = response.getEntity();
		    //System.out.println("USER " + EntityUtils.toString(responseEntity));
	    	if(username.equals(EntityUtils.toString(responseEntity))) {
	    		return true;
	    	} else {
	    		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    		System.out.println(">User not valid. Please enter username");
	        	String s = in.readLine();
	        	authenticateUser(s);
	    	}
		} catch (ClientProtocolException e) {
		    e.printStackTrace();
		    System.exit(0);
		} catch (IOException e) {
		    e.printStackTrace();
		    System.exit(0);
		} finally {
		    httpclient.getConnectionManager().shutdown();
		}		
		
		return false;
	}
	
	private static boolean checkPassword(String password) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
		    HttpPost httpPost = new HttpPost(downloadURL);
	
		    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
		    params.add(new BasicNameValuePair("commandType", "getCLIPassword"));
	
		    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		    
		    // Make the request
		    response = httpclient.execute(httpPost);
		    HttpEntity responseEntity = response.getEntity();
		    if(password.equals(EntityUtils.toString(responseEntity))) {
	    		return true;
	    	} else {
	    		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    		System.out.println(">Password not valid. Please enter password");
	        	String s = in.readLine();
	        	checkPassword(s);
	    	}
		} catch (ClientProtocolException e) {
		    e.printStackTrace();
		    System.exit(0);
		} catch (IOException e) {
		    e.printStackTrace();
		    System.exit(0);
		} finally {
		    httpclient.getConnectionManager().shutdown();
		}		
		
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
    	java.io.InputStream stream  = new java.io.FileInputStream("./configCLI.properties");
    	Properties properties = new Properties();
    	properties.load(stream);
    	try {
    		stream.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
    	downloadURL = (String) properties.get("downloadURL");
    
    	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    	System.out.println(">Please enter username");
    	String s = in.readLine();
    	authenticateUser(s);
    	System.out.println(">Please enter password");
    	String s1 = in.readLine();
    	checkPassword(s1);
    	System.out.println(">Welcome " + s);
    	System.out.print(">");
    	while ((s = in.readLine()) != null) {
    		StringTokenizer tokenizer = new StringTokenizer(s, " ");
    		while(tokenizer.hasMoreTokens()) {
    			String command = tokenizer.nextToken();
    			if (command.equalsIgnoreCase("-listAll") ||
    					command.equalsIgnoreCase("-testConfig") ||
    					command.equalsIgnoreCase("-showConfig")) {
        			DefaultHttpClient httpclient = new DefaultHttpClient();
        			HttpResponse response;
//        			try {
        			    HttpPost httpPost = new HttpPost(downloadURL);

        			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
        			    params.add(new BasicNameValuePair("commandType", s));

        			    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			    
        			    // Make the request
        			    response = httpclient.execute(httpPost);

        			    HttpEntity responseEntity = response.getEntity();

        			    System.out.println("----------------------------------------");
        			    System.out.println(response.getStatusLine());
        			    if(responseEntity != null) {
        			        System.out.println("Response content length: " + responseEntity.getContentLength());
        			    }

        			    String jsonResultString = EntityUtils.toString(responseEntity);
        			    EntityUtils.consume(responseEntity);
        			    System.out.println("----------------------------------------");
        			    System.out.println("result:\n" + jsonResultString);
        			    System.out.println();
//        			} catch (ClientProtocolException e) {
//        			    e.printStackTrace();
//        			} catch (IOException e) {
//        			    e.printStackTrace();
//        			} finally {
        			    httpclient.getConnectionManager().shutdown();
//        			};
        		} else if (command.equalsIgnoreCase("-download")) {
        			String downURL = tokenizer.nextToken();
        			DefaultHttpClient httpclient = new DefaultHttpClient();
        			HttpResponse response;
//        			try {
        			    HttpPost httpPost = new HttpPost(downloadURL);

        			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
        			    params.add(new BasicNameValuePair("commandType", "startDownload"));
        			    params.add(new BasicNameValuePair("URI", downURL));

        			    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			    
        			    // Make the request
        			    response = httpclient.execute(httpPost);

        			    HttpEntity responseEntity = response.getEntity();

        			    System.out.println("----------------------------------------");
        			    System.out.println(response.getStatusLine());
        			    if(responseEntity != null) {
        			        System.out.println("Response content length: " + responseEntity.getContentLength());
        			    }

        			    String status = EntityUtils.toString(responseEntity);
        			    EntityUtils.consume(responseEntity);
        			    System.out.println("----------------------------------------");
        			    System.out.println("result:\n" + status);
        			    System.out.println();
//        			} catch (ClientProtocolException e) {
//        			    e.printStackTrace();
//        			} catch (IOException e) {
//        			    e.printStackTrace();
//        			} finally {
//        			    httpclient.getConnectionManager().shutdown();
//        			}
        		} else if (command.equalsIgnoreCase("-info") ||
        				command.equalsIgnoreCase("-pause") ||
        				command.equalsIgnoreCase("-resume") ||
        				command.equalsIgnoreCase("-remove") ||
        				command.equalsIgnoreCase("-changePriority")) {
        			String downId = tokenizer.nextToken();
        			DefaultHttpClient httpclient = new DefaultHttpClient();
        			HttpResponse response;
//        			try {
        			    HttpPost httpPost = new HttpPost(downloadURL);

        			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
        			    params.add(new BasicNameValuePair("commandType", command));
        			    params.add(new BasicNameValuePair("gid", downId));

        			    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			    
        			    // Make the request
        			    response = httpclient.execute(httpPost);

        			    HttpEntity responseEntity = response.getEntity();

        			    System.out.println("----------------------------------------");
        			    System.out.println(response.getStatusLine());
        			    if(responseEntity != null) {
        			        System.out.println("Response content length: " + responseEntity.getContentLength());
        			    }

        			    String status = EntityUtils.toString(responseEntity);
        			    EntityUtils.consume(responseEntity);
        			    System.out.println("----------------------------------------");
        			    System.out.println("result:" + status);
        			    System.out.println();
//        			} catch (ClientProtocolException e) {
//        			    e.printStackTrace();
//        			} catch (IOException e) {
//        			    e.printStackTrace();
//        			} finally {
        			    httpclient.getConnectionManager().shutdown();
//        			}
        		} else if (command.equalsIgnoreCase("-addDAR") ||
        				command.equalsIgnoreCase("-cancelDAR") ||
        				command.equalsIgnoreCase("-pauseDAR") ||
        				command.equalsIgnoreCase("-resumeDAR")) {
        			String darURL = tokenizer.nextToken();
        			DefaultHttpClient httpclient = new DefaultHttpClient();
        			HttpResponse response;
//        			try {
        			    HttpPost httpPost = new HttpPost(downloadURL);

        			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
        			    params.add(new BasicNameValuePair("commandType", command));
        			    params.add(new BasicNameValuePair("DARUri", darURL));

        			    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			    
        			    // Make the request
        			    response = httpclient.execute(httpPost);

        			    HttpEntity responseEntity = response.getEntity();

        			    System.out.println("----------------------------------------");
        			    System.out.println(response.getStatusLine());
        			    if(responseEntity != null) {
        			        System.out.println("Response content length: " + responseEntity.getContentLength());
        			    }

        			    String id = EntityUtils.toString(responseEntity);
        			    EntityUtils.consume(responseEntity);
        			    System.out.println("----------------------------------------");
        			    System.out.println("result:" + id);
        			    System.out.println();
//        			} catch (ClientProtocolException e) {
//        			    e.printStackTrace();
//        			} finally {
        			    httpclient.getConnectionManager().shutdown();
//        			}
        		} else if (command.equalsIgnoreCase("-setConfig")) {
        			String key = tokenizer.nextToken();
        			String value = tokenizer.nextToken();
        			DefaultHttpClient httpclient = new DefaultHttpClient();
        			HttpResponse response;
        			try {
        			    HttpPost httpPost = new HttpPost(downloadURL);

        			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
        			    params.add(new BasicNameValuePair("commandType", command));
        			    params.add(new BasicNameValuePair("key", key));
        			    params.add(new BasicNameValuePair("value", value));

        			    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			    
        			    // Make the request
        			    response = httpclient.execute(httpPost);

        			    HttpEntity responseEntity = response.getEntity();

        			    System.out.println("----------------------------------------");
        			    System.out.println(response.getStatusLine());
        			    if(responseEntity != null) {
        			        System.out.println("Response content length: " + responseEntity.getContentLength());
        			    }

        			    String id = EntityUtils.toString(responseEntity);
        			    EntityUtils.consume(responseEntity);
        			    System.out.println("----------------------------------------");
        			    System.out.println("result:" + id);
        			    System.out.println();

        			} catch (NoSuchElementException e) {
        				System.out.println(usage());
        			} finally {
        			    httpclient.getConnectionManager().shutdown();
        			}
        		} else if (s.equalsIgnoreCase("-bye") ) {
        			System.exit(0);
        		} else if (s.length()!= 0 ){
        			System.out.println(usage());
        		}
        		
    		}
    		System.out.print(">");
    		
    	}
    	System.out.print(">");
	}

}
