package it.acsys.test;

import java.io.*;

public class GenerateDAR {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File metalinkDir = new File("./metalinks");
		File[] files = metalinkDir.listFiles();
		for(int n=0; n<files.length; n++) {
			try {
				FileInputStream registrationFile = new java.io.FileInputStream("./DAR_TEMPLATE.xml");
		    	  DataInputStream in = new DataInputStream(registrationFile);
		    	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    	  StringBuffer sb = new StringBuffer();
		    	  String strLine;
		    	  while((strLine = br.readLine()) != null) {
		    		  sb.append(strLine);
		    	  }
		    	  String filecontent = sb.toString();
		    	  in.close();
		    	  filecontent = filecontent.replace("{METALINK_URL}", files[n].getName());
		    	  File out = new File("./DARs/DAR_"+ n +".xml");
		    	  FileOutputStream fos = new FileOutputStream(out);
		    	  fos.write(filecontent.getBytes());
		    	  fos.flush();
		    	  fos.close();
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

	}

}
