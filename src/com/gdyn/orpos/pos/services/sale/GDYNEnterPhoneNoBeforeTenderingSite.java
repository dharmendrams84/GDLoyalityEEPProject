package com.gdyn.orpos.pos.services.sale;

// foundation imports
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DataInputBeanModel;
import oracle.retail.stores.pos.ui.beans.GDYNPhoneNoBeanModel;

import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;

//--------------------------------------------------------------------------
/**
 This site displays the ENTER_LOYALTY_ID screen.
 <p>
 @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
//--------------------------------------------------------------------------
/**
 * @author VivekIgnitiv
 *
 */
public class GDYNEnterPhoneNoBeforeTenderingSite extends PosSiteActionAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1;
	/**
	 * revision number
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	public static final String BLANK_STRING = "";

	// ----------------------------------------------------------------------
	/**
	 * Displays the ENTER_LOYALTY_ID screen.
	 * <P>
	 * 
	 * @param bus
	 *            Service Bus
	 **/
	// ----------------------------------------------------------------------
	public void arrive(BusIfc bus) {
		GDYNSaleCargo saleCargo = (GDYNSaleCargo) bus.getCargo();
		POSUIManagerIfc ui;
		if (saleCargo.getLoyaltyPhoneNo() != null
				&& saleCargo.getLoyaltyPhoneNo().equalsIgnoreCase(
						BLANK_STRING)) {
		        ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
		        ui.showScreen(GDYNPOSUIManagerIfc.ENTER_PHONE_NO, new GDYNPhoneNoBeanModel());
		}
		else {
			bus.mail(CommonLetterIfc.CONTINUE, BusIfc.CURRENT);
		}
		
		
	}

}
