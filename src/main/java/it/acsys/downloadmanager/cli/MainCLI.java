package it.acsys.downloadmanager.cli;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class MainCLI {

	private static String user = "";
	private static String password;
	private static String session;
	
	private static CloseableHttpClient httpclient = null;
	
	public static String downloadURL = "//http://localhost:8080/DownloadManager/DownloadsMonitorServlet";

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
		help += "-bye: exit the program\n";
		return help;
	}
	
	private static boolean checkCredential(Console c) throws IOException {
		
		Properties usersProperties = new Properties();
		try {
        	File configFile = new File("../etc/users.properties");
        	InputStream stream  = new FileInputStream(configFile);
        	usersProperties.load(stream);
        	stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		String CLIUsername = usersProperties.getProperty("CLIUsername");
		if(!CLIUsername.equals("")) {
			System.out.println(">Please enter username");
			user = c.readLine();
			System.out.println(">Please enter password");
	    	password = new String(c.readPassword());
		}
		
		
    	CloseableHttpResponse response = null;
		try {
			//System.out.println("downloadURL " + downloadURL);
		    HttpPost httpPost = new HttpPost(downloadURL);
//		    String authentication = Base64.encodeBase64String((user+":"+password).getBytes());
//		    httpPost.setHeader("Authorization", " Basic " + authentication);		    
		    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
		    params.add(new BasicNameValuePair("commandType", "checkCredential"));
		    params.add(new BasicNameValuePair("username", user));
		    params.add(new BasicNameValuePair("password", password));
		    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		    
		    // Make the request
		    response = httpclient.execute(httpPost);
		    
		    HttpEntity responseEntity = response.getEntity();
//		    System.out.println("USER " + EntityUtils.toString(responseEntity));
		    String resp = EntityUtils.toString(responseEntity);
	    	if(resp.startsWith("JSESSIONID=")) {
	    		session = resp.replace("JSESSIONID=", "");
	    		return true;
	    	} else {
	    		System.out.println(">User/Password not valid.");
	        	checkCredential(c);
	    	}
		} catch (IOException e) {
		    System.out.println("ERROR in contacting Download Manager service " + e.getMessage());
			//e.printStackTrace();
		    System.exit(0);
		} finally {
			httpclient.close();
		}
		
		return false;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
    	InputStream stream  = new FileInputStream("./configCLI.properties");
    	Properties properties = new Properties();
    	properties.load(stream);
    	try {
    		stream.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
    	downloadURL = (String) properties.get("downloadURL");
    
    	KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File("../etc/keystore"));
        try {
            trustStore.load(instream, "storepwd".toCharArray());
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManagerShared(true)
                .build();
        
    	//BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        
    	checkCredential(c);
    	System.out.println(">Welcome " + user);
    	System.out.print(">");
    	String s = null;
    	while ((s = c.readLine()) != null) {
    		StringTokenizer tokenizer = new StringTokenizer(s, " ");
    		while(tokenizer.hasMoreTokens()) {
    			String command = tokenizer.nextToken();
    			HttpPost httpPost = new HttpPost(downloadURL);
    			java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
    			httpPost.addHeader("Cookie", "JSESSIONID="+session);
    			try {
	    			if (command.equalsIgnoreCase("-listAll") ||
	    					command.equalsIgnoreCase("-testConfig") ||
	    					command.equalsIgnoreCase("-showConfig")) {
	        			
	    				CloseableHttpResponse response;
	//        			try {
	        			    
	        			    String authentication = Base64.encodeBase64String((user+":"+password).getBytes());
	        			    httpPost.setHeader("Authorization", " Basic " + authentication);
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
	        			    httpclient.close();
	        		} else if (command.equalsIgnoreCase("-download")) {
	        			
	        			try {
	        				String downURL = tokenizer.nextToken();
	        				CloseableHttpResponse response;
	        			    String authentication = Base64.encodeBase64String((user+":"+password).getBytes());
	        			    httpPost.setHeader("Authorization", " Basic " + authentication);
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
	        			} catch (Exception e) {
	        				System.out.println(usage());
	        			}
	        		} else if (command.equalsIgnoreCase("-info") ||
	        				command.equalsIgnoreCase("-pause") ||
	        				command.equalsIgnoreCase("-resume") ||
	        				command.equalsIgnoreCase("-remove") ||
	        				command.equalsIgnoreCase("-changePriority")) {
	        			String downId = null;
	        			try {
	        				downId = tokenizer.nextToken();
	        				CloseableHttpResponse response;
	//            			try {
	            			    String authentication = Base64.encodeBase64String((user+":"+password).getBytes());
	            			    httpPost.setHeader("Authorization", " Basic " + authentication);
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
	            			    httpclient.close();
	        			} catch(Exception e) {
	        				System.out.println(usage());
	        			}
	        		} else if (command.equalsIgnoreCase("-addDAR") ||
	        				command.equalsIgnoreCase("-cancelDAR") ||
	        				command.equalsIgnoreCase("-pauseDAR") ||
	        				command.equalsIgnoreCase("-resumeDAR")) {
	        			CloseableHttpResponse response = null;
	        			try {
	        				String darURL = tokenizer.nextToken();
	        			    String authentication = Base64.encodeBase64String((user+":"+password).getBytes());
	        			    httpPost.setHeader("Authorization", " Basic " + authentication);
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
	        			} catch (Exception e) {
	        				System.out.println(usage());
	        			} finally {
	        				httpclient.close();
	        			}
	        		} else if (command.equalsIgnoreCase("-setConfig")) {
	        			CloseableHttpResponse response = null;
	        			try {
	        				String key = tokenizer.nextToken();
	            			String value = tokenizer.nextToken();		            			
	        			    String authentication = Base64.encodeBase64String((user+":"+password).getBytes());
	        			    httpPost.setHeader("Authorization", " Basic " + authentication);
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
	        				httpclient.close();
	        			}
	        		} else if (s.equalsIgnoreCase("-bye") ) {
	        			System.exit(0);
	        		} else if (s.length()!= 0 ){
	        			System.out.println(usage());
	        		}
    		} catch(NoHttpResponseException ex) {
     			System.out.print("> Session expired. Please relaunch CLI client.");
     			System.exit(0);
     		} 
	        		
	    		}
    		System.out.print(">");
    		
    	}
    	System.out.print(">");
	}

}
