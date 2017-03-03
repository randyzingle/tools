package com.bms;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {
	
	private PathHolderBean pathHolderBean;
	
	@Autowired
	public void setPathHolderBean(PathHolderBean pathHolderBean) {
		this.pathHolderBean = pathHolderBean;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		System.out.println("****** STARTING UP!");
		try {
			System.out.println("long running process like downloading keystores...");
			for (int i=0; i<2; i++) {
				Thread.sleep(1000L);
				System.out.println(i);
			}
			System.out.println("Download file from S3....");
			getFile();
		} catch (Exception ex) {ex.printStackTrace();}

	}
	
	private void getFile() throws Exception {
		String outPath = pathHolderBean.getJarPath();
		AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
		String bucketName = "ci-360-deployment-dev-us-east-1";
//		String keystore = "shared/kafka/singleserver/kafkass.client.keystore.jks";
		String keyroot = "shared/kafka/singleserver/";
		String keyfile = "kafkass.client.keystore.jks";
		String keystore = keyroot+keyfile;
		S3Object s3object = client.getObject(new GetObjectRequest(bucketName, keystore));
		System.out.println("Content-Type: "  + 
        		s3object.getObjectMetadata().getContentType());
		InputStream is = s3object.getObjectContent();
		System.out.println("found S3 keystore and opened input stream");
		
		// read data and write to file
		FileOutputStream fos = new FileOutputStream(outPath+"/"+keyfile);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		int c;
		while ( (c = bis.read()) != -1) {
			fos.write(c);
		}
		
		bis.close();
		fos.flush();
		fos.close();
	}

}
