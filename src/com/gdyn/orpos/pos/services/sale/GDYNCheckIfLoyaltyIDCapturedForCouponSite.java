package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;

public class GDYNCheckIfLoyaltyIDCapturedForCouponSite extends
		PosSiteActionAdapter 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5992604435884039946L;

	@Override
	public void arrive(BusIfc bus) 
	{
		
		GDYNSaleCargoIfc saleCargo = (GDYNSaleCargoIfc) bus.getCargo();
		
		if(saleCargo.getLoyaltyIdNumber().equals(""))
		{
			//Commented by Monica to fix POS-283
			/*((GDYNSaleReturnTransaction)saleCargo.getTransaction()).getItemContainerProxy().removeLineItem
			(((GDYNSaleReturnTransaction)saleCargo.getTransaction()).getLineItemsVector().size()-1);*/
			bus.mail(new Letter("No"), BusIfc.CURRENT);
		}
		else
		{
			bus.mail(new Letter("Continue"), BusIfc.CURRENT);
		}
			
		
	}

}
