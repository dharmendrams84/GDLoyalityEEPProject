package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.LaneActionAdapter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.CaptureCustomerInfoBeanModel;
import oracle.retail.stores.pos.ui.beans.DataInputBeanModel;
import oracle.retail.stores.pos.ui.beans.GDYNPhoneNoBeanModel;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;

public class GDYNPhoneNoEnteredRoad extends LaneActionAdapter{

	 /**
		 * 
		 */
		private static final long serialVersionUID = 387947864548336466L;
	

		/**
	        revision number
	    **/
	    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";
	    
	    //----------------------------------------------------------------------
	    /**
	        Stores the loyalty ID in the cargo.
	        <P>
	        @param  bus  Service Bus
	    **/
	    //----------------------------------------------------------------------
	public void traverse(BusIfc bus) {
	     /*
	      * Get the input value from the UI Manager
	      */
		POSUIManagerIfc ui = (POSUIManagerIfc) bus
				.getManager(UIManagerIfc.TYPE);
	    POSBaseBeanModel p =	(POSBaseBeanModel) ui.getModel();
	    
	    GDYNPhoneNoBeanModel dataBeanModel = (GDYNPhoneNoBeanModel) ui
				.getModel(GDYNPOSUIManagerIfc.ENTER_PHONE_NO);
		
		
		String phoneNo = dataBeanModel.getFieldPhoneNo();
		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		
		if (!GDYNLoyalityConstants.isEmpty(phoneNo)) {
			cargo.setLoyaltyPhoneNo(phoneNo.trim());
		} else {
			cargo.setLoyaltyPhoneNo("");
		}
		logger.info("Exiting GDYNPhoneNoEnteredRoad");
	}
}
