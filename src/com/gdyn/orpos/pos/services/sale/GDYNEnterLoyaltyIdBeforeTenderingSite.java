package com.gdyn.orpos.pos.services.sale;

// foundation imports
import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;
import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;

import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;

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
public class GDYNEnterLoyaltyIdBeforeTenderingSite extends PosSiteActionAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4214625506436563691L;
	/**
	 * revision number
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	public static final String LOYALTY_ID = "";

	public static final String LOYALTY_EMAIL_ID = "";

	public static final String LOYALTY_PHONE_NO = "";
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
		boolean customerLoyaltyEnable = false;
		String customerLoyaltyPromptValue = "2";// get the prompt for loyalty
												// value from parameter
												// (1=Start,
												// 2=End,3=StartAndEnd)
		GDYNSaleCargo saleCargo = (GDYNSaleCargo) bus.getCargo();

		if (saleCargo.getLoyaltyIdNumber() != null
				&& saleCargo.getLoyaltyIdNumber().equalsIgnoreCase(LOYALTY_ID)) {
			POSUIManagerIfc ui;

			ParameterManagerIfc pm = (ParameterManagerIfc) bus
					.getManager(ParameterManagerIfc.TYPE);

			try {
				customerLoyaltyEnable = pm
						.getBooleanValue(GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable);

				customerLoyaltyPromptValue = pm
						.getStringValue(GDYNParameterConstantsIfc.CUSTOMER_Prompt_For_Loyalty_Screen);

				if (customerLoyaltyEnable) {
					// checking for the parameter value and also if already
					// loyalty/email is captured then continue to the tender
					// screen
					if ((customerLoyaltyPromptValue.equalsIgnoreCase("2") || customerLoyaltyPromptValue
							.equalsIgnoreCase("3"))
							&& (LOYALTY_ID.equalsIgnoreCase(saleCargo
									.getLoyaltyIdNumber()) && LOYALTY_EMAIL_ID
									.equalsIgnoreCase(saleCargo
											.getLoyaltyEmailId())&& LOYALTY_PHONE_NO.equalsIgnoreCase(saleCargo.getLoyaltyPhoneNo()))) {
						ui = (POSUIManagerIfc) bus
								.getManager(UIManagerIfc.TYPE);
						ui.showScreen(GDYNPOSUIManagerIfc.ENTER_LOYALTY_ID,
								new POSBaseBeanModel());
					} else {
						bus.mail(CommonLetterIfc.NEXT, BusIfc.CURRENT);

					}
				} else {

					bus.mail(CommonLetterIfc.CONTINUE, BusIfc.CURRENT);
				}
			} catch (ParameterException pe) {
				logger.error("Unable to get parameter value for "
						+ GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable
						+ " " + Util.throwableToString(pe) + "");
				// If there is Parameter Exception then still showing the
				// Loyalty Screen
				ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
				ui.showScreen(GDYNPOSUIManagerIfc.ENTER_LOYALTY_ID,
						new POSBaseBeanModel());
			}

		} else {
			bus.mail(CommonLetterIfc.CONTINUE, BusIfc.CURRENT);
		}
	}
}
