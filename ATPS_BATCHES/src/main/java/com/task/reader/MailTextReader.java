package com.task.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

//added a new comment for git

/**
 * this class is to read the text file containig the mail subject
 * @author user
 *
 */
@Component
public class MailTextReader {
	/**
	 * injecting ResourceLoader object
	 */
	@Autowired
    ResourceLoader resourceLoader;

	
	String data=null,line=null,oldContent="";
	String newContent;
	
	/**
	 * reading and replacing the text
	 * @return newContent
	 */
	public String readFile(String firstName){
		try {

			Resource resource = resourceLoader.getResource("classpath:MailBody.txt");
			InputStream input = resource.getInputStream();
		File fileToBeModified=resource.getFile();//new File("resources/MailBody.txt");
		BufferedReader reader = new BufferedReader(new FileReader(fileToBeModified));
		 
		line = reader.readLine();

		while (line != null)
		{

		 oldContent = oldContent + line + System.lineSeparator();
		 line = reader.readLine();
		}
		newContent = oldContent.replace("name",firstName);
		reader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return newContent;
		
	}

}
