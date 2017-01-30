package oracle.retail.stores.domain.manager.datareplication;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.retail.stores.domain.arts.DataTransactionFactory;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.tour.gate.Gateway;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdyn.orpos.domain.arts.GDYNCouponAttributeDataTransaction;
import com.gdyn.orpos.domain.arts.GDYNDataTransactionKeys;



public class GDYNLoyaltyCouponBatchGenerator 
{
	
    protected static final Logger logger = Logger.getLogger(GDYNLoyaltyCouponBatchGenerator.class);
    
    ArrayList<CouponAttribute> couponModels = null;
    
    File[] xmlPaths = null;

	private String inputDirectory = "c:/LoyaltyCoupon/incoming/";
	
	private String outputDirectory = "c:/LoyaltyCoupon/archive/";
	
	private String errorDiectory = "C:/LoyaltyCoupon/error/";
	private String currentDateTime = null;
	
	boolean key = false;
    
    

	public GDYNLoyaltyCouponBatchGenerator() 
	{
		// TODO Auto-generated constructor stub
		logger.debug("GDYNLoyaltyCouponBatchGenerator.GDYNLoyaltyCouponBatchGenerator()");
		
	}
	
	public boolean importLoyaltyCouponfromXML()
	{
		logger.debug("GDYNLoyaltyCouponBatchGenerator.GDYNLoyaltyCouponBatchGenerator()");
		
		boolean bOk = true;
		
		initializeDirectories();
		
		
		 try
	        {
			 // get list of files from the common directories.
			 logger.debug("Start loyalty Coupon extraction from xml");
			 logger.debug("Look for list of xml files in the current directory");
			 
			 LookUpLoyaltyCouponXMLList();
	        }
	        // catch any other exception that slipped through the cracks
		    catch(NullPointerException e)
		 	{
		    	logger.debug("Unable to read XML File");
		    	logger.debug(e.getMessage());
		    	bOk = false;
		 	}
	        catch (Exception e)
	        {
	        	logger.debug("Unable to read XML File due to unknown exception");
		    	logger.debug(e.getMessage());
	        	bOk = false;
	        }
		 
		 	if(bOk)
		 	{
		 		// if xml are read to the list, lets parse them and update to database one by one 
		 		logger.debug("start parsing XML one by one");
		 		bOk = parseXMLFileAndUpdateToDB();
		 	}
		 
		 

	        if (logger.isDebugEnabled())
	        {
	            logger.debug("Completed redaing of Loyalty Coupon");
	        }
		
		
		 return bOk;
		
	}

	private void initializeDirectories()
	{
		String inputDirectory = Gateway.getProperty("application", "dir.coupon", this.inputDirectory);
		String outputDirectory = Gateway.getProperty("application", "dir.coupon.output", this.outputDirectory);
		String errorDiectory = Gateway.getProperty("application", "dir.coupon.error", this.errorDiectory);

		this.inputDirectory=inputDirectory;
		this.outputDirectory=outputDirectory;
		this.errorDiectory=errorDiectory;
		
	}

	private boolean parseXMLFileAndUpdateToDB() 
	{
		logger.debug("GDYNLoyaltyCouponBatchGenerator.parseXMLFileAndUpdateToDB()");
		boolean bOk = false;
		key = true;
		
		//loop in xml files one by one
		
		if(xmlPaths!=null)
		{
			for(int i=0;i < xmlPaths.length;i++)
			{
				// parse xml file into coupon attribute object.
				
				try
				{
					couponModels = parseXMLtoModel(xmlPaths[i]);
				}
				catch(SAXException e)
				{
					logger.debug("error key is true sax exception");
					key = false;
					logger.error(" Error parsing the file \n"+e );
				}
				catch(IOException e)
				{
					logger.debug("error key is true IOException");
					key = false;
					logger.error("I/O Error while reaching the file \n"+e );
				}
				catch(ParserConfigurationException e)
				{
					logger.debug("error key is true ParserConfigurationException");
					key = false;
					logger.error("xml configuration error \n"+e );
				}
				catch(Exception e)
				{
					logger.debug("error key is true unknownException");
					key = false;
					logger.error("unknown exception  \n"+e );
				}
				
				//update coupon attributes to DB.
				if(couponModels!=null)
				{
					try
					{
					
						bOk = updateXMLtoDatabase(couponModels);
					}
					catch(DataException e)
					{
						logger.error("Database read/write Error while copying the file \n"+e );
						//key = true;
	
					}
					catch(Exception e)
					{
						logger.error("Unknown Error while accessing the database t \n"+e );
						//key = true;
	
					}
				
					// move file to output folder.
					try
					{
						moveProcessedXmltoOutputDirectory(xmlPaths[i]);
					}
					catch(IOException e)
					{
						logger.error("I/O Error while accessing the file \n"+e );
	
					}
					catch(Exception e)
					{
						logger.error("Unknown error while moving the file \n"+e );
	
					}
				}
				else
				{
					try
					{
						moveProcessedXmltoOutputDirectory(xmlPaths[i]);
					}
					catch(IOException e)
					{
						logger.error("I/O Error while accessing the file \n"+e );
	
					}
					catch(Exception e)
					{
						logger.error("Unknown error while moving the file \n"+e );
	
					}
					bOk = false;
					
				}
			}
			
		}
		
		return bOk;
	}

	private void moveProcessedXmltoOutputDirectory(File file) throws IOException
	{
		logger.debug("GDYNLoyaltyCouponBatchGenerator.moveProcessedXmltoOutputDirectory()");
		logger.debug("output directory = "+outputDirectory);
		logger.debug("current file path = "+file.getAbsolutePath());

		InputStream inStream = null;
		OutputStream outStream = null;
			


		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		// get current date time with Date()
		Date date = new Date();

		currentDateTime = dateFormat.format(date);

			File afile = new File(inputDirectory + file.getName());

			if (key) {
				File bfile = new File(outputDirectory.concat(currentDateTime.toString())
						+ file.getName());

				inStream = new FileInputStream(afile);
				outStream = new FileOutputStream(bfile);
				logger.info("File processed successfully and moved to "
						+ outputDirectory + " with file name "
						+ currentDateTime.toString() + file.getName());

			} else {
				File bfile = new File(errorDiectory.concat(currentDateTime.toString())
						+ file.getName());

				inStream = new FileInputStream(afile);
				outStream = new FileOutputStream(bfile);
				logger.info("File processing is unsuccessfull and moved to "
						+ errorDiectory + " with file name "
						+ currentDateTime.toString() + file.getName());
			}

			byte[] buffer = new byte[1024];

			int length;

			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}

			inStream.close();
			outStream.close();

			// delete the original file
			afile.delete();
		
    	    
		
	}
	
	

	private boolean updateXMLtoDatabase(ArrayList<CouponAttribute> couponModels) throws DataException
	{
		// We ned to save the coupon Attribute
		GDYNCouponAttributeDataTransaction ct = (GDYNCouponAttributeDataTransaction)DataTransactionFactory
                .create(GDYNDataTransactionKeys.COUPON_ATTRIBUTE_DATA_TRANSACTION);
        //ct.setTransactionName("UpdateCouponAttribute");
        
        ct.updateCouponAttributes(couponModels);
        
		
		
		return false;
	}

	private ArrayList<CouponAttribute> parseXMLtoModel(File file) throws SAXException, IOException, ParserConfigurationException
	{
		logger.debug("GDYNLoyaltyCouponBatchGenerator.parseXMLtoModel()");

		
		
		// create an array List to save coupon attribute from XML.
		ArrayList<CouponAttribute> myCouponAttList = null;
		
			// read file into file object
			//File xmlFile = new File(file.getAbsolutePath());
			//parsing using DOM parser.
			logger.debug("Parsing of couponAttribute XML begin");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);Element docEle = document.getDocumentElement();

			//get a nodelist of elements
			
			NodeList nl = docEle.getElementsByTagName("CouponAttribute");
			
			myCouponAttList = new ArrayList<CouponAttribute>();
			if(nl != null && nl.getLength() > 0) 
			{
				for(int i = 0 ; i < nl.getLength();i++) 
				{

					//get the employee element
					Element el = (Element)nl.item(i);

					//get the Employee object
					CouponAttribute e = getCouponAttribute(el);

					//add it to list
					myCouponAttList.add(e);
				}
			}
		
		
		
		return myCouponAttList;
	}
	
	
	private static CouponAttribute getCouponAttribute(Element empEl) 
	{
		//for each <CouponAttribute> element get text  of all attrinutes
				logger.debug("GDYNLoyaltyCouponBatchGenerator.getCouponAttribute()");
				String id = empEl.getAttribute("ID");
				String minThreshold = empEl.getAttribute("MinThreshold");
				String couponType = empEl.getAttribute("CouponType");
				String externalSysValidation = empEl.getAttribute("ExternalSysValidation");
				String maxAmount = empEl.getAttribute("MaxAmount");
				String maxDiscount = empEl.getAttribute("MaxDiscount");
				
				logger.debug("Coupon Attributes inside XML");
				logger.debug("\n ID = "+id+"\t minimum threshold = "+minThreshold+"\t coupon type = "+
				couponType+"\t max amount = "+maxAmount+"\t max discount = "+maxDiscount+"\t external "
						+ "sys validation = "+externalSysValidation);
				CouponHierarchy couponhierarchys[] = null;
				//empEl.get
				NodeList HierachyList = empEl.getElementsByTagName("Hierarchy");
				
				couponhierarchys = new CouponHierarchy[HierachyList.getLength()];
				
				if(HierachyList != null && HierachyList.getLength() > 0) 
				{
					for(int i = 0 ; i < HierachyList.getLength();i++) 
					{

						//get the employee element
						Element el = (Element)HierachyList.item(i);

						//get the Employee object
						CouponHierarchy e = getCouponHierarchy(el);

						//add it to list
						couponhierarchys[i]=e;
					}
				}
				

				//Create a new Employee with the value read from the xml nodes
				CouponAttribute e = new CouponAttribute(id,minThreshold,couponType,maxAmount,maxDiscount,externalSysValidation,couponhierarchys);

				return e;
	}

	private static CouponHierarchy getCouponHierarchy(Element empEl) 
	{			
		logger.debug("GDYNLoyaltyCouponBatchGenerator.getCouponHierarchy()");

		String applyTO = empEl.getAttribute("ApplyTO");
		String division = empEl.getAttribute("Division");
		String group = empEl.getAttribute("Group");
		String department = empEl.getAttribute("Department");
		String hierarchyclass = empEl.getAttribute("Class");
		String subClass = empEl.getAttribute("SubClass");
		
		logger.debug("coupon hierarchy info inside coupon attribute");
		logger.debug("\n\n\n applyTO = "+applyTO+"\t division = "+division+"\t group = "+group+"\t department = "
				+department+ "\t class = "+hierarchyclass+"\t subclass = "+subClass);
		
		
		
		//Create a new Employee with the value read from the xml nodes
		CouponHierarchy e = new CouponHierarchy(applyTO,division,group,department,hierarchyclass,subClass);
		
		return e;
	}


	private void LookUpLoyaltyCouponXMLList() 
	{
			logger.debug("GDYNLoyaltyCouponBatchGenerator.LookUpLoyaltyCouponXMLList()");
		
			// read file from input directory
			logger.debug("reading file to list from inputDirectory");
		 	File dir = new File(inputDirectory );
		 	
		 	// filter xml files in case there are other formats, which we need to neglect.
			FileFilter filter = new FileFilter() {
			    @Override
			    public boolean accept(File file) {
			       return file.isFile() && file.getName().endsWith(".xml");
			    }
			};
			
			xmlPaths = dir.listFiles(filter);
			
			if(xmlPaths==null)
			{
				logger.debug("no files seems to be there inside directory , throwing null pointer exception");
				throw new NullPointerException("At the moment there are no xml files to read ");
			}
		
	}


}
