package com.bms;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.esotericsoftware.minlog.Log;

@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {
	private Logger logger = Logger.getLogger(this.getClass());
	
	// READ these in as properties
	String bucketName = "ci-360-deployment-dev-us-east-1";
	String keyroot = "shared/kafka/singleserver/kafkass.";
	String keystore = "client.keystore.jks";
	String truststore = "client.truststore.jks";
	
	private PathHolderBean pathHolderBean;
	
	@Autowired
	public void setPathHolderBean(PathHolderBean pathHolderBean) {
		this.pathHolderBean = pathHolderBean;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.info("****** ApplicationListener got ContextRefreshedEvent");
		try {
			logger.info("Download keystore and truststore from S3....");
			downloadS3Keystores();
		} catch (Exception ex) {ex.printStackTrace();}

	}
	
	public void doIt() {
		System.out.println("Downloading keystores from S3...");
		downloadS3Keystores();
	}
	
	private void downloadS3Keystores() {
		String outPath = pathHolderBean.getKeystorePath();
		AmazonS3 client = AmazonS3ClientBuilder.defaultClient();

		String s3KeystorePath = keyroot+keystore;
		String s3TruststorePath = keyroot+truststore;
		String [] keys = {s3KeystorePath, s3TruststorePath};
		
		for (String key: keys) {	
			try {
				boolean keyexists = client.doesObjectExist(bucketName, key);
				if (!keyexists) {
					String s = String.format("ERROR - could not find keystore file: %s in S3 bucket: %s%n", 
							bucketName, key);
					throw new Exception(s);
				}
				S3Object s3object = client.getObject(new GetObjectRequest(bucketName, key));
	
				InputStream is = s3object.getObjectContent();
				logger.info(String.format("Found keystore file: %s in S3 bucket: %s%n", 
						bucketName, key));
				
				// read data and write to file
				String[] outFile = key.split("/");
				FileOutputStream fos = new FileOutputStream(outPath+"/"+outFile[outFile.length-1]);
				
				BufferedInputStream bis = new BufferedInputStream(is);
				int c;
				while ( (c = bis.read()) != -1) {
					fos.write(c);
				}
				
				bis.close();
				fos.flush();
				fos.close();
			} catch (AmazonClientException acex) {
				logger.info("failed to get key: " + key + " from S3: " + acex.getMessage());
			} catch (Exception ex) {
				logger.info("failed to get key: " + key + " from S3: " + ex.getMessage());
			}
		}
	}

}
