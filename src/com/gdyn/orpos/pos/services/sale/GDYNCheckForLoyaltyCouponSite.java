package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;

import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;

public class GDYNCheckForLoyaltyCouponSite extends PosSiteActionAdapter 
{
	
	
	// ----------------------------------------------------------------------
		/**
		 * Check if Loyalty Coupon is entered.
		 * <P>
		 * 
		 * @param bus
		 *            Service Bus
		 **/
		// ----------------------------------------------------------------------
	private static final long serialVersionUID = 6305663601357299445L;

	@Override
	public void arrive(BusIfc bus) 
	{
		
		String letter = "Continue";
		
		ParameterManagerIfc pm = (ParameterManagerIfc) bus
				.getManager(ParameterManagerIfc.TYPE);
		
		GDYNSaleCargoIfc saleCargo = (GDYNSaleCargoIfc) bus.getCargo();
		
		// check if the entered item is a coupon
		
		PLUItemIfc pluItem =  saleCargo.getPLUItem();
		
		boolean customerLoyaltyEnable = false;
		
		try 
		{
			customerLoyaltyEnable = pm
					.getBooleanValue(GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable);
		} 
		catch (ParameterException e)
		{
			e.printStackTrace();
			// manually set some default
			customerLoyaltyEnable = false;
		}
		
		// if loyalty flag is enable then only proceed.
		
		if(customerLoyaltyEnable)
		{

		
			if(pluItem.getItemClassification().getItemType() == 3)
			{
				//item is coupon
				
				// get webserviceEnabledFlag = false
				
				
				// check if the webserviceEnabledFlag is enabled
				
				//Added below null condition by Monica
				if(pluItem.getLoyalityCpnAttrDtls()!=null &&   pluItem.getLoyalityCpnAttrDtls()!=null
						&& !Util.isEmpty(pluItem.getLoyalityCpnAttrDtls().getValidityFlag())
						&& pluItem.getLoyalityCpnAttrDtls().getValidityFlag().equalsIgnoreCase("Y"))
				{
					
					// check for coupon type codes.
					//presently hard coding this as 1
					
					if(pluItem.getLoyalityCpnAttrDtls()!=null && pluItem.getLoyalityCpnAttrDtls().getLoylCpnType().equalsIgnoreCase("BADGE")
							||pluItem.getLoyalityCpnAttrDtls().getLoylCpnType().equalsIgnoreCase("TARGET")
							||pluItem.getLoyalityCpnAttrDtls().getLoylCpnType().equalsIgnoreCase("LOYLTY"))
					{
						
						letter = "Continue";
					}
						
					else
					{
						//normal coupon. take base flow.
						
						letter = "Invalid";
					}
						
				}
				else
				{	
					letter = "Invalid";
				}
					
					
			}
	
			else
			{
				
				// if item is not coupon, then we can check if loyalty coupon has already been added to the transaction
				
				if(saleCargo.isLoyaltyCouponPresntinTrxn())
				{
					letter = "Add";
				}
				else
				{
					letter = "Invalid";
				}
			}
		}
		else
		{
			letter = "Invalid";
		}
		
		bus.mail(new Letter(letter), BusIfc.CURRENT);
		
	}

}
