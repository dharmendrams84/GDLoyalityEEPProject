/* ===========================================================================
* Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/transaction/TransactionTotals.java /rgbustores_13.4x_generic_branch/10 2012/02/16 17:18:00 mkutiana Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    mkutiana  02/16/12 - XbranchMerge
 *                         mkutiana_bug13692985-receipt_yousave_calc-2 from
 *                         main
 *    mkutiana  02/16/12 - Modified You Save of Receipt calculation to included
 *                         promotion discounts
 *    mkutiana  02/08/12 - XbranchMerge
 *                         mkutiana_bug13692985-receipt_yousave_calc from
 *                         rgbustores_13.4x_generic_branch
 *    mkutiana  02/07/12 - modified YOU SAVE calculation for receipt display to
 *                         use Sale item totals only
 *    mchellap  01/23/12 - Do not show YOU SAVED message for even exchange
 *                         transactions
 *    rsnayak   08/16/11 - Fix to update Change due
 *    cgreene   07/07/11 - calculate extended perm price by multiplying with
 *                         quantity
 *    cgreene   07/07/11 - calculate amountOff with extended absolute values
 *    cgreene   06/13/11 - do not add quantity of line item it is a coupon
 *    cgreene   03/22/11 - prevent NPE during check for temp prices changes
 *    cgreene   03/18/11 - XbranchMerge cgreene_124_receipt_quick_wins from
 *                         main
 *    cgreene   03/16/11 - implement You Saved feature on reciept and
 *                         AllowMultipleQuantity parameter
 *    nkgautam  06/22/10 - bill pay changes
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    abondala  01/03/10 - update header date
 *    blarsen   07/14/09 - XbranchMerge
 *                         blarsen_bug8673286-pos-crash-pickup-with-employee-discount
 *                         from rgbustores_13.1x_branch
 *    blarsen   07/09/09 - Adding 2 missing members from the clone method. The
 *                         uncloned (thus, zero) discountEligibleSubtotal
 *                         member was causing a divide by zero. See 13.1.1 HPQC
 *                         3931
 *    cgreene   06/22/09 - removed unnecessary creation of BigDecimal
 *    cgreene   01/08/09 - set package number for send package in get methods
 *    acadar    10/28/08 - localization for item tax reason codes
 *    cgreene   09/19/08 - updated with changes per FindBugs findings
 *
 * ===========================================================================
     $Log:
      10   360Commerce 1.9         5/3/2007 11:57:43 PM   Sandy Gu
           Enhance transaction persistence layer to store inclusive tax
      9    360Commerce 1.8         4/30/2007 6:05:12 PM   Brett J. Larsen CR
           26474 - added deprecation release # for methods deprecated by the
           VAT feature

      8    360Commerce 1.7         4/30/2007 5:38:35 PM   Sandy Gu        added
            api to handle inclusive tax
      7    360Commerce 1.6         4/25/2007 10:00:17 AM  Anda D. Cadar   I18N
           merge
      6    360Commerce 1.5         7/21/2006 4:14:15 PM   Brendan W. Farrell
           Merge from v7.x.  Use ifc so that it is extendable.
      5    360Commerce 1.4         1/25/2006 4:11:53 PM   Brett J. Larsen merge
            7.1.1 changes (aka. 7.0.3 fixes) into 360Commerce view
      4    360Commerce 1.3         1/22/2006 11:41:58 AM  Ron W. Haight
           Removed references to com.ibm.math.BigDecimal
      3    360Commerce 1.2         3/31/2005 4:30:36 PM   Robert Pearse
      2    360Commerce 1.1         3/10/2005 10:26:26 AM  Robert Pearse
      1    360Commerce 1.0         2/11/2005 12:15:18 PM  Robert Pearse
     $:
      5    .v700     1.2.1.1     1/6/2006 12:37:58      Deepanshu       CR
           6017: Calculate and save tax exempt
      4    .v700     1.2.1.0     1/4/2006 12:01:09      Deepanshu       CR
           6160: Alterations not to be counted as units sold
      3    360Commerce1.2         3/31/2005 15:30:36     Robert Pearse
      2    360Commerce1.1         3/10/2005 10:26:26     Robert Pearse
      1    360Commerce1.0         2/11/2005 12:15:18     Robert Pearse
     $
     Revision 1.37  2004/09/23 00:30:51  kmcbride
     @scr 7211: Inserting serialVersionUIDs in these Serializable classes

     Revision 1.36  2004/09/10 16:34:01  rsachdeva
     @scr 6791 Transaction Level Send Deprecated initialize with shippingCharge

     Revision 1.35  2004/08/31 20:53:14  rsachdeva
     @scr 6791 Transaction Level Send

     Revision 1.34  2004/08/24 19:10:05  rsachdeva
     @scr 6791 Transaction Level Send

     Revision 1.33  2004/08/23 16:15:46  cdb
     @scr 4204 Removed tab characters

     Revision 1.32  2004/08/20 21:27:44  rsachdeva
     @scr 6791 Transaction Level Send

     Revision 1.31  2004/08/09 16:47:55  rsachdeva
     @scr 6791 Transaction Level Send Javadoc

     Revision 1.30  2004/08/09 14:25:58  rsachdeva
     @scr 6791 Transaction Level Send

     Revision 1.29  2004/07/02 00:14:53  rzurga
     @scr 5107 Customer Point of Interaction- Several elements mising from CPOI screen

     Minor cleanup.

     Revision 1.28  2004/07/02 00:00:38  rzurga
     @scr 5107 Customer Point of Interaction- Several elements mising from CPOI screen

     Added quantitySale to TransactionTotals that accounts for the sale items only.

     Revision 1.27  2004/06/29 21:29:16  jdeleau
     @scr 5777 Improve on the way return taxes are calculated, to solve this defect.
     Returns and purchases were going into the same container, and return
     values were being cleared on subsequent calculations.

     Revision 1.26  2004/06/29 15:34:31  jdeleau
     @scr 5777 Deleting items from the screen now recalculates
     tax correctly

     Revision 1.25  2004/06/11 18:59:53  lzhao
     @scr 4670: Change the way to getCalculatedShippingCharge

     Revision 1.24  2004/06/09 20:38:48  rsachdeva
     @scr 4670 Send: Multiple Sends Initialize

     Revision 1.23  2004/06/02 18:39:44  rsachdeva
     @scr 4670 Send: Multiple Sends toString Updated

     Revision 1.22  2004/05/27 22:18:12  rsachdeva
     @scr 4670 Send: Multiple Sends

     Revision 1.21  2004/05/27 16:59:23  mkp1
     @scr 2775 Checking in first revision of new tax engine.

     Revision 1.20  2004/05/18 15:04:40  rsachdeva
     @scr 4670 Send: Multiple Sends clone

     Revision 1.19  2004/05/06 20:50:57  epd
     @scr 4267 Changes to logic to make sure transaction was really retrieved when the code thought it was

     Revision 1.18  2004/05/04 16:15:44  rsachdeva
     @scr 4670 Send: Multiple Sends

     Revision 1.17  2004/04/15 22:17:41  rsachdeva
     @scr 3906 Non Merchandise

     Revision 1.16  2004/04/15 15:40:32  jriggins
     @scr 3979 Added price adjustment line item logic

     Revision 1.15  2004/04/14 21:14:31  rsachdeva
     @scr 3906 Sale

     Revision 1.14  2004/04/06 20:15:12  jriggins
     @scr 3979 Added a check for PriceAdjustmentLineItems in updateTransactionTotals(). Also reworked the original conditional statement for other line items

     Revision 1.13  2004/04/05 16:48:47  rsachdeva
     @scr 3906 Sale

     Revision 1.12  2004/04/05 16:27:13  rsachdeva
     @scr 3906 Sale

     Revision 1.11  2004/03/15 20:28:34  cdb
     @scr 3588 Updated ItemPrice test. Removed ItemPrice deprecated (2 release) methods.

     Revision 1.10  2004/03/10 19:25:53  mweis
     @scr 0 Clear up comment on restocking fees.

     Revision 1.9  2004/03/01 22:17:53  rsachdeva
     @scr 3906 Unit of Measure

     Revision 1.8  2004/03/01 22:05:19  rsachdeva
     @scr 3906 Unit of Measure

     Revision 1.7  2004/02/20 17:01:08  bjosserand
     @scr 0 Mail Bank Check

     Revision 1.6  2004/02/17 17:57:43  bwf
     @scr 0 Organize imports.

     Revision 1.5  2004/02/17 16:18:52  rhafernik
     @scr 0 log4j conversion

     Revision 1.4  2004/02/12 22:56:56  tfritz
     @scr 3718 Added nonTaxable functionality

     Revision 1.3  2004/02/12 17:14:42  mcs
     Forcing head revision

     Revision 1.2  2004/02/11 23:28:51  bwf
     @scr 0 Organize imports.

     Revision 1.1.1.1  2004/02/11 01:04:34  cschellenger
     updating to pvcs 360store-current


 *
 *    Rev 1.3   Dec 16 2003 10:08:52   lzhao
 * code review follow up
 *
 *    Rev 1.2   Dec 05 2003 08:57:12   lzhao
 * gift card sale will not count as part of units sold.
 * Resolution for 3371: Feature Enhancement:  Gift Card Enhancement
 *
 *    Rev 1.1   Oct 02 2003 10:45:58   baa
 * fix for rss defect 1406
 * Resolution for 3402: RSS  PRF 1406 Tender Information on Void transactions does not looks correct
 *
 *    Rev 1.0   Aug 29 2003 15:41:20   CSchellenger
 * Initial revision.
 *
 *    Rev 1.6   Jun 07 2003 11:48:54   sfl
 * Added new data attribute and supporting methods to store tax information for receipt use.
 * Resolution for POS SCR-1749: POS 6.0 Tax Package
 *
 *    Rev 1.5   May 19 2003 10:52:48   dfh
 * added a specific customer's updates that allow manual return item tax override to be used, may not need this for product, did not want changes to be lost - have commented out the code block in method calculateGroupTax, missed abs()
 * Resolution for Domain SCR-104: Merge 5.1/5.5 into 6.0
 *
 *    Rev 1.4   May 19 2003 10:48:20   dfh
 * added a specific customer's updates that allow manual return item tax override to be used, may not need this for product, did not want changes to be lost - have commented out the code block in method calculateGroupTax
 * Resolution for Domain SCR-104: Merge 5.1/5.5 into 6.0
 *
 *    Rev 1.3   Jan 31 2003 13:40:48   sfl
 * Modified the adjustRounding method to make sure
 * that the currency amount will be rounded from after the
 * 3rd digit after decimal point.
 * Resolution for POS SCR-1749: POS 6.0 Tax Package
 *
 *    Rev 1.2   Dec 13 2002 16:00:56   sfl
 * Keep long precision for grand total amount till the last moment so that no roundings happens before the grand total is ready for display.
 * Resolution for POS SCR-1749: POS 6.0 Tax Package
 *
 *    Rev 1.1   03 Oct 2002 17:07:34   sfl
 * Plugged in a simplified tax computing method with
 * longer precision during calculation.
 *
 *    Rev 1.0   Jun 03 2002 17:07:02   msg
 * Initial revision.
 *
 *    Rev 1.4   15 May 2002 17:14:20   vxs
 * Removed concatenations from logging statements
 * Resolution for POS SCR-1632: Updates for Gap
 *
 *    Rev 1.3   20 Mar 2002 12:47:50   vxs
 * Modified toString()
 * Resolution for POS SCR-954: Domain - Arts Translation
 *
 *    Rev 1.1   Mar 18 2002 23:11:44   msg
 * - updated copyright
 *
 *    Rev 1.0   Mar 18 2002 12:30:36   msg
 * Initial revision.
 *
 *    Rev 1.7   12 Mar 2002 10:40:00   pjf
 * Remove call to new TaxGroup().
 * Resolution for POS SCR-1550: Use Factory to get new object instances in POS & Domain
 *
 *    Rev 1.6   Feb 23 2002 10:31:32   mpm
 * Modified Util.BIG_DECIMAL to Util.I_BIG_DECIMAL, Util.ROUND_HALF to Util.I_ROUND_HALF.
 * Resolution for Domain SCR-35: Accept Foundation BigDecimal backward-compatibility changes
 *
 *    Rev 1.5   Feb 05 2002 16:36:34   mpm
 * Modified to use IBM BigDecimal class.
 * Resolution for Domain SCR-27: Employ IBM BigDecimal class
 *
 *    Rev 1.4   24 Jan 2002 17:04:24   pjf
 * Refactoring, Updates to correct defects introduced by UI conversion.
 * Resolution for POS SCR-832: Kit quantity is displaying +1 in the total region,  quantity
 * Resolution for POS SCR-837: Kit discount amount not showing on sell item screen.
 * Resolution for POS SCR-838: Kit Header tax flag displayed incorrectly on sell item screen.
 *
 *    Rev 1.3   21 Jan 2002 16:37:02   pjf
 * Defect fix - don't prorate tax when single return item is in  a tax group.
 * Resolution for POS SCR-155: Trans level tax amount override on return = total tax * #items
 *
 *    Rev 1.2   Dec 26 2001 17:26:02   mpm
 * Modified to properly handle order transaction retrieval.
 * Resolution for Domain SCR-14: Special Order modifications
 *
 *    Rev 1.1   07 Dec 2001 18:37:06   sfl
 * The calculatedShippingCharge and shippingMethod
 * attributes are moved from SaleReturnTransaction.java
 * to here to let more applications in the future to use
 * these information.
 * Resolution for Domain SCR-19: Domain SCR for Shipping Method use case in Send Package
 *
 *    Rev 1.0   Sep 20 2001 16:05:48   msg
 * Initial revision.
 *
 *    Rev 1.1   Sep 17 2001 12:39:52   msg
 * header update
 * ===========================================================================
 */
package oracle.retail.stores.domain.transaction;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.parameter.ParameterConstantsIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.discount.DiscountCalculationIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.financial.ShippingMethodIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.DiscountableTaxableLineItemIfc;
import oracle.retail.stores.domain.lineitem.ItemPriceIfc;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.lineitem.OrderLineItemIfc;
import oracle.retail.stores.domain.lineitem.PriceAdjustmentLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc;
import oracle.retail.stores.domain.lineitem.TaxLineItemInformationIfc;
import oracle.retail.stores.domain.stock.AlterationPLUItemIfc;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.tax.InternalTaxEngineIfc;
import oracle.retail.stores.domain.tax.TaxConstantsIfc;
import oracle.retail.stores.domain.tax.TaxEngineIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.conduit.Dispatcher;
import oracle.retail.stores.foundation.utility.Util;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.transaction.GDYNTransactionTotalsIfc;

/**
 * This class holds and computes the transaction totals based on the line items,
 * the discounts and the taxes.
 *
 * @version $Revision: /rgbustores_13.4x_generic_branch/10 $
 */
public class TransactionTotals implements TransactionTotalsIfc
{
    // This id is used to tell the compiler not to generate a new serialVersionUID.
    static final long serialVersionUID = 4731987200275355460L;

    /** Debug logger */
    private static final Logger logger = Logger.getLogger(TransactionTotals.class);

    /**
     * revision number supplied by source-code control system
     */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/10 $";

    /**
     * sub total of items (without discount)
     */
    protected CurrencyIfc subtotal;

    /**
     * sub total of sale items (without discount)
     */
    protected CurrencyIfc saleSubtotal;

    /**
     * sub total of returned items (without discount)
     */
    protected CurrencyIfc returnSubtotal;

    /**
     * sum of discounts
     */
    protected CurrencyIfc discountTotal;

    /**
     * sum of restockingFee
     */
    protected CurrencyIfc restockingFeeTotal;

    /**
     * sum of transaction discounts
     */
    protected CurrencyIfc transactionDiscountTotal;

    /**
     * sum of item discounts
     */
    protected CurrencyIfc itemDiscountTotal;

    /**
     * sum of sale item discounts
     */
    protected CurrencyIfc saleDiscountTotal;
    
    /**
     * sum of sale item discounts - includes the promotional discounts.
     */
    protected CurrencyIfc saleDiscountAndPromotionTotal;

    /**
     * sum of return item discounts
     */
    protected CurrencyIfc returnDiscountTotal;

    /**
     * sum of taxes
     */
    protected CurrencyIfc taxTotal;

    /**
     * sum of inclusive taxes
     */
    protected CurrencyIfc inclusiveTaxTotal;

    /**
     * sum of taxes for UI use
     */
    protected CurrencyIfc taxTotalUI;

    /**
     * sum of taxes for tax overridden items
     */
    protected CurrencyIfc taxExceptionsTotal;

    /**
     * sum of quantities
     */
    protected BigDecimal quantityTotal;

    /**
     * sum of sale quantities
     */
    protected BigDecimal quantitySale;

    /**
     * grand total
     */
    protected CurrencyIfc grandTotal;

    /**
     * total amount off
     */
    protected CurrencyIfc amountOffTotal;

    /**
     * amount tendered
     */
    protected CurrencyIfc amountTender;

    /**
     * balance due
     */
    protected CurrencyIfc balanceDue;
    
    /**
    change due
    **/
    protected CurrencyIfc changeDue;
  
    /**
     * number of line items (why go hunt for this multiple times?
     */
    protected int numItems = 0;

    /**
     * Send package line item for each send
     */
    protected Vector<SendPackageLineItemIfc> sendPackages;

    /**
     * Checks send level assigned.
     */
    protected boolean transactionLevelSendAssigned = false;

    /**
     * subtotal of items eligible for transaction discounts (used only for
     * calculation)
     */
    protected CurrencyIfc discountEligibleSubtotal;

    /**
     * discount calculation class
     */
    protected DiscountCalculationIfc discountCalculator = null;

    /**
     * layaway fee
     */
    protected CurrencyIfc layawayFee;

    /**
     * unit of measure as units
     */
    protected boolean uomAsUnits = true;

    /**
     * Tax Information Container new for release 7.0
     */
    protected TaxInformationContainerIfc taxInformationContainer;

    /**
     * Tax engine new for release 7.0
     */
    protected TaxEngineIfc taxEngine;

    /**
     * property configured for quantity total incremented using non-merchandise
     * quantity
     */
    protected static final String QUANTITY_TOTAL_NONMERCHANDISE = "QuantityTotalNonMerchandise";

    /**
     * property configured for including/excluding discount in subtotal
     */
    protected static final String DISCOUNT_INCLUDED_IN_SUBTOTAL = "DiscountIncludedInSubtotal";

    /**
     * default true value
     */
    protected static final String DEFAULT_TRUE_VALUE = "true";

    /**
     * default false value
     */
    protected static final String DEFAULT_FALSE_VALUE = "false";

    /**
     * sum of exempt taxes
     */
    protected CurrencyIfc exemptTaxTotal;
    
    /**
     * variable to check whether loyality discount is applied
     *//*
    protected  Boolean isLoyalityDiscountApplied = false;
    
    *//**
     * variable to hold total loyalityDiscount Applied 
     * 
     *//*
    private  BigDecimal totalLoyalityDiscountAmt = BigDecimal.ZERO;*/
    
    /**
	 * @return the totalLoyalityDiscountAmt
	 */
	/*public  BigDecimal getTotalLoyalityDiscountAmt() {
		return totalLoyalityDiscountAmt;
	}*/


	/**
	 * @param totalLoyalityDiscountAmt the totalLoyalityDiscountAmt to set
	 */
	/*public  void setTotalLoyalityDiscountAmt(BigDecimal d) {
		totalLoyalityDiscountAmt = d;
	}*/


	/**
	 * @return the isLoyalityDiscountApplied
	 */
	/*public  Boolean getIsLoyalityDiscountApplied() {
		return isLoyalityDiscountApplied;
	}*/

	
	/**
	 * @param isLoyalityDiscountApplied the isLoyalityDiscountApplied to set
	 */
	/*public  void setIsLoyalityDiscountApplied(Boolean b) {
		isLoyalityDiscountApplied = b;
	}*/

	/**
     * Constructs TransactionTotals object.
     *
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     */
    public TransactionTotals()
    {
        // initialize transaction totals (to 0)
        subtotal = DomainGateway.getBaseCurrencyInstance();
        saleSubtotal = DomainGateway.getBaseCurrencyInstance();
        returnSubtotal = DomainGateway.getBaseCurrencyInstance();
        discountTotal = DomainGateway.getBaseCurrencyInstance();
        restockingFeeTotal = DomainGateway.getBaseCurrencyInstance();
        transactionDiscountTotal = DomainGateway.getBaseCurrencyInstance();
        itemDiscountTotal = DomainGateway.getBaseCurrencyInstance();
        saleDiscountTotal = DomainGateway.getBaseCurrencyInstance();
        saleDiscountAndPromotionTotal = DomainGateway.getBaseCurrencyInstance();
        returnDiscountTotal = DomainGateway.getBaseCurrencyInstance();
        taxTotal = DomainGateway.getBaseCurrencyInstance();
        inclusiveTaxTotal = DomainGateway.getBaseCurrencyInstance();
        taxTotalUI = DomainGateway.getBaseCurrencyInstance();
        taxExceptionsTotal = DomainGateway.getBaseCurrencyInstance();
        grandTotal = DomainGateway.getBaseCurrencyInstance();
        amountOffTotal = DomainGateway.getBaseCurrencyInstance();
        amountTender = DomainGateway.getBaseCurrencyInstance();
        balanceDue = DomainGateway.getBaseCurrencyInstance();
        changeDue = DomainGateway.getBaseCurrencyInstance();
        discountEligibleSubtotal = DomainGateway.getBaseCurrencyInstance();
        quantityTotal = BigDecimal.ZERO;
        quantitySale = BigDecimal.ZERO;
        discountCalculator = DomainGateway.getFactory().getDiscountCalculationInstance();
        layawayFee = DomainGateway.getBaseCurrencyInstance();
        taxInformationContainer = DomainGateway.getFactory().getTaxInformationContainerInstance();
        taxEngine = DomainGateway.getFactory().getTaxEngineInstance();
        sendPackages = new Vector<SendPackageLineItemIfc>();
      /*  isLoyalityDiscountApplied = false;
        totalLoyalityDiscountAmt = BigDecimal.ZERO;*/
    }

    /**
     * Constructs TransactionTotals object with specified parameters.
     *
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     *
     * @param saleSub subtotal of sale items
     * @param returnSub subtotal of returned items
     * @param saleDisc discount total on sale items
     * @param saleDiscAndProm discount total including promotional discount on sale items
     * @param returnDisc discount total on return items
     * @param tax tax total
     * @param grand grand total
     * @param tendered amount tendered
     * @param due balance due
     */
    public TransactionTotals(CurrencyIfc saleSub,
                             CurrencyIfc returnSub,
                             CurrencyIfc saleDisc,
                             CurrencyIfc saleDiscAndProm,
                             CurrencyIfc returnDisc,
                             CurrencyIfc tax,
                             CurrencyIfc inclusiveTax,
                             CurrencyIfc grand,
                             CurrencyIfc tendered,
                             CurrencyIfc due)
    {
        // initialize transaction totals
        initialize(saleSub,
                   returnSub,
                   saleDisc,
                   saleDiscAndProm,
                   returnDisc,
                   tax,
                   inclusiveTax,
                   grand,
                   tendered,
                   due);
    }

    /**
     * Initializes object with passed in values.
     *
     * @param saleSub subtotal of sale items
     * @param returnSub subtotal of returned items
     * @param saleDisc discount total on sale items
     * @param saleDiscAndProm discount total including promotional discount on sale items
     * @param returnDisc discount total on return items
     * @param tax tax total
     * @param grand grand total
     * @param tendered amount tendered
     * @param due balance due
     */
    public void initialize(CurrencyIfc saleSub,
                           CurrencyIfc returnSub,
                           CurrencyIfc saleDisc,
                           CurrencyIfc saleDiscAndPromo,
                           CurrencyIfc returnDisc,
                           CurrencyIfc tax,
                           CurrencyIfc inclusiveTax,
                           CurrencyIfc grand,
                           CurrencyIfc tendered,
                           CurrencyIfc due)
    {
        // initialize transaction totals
        saleSubtotal = DomainGateway.getBaseCurrencyInstance(saleSub.getStringValue());
        returnSubtotal = DomainGateway.getBaseCurrencyInstance(returnSub.getStringValue());
        subtotal = (CurrencyIfc)saleSubtotal.clone();
        subtotal = subtotal.subtract(returnSub);
        saleDiscountTotal = DomainGateway.getBaseCurrencyInstance(saleDisc.getStringValue());
        saleDiscountAndPromotionTotal = DomainGateway.getBaseCurrencyInstance(saleDiscAndPromo.getStringValue());
        returnDiscountTotal = DomainGateway.getBaseCurrencyInstance(returnDisc.getStringValue());
        discountTotal = (CurrencyIfc)saleDiscountTotal.clone();
        discountTotal = discountTotal.subtract(returnDiscountTotal);
        taxTotal = DomainGateway.getBaseCurrencyInstance(tax.getStringValue());
        inclusiveTaxTotal = DomainGateway.getBaseCurrencyInstance(inclusiveTax.getStringValue());
        taxTotalUI = DomainGateway.getBaseCurrencyInstance(adjustRounding(tax,
                TransactionTotalsIfc.UI_PRINT_TAX_DISPLAY_SCALE).getStringValue());
        grandTotal = DomainGateway.getBaseCurrencyInstance(grand.getStringValue());
        amountTender = DomainGateway.getBaseCurrencyInstance(tendered.getStringValue());
        balanceDue = DomainGateway.getBaseCurrencyInstance(due.getStringValue());
        taxInformationContainer = DomainGateway.getFactory().getTaxInformationContainerInstance();
        taxEngine = DomainGateway.getFactory().getTaxEngineInstance();
        sendPackages = new Vector<SendPackageLineItemIfc>();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()
    {
        // create new object
        TransactionTotals t = new TransactionTotals();

        // set clone attributes
        setCloneAttributes(t);

        // pass back object
        return t;

    }

    /**
     * Sets attributes in clone.
     *
     * @param clone new instance of class
     */
    protected void setCloneAttributes(TransactionTotals clone)
    {
        clone.setSubtotal((CurrencyIfc)getSubtotal().clone());
        clone.setSaleSubtotal((CurrencyIfc)getSaleSubtotal().clone());
        clone.setReturnSubtotal((CurrencyIfc)getReturnSubtotal().clone());
        clone.setDiscountTotal((CurrencyIfc)getDiscountTotal().clone());
        clone.setSaleDiscountTotal((CurrencyIfc)getSaleDiscountTotal().clone());
        clone.setSaleDiscountAndPromotionTotal((CurrencyIfc)getSaleDiscountAndPromotionTotal().clone());
        clone.setReturnDiscountTotal((CurrencyIfc)getReturnDiscountTotal().clone());
        clone.setTaxTotal((CurrencyIfc)getTaxTotal().clone());
        clone.setInclusiveTaxTotal((CurrencyIfc)getInclusiveTaxTotal().clone());
        clone.setTaxTotalUI((CurrencyIfc)getTaxTotalUI().clone());
        clone.setTaxExceptionsTotal((CurrencyIfc)getTaxExceptionsTotal().clone());
        clone.setGrandTotal((CurrencyIfc)getGrandTotal().clone());
        clone.setAmountTender((CurrencyIfc)getAmountTender().clone());
        clone.setBalanceDue((CurrencyIfc)getBalanceDue().clone());
        clone.setChangeDue((CurrencyIfc) getChangeDue().clone());
        clone.setQuantityTotal(getQuantityTotal());
        clone.setQuantitySale(getQuantitySale());
        clone.setTransactionDiscountTotal((CurrencyIfc)getTransactionDiscountTotal().clone());
        clone.setItemDiscountTotal((CurrencyIfc)getItemDiscountTotal().clone());
        clone.setNumItems(numItems);
        clone.setDiscountEligibleSubtotal(getDiscountEligibleSubtotal());
        clone.setAllItemUOMUnits(uomAsUnits);
        if (getAmountOffTotal() != null)
        {
            clone.amountOffTotal = (CurrencyIfc)getAmountOffTotal().clone();
        }

        // set the restocking fee total
        if (restockingFeeTotal != null)
        {
            clone.setRestockingFeeTotal((CurrencyIfc)restockingFeeTotal.clone());
        }

        // set send package info
        for (int k = 0; k < this.getItemSendPackagesCount(); k++)
        {
            SendPackageLineItemIfc sendPackage = sendPackages.get(k);
            clone.addSendPackage((SendPackageLineItemIfc)sendPackage.clone());
        }
        clone.setTransactionLevelSendAssigned(this.transactionLevelSendAssigned);
        // set the layaway fee
        if (layawayFee != null)
        {
            clone.setLayawayFee((CurrencyIfc)layawayFee.clone());
        }
        if (taxInformationContainer != null)
        {
            clone.setTaxInformationContainer((TaxInformationContainerIfc)taxInformationContainer.clone());
        }
        else
        {
            clone.setTaxInformationContainer(null);
        }

        if (taxEngine != null)
        {
            clone.setTaxEngine((TaxEngineIfc)taxEngine.clone());
        }
        else
        {
            clone.setTaxEngine(null);
        }
    }

    /**
     * Updates transaction totals.
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>transaction totals updated
     * </UL>
     *
     * @param lineItems vector of line items
     * @param discounts array of transaction discounts
     * @param tax transaction tax object
     */
    public void updateTransactionTotals(AbstractTransactionLineItemIfc[] lineItems,
                                        TransactionDiscountStrategyIfc[] discounts,
                                        TransactionTaxIfc tax)
    {

        // local item references
        numItems = lineItems.length;
        // remove order line items that have been canceled
        Vector<AbstractTransactionLineItemIfc> totalableLineItems = new Vector<AbstractTransactionLineItemIfc>();
        // walk current lineItems, add non-canceled and not-price adjustment
        // ones to totalableLineItems
        for (int i = 0; i < numItems; i++)
        {
            // Add the item unless it is a price adjustment instance or a
            // canceled order line item
            if (!(lineItems[i] instanceof PriceAdjustmentLineItemIfc)
                    && (!(lineItems[i] instanceof OrderLineItemIfc) || ((OrderLineItemIfc) lineItems[i])
                            .getItemStatus() != OrderLineItemIfc.ORDER_ITEM_STATUS_CANCELED))
            {
                totalableLineItems.add(lineItems[i]);
            }
        }

        // reset new lineItems size
        numItems = totalableLineItems.size();

        // initialize values
        subtotal.setZero();
        saleSubtotal.setZero();
        returnSubtotal.setZero();
        discountTotal.setZero();
        saleDiscountTotal.setZero();
        saleDiscountAndPromotionTotal.setZero();
        transactionDiscountTotal.setZero();
        itemDiscountTotal.setZero();
        restockingFeeTotal.setZero();
        returnDiscountTotal.setZero();
        taxTotal.setZero();
        inclusiveTaxTotal.setZero();
        taxTotalUI.setZero();
        taxExceptionsTotal.setZero();
        quantityTotal = BigDecimal.ZERO;
        quantitySale = BigDecimal.ZERO;
        taxInformationContainer.reset();

        // calculate sub totals
        calculateSubtotals(totalableLineItems);

        // set grand total
        calculateGrandTotal();

        if (discounts != null)
        {
            if (discounts.length > 0)
            {
                // calculate discounts
                discountCalculator.calculateDiscounts(this, discounts, totalableLineItems);
            }
        }

        // add the discount total to the promotions
        amountOffTotal = amountOffTotal.add(discountTotal);

        // update taxes if not tax exempt
        if (tax != null)
        {
            // update taxes
            // calculateTaxes(totalableLineItems, tax);
            if (tax.getTaxMode() == TaxConstantsIfc.TAX_MODE_EXEMPT)
            {
                computeExemptTaxes(totalableLineItems, tax);
            }
            else
            {
                computeTaxes(totalableLineItems, tax);
            }
        }
        else
        {
            throw new NullPointerException("Null TransactionTax in TransactionTotals.updateTransactionTotals()");
        }

        // reset grand total
        calculateGrandTotal();

        // set balance due to grand total
        balanceDue.setStringValue(grandTotal.getStringValue());
        // subtract amount tendered (calculated elsewhere)
        balanceDue = grandTotal.subtract(amountTender);
    }

    /**
     * Updates transaction totals.
     *
     * @param paymentAmount a CurrencyIfc payment amount to be used to update
     *            transaction totals.
     */
    public void updateTransactionTotalsForPayment(CurrencyIfc paymentAmount)
    {
        // initialize values
        subtotal.setZero();
        saleSubtotal.setZero();
        returnSubtotal.setZero();
        discountTotal.setZero();
        saleDiscountTotal.setZero();
        saleDiscountAndPromotionTotal.setZero();
        transactionDiscountTotal.setZero();
        itemDiscountTotal.setZero();
        returnDiscountTotal.setZero();
        taxTotal.setZero();
        inclusiveTaxTotal.setZero();
        taxTotalUI.setZero();
        taxExceptionsTotal.setZero();
        quantityTotal = BigDecimal.ZERO;
        quantitySale = BigDecimal.ZERO;
        taxInformationContainer.reset();

        // Substitute for calculateSubTotals(Vector lineItems)
        subtotal = subtotal.add(paymentAmount);
        saleSubtotal = saleSubtotal.add(paymentAmount);
        quantityTotal = quantityTotal.add(Util.I_BIG_DECIMAL_ONE);
        quantitySale = quantitySale.add(Util.I_BIG_DECIMAL_ONE);

        // set grand total
        calculateGrandTotal();

        // set balance due to grand total
        balanceDue.setStringValue(grandTotal.getStringValue());
        // subtract amount tendered (calculated elsewhere)
        balanceDue = grandTotal.subtract(amountTender);

    }

    /**
     * Updates transaction totals for bill payments
     * @param paymentAmount a CurrencyIfc payment amount to be used to update
     *            transaction totals.
     */
    public void updateTransactionTotalsForBillPayment(CurrencyIfc billPaymentAmount)
    {
        // Substitute for calculateSubTotals(Vector lineItems)
        subtotal = subtotal.add(billPaymentAmount);
        saleSubtotal = saleSubtotal.add(billPaymentAmount);
        quantityTotal = quantityTotal.add(Util.I_BIG_DECIMAL_ONE);
        quantitySale = quantitySale.add(Util.I_BIG_DECIMAL_ONE);

        // set grand total
        calculateGrandTotal();

        // set balance due to grand total
        balanceDue.setStringValue(grandTotal.getStringValue());
        // subtract amount tendered (calculated elsewhere)
        balanceDue = grandTotal.subtract(amountTender);

    }

    /**
     * Calculates subtotals, both overall and by sale or return. Also calculates
     * quantity total.
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>subtotal calculated
     * <LI>saleSubtotal calculated
     * <LI>returnSubtotal calculated
     * <LI>discountTotal calculated (incremental value)
     * <LI>saleDiscountTotal calculated (incremental value)
     * <LI>saleDiscountAndPromotionTotal calculated (incremental value)
     * <LI>returnDiscountTotal calculated (incremental value)
     * </UL>
     *
     * @param lineItems vector of line items
     */
    protected void calculateSubtotals(Vector<AbstractTransactionLineItemIfc> lineItems)
    {
        // local reference to line item
        SaleReturnLineItemIfc li = null;
        CurrencyIfc extendedSellingPrice = DomainGateway.getBaseCurrencyInstance();
        CurrencyIfc itemDiscountAmount = DomainGateway.getBaseCurrencyInstance();
        Enumeration<AbstractTransactionLineItemIfc> e = lineItems.elements();
        discountEligibleSubtotal.setZero();
        amountOffTotal.setZero();
        // initially assume that all items have unit of measure as units
        setAllItemUOMUnits(true);
        // loop through line items , capturing sale, return subtotals and item
        // discounts
        while (e.hasMoreElements())
        {
            li = (SaleReturnLineItemIfc)e.nextElement();
            if (li.isUnitOfMeasureItem())
            {
                setAllItemUOMUnits(false);
            }

            if (!li.isKitHeader() && !li.isPriceAdjustmentLineItem())
            {
                BigDecimal itemQuantity = li.getItemQuantityDecimal();

                extendedSellingPrice = li.getExtendedSellingPrice();
                itemDiscountAmount = li.getItemDiscountAmount();
                subtotal = subtotal.add(extendedSellingPrice);
                discountTotal = discountTotal.add(itemDiscountAmount);;
                boolean incrementQuantity = true;
                // check if sale or returned item
                if (itemQuantity.signum() > 0)
                {
                    if (li.getPLUItem() != null && li.getPLUItem().hasTemporaryPriceChanges())
                    {
                        // get the permanent price and mutliply it by the
                        // quantity.
                        CurrencyIfc extendedPermanentPrice = li.getItemPrice().getPermanentSellingPrice()
                                .multiply(li.getItemQuantityDecimal()).abs();
                        // subtract the selling price from the full permanent
                        // price
                        amountOffTotal = amountOffTotal
                                .add(extendedPermanentPrice.subtract(extendedSellingPrice.abs()));
                        saleDiscountAndPromotionTotal = saleDiscountAndPromotionTotal
                        .add(extendedPermanentPrice.subtract(extendedSellingPrice.abs()));
                    }
                    
                    saleSubtotal = saleSubtotal.add(extendedSellingPrice);
                    saleDiscountTotal = saleDiscountTotal.add(itemDiscountAmount);
                    saleDiscountAndPromotionTotal = saleDiscountAndPromotionTotal.add(itemDiscountAmount);
                    
                    // reset item transaction discount
                    li.clearTransactionDiscounts();
                    li.recalculateItemTotal();

                    // bump up item quantity
                    if (incrementQuantity && li.isServiceItem())
                    {
                        incrementQuantity = this.isNonMerchandiseQuantityIncremented();
                    }
                    // gift card issue and reload will not count as
                    // units sold
                    // Alterations will not count as units sold
                    if ((li.getPLUItem() instanceof GiftCardPLUItemIfc)
                            || (li.getPLUItem() instanceof AlterationPLUItemIfc))
                    {
                        incrementQuantity = false;
                    }
                    if (incrementQuantity)
                    {
                        quantityTotal = quantityTotal.add(itemQuantity);
                        if (!li.getPLUItem().isStoreCoupon())
                        {
                            quantitySale = quantitySale.add(itemQuantity);
                        }
                    }

                    // if item eligible for discounts, save total
                    if (li.isDiscountEligible())
                    {
                        discountEligibleSubtotal = discountEligibleSubtotal.add(extendedSellingPrice);
                        discountEligibleSubtotal = discountEligibleSubtotal.subtract(itemDiscountAmount);
                    }

                }
                // handle returned item
                else
                {
                    returnSubtotal = returnSubtotal.add(extendedSellingPrice);
                    returnDiscountTotal = returnDiscountTotal.add(itemDiscountAmount);                 

                    
                    // get item quantity totaled, but use absolute value
                    // Non Merchandise Items could be returned, hence
                    // checking
                    if (li.isServiceItem() && !this.isNonMerchandiseQuantityIncremented())
                    {
                        incrementQuantity = false;
                    }
                    if (incrementQuantity)
                    {
                        quantityTotal = quantityTotal.subtract(itemQuantity);
                    }
                }

                // reset item discount total
                li.setItemDiscountTotal(itemDiscountAmount);

                // roll up item discounts
                itemDiscountTotal = itemDiscountTotal.add(itemDiscountAmount);

                // if it is a returned item calculate the restocking fee total
                if (itemQuantity.signum() < 0)
                {
                    ItemPriceIfc itemPrice = li.getItemPrice();
                    if (itemPrice != null)
                    {
                        CurrencyIfc itemExtendedRestockingFee = itemPrice.getExtendedRestockingFee();
                        if (itemExtendedRestockingFee != null)
                        {
                            restockingFeeTotal = restockingFeeTotal.add(itemExtendedRestockingFee);
                        }
                    }
                }
            }
        }

    }

    /**
     * This checks configuration in properties file. Default is to include
     * non-merchandise items. This is used for sale totals summary table
     * display.
     *
     * @return boolean true if quantity to be incremented with non-merchandise
     */
    public boolean isNonMerchandiseQuantityIncremented()
    {
        boolean incrementNonMerchandiseQuantity = true;
        String countQuantity = DomainGateway.getProperty(QUANTITY_TOTAL_NONMERCHANDISE, DEFAULT_TRUE_VALUE);
        if (!countQuantity.equalsIgnoreCase(DEFAULT_TRUE_VALUE))
        {
            incrementNonMerchandiseQuantity = false;
        }
        return incrementNonMerchandiseQuantity;
    }

    /**
     * This checks configuration in properties file and accordingly returns
     * subtotal which includes/excludes discounts. Default is to exclude
     * discounts. This is used for sale totals summary table display.
     *
     * @return CurrencyIfc excludes/includes discounts from subtotal
     */
    public CurrencyIfc getUISubtotalAsConfigured()
    {
        CurrencyIfc calculateSubtotalAsConfigured = null;
        String isDiscountIncludedSubtotal = DomainGateway.getProperty(DISCOUNT_INCLUDED_IN_SUBTOTAL,
                DEFAULT_FALSE_VALUE);
        if (isDiscountIncludedSubtotal.equalsIgnoreCase(DEFAULT_FALSE_VALUE))
        {
            calculateSubtotalAsConfigured = this.getSubtotal();
        }
        else
        {
            calculateSubtotalAsConfigured = this.getPreTaxSubtotal();
        }
        return calculateSubtotalAsConfigured;
    }

    /**
     * Returns pretax total, i.e., subtotal less discount total.
     *
     * @return pretax total
     */
    public CurrencyIfc getPreTaxSubtotal()
    {
        return (subtotal.subtract(discountTotal));
    }

    /**
     * Calculates grand total.
     */
    public void calculateGrandTotal()
    {
        // set grand total, balance due
        grandTotal = subtotal.subtract(discountTotal);
        // tax total to be done after third pass
        grandTotal = grandTotal.add(taxTotal);
        // grandTotal = grandTotal.add(this.taxTotalUI);
        // on a return, subtract any restocking fees
        grandTotal = grandTotal.subtract(restockingFeeTotal);
        // add calculated shipping charge
        grandTotal = grandTotal.add(getCalculatedShippingCharge());
        // add layaway fee
        grandTotal = grandTotal.add(layawayFee);
        // As a final step, round long precisioned grand total amount so that it
        // can be displayed at UI, E-Journal
        grandTotal = adjustRounding(grandTotal, TransactionTotalsIfc.UI_PRINT_TAX_DISPLAY_SCALE);
    }

    /**
     * Compute taxes.
     *
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>Valid line item has been entered
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>Item tax amounts and rate have been set
     * <LI>Totals have been updated
     * </UL>
     *
     * @param lineItems vector of line items
     * @param tax TransactionTax object
     */
    protected void computeTaxes(Vector<AbstractTransactionLineItemIfc> lineItems, TransactionTaxIfc tax)
    {
        TaxLineItemInformationIfc[] items = lineItems.toArray(new TaxLineItemInformationIfc[lineItems.size()]);
        taxEngine.calculateTax(items, this, tax);
    }

    /**
     * Compute exempt taxes.
     *
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>Valid line item has been entered
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>Item tax amounts and rate have been set
     * <LI>Totals have been updated
     * </UL>
     *
     * @param lineItems vector of line items
     * @param tax TransactionTax object
     */
    protected void computeExemptTaxes(Vector<AbstractTransactionLineItemIfc> lineItems, TransactionTaxIfc tax)
    {
        TaxLineItemInformationIfc[] items = lineItems.toArray(new TaxLineItemInformationIfc[lineItems.size()]);
        ((InternalTaxEngineIfc)taxEngine).calculateExepmtTax(items, this, tax);
    }

    /**
     * Update tax exceptions total.
     *
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>Item tax amounts and rate have been set
     * <LI>taxExceptionsTotal have been updated
     * </UL>
     *
     * @param lineItems vector of line items
     */
    protected void calculateTaxExceptionsTotal(Vector<AbstractTransactionLineItemIfc> lineItems)
    {
        AbstractTransactionLineItemIfc li = null;
        Enumeration<AbstractTransactionLineItemIfc> e = lineItems.elements();
        while (e.hasMoreElements())
        {

            li = e.nextElement();

            if (li instanceof DiscountableTaxableLineItemIfc)
            {
                DiscountableTaxableLineItemIfc dtli = (DiscountableTaxableLineItemIfc)li;

                // external tax mgr
                SaleReturnLineItemIfc srli = (SaleReturnLineItemIfc)dtli;

                ItemTaxIfc it = srli.getItemTax();

                // keep track of tax total for overridden items.
                if(it.getTaxMode() == TaxIfc.TAX_MODE_OVERRIDE_AMOUNT ||
                   it.getTaxMode() == TaxIfc.TAX_MODE_OVERRIDE_RATE)
                {
                    taxExceptionsTotal = taxExceptionsTotal.add(it.getItemTaxAmount());
                }// done setting taxExemptionsTotal.
            }
        }
    }

    /**
     * Updates tender totals.
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>tender totals updated
     * </UL>
     *
     * @param lineItems vector of tender line items
     */
    public void updateTenderTotals(TenderLineItemIfc[] lineItems)
    {
        amountTender.setZero();
        int numTenderItems = 0;
        if (lineItems != null)
        {
            numTenderItems = lineItems.length;
        }
        for (int i = 0; i < numTenderItems; i++)
        {
            amountTender = amountTender.add(lineItems[i].getAmountTender());
        }
        balanceDue = grandTotal.subtract(amountTender);
    }

    /**
     * Retrieves subtotal.
     *
     * @return subtotal
     */
    public CurrencyIfc getSubtotal()
    {
        return (subtotal);
    }

    /**
     * Sets subtotal.
     *
     * @param d subtotal
     */
    public void setSubtotal(CurrencyIfc d)
    {
        subtotal = d;
    }

    /**
     * Retrieves subtotal of sale items.
     *
     * @return saleSubtotal
     */
    public CurrencyIfc getSaleSubtotal()
    {
        return (saleSubtotal);
    }

    /**
     * Sets subtotal of sale items.
     *
     * @param d saleSubtotal
     */
    public void setSaleSubtotal(CurrencyIfc d)
    {
        saleSubtotal = d;
    }

    /**
     * Retrieves subtotal of return items.
     *
     * @return returnSubtotal
     */
    public CurrencyIfc getReturnSubtotal()
    {
        return (returnSubtotal);
    }

    /**
     * Sets subtotal of return items.
     *
     * @param d returnSubtotal
     */
    public void setReturnSubtotal(CurrencyIfc d)
    {
        returnSubtotal = d;
    }

    /**
     * Retrieves discount total.
     *
     * @return discount total
     */
    public CurrencyIfc getDiscountTotal()
    {
        return (discountTotal);
    }

    /**
     * Sets discount total.
     *
     * @param d discount total
     */
    public void setDiscountTotal(CurrencyIfc d)
    {
        discountTotal = d;
    }

    /**
     * Retrieves restocking fee total.
     *
     * @return restockingFee as CurrencyIfc
     */
    public CurrencyIfc getRestockingFeeTotal()
    {
        return (restockingFeeTotal);
    }

    /**
     * Sets restocking fee total.
     *
     * @param rf as CurrencyIfc
     */
    public void setRestockingFeeTotal(CurrencyIfc rf)
    {
        restockingFeeTotal = rf;
    }

    /**
     * Retrieves discount total of sale items.
     *
     * @sale saleDiscountTotal
     */
    public CurrencyIfc getSaleDiscountTotal()
    {
        return (saleDiscountTotal);
    }

    /**
     * Sets discount total of sale items.
     *
     * @param d saleDiscountTotal
     */
    public void setSaleDiscountTotal(CurrencyIfc d)
    {
        saleDiscountTotal = d;
    }
    
    /**
     * Retrieves Total amount off on Sale items includes promotion discounts.
     *
     * @return saleDiscountAndPromotionTotal
     */
    public CurrencyIfc getSaleDiscountAndPromotionTotal()
    {
        return (saleDiscountAndPromotionTotal);
    }

    /**
     * Sets Total amount off on Sale items includes promotion discounts.
     *
     * @param d saleDiscountAndPromotionTotal
     */
    public void setSaleDiscountAndPromotionTotal(CurrencyIfc d)
    {
    	saleDiscountAndPromotionTotal = d;
    }

    /**
     * Retrieves discount total of return items.
     *
     * @return returnDiscountTotal
     */
    public CurrencyIfc getReturnDiscountTotal()
    {
        return (returnDiscountTotal);
    }

    /**
     * Sets discount total of return items.
     *
     * @param d returnDiscountTotal
     */
    public void setReturnDiscountTotal(CurrencyIfc d)
    {
        returnDiscountTotal = d;
    }

    /**
     * Retrieves sum of transaction discounts.
     *
     * @transaction transactionDiscountTotal
     */
    public CurrencyIfc getTransactionDiscountTotal()
    {
        return (transactionDiscountTotal);
    }

    /**
     * Sets sum of transaction discounts.
     *
     * @param d transactionDiscountTotal
     */
    public void setTransactionDiscountTotal(CurrencyIfc d)
    {
        transactionDiscountTotal = d;
    }

    /**
     * Retrieves sum of item discounts.
     *
     * @item itemDiscountTotal
     */
    public CurrencyIfc getItemDiscountTotal()
    {
        return (itemDiscountTotal);
    }

    /**
     * Sets sum of item discounts.
     *
     * @param d itemDiscountTotal
     */
    public void setItemDiscountTotal(CurrencyIfc d)
    {
        itemDiscountTotal = d;
    }

    /**
     * Retrieves tax total.
     *
     * @return tax total
     */
    public CurrencyIfc getTaxTotal()
    {
        return (this.taxTotal);
    }

    /**
     * Retrieves inclusive tax total.
     *
     * @return tax total
     */
    public CurrencyIfc getInclusiveTaxTotal()
    {
        return (this.inclusiveTaxTotal);
    }

    /**
     * Retrieves tax total for UI use.
     *
     * @return taxtotalUI
     */
    public CurrencyIfc getTaxTotalUI()
    {
        return (this.taxTotalUI);
    }

    /**
     * Adjust rounding for CurrencyIfc object
     *
     * @param amount CurrencyIfc
     * @param scale int
     * @return CurrencyIfc with rounded amount
     */
    public CurrencyIfc adjustRounding(CurrencyIfc amount, int scale)
    {
        BigDecimal bd = amount.getDecimalValue();
        BigDecimal bOne = BigDecimal.ONE;

        // Need to do rounding in two steps, starting from the 3rd decimal digit
        // first,
        // then round again at the 2nd decimal digit.
        bd = bd.divide(bOne, 3, BigDecimal.ROUND_HALF_UP);
        CurrencyIfc roundedCurrency = DomainGateway.getBaseCurrencyInstance(bd);

        BigDecimal bd2 = roundedCurrency.getDecimalValue();
        bd2 = bd2.divide(bOne, scale, BigDecimal.ROUND_HALF_UP);

        roundedCurrency = DomainGateway.getBaseCurrencyInstance(bd2);
        return (roundedCurrency);
    }

    /**
     * Retrieves tax total for overridden items. Total is calculated by adding
     * taxes of all items which are overridden by rate or amount.
     *
     * @return CurrencyIfc object representing taxExceptionsTotal
     */
    public CurrencyIfc getTaxExceptionsTotal()
    {
        return taxExceptionsTotal;
    }

    /**
     * Sets tax total.
     *
     * @param d tax total
     */
    public void setTaxTotal(CurrencyIfc d)
    {
        taxTotal = d;
    }

    /**
     * Sets inclusive tax total.
     *
     * @param d tax total
     */
    public void setInclusiveTaxTotal(CurrencyIfc d)
    {
        inclusiveTaxTotal = d;
    }

    /**
     * Sets tax total for UI use.
     *
     * @param d tax total
     */
    public void setTaxTotalUI(CurrencyIfc d)
    {
        taxTotalUI = d;
    }

    /**
     * Sets tax exceptions total. Total is calculated by adding taxes of all
     * items which are overridden by rate or amount.
     *
     * @param d CurrencyIfc object representing the tax exceptions total.
     */
    public void setTaxExceptionsTotal(CurrencyIfc d)
    {
        taxExceptionsTotal = d;
    }

    /**
     * Retrieves quantity sale. Does not include service items and coupons.
     *
     * @return quantity sale
     */
    public BigDecimal getQuantitySale()
    {
        return (quantitySale);
    }

    /**
     * Retrieves quantity total.
     *
     * @return quantity total
     */
    public BigDecimal getQuantityTotal()
    {
        return (quantityTotal);
    }

    /**
     * Checks if all items have unit of measure as units
     *
     * @return boolean true if all items have unit of measure as units
     */
    public boolean isAllItemUOMUnits()
    {
        return this.uomAsUnits;
    }

    /**
     * If any item does not have unit of measure as units, this is set to false,
     * otherwise true.
     *
     * @param uomAsUnits unit of measure as units
     */
    public void setAllItemUOMUnits(boolean uomAsUnits)
    {
        this.uomAsUnits = uomAsUnits;
    }

    /**
     * Sets quantity total.
     *
     * @param d quantity total
     */
    public void setQuantityTotal(BigDecimal d)
    {
        quantityTotal = d;
    }

    /**
     * Sets quantity sale.
     *
     * @param d quantity sale
     */
    public void setQuantitySale(BigDecimal d)
    {
        quantitySale = d;
    }

    /**
     * Retrieves grand total.
     *
     * @return grand total
     */
    public CurrencyIfc getGrandTotal()
    {
        return (grandTotal);
    }

    /**
     * Sets grand total.
     *
     * @param d grand total
     */
    public void setGrandTotal(CurrencyIfc d)
    {
        grandTotal = d;
    }

    /**
     * Retrieves tender amount.
     *
     * @return tender amount
     */
    public CurrencyIfc getAmountTender()
    {
        return (amountTender);
    }

    /**
     * Sets amount tendered.
     *
     * @param d amount tendered
     */
    public void setAmountTender(CurrencyIfc d)
    {
        amountTender = d;
    }

    /**
     * Retrieves the amount of all the discounts, promotions and manual
     * markdowns applied to the transaction.
     *
     * @return tender amount
     */
    public CurrencyIfc getAmountOffTotal()
    {
        return amountOffTotal;
    }

    /**
     * Only returns {@link #getSaleDiscountAndPromotionTotal()} if the amount is larger than
     * {@link ParameterConstantsIfc#PRINTING_YouSavedThresholdPercent} of
     * the {@link #getSaleSubtotal()}.
     *
     * @return tender amount
     */
    public CurrencyIfc getAmountOffTotalForReceipt()
    {
        CurrencyIfc amountOffTotalSaleForReceipt = getSaleDiscountAndPromotionTotal();
        
        
        if (amountOffTotalSaleForReceipt != null && getSaleSubtotal() != null)
        {
            ParameterManagerIfc parmMgr = (ParameterManagerIfc)Dispatcher.getDispatcher().getManager(ParameterManagerIfc.TYPE);
            if (parmMgr != null)
            {
                try
                {
                    int parm = parmMgr.getIntegerValue(ParameterConstantsIfc.PRINTING_YouSavedThresholdPercent);
                    CurrencyIfc saleSubTotal = getSaleSubtotal();                    
                    CurrencyIfc pctCurr = amountOffTotalSaleForReceipt.divide(saleSubTotal);                    
                    int pct = pctCurr.getDecimalValue().multiply(BigDecimal.valueOf(100)).intValue();                    
                    if (pct < parm)
                    {
                        return null;
                    }
                }
                catch (ParameterException e)
                {
                    logger.warn("Unable to determine parameter YouSavedThresholdPercent.", e);
                }
            }
        }
       
      
        
        return amountOffTotalSaleForReceipt;
    }

    /**
     * Retrieves balance due.
     *
     * @return balance due
     */
    public CurrencyIfc getBalanceDue()
    {
        return (balanceDue);
    }

    /**
     * Sets balance due.
     *
     * @param d balance due
     */
    public void setBalanceDue(CurrencyIfc d)
    {
        balanceDue = d;

    }
    
     /**
     * Retrieves change due.
     * <P>
     * 
     * @return change due
     **/
    public CurrencyIfc getChangeDue()
    {
        return (changeDue);
    }

    /**
     * Sets change due.
     * <P>
     * 
     * @param d change due
     **/

    public void setChangeDue(CurrencyIfc d)
    {
        changeDue = d;
    }
    

    /**
     * Retrieves number of line items .
     *
     * @return number of line items
     */
    public int getNumItems()
    {
        return (numItems);
    }

    /**
     * Sets number of line items .
     *
     * @param value number of line items
     */
    public void setNumItems(int value)
    {
        numItems = value;
    }

    /**
     * Retrieves subtotal of discount-eligible items.
     *
     * @return discountEligibleSubtotal
     */
    public CurrencyIfc getDiscountEligibleSubtotal()
    {
        return (discountEligibleSubtotal);
    }

    /**
     * Sets subtotal of discount-eligible items.
     *
     * @param d discountEligibleSubtotal
     */
    public void setDiscountEligibleSubtotal(CurrencyIfc d)
    {
        discountEligibleSubtotal = d;
    }

    /**
     * Retrieves calculated shipping charge.
     *
     * @return calculatedShippingCharge as CurrencyIfc
     */
    public CurrencyIfc getCalculatedShippingCharge()
    {
        CurrencyIfc calculatedShippingCharge = DomainGateway.getBaseCurrencyInstance();
        for (int i = 0; i < getItemSendPackagesCount(); i++)
        {
            ShippingMethodIfc shippingMethod = getSendPackages()[i].getShippingMethod();
            if (shippingMethod != null)
            {
                calculatedShippingCharge = calculatedShippingCharge.add(shippingMethod.getCalculatedShippingCharge());
            }
        }
        return (calculatedShippingCharge);
    }

    /**
     * Gets the total send packages count. If value > 0 is returned, it implies
     * that sends (one or more) have taken place. The Value returned is as per
     * the total number of sends. Item send packages count implies send packages
     * that has/should have items in it. This should not be confused with send
     * level, as it is used for both item level and transaction level send
     *
     * @return int item send packages count
     */
    public int getItemSendPackagesCount()
    {
        return sendPackages.size();
    }

    /**
     * For each new send adds a send package line item containing shipping-to
     * customer and shipping method used
     *
     * @param sendPackage send package line item
     */
    public void addSendPackage(SendPackageLineItemIfc sendPackageUsed)
    {
        sendPackages.add(sendPackageUsed);
        sendPackageUsed.setPackageNumber(sendPackages.size());
    }

    /**
     * Gets send packages for all the sends. Beware this returns the actual
     * vector. Refrain from manipulating it directly.
     *
     * @return Vector send packages for all the sends
     */
    public Vector<SendPackageLineItemIfc> getSendPackageVector()
    {
        return sendPackages;
    }

    /**
     * Get send package at the requested index. This is zero-based, which would
     * be the package's number minus one.
     *
     * @return package at the requested index.
     */
    public SendPackageLineItemIfc getSendPackage(int index)
    {
        SendPackageLineItemIfc sendPackage = sendPackages.get(index);
        sendPackage.setPackageNumber(index + 1);
        return sendPackage;
    }

    /**
     * Remove send package at the requested index. This is zero-based, which would
     * be the package's number minus one.
     *
     * @return package at the requested index.
     */
    public SendPackageLineItemIfc removeSendPackage(int index)
    {
        SendPackageLineItemIfc sendPackage = sendPackages.remove(index);
        sendPackage.setPackageNumber(index + 1);
        return sendPackage;
    }

    /**
     * Gets send packages for all the sends
     *
     * @return an array of send packages for all the sends
     */
    public SendPackageLineItemIfc[] getSendPackages()
    {
        SendPackageLineItemIfc[] array = new SendPackageLineItemIfc[sendPackages.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = sendPackages.get(i);
            array[i].setPackageNumber(i + 1);
        }
        return array;
    }

    /**
     * Sets Transaction Level Send. This implies that the Send Level being used
     * is Transaction, not Item.
     *
     * @param transactionLevelSendAssigned true means transaction level send
     */
    public void setTransactionLevelSendAssigned(boolean transactionLevelSendAssigned)
    {
        this.transactionLevelSendAssigned = transactionLevelSendAssigned;
    }

    /**
     * Checks for Transaction or Item Level Send. Transaction Level send gets
     * shipping method info after clicking tender. Multiple sends are not
     * allowed.
     *
     * @return boolean true means transaction level send
     */
    public boolean isTransactionLevelSendAssigned()
    {
        boolean assigned = false;
        if (this.getItemSendPackagesCount() == 1 && this.transactionLevelSendAssigned)
        {
            assigned = true;
        }
        return assigned;
    }

    /**
     * Retrieves layaway fee.
     *
     * @return layawayFee as CurrencyIfc
     */
    public CurrencyIfc getLayawayFee()
    {
        return (layawayFee);
    }

    /**
     * Sets layaway fee.
     *
     * @param lf as CurrencyIfc
     */
    public void setLayawayFee(CurrencyIfc lf)
    {
        layawayFee = lf;
    }

    /**
     * Retrieves the container for storing the results of the tax calculations
     *
     * @return
     */
    public TaxInformationContainerIfc getTaxInformationContainer()
    {
        return taxInformationContainer;
    }

    /**
     * Set the tax information container
     *
     * @param value
     */
    public void setTaxInformationContainer(TaxInformationContainerIfc value)
    {
        taxInformationContainer = value;
    }

    /**
     * Retrieves the current tax engine
     *
     * @return TaxEngineIfc
     */
    public TaxEngineIfc getTaxEngine()
    {
        return taxEngine;
    }

    /**
     * Sets the tax engine to use for calculating taxes
     *
     * @param value
     */
    public void setTaxEngine(TaxEngineIfc value)
    {
        taxEngine = value;
    }

    /**
     * Retrieves exempt tax total.
     *
     * @return CurrencyIfc the exempt tax total
     */
    public CurrencyIfc getExemptTaxTotal()
    {
        return (this.exemptTaxTotal);
    }

    /**
     * Sets exempt tax total.
     *
     * @param taxAmount CurrencyIfc the exempt tax amount
     */
    public void setExemptTaxTotal(CurrencyIfc taxAmount)
    {
        exemptTaxTotal = taxAmount;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        // quick exit
        if (obj == this)
        {
            return true;
        }

        boolean isEqual = false; // set the return code to false

        try
        {
            TransactionTotals c = (TransactionTotals)obj;
            // compare all the attributes of TransactionTotals
            if (Util.isObjectEqual(subtotal, c.getSubtotal())
                    && Util.isObjectEqual(saleSubtotal, c.getSaleSubtotal())
                    && Util.isObjectEqual(returnSubtotal, c.getReturnSubtotal())
                    && Util.isObjectEqual(discountTotal, c.getDiscountTotal())
                    && Util.isObjectEqual(transactionDiscountTotal, c.getTransactionDiscountTotal())
                    && Util.isObjectEqual(itemDiscountTotal, c.getItemDiscountTotal())
                    && Util.isObjectEqual(saleDiscountTotal, c.getSaleDiscountTotal())
                    && Util.isObjectEqual(saleDiscountAndPromotionTotal, c.getSaleDiscountAndPromotionTotal())
                    && Util.isObjectEqual(returnDiscountTotal, c.getReturnDiscountTotal())
                    && Util.isObjectEqual(taxTotal, c.getTaxTotal())
                    && Util.isObjectEqual(inclusiveTaxTotal, c.getInclusiveTaxTotal())
                    && Util.isObjectEqual(taxExceptionsTotal, c.getTaxExceptionsTotal())
                    && Util.isObjectEqual(layawayFee, c.getLayawayFee())
                    && Util.isObjectEqual(quantityTotal, c.getQuantityTotal())
                    && Util.isObjectEqual(quantitySale, c.getQuantitySale())
                    && Util.isObjectEqual(grandTotal, c.getGrandTotal())
                    && Util.isObjectEqual(amountTender, c.getAmountTender())
                    && Util.isObjectEqual(balanceDue, c.getBalanceDue())
                    && numItems == c.getNumItems()
                    && Util.isObjectEqual(restockingFeeTotal, c.getRestockingFeeTotal())
                    && Util.isObjectEqual(taxInformationContainer, c.getTaxInformationContainer())
                    && transactionLevelSendAssigned == c.transactionLevelSendAssigned
                    && Util.isObjectEqual(sendPackages, c.sendPackages))
            {
                isEqual = true; // set the return code to true
            }
        }
        catch (Exception e)
        {
            // ignore, leave false
        }
        return isEqual;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        // build result string
        StringBuilder strResult = new StringBuilder("Class:  TransactionTotals (Revision " + getRevisionNumber()
                + ") @" + hashCode());
        strResult.append("\n");

        // add attributes to string
        if (subtotal == null)
        {
            strResult.append("subtotal:                           [null]\n");
        }
        else
        {
            strResult.append("subtotal:                           [").append(subtotal).append("]\n");
        }
        if (saleSubtotal == null)
        {
            strResult.append("saleSubtotal:                       [null]\n");
        }
        else
        {
            strResult.append("saleSubtotal:                       [").append(saleSubtotal).append("]\n");
        }
        if (returnSubtotal == null)
        {
            strResult.append("returnSubtotal:                     [null]\n");
        }
        else
        {
            strResult.append("returnSubtotal:                     [").append(returnSubtotal).append("]\n");
        }
        if (discountTotal == null)
        {
            strResult.append("discountTotal:                      [null]\n");
        }
        else
        {
            strResult.append("discountTotal:                      [").append(discountTotal).append("]\n");
        }
        if (transactionDiscountTotal == null)
        {
            strResult.append("transactionDiscountTotal:           [null]\n");
        }
        else
        {
            strResult.append("transactionDiscountTotal:           [").append(transactionDiscountTotal).append("]\n");
        }
        if (itemDiscountTotal == null)
        {
            strResult.append("itemDiscountTotal:                  [null]\n");
        }
        else
        {
            strResult.append("itemDiscountTotal:                  [").append(itemDiscountTotal).append("]\n");
        }
        if (saleDiscountTotal == null)
        {
            strResult.append("saleDiscountTotal:                  [null]\n");
        }
        else
        {
            strResult.append("saleDiscountTotal:                  [").append(saleDiscountTotal).append("]\n");
        }
        if (saleDiscountAndPromotionTotal == null)
        {
            strResult.append("totalForYOUSAVEReceipt:                  [null]\n");
        }
        else
        {
            strResult.append("totalForYOUSAVEReceipt:                  [").append(saleDiscountAndPromotionTotal).append("]\n");
        }
        if (returnDiscountTotal == null)
        {
            strResult.append("returnDiscountTotal:                [null]\n");
        }
        else
        {
            strResult.append("returnDiscountTotal:                [").append(returnDiscountTotal).append("]\n");
        }
        if (restockingFeeTotal == null)
        {
            strResult.append("restockingFeeTotal:                [null]\n");
        }
        else
        {
            strResult.append("restockingFeeTotal:                [").append(restockingFeeTotal).append("]\n");
        }
        if (taxTotal == null)
        {
            strResult.append("taxTotal:                           [null]\n");
        }
        else
        {
            strResult.append("taxTotal:                           [").append(taxTotal).append("]\n");
        }
        if (inclusiveTaxTotal == null)
        {
            strResult.append("inclusiveTaxTotal:                  [null]\n");
        }
        else
        {
            strResult.append("inclusiveTaxTotal:                  [").append(inclusiveTaxTotal).append("]\n");
        }
        if (taxTotalUI == null)
        {
            strResult.append("taxTotalUI:                         [null]\n");
        }
        else
        {
            strResult.append("taxTotalUI:                         [").append(taxTotalUI).append("]\n");
        }
        if (taxExceptionsTotal == null)
        {
            strResult.append("taxExceptionsTotal:                 [null]\n");
        }
        else
        {
            strResult.append("taxExceptionsTotal:                           [").append(taxExceptionsTotal)
                    .append("]\n");
        }
        if (taxTotal == null)
        {
            strResult.append("quantityTotal:                      [null]\n");
            strResult.append("quantitySale:                      [null]\n");
        }
        else
        {
            strResult.append("quantityTotal:                      [").append(quantityTotal).append("]\n");
            strResult.append("quantitySale:                      [").append(quantitySale).append("]\n");
        }
        if (layawayFee == null)
        {
            strResult.append("layawayFee:                         [null]\n");
        }
        else
        {
            strResult.append("layawayFee:                         [").append(layawayFee).append("]\n");
        }
        if (grandTotal == null)
        {
            strResult.append("grandTotal:                         [null]\n");
        }
        else
        {
            strResult.append("grandTotal:                         [").append(grandTotal).append("]\n");
        }
        if (amountTender == null)
        {
            strResult.append("amountTender:                       [null]\n");
        }
        else
        {
            strResult.append("amountTender:                       [").append(amountTender).append("]\n");
        }
        if (balanceDue == null)
        {
            strResult.append("balanceDue:                         [null]\n");
        }
        else
        {
            strResult.append("balanceDue:                         [").append(balanceDue).append("]\n");
        }

        strResult.append("total number of sends:              [").append(getItemSendPackagesCount()).append("]\n");

        CurrencyIfc calculatedShippingCharge = getCalculatedShippingCharge();
        if (calculatedShippingCharge == null)
        {
            strResult.append("calculatedShippingCharge:           [null]\n");
        }
        else
        {
            strResult.append("calculatedShippingCharge:           [").append(calculatedShippingCharge).append("]\n");
        }
        strResult.append("numItems:                           [").append(numItems).append("]\n");
        if (discountEligibleSubtotal == null)
        {
            strResult.append("discountEligibleSubtotal:           [null]\n");
        }
        else
        {
            strResult.append("discountEligibleSubtotal:           [").append(discountEligibleSubtotal).append("]\n");
        }
        if (taxInformationContainer == null)
        {
            strResult.append("taxInformationContainer:               [null]\n");
        }
        else
        {
            strResult.append(taxInformationContainer);
        }

        // pass back result
        return strResult.toString();
    }

    /**
     * Retrieves the Team Connection revision number.
     *
     * @return String representation of revision number
     */
    public String getRevisionNumber()
    {
        // return string
        return (revisionNumber);
    }

    /**
     * TransactionTotals main method.
     *
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>toString() output
     * </UL>
     *
     * @param args[] command-line parameters
     */
    public static void main(String args[])
    {
        // instantiate class
        TransactionTotals t = new TransactionTotals();
        // output toString()
        System.out.println(t.toString());
    }
}
