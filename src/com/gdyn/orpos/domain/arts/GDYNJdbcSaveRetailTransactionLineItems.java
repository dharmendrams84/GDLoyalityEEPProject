//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.common.sql.SQLUpdateStatement;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcSaveRetailTransactionLineItems;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.discount.PromotionLineItemIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.stock.UnknownItemIfc;
import oracle.retail.stores.domain.tax.TaxConstantsIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.utility.GiftCardIfc;
import oracle.retail.stores.domain.utility.SecurityOverrideIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.device.EncipheredCardDataIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;
import com.gdyn.orpos.domain.utility.GiftCard.GDYNGiftCard;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

/**
 * Extending the base JdbcSaveRetailTransactionLineItems for Groupe Dynamite
 * - Tax Exemptions
 * 
 * @author mlawrence
 * 
 */
public class GDYNJdbcSaveRetailTransactionLineItems extends JdbcSaveRetailTransactionLineItems implements
        GDYNARTSDatabaseIfc
{

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 7424673133588786165L;

    /**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(GDYNJdbcSaveRetailTransactionLineItems.class);

    /**
     * Saves the transaction tax information
     * 
     * @param dataConnection
     *            Data Source
     * @param transaction
     *            The retail transaction
     * @param lineItemSequenceNumber
     *            The current sequence number of the line
     *            items
     * @exception DataException
     *                upon error
     */
    public void saveTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            int lineItemSequenceNumber) throws DataException
    {
        try
        {
            insertTaxLineItem(dataConnection, transaction, lineItemSequenceNumber);
        }
        catch (DataException e)
        {
            updateTaxLineItem(dataConnection, transaction, lineItemSequenceNumber);
        }

        if (transaction.getTransactionTax().getTaxMode() == TaxIfc.TAX_MODE_EXEMPT
                || transaction.getTransactionTax().getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
        {
            /*
             * If it's tax exempt, add a Tax Exemption Modififier
             */
            try
            {
                insertTaxExemptionModifier(dataConnection, transaction, lineItemSequenceNumber);
            }
            catch (DataException e)
            {
                updateTaxExemptionModifier(dataConnection, transaction, lineItemSequenceNumber);
            }
        }
    }

    /**
     * Insert a SaleReturnLineItem's tax information into the database. This
     * save the tax rule that the taxInfo is applying.
     * 
     * @param dataConnection
     *            Connection to the database
     * @param transaction
     *            Transaction this line item is part of
     * @param lineItem
     *            The lien item being saved
     * @param taxInfo
     *            Information about tax charged on that line item
     * @throws DataException
     *             If database save fails.
     */
    public void insertSaleReturnTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem, TaxInformationIfc taxInfo) throws DataException
    {
        SQLInsertStatement sql = new SQLInsertStatement();

        // Table
        sql.setTable(TABLE_SALE_RETURN_TAX_LINE_ITEM);

        // Fields
        sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(transaction));
        sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(transaction));
        sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER, getTransactionSequenceNumber(transaction));
        sql.addColumn(FIELD_BUSINESS_DAY_DATE, getBusinessDayString(transaction));
        sql.addColumn(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER, getLineItemSequenceNumber(lineItem));
        sql.addColumn(FIELD_TAX_AUTHORITY_ID, taxInfo.getTaxAuthorityID());
        sql.addColumn(FIELD_TAX_GROUP_ID, taxInfo.getTaxGroupID());
        sql.addColumn(FIELD_TAX_TYPE, taxInfo.getTaxTypeCode());
        sql.addColumn(FIELD_TAX_HOLIDAY, makeStringFromBoolean(taxInfo.getTaxHoliday()));
        sql.addColumn(FIELD_TAXABLE_SALE_RETURN_AMOUNT, taxInfo.getTaxableAmount().toString());
        sql.addColumn(FIELD_SALE_RETURN_TAX_AMOUNT, taxInfo.getTaxAmount().toString());
        sql.addColumn(FIELD_ITEM_TAX_AMOUNT_TOTAL, getItemTaxAmount(lineItem));
        sql.addColumn(FIELD_ITEM_TAX_INC_AMOUNT_TOTAL, getItemInclusiveTaxAmount(lineItem));
        sql.addColumn(FIELD_TAX_RULE_NAME, makeSafeString(taxInfo.getTaxRuleName()));
        sql.addColumn(FIELD_TAX_PERCENTAGE, String.valueOf(taxInfo.getTaxPercentage().floatValue()));
        sql.addColumn(FIELD_TAX_MODE, taxInfo.getTaxMode());
        sql.addColumn(FIELD_FLG_TAX_INCLUSIVE, makeStringFromBoolean(taxInfo.getInclusiveTaxFlag()));
        if (taxInfo.getUniqueID() != null && !taxInfo.getUniqueID().equals(""))
        {
            sql.addColumn(FIELD_UNIQUE_ID, makeSafeString(taxInfo.getUniqueID()));
        }
        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());
        sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP, getSQLCurrentTimestampFunction());
        
        // Customer Category is located in the Tax Reason Code

        // Tax Exemptions
        sql.addColumn(FIELD_TAX_ID_IMAGE_NAME, getTaxIDImageName(transaction));
        sql.addColumn(FIELD_TAX_EXEMPT_BAND_COUNCIL_REGISTRY, getBandCouncilRegistry(transaction));
        sql.addColumn(FIELD_TAX_ID_EXPIRY_DATE, getTaxIDExpiryDate(transaction));
       //	Added by Monica on 11/5/2015 for issue tax exemption code is applied for the taxable item in the RTLog when exchange is performed
        if(lineItem.isReturnLineItem()&& lineItem.getReturnItem()!=null && (lineItem.getItemTax().getOriginalTaxMode()==TaxConstantsIfc.TAX_MODE_STANDARD))
    	{        
        	sql.addColumn(FIELD_CATEGORY_CODE, null);       	
        }
        else
        {
        	sql.addColumn(FIELD_CATEGORY_CODE, getTaxReasonCode(transaction));        	
        }
        
        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de.toString());
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "insertSaleReturnTaxLineItem", e);
        }
    }

    /**
     * Saves the sale the retail modifiers
     * 
     * @param dataConnection
     *            Data Source
     * @param transaction
     *            The Retail Transaction to save
     * @param lineItem
     *            The sales/return line item
     * @exception DataException
     */
    public void saveRetailPriceModifiers(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem) throws DataException
    {
        int discountSequenceNumber = 0;

        /*
         * See if there is a price override
         */
        if (lineItem.getItemPrice().isPriceOverride())
        {
            try
            {
                insertRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, null);
            }
            catch (DataException e)
            {
                updateRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, null);
            }

            ++discountSequenceNumber;
        }

        ItemDiscountStrategyIfc[] modifiers = lineItem.getItemPrice().getItemDiscounts();
        ItemDiscountStrategyIfc discountLineItem;

        // get number of discounts for loop
        int numDiscounts = 0;
        if (modifiers != null)
        {
            numDiscounts = modifiers.length;
        }

        /*
         * Loop through each line item.
         */
        for (int i = 0; i < numDiscounts; i++)
        {
            discountLineItem = modifiers[i];

            /*
             * In the case of a sale line item, a transaction discount will be written to the Sale
             * Return Price Modifier Table; however, since returns have been converted to Item
             * level discounts they must go into the Retail Price modifier table.
             */
            boolean saveRetailPriceModifier = false;
            if (discountLineItem.getDiscountScope() == DiscountRuleConstantsIfc.DISCOUNT_SCOPE_TRANSACTION)
            {
                if (lineItem.isReturnLineItem())
                {
                    saveRetailPriceModifier = true;
                }
            }
            else
            {
                saveRetailPriceModifier = true;
            }

            if (saveRetailPriceModifier)
            {
                /*
                 * If the insert fails, then try to update the line item
                 */
                try
                {
                    insertRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber,
                            discountLineItem);
                }
                catch (DataException e)
                {
                    updateRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber,
                            discountLineItem);
                }
            }
            else
            {
                /*
                 * If the insert fails, then try to update the line item
                 */
                try
                {
                    insertSaleReturnPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber,
                            discountLineItem);
                }
                catch (DataException e)
                {
                    updateSaleReturnPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber,
                            discountLineItem);
                }
            }

            ++discountSequenceNumber;
        }

        /*code changes done by Dharmendra on 31/08/2016 to fix POS-224*/
        SaleReturnTransactionIfc saleReturnTransaction = (SaleReturnTransactionIfc) transaction;
        String employeeDiscountNumber = saleReturnTransaction.getEmployeeDiscountID();

        // Save the Promotions
        PromotionLineItemIfc[] promotionLineItems = lineItem.getItemPrice().getPromotionLineItems();
        PromotionLineItemIfc promotionLineItem;

        if (promotionLineItems != null && promotionLineItems.length > 0
                // unknown items have a temp price but are not on promotion
                && !(lineItem.getPLUItem() instanceof UnknownItemIfc)
                // price entry required items have an override price but are not on promotion
                && !(lineItem.getPLUItem().getItemClassification().isPriceEntryRequired()))
        {
            for (int sequenceNumber = 0; sequenceNumber < promotionLineItems.length; sequenceNumber++)
            {
                promotionLineItem = promotionLineItems[sequenceNumber];
                try
                {
					logger.info("employeeDiscountNumber in GDYNJdbcSaveRetailTransactionLineItems  "
							+ employeeDiscountNumber);
					if (Util.isEmpty(employeeDiscountNumber)) {
						insertPromotionLineItem(dataConnection, transaction,
								lineItem, promotionLineItem, sequenceNumber);
					}
                }
                catch (DataException e)
                {
                	/*code changes done by Dharmendra on 31/08/2016 to fix POS-224*/
    				if (Util.isEmpty(employeeDiscountNumber)) {

                    updatePromotionLineItem(dataConnection, transaction, lineItem, promotionLineItem);
    				}
                }

            }
        }
        
      
 
 
    }
    

    // Begin GD-49: Develop Employee Discount Module
    // lcatania (Starmount) Mar 7, 2013
    
    /**
     * Inserts a price modifier. Used for item discount information.
     * A line item modifier that reflects a modification of the retail selling
     * price. It is provided by PLU through the application of a predefined
     * price change rule that depends on parameters provided during the sale
     * transaction. Examples of the kinds of parameters provided in this
     * scenario include: number of items purchased, customer (or shopper)
     * affiliation, etc.
     *
     * @param dataConnection Data source connection to use
     * @param transaction The retail transaction
     * @param lineItem sale/return line item
     * @param sequenceNumber The sequence number of the modifier
     * @param discountLineItem optional discount information
     * @exception DataException upon error
     */
    public void insertRetailPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
            throws DataException
    {
        SQLInsertStatement sql = new SQLInsertStatement();

        // Table
        sql.setTable(TABLE_RETAIL_PRICE_MODIFIER);

        // Get the line and transaction number.
        String lineNumber = getLineItemSequenceNumber(lineItem);
        String tranNumber = getTransactionSequenceNumber(transaction);

        // Fields
        sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(transaction));
        sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(transaction));
        sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER, tranNumber);
        sql.addColumn(FIELD_BUSINESS_DAY_DATE, getBusinessDayString(transaction));
        sql.addColumn(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER, lineNumber);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER, getSequenceNumber(sequenceNumber));
        sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP, getSQLCurrentTimestampFunction());
        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());

        if (discountLineItem != null)
        { // Item discount
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, getDiscountRuleID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_PERCENT, getPriceModifierPercent(discountLineItem));
            // The amount value placed in the table must always be positive.
            CurrencyIfc amount = DomainGateway.getBaseCurrencyInstance(getPriceModifierAmount(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, amount.abs().getStringValue());
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_METHOD_CODE, getPriceModifierMethodCode(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE,
                    getPriceModifierAssignmentBasis(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_ID, makeSafeString(discountLineItem
                    .getDiscountEmployeeID()));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DAMAGE_DISCOUNT,
                    getPriceModifierDamageDiscountFlag(discountLineItem));
            sql.addColumn(FIELD_PCD_INCLUDED_IN_BEST_DEAL, getIncludedInBestDealFlag(discountLineItem));
            sql.addColumn(FIELD_ADVANCED_PRICING_RULE, getAdvancedPricingRuleFlag(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID,
                    getPriceModifierReferenceID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID_TYPE_CODE,
                    getPriceModifierReferenceIDTypeCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_TYPE_CODE, discountLineItem.getTypeCode());
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE,
                    inQuotes(discountLineItem.getAccountingMethod()));
            sql.addColumn(FIELD_PROMOTION_ID, discountLineItem.getPromotionId());
            sql.addColumn(FIELD_PROMOTION_COMPONENT_ID, discountLineItem.getPromotionComponentId());
            sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID, discountLineItem.getPromotionComponentDetailId());
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, discountLineItem.getPricingGroupID());
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_NAME, getDiscountEmployeeName(discountLineItem));
        }
        else
        { // Price Override
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, "0");
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(lineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(lineItem));
            // if security override data exists, use it
            SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
            if (priceOverrideAuthorization != null)
            {
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_EMPLOYEE_ID,
                        makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_ENTRY_METHOD_CODE, priceOverrideAuthorization
                        .getEntryMethod().getIxRetailCode());
            }
        }
        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de.toString());
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "insertRetailPriceModifier", e);
        }
    }
    
    
    /**
     * Updates a single price modifier.
     *
     * @param dataConnection Data Source
     * @param transaction The retail transaction
     * @param lineItem the sales/return line item
     * @param sequenceNumber The sequence number of the modifier
     * @param discountLineItem discount
     * @exception DataException upon error
     */
    public void updateRetailPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
            throws DataException
    {
        SQLUpdateStatement sql = new SQLUpdateStatement();

        // Table
        sql.setTable(TABLE_RETAIL_PRICE_MODIFIER);

        // Fields
        if (discountLineItem != null)
        { // Item discount
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, getDiscountRuleID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_PERCENT, getPriceModifierPercent(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_METHOD_CODE, getPriceModifierMethodCode(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE,
                    getPriceModifierAssignmentBasis(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_ID, makeSafeString(discountLineItem
                    .getDiscountEmployeeID()));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DAMAGE_DISCOUNT,
                    getPriceModifierDamageDiscountFlag(discountLineItem));
            sql.addColumn(FIELD_PCD_INCLUDED_IN_BEST_DEAL, getIncludedInBestDealFlag(discountLineItem));
            sql.addColumn(FIELD_ADVANCED_PRICING_RULE, getAdvancedPricingRuleFlag(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID,
                    getPriceModifierReferenceID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID_TYPE_CODE,
                    getPriceModifierReferenceIDTypeCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_TYPE_CODE, discountLineItem.getTypeCode());
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE,
                    inQuotes(discountLineItem.getAccountingMethod()));
            sql.addColumn(FIELD_PROMOTION_ID, discountLineItem.getPromotionId());
            sql.addColumn(FIELD_PROMOTION_COMPONENT_ID, discountLineItem.getPromotionComponentId());
            sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID, discountLineItem.getPromotionComponentDetailId());
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, discountLineItem.getPricingGroupID());
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_NAME, getDiscountEmployeeName(discountLineItem));
        }
        else
        { // Price Override
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, "0");
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(lineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(lineItem));
            // if security override data exists, use it
            SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
            if (priceOverrideAuthorization != null)
            {
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_EMPLOYEE_ID,
                        makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_ENTRY_METHOD_CODE, priceOverrideAuthorization
                        .getEntryMethod().getIxRetailCode());
            }
        }

        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());

        // Qualifiers
        sql.addQualifier(FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
        sql.addQualifier(FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
        sql.addQualifier(FIELD_TRANSACTION_SEQUENCE_NUMBER + " = " + getTransactionSequenceNumber(transaction));
        sql.addQualifier(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER + " = "
                + getLineItemSequenceNumber(lineItem));
        sql.addQualifier(FIELD_BUSINESS_DAY_DATE + " = " + getBusinessDayString(transaction));
        sql.addQualifier(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER + " = " + getSequenceNumber(sequenceNumber));

        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de.toString());
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "updateRetailPriceModifier", e);
        }

        if (0 >= dataConnection.getUpdateCount())
        {
            throw new DataException(DataException.NO_DATA, "Update RetailPriceModifier");
        }
    }
    
    // End GD-49: Develop Employee Discount Module
    

    /**
     * Inserts a Transaction Price Modifier. For now, this table is used for
     * ReSA. In POS, the transaction discounts go to the TR_LTM_DSC table but it
     * is not split up by line item. In order to figure out the discount amount
     * for each line item, a complex calculation must be implemented. It is
     * already implemented in POS, but it is not visible by ReSA (ReSA -export
     * module- doesn't depend on the domain module). Since we already have all
     * the correct amounts in the lineItem object, we can avoid replicating the
     * complex calculation for ReSA by writing the data to this new table.
     * 
     * @param dataConnection
     *            Data Source
     * @param transaction
     *            The retail transaction
     * @param lineItem
     *            the sales/return line item
     * @param sequenceNumber
     *            The sequence number of the modifier
     * @param discountLineItem
     *            discount
     * @exception DataException
     *                upon error
     */
    public void insertSaleReturnPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
            throws DataException
    {
        SQLInsertStatement sql = new SQLInsertStatement();

        // Table
        sql.setTable(TABLE_SALE_RETURN_PRICE_MODIFIER);

        // Get the line and transaction number.
        String lineNumber = getLineItemSequenceNumber(lineItem);
        String tranNumber = getTransactionSequenceNumber(transaction);

        // Fields
        sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(transaction));
        sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(transaction));
        sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER, tranNumber);
        sql.addColumn(FIELD_BUSINESS_DAY_DATE, getBusinessDayString(transaction));
        sql.addColumn(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER, lineNumber);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER, getSequenceNumber(sequenceNumber));
        sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP, getSQLCurrentTimestampFunction());
        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());

        if (discountLineItem != null)
        {
            // Item discount
            int promotionId = 0;
            if (transaction.getTransactionDiscounts() != null && transaction.getTransactionDiscounts().length > 0)
            {
                promotionId = transaction.getTransactionDiscounts()[0].getPromotionId();
            }
            else
            {
                promotionId = discountLineItem.getPromotionId();
            }
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, getDiscountRuleID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_PERCENT, getPriceModifierPercent(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_METHOD_CODE, getPriceModifierMethodCode(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE,
                    getPriceModifierAssignmentBasis(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_ID, makeSafeString(discountLineItem
                    .getDiscountEmployeeID()));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DAMAGE_DISCOUNT,
                    getPriceModifierDamageDiscountFlag(discountLineItem));
            sql.addColumn(FIELD_PCD_INCLUDED_IN_BEST_DEAL, getIncludedInBestDealFlag(discountLineItem));
            sql.addColumn(FIELD_ADVANCED_PRICING_RULE, getAdvancedPricingRuleFlag(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID,
                    getPriceModifierReferenceID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID_TYPE_CODE,
                    getPriceModifierReferenceIDTypeCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_TYPE_CODE, discountLineItem.getTypeCode());
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE,
                    inQuotes(discountLineItem.getAccountingMethod()));
            sql.addColumn(FIELD_PROMOTION_ID, promotionId);
            sql.addColumn(FIELD_PROMOTION_COMPONENT_ID, discountLineItem.getPromotionComponentId());
            sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID, discountLineItem.getPromotionComponentDetailId());
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, discountLineItem.getPricingGroupID());
        }
        else
        {
            // Price Override
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, "0");
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(lineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(lineItem));
            // if security override data exists, use it
            SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
            if (priceOverrideAuthorization != null)
            {
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_EMPLOYEE_ID,
                        makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_ENTRY_METHOD_CODE, priceOverrideAuthorization
                        .getEntryMethod().getIxRetailCode());
            }
        }
        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de.toString());
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "Insert SaleReturnPriceModifier", e);
        }
    }

    /**
     * Updates a Transaction Price Modifier. For now, this table is used for
     * ReSA. In POS, the transaction discounts go to the TR_LTM_DSC table but it
     * is not split up by line item. In order to figure out the discount amount
     * for each line item, a complex calculation must be implemented. It is
     * already implemented in POS, but it is not visible by ReSA (ReSA -export
     * module- doesn't depend on the domain module). Since we already have all
     * the correct amounts in the lineItem object, we can avoid replicating the
     * complex calculation for ReSA by writing the data to this new table.
     * 
     * @param dataConnection
     *            Data Source
     * @param transaction
     *            The retail transaction
     * @param lineItem
     *            the sales/return line item
     * @param sequenceNumber
     *            The sequence number of the modifier
     * @param discountLineItem
     *            discount
     * @exception DataException
     *                upon error
     */
    public void updateSaleReturnPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
            throws DataException
    {
        SQLUpdateStatement sql = new SQLUpdateStatement();

        // Table
        sql.setTable(TABLE_SALE_RETURN_PRICE_MODIFIER);

        // Fields
        if (discountLineItem != null)
        {
            // Item discount
            int promotionId = 0;
            if (transaction.getTransactionDiscounts() != null && transaction.getTransactionDiscounts().length > 0)
            {
                promotionId = transaction.getTransactionDiscounts()[0].getPromotionId();
            }
            else
            {
                promotionId = discountLineItem.getPromotionId();
            }
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, getDiscountRuleID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_PERCENT, getPriceModifierPercent(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_METHOD_CODE, getPriceModifierMethodCode(discountLineItem));
            sql.addColumn(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE,
                    getPriceModifierAssignmentBasis(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_ID, makeSafeString(discountLineItem
                    .getDiscountEmployeeID()));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DAMAGE_DISCOUNT,
                    getPriceModifierDamageDiscountFlag(discountLineItem));
            sql.addColumn(FIELD_PCD_INCLUDED_IN_BEST_DEAL, getIncludedInBestDealFlag(discountLineItem));
            sql.addColumn(FIELD_ADVANCED_PRICING_RULE, getAdvancedPricingRuleFlag(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID,
                    getPriceModifierReferenceID(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID_TYPE_CODE,
                    getPriceModifierReferenceIDTypeCode(discountLineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_TYPE_CODE, discountLineItem.getTypeCode());
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE,
                    inQuotes(discountLineItem.getAccountingMethod()));
            sql.addColumn(FIELD_PROMOTION_ID, promotionId);
            sql.addColumn(FIELD_PROMOTION_COMPONENT_ID, discountLineItem.getPromotionComponentId());
            sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID, discountLineItem.getPromotionComponentDetailId());
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, discountLineItem.getPricingGroupID());
        }
        else
        { // Price Override
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID, "0");
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE, getPriceModifierReasonCode(lineItem));
            sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT, getPriceModifierAmount(lineItem));
            // if security override data exists, use it
            SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
            if (priceOverrideAuthorization != null)
            {
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_EMPLOYEE_ID,
                        makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
                sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_ENTRY_METHOD_CODE, priceOverrideAuthorization
                        .getEntryMethod().getIxRetailCode());
            }
        }

        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());

        // Qualifiers
        sql.addQualifier(FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
        sql.addQualifier(FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
        sql.addQualifier(FIELD_TRANSACTION_SEQUENCE_NUMBER + " = " + getTransactionSequenceNumber(transaction));
        sql.addQualifier(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER + " = "
                + getLineItemSequenceNumber(lineItem));
        sql.addQualifier(FIELD_BUSINESS_DAY_DATE + " = " + getBusinessDayString(transaction));
        sql.addQualifier(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER + " = " + getSequenceNumber(sequenceNumber));

        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de.toString());
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "UpdateSaleReturnPriceModifier", e);
        }

        if (0 >= dataConnection.getUpdateCount())
        {
            throw new DataException(DataException.NO_DATA, "Update SaleReturnPriceModifier");
        }
    }

    /**
     * Returns the transaction tax exempt image
     * 
     * @param transaction
     *            The retail transaction
     * @return String taxExemptIdImage
     */
    protected String getTaxIDImageName(RetailTransactionIfc transaction)
    {
        String taxExemptIdImage = null;

        if (transaction != null && transaction instanceof GDYNSaleReturnTransactionIfc)
        {
            GDYNSaleReturnTransactionIfc saleTransaction = (GDYNSaleReturnTransactionIfc) transaction;
            if (saleTransaction.getTransactionTax() != null)
            {
                GDYNTransactionTaxIfc transactionTax = (GDYNTransactionTaxIfc) saleTransaction.getTransactionTax();
                taxExemptIdImage = transactionTax.getTaxExemptIdImageName();
            }
        }
        return makeSafeString(taxExemptIdImage);
    }

    /**
     * Returns the transaction tax exempt band or council registry information
     * 
     * @param transaction
     *            The retail transaction
     * @return String taxExemptBandCouncilRegistry
     */
    protected String getBandCouncilRegistry(RetailTransactionIfc transaction)
    {
        String taxExemptBandCouncilRegistry = null;

        if (transaction != null && transaction instanceof GDYNSaleReturnTransactionIfc)
        {
            GDYNSaleReturnTransactionIfc saleTransaction = (GDYNSaleReturnTransactionIfc) transaction;
            if (saleTransaction.getTransactionTax() != null)
            {
                GDYNTransactionTaxIfc transactionTax = (GDYNTransactionTaxIfc) saleTransaction.getTransactionTax();
                taxExemptBandCouncilRegistry = transactionTax.getBandRegistryId();
            }
        }
        return makeSafeString(taxExemptBandCouncilRegistry);
    }

    /**
     * Returns the transaction tax id expiry date
     * 
     * @param transaction
     *            The retail transaction
     * @return Date taxIDExpiryDate
     */
    protected String getTaxIDExpiryDate(RetailTransactionIfc transaction)
    {
        String taxIDExpiryDateString = null;

        if (transaction != null && transaction instanceof GDYNSaleReturnTransactionIfc)
        {
            GDYNSaleReturnTransactionIfc saleTransaction = (GDYNSaleReturnTransactionIfc) transaction;
            if (saleTransaction.getTransactionTax() != null)
            {
                GDYNTransactionTaxIfc transactionTax = (GDYNTransactionTaxIfc) saleTransaction.getTransactionTax();
                if (transactionTax.getIdExpirationDate() != null)
                {
                    taxIDExpiryDateString = dateToSQLDateString(transactionTax.getIdExpirationDate());
                }
            }
        }
        return taxIDExpiryDateString;
    }

    // Begin GD-49: Develop Employee Discount Module
    // lcatania (Starmount) Mar 7, 2013
    /**
     * Get the employee name for the given Employee Discount
     * 
     * @param discountLineItem
     * @return employeeName
     */
    protected String getDiscountEmployeeName(ItemDiscountStrategyIfc discountLineItem)
    {
        String employeeName = null;
        if (discountLineItem.getDiscountEmployee() != null)
        {
            EmployeeIfc employee = discountLineItem.getDiscountEmployee();
            if (employee.getPersonName() != null)
            {
                employeeName = employee.getPersonName().getFullName();
            }
        }
        return makeSafeString(employeeName);
    }
    // End GD-49: Develop Employee Discount Module
    

    
    // Begin (GD-440) GD_CR 13 - Save Gift Card Authorization and Modify RTLog
    // dmartinez (Starmount) Feb 7, 2014
    /**
     * Inserts a Gift Card. Customized for Group Dynamite.
     * Adding gift card authorization field.
     * 
     * 
     * @param dataConnection Data source connection to use
     * @param transaction The retail transaction
     * @param lineItem The sale/return line item
     * @exception DataException upon error
     */
    public void insertGiftCard(JdbcDataConnection dataConnection, RetailTransactionIfc transaction,
            SaleReturnLineItemIfc lineItem) throws DataException
    {
        // Get gift card
        GiftCardIfc giftCard = ((GiftCardPLUItemIfc) (lineItem.getPLUItem())).getGiftCard();

        SQLInsertStatement sql = new SQLInsertStatement();

        // Table
        sql.setTable(TABLE_GIFT_CARD);

        // Fields
        sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(transaction));
        sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(transaction));
        sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER, getTransactionSequenceNumber(transaction));
        sql.addColumn(FIELD_BUSINESS_DAY_DATE, getBusinessDayString(transaction));
        sql.addColumn(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER, getSequenceNumber(lineItem.getLineNumber()));
        EncipheredCardDataIfc cardData = giftCard.getEncipheredCardData();
        if(cardData != null)
        {
            sql.addColumn(FIELD_GIFT_CARD_SERIAL_NUMBER, inQuotes(cardData.getEncryptedAcctNumber()));
            sql.addColumn(FIELD_MASKED_GIFT_CARD_SERIAL_NUMBER, inQuotes(cardData.getMaskedAcctNumber()));
        }
        sql.addColumn(FIELD_GIFT_CARD_ACTIVATION_ADJUDICATION_CODE, makeSafeString(giftCard.getApprovalCode()));
        sql.addColumn(FIELD_GIFT_CARD_ENTRY_METHOD, getEntryMethod(giftCard.getEntryMethod()));
        sql.addColumn(FIELD_GIFT_CARD_REQUEST_TYPE, inQuotes(String.valueOf(giftCard.getRequestType())));
        if(giftCard.getCurrentBalance() != null)
        {
            sql.addColumn(FIELD_GIFT_CARD_CURRENT_BALANCE, giftCard.getCurrentBalance().getStringValue());
        }
        if(giftCard.getInitialBalance() != null)
        {
            sql.addColumn(FIELD_GIFT_CARD_INITIAL_BALANCE, giftCard.getInitialBalance().getStringValue());
        }
        //+I18N
        sql.addColumn(FIELD_CURRENCY_ID, lineItem.getExtendedSellingPrice().getType().getCurrencyId());
        //-I18N
        sql.addColumn(FIELD_TENDER_AUTHORIZATION_SETTLEMENT_DATA, makeSafeString(giftCard.getSettlementData()));

        String authDateTime = getAuthorizationDateTime(giftCard.getAuthorizedDateTime());
        if(
           (authDateTime == null)    ||
           (authDateTime.equals("")) ||
           (authDateTime.equals("null"))
          )
        {
            sql.addColumn(FIELD_TENDER_AUTHORIZATION_DATE_TIME, getSQLCurrentTimestampFunction());
        }
        else
        {
            sql.addColumn(FIELD_TENDER_AUTHORIZATION_DATE_TIME, authDateTime);
        }

        sql.addColumn(FIELD_TENDER_AUTHORIZATION_JOURNAL_KEY, makeSafeString(giftCard.getJournalKey()));
        
        //Adding new Authorization Number
        if(giftCard instanceof GDYNGiftCard)
        {
            sql.addColumn(FIELD_GIFT_CARD_ADJUDICATION_CODE, makeSafeString(((GDYNGiftCard) giftCard).getAuthorizationNumber()));            
        }
        

        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de.toString());
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "insertGiftCard", e);
        }
    } 
    //End (GD-440) GD_CR 13 

    /*
     * we override this method in order to filter thru not inserting the default tax line item
     */
    public void saveSaleReturnLineItemTaxInformation(JdbcDataConnection dataConnection,
            RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem) throws DataException
    {

        TaxInformationIfc[] taxInfo = lineItem.getTaxInformationContainer().getTaxInformation();
        if (taxInfo != null)
        {
        	for (int i = 0; i < taxInfo.length; i++)
        	{
        		//Mansi: Modified the below code to not to save the tax information in DB when the tax authority is default tax i.e. 0.
        		if(taxInfo[i].getTaxAuthorityID() != 0)
        		{
        			insertSaleReturnTaxLineItem(dataConnection, transaction, lineItem, taxInfo[i]);
        		}
        	}
        }
        // To support reporting tax amount to multiple tax jurisdictions,
        // need to save each individual tax jurisdiction tax amount and the
        // combined tax amount.
        /*
         * Hashtable itemTaxByTaxJurisdiction =
         * lineItem.getItemTax().getTaxByTaxJurisdiction(); if
         * (itemTaxByTaxJurisdiction != null) { if
         * (itemTaxByTaxJurisdiction.size() > 0) { Enumeration authority =
         * itemTaxByTaxJurisdiction.keys(); while (authority.hasMoreElements()) {
         * String authorityID = (String)authority.nextElement(); Vector taxData =
         * (Vector)itemTaxByTaxJurisdiction.get(authorityID); CurrencyIfc
         * taxAmountByTaxAuthority = (CurrencyIfc)taxData.elementAt(0); String
         * taxRuleName = (String)taxData.elementAt(1); BigDecimal taxRate =
         * (BigDecimal)taxData.elementAt(2); int taxAuthorityID =
         * Integer.parseInt(authorityID.substring((authorityID).indexOf(".") +
         * 1, authorityID.length()));
         * insertSaleReturnTaxLineItem(dataConnection, transaction, lineItem,
         * taxAuthorityID, taxAmountByTaxAuthority, taxRuleName, taxRate); } } //
         * End of hashtable size checking }
         */
    }
    
    /**
     * Returns the transaction tax reason code
     *
     * @param transaction The retail transaction
     * @return the tax reason code
     */
    //Ashwini added: if reason code is -1 set category code as null, else set tax reason code
    protected String getTaxReasonCode(RetailTransactionIfc transaction)
    {
    	String taxReasonCode = null;

    	if (transaction != null && transaction instanceof GDYNSaleReturnTransactionIfc)
    	{
    		GDYNSaleReturnTransactionIfc saleTransaction = (GDYNSaleReturnTransactionIfc) transaction;
    		if (saleTransaction.getTransactionTax() != null)
    		{
    			GDYNTransactionTaxIfc transactionTax = (GDYNTransactionTaxIfc) saleTransaction.getTransactionTax();

    			if(transactionTax.getReason().getCode() != null && transactionTax.getReason().getCode().equals("-1") == false)
    			{
    				
    				taxReasonCode = makeSafeString(transactionTax.getReason().getCode());
    			}
    		}
    	}
    	
    	return taxReasonCode;
    }
    	
}
