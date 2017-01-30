package com.gdyn.orpos.pos.services.sale;

// foundation imports
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.LaneActionAdapter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;

//--------------------------------------------------------------------------
/**
 This road is traveled when the loyalty ID has been entered.
 It stores the loyalty ID in the cargo.
 <p>
 @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
//--------------------------------------------------------------------------
/**
 * @author VivekIgnitiv
 *
 */
public class GDYNLoyaltyIdEnteredRoad extends LaneActionAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 387947864548336466L;

	/**
	 * revision number
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	// ----------------------------------------------------------------------
	/**
	 * Stores the loyalty ID in the cargo.
	 * <P>
	 * 
	 * @param bus
	 *            Service Bus
	 **/
	// ----------------------------------------------------------------------
	public void traverse(BusIfc bus) {
		/*
		 * Get the input value from the UI Manager
		 */
		POSUIManagerIfc ui;
		ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
		String value = ui.getInput();
		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		if (value != null) {
			cargo.setLoyaltyIdNumber(value);
		}
	}
}
