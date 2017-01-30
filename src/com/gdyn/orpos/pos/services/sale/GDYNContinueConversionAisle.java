package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosLaneActionAdapter;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;

public class GDYNContinueConversionAisle  extends PosLaneActionAdapter
{
	
	  /**
	 * 
	 */
	private static final long serialVersionUID = -385585746690941842L;

	public void traverse(BusIfc bus)
	    {
			GDYNSaleCargo saleCargo = (GDYNSaleCargo)bus.getCargo();
	        
	        if(saleCargo.isCouponRedemptionFlow())
	        {
	        	// go back to coupon redemptionflow
	        	saleCargo.setCouponRedemptionFlow(false);
	        	Letter letter = new Letter("Check");
	        	bus.mail(letter, BusIfc.CURRENT);
	        }
	        else
	        {
	        	Letter letter = new Letter(CommonLetterIfc.CONTINUE);
	        	bus.mail(letter, BusIfc.CURRENT);
	        }
	    }

}
