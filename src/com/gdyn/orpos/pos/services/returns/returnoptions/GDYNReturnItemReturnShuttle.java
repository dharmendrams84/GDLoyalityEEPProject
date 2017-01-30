/* ===========================================================================
* Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/returns/returnoptions/ReturnItemReturnShuttle.java /rgbustores_13.4x_generic_branch/3 2011/08/18 08:44:04 jswan Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    jswan     08/17/11 - Modified to prevent the return of Gift Cards as
 *                         items and part of a transaction. Also cleaned up
 *                         references to gift cards objects in the return
 *                         tours.
 *    cgreene   07/26/11 - removed tenderauth and giftcard.activation tours and
 *                         financialnetwork interfaces.
 *    rrkohli   10/01/10 - FIX TO PREVENT POS FROM CRASHING ON RETURN WITH NO
 *                         RECEIPT TRANSACTION
 *    jswan     06/30/10 - Checkin for first promotion of External Order
 *                         integration.
 *    jswan     06/17/10 - Checkin external order integration files for
 *                         refresh.
 *    jswan     06/01/10 - Checked in for refresh to latest lable.
 *    jswan     05/14/10 - ExternalOrder mods checkin for refresh to tip.
 *    cgreene   05/26/10 - convert to oracle packaging
 *    abondala  01/03/10 - update header date
 *
 * ===========================================================================
 * $Log:
 *    3    360Commerce 1.2         3/31/2005 4:29:45 PM   Robert Pearse   
 *    2    360Commerce 1.1         3/10/2005 10:24:51 AM  Robert Pearse   
 *    1    360Commerce 1.0         2/11/2005 12:13:53 PM  Robert Pearse   
 *
 *   Revision 1.8  2004/07/27 19:45:40  jdeleau
 *   @scr 6305 Flow corrections on return without a receipt for linked customer.
 *
 *   Revision 1.7  2004/03/26 05:39:05  baa
 *   @scr 3561 Returns - modify flow to support entering price code for not found gift receipt
 *
 *   Revision 1.6  2004/03/22 22:39:47  epd
 *   @scr 3561 Refactored cargo to get rid of itemQuantities attribute.  Added it to ReturnItemIfc instead.  Refactored to reduce code complexity and confusion.
 *
 *   Revision 1.5  2004/03/10 14:16:46  baa
 *   @scr 0 fix javadoc warnings
 *
 *   Revision 1.4  2004/02/16 13:36:40  baa
 *   @scr  3561 returns enhancements
 *
 *   Revision 1.3  2004/02/12 16:51:52  mcs
 *   Forcing head revision
 *
 *   Revision 1.2  2004/02/11 21:52:25  rhafernik
 *   @scr 0 Log4J conversion and code cleanup
 *
 *   Revision 1.1.1.1  2004/02/11 01:04:20  cschellenger
 *   updating to pvcs 360store-current
 *
 *
 * 
 *    Rev 1.2   05 Feb 2004 23:27:26   baa
 * return multiple items
 * 
 *    Rev 1.1   08 Nov 2003 01:42:58   baa
 * cleanup -sale refactoring
 * 
 *    Rev 1.0   Aug 29 2003 16:06:20   CSchellenger
 * Initial revision.
 * 
 *    Rev 1.0   Apr 29 2002 15:05:12   msg
 * Initial revision.
 * 
 *    Rev 1.0   Mar 18 2002 11:46:30   msg
 * Initial revision.
 * 
 *    Rev 1.4   Feb 25 2002 14:55:34   blj
 * Fixed esc so that it goes to the right screen.
 * Resolution for POS SCR-923: Started return by gift receipt.  Esc to do the return by item-got gift rec item screen
 * 
 *    Rev 1.3   Feb 05 2002 16:43:22   mpm
 * Modified to use IBM BigDecimal.
 * Resolution for POS SCR-1121: Employ IBM BigDecimal
 * 
 *    Rev 1.2   17 Jan 2002 17:37:34   baa
 * update roles/security model
 * Resolution for POS SCR-714: Roles/Security 5.0 Updates
 *
 *    Rev 1.1   20 Nov 2001 09:17:08   pjf
 * Changes to support manual return of kit components when kit header item number entered.
 * Resolution for POS SCR-8: Item Kits
 *
 *    Rev 1.0   Sep 21 2001 11:25:20   msg
 * Initial revision.
 *
 *    Rev 1.1   Sep 17 2001 13:12:48   msg
 * header update
 * ===========================================================================
 */
package com.gdyn.orpos.pos.services.returns.returnoptions;

import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.returns.returncommon.ReturnData;
import oracle.retail.stores.pos.services.returns.returnfindtrans.ReturnTransactionReturnShuttle;

import org.apache.log4j.Logger;

import com.gdyn.orpos.pos.services.returns.returnitem.GDYNReturnItemCargo;

/**
 * This shuttle updates the Return Options service with the information from the
 * Return Item service.
 */
public class GDYNReturnItemReturnShuttle extends ReturnTransactionReturnShuttle
{
    /** serialVersionUID */
    private static final long serialVersionUID = 8048706880870715269L;

    /**
     * The logger to which log messages will be sent.
     */
    protected static final Logger logger = Logger.getLogger(GDYNReturnItemReturnShuttle.class);

    /**
     * return cargo
     */
    protected GDYNReturnItemCargo returnItemCargo;

    /**
     * Copies information needed from child service.
     * 
     * @param bus Child Service Bus to copy cargo from.
     */
    @Override
    public void load(BusIfc bus)
    {
        // retrieve cargo from the child(ItemReturn Cargo)
        returnItemCargo = (GDYNReturnItemCargo)bus.getCargo();
    }

    /**
     * Stores information needed by parent service.
     * 
     * @param bus Parent Service Bus to copy cargo to.
     */
    @Override
    public void unload(BusIfc bus)
    {
        GDYNReturnOptionsCargo cargo = (GDYNReturnOptionsCargo)bus.getCargo();

        if (returnItemCargo.getTransferCargo())
        {
            cargo.setTransferCargo(true);
            ReturnData rd = new ReturnData();

            PLUItemIfc[] pluItems = returnItemCargo.getPLUItems();
            rd.setReturnItems(returnItemCargo.getReturnItems());
            rd.setPLUItems(pluItems);
            rd.setSaleReturnLineItems(returnItemCargo.getReturnSaleLineItems());
            cargo.setReturnData(rd);
            cargo.setOriginalTransaction(returnItemCargo.getOriginalTransaction());
            cargo.setOriginalTransactionId(returnItemCargo.getOriginalTransactionId());
            cargo.setHaveReceipt(returnItemCargo.haveReceipt());
        }

        // added by Vivek
        cargo.setLoyaltyEnable(returnItemCargo.isLoyaltyEnable());
        cargo.setOriginalLoyaltyID(returnItemCargo.getOriginalLoyaltyID());
        // Update gift receipt flag
        cargo.setGiftReceiptSelected(returnItemCargo.isGiftReceiptSelected());
        cargo.setTriedLinkingCustomer(false);
        cargo.resetExternalOrderItemsSelectForReturn();
    }

}