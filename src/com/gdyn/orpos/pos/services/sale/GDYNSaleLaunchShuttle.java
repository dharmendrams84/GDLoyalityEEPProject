/* ===========================================================================
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/sale/SaleLaunchShuttle.java /rgbustores_13.4x_generic_branch/1 2011/05/05 16:17:10 mszekely Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    jswan     06/30/10 - Checkin for first promotion of External Order
 *                         integration.
 *    cgreene   05/26/10 - convert to oracle packaging
 *    abondala  01/03/10 - update header date
 *
 * ===========================================================================
 * $Log:
 *    4    360Commerce 1.3         2/10/2006 11:06:44 AM  Deepanshu       CR
 *         6092: Sales Assoc sould be last 4 digits of Sales Assoc ID and not
 *         of Cashier ID on the recipt
 *    3    360Commerce 1.2         3/31/2005 4:29:48 PM   Robert Pearse   
 *    2    360Commerce 1.1         3/10/2005 10:24:58 AM  Robert Pearse   
 *    1    360Commerce 1.0         2/11/2005 12:14:00 PM  Robert Pearse   
 *
 *   Revision 1.11  2004/07/27 22:29:28  jdeleau
 *   @scr 6485 Make sure the undo button on the sell item screen does
 *   not force the operator to re-enter the users zip of phone.
 *
 *   Revision 1.10  2004/07/23 22:17:26  epd
 *   @scr 5963 (ServicesImpact) Major update.  Lots of changes to fix RegisterADO singleton references and fix training mode
 *
 *   Revision 1.9  2004/07/14 15:40:19  jdeleau
 *   @scr 5025 Persist the item selection on the sale screen across services, such that
 *   when it returns to the sale screen the same items are selected, if possible.
 *
 *   Revision 1.8  2004/03/22 17:26:43  blj
 *   @scr 3872 - added redeem security, receipt printing and saving redeem transactions.
 *
 *   Revision 1.7  2004/03/17 16:00:15  epd
 *   @scr 3561 Bug fixing and refactoring
 *
 *   Revision 1.6  2004/03/15 21:55:15  jdeleau
 *   @scr 4040 Automatic logoff after timeout
 *
 *   Revision 1.5  2004/03/15 17:19:44  epd
 *   @scr 3561 Fixed saving of original txn line items for returns
 *
 *   Revision 1.4  2004/03/11 20:03:21  blj
 *   @scr 3871 - added/updated shuttles to/from redeem, to/from tender, to/from completesale.
 *   also updated sites cargo for new redeem transaction.
 *
 *   Revision 1.3  2004/02/12 16:48:17  mcs
 *   Forcing head revision
 *
 *   Revision 1.2  2004/02/11 21:22:50  rhafernik
 *   @scr 0 Log4J conversion and code cleanup
 *
 *   Revision 1.1.1.1  2004/02/11 01:04:11  cschellenger
 *   updating to pvcs 360store-current
 *
 *
 * 
 *    Rev 1.4   Nov 26 2003 09:12:30   lzhao
 * remove tendering.
 * Resolution for 3371: Feature Enhancement:  Gift Card Enhancement
 * 
 *    Rev 1.3   Nov 07 2003 12:38:00   baa
 * use SaleCargoIfc
 * Resolution for 3430: Sale Service Refactoring
 * 
 *    Rev 1.2   Nov 06 2003 00:54:48   cdb
 * Updated to preserve "password required" field.
 * Resolution for 3430: Sale Service Refactoring
 * 
 *    Rev 1.1   Nov 04 2003 14:49:08   rsachdeva
 * SaleCargoIfc setTendering
 * Resolution for POS SCR-3430: Sale Service Refactoring
 * 
 *    Rev 1.0   Nov 03 2003 14:49:18   baa
 * Initial revision.
 * ===========================================================================
 */
package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.common.TimedCargoIfc;
import oracle.retail.stores.pos.services.sale.SaleLaunchShuttle;

import org.apache.log4j.Logger;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;

//--------------------------------------------------------------------------
/**
 * This shuttle copies information from the cargo used within the Sale service
 * and its subservices.
 * <p>
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
// --------------------------------------------------------------------------
public class GDYNSaleLaunchShuttle extends SaleLaunchShuttle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6258848378676703365L;

	/**
	 * The logger to which log messages will be sent.
	 **/
	protected static Logger logger = Logger
			.getLogger(oracle.retail.stores.pos.services.sale.SaleLaunchShuttle.class);

	/**
	 * revision number supplied by Team Connection
	 **/
	public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

	protected GDYNSaleCargoIfc saleCargo;

	// ----------------------------------------------------------------------
	/**
	 * Loads cargo from modifyitem service.
	 * <P>
	 * <B>Pre-Condition(s)</B>
	 * <UL>
	 * <LI>Cargo will contain the selected item
	 * </UL>
	 * <B>Post-Condition(s)</B>
	 * <UL>
	 * <LI>
	 * </UL>
	 * 
	 * @param bus
	 *            Service Bus
	 **/
	// ----------------------------------------------------------------------
	public void load(BusIfc bus) {
		super.load(bus);

		// retrieve cargo from the parent
		saleCargo = (GDYNSaleCargoIfc) bus.getCargo();

	}

	// ----------------------------------------------------------------------
	/**
	 * Loads data into alterations service.
	 * <P>
	 * <B>Pre-Condition(s)</B>
	 * <UL>
	 * <LI>Cargo will contain the selected item
	 * </UL>
	 * <B>Post-Condition(s)</B>
	 * <UL>
	 * <LI>
	 * </UL>
	 * 
	 * @param bus
	 *            Service Bus
	 **/
	// ----------------------------------------------------------------------
	public void unload(BusIfc bus) {
		// unload financial cargo
		super.unload(bus);

		// retrieve cargo from the child
		GDYNSaleCargoIfc cargo = (GDYNSaleCargoIfc) bus.getCargo();
		cargo.setAccessFunctionID(saleCargo.getAccessFunctionID());
		cargo.setPasswordRequired(saleCargo.isPasswordRequired());
		cargo.setCustomerInfo(saleCargo.getCustomerInfo());
		cargo.setEmployee(saleCargo.getEmployee());
		cargo.setLineItem(saleCargo.getLineItem());
		cargo.setOperator(saleCargo.getOperator());
		cargo.setPLUItem(saleCargo.getPLUItem());
		cargo.setIndices(saleCargo.getIndices());
		cargo.setSalesAssociate(saleCargo.getEmployee());
		cargo.setTransaction(saleCargo.getTransaction());
		cargo.setCanSkipCustomerPrompt(saleCargo.getCanSkipCustomerPrompt());

		SaleReturnTransactionIfc[] originalTxns = saleCargo
				.getOriginalReturnTransactions();
		if (originalTxns != null) {
			for (int i = 0; i < originalTxns.length; i++) {
				cargo.addOriginalReturnTransaction(originalTxns[i]);
			}
		}

		// Record the timeout status in the cargo, so called services will know
		if (cargo instanceof TimedCargoIfc
				&& saleCargo instanceof TimedCargoIfc) {
			((TimedCargoIfc) cargo).setTimeout(((TimedCargoIfc) saleCargo)
					.isTimeout());
		}

		if (saleCargo.getLoyaltyIdNumber() != null) {
			cargo.setLoyaltyIdNumber(saleCargo.getLoyaltyIdNumber());
		}
		if (saleCargo.getLoyaltyEmailId() != null) {
			cargo.setLoyaltyEmailId(saleCargo.getLoyaltyEmailId());
		}
	
		//code changes added by Dharmendra to set loyaltyPhoneNo in sale cargo 
		String loyaltyPhoneNo = saleCargo.getLoyaltyPhoneNo();
		if (GDYNLoyalityConstants.isEmpty(loyaltyPhoneNo)) {
			cargo.setLoyaltyPhoneNo("");
		} else {
			cargo.setLoyaltyPhoneNo(loyaltyPhoneNo);
		}
		
		saleCargo.setLoyaltyPhoneNo("");
	}

}
