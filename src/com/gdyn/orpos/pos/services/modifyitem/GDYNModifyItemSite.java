//------------------------------------------------------------------------------
//
// Copyright (c) 2012, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.modifyitem;

import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.pos.services.modifyitem.ModifyItemSite;
import oracle.retail.stores.pos.ui.beans.NavigationButtonBeanModel;

//------------------------------------------------------------------------------
/**
 * Extends the base ModifyItemSite so that the tax button can always be 
 * disabled, regardless of the enable/disable logic in the parent class.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNModifyItemSite extends ModifyItemSite
{
    /** serial UID */
    private static final long serialVersionUID = -7430128542743206576L;

    //--------------------------------------------------------------------------
    /**
     * Overridden to always disable tax button for items.
     */
    @Override
    protected NavigationButtonBeanModel getNavigationButtonBeanModel(
                                            SaleReturnLineItemIfc[] lineItems, 
                                            RetailTransactionIfc transaction,
                                            int transType, int taxMode)
    {
        NavigationButtonBeanModel buttonModel =
            super.getNavigationButtonBeanModel(
                lineItems, transaction, transType, taxMode);
        
        
        buttonModel.setButtonEnabled(ACTION_TAX, false);
        
        /* Code changes added by Dharmendra to fix the POS-193 issue on 11-Aug-2016*/
		if (transaction != null) {
			SaleReturnTransactionIfc saleReturnTransactionIfc = (SaleReturnTransactionIfc) transaction;
			String discountEmployeeId = saleReturnTransactionIfc
					.getEmployeeDiscountID();
			
			if (discountEmployeeId != null
					&& !"".equalsIgnoreCase(discountEmployeeId)) {
				buttonModel.setButtonEnabled(ACTION_GIFT_RECEIPT, false);
			}
		}
        return buttonModel;
    }

}
