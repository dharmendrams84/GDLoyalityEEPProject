package com.gdyn.orpos.pos.services.returns.returnitem;

// domain imports
import com.gdyn.orpos.utility.GDYNCheckDigitUtility;

import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.ifc.LetterIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

//--------------------------------------------------------------------------
/**
 This site validates the loyalty ID.
 <p>
 @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
//--------------------------------------------------------------------------
/**
 * @author VivekIgnitiv
 *
 */
public class GDYNValidateLoyaltyIdSite extends PosSiteActionAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9091951266688381484L;
	/**
	 * revision number of this class
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	// ----------------------------------------------------------------------
	/**
	 * Looks up the loyalty from the cargo. Sends a Success letter if found,
	 * sends a Failure letter if not found.
	 * <P>
	 * 
	 * @param bus
	 *            Service Bus
	 **/
	// ----------------------------------------------------------------------
	public void arrive(BusIfc bus) {
		
		LetterIfc letter = new Letter(CommonLetterIfc.SUCCESS);
		GDYNReturnItemCargo cargo = (GDYNReturnItemCargo) bus.getCargo();
		UtilityManagerIfc utility = (UtilityManagerIfc) bus
				.getManager(UtilityManagerIfc.TYPE);
		
		if (cargo.getOriginalLoyaltyID().length() >= 1
				&& cargo.getOriginalLoyaltyID().length() <= 10) {
			//POS-381 new condition added by Dharmendra to check if length of loyality id is 10
			if (cargo.getOriginalLoyaltyID().trim().length() == 10
					&& utility
							.validateCheckDigit(
									GDYNCheckDigitUtility.CHECK_DIGIT_FUNCTION_LOYALTY_NUMBER,
									cargo.getOriginalLoyaltyID())) {
				bus.mail(letter, BusIfc.CURRENT);
			} else {
				DialogBeanModel dialogModel = new DialogBeanModel();
				dialogModel.setResourceID("INVALID_LOYALTY_ID");
				dialogModel.setType(DialogScreensIfc.ERROR);
				dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_CONTINUE,
						CommonLetterIfc.OK);

				// display dialog
				POSUIManagerIfc ui = (POSUIManagerIfc) bus
						.getManager(UIManagerIfc.TYPE);
				ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
			}
		} else {
			cargo.setOriginalLoyaltyID(""); // set empty string if the loyalty is not valid.
			bus.mail(letter, BusIfc.CURRENT);
		}
	}

}
