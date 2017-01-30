package oracle.retail.stores.domain.manager.datareplication;

import oracle.retail.stores.domain.manager.daemon.DaemonTechnician;
import oracle.retail.stores.foundation.tour.manager.TechnicianIfc;
import oracle.retail.stores.foundation.utility.Util;

public class GDYNLoyaltyCouponReaderDaemonTechnician extends DaemonTechnician implements TechnicianIfc
{
	 
	   public GDYNLoyaltyCouponReaderDaemonTechnician() 
	   {
		   
	   }
	 
	   protected void configureDaemonTread()
	   {
		     super.configureDaemonTread();
		     GDYNLoyaltyCouponReaderDaemonThread pThread = (GDYNLoyaltyCouponReaderDaemonThread)this.daemonThread;
	   
	   }
	 
	   public String toString()
	   {
	     StringBuilder strResult = Util.classToStringHeader("GDYNLoyaltyCouponReaderDaemonTechnician", getRevisionNumber()
	    		 , hashCode()).append(Util.formatToStringEntry("daemon name", getDaemonName()))
	    		 .append(Util.formatToStringEntry("sleep interval", Long.toString(getSleepInterval())))
	    		 .append(Util.formatToStringEntry("automatic start", isAutomaticStart()))
	    		 .append(Util.formatToStringEntry("daemon thread class name", getDaemonClassName()));
		 
	     return strResult.toString();
	   }


}
