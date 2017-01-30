package oracle.retail.stores.domain.manager.datareplication;

import oracle.retail.stores.domain.manager.daemon.DaemonThread;
import oracle.retail.stores.domain.manager.daemon.DaemonThreadIfc;
import oracle.retail.stores.foundation.utility.Util;

import org.apache.log4j.Logger;


public class GDYNLoyaltyCouponReaderDaemonThread extends DaemonThread implements DaemonThreadIfc
{


	   public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";
	   private static final Logger logger = Logger.getLogger(GDYNLoyaltyCouponReaderDaemonThread.class);
	   protected String extractorConfigurationFileName = null;
	   protected GDYNLoyaltyCouponBatchGenerator processor = null;
	   /*     */   
	   /*     */ 
	   protected boolean compressed = false;
	   
	 
	 
	 
	   public GDYNLoyaltyCouponReaderDaemonThread()
	   {
		   super("GDYNLoyaltyCouponReaderDaemonThread");
		   logger.info("GDYNLoyaltyCouponReaderDaemonThread()");
	    
	     if (logger.isDebugEnabled())
	     {
	       logger.debug("Loyalty Coupon Reader Daemon created");
	     }
	   }
	   
	 
	 
	 
	 
	 
	   protected void initializeRunCycle()
	   {
	     super.initializeRunCycle();
	     
	     try
	     {
	       if (logger.isInfoEnabled())
	       {
	         StringBuilder buff = new StringBuilder();
	         buff.append("GDYNLoyaltyCouponReaderDaemonThread Run cycle begins: ");
	         buff.append("LogWriter:");
	         buff.append("\"");
	         buff.append(isCompressed());
	         buff.append("\"");
	         logger.info(buff.toString());
	       }
	       this.processor = new GDYNLoyaltyCouponBatchGenerator();
	       GDYNLoyaltyCouponBatchGenerator dProcessor = (GDYNLoyaltyCouponBatchGenerator)this.processor;
	       //dProcessor.setExtractorConfigurationFileName(getExtractorConfigurationFileName());
	       //dProcessor.setCompressed(isCompressed());
	     }
	     catch (Exception e)
	     {
	       logger.error("An exception occurred during Loyalty Coupon Batch generator initialization " + e.toString() + "");
	       
	       logger.error("" + Util.throwableToString(e) + "");
	     }
	     
	     if (logger.isInfoEnabled()) {
	       logger.info("Loyalty Coupon Batch generator run cycle initialization complete");
	     }
	   }
	   
	 
	 
	 
	 
	   protected void runTasks()
	   {
	     this.processor.importLoyaltyCouponfromXML();
	   }
	   
	 
	 
	 
	 
	   public String toString()
	   {
	     String string = super.toString();
	     
	     StringBuilder strResult = new StringBuilder();
	     strResult.append(string).append(Util.classToStringHeader("GDYNLoyaltyCouponReaderDaemonThread", 
	    		 getRevisionNumber(), hashCode())).append(Util.formatToStringEntry("compression ", isCompressed()));
		 
	     return strResult.toString();
	   }
	   
	 
	 
	 
	 
	 
	   public String getRevisionNumber()
	   {
	     return Util.parseRevisionNumber("$Revision: /rgbustores_13.4x_generic_branch/1 $");
	   }
	   
	 
	 
	 
	 
	 
	
	 
	 
	 
	 
	   public boolean isCompressed()
	   {
	     return this.compressed;
	   }
	   
	 
	 
	 
	 
	 
	   public void setCompressed(boolean value)
	   {
	     this.compressed = value;
	   }

}
