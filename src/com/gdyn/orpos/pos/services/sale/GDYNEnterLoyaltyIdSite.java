package com.gdyn.orpos.pos.services.sale;

// foundation imports
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;

import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;

//--------------------------------------------------------------------------
/**
 This site displays the ENTER_LOYALTY_ID screen when we did not captured at the start.
 <p>
 @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
//--------------------------------------------------------------------------
/**
 * @author VivekIgnitiv
 *
 */
public class GDYNEnterLoyaltyIdSite extends PosSiteActionAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4214625506436563691L;
	/**
	 * revision number
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	public static final String LOYALTY_ID = "";

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

		/*
		 * no parameter check is required here. we already checked with the
		 * paramter. This should always show if we have not captured the
		 * loyalty/email at the start or startandend.
		 */
		POSUIManagerIfc ui;

		ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
		ui.showScreen(GDYNPOSUIManagerIfc.ENTER_LOYALTY_ID,
				new POSBaseBeanModel());

	}
}
