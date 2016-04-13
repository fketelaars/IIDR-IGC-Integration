package com.ibm.replication.iidr.igcrest;


import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import javax.net.ssl.SSLContext;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.http.impl.client.*;


public class IGCRest {
	
	final private static String IGC_URL = "https://{0}:{1,number,#}/ibm/iis/igc-rest/v1{2}";
	private int port;
	private String hostname;
	private CloseableHttpClient httpclient;
	
    final public static String IIDR_BUNDLE_ID = "IIDR";
	
    final static Logger logger = Logger.getLogger("com.ibm.replication.iidr.igc.rest.IGCRest");
	
	public IGCRest(String hostname, int port, String username, String password, boolean trustSelfSignedCertificates) 
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		this.hostname = hostname;
		this.port = port;
		
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials( new AuthScope(this.hostname, this.port),
                					  new UsernamePasswordCredentials(username, password));
        
        if (trustSelfSignedCertificates) {
        	SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null,
                     new TrustSelfSignedStrategy())
                    .build();
            
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] { "TLSv1" },
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            
            this.httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCredentialsProvider(credsProvider).build();
    		
        } else {
        	this.httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        }
        
	}
	
	private String getUrl(String urlSuffix) {
		return MessageFormat.format(IGCRest.IGC_URL, new Object[] {this.hostname, this.port, urlSuffix});
	}
	
	public void postAssets(String content) throws ClientProtocolException, ParseException, IOException, IGCRestException  {
		
		logger.info("Posting assets to Information Governance Catalog on " + this.hostname);
		
		
		postRequest("/bundles/assets", content);
	}
	
	
	public void postFlows(String content) throws ClientProtocolException, ParseException, IOException, IGCRestException {
		
		logger.info("Posting flows to Information Governance Catalog on " + this.hostname);
		
		postRequest("/flows/upload", content);
	}
	
	public void postRequest(String urlSuffix, String content) throws ClientProtocolException, IOException, ParseException, IGCRestException {
		
		HttpPost httpPost = new HttpPost(this.getUrl(urlSuffix));
		
		
			StringEntity input = new StringEntity(content);
			
			httpPost.setEntity(input);
			
			httpPost.addHeader("Accept", "application/json");
			httpPost.addHeader("Content-Type", "application/xml");
			
			CloseableHttpResponse response = this.httpclient.execute(httpPost);
			
            try {
            	
    			if (response.getStatusLine().getStatusCode() != 200) {
    				throw new IGCRestException(response.getStatusLine().getStatusCode(), 
    						EntityUtils.toString(response.getEntity()));
    	        } 
    			
            } finally {
                response.close();
            }
 	}
	
	public String getRequest(String urlSuffix) throws ClientProtocolException, IOException, ParseException, IGCRestException {
		
		String responseBody = "";
		
		HttpGet httpGet = new HttpGet(this.getUrl(urlSuffix));
		
		
		CloseableHttpResponse response = this.httpclient.execute(httpGet);
		
		try {
        	
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new IGCRestException(response.getStatusLine().getStatusCode(), 
						EntityUtils.toString(response.getEntity()));
	        } 
			
			responseBody = EntityUtils.toString(response.getEntity());
			
        } finally {
            response.close();
        }
		
		return responseBody;
		
	}
	
	private boolean bundleExists() throws ClientProtocolException, ParseException, IOException, IGCRestException {
		String[] bundles = this.getRequest("/bundles/").replaceAll("[\\[\\]\"]","").split(",");
		
		for (int i = 0; i< bundles.length; ++i) {
			if (bundles[i].equals(IGCRest.IIDR_BUNDLE_ID)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public void uploadBundleIfMissing(String bundleFilePath, boolean update) throws ClientProtocolException, IOException, ParseException, IGCRestException {
		
		if (!this.bundleExists() || update) {
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			
			FileBody body = new FileBody(new File(bundleFilePath));
			
			builder.addPart("bundleFile", body);
			
			HttpEntity reqEntity = builder.build();
			
			CloseableHttpResponse response = null;
			
			if (!this.bundleExists()) {
				
				logger.info("Uploadig missing bundle " + IGCRest.IIDR_BUNDLE_ID);
				HttpPost httpPost = new HttpPost(this.getUrl("/bundles"));
				
				httpPost.setEntity(reqEntity);
				
				response = this.httpclient.execute(httpPost);
				
			} else {
				
				logger.info("Updating existing bundle " + IGCRest.IIDR_BUNDLE_ID);
				
				HttpPut httpPut = new HttpPut(this.getUrl("/bundles"));
				
				httpPut.setEntity(reqEntity);
				
				response = this.httpclient.execute(httpPut);
				
			}
			
			
			
			try {
	        	
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new IGCRestException(response.getStatusLine().getStatusCode(), 
							EntityUtils.toString(response.getEntity()));
		        } 
				
	        } finally {
 	            response.close();
	        }
		}
		
	}
	
	public static void main (String[] args) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, ParseException, IOException, IGCRestException {
		IGCRest igcRest = new IGCRest("troia", 9443, "isadmin", "passw0rd", true);
		
		if (!igcRest.bundleExists()) {
			igcRest.uploadBundleIfMissing("C:\\Work\\Box Sync\\Projects\\Personal\\IIDR_OpenIGC\\bundleDetails\\bundleDetails.zip", true);
		}
		
	}

}
