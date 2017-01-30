/* ===========================================================================
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/sale/validate/ValidateAmountsAisle.java /rgbustores_13.4x_generic_branch/1 2011/05/05 16:17:10 mszekely Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED (MM/DD/YY)
 *    blarse 12/21/10 - Removed warnings. Changed to use an updated (and
 *                      consistent) method in DiscountUtility.
 *    acadar 06/10/10 - use default locale for currency display
 *    acadar 06/09/10 - XbranchMerge acadar_tech30 from
 *                      st_rgbustores_techissueseatel_generic_branch
 *    cgreen 05/26/10 - convert to oracle packaging
 *    cgreen 04/26/10 - XbranchMerge cgreene_tech43 from
 *                      st_rgbustores_techissueseatel_generic_branch
 *    acadar 04/09/10 - optimize calls to LocaleMAp
 *    acadar 04/08/10 - merge to tip
 *    acadar 04/05/10 - use default locale for currency and date/time display
 *    cgreen 04/02/10 - remove deprecated LocaleContantsIfc and currencies
 *    abonda 01/03/10 - update header date
 *    sswamy 11/05/08 - Checkin after merges
 *    acadar 10/30/08 - use localized reason codes for item and transaction
 *                      discounts
 * ===========================================================================
     $Log:
      9    360Commerce 1.8         4/25/2007 8:52:46 AM   Anda D. Cadar   I18N
           merge

      8    360Commerce 1.7         3/29/2007 5:47:12 PM   Michael Boyd    CR
           26172 - v8x merge to trunk


           10   .v8x      1.6.1.2     1/19/2007 5:08:06 PM   Charles D. Baker
           CR
           22742 - Incorrectly checked in revision 9 against wrong CR number.
           Should've been 22742.
           9    .v8x      1.6.1.1     1/19/2007 5:04:46 PM   Charles D. Baker
           CR
           24764 - Corrected alignment of subtotal in EJournal.
           8    .v8x      1.6.1.0     1/18/2007 5:12:37 AM   Manas Sahu
           CR
           22742: Ejournal has 'Sub-Total' field header. EJournal should have
           "Subtotal' field header to be consistent with receipt

           Change in method journalTotals() Line number 550.
      7    360Commerce 1.6         2/24/2006 10:25:02 AM  Brett J. Larsen CR
           10575 - incorrect tax amount in e-journal for tax exempt
           transactions

           replacing faulty code with call to new code in JournalUtilities
      6    360Commerce 1.5         2/16/2006 9:59:25 AM   Jason L. DeLeau 4140:
            Fix flow and error message for unused store coupons.
      5    360Commerce 1.4         2/15/2006 10:03:55 AM  Brett J. Larsen CR
           10575 - journalTotals printing wrong tax info when transaction is
           tax exempt - side-effect of CR 6017
      4    360Commerce 1.3         1/22/2006 11:45:02 AM  Ron W. Haight
           removed references to com.ibm.math.BigDecimal
      3    360Commerce 1.2         3/31/2005 4:30:41 PM   Robert Pearse
      2    360Commerce 1.1         3/10/2005 10:26:38 AM  Robert Pearse
      1    360Commerce 1.0         2/11/2005 12:15:27 PM  Robert Pearse
     $
     Revision 1.15.2.1  2004/10/18 18:20:54  jdeleau
     @scr 7381 Correct printing of tax in the e-journal for when printItemTax
     is turned off.


     Revision 1.15  2004/09/30 20:21:52  jdeleau
     @scr 7263 Make printItemTax apply to e-journal as well as receipts.

     Revision 1.14  2004/08/23 16:15:59  cdb
     @scr 4204 Removed tab characters

     Revision 1.13  2004/08/10 15:51:12  rsachdeva
     @scr 6791 Transaction Level Send

     Revision 1.12  2004/07/21 18:40:36  jriggins
     @scr 6327 Added clones for references to tax information that was being inadvertantly modified globally.

     Revision 1.11  2004/07/16 19:11:35  cdb
     @scr 4559 Discount Cleanup. Corrected prorate behavior for 0 subtotal to be
     more consistent with similar behavior.

     Revision 1.10  2004/07/16 02:53:09  cdb
     @scr 4559 Re-enabled error dialog for invalid discount.

     Revision 1.9  2004/07/15 19:24:10  lzhao
     @scr 6266: align the taxable amount and tax amount.

     Revision 1.8  2004/07/14 23:02:00  lzhao
     @scr 6266: add a space between text and amount.

     Revision 1.7  2004/07/13 20:16:57  lzhao
     @scr 6090: detail tax info in ejournal

     Revision 1.6  2004/07/09 20:56:15  cdb
     @scr 6115    Removed unusual check that discount be less than discounted price. Updated to validate
     employee transaction discounts as well.

     Revision 1.5  2004/06/01 16:04:03  awilliam
     @scr 5261 invalid coupon printing on receipt

     Revision 1.4  2004/05/12 19:46:05  rsachdeva
     @scr 4670 Send: Multiple Sends Journal Total Shipping Charge

     Revision 1.3  2004/02/12 16:48:21  mcs
     Forcing head revision

     Revision 1.2  2004/02/11 21:22:50  rhafernik
     @scr 0 Log4J conversion and code cleanup

     Revision 1.1.1.1  2004/02/11 01:04:12  cschellenger
     updating to pvcs 360store-current


 *
 *    Rev 1.2   Nov 21 2003 13:34:52   rrn
 * Changed "Removed" to "Deleted" in journalDiscountsRemoval method.
 * Resolution for 3506: Journal format changes
 *
 *    Rev 1.1   08 Nov 2003 01:27:22   baa
 * cleanup -sale refactoring
 *
 *    Rev 1.0   Nov 05 2003 17:39:02   sfl
 * Initial revision.
 * Resolution for POS SCR-3430: Sale Service Refactoring
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.gdyn.orpos.pos.services.sale.validate;

// java imports
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNEmployeeDiscountUtility;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNLoyalityDiscountUtility;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.ifc.JournalManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.utility.LocaleMap;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.journal.JournalFormatterManagerIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.PosLaneActionAdapter;
import oracle.retail.stores.pos.services.sale.SaleCargoIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

//--------------------------------------------------------------------------
/**
 * Validates Transaction amounts before tendering
 * <p>
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 **/
// --------------------------------------------------------------------------
@SuppressWarnings("deprecation")
public class GDYNValidateAmountsAisle extends PosLaneActionAdapter
{
    /**
     *
     */
    private static final long serialVersionUID = -5261724879892548116L;
    /**
     * revision number
     **/
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";
    /**
     * constant for error dialog screen
     **/
    public static final String INVALID_TRANS_DISC = "InvalidTransactionDiscount";
    /**
     * constant for error dialog screen
     **/
    public static final String INVALID_TRANS_DISC_DLR = "InvalidTransDiscountDlr";
    /**
     * constant for invalid tax error dialog screen
     **/
    public static final String INVALID_TAX = "InvalidTax";
    /**
     * constant for parameter name
     **/
    public static final String MAX_DISC_PCT = "MaximumTransactionDiscountAmountPercent";
    /**
     * constant for parameter name
     **/
    public static final String MAX_EMP_DISC_PCT = "MaximumEmployeeTransactionDiscountAmountPercent";
    /**
     * length of available space for discount value
     **/
    public static int AVAIL_DISCOUNT_LENGTH = 23;
    /**
     * default shipping charge amount string constant
     **/
    public static final String NO_SHIPPING_CHARGE = "0.00";

    /**
     * invalid trans disc. dlr trans. disc. tag
     **/
    public static final String INVALID_TRANS_DISCOUNT_DLR_TRANS_DISC_TAG =
            "InvalidTransDiscountDlr.TransactionDiscount";
    /**
     * invalid trans disc. dlr trans. disc. default text
     **/
    public static final String INVALID_TRANS_DISCOUNT_DLR_TRANS_DISC_TEXT =
            "transaction discount.";
    /**
     * total shipping charge
     **/
    public static final String TOTAL_SHIPPING_CHARGE = "Total Shipping Charge ";

    // Constant for new letter for Tax Exempt Validation
    public static final String VALIDATE_TAX_EXEMPT = "TaxExempt";

    // ----------------------------------------------------------------------
    /**
     * Subject the transaction discount to several tests to determine
     * whether to accept or reject the discount. For example,
     * the transaction must contain discountable items, otherwise the
     * transaction discount must be removed. The trans discount cannot exceed
     * the maximum transaction discount allowed. And, at the item level, the
     * discount applied cannot exceed the item amount.
     * 
     * @param bus
     *            Service Bus
     **/
    // ----------------------------------------------------------------------
    public void traverse(BusIfc bus)
    {

        SaleCargoIfc cargo = (SaleCargoIfc) bus.getCargo();
        ParameterManagerIfc pm = (ParameterManagerIfc) bus.getManager(ParameterManagerIfc.TYPE);
        POSUIManagerIfc ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
        UtilityManagerIfc utility =
                (UtilityManagerIfc) bus.getManager(UtilityManagerIfc.TYPE);

        // Get the transaction
        SaleReturnTransactionIfc transaction = cargo.getTransaction();

        // get Maximum Discount Percent from parameter
        BigDecimal maxTransDiscPct = getMaximumDiscountPercent(pm, bus.getServiceName());
        BigDecimal maxEmpTransDiscPct = getMaximumEmployeeDiscountPercent(pm, bus.getServiceName());
        // Run through Five tests and exit if there is a failure
        while (true)
        {
            // First Test:
            // Are there items eligible for the transaction discounts applied?
            if (transaction.hasTransactionDiscounts() & !transaction.hasDiscountableItems())
            {
                // clear all transaction discounts to allow operator to proceed
                transaction.clearTransactionDiscounts(true);

                // journal removal of discount due to Clear key
                journalDiscountRemoval(transaction);

                // set message argument to 'transaction discount' letting the operator know that
                // all discounts cannot pass through. In other cases, this argument would be
                // set to store coupon indicating that not all store coupons can be applied to current items
                String trDisc =
                        utility.retrieveDialogText(INVALID_TRANS_DISCOUNT_DLR_TRANS_DISC_TAG,
                                INVALID_TRANS_DISCOUNT_DLR_TRANS_DISC_TEXT);
                String[] argument = new String[] { trDisc };

                // setting letter as LaunchTender because we want to keep on going past the
                // error screen. Not setting the oklettername for the model will cause the
                // operator to go back to previous screen from the error screen.
                // displayDialogWithArgs(INVALID_TRANS_DISC_DLR,ui,argument,"LaunchTender");
                displayDialogWithArgs(INVALID_TRANS_DISC_DLR, ui, argument, VALIDATE_TAX_EXEMPT);
                break;
            }

            // Second Test:
            // Do any of the item's discount amounts exceed its selling price?
            if (transaction.itemsDiscountExceedsSellingPrice())
            {
                displayDialog(INVALID_TRANS_DISC, ui);
                break;
            }

            // Third Test:
            // Does the transaction discount amount exceed the Maximum amount allowed?
            CurrencyIfc maxDiscountAmount = transaction.calculateSubtotalPercentage(maxTransDiscPct);
            CurrencyIfc maxEmpDiscountAmount = transaction.calculateSubtotalPercentage(maxEmpTransDiscPct);
            // In an exchange it is possible for the discount amount to exceed the subtotal
            if (transaction.hasTransactionDiscounts())
            {
                if (transaction.transactionDiscountsExceed(maxDiscountAmount) && !transaction.isExchange())
                {
                    // format the percent value
                    maxTransDiscPct = maxTransDiscPct.setScale(2).movePointRight(2);

                    // Build and display message that the markdown is out of range
                    Locale uiLocale = LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE);
                    String[] msg = new String[3];
                    msg[0] = utility.retrieveCommonText("Discount", "discount", uiLocale);
                    msg[1] = maxTransDiscPct.toString();
                    msg[2] = utility.retrieveCommonText("Transaction", "transaction", uiLocale).toLowerCase(uiLocale);

                    displayDialogWithArgs("InvalidItemDiscount", ui, msg, null);
                    break;
                }

                if (transaction.transactionEmployeeDiscountsExceed(maxEmpDiscountAmount) && !transaction.isExchange())
                {
                    // format the percent value
                    maxEmpTransDiscPct = maxEmpTransDiscPct.setScale(2).movePointRight(2);

                    // Build and display message that the markdown is out of range
                    Locale uiLocale = LocaleMap.getLocale(LocaleConstantsIfc.USER_INTERFACE);
                    String[] msg = new String[3];
                    msg[0] = utility.retrieveCommonText("Employee Discount", "employee discount", uiLocale);
                    msg[1] = maxEmpTransDiscPct.toString();
                    msg[2] = utility.retrieveCommonText("Transaction", "transaction", uiLocale).toLowerCase(uiLocale);

                    displayDialogWithArgs("InvalidItemDiscount", ui, msg, null);
                    break;
                }
            }

            // Fourth Test:
            // Is the tax applied to each item valid?
            if (transaction.itemsTaxExceedsSellingPrice())
            {
                displayDialog(INVALID_TAX, ui);
                break;
            }

            // Fifth Test:
            // Determine if there are store coupons scanned which cannot be applied to
            // any of the current sell items in which case just let the operator know about it.
            
          
            if (!GDYNLoyalityConstants.isLoyalityCpnExists &&!transaction.areAllStoreCouponsApplied())
            {
                // Indicate through the argument text that not all store coupons present can be
                // applied to the current items. Advanced pricing will automatically ignore
                // the inapplicable store coupon.
                String stCoupon =
                        utility.retrieveDialogText("InvalidTransDiscountDlr.StoreCoupon",
                                "store coupon.");
                String[] argument = new String[] { stCoupon };

                // setting letter as LaunchTender because we want to keep on going past the
                // error screen. Not setting the oklettername for the model will cause the
                // operator to go back to previous screen from the error screen.
                // Previous change caused an opportunity to bypassing the totals when store
                // coupons were not applied. Before this resulted in a failure, now the
                // tender site continues to launch.
                // /// 6 360Commerce 1.5 2/16/2006 9:59:25 AM Jason L. DeLeau 4140:
                // // Fix flow and error message for unused store coupons.
                // To fix this, we go ahead and journal the totals as we dislay the dialog
                journalTotals(cargo, transaction, pm);
                // displayDialogWithArgs(INVALID_TRANS_DISC_DLR,ui,argument,"LaunchTender");
                displayDialogWithArgs(INVALID_TRANS_DISC_DLR, ui, argument, VALIDATE_TAX_EXEMPT);
                break;
            }
            // Go on to Tender, since discount is valid
            journalTotals(cargo, transaction, pm);
//            bus.mail(new Letter("LaunchTender"), BusIfc.CURRENT);
            bus.mail(new Letter(VALIDATE_TAX_EXEMPT), BusIfc.CURRENT);
            break;
        } // end while loop
    }

    // ----------------------------------------------------------------------
    /**
     * Display the appropriate dialog screen.
     * 
     * @param value
     *            that represents the dialog screen
     * @param ui
     **/
    // ----------------------------------------------------------------------
    protected void displayDialog(String value, POSUIManagerIfc ui)
    {
        DialogBeanModel dialogModel = new DialogBeanModel();
        dialogModel.setResourceID(value);
        dialogModel.setType(DialogScreensIfc.ERROR);
        ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
    }

    // ----------------------------------------------------------------------
    /**
     * Display the appropriate dialog screen with args.
     * 
     * @param resource
     *            ID
     * @param ui
     * @param arguments
     * @param ok
     *            Name
     **/
    // ----------------------------------------------------------------------
    protected void displayDialogWithArgs(String resourceID, POSUIManagerIfc ui, String[] msg, String okLetter)
    {
        DialogBeanModel dialogModel = new DialogBeanModel();
        dialogModel.setResourceID(resourceID);
        dialogModel.setType(DialogScreensIfc.ERROR);
        dialogModel.setArgs(msg);
        if (okLetter != null)
        {
            dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK, okLetter);
        }
        ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
    }

    // ----------------------------------------------------------------------
    /**
     * Returns BigDecimal, the maximum trans discount % allowed.
     * 
     * @param the
     *            parameter manager
     * @param serviceName
     *            service name
     * @return maximum trans discount percent
     **/
    // ----------------------------------------------------------------------
    protected BigDecimal getMaximumDiscountPercent(ParameterManagerIfc pm,
            String serviceName)
    {
        // retrieve maximum trans discount % from parameter file
        BigDecimal maximum = new BigDecimal("100.00"); // default
        try
        {
            String s = pm.getStringValue(MAX_DISC_PCT);
            s.trim();
            maximum = new BigDecimal(s);
            if (logger.isInfoEnabled())
                logger.info(
                        "Parameter read: " + MAX_DISC_PCT + "=[" + maximum + "]");
        }
        catch (ParameterException e)
        {
            logger.error("" + Util.throwableToString(e) + "");
        }

        // convert maximum to a percent
        maximum = maximum.movePointLeft(2);

        return (maximum);
    }

    // ----------------------------------------------------------------------
    /**
     * Returns BigDecimal, the maximum employee trans discount % allowed.
     * 
     * @param the
     *            parameter manager
     * @param serviceName
     *            service name
     * @return maximum employee trans discount percent
     **/
    // ----------------------------------------------------------------------
    protected BigDecimal getMaximumEmployeeDiscountPercent(ParameterManagerIfc pm,
            String serviceName)
    {
        // retrieve maximum trans discount % from parameter file
        BigDecimal maximum = new BigDecimal("100.00"); // default
        try
        {
            String s = pm.getStringValue(MAX_EMP_DISC_PCT);
            s.trim();
            maximum = new BigDecimal(s);
            if (logger.isInfoEnabled())
                logger.info(
                        "Parameter read: " + MAX_EMP_DISC_PCT + "=[" + maximum + "]");
        }
        catch (ParameterException e)
        {
            logger.error("" + Util.throwableToString(e) + "");
        }

        // convert maximum to a percent
        maximum = maximum.movePointLeft(2);

        return (maximum);
    }

    /**
     * Journals Transaction totals.
     * 
     * @param cargo
     * @param transaction
     * @param pm
     *            Parameter Manager
     */
    public void journalTotals(SaleCargoIfc cargo,
            SaleReturnTransactionIfc transaction,
            ParameterManagerIfc pm)
    {
        JournalManagerIfc journal =
                (JournalManagerIfc) Gateway.getDispatcher().
                        getManager(JournalManagerIfc.TYPE);
        JournalFormatterManagerIfc formatter =
                (JournalFormatterManagerIfc) Gateway.getDispatcher().
                        getManager(JournalFormatterManagerIfc.TYPE);
        if (journal != null && formatter != null)
        {
            journal.journal(transaction.getCashier().getLoginID(),
                    transaction.getTransactionID(),
                    formatter.journalTotals(transaction, pm));
        }

    }

    // -------------------------------------------------------------------
    /**
     * journal the removal of discounts on the transaction
     * 
     * @param transaction
     **/
    // -------------------------------------------------------------------
    public void journalDiscountRemoval(SaleReturnTransactionIfc transaction)
    {
        // get transaction level discount dollar amounts
        TransactionDiscountStrategyIfc[] discountAmounts = transaction.getTransactionDiscountsByAmount();
        TransactionDiscountStrategyIfc[] discountPercentages = transaction.getTransactionDiscountsByPercentage();

        JournalManagerIfc mgr = (JournalManagerIfc) Gateway.getDispatcher().getManager(JournalManagerIfc.TYPE);
        StringBuffer msg = new StringBuffer();
        if (discountAmounts != null)
        {
            for (int i = 0; i < discountAmounts.length; i++)
            {
                CurrencyIfc discountCurr = discountAmounts[i].getDiscountAmount();
                String discountAmountStr = discountCurr.toFormattedString().trim();

                msg.append(Util.EOL)
                        .append("TRANS: Discount")
                        .append(Util.SPACES.substring(discountAmountStr.length(), AVAIL_DISCOUNT_LENGTH))
                        .append(discountAmountStr)
                        .append(Util.EOL)
                        .append("  Discount: Amt. Deleted")
                        .append(Util.EOL)
                        .append("  Disc. Rsn.: ")
                        .append(discountAmounts[i].getReason().getText(LocaleMap.getLocale(LocaleConstantsIfc.JOURNAL)))
                        .append(Util.EOL);
            }
        }

        if (discountPercentages != null)
        {
            for (int i = 0; i < discountPercentages.length; i++)
            {
                double discountRate = 100.0 * discountPercentages[i].getDiscountRate().doubleValue();
                String discountPercentStr = (new Double(discountRate)).toString().trim();
                msg.append(Util.EOL)
                        .append("TRANS: Discount")
                        .append(Util.EOL)
                        .append("  Discount: (")
                        .append(discountPercentStr)
                        .append("%) Deleted")
                        .append(Util.EOL)
                        .append("  Disc. Rsn.: ")
                        .append(discountPercentages[i].getReason().getText(
                                LocaleMap.getLocale(LocaleConstantsIfc.JOURNAL)))
                        .append(Util.EOL);
            }
        }
        String str = "";
        mgr.journal(str, str, msg.toString());
    }
}
