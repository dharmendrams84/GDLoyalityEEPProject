//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.sale;

// foundation imports
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.sale.ItemAddedRoad;
import oracle.retail.stores.pos.services.sale.SaleCargoIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNEmployeeDiscountUtility;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNLoyalityDiscountUtility;
// Begin GD-384: Only applies employee discount to items posted to sell item screen 
// at time employee discount applied - not to items added afterwards
// lcatania (Starmount) Apr 26, 2013

/**
 * Extended to calculate employee discount for items added after the discount
 * was already applied
 * @author lcatania
 *
 */
public class GDYNItemAddedRoad extends ItemAddedRoad
{
    /**
     * Serial Version ID
     */
    private static final long serialVersionUID = 3662219643981679909L;

    /**
       Journals the added line item information and makes the call to
       display the item info on the pole display device.

       @param  bus  Service Bus
    **/
    
   
    public void traverse(BusIfc bus)
 {
		GDYNEmployeeDiscountUtility.calculateDiscountItemAdded(bus);
         super.traverse(bus);
     	SaleCargoIfc cargo= (SaleCargoIfc)bus.getCargo();
     	
     	 
     	//By Monica for Discount 
     	//POS-334 changes done by Dharmnedra to check number of coupon items before applying loyality discount
		if (cargo.getTransaction() != null
				&& !(cargo.getTransaction().getEmployeeDiscountID() != null)) {

			List<PLUItemIfc> couponItemsList = GDYNLoyalityDiscountUtility
					.getCouponItemsList(bus);
			Boolean isItemLevelDiscount = GDYNLoyalityDiscountUtility
					.isDiscountScopeItem(couponItemsList);
			SaleReturnTransactionIfc transaction = cargo.getTransaction();

			if (couponItemsList == null || couponItemsList.size() == 0) {

				logger.info("Noloyality coupon items in the transaction");
				GDYNLoyalityConstants.isLoyalityCpnExists = Boolean.FALSE;
				GDYNLoyalityConstants.minThreshHoldAmt = BigDecimal.ZERO;
			} else if ((couponItemsList.size() == 1) && isItemLevelDiscount) {

				GDYNLoyalityDiscountUtility.applyItemLoyalityDiscount(bus,
						couponItemsList);
			} else if ((couponItemsList.size() == 1)
					&& (couponItemsList.get(0).getLoyalityCpnHrchyDtlsList() == null || couponItemsList
							.get(0).getLoyalityCpnHrchyDtlsList().size() == 0)) {
				GDYNLoyalityDiscountUtility.applyLoyalityTransactionDiscount(
						bus, couponItemsList);

			} else if (couponItemsList.size() == 1
					&& (couponItemsList.get(0).getLoyalityCpnHrchyDtlsList() != null || couponItemsList
							.get(0).getLoyalityCpnHrchyDtlsList().size() > 0)) {

				GDYNLoyalityDiscountUtility.applyLoyalityDiscount(bus,
						couponItemsList);
			}
		//POS-334 End changes done by Dharmnedra to check number of coupon items before applying loyality discount
		
	}
    
 }
    
}

// End GD-384: Only applies employee discount to items posted to sell item screen...