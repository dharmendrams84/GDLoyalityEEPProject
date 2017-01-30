package com.gdyn.orpos.pos.services.sale;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransaction;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNLoyalityDiscountUtility;

import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;

public class GDYNAddCouponToTransactionSite extends PosSiteActionAdapter 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5874115021183550621L;

	@Override
	public void arrive(BusIfc bus) 
	{
				
		// add coupon to the transaction 
		
		GDYNSaleCargo saleCargo = (GDYNSaleCargo) bus.getCargo();
		
		/*if (saleCargo.getTransaction()!=null && saleCargo.getTransaction() instanceof GDYNSaleReturnTransaction)
		{
			
			// coupon already exist in transaction.
			// further procesing can be done here once coupon are developed
			List<PLUItemIfc> couponItemsList = GDYNLoyalityDiscountUtility
					.getCouponItemsList(bus);
			
			if (couponItemsList == null || couponItemsList.size() == 0) {
				GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
				SaleReturnTransactionIfc transaction = cargo.getTransaction();

				Vector lineItemsvector = transaction.getItemContainerProxy()
						.getLineItemsVector();
				Vector<SaleReturnLineItemIfc> lineItemsvector1 = transaction.getItemContainerProxy()
						.getLineItemsVector();
				lineItemsvector1.get(0).getPLUItem().getAdvancedPricingRules();
				GDYNLoyalityDiscountUtility
						.clearAllLoyalityDiscount(lineItemsvector);
				GDYNLoyalityConstants.isLoyalityCpnExists = Boolean.FALSE;
				GDYNLoyalityConstants.minThreshHoldAmt = BigDecimal.ZERO;
			} else if (couponItemsList != null
					&& couponItemsList.size() != 0
					&& (couponItemsList.get(0).getLoyalityCpnHrchyDtlsList() == null || couponItemsList
							.get(0).getLoyalityCpnHrchyDtlsList().size() == 0)) {
				GDYNLoyalityDiscountUtility.applyLoyalityTransactionDiscount(bus,
						couponItemsList);
				
			} else if (couponItemsList != null && couponItemsList.size() != 0) {
				GDYNLoyalityDiscountUtility.applyLoyalityDiscount(bus,
						couponItemsList);
			}
			
			System.out.println("GDYNLoyalityConstants.isLoyalityCpnExists :-  "+GDYNLoyalityConstants.isLoyalityCpnExists + " : "+GDYNLoyalityConstants.minThreshHoldAmt);

		}*/
		
		//calculate best deal
		
		bus.mail(new Letter("Continue"), BusIfc.CURRENT);
	}

}
