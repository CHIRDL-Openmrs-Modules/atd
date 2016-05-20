/**
 * 
 */
package org.openmrs.module.atd;

import java.io.File;
import java.io.FileInputStream;

import com.biscom.ArrayOfAttachment;
import com.biscom.ArrayOfRecipientInfo;
import com.biscom.Attachment;
import com.biscom.FAXCOMX0020Service;
import com.biscom.FAXCOMX0020ServiceSoap;
import com.biscom.RecipientInfo;
import com.biscom.ResultMessage;
import com.biscom.SenderInfo;

public class TestFax {
	private static final String SENDER_NAME_CHICA = "CHICA";
	private static final String DEFAULT_ID = "";
	private static final String SUBJECT = "Bicycle Safety Handout";
	private static final String MEMO = "Guidelines for keeping your child safe when reading a bike.";
	private static final int _legacyMode = 1;
	private static final int logonThroughUserAcount = 2;
	private static final String SEND_TIME_IMMEDIATE = "0.0";
	private static final String SEND_TIME_OFF_PEAK = "1.0";
	private static final int RESOLUTION_STANDARD = 0;
	private static final int RESOLUTION_HIGH = 1;
	private static final int PRIORITY_URGENT = 3;
	private static final int PRIORITY_HIGH = 2;
	private static final int PRIORITY_NORMAL = 1;
	private static final int PRIORITY_LOW = 0;
	private static final String TITLE_PAGE_DEFAULT = DEFAULT_ID;
	
	/**
	 * @param args
	 */
	public  static void main(String[] args) {
		
		
		FAXCOMX0020Service service = new FAXCOMX0020Service();
	
		FAXCOMX0020ServiceSoap port = service.getFAXCOMX0020ServiceSoap();
		String faxQueue = "\\\\vsapps208\\FaxcomQ_Queue01";
		String userName =  "CHICAFAX";
		String password = "chicafax";
		String coverPage = DEFAULT_ID;
		try{
	
		ArrayOfAttachment attachments = new ArrayOfAttachment();
		Attachment attachment = new Attachment();
		attachment.setFileName("BICYCLE_SAFETY_JIT_template.pdf");
			File fatt = new File("C:\\chica\\config\\formTemplates\\BICYCLE_SAFETY_JIT_template.pdf");
			FileInputStream fin = new FileInputStream(fatt);
			byte[] fileContents = new byte[(int)fatt.length()];
			fin.read(fileContents);
		attachment.setFileContent(fileContents);
		attachments.getAttachment().add(attachment);
		
		SenderInfo sender = new SenderInfo();
		sender.setName(SENDER_NAME_CHICA);
		sender.setCompany("CHSR");
		
	
		
		ArrayOfRecipientInfo recipients = new ArrayOfRecipientInfo();
		String clinicFaxNumber = "3172780456";
		RecipientInfo recInfo = new RecipientInfo();
		recInfo.setName("Eskenazi Clinic 1");
		recInfo.setFaxNumber(clinicFaxNumber);
		recipients.getRecipientInfo().add(recInfo);
		
		boolean doFax = true;
		if (doFax) {
		ResultMessage rm = port.loginAndSendNewFaxMessage(faxQueue, userName, password, logonThroughUserAcount, DEFAULT_ID, 
				PRIORITY_HIGH, SEND_TIME_IMMEDIATE, RESOLUTION_HIGH, SUBJECT, coverPage,
				MEMO, sender, recipients, attachments , DEFAULT_ID);
		System.out.println(rm.getDetail());
		
		}
			

		}catch(Exception e){
			System.out.println(e.getMessage() + e.getCause());
		}finally{

			try {
				port.releaseSession();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
	