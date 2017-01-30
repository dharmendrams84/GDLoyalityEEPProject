package com.gdyn.orpos.pos.services.returns.returnitem;

// foundation imports
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;

import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;
import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;

//--------------------------------------------------------------------------
/**
 This site displays the ENTER_LOYALTY_ID screen when doing returns.
 <p>
 @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
//--------------------------------------------------------------------------
/**
 * @author VivekIgnitiv
 *
 */
public class GDYNEnterOriginalLoyaltyIdSite extends PosSiteActionAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4214625506436563691L;
	/**
	 * revision number
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

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

		POSUIManagerIfc ui;
		GDYNReturnItemCargo cargo = (GDYNReturnItemCargo) bus.getCargo();
		ParameterManagerIfc pm = (ParameterManagerIfc) bus
				.getManager(ParameterManagerIfc.TYPE);

		try {
			cargo.setLoyaltyEnable(pm
					.getBooleanValue(GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable));
			/*
			 * Check if the Loyalty Enable parameter is set to true then show
			 * the Loyalty Prompt.
			 */
			if (cargo.isLoyaltyEnable()) {
				ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
				ui.showScreen(GDYNPOSUIManagerIfc.ENTER_ORIGINAL_LOYALTY_ID,
						new POSBaseBeanModel());
			}
			else {
				bus.mail(CommonLetterIfc.CONTINUE, BusIfc.CURRENT);
			}
		} catch (ParameterException pe) {
			logger.error("Unable to get parameter value for "
					+ GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable + " "
					+ Util.throwableToString(pe) + "");
			// If there is Parameter Exception then still showing the Loyalty
			// Screen
			ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
			ui.showScreen(GDYNPOSUIManagerIfc.ENTER_ORIGINAL_LOYALTY_ID,
					new POSBaseBeanModel());
		}
	}
}
