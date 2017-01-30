/* ===========================================================================
* Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/pricing/PricingCargo.java /main/16 2011/02/16 09:13:32 cgreene Exp $
 * ===========================================================================
 * NOTES <other useful comments, qualifications, etc.>
 *
 * MODIFIED (MM/DD/YY)
 *    cgreen 02/15/11 - move constants into interfaces and refactor
 *    acadar 06/10/10 - use default locale for currency display
 *    acadar 06/09/10 - XbranchMerge acadar_tech30 from
 *                      st_rgbustores_techissueseatel_generic_branch
 *    cgreen 05/26/10 - convert to oracle packaging
 *    cgreen 04/27/10 - updating deprecated names
 *    acadar 04/05/10 - use default locale for currency and date/time display
 *    abonda 01/03/10 - update header date
 *    acadar 11/03/08 - localization of reason codes for discounts and merging
 *                      to tip
 *
 * ===========================================================================

     $Log:
      11   360Commerce 1.10        4/24/2008 3:38:52 PM   Alan N. Sinton  CR
           29873: Improved comment for method added in this CR.  Changes
           reviewed by Brett Larsen.
      10   360Commerce 1.9         3/3/2008 10:05:53 AM   Alan N. Sinton  CR
           29873: Merge from v12x branch.  Code reviewed by Jack Swan.
      9    360Commerce 1.8         8/9/2007 10:50:40 AM   Ashok.Mondal    CR
           28179 :Fix the alignment problem with manual discount on eJournal.
      8    360Commerce 1.7         6/4/2007 6:01:32 PM    Alan N. Sinton  CR
           26486 - Changes per review comments.
      7    360Commerce 1.6         5/21/2007 9:16:22 AM   Anda D. Cadar   EJ
           changes
      6    360Commerce 1.5         5/14/2007 2:32:57 PM   Alan N. Sinton  CR
           26486 - EJournal enhancements for VAT.
      5    360Commerce 1.4         4/25/2007 8:52:17 AM   Anda D. Cadar   I18N
           merge

      4    360Commerce 1.3         1/22/2006 11:45:16 AM  Ron W. Haight
           removed references to com.ibm.math.BigDecimal
      3    360Commerce 1.2         3/31/2005 4:29:29 PM   Robert Pearse
      2    360Commerce 1.1         3/10/2005 10:24:22 AM  Robert Pearse
      1    360Commerce 1.0         2/11/2005 12:13:24 PM  Robert Pearse
     $
     Revision 1.48.2.1  2004/11/24 20:56:36  cdb
     @scr 7693 Updated to make app more robust when DB is missing required data.

     Revision 1.48  2004/09/27 22:32:04  bwf
     @scr 7244 Merged 2 versions of abstractfinancialcargo.

     Revision 1.47  2004/07/20 22:42:46  dcobb
     @scr 4377 Invalid Reason Code clears markdown fields
     Save the bean model in the cargo and clear the selected reason code.

     Revision 1.46  2004/07/12 20:30:38  rsachdeva
     @scr 6164 NullPointerException in createItemDiscountByAmountStrategy

     Revision 1.45  2004/07/02 00:34:01  cdb
     @scr 5337 Cleanup and Optimization

     Revision 1.44  2004/07/02 00:18:12  cdb
     @scr 5337 Corrected incomplete transaction discount removal in the
     case of "only one discount allowed." Wasn't removing Employee
     Transaction Discounts.

     Revision 1.43  2004/06/30 00:41:58  jriggins
     @scr 5466 Added logic for maintaining original SaleReturnTransactionIfc instances for transactions which contain returns. This is needed in order to update the line item data for the return components of price adjusted line items in the database.

     Revision 1.42  2004/06/15 16:47:21  awilliam
     @scr 5337 able to apply second employee discount no error msg appears

     Revision 1.41  2004/05/05 18:44:53  jriggins
     @scr 4680 Moved Price Adjustment button from Sale to Pricing

     Revision 1.40  2004/03/26 21:18:19  cdb
     @scr 4204 Removing Tabs.

     Revision 1.39  2004/03/22 18:35:05  cdb
     @scr 3588 Corrected some javadoc

     Revision 1.38  2004/03/22 14:52:45  dcobb
     @scr 3911 Feature Enhancement: Markdown
     Code review cleanup.

     Revision 1.37  2004/03/19 23:27:50  dcobb
     @scr 3911 Feature Enhancement: Markdown
     Code review cleanup.

     Revision 1.36  2004/03/19 21:37:23  dcobb
     @scr 3870 Feature Enhancement: Damage Discount
     Damage discount by amount crashed with null pointer.

     Revision 1.35  2004/03/18 23:13:11  cdb
     @scr 3588 A few code review corrections Dan felt responsible for.

     Revision 1.34  2004/03/16 18:30:46  cdb
     @scr 0 Removed tabs from all java source code.

     Revision 1.33  2004/03/11 22:29:08  dcobb
     @scr 3911 Feature Enhancement: Markdown
     Extracted common code to AbstractAmountEnteredAisle.

     Revision 1.32  2004/03/08 20:03:39  cdb
     @scr 3588 One last update to prorating algorithm

     Revision 1.31  2004/03/04 19:53:17  dcobb
     @scr 3911 Feature Enhancement: Markdown
     Clear markdown items.

     Revision 1.30  2004/03/03 23:03:14  dcobb
     @scr 3911 Feature Enhancement: Markdown
     Set Accounting method markdown.

     Revision 1.29  2004/03/03 21:03:45  cdb
     @scr 3588 Added employee transaction discount service.

     Revision 1.28  2004/02/26 21:23:33  cdb
     @scr 3588 One more refinement of Prorate algorithm.

     Revision 1.27  2004/02/26 18:26:20  cdb
     @scr 3588 Item Discounts no longer have the Damage
     selection. Use the Damage Discount flow to apply Damage
     Discounts.

     Revision 1.26  2004/02/25 23:17:44  cdb
     @scr 3588 Refactored prorating algorithm.

     Revision 1.25  2004/02/25 22:51:41  dcobb
     @scr 3870 Feature Enhancement: Damage Discounts

     Revision 1.24  2004/02/25 20:40:54  cdb
     @scr 3588 Corrected a problem with prorating calculations.

     Revision 1.23  2004/02/25 18:59:04  cdb
     @scr 3588 Corrected a problem with return items.

     Revision 1.22  2004/02/24 22:36:29  cdb
     @scr 3588 Added ability to check for previously existing
     discounts of the same type and capture the prorate user
     selection. Also migrated item discounts to validate in
     the percent and amount entered aisle to be consistent
     with employee discounts.

     Revision 1.21  2004/02/24 00:50:40  cdb
     @scr 3588 Provided for Transaction Discounts to remove
     previously existing discounts if they Only One Discount is allowed.

     Revision 1.20  2004/02/21 18:30:39  cdb
     @scr 3588 Updated prorating algorithm to actually prorate discounts.

     Revision 1.19  2004/02/20 22:25:22  cdb
     @scr 3588 Added discount allowed checking for item discounts.

     Revision 1.18  2004/02/20 21:03:12  cdb
     @scr 3588 Removed development log entries in header,
     updated to more properly handle non-prorated discounts.


     Revision 1.3  2004/02/12 16:51:34  mcs
     Forcing head revision

     Revision 1.2  2004/02/11 21:52:05  rhafernik
     @scr 0 Log4J conversion and code cleanup

     Revision 1.1.1.1  2004/02/11 01:04:19  cschellenger
     updating to pvcs 360store-current


 *
 *    Rev 1.1   Oct 17 2003 10:13:08   bwf
 * Added employeeDiscountID.
 * Resolution for 3412: Feature Enhancement: Employee Discount
 *
 *    Rev 1.0   Aug 29 2003 16:05:18   CSchellenger
 * Initial revision.
 *
 *    Rev 1.2   Jul 10 2003 17:04:22   bwf
 * Added check for mardown reason codes.
 * Resolution for 2678: Markdown Reason Codes not displaying correct information in Pricing
 *
 *    Rev 1.1   Jan 13 2003 15:47:58   RSachdeva
 * Replaced AbstractFinancialCargo.getCodeListMap()   by UtilityManagerIfc.getCodeListMap()
 * Resolution for POS SCR-1907: Remove deprecated calls to AbstractFinancialCargo.getCodeListMap()
 *
 *    Rev 1.0   02 May 2002 17:39:16   jbp
 * Initial revision.
 * Resolution for POS SCR-1626: Pricing Feature

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package oracle.retail.stores.pos.services.pricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.ItemDiscountByAmountIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountByAmountIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountByPercentageIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.utility.CodeListIfc;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.ifc.JournalManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.journal.JournalFormatterManagerIfc;
import oracle.retail.stores.pos.services.common.AbstractFinancialCargo;
import oracle.retail.stores.pos.services.sale.SaleCargo;
import oracle.retail.stores.pos.ui.beans.DecimalWithReasonBeanModel;
import oracle.retail.stores.utility.I18NConstantsIfc;
import oracle.retail.stores.utility.I18NHelper;
import oracle.retail.stores.utility.JournalConstantsIfc;

import org.apache.log4j.Logger;

/**
 * This class represents the cargo for Pricing services.
 * 
 * @version $Revision: /main/16 $
 */
public class PricingCargo extends AbstractFinancialCargo implements DiscountCargoIfc
{
    private static final long serialVersionUID = -2439853662433610685L;

    /** The logger to which log messages will be sent. **/
    protected static final Logger logger = Logger.getLogger(PricingCargo.class);

    /** line item **/
    protected SaleReturnLineItemIfc item = null;

    /** transaction - sale or return **/
    protected RetailTransactionIfc transaction = null;

    /** discount type **/
    protected int discountType = 0;

    /** line items **/
    protected SaleReturnLineItemIfc[] items;

    /** line item index **/
    protected int index;

    /** line item indices **/
    protected int[] indices;

    /** employee discount id number **/
    protected String employeeDiscountID = null;

    /** captures if user has chosen to prorate item discount by amount **/
    protected boolean prorateDiscountByAmount = false;

    /** captures if user has chosen to prorate but selected items
        are a mix of sell and return items **/
    protected boolean containsSellAndReturnItems = false;

    /** List of validated discounts **/
    protected HashMap validatedDiscountHashMap = new HashMap();

    /** Flag for enabling/disabling the price adjustment button **/
    protected boolean isPriceAdjustmentButtonEnabled = false;
    /**
     * Contains the original transaction data for those transactions containing return items. So
     * far this only applies to the return components of price adjusted line items.
     */
    protected Vector originalReturnTransactions;

    /** the DecimalWithReasonBeanModel  **/
    protected DecimalWithReasonBeanModel decimalWithReasonBeanModel = null;

    /**
     * Localized Reason Code
     */
    protected LocalizedCodeIfc selectedReasonCode = DomainGateway.getFactory().getLocalizedCode();

    /**
     * The Code List
     */
    protected CodeListIfc priceOverrideCodeList = null;

    /**
     * Manual item discount amount reason codes
     */
    protected CodeListIfc localizedDiscountAmountCodeList = null;

    /**
     * Manual item discount percent reason codes
     *
     */
    protected CodeListIfc localizedDiscountPercentCodeList =  null;

    /**
     * Markdown percent reason codes
     */
    protected CodeListIfc localizedMarkdownPercentCodeList = null;

    /**
     * Damage Discount reason codes
     */
    protected CodeListIfc localizedDamageDiscountCodeList =  null;

    /**
     * Markdown discount reason codes
     */
    protected CodeListIfc localizedMarkdownAmountCodeList = null;

    /**
     * Sets line item.
     * 
     * @param value line item
     */
    public void setItem(SaleReturnLineItemIfc value)
    {
        item = value;
    }

    /**
     * Retrieves line item.
     * 
     * @return line item
     */
    public SaleReturnLineItemIfc getItem()
    {
        return item;
    }

    /**
     * Retrieves line-item price-override reason code list.
     * 
     * @return line-item price-override reason code list
     */
    public CodeListIfc getPriceOverrideCodeList()
    {
        return priceOverrideCodeList;
    }

    /**
     * Sets the price override code list
     */
    public void setPriceOverrideCodeList(CodeListIfc codeList)
    {
        priceOverrideCodeList = codeList;
    }

    /**
     * Retrieves transaction identifier.
     * 
     * @return transaction identifier
     */
    public String getTransactionID()
    {
        String id = "";
        if (transaction != null)
        {
            id = transaction.getTransactionID();
        }
        return (id);
    }

    /**
     * Sets transaction.
     * 
     * @param transaction the retail transaction
     */
    public void setTransaction(RetailTransactionIfc transaction)
    {
        this.transaction = transaction;
    }

    /**
     * Returns the current retail transaction.
     * 
     * @return transaction the retail transaction
     */
    public RetailTransactionIfc getTransaction()
    {
        return this.transaction;
    }

    /**
     * Returns discountType
     * 
     * @return discountType discount type
     */
    public int getDiscountType()
    {
        return this.discountType;
    }

    /**
     * Sets discountType.
     * 
     * @param discountType the discount type
     */
    public void setDiscountType(int discountType)
    {
        this.discountType = discountType;
    }

    /**
     * Retrieves line-item discount-by-amount reason code list.
     * 
     * @return line-item discount-by-amount reason code list
     */
    public CodeListIfc getLocalizedDiscountAmountCodeList()
    {

        return localizedDiscountAmountCodeList;
    }

    /**
     * Sets the line-item discount-by-amount reason code list.
     * 
     * @return line-item discount-by-amount reason code list
     */
    public void setLocalizedDiscountAmountCodeList(CodeListIfc code)
    {
        localizedDiscountAmountCodeList = code;
    }

    /**
     * Retrieves line-item discount-by-percent reason code list.
     * 
     * @return line-item discount-by-percent reason code list
     */
    public CodeListIfc getLocalizedDiscountPercentCodeList()
    {
        return localizedDiscountPercentCodeList;
    }

    /**
     * Sets the line-item discount-by-percent reason code list.
     * 
     * @return line-item discount-by-percent reason code list
     */
    public void setLocalizedDiscountPercentCodeList(CodeListIfc list)
    {
        localizedDiscountPercentCodeList = list;
    }

    /**
     * Retrieves markdown-by-amount reason code list.
     * 
     * @return line-item discount-by-amount reason code list
     */
    public CodeListIfc getLocalizedMarkdownAmountCodeList()
    {
        return localizedMarkdownAmountCodeList;
    }

    /**
     * Retrieves markdown-by-amount reason code list.
     * 
     * @param line-item discount-by-amount reason code list
     */
    public void setLocalizedMarkdownAmountCodeList(CodeListIfc list)
    {
        localizedMarkdownAmountCodeList = list;
    }

    /**
     * Retrieves markdown-by-percent reason code list.
     * 
     * @return line-item discount-by-percent reason code list
     */
    public CodeListIfc getLocalizedMarkdownPercentCodeList()
    {

        return localizedMarkdownPercentCodeList;
    }

    /**
     * Sets the markdown-by-percent reason code list.
     * 
     * @return line-item discount-by-percent reason code list
     */
    public void setLocalizedMarkdownPercentCodeList(CodeListIfc list)
    {
        localizedMarkdownPercentCodeList = list;
    }

    /**
     * Retrieves list of line items .
     * 
     * @return list of line items
     */
    public SaleReturnLineItemIfc[] getItems()
    {
        return items;
    }

    /**
     * Sets list of line items.
     * 
     * @param values of list of line item
     */
    public void setItems(SaleReturnLineItemIfc[] values)
    {
        items = values;
    }

    /**
     * Retrieves line-item indices.
     * 
     * @return line-item indices
     */
    public int[] getIndices()
    {
        return (indices);
    }

    /**
     * Sets line-item indices.
     * 
     * @param values line-item indices
     */
    public void setIndices(int[] values)
    {
        indices = values;
    }

    /**
     * Retrieves line-item index.
     * 
     * @return line-item index
     */
    public int getIndex()
    {
        return (index);
    }

    /**
     * Sets line-item index.
     * 
     * @param value line-item index
     */
    public void setIndex(int value)
    {
        index = value;
    }

    /**
     * Retrieves employee discount id.
     * 
     * @return employeeDiscountID string
     */
    public String getEmployeeDiscountID()
    {
        return employeeDiscountID;
    }

    /**
     * Sets employeeDiscountID.
     * 
     * @param employeeDiscountID string
     */
    public void setEmployeeDiscountID(String employeeDiscountID)
    {
        this.employeeDiscountID = employeeDiscountID;
    }

    /**
     * Retrieves if user has chosen to prorate and selected items are a mix of
     * sell and return items.
     * 
     * @return true if selected items are a mix of sell and return items
     */
    public boolean containsSellAndReturnItems()
    {
        return containsSellAndReturnItems;
    }

    /**
     * Captures if user has chosen to prorate and selected items are a mix of
     * sell and return items.
     * 
     * @param value true if selected items are a mix of sell and return items
     */
    public void setContainsSellAndReturnItems(boolean value)
    {
        containsSellAndReturnItems = value;
    }

    /**
     * Retrieves if user has chosen to prorate item discount by amount.
     * 
     * @return true if discount should be prorated
     */
    public boolean isProrateDiscountByAmount()
    {
        return prorateDiscountByAmount;
    }

    /**
     * captures if user has chosen to prorate item discount by amount.
     * 
     * @param value true if discount should be prorated
     */
    public void setProrateDiscountByAmount(boolean value)
    {
        prorateDiscountByAmount = value;
    }

    /**
     * Retrieves a HashMap of validated discounts.
     * 
     * @return HashMap of validated discounts
     */
    public HashMap getValidDiscounts()
    {
        return validatedDiscountHashMap;
    }

    /**
     * Sets list of validated discounts.
     * 
     * @param value of validated discounts
     */
    public void setValidDiscounts(HashMap value)
    {
        if (value != null)
        {
            validatedDiscountHashMap = value;
        }
        else
        {
            validatedDiscountHashMap.clear();
        }
    }

    /**
     * Determines if the transaction already has existing manual discounts.
     * 
     * @param PricingCargo
     * @return true if the transaction has existing manual discounts, false
     *         otherwise
     */
    public boolean hasExistingManualDiscounts()
    {
        boolean hasExistingManualDiscounts = false;
        // First check the line items for existing manual item discounts
        SaleReturnLineItemIfc[] lineItems = getItems();
        if (lineItems != null && lineItems.length > 0)
        {
            for(int x=0; x < lineItems.length; x++)
            {
                SaleReturnLineItemIfc srli = lineItems[x];
                if (itemDiscountsExist(srli.getItemDiscountsByAmount()) ||
                        itemDiscountsExist(srli.getItemDiscountsByPercentage()))
                {
                    hasExistingManualDiscounts = true;
                    break;
                }
            } // end sale return line item loop
        } // end if lineItems not empty

        // If we didn't find any manual discounts in the line items,
        // try looking for manual employee transaction discounts
        if (!hasExistingManualDiscounts)
        {
            SaleReturnTransactionIfc transaction = (SaleReturnTransactionIfc)getTransaction();
            if (transaction != null)
            {

                TransactionDiscountStrategyIfc[] discountArray =
                    transaction.getTransactionDiscounts(
                            DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT,
                            DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
                if (discountArray != null && discountArray.length > 0)
                {
                    hasExistingManualDiscounts = true;
                }
                else
                {

                    // Then try discounts by percentage
                   discountArray = transaction.getTransactionDiscounts(
                                DiscountRuleConstantsIfc.DISCOUNT_METHOD_PERCENTAGE,
                                DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
                    if (discountArray != null && discountArray.length > 0)
                    {
                        hasExistingManualDiscounts = true;
                    }
                    else
                    {
                        // Try discounts by amount
                        discountArray =
                            transaction.getTransactionDiscounts(
                                    DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT,
                                    DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
                        if (discountArray != null && discountArray.length > 0)
                        {
                            hasExistingManualDiscounts = true;
                        }
                        else
                        {

                            // Then try discounts by percentage
                            discountArray = transaction.getTransactionDiscounts(
                                    DiscountRuleConstantsIfc.DISCOUNT_METHOD_PERCENTAGE,
                                    DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
                            if (discountArray != null && discountArray.length > 0)
                            {
                                hasExistingManualDiscounts = true;
                            }
                        }
                    }
                }
            }
        }
        return hasExistingManualDiscounts;
    }

    /**
         Determines if the array of discount rules has existing manual discounts. <P>
         @param sgyArray Array of discount strategies that may contain manual discounts
         @return true if manual discounts exist in array
     */
    private boolean itemDiscountsExist(ItemDiscountStrategyIfc[] sgyArray)
    {
        boolean discountsExist = false;
        if (sgyArray != null && sgyArray.length > 0)
        {
            for (int i = 0; i < sgyArray.length; i++)
            {
                ItemDiscountStrategyIfc discount = sgyArray[i];
                // checks that the discount is not APR and
                // that the discount matches the operation.
                if (!discount.isAdvancedPricingRule() &&
                    discount.getTypeCode() == DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM)
                {
                    discountsExist = true;
                    break;
                }
            } // end discount rule loop
        } // end if sgyArray is not empty
        return discountsExist;
    }

    /**
         Removes manual discounts associated with the line sale return line item array.
         Journals the removal of those discounts if journal is not null <P>
         @param lineItems Array of SaleReturnLineItems with possible discounts to remove
         @param journal JournalManager to journal removal of discounts
     */
    private void removeAllManualDiscounts(SaleReturnLineItemIfc[] lineItems, JournalManagerIfc journal)
    {
        JournalFormatterManagerIfc formatter =
            (JournalFormatterManagerIfc)Gateway.getDispatcher().getManager(JournalFormatterManagerIfc.TYPE);
        for (int x = 0; lineItems != null && x < lineItems.length; x++)
        {
            SaleReturnLineItemIfc srli = lineItems[x];
            //new condition added by Dharmendra to fix POS-285
            if (srli != null&&!srli.isReturnLineItem())
            {
            	
                if (journal != null)
                {
                    // Journal the removal of item discounts by amount (includes Markdowns)
                    ItemDiscountStrategyIfc[] currentDiscounts = srli.getItemPrice().getItemDiscountsByAmount();
                    if((currentDiscounts != null) && (currentDiscounts.length > 0))
                    {
                        // find the percent discount stategy that is a discount.
                        for (int j = 0; j < currentDiscounts.length; j++)
                        {
                            if (currentDiscounts[j].getAccountingMethod() ==
                                DiscountRuleConstantsIfc.ACCOUNTING_METHOD_DISCOUNT ||
                                currentDiscounts[j].getAccountingMethod() ==
                                    DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN)
                            {
                                // write to the journal
                                journal.journal(getOperator().getEmployeeID(),
                                        getTransactionID(),
                                        formatter.toJournalManualDiscount(srli, currentDiscounts[j], true));
                            }
                        }
                    } // end if current discounts isn't empty - looking for discounts by amount

                    // Journal the removal of item discounts by percentage (includes Markdowns)
                    currentDiscounts = srli.getItemPrice().getItemDiscountsByPercentage();
                    if((currentDiscounts != null) && (currentDiscounts.length > 0))
                    {
                        // find the percent discount stategy that is a discount.
                        for (int j = 0; j < currentDiscounts.length; j++)
                        {
                            if (currentDiscounts[j].getAccountingMethod() ==
                                DiscountRuleConstantsIfc.ACCOUNTING_METHOD_DISCOUNT ||
                                currentDiscounts[j].getAccountingMethod() ==
                                    DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN)
                            {

                                // write to the journal
                                journal.journal(getOperator().getEmployeeID(),
                                        getTransactionID(),
                                        formatter.toJournalManualDiscount(srli, currentDiscounts[j], true));
                            }
                        }
                    } // end if current discounts not empty - looking for discounts by percentage
                } // end if journal is not null

                // Clear Employee discounts
                srli.clearItemDiscountsByAmount(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
                        DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE,
                        false);
                srli.clearItemDiscountsByPercentage(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
                        DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE,
                        false);

                // clear item discounts
                srli.clearItemDiscountsByAmount(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
                        DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL,
                        false);
                srli.clearItemDiscountsByPercentage(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
                        DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL,
                        false);

                // Clear Damage Discounts
                srli.clearItemDiscountsByAmount(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
                        DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL,
                        true);
                srli.clearItemDiscountsByPercentage(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
                        DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL,
                        true);

                // Clear markdowns
                srli.clearItemMarkdownsByAmount(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM);
                srli.clearItemMarkdownsByPercentage(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM);
            } // end if srli not null
        } // end line item for loop
    }

    /**
         Removes manual discounts associated with the line sale return line item. If
         the sale return line item is null, it will remove the discounts from all
         line items in the transaction.
         Journals the removal of those discounts if journal is not null <P>
         @param srli SaleReturnLineItem with possible discounts to remove
         @param journal JournalManager to journal removal of discounts
     */
    public void removeAllManualDiscounts(SaleReturnLineItemIfc srli, JournalManagerIfc journal)
    {
        Locale locale  =LocaleMap.getLocale(LocaleConstantsIfc.JOURNAL);
        SaleReturnLineItemIfc[] lineItems = null;
        // Set the entered Sale Return Line Item in an array of one,
        // or if it's null, get the line items from the transaction
        if (srli == null && getTransaction() != null)
        {
            lineItems = (SaleReturnLineItemIfc[])((SaleReturnTransactionIfc)getTransaction()).getLineItems();
            setItems(lineItems);
        }
        else
        {
            lineItems = new SaleReturnLineItemIfc[]{srli};
        }
        removeAllManualDiscounts(lineItems, journal);

        if (getTransaction() != null)
        {
            // Journal deletion of transaction discounts by amount
            TransactionDiscountStrategyIfc[] discounts1 = ((SaleReturnTransactionIfc)getTransaction()).getTransactionDiscounts(
                    DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT,
                    DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
            TransactionDiscountStrategyIfc[] discounts2 = ((SaleReturnTransactionIfc)getTransaction()).getTransactionDiscounts(
                    DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT,
                    DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
            TransactionDiscountStrategyIfc[] discounts = new TransactionDiscountStrategyIfc[discounts1.length + discounts2.length];
            System.arraycopy(discounts1,0,discounts,0,discounts1.length);
            System.arraycopy(discounts2,0,discounts,discounts1.length,discounts2.length);
            if (discounts != null)
            {
                if (journal != null)
                {
                    for (int i = 0; i < discounts.length; i++)
                    {
                        TransactionDiscountStrategyIfc discount = discounts[i];

                        if (discount instanceof TransactionDiscountByAmountIfc)
                        {
                            TransactionDiscountByAmountIfc discountAmount =
                                (TransactionDiscountByAmountIfc)discount;
                            CurrencyIfc discountCurr = discountAmount.getDiscountAmount();
                            String discountAmountStr =
                                discountCurr.toGroupFormattedString().trim();
                            StringBuffer msg = new StringBuffer();

                            String msgType = "";
                            msg.append(Util.EOL);
                            if (discount.getAssignmentBasis() == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                            {
//                                msg.append("Employee Discount: ");
                            	msgType = JournalConstantsIfc.EMPLOYEE_DISCOUNT;
                            }
                            else
                            {
                            msgType=JournalConstantsIfc.TRANS_DISCOUNT;
                            }
//                            msg.append(Util.SPACES.substring(discountAmountStr.length(), AVAIL_DISCOUNT_LENGTH - 1))
//                            .append(discountAmountStr)
//                            .append(Util.EOL)
//                            .append("  Discount: Amt. Deleted")            //RRNdebug - replaced "Removed"
//                            .append(Util.EOL);

                            Object discountDataArgs[] = {discountAmountStr};

                            String discountType = I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, msgType, discountDataArgs);

                            String discDeleted = I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, JournalConstantsIfc.DISC_DELETED, null);

                            msg.append(discountType)
                            .append(Util.EOL)
                            .append(discDeleted)
                            .append(Util.EOL);

                            if (discount.getAssignmentBasis() == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                            {
//                                msg.append("  Emp. ID.: ");
//                                msg.append(discount.getDiscountEmployeeID());

                                Object empDataArgs[] = {discount.getDiscountEmployeeID()};
                                String empId=I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, JournalConstantsIfc.EMPLOYEE_ID, empDataArgs);

                                msg.append(empId);

                            }
                            else
                            {
                                // This needs to be modified
                            	String reasonCodeText = discountAmount.getReason().getText(locale);
                                Object reasonCodeDataArgs[]={reasonCodeText};


                                String reasonCode=I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, JournalConstantsIfc.DISC_REASON_CODE_LABEL, reasonCodeDataArgs);

                                msg.append(reasonCode);
                            }

                            String str = "";
                            journal.journal(str, str, msg.toString());
                        } // if the discount is a transaction discount by amount
                    } // end discounts for loop
                } // end if journal not null
                // Clear transaction of discounts by amount
                ((SaleReturnTransactionIfc)getTransaction()).clearTransactionDiscounts(DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT,
                        DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
                ((SaleReturnTransactionIfc)getTransaction()).clearTransactionDiscounts(DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT,
                        DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
            } // end if transaction discounts by amount exist

            // Journal deletion of transaction discounts by percentage
            discounts1 = ((SaleReturnTransactionIfc)getTransaction()).getTransactionDiscounts(
                    DiscountRuleConstantsIfc.DISCOUNT_METHOD_PERCENTAGE,
                    DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
            discounts2 = ((SaleReturnTransactionIfc)getTransaction()).getTransactionDiscounts(
                    DiscountRuleConstantsIfc.DISCOUNT_METHOD_PERCENTAGE,
                    DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
            discounts = new TransactionDiscountStrategyIfc[discounts1.length + discounts2.length];
            System.arraycopy(discounts1,0,discounts,0,discounts1.length);
            System.arraycopy(discounts2,0,discounts,discounts1.length,discounts2.length);
            if (discounts != null)
            {
                if (journal != null)
                {
                    for (int i = 0; i < discounts.length; i++)
                    {
                        TransactionDiscountStrategyIfc discount = discounts[i];

                        if (discount instanceof TransactionDiscountByPercentageIfc)
                        {
                            TransactionDiscountByPercentageIfc discountPercent =
                                (TransactionDiscountByPercentageIfc)discount;
                            double discountPercentDbl = 100.0 * discountPercent.getDiscountRate().doubleValue();
                            String discountPercentStr = (new Double(discountPercentDbl)).toString().trim();

                            // journal removal of discount
                            StringBuffer msg = new StringBuffer();
                            msg.append(Util.EOL);
                            String msgType ="";
                            if (discount.getAssignmentBasis() == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                            {
//                                msg.append("Employee Discount: ");
                            	msgType = JournalConstantsIfc.EMPLOYEE_DISCOUNT;
                            }
                            else
                            {
                            msgType=JournalConstantsIfc.TRANS_DISCOUNT;
                            }
//                            msg.append(Util.EOL)
//                            .append("  Discount: ")
//                            .append("(")
//                            .append(discountPercentStr)
//                            .append("%) Deleted")                   //RRNdebug - replaced "Removed"
//                            .append(Util.EOL);
                            Object discountDataArgs[] = {""};

                            String discountType = I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, msgType, discountDataArgs);

                            Object discPercDataArgs[]= {discountPercentStr};

                            String discDeleted = I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, JournalConstantsIfc.DISC_PER_DELETED, discPercDataArgs);


                            msg
                            .append(discountType)
                            .append(Util.EOL)
                            .append(discDeleted)
                            .append(Util.EOL);


                            if (discount.getAssignmentBasis() == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                            {
//                                msg.append("  Emp. ID.: ");
//                                msg.append(discount.getDiscountEmployeeID());

                                Object empDataArgs[] = {discount.getDiscountEmployeeID()};
                                String empId=I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, JournalConstantsIfc.EMPLOYEE_ID, empDataArgs);

                                msg.append(empId);

                            }
                            else
                            {

                                // This needs to be modified
                            	String reasonCodeText = discountPercent.getReason().getText(locale);
                                Object reasonCodeDataArgs[]={reasonCodeText};


                                String reasonCode=I18NHelper.getString(I18NConstantsIfc.EJOURNAL_TYPE, JournalConstantsIfc.DISC_REASON_CODE_LABEL, reasonCodeDataArgs);

                                msg.append(reasonCode);
                            }

                            String str = "";
                            journal.journal(str, str, msg.toString());
                        } // end if discount is transaction discount by percentage
                    } // end discounts for loop
                } // end if journal isn't null

                // Clear transaction of discounts by percentage
                ((SaleReturnTransactionIfc)getTransaction()).clearTransactionDiscounts(DiscountRuleConstantsIfc.DISCOUNT_METHOD_PERCENTAGE,
                        DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
                ((SaleReturnTransactionIfc)getTransaction()).clearTransactionDiscounts(DiscountRuleConstantsIfc.DISCOUNT_METHOD_PERCENTAGE,
                        DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
            } // end if transaction discounts by percentage isn't null
        } // end if a transaction exists
    }

    /**
     * Verifies if the discounts are valid
     * @param maximumDiscountPercent
     * @param lineItems
     * @param totalDiscount
     * @param isOneDiscountAllowed
     * @param assignmentBasis
     * @param reason
     * @param isDamageDiscount
     * @param isMarkdown
     * @return
     */
    public boolean hasInvalidDiscounts(BigDecimal maximumDiscountPercent,
                                       SaleReturnLineItemIfc[] lineItems,
                                       CurrencyIfc totalDiscount,
                                       boolean isOneDiscountAllowed,
                                       int assignmentBasis,
                                       LocalizedCodeIfc reason,
                                       boolean isDamageDiscount,
                                       boolean isMarkdown)
    {
        // return value
        boolean hasInvalidDiscounts = false;

        // If this is an employee discount, it must not be damaged
        if (assignmentBasis == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
        {
            isDamageDiscount = false;
        }

        // Clone SaleReturnLineItems and find total discount of this type
        CurrencyIfc remainingTotal = (CurrencyIfc)totalDiscount.clone();
        remainingTotal.setZero();
        CurrencyIfc remainingDiscountedTotal = (CurrencyIfc)totalDiscount.clone();
        remainingDiscountedTotal.setZero();

        SaleReturnLineItemIfc[] cloneLineItems = new SaleReturnLineItemIfc[lineItems.length];
        for (int x = 0; x < lineItems.length; x++)
        {
            Integer indexInteger = new Integer(x);
            cloneLineItems[x] = (SaleReturnLineItemIfc)lineItems[x].clone();
            if (validatedDiscountHashMap.containsKey(new Integer(x)))
            {
                if (isOneDiscountAllowed)
                {
                    removeAllManualDiscounts(cloneLineItems[x], null);
                }
                else
                {
                    if (isMarkdown)
                    {
                        cloneLineItems[x].clearItemMarkdownsByAmount(DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM);
                    }
                    else
                    {
                        cloneLineItems[x].clearItemDiscountsByAmount(assignmentBasis, isDamageDiscount);
                    }
                }
                if (!isProrateDiscountByAmount())
                {
                    CurrencyIfc itemDiscount = (CurrencyIfc)totalDiscount.clone();
                    // Apply the discount
                    ItemDiscountByAmountIfc currentDiscountStrategy = createItemDiscountByAmountStrategy(itemDiscount,
                                                                                                         assignmentBasis,
                                                                                                         reason,
                                                                                                         isDamageDiscount,
                                                                                                         isMarkdown);
                    cloneLineItems[x].addItemDiscount(currentDiscountStrategy);
                    cloneLineItems[x].calculateLineItemPrice();

                    // If this discount drops the price below zero, remove this discount and
                    // try with the remaining discounts.
                    if((cloneLineItems[x].isSaleLineItem() && cloneLineItems[x].getExtendedDiscountedSellingPrice().signum() < 0) ||
                            (cloneLineItems[x].isReturnLineItem() && cloneLineItems[x].getExtendedDiscountedSellingPrice().signum() > 0))
                    {
                        CurrencyIfc maximumDiscountAmount = cloneLineItems[x].getExtendedSellingPrice().multiply(maximumDiscountPercent.movePointLeft(2));
                        if (itemDiscount.abs().compareTo(maximumDiscountAmount.abs()) > 0)
                        {
                            // Save the ones that violate the maximum discount amount
                            // for the sake of dialogs
                            validatedDiscountHashMap.put(indexInteger, currentDiscountStrategy);
                        }
                        else
                        {
                            validatedDiscountHashMap.remove(indexInteger);
                            hasInvalidDiscounts = true;
                        }
                    }
                    else
                    {
                        validatedDiscountHashMap.put(indexInteger, currentDiscountStrategy);
                    }
                }
                else
                {
                    remainingTotal = remainingTotal.add(cloneLineItems[x].getExtendedSellingPrice().abs());
                    remainingDiscountedTotal = remainingDiscountedTotal.add(cloneLineItems[x].getExtendedDiscountedSellingPrice().abs());
                }
            }
        }

        if (isProrateDiscountByAmount())
        {
            // First sort the line items by extended discounted selling price and
            // find out if there are any sale items
            Vector orderedVector = new Vector();
            boolean hasSaleItems = sortDiscountLineItems(cloneLineItems, orderedVector);
            boolean hasSaleAndReturnItems = false;

            for (int x = 0; x < orderedVector.size(); x++)
            {
                //Get the index
                Integer indexInteger = (Integer)orderedVector.get(x);
                int sortIndex = indexInteger.intValue();

                // discover the current item price
                SaleReturnLineItemIfc clone = cloneLineItems[sortIndex];

                // If we're prorating and we mix sale and return items,
                // we can only apply the discount to the sell items.
                if (clone.isReturnLineItem() && hasSaleItems)
                {
                    hasSaleAndReturnItems = true;
                    validatedDiscountHashMap.remove(indexInteger);
                    hasInvalidDiscounts = hasInvalidDiscounts(maximumDiscountPercent,
                            cloneLineItems,
                            totalDiscount,
                            isOneDiscountAllowed,
                            assignmentBasis,
                            reason,
                            isDamageDiscount,
                            isMarkdown);
                    break;
                }
            }
            if (!hasSaleAndReturnItems)
            {
                CurrencyIfc remainingDiscount = (CurrencyIfc)totalDiscount.clone();
                CurrencyIfc maximumDiscountAmount = remainingTotal.multiply(maximumDiscountPercent.movePointLeft(2));
                if (remainingDiscount.compareTo(maximumDiscountAmount) > 0)
                {
                    for (int x = 0; x < orderedVector.size(); x++)
                    {
                        //Get the index
                        Integer indexInteger = (Integer)orderedVector.get(x);

                        CurrencyIfc itemDiscount = (CurrencyIfc)totalDiscount.clone();
                        // Apply the discount
                        ItemDiscountByAmountIfc currentDiscountStrategy = createItemDiscountByAmountStrategy(itemDiscount,
                                                                                                                assignmentBasis,
                                                                                                                    reason,
                                                                                                                        isDamageDiscount,
                                                                                                                            isMarkdown);
                        validatedDiscountHashMap.put(indexInteger, currentDiscountStrategy);
                    }
                }
                else
                {
                    // Now that we have an list ordered by price, we can start applying the discount
                    int numberOfDiscountsRemaining = validatedDiscountHashMap.size();
                    for (int x = 0; x < orderedVector.size(); x++)
                    {
                        //Get the index
                        Integer indexInteger = (Integer)orderedVector.get(x);
                        int sortIndex = indexInteger.intValue();

                        // discover the current item price
                        SaleReturnLineItemIfc clone = cloneLineItems[sortIndex];
                        CurrencyIfc currentPrice = (CurrencyIfc)clone.getExtendedDiscountedSellingPrice().clone();

                        maximumDiscountAmount = clone.getExtendedSellingPrice().multiply(maximumDiscountPercent.movePointLeft(2));
                        CurrencyIfc itemDiscount = currentPrice.prorate(remainingDiscount, remainingDiscountedTotal);
                        // To save time, check to see if this violates the maximum discount amount
                        // parameter and adjust accoringly - only if prorated.
                        if (itemDiscount.abs().compareTo(maximumDiscountAmount.abs()) > 0)
                        {
                            itemDiscount = maximumDiscountAmount;
                        }

                        // Apply the discount
                        ItemDiscountByAmountIfc currentDiscountStrategy = createItemDiscountByAmountStrategy(itemDiscount,
                                                                                                                 assignmentBasis,
                                                                                                                     reason,
                                                                                                                         isDamageDiscount,
                                                                                                                             isMarkdown);
                        clone.addItemDiscount(currentDiscountStrategy);
                        clone.calculateLineItemPrice();

                        // If this discount drops the price below zero, remove this discount and
                        // try with the remaining discounts.
                        if((clone.isSaleLineItem() && clone.getExtendedDiscountedSellingPrice().signum() < 0) ||
                                (clone.isReturnLineItem() && clone.getExtendedDiscountedSellingPrice().signum() > 0))
                        {
                            // If this is a prorated discount, there's still hope
                            // by applying the discounted price as the new discount. It's going
                            // to be less than the maximum discount amount because of the check
                            // when creating the prorated discount to begin with.
                            clone.clearItemDiscountsByAmount(assignmentBasis, isDamageDiscount);
                            clone.calculateLineItemPrice();
                            itemDiscount = clone.getExtendedDiscountedSellingPrice();

                            currentDiscountStrategy = createItemDiscountByAmountStrategy(itemDiscount,
                                                                                             assignmentBasis,
                                                                                                 reason,
                                                                                                     isDamageDiscount,
                                                                                                         isMarkdown);
                            clone.addItemDiscount(currentDiscountStrategy);
                            clone.calculateLineItemPrice();
                            // This really shouldn't be a possiblity, but we'll handle it just in case.
                            if((clone.isSaleLineItem() && clone.getExtendedDiscountedSellingPrice().signum() < 0) ||
                                    (clone.isReturnLineItem() && clone.getExtendedDiscountedSellingPrice().signum() > 0))
                            {
                                validatedDiscountHashMap.remove(indexInteger);
                                hasInvalidDiscounts = true;
                                hasInvalidDiscounts(maximumDiscountPercent,
                                                    cloneLineItems,
                                                    totalDiscount,
                                                    isOneDiscountAllowed,
                                                    assignmentBasis,
                                                    reason,
                                                    isDamageDiscount,
                                                    isMarkdown);
                                break;
                            }
                            else
                            {
                                numberOfDiscountsRemaining--;
                                remainingDiscount = remainingDiscount.subtract(currentDiscountStrategy.getDiscountAmount());
                                remainingTotal = remainingTotal.subtract(currentPrice);
                                remainingDiscountedTotal = remainingDiscountedTotal.subtract(currentPrice);
                                validatedDiscountHashMap.put(indexInteger, currentDiscountStrategy);

                                // If we haven't applied all of the prorated discount amount by the time we've reached
                                // the last item, we have no choice but to acknoledge there are no valid
                                // discount combinations.
                                if (numberOfDiscountsRemaining == 0 && remainingDiscount.signum() != 0)
                                {
                                    validatedDiscountHashMap.clear();
                                    hasInvalidDiscounts = true;
                                    break;
                                }
                            }
                        }
                        else // Easy success - the first discount didn't drive any prices negative
                        {
                            validatedDiscountHashMap.put(indexInteger, currentDiscountStrategy);

                            // We need to figure out how much is actually remaining after the
                            // discount has been applied
                            remainingDiscount = remainingDiscount.subtract(currentDiscountStrategy.getDiscountAmount());
                            remainingTotal = remainingTotal.subtract(currentPrice);
                            remainingDiscountedTotal = remainingDiscountedTotal.subtract(currentPrice);
                            numberOfDiscountsRemaining--;

                            // If we're out of items and have discount left over, we can't proceed.
                            // There's no valid discount combination.
                            if (numberOfDiscountsRemaining == 0 && remainingDiscount.signum() != 0)
                            {
                                validatedDiscountHashMap.clear();
                                hasInvalidDiscounts = true;
                                break;
                            }
                        } // else - if final price is negative

                    } // for
                }
            }
        }
        return hasInvalidDiscounts;
    }


    /**
        Sorts the line items by extended discounted selling price.
        Indicates if sale items are present.
        @param  lineItems Array of SaleReturnLineItems
        @param  orderedVector ordered list of discounts
        @return true if array contains any sale items
     */
    protected boolean sortDiscountLineItems(SaleReturnLineItemIfc[] lineItems, Vector orderedVector)

    {
        boolean hasSaleItems = false;

        // Sort the discounts according to their price.
        Integer indexInteger = null;
        for (Iterator i = validatedDiscountHashMap.keySet().iterator(); i.hasNext();)
        {
            indexInteger = (Integer)i.next();
            int index = indexInteger.intValue();
            SaleReturnLineItemIfc srli = lineItems[index];
            if (srli.isSaleLineItem())
            {
                hasSaleItems = true;
            }

            // Have to add the first element to get the ball rolling
            if (0 == orderedVector.size())
            {
                orderedVector.add(indexInteger);
            }
            else
            {
                int startSize = orderedVector.size();
                boolean inserted = false;
                for (int x = 0; !inserted && x < startSize; x++)
                {
                    int sortIndex = ((Integer)orderedVector.elementAt(x)).intValue();
                    if (lineItems[sortIndex].getExtendedDiscountedSellingPrice().abs()
                            .compareTo(srli.getExtendedDiscountedSellingPrice().abs()) > 0 )
                    {
                        orderedVector.add(x, indexInteger);
                        inserted = true;
                    }
                    else if( x + 1 == orderedVector.size())
                    {
                        orderedVector.add(indexInteger);
                        inserted = true;
                    }
                } // for - ordered hash additions
            } // else
        } // for - discount hash iteration
        return hasSaleItems;
    }

    /**
     * Creates discount strategy.
     * 
     * @param discount CurrencyIfc of discount amount
     * @param assignmentBasis Assignment basis of discount
     * @param reason Reason code ID or name
     * @param isDamaged True if this is to be a damage discount
     * @param isMarkdown True if this is to be a Markdown
     * @return The newly created ItemDiscountByAmount
     */
    public ItemDiscountByAmountIfc createItemDiscountByAmountStrategy(CurrencyIfc discount,
                                                                      int assignmentBasis,
                                                                      LocalizedCodeIfc reason,
                                                                      boolean isDamaged,
                                                                      boolean isMarkdown)
    {

        ItemDiscountByAmountIfc sgy = DomainGateway.getFactory().getItemDiscountByAmountInstance();
        sgy.setDiscountAmount(discount);
        sgy.setReason(reason);
        //for manual discounts the name of the discount is the same as the reason code text
        sgy.setLocalizedNames(reason.getText());
        sgy.setMarkdownFlag(isMarkdown);
        sgy.setAssignmentBasis(assignmentBasis);
        if (assignmentBasis == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
        {
            sgy.setDiscountEmployee(getEmployeeDiscountID());
            sgy.setDamageDiscount(false);
        }
        else
        {
            sgy.setDamageDiscount(isDamaged);
            if (!isDamaged)
            {
                sgy.setReason(reason);
            }
        }
        if (isMarkdown)
        {
            sgy.setAccountingMethod(DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN);
        }

        return sgy;
    }

    /**
     * Returns true if only one discount is allowed per item.
     * 
     * @param pm The parameter manager
     * @param logger Used for logging purposes
     * @return true if only one discount is allowed per item, false otherwise
     */
    public boolean isOnlyOneDiscountAllowed(ParameterManagerIfc pm, Logger logger)
    {
        boolean isOnlyOneDiscount = false;
        String parameterValue = "";

        // retrieve Maximum Number of Discounts allowed from parameter file
        try
        {
            parameterValue = pm.getStringValue(PricingCargo.MAX_DISCOUNTS_ALLOWED);
            parameterValue.trim();
            if (PricingCargo.ONE_TOTAL.equals(parameterValue))
            {
                isOnlyOneDiscount = true;
            }
            else if (!PricingCargo.ONE_OF_EACH_TYPE.equals(parameterValue))
            {
                logger.error(
                        "Parameter read: "
                        + PricingCargo.MAX_DISCOUNTS_ALLOWED
                        + "=[" + parameterValue + "]");
            }
        }
        catch (ParameterException e)
        {
            logger.error(Util.throwableToString(e));
        }

        return isOnlyOneDiscount;
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return String representation of object
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append("Class: " + getClass() + Util.EOL);

        return(sb.toString());
    }

    /**
     * @return Returns the isPriceAdjustmentButtonEnabled flag
     */
    public boolean isPriceAdjustmentButtonEnabled()
    {
        return isPriceAdjustmentButtonEnabled;
    }

    /**
     * @param isPriceAdjustmentButtonEnabled Flag to set
     */
    public void setPriceAdjustmentButtonEnabled(boolean isPriceAdjustmentButtonEnabled)
    {
        this.isPriceAdjustmentButtonEnabled = isPriceAdjustmentButtonEnabled;
    }

    /*
     * These methods dealing with the original return transactions list were copied from
     * oracle.retail.stores.pos.services.sale.SaleCargo
     */

    /**
     * Retrieve the array of transactions on which items have been returned.
     * This cargo does not track this data.
     * 
     * @return SaleReturnTransactionIfc[]
     */
    public SaleReturnTransactionIfc[] getOriginalReturnTransactions()
    {
        SaleReturnTransactionIfc[] transactions = new SaleReturnTransactionIfc[0];

        if (originalReturnTransactions != null)
        {
            transactions = new SaleReturnTransactionIfc[originalReturnTransactions.size()];
            originalReturnTransactions.copyInto(transactions);
        }
        return transactions;
    }

    /**
     * Sets the array of transactions on which items have been returned. This
     * cargo does not track this data.
     * 
     * @param origTxns retrieved return transactions
     */
    public void setOriginalReturnTransactions(SaleReturnTransactionIfc[] origTxns)
    {
        originalReturnTransactions = new Vector();
        for(int i = 0; (origTxns != null) && (i < origTxns.length); i++)
        {
            originalReturnTransactions.addElement(origTxns[i]);
        }
    }

    /**
     * Retrieve the array of transactions on which items have been returned.
     * This cargo does not track this data.
     */
    public void resetOriginalReturnTransactions()
    {
        originalReturnTransactions = null;
    }

    /**
     * Add a transaction to the vector of transactions on which items have been returned.
     * This cargo does not track this data.
     * @param transaction SaleReturnTransactionIfc
     */
    public void addOriginalReturnTransaction(SaleReturnTransactionIfc transaction)
    {
        // check to see if an array already exist; if not make one.
        if (originalReturnTransactions == null)
        {
            originalReturnTransactions = new Vector();
        }
        else
        {
            // Check to see if this transaction is already in the array.
            // if so, remove the current reference.
            int size = originalReturnTransactions.size();
            for(int i = 0; i < size; i++)
            {
                SaleReturnTransactionIfc temp =
                    (SaleReturnTransactionIfc)originalReturnTransactions.elementAt(i);
                if (SaleCargo.areTransactionIDsTheSame(temp, transaction))
                {
                    originalReturnTransactions.removeElementAt(i);
                    // Stop the loop.
                    i = size;
                }
            }
        }
        originalReturnTransactions.addElement(transaction);
    }

    /**
     * Returns the DecimalWithReasonBeanModel
     * 
     * @return DecimalWithReasonBeanModel
     */
    public DecimalWithReasonBeanModel getDecimalWithReasonBeanModel()
    {
        return decimalWithReasonBeanModel;
    }

    /**
     * Sets the DeciamlWithReasonBeanModel
     * 
     * @param value DecimalWithReasonBeanModel
     */
    public void setDecimalWithReasonBeanModel(DecimalWithReasonBeanModel value)
    {
        decimalWithReasonBeanModel = value;
    }

    /**
     * Gets the localized reason code
     * 
     * @return LocalizedCodeIfc
     */
    public LocalizedCodeIfc getSelectedReasonCode()
    {
        return selectedReasonCode;
    }

    /**
     * Sets the localized reason code
     * 
     * @param LocalizedCodeIfc
     */
    public void setSelectedReasonCode(LocalizedCodeIfc reasonCode)
    {
        selectedReasonCode = reasonCode;
    }
}
