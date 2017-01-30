package com.gdyn.orpos.pos.services.sale;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.utility.LocaleMap;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;
import oracle.retail.stores.pos.ui.plaf.UIFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GDYNCallLoyaltyWebserviceSite  extends PosSiteActionAdapter 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 582640060101732345L;


	public void arrive(BusIfc bus) 
	{
		
		// call loyalty web service.
		 String letter = "Invalid";

		 
		 GDYNSaleCargo saleCargo = (GDYNSaleCargo) bus.getCargo();
		 
		// String loyaltyID = ((GDYNSaleReturnTransactionIfc)saleCargo.getTransaction()).getLoyaltyID();
		 String loyaltyID = saleCargo.getLoyaltyIdNumber();
		 //Added by Monica to send barcode in request as per POS-290
		 String CouponCode="";
		 if(saleCargo.getPLUItem().getPosItemID()!=null)
		 {
		  CouponCode = saleCargo.getPLUItem().getPosItemID();
		 }
		
		 
		 // get locale from application.properties
		 Locale locale1 = LocaleMap.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);
		 
		 // get user name , pwd and URL from xml .
		 
		String username= "";
		String pwd= "";
		String URL = "";
		
		
		// attempting to get webservice credential from  properties and save encrypted back to property.
		/*
		if(pwd.startsWith("!"))
		{
			pwd=pwd.substring(1);
			//write it to encoded format
			//encoding  byte array into base 64
			byte[] encoded = Base64.encodeBase64(pwd.getBytes()); 
			
			try
			{
				FileInputStream in = new FileInputStream(GDYNCallLoyaltyWebserviceSite.class.getClassLoader().getResource("application.properties").getPath()) ;
				Properties props = new Properties();
				props.load(in);
				in.close();
				
				FileOutputStream out = new FileOutputStream(GDYNCallLoyaltyWebserviceSite.class.getClassLoader().getResource("application.properties").getPath());
				props.setProperty("LoyaltyWebServicePwd", new String(encoded));
				props.store(out, null);
				out.close();
			}
			catch(IOException e)
			{
				logger.error("error writing output to the file application.properties.");
			}
				
		}
		else
		{
			//decode pwd from application dot properties file
			byte[] decoded = Base64.decodeBase64(pwd.getBytes());
			pwd = new String(decoded);
		}*/
		
		
		
		//trying to get credential from xml file and if not encoded , encode password  via local WebserviceCD.xml
		
		try
	    {
			// get config path using application.properties file.
		 	String xmlPATH= GDYNCallLoyaltyWebserviceSite.class.getClassLoader().getResource("application.properties").getPath();
		 	xmlPATH= xmlPATH.substring(0,xmlPATH.length()-22);
		 	xmlPATH = xmlPATH + "WebserviceCD.xml";
		 	
	    	File xmlFile = new File(xmlPATH);
	    	
	    	// start parsing
	    	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(xmlFile);
			
			//Node cred = document.getFirstChild();
			
			//get service name , currently we have loyalty and employee purchase. loyalty should be added in first node
			Node service = document.getElementsByTagName("service").item(0);
			
			
			// get child nodes of service in list.
			NodeList list = service.getChildNodes();
			
			//loop to find out the specific tag
			for (int i = 0; i < list.getLength(); i++) 
			{

                Node node = list.item(i);
                
                
                // if node is username , get username from it.
                if ("username".equals(node.getNodeName())) 
 			   {
                	username = node.getTextContent();
 			   }
			

			   // get the password element, if not encrypted encrypt it.
			   if ("password".equals(node.getNodeName())) 
			   {
				   //node.getTextContent();
				   
				   if(node.getTextContent().startsWith("!"))
				   {
					   
					   pwd=node.getTextContent().substring(1);
					   
					   //encode password and save it to xml.
					   byte[] encoded = Base64.encodeBase64(pwd.getBytes()); 
					   node.setTextContent(new String(encoded));
					   
					   TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(document);
						StreamResult result = new StreamResult(new File(xmlPATH));
						transformer.transform(source, result);
					   
				   }
				   else
				   {
					   // password is already encoded so just get the value and skip transforming
					   byte[] decoded = Base64.decodeBase64(node.getTextContent().getBytes());
						pwd = new String(decoded);
					   
				   }
			   }
			   
			   
			   // if node is URL , get URL from it.
               if ("URL".equals(node.getNodeName())) 
			   {
               	URL = node.getTextContent();
			   }
			
			}
			//nodeAttr.setTextContent("2");

			
	    }
	    catch(IOException e)
	    {
	    	logger.error("file not found");
	    }
	    catch(SAXException e)
	    {
	    	logger.error("tag not found");
	    }
	    catch(ParserConfigurationException e)
	    {
	    	logger.error(" parsing error");
	    }
		catch (TransformerException e) 
		{
			logger.error("XML transform error");
		}

		
		 // get credential from properties file.
		 
		 
		 //set credential
		 CredentialsProvider provider = new BasicCredentialsProvider();
		 UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, pwd);
		 provider.setCredentials(AuthScope.ANY, credentials);
		 //set time out for webservice
		 
		 String timeOutMilliSeconds = Gateway.getProperty("application", "LoyaltyCouponConnectionTimeOut", "5000");
	     int connTimeout=Integer.parseInt(timeOutMilliSeconds);
		 
		 RequestConfig.Builder requestBuilder = RequestConfig.custom();
		 requestBuilder = requestBuilder.setConnectTimeout(connTimeout);
		 requestBuilder = requestBuilder.setConnectionRequestTimeout(connTimeout);

		 HttpClientBuilder builder = HttpClientBuilder.create();     
		 builder.setDefaultRequestConfig(requestBuilder.build());
		 HttpClient client = builder.build();
		 client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		 
		 URL=URL+locale1.getLanguage()+"/user/"+loyaltyID+"/validate/"+CouponCode;
		 
		
		 // hardcode to skip it untill we have stub response, testing pending for later
		 
		 //boolean skipWebserviceCall = false;
		 
		/* if(skipWebserviceCall)
		 {*/
		 
			  HttpGet request = new HttpGet(URL);
			  String errorCode = "";
			  String errorMessage = "";
			  //request.set
			  try
			  {
				  
			  
				  HttpResponse response = client.execute(request);
				  if(response!=null )
				  {
				  BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
				  String line = "";
				  rd.toString();
				  StringBuilder sb = new StringBuilder();
				  while ((line = rd.readLine()) != null) 
				  {
				    //System.out.println(line);
				    logger.info(line);
				    sb.append(line);
				  }
				  
				  JSONObject json = new JSONObject(sb.toString());
				  
				  String responseStatus = json.getString("status");
				  
				  
				 //boolean hardcode= true;
				  
				  if(responseStatus.equals("OK"))//hard coding for now responseStatus.equals("OK"))
				   {
					
					  if(json.get("valid").equals(true))//json.getString("valid_coupon").equals("true"))
					  {
						 					 
							  saleCargo.setLoyaltyCouponPresntinTrxn(true);
							  letter = "Valid";
							  bus.mail(new Letter(letter), BusIfc.CURRENT);
					  		  
							 // JSONObject myObject = new JSONObject(source);
						 	
					  }
					  else
					  {
						  errorCode= json.getString("code");
						  errorMessage = json.getString("message");  
						  POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
				            DialogBeanModel dialogBean = new DialogBeanModel();
				            Color BannerColor = Color.RED;
				            String strBannerColor = UIFactory.getInstance().getUIProperty("Color.attention",
				            		LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE));
				            if (!strBannerColor.equals(""))
				            {
				                BannerColor = Color.decode(strBannerColor);
				            }
				            String msg = errorCode + "  " + errorMessage;
				            String args[] = new String[2];
				            args[0]=errorCode;
				            args[1]=errorMessage;				            
				            dialogBean.setArgs(args);
				            //dialogBean.setDescription(msg);
				            dialogBean.setResourceID("InvalidCoupon");
				            dialogBean.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
				            
				            dialogBean.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Invalid");
				            dialogBean.setBannerColor(BannerColor);
	
				            ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogBean);
						  
					  }
					  
				  }
				  else
				  {
					 // JSONObject errorJson = new JSONObject(sb.toString());
					  //saleCargo.setLoyaltyIdNumber("");
					 Object errorJson = json.get("error");					 
					  errorCode= ((JSONObject) errorJson).getString("code");
					  errorMessage = ((JSONObject) errorJson).getString("message");
					  
					  POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
			            DialogBeanModel dialogBean = new DialogBeanModel();
			            Color BannerColor = Color.RED;
			            String strBannerColor = UIFactory.getInstance().getUIProperty("Color.attention",
			            		LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE));
			            if (!strBannerColor.equals(""))
			            {
			                BannerColor = Color.decode(strBannerColor);
			            }
			            String args[] = new String[2];
			            args[0]=errorCode;
			            args[1]=errorMessage;
			            //errorCode + "  " + errorMessage;
			            //dialogBean.setDescription(msg);
			            dialogBean.setArgs(args);
			            dialogBean.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
			            dialogBean.setResourceID("LoyaltyIDUnrecognized");
			            dialogBean.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Invalid");
			            dialogBean.setBannerColor(BannerColor);	
			            ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogBean);
					  
					  
				  }
				  }
				  //If WebService is down still the coupon is accepted
				  else
				  {
					  saleCargo.setLoyaltyCouponPresntinTrxn(true);
					  letter = "Valid";
					  bus.mail(new Letter(letter), BusIfc.CURRENT);
				  }
			  }
			  catch(ClientProtocolException e)
			  {
				  POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
		            DialogBeanModel dialogBean = new DialogBeanModel();
		            Color BannerColor = Color.RED;
		            String strBannerColor = UIFactory.getInstance().getUIProperty("Color.attention",
		            		LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE));
		            if (!strBannerColor.equals(""))
		            {
		                BannerColor = Color.decode(strBannerColor);
		            }
		            dialogBean.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
		            
		            dialogBean.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Invalid");
		            dialogBean.setBannerColor(BannerColor);
	
		            ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogBean);
				  
			  }
			  catch(IOException e)
			  {
				  POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
		            DialogBeanModel dialogBean = new DialogBeanModel();
		            Color BannerColor = Color.RED;
		            String strBannerColor = UIFactory.getInstance().getUIProperty("Color.attention",
		            		LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE));
		            if (!strBannerColor.equals(""))
		            {
		                BannerColor = Color.decode(strBannerColor);
		            }
		            dialogBean.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
		            
		            dialogBean.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Invalid");
		            dialogBean.setBannerColor(BannerColor);
	
		            ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogBean);
				  
			  }
			  catch(Exception e)
			  {
				  POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);				 
		            DialogBeanModel dialogBean = new DialogBeanModel();
		            Color BannerColor = Color.RED;
		            String strBannerColor = UIFactory.getInstance().getUIProperty("Color.attention",
		            		LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE));
		            if (!strBannerColor.equals(""))
		            {
		                BannerColor = Color.decode(strBannerColor);
		            }
		            dialogBean.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
		            
		            dialogBean.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Invalid");
		            dialogBean.setBannerColor(BannerColor);
	
		            ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogBean);
				  
				  
			  }
		 }
		 /*else
		 {	
			 	letter = "Valid";
			 	saleCargo.setLoyaltyCouponPresntinTrxn(true);
			  bus.mail(new Letter(letter), BusIfc.CURRENT);
		 }
		  
	}*/
	
	
	
		
		
	}
		

	


