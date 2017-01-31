/* ===========================================================================
* Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/sale/AddItemSite.java /rgbustores_13.4x_generic_branch/7 2012/02/21 15:42:10 asinton Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    asinton   02/21/12 - To set cargo.setRefreshNeeded(true), added check for
 *                         advanced pricing rules as they may add discounts to
 *                         items already entered.
 *    cgreene   08/10/11 - quickwin - implement dialog for trying to enter
 *                         multiple qty of serialized item
 *    blarsen   06/14/11 - Adding storeID to scrolling receipt request.
 *    cgreene   06/07/11 - update to first pass of removing pospal project
 *    sgu       06/02/10 - refactor AddItemSite to move business logic to
 *                         TransactionUtility
 *    sgu       06/01/10 - check in after merge
 *    sgu       06/01/10 - check in order sell item flow
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   01/22/10 - remove check for LAYAWAY_INITIATE so that items can
 *                         be added to layaways again
 *    sbeesnal  01/20/10 - set the refresh flag to true so that line items is
 *                         refreshed in the CPOI display.
 *    sbeesnal  01/20/10 - set the refresh flag to true so that line items is
 *                         refreshed in
 *    abondala  01/03/10 - update header date
 *    nkgautam  11/26/09 - Serialisation Code changes
 *    aphulamb  11/17/08 - Pickup Delivery order
 *
 * ===========================================================================
 * $Log:
 *    4    360Commerce 1.3         5/2/2008 11:27:45 AM   Jack G. Swan    In
 *         the case of giftcard issue and reload, set the current price on the
 *          permanent price.  Code reviewed by Tony Zgarga.
 *    3    360Commerce 1.2         3/31/2005 4:27:09 PM   Robert Pearse
 *    2    360Commerce 1.1         3/10/2005 10:19:31 AM  Robert Pearse
 *    1    360Commerce 1.0         2/11/2005 12:09:24 PM  Robert Pearse
 *
 *   Revision 1.19  2004/09/24 18:49:22  mweis
 *   @scr 7012 In Sale, do not prompt for inventory information when building an order.
 *
 *   Revision 1.18  2004/08/10 15:17:35  rsachdeva
 *   @scr 6791 Transaction Level Send
 *
 *   Revision 1.17  2004/07/15 15:43:47  rsachdeva
 *   @scr 6270 Line Display and CPOI
 *
 *   Revision 1.16  2004/07/13 20:15:24  lzhao
 *   @scr 6060: gift receipt for gift card reload.
 *
 *   Revision 1.15  2004/06/28 16:53:44  aschenk
 *   @scr 4864 - Added Gift receipt option to Transaction menu
 *
 *   Revision 1.14  2004/06/25 20:25:00  bwf
 *   @scr 4107 Fixed gift card issue automatic gift receipt.
 *
 *   Revision 1.13  2004/04/05 15:47:54  jdeleau
 *   @scr 4090 Code review comments incorporated into the codebase
 *
 *   Revision 1.12  2004/03/25 20:25:15  jdeleau
 *   @scr 4090 Deleted items appearing on Ingenico, I18N, perf improvements.
 *   See the scr for more info.
 *
 *   Revision 1.11  2004/03/23 15:18:46  tfritz
 *   @scr 3981 - Do not display the line item if the item is not eligible for a special order
 *
 *   Revision 1.10  2004/03/22 18:31:23  pkillick
 *   @scr 3981 -Changes made to inhibit the ordering of items that are not eligible for Special Orders.
 *
 *   Revision 1.9  2004/03/10 19:41:52  baa
 *   @scr work for parsing size from scanned item
 *
 *   Revision 1.8  2004/03/02 18:49:54  baa
 *   @scr 3561 Returns add size info to journal and receipt
 *
 *   Revision 1.7  2004/02/25 15:50:41  epd
 *   @scr 3561 Updated to repair the addiing of items to a txn
 *
 *   Revision 1.6  2004/02/24 20:14:10  epd
 *   @scr 3561 Updates to manage how items are added to transaction
 *
 *   Revision 1.5  2004/02/20 21:08:20  epd
 *   @scr 3561 fixed adding of items to transaction
 *
 *   Revision 1.4  2004/02/20 19:51:59  epd
 *   @scr 3561 Updates to prompt for item size if the item requires a size
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
 *    Rev 1.4   14 Jan 2004 08:52:00   awilliamson
 * Removed system.out.println(letter) Command
 *
 *    Rev 1.3   13 Jan 2004 09:07:36   awilliamson
 * Fixed defect scr3650
 *
 *    Rev 1.2   Nov 17 2003 08:39:02   jriggins
 * Using the SaleReturnLineItemIfc that is already stored in the SaleCargo instance in order to avoid a NullPointerException
 * Resolution for 3430: Sale Service Refactoring
 *
 *    Rev 1.1   Nov 07 2003 12:36:18   baa
 * use SaleCargoIfc
 * Resolution for 3430: Sale Service Refactoring
 *
 *    Rev 1.0   Nov 05 2003 14:13:42   baa
 * Initial revision.
 * ===========================================================================
 */
package oracle.retail.stores.pos.services.sale;

import java.util.List;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNLoyalityDiscountUtility;

import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.WorkstationIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.foundation.manager.device.DeviceException;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.service.SessionBusIfc;
import oracle.retail.stores.pos.device.POSDeviceActions;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.services.common.CPOIPaymentUtility;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.utility.TransactionUtility;

/**
 * This site adds an item to the transaction.
 *
 * @version $Revision: /rgbustores_13.4x_generic_branch/7 $
 */
public class AddItemSite extends PosSiteActionAdapter
{
    private static final long serialVersionUID = -2064793445635929721L;
    /**
     * revision number
     */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/7 $";

    /**
     * Adds the item to the transaction. Mails Continue letter is special order
     * to not ask for serial numbers, else mails GetSerialNumbers letter to
     * possibly ask for serial numbers.
     *
     * @param bus Service Bus
     */
    @Override
    public void arrive(BusIfc bus)
    {
        String letter = CommonLetterIfc.CONTINUE;

        // Grab the item from the cargo
        SaleCargo cargo = (SaleCargo)bus.getCargo();
        SaleReturnTransactionIfc transaction = cargo.getTransaction();
        SaleReturnLineItemIfc srli = cargo.getLineItem();
        
        
        
        
        
        boolean isItemScaned = cargo.isItemScanned();
        String itemSizeCode = cargo.getItemSizeCode();
        ParameterManagerIfc pm = (ParameterManagerIfc) bus.getManager(ParameterManagerIfc.TYPE);

        // call the transaction utility to process the newly added sale line item
        boolean isValidFlag = TransactionUtility.processNewSaleLineItem(transaction, srli, isItemScaned, itemSizeCode, null, pm);

        if (!isValidFlag)
        {
            letter = "NotValid";
        }
        else
        {
            // check for serial item if serialized item or kit header
            if (srli.isSerializedItem() || srli.isKitHeader() || srli.getItemSerial() != null)
            {
                if (srli.getItemQuantity().doubleValue() != 1.0)
                {
                    letter = CommonLetterIfc.TOO_MANY;
                }
                else
                {
                    letter = "GetSerialNumbers";
                }
            }

            // reset item scanner
            cargo.setItemScanned(false);

            // reset item size
            cargo.setItemSizeCode(null);

            // when a new item gets added to the screen the price can get
            // modified for already entered items if any discount rule is
            // applicable including them, so signature capture device needs
            // to be  refreshed.
            if(srli.getAdvancedPricingDiscount() != null ||
                    (srli.getPLUItem() != null && srli.getPLUItem().getAdvancedPricingRules().length > 0))
            {
                cargo.setRefreshNeeded(true);
            }

    		// send to line display device
    		POSDeviceActions pda = new POSDeviceActions((SessionBusIfc)bus);
    		try
    		{
    			pda.lineDisplayItem(srli);
    		}
    		catch (DeviceException e)
    		{
    			logger.warn("Unable to use Line Display.", e);
    		}

            WorkstationIfc workstation = cargo.getRegister().getWorkstation();
            CPOIPaymentUtility.getInstance().addItem(workstation, srli, transaction);
        }
        
        //start POS-334 code changes added by Dharmendra to fix 
        List<PLUItemIfc> couponItemsList = GDYNLoyalityDiscountUtility.getAllCouponItemsList(transaction);
        Boolean areAllItemLevelCpns = GDYNLoyalityDiscountUtility.areAllItemLevelCoupons(couponItemsList);
        Boolean areAllTransactionLevelCpns = GDYNLoyalityDiscountUtility.areAllItemLevelCoupons(couponItemsList);
        logger.info("areAllItemLevelCpns "+areAllItemLevelCpns+" areAllTransactionLevelCpns "+areAllTransactionLevelCpns);
        Boolean eligibleItemsExist = true;
       
        if(srli.getPLUItem().isStoreCoupon()
        		&& GDYNLoyalityDiscountUtility.checkIfCouponAlreadyExits(transaction, srli.getPLUItem())){
        	letter ="CouponAlreadyExits";
        	GDYNLoyalityConstants.recalculateMultipleCpnDiscount = true;
		} else if ((couponItemsList.size() > 1 && !areAllItemLevelCpns)) {
			letter = "MultipleCouponsNotAllowed";
			couponItemsList.remove(couponItemsList.size()-1);
			GDYNLoyalityConstants.recalculateMultipleCpnDiscount = true;
			 
		} else if (couponItemsList.size() > 1 && areAllItemLevelCpns) {
			eligibleItemsExist = GDYNLoyalityDiscountUtility
					.applyMultipleItemLoyalityDiscount(bus, couponItemsList);
			if (!eligibleItemsExist)
				letter = "NoEligibleItemForCoupon";
		}
       //end POS-334 code changes added by Dharmendra to fix 
        
        bus.mail(new Letter(letter), BusIfc.CURRENT);
    }
}
