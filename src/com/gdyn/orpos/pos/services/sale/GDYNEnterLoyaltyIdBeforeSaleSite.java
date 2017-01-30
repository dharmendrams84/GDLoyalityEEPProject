package com.gdyn.orpos.pos.services.sale;

// foundation imports
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.NavigationButtonBeanModel;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;

import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;
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
public class GDYNEnterLoyaltyIdBeforeSaleSite extends PosSiteActionAdapter {
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
		
		
		/*  code changes added by Ashwinee to fix  issue POS-317 on  11/01/2017 */
		   
		  /* GDYNSaleCargoIfc saleCargo = (GDYNSaleCargoIfc) bus.getCargo();  
		   saleCargo.setLoyaltyPhoneNo("");*/
		
		/* End code changes added by Ashwinee to fix issue POS-317 on  11/01/2017 */
		
		String customerLoyaltyPromptValue = "1"; // get the prompt for loyalty
													// value from parameter
													// (1=Start,
													// 2=End,3=StartAndEnd)
		POSUIManagerIfc ui;
		/*
		 * setting the empty string value to Loyalty and Email.
		 */

		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		cargo.setLoyaltyEmailId(LOYALTY_EMAIL_ID);
		cargo.setLoyaltyIdNumber(LOYALTY_ID);

		ParameterManagerIfc pm = (ParameterManagerIfc) bus
				.getManager(ParameterManagerIfc.TYPE);

		try {
			customerLoyaltyEnable = pm
					.getBooleanValue(GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable);

			customerLoyaltyPromptValue = pm
					.getStringValue(GDYNParameterConstantsIfc.CUSTOMER_Prompt_For_Loyalty_Screen);

			if (customerLoyaltyEnable) {
				if (customerLoyaltyPromptValue.equalsIgnoreCase("1")
						|| customerLoyaltyPromptValue.equalsIgnoreCase("3")) {

					ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
					//POSBaseBeanModel model = (POSBaseBeanModel)ui.getModel();					
					POSBaseBeanModel model = new POSBaseBeanModel();
					NavigationButtonBeanModel navModel = new NavigationButtonBeanModel();
					if(((GDYNSaleCargo)bus.getCargo()).isCouponRedemptionFlow())
					{
						/*  code changes added by Ashwinee to fix POS-374 issue on  11/01/2017 */
						navModel.setButtonEnabled("PhoneNo", false);
						
						navModel.setButtonEnabled("Email", false);
						
						
						
					}
					else
					{
						/*  code changes added by Ashwinee to fix POS-374 issue on  11/01/2017 */
						navModel.setButtonEnabled("PhoneNo", true);
						
						navModel.setButtonEnabled("Email", true);
											
					}
					
					model.setLocalButtonBeanModel(navModel);
					ui.showScreen(GDYNPOSUIManagerIfc.ENTER_LOYALTY_ID,
							model);
				} else {
					bus.mail(CommonLetterIfc.CONTINUE, BusIfc.CURRENT);

				}
			} else {
				bus.mail(CommonLetterIfc.CONTINUE, BusIfc.CURRENT);

			}
		} catch (ParameterException pe) {
			logger.error("Unable to get parameter value for "
					+ GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable + " "
					+ Util.throwableToString(pe) + "");
			// If there is Parameter Exception then still showing the Loyalty
			// Screen
			ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
			POSBaseBeanModel beanModel = new POSBaseBeanModel();
			//beanModel.
			NavigationButtonBeanModel navModel = new NavigationButtonBeanModel();
			if(((GDYNSaleCargo)bus.getCargo()).isCouponRedemptionFlow())
			{
				/*  code changes added by Ashwinee to fix POS-374 issue on  11/01/2017 */
				navModel.setButtonEnabled("PhoneNo", false);
				navModel.setButtonEnabled("Email", false);
							
			}
			
			beanModel.setLocalButtonBeanModel(navModel);
			ui.showScreen(GDYNPOSUIManagerIfc.ENTER_LOYALTY_ID,
					beanModel);
		}

	}

}
