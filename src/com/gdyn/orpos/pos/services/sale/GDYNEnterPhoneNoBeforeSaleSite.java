package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;

import oracle.retail.stores.pos.ui.beans.GDYNPhoneNoBeanModel;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;
import oracle.retail.stores.pos.ui.beans.ValidatingBean;

import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;
import com.gdyn.orpos.pos.ui.beans.GDYNEnterPhoneNoBean;

public class GDYNEnterPhoneNoBeforeSaleSite extends PosSiteActionAdapter{

	 /**
		 * 
		 */
		private static final long serialVersionUID = -4214625506436563691L;
		/**
	        revision number
	    **/
	    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	    //----------------------------------------------------------------------
	    /**
	        Displays the ENTER_LOYALTY_ID screen.
	        <P>
	        @param  bus     Service Bus
	    **/
	    //----------------------------------------------------------------------
	    public void arrive(BusIfc bus)
	    {
	            
	        POSUIManagerIfc ui;
	        ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
	        ui.showScreen(GDYNPOSUIManagerIfc.ENTER_PHONE_NO, new GDYNPhoneNoBeanModel());
	        logger.info("Exiting GDYNEnterPhoneNoBeforeSaleSite");
	    }
	    
}
