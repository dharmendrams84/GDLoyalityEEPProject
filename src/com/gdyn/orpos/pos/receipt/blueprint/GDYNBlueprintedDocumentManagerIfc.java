//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.receipt.blueprint;

import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.pos.receipt.PrintableDocumentManagerIfc;
import oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc;

/**
 * This interface exposed the return receipt counts.
 * 
 * @author MSolis
 *
 */
public interface GDYNBlueprintedDocumentManagerIfc extends PrintableDocumentManagerIfc
{  
    public int getPrintReceiptReturnCount();
    
    public void setPrintReceiptReturnCount(int count);
    
    public void setRemainingSpend(SaleReturnTransactionIfc transaction, ReceiptParameterBeanIfc receiptParameter);
}
