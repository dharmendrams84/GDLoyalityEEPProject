package com.gdyn.orpos.pos.services.sale;

import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;

import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

public class GDYNCheckForLoyaltyIDSite extends PosSiteActionAdapter 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5199607013057910617L;
	
	
	@Override
	public void arrive(BusIfc bus) 
	{
		
		// check if loyalty id is already added.
		
		String letter = "Continue";
		
		GDYNSaleCargoIfc saleCargo = (GDYNSaleCargoIfc) bus.getCargo();
		
		
		logger.debug("GDYNCheckForLoyaltyIDSite.arrive()"+ ((GDYNSaleReturnTransactionIfc)saleCargo.getTransaction()).getLoyaltyID());
		if(saleCargo.getLoyaltyIdNumber().equals(""))
		{
			
			saleCargo.setCouponRedemptionFlow(true);
			DialogBeanModel dialogModel = new DialogBeanModel();
			dialogModel.setResourceID("LoyaltyIDNotCaptured");
			dialogModel.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
			//String msg = "Press Ok to Capture Loyalty ID";
			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK,
					CommonLetterIfc.NO);

			// display dialog
			POSUIManagerIfc ui = (POSUIManagerIfc) bus
					.getManager(UIManagerIfc.TYPE);
			ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
			//letter = "No";
		}
		else
		{
			bus.mail(new Letter(letter), BusIfc.CURRENT);
		}
	}

}
