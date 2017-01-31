/* ===========================================================================
* Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/tender/TenderingInfoUISite.java /rgbustores_13.4x_generic_branch/8 2011/06/30 09:59:24 jswan Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    jswan     06/29/11 - Modified to use ADO transaction object.
 *    icole     06/17/11 - Remove redunant non working CPOI tenders code
 *    icole     06/16/11 - Removed commented out lines
 *    icole     06/16/11 - Changes for CurrencyIfc, Sardine refresh items list,
 *                         other simulted changes.
 *    cgreene   06/15/11 - implement gift card for servebase and training mode
 *    blarsen   06/15/11 - Adding storeID to customer interaction request.
 *    icole     06/14/11 - Restore CurrencyIfc
 *    blarsen   06/14/11 - Adding storeID to scrolling receipt request.
 *    icole     06/09/11 - CPOI code changes
 *    cgreene   06/07/11 - update to first pass of removing pospal project
 *    icole     04/28/11 - Payment updates
 *    cgreene   02/15/11 - move constants into interfaces and refactor
 *    acadar    06/10/10 - use default locale for currency display
 *    acadar    06/09/10 - XbranchMerge acadar_tech30 from
 *                         st_rgbustores_techissueseatel_generic_branch
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    acadar    04/09/10 - optimize calls to LocaleMAp
 *    acadar    04/05/10 - use default locale for currency and date/time
 *                         display
 *    abondala  01/03/10 - update header date
 *    asinton   04/03/09 - Localizing the amounts that appear in the CPOI
 *                         device.
 *    asinton   02/26/09 - changes per review comments from Christian Greene.
 *    asinton   02/24/09 - Added utility manager.
 *    asinton   02/24/09 - Honor thy customer's preferred language.
 *
 * ===========================================================================
 * $Log:
 *   3    360Commerce 1.2         3/31/2005 4:30:25 PM   Robert Pearse
 *   2    360Commerce 1.1         3/10/2005 10:25:59 AM  Robert Pearse
 *   1    360Commerce 1.0         2/11/2005 12:14:54 PM  Robert Pearse
 *
 *  Revision 1.12  2004/09/17 23:00:01  rzurga
 *  @scr 7218 Move CPOI screen name constants to CIDAction to make it more generic
 *
 *  Revision 1.11  2004/07/28 22:56:37  cdb
 *  @scr 6179 Externalized some CIDScreen values.
 *
 *  Revision 1.10  2004/07/24 16:43:52  rzurga
 *  @scr 6463 Items are showing on CPOI sell item from previous transaction
 *  Remove newly introduced automatic hiding of non-active CPOI screens
 *  Enable clearing of non-visible CPOI screens
 *  Improve appearance by clearing first, then setting fields and finally showing the CPOI screen
 *
 *  Revision 1.9  2004/07/22 23:33:07  dcobb
 *  @scr 3676 Add tender display to ingenico.
 *
 *  Revision 1.8  2004/07/22 22:38:41  bwf
 *  @scr 3676 Add tender display to ingenico.
 *
 *  Revision 1.6  2004/07/12 21:49:06  bwf
 *  @scr 6170 The check tender was being remove twice.  Removed the
 *                    unnecessary one.
 *
 *  Revision 1.5  2004/04/05 15:47:54  jdeleau
 *  @scr 4090 Code review comments incorporated into the codebase
 *
 *  Revision 1.4  2004/03/25 20:25:15  jdeleau
 *  @scr 4090 Deleted items appearing on Ingenico, I18N, perf improvements.
 *  See the scr for more info.
 *
 *  Revision 1.3  2004/02/12 16:48:22  mcs
 *  Forcing head revision
 *
 *  Revision 1.2  2004/02/11 21:22:51  rhafernik
 *  @scr 0 Log4J conversion and code cleanup
 *
 *  Revision 1.1.1.1  2004/02/11 01:04:12  cschellenger
 *  updating to pvcs 360store-current
 *
 *
 *
 *    Rev 1.1   Nov 19 2003 14:15:36   epd
 * updated to use TDO factory
 *
 *    Rev 1.0   Nov 04 2003 11:17:54   epd
 * Initial revision.
 *
 *    Rev 1.0   Oct 23 2003 17:29:52   epd
 * Initial revision.
 *
 *    Rev 1.0   Oct 17 2003 13:06:48   epd
 * Initial revision.
 *
 * ===========================================================================
 */
package oracle.retail.stores.pos.services.tender;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.WorkstationIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransaction;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.foundation.manager.device.DeviceException;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.service.SessionBusIfc;
import oracle.retail.stores.pos.device.POSDeviceActions;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CPOIPaymentUtility;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.tdo.TDOException;
import oracle.retail.stores.pos.tdo.TDOFactory;
import oracle.retail.stores.pos.tdo.TDOUIIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransaction;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;

/**
 * User has selected Tender
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/8 $
 */
public class TenderingInfoUISite extends PosSiteActionAdapter
{
    private static final long serialVersionUID = -2867106822797919316L;

    /**
     * revision number supplied by source-code-control system
     */
    public static String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/8 $";

    /**
     * Determine transaction type - Display appropriate UI - If the balance is
     * paid, proceed
     * 
     * @param bus the bus arriving at this site
     */
    @SuppressWarnings("unchecked")
	@Override
    public void arrive(BusIfc bus)
    {
        displayPoleDisplayInfo(bus);
        TenderCargo cargo = (TenderCargo)bus.getCargo();
        CPOIPaymentUtility cpoiPaymentUtility = CPOIPaymentUtility.getInstance();
		
	
		
		 GDYNSaleReturnTransaction transaction = (GDYNSaleReturnTransaction)cargo.getTransaction();
		
		
			
		/*  code changes added by Dharmendra to fix issue POS-203 on 17/08/2016*/
		String discountEmployeeId = "";
		
		int periodId = 0;
		int emplGrpId = 0;		
		String emplIdSrc = "";
		
		Boolean isReturnTransaction = Boolean.FALSE;
		
		/*code changes  added by Dharmendra on 22/08/2016 to fix issue POS-209*/
		Map<String, Integer> entitlementMap = new HashMap<String, Integer>();

		if (cargo.getOriginalReturnTransactions() != null
				&& cargo.getOriginalReturnTransactions().length != 0) {
			discountEmployeeId = cargo.getOriginalReturnTransactions()[0]
					.getEmployeeDiscountID();
			
			isReturnTransaction = Boolean.TRUE;
			String transactionDiscountEmplId =  transaction.getEmployeeDiscountID();
			logger.debug(" discountEmployeeId of return transaction " + transactionDiscountEmplId);
			if(Util.isEmpty(transactionDiscountEmplId)){
				logger.debug("setting discount employee id of return transaction as it is empty");
				transaction.setEmployeeDiscountID(transactionDiscountEmplId);
			}
			SaleReturnTransaction originalTransaction = (SaleReturnTransaction) cargo
					.getOriginalReturnTransactions()[0];
			
			logger.debug("employeeDiscountName from original transaction "
							+ " discountEmployeeId "
							+ discountEmployeeId
							+ " originalTransaction.getTransactionSequenceNumber "
							+ originalTransaction
									.getTransactionSequenceNumber());
			transaction.setEmployeeDiscountID(discountEmployeeId);
			
			if (originalTransaction.getLineItemsVector() != null
					&& originalTransaction.getLineItemsVector().size() != 0) {
				logger.debug("Line items exists in return transaction");
				for (int i = 0; i < originalTransaction.getLineItemsVector()
						.size(); i++) {
					SaleReturnLineItem saleReturnLineItem = ((SaleReturnLineItem) originalTransaction
							.getLineItemsVector().get(i));
					if (saleReturnLineItem != null) {
						
						
						if (saleReturnLineItem.getPLUItem() != null) {
							
							PLUItemIfc pluItem = saleReturnLineItem
									.getPLUItem();
							logger.debug("pluItem " + pluItem.getPeriodId()
									+ " pluItem.getEmplGrpId() "
									+ pluItem.getEmplGrpId()
									+ " pluItem.getEmplIdSrc() "
									+ pluItem.getEmplIdSrc()
									+ "  pluItem.getEntitlementId() "
									+ pluItem.getEntitlementId());
							periodId = pluItem.getPeriodId();							
							emplGrpId = pluItem.getEmplGrpId();
							emplIdSrc = pluItem.getEmplIdSrc();
							entitlementMap.put(pluItem.getItemID(), pluItem.getEntitlementId());

						}

					}
				}
			}

		}
		
		if(isReturnTransaction&& transaction.getItemContainerProxy().getLineItemsVector()!=null && transaction.getItemContainerProxy().getLineItemsVector().size()!=0){
			Vector<SaleReturnLineItem> vector =  transaction.getItemContainerProxy().getLineItemsVector();
			Iterator<SaleReturnLineItem> iterator = vector.iterator();
			while(iterator.hasNext()){
				
			SaleReturnLineItem srli =	(SaleReturnLineItem)iterator.next();
			Integer itemEntitlementId= entitlementMap.get(srli.getPLUItemID());
			if(itemEntitlementId==null){
				itemEntitlementId =0 ;
			}
			srli.getPLUItem().setPeriodId(periodId);
			srli.getPLUItem().setEntitlementId(itemEntitlementId);
			srli.getPLUItem().setEmplGrpId(emplGrpId);
			srli.getPLUItem().setEmplIdSrc(emplIdSrc);
			logger.debug("After setting periodId " + periodId + " emplGrpId " + emplGrpId
					+ " itemEntitlementId " + itemEntitlementId + " emplIdSrc "
				+ emplIdSrc);
			}
		}
		
		
        WorkstationIfc workstation = cargo.getRegister().getWorkstation();
        cpoiPaymentUtility.beginScrollingReceipt(workstation, false);
        cpoiPaymentUtility.addTenders(workstation, (TenderableTransactionIfc)cargo.getCurrentTransactionADO().toLegacy());
       
       
    	Vector<SaleReturnLineItem> lineItemsvector = transaction.getItemContainerProxy()
				.getLineItemsVector();
    	Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsvector
				.iterator();
    	
       
    	// code changes to fix POS #373 issue.
    	
	    Boolean loylCpnExists= Boolean.FALSE;
    	
    	// End code changes to fix POS #373 issue.
    	
    	BigDecimal minThreshHoldAmt =  GDYNLoyalityConstants.minThreshHoldAmt;
    	if(minThreshHoldAmt==null){
    		minThreshHoldAmt = BigDecimal.ZERO;
    	}
    	Boolean isloyailityElligibleItemExist = Boolean.FALSE;
    	Boolean loylCpnMrchyDtlsExists = Boolean.FALSE;
    	
    	lineItemsVectorIterator = lineItemsvector
				.iterator();
		
		String couponItemDesc = "";
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItemIfc srli = lineItemsVectorIterator.next();
			
			
			
				
			
			if(srli.getPLUItem().isStoreCoupon()&&srli.getPLUItem().getLoyalityCpnAttrDtls()!=null){
		    	loylCpnExists =Boolean.TRUE;
				couponItemDesc = srli.getPLUItem().getManufacturerItemUPC();
				if(srli.getPLUItem().getLoyalityCpnHrchyDtlsList() != null
						&& srli.getPLUItem().getLoyalityCpnHrchyDtlsList().size() != 0){
					loylCpnMrchyDtlsExists = Boolean.TRUE;
					break;
				}
			}
		}
    	
		logger.info("coupon item has merchandise details "+loylCpnMrchyDtlsExists + "  minimum thresh hold amount "+minThreshHoldAmt);
		
    	lineItemsVectorIterator = lineItemsvector
				.iterator();
    	
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItemIfc srli = lineItemsVectorIterator.next();
			if (srli.getPLUItem().getIsLoylDiscountElligible()) {
				isloyailityElligibleItemExist = Boolean.TRUE;
				break;
			}
		}
		
		logger.info("Transaction contains elligible items "+isloyailityElligibleItemExist);
		logger.info("transaction subtotal "+transaction.getTransactionTotals().getSubtotal()
						.getDecimalValue());
		lineItemsvector = transaction.getItemContainerProxy().getLineItemsVector();
		String cpnItemApplyTo = "";
		for(SaleReturnLineItem srli : lineItemsvector){
			if(srli.getPLUItem().isStoreCoupon()){
				cpnItemApplyTo = srli.getPLUItem().getLoyalityCpnAttrDtls().getItmApplyTo();
				logger.info("cpnItemApplyTo "+cpnItemApplyTo);
			}
		}
		
		
		for(SaleReturnLineItem srli : lineItemsvector){
			if(!srli.getPLUItem().isStoreCoupon()){
				srli.getPLUItem().setIsLoylDiscountElligible(true);
				PLUItemIfc pluItemIfc = srli.getPLUItem();
				Boolean isLoyaityDiscountElligible =false;
				if (cpnItemApplyTo == null
						|| GDYNLoyalityConstants.blankString
								.equalsIgnoreCase(cpnItemApplyTo)
						|| GDYNLoyalityConstants.itemApplyToB
								.equalsIgnoreCase(cpnItemApplyTo)) {
					isLoyaityDiscountElligible = Boolean.TRUE;
				} else if (GDYNLoyalityConstants.itemApplyToC
						.equalsIgnoreCase(cpnItemApplyTo)
						&& (!pluItemIfc.getItemClassification()
								.getEmployeeDiscountAllowedFlag())) {
					isLoyaityDiscountElligible = Boolean.TRUE;

				} else if (GDYNLoyalityConstants.itemApplyToR
						.equalsIgnoreCase(cpnItemApplyTo)
						&& (pluItemIfc.getItemClassification()
								.getEmployeeDiscountAllowedFlag())) {
					isLoyaityDiscountElligible = Boolean.TRUE;
				}
				srli.getPLUItem().setIsLoylDiscountElligible(isLoyaityDiscountElligible);

				}
		}
		
	if(loylCpnExists
					&& transaction.getTransactionTotals().getSubtotal()
							.getDecimalValue().compareTo(minThreshHoldAmt) < 0){
			DialogBeanModel dialogModel = new DialogBeanModel();
			dialogModel.setResourceID("MinThreshHoldNotMet");
			dialogModel.setType(DialogScreensIfc.ERROR);

			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Undo");
			// display dialog
			POSUIManagerIfc ui = (POSUIManagerIfc) bus
					.getManager(UIManagerIfc.TYPE);
			ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);

		}else if (loylCpnMrchyDtlsExists&&loylCpnExists
					&& transaction.getTransactionTotals().getSubtotal()
							.getDecimalValue().compareTo(minThreshHoldAmt) < 0) {
				
				DialogBeanModel dialogModel = new DialogBeanModel();
				dialogModel.setResourceID("MinThreshHoldNotMet");
				dialogModel.setType(DialogScreensIfc.ERROR);

				dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Undo");
				// display dialog
				POSUIManagerIfc ui = (POSUIManagerIfc) bus
						.getManager(UIManagerIfc.TYPE);
				ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
			} else if ((loylCpnMrchyDtlsExists&&loylCpnExists && !isloyailityElligibleItemExist)
					||(loylCpnExists&& transaction.getTransactionTotals().getDiscountTotal().getDecimalValue().compareTo(BigDecimal.ZERO)==0)) {
				DialogBeanModel dialogModel = new DialogBeanModel();
				dialogModel.setResourceID("LoyaltyNoElligibleItems");
				dialogModel.setType(DialogScreensIfc.ERROR);
				//String args[]=new String[]{couponItemId};
				if(couponItemDesc==null||couponItemDesc.length()==0){
				     couponItemDesc = "";
				    }
				String args[]=new String[]{couponItemDesc};
				dialogModel.setArgs(args);
				dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK, "Undo");
				// display dialog
				POSUIManagerIfc ui = (POSUIManagerIfc) bus
						.getManager(UIManagerIfc.TYPE);
				ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);

			} else {
				
				if (transaction.getEmployeeDiscountID() != null) 
				{
					bus.mail(new Letter("EmployeeDiscCheck"), BusIfc.CURRENT);
				}
				else
				{
				bus.mail(CommonLetterIfc.NEXT, BusIfc.CURRENT);
				}
			}
		
    	
    }

 
    /**
     * Displays transaction info on the pole device
     * @param bus
     */
    protected void displayPoleDisplayInfo(BusIfc bus)
    {
        try
        {
            POSDeviceActions pda = new POSDeviceActions((SessionBusIfc) bus);
            pda.clearText();

            TenderCargo cargo = (TenderCargo)bus.getCargo();

            // build bean model helper
            TDOUIIfc tdo = null;
            try
            {
                tdo = (TDOUIIfc)TDOFactory.create("tdo.tender.TenderLineDisplay");
            }
            catch (TDOException tdoe)
            {
                logger.error(tdoe);
            }

            pda.displayTextAt(0, 0, tdo.formatPoleDisplayLine1(cargo.getCurrentTransactionADO()));
            pda.displayTextAt(1, 0, tdo.formatPoleDisplayLine2(cargo.getCurrentTransactionADO()));
        }
        catch (DeviceException e)
        {
            logger.error(e);
        }

    }
}
