package com.gdyn.orpos.pos.services.sale;

// domain imports
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.ifc.LetterIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransaction;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.utility.GDYNCheckDigitUtility;

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
public class GDYNValidateLoyaltyIdBeforeTenderingSite extends
		PosSiteActionAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9091951266688381484L;
	/**
	 * revision number of this class
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	public static final String LOYALTY_ID = "";

	public static final String LOYALTY_EMAIL_ID = "";

	public static final String END = "2";

	public static final String START_AND_END = "3";
	
	public static final String LOYALTY_PHONE_NO = "";

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
		GDYNSaleCargo saleCargo = (GDYNSaleCargo) bus.getCargo();
		RetailTransactionIfc trans = saleCargo.getRetailTransaction();
		UtilityManagerIfc utility = (UtilityManagerIfc) bus
				.getManager(UtilityManagerIfc.TYPE);
		String customerLoyaltyPromptValue = "3";
		ParameterManagerIfc pm = (ParameterManagerIfc) bus
				.getManager(ParameterManagerIfc.TYPE);
		try {

			customerLoyaltyPromptValue = pm
					.getStringValue(GDYNParameterConstantsIfc.CUSTOMER_Prompt_For_Loyalty_Screen);
		} catch (ParameterException pe) {
			logger.error("Unable to get parameter value for "
					+ GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable + " "
					+ Util.throwableToString(pe) + "");
		}

		// display the dialog prompt if the Loyalty/email is not captured before
		// tendering
		if (LOYALTY_ID.equalsIgnoreCase(saleCargo.getLoyaltyIdNumber())
				&& LOYALTY_EMAIL_ID.equalsIgnoreCase(saleCargo
						.getLoyaltyEmailId())
				&& LOYALTY_PHONE_NO.equalsIgnoreCase(saleCargo.getLoyaltyPhoneNo().trim())
				&& (START_AND_END.equalsIgnoreCase(customerLoyaltyPromptValue) || END
						.equalsIgnoreCase(customerLoyaltyPromptValue))) {
			// initialize model bean
			DialogBeanModel dialogModel = new DialogBeanModel();
			dialogModel.setResourceID("NO_LOYALTY");
			dialogModel.setType(DialogScreensIfc.YES_NO);
			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_NO,
					CommonLetterIfc.CONTINUE);
			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_YES,
					CommonLetterIfc.OK);

			// display dialog
			POSUIManagerIfc ui = (POSUIManagerIfc) bus
					.getManager(UIManagerIfc.TYPE);
			ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);

		} else if (saleCargo.getLoyaltyIdNumber().length() >= 1
				&& saleCargo.getLoyaltyIdNumber().length() <= 10) {
			//POS-381 new condition added by Dharmendra to check if length of loyality id is 10
			if (saleCargo.getLoyaltyIdNumber().trim().length()==10&&
					utility.validateCheckDigit(
					GDYNCheckDigitUtility.CHECK_DIGIT_FUNCTION_LOYALTY_NUMBER,
					saleCargo.getLoyaltyIdNumber())) {
				// set the valid loyalty Id to the cargo
				saleCargo.setLoyaltyIdNumber(saleCargo.getLoyaltyIdNumber());
				if (trans instanceof GDYNSaleReturnTransaction) {

					GDYNSaleReturnTransactionIfc gdynTrans = (GDYNSaleReturnTransaction) trans;
					//code changes added by Dharmendra on 06/12/2016 to fix issue POS-346
					
					String originalLoyalityId = gdynTrans.getOriginalLoyaltyID();
					String originalLoyalityIdLeftPadded = "";
					if(!GDYNLoyalityConstants.isEmpty(originalLoyalityId) && originalLoyalityId.length()!=10){
					 originalLoyalityIdLeftPadded = String.format("%010d", Integer.parseInt(originalLoyalityId));
					}else{
						originalLoyalityIdLeftPadded = originalLoyalityId;
					}
					String saleCargoLoyalityIdLeftPadded = "";
					String saleCargoLoyalityId = saleCargo.getLoyaltyIdNumber();
					
					if (!GDYNLoyalityConstants.isEmpty(saleCargoLoyalityId)
							&& saleCargoLoyalityId.length() != 10) {
						saleCargoLoyalityIdLeftPadded = String.format("%010d",Integer.parseInt(saleCargoLoyalityId));
					} else {
						saleCargoLoyalityIdLeftPadded = saleCargoLoyalityId;
					}
					
					if (gdynTrans.getOriginalLoyaltyID() != null
							&& LOYALTY_ID.equalsIgnoreCase(gdynTrans
									.getOriginalLoyaltyID())) {

						bus.mail(letter, BusIfc.CURRENT);
						return;
					} else if (saleCargo.getLoyaltyIdNumber().equalsIgnoreCase(
							gdynTrans.getOriginalLoyaltyID())) {

						bus.mail(letter, BusIfc.CURRENT);
						return;

					}
					//else if block added by Dharmendra on 06/12/2016 to fix issue POS-346
					else if(saleCargoLoyalityIdLeftPadded.equalsIgnoreCase(originalLoyalityIdLeftPadded)){
						bus.mail(letter, BusIfc.CURRENT);
						return;
					}
					else {

						DialogBeanModel dialogModel = new DialogBeanModel();
						dialogModel.setResourceID("INCONSISTENT_LOYALTY_ID");
						dialogModel.setType(DialogScreensIfc.ERROR);
						dialogModel.setButtonLetter(
								DialogScreensIfc.BUTTON_CONTINUE,
								CommonLetterIfc.OK);

						// display dialog
						POSUIManagerIfc ui = (POSUIManagerIfc) bus
								.getManager(UIManagerIfc.TYPE);
						ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE,
								dialogModel);
					}
				} else {
					bus.mail(letter, BusIfc.CURRENT);
				}
			} else {
				saleCargo.setLoyaltyIdNumber(""); // set empty string if the
													// loyalty is not valid.
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
			saleCargo.setLoyaltyIdNumber(LOYALTY_ID);
			bus.mail(letter, BusIfc.CURRENT);
		}
	}

}
