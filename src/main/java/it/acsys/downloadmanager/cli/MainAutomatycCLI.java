package it.acsys.downloadmanager.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class MainAutomatycCLI {
	
	public static String downloadURL = "http://localhost:8080/DownloadManager/DownloadsMonitorServlet";
	private static CloseableHttpClient httpclient = null;
	
	public static String usage() {
		String help = "Usage:\n";
		help +=  "-listAll  : show the downloads list with history (select in database)\n";
		help +=  "-listCurrent  : show the current downloads list (ask current downloads to aria)\n";
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
		return help;
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
    	
		if (args[0].equalsIgnoreCase("-listAll")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);
			    
			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "listAll"));

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
			    System.out.println("result:" + jsonResultString);
			    System.out.println();
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			};
		} else if (args[0].equalsIgnoreCase("-listCurrent")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "listCurrent"));

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
			    System.out.println("result:" + jsonResultString);
			    System.out.println();
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-download")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "startDownload"));
			    params.add(new BasicNameValuePair("URI", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-info")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "getStatus"));
			    params.add(new BasicNameValuePair("gid", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-pause")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "pause"));
			    params.add(new BasicNameValuePair("gid", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-resume")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "resume"));
			    params.add(new BasicNameValuePair("gid", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-remove")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "remove"));
			    params.add(new BasicNameValuePair("gid", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-changePriority")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", "changePriority"));
			    params.add(new BasicNameValuePair("gid", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else if (args[0].equalsIgnoreCase("-addDAR") ||
				args[0].equalsIgnoreCase("-cancelDAR") ||
				args[0].equalsIgnoreCase("-pauseDAR") ||
				args[0].equalsIgnoreCase("-resumeDAR")) {
			HttpResponse response;
			try {
			    HttpPost httpPost = new HttpPost(downloadURL);

			    java.util.List <NameValuePair> params = new ArrayList <NameValuePair>();
			    params.add(new BasicNameValuePair("commandType", args[0]));
			    params.add(new BasicNameValuePair("DARUri", args[1]));

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
			} catch (ClientProtocolException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    httpclient.getConnectionManager().shutdown();
			}
		} else {
			System.out.println(usage());
		}
	}

}
