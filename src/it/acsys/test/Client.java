package it.acsys.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.spi.SyncResolver;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.serializer.StringSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL("http://localhost:6800/rpc"));
		} catch(MalformedURLException ex)  	{
			ex.printStackTrace();
		}
		client.setConfig(config);
		File metalinkDir = new File("/home/anngal/jetty-hightide-7.5.3.v20111011/metalinks");
		File[] files = metalinkDir.listFiles();
		String destPath = "/home/anngal/jetty-hightide-7.5.3.v20111011/Repository";
		for(int n=0; n<files.length; n++) {
			
			String gid= String.valueOf(System.currentTimeMillis());
			Map<String, String> map = new HashMap<String, String>();
			map.put("dir", destPath);
			map.put("gid", gid);
			String uri = "http://ngeo-cat2/sprint4/s2-products/000000003/" + files[n].getName();
			
			Object[] params = new Object[]{new String[]{uri}};
			
			try {
				gid = (String) client.execute("aria2.addUri",params);
			} catch(XmlRpcException e) {
				e.printStackTrace();
			}
			
			System.out.println("URIDownloader " + files[n].getName() + " with gid " + gid);
			
		}
		
		System.exit(0);
	}
	

}
