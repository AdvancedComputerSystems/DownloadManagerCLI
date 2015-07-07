package it.acsys.downloadmanager.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
	
	private static boolean checkCredential(BufferedReader in) throws IOException {
		
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
			user = in.readLine();
			System.out.println(">Please enter password");
	    	password = in.readLine();
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
	        	checkCredential(in);
	    	}
		} catch (IOException e) {
		    System.out.println("ERROR in contacting Download Manager service " + e.getMessage());
			//e.printStackTrace();
		    System.exit(0);
		} finally {
			response.close();
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
                .build();
        
    	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    	checkCredential(in);
    	System.out.println(">Welcome " + user);
    	System.out.print(">");
    	String s = null;
    	while ((s = in.readLine()) != null) {
    		StringTokenizer tokenizer = new StringTokenizer(s, " ");
    		while(tokenizer.hasMoreTokens()) {
    			String command = tokenizer.nextToken();
    			
    			
    			//DefaultHttpClient httpclient = new DefaultHttpClient();
    			HttpPost httpPost = new HttpPost(downloadURL);
    			java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
    			httpPost.addHeader("Cookie", "JSESSIONID="+session);
    			
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
//        			} catch (ClientProtocolException e) {
//        			    e.printStackTrace();
//        			} catch (IOException e) {
//        			    e.printStackTrace();
//        			} finally {
        			    response.close();
//        			};
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
        				response.close();
        			}
        		} else if (command.equalsIgnoreCase("-setConfig")) {
        			try {
        				String key = tokenizer.nextToken();
            			
            			String value = tokenizer.nextToken();        			
            			HttpResponse response;
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
        		
    		}
    		System.out.print(">");
    		
    	}
    	System.out.print(">");
	}
 
 
    private static class ConsoleEraser extends Thread {
        private boolean running = true;
        public void run() {
            while (running) {
                System.out.print("\010");
            }
        }
        public synchronized void halt() {
            running = false;
        }
    }

}
