/* ===========================================================================
 *   Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
 *   All rights reserved.
 *   
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    pwai  01/22/15 - change is made on method setCloneAttributes for GD 559938 - 
 *                      ORPOS Tax Exemption with Retrieved Transaction - incorrect tax amount
 *    
 * ===========================================================================
 */
package com.gdyn.orpos.domain.transaction;

import java.math.BigDecimal;

import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.WorkstationIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransaction;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

import com.gdyn.orpos.domain.lineitem.GDYNItemContainerProxyIfc;
import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;

/**
 * Extend the normal SaleReturnTransaction class to support the customer survey
 * invitation id and url.
 * 
 * @author MSolis
 * 
 */
@SuppressWarnings("unchecked")
public class GDYNSaleReturnTransaction extends SaleReturnTransaction implements
		GDYNSaleReturnTransactionIfc {
	private static final long serialVersionUID = 3309770545928083682L;

	/**
	 * The customer unique invitation code
	 */
	protected String surveyCustomerInviteID;

	/**
	 * Hold coupon refernce number
	 */

	protected String couponReferenceNumber;

	/**
	 * The shortened URL
	 */
	protected String surveyCustomerInviteURL;

	// Name of the Employee on the Employee Discount applied to the transaction.
	protected String employeeDiscountName;

	/**
	 * True if transaction has both sale and return items
	 */
	protected boolean originalExchangeTransaction = false;

	protected TransactionTaxIfc originalTransactionTax;

	/*
	 * Modified by Vivek for Loyalty Enhancement. Creating two variable (Loyalty
	 * and Email ID).
	 */
	protected String loyaltyID = "";

	protected String loyaltyEmailID = "";
	
	protected String originalLoyaltyID ="";
	
	protected String loyaltyPhoneNo = "";
	

	protected boolean isLoyaltyEnable = false;

	public TransactionTaxIfc getOriginalTransactionTax() {
		return this.originalTransactionTax;
	}

	public void setOriginalTransactionTax(
			TransactionTaxIfc originalTransactionTax) {
		this.originalTransactionTax = originalTransactionTax;
	}

	/**
	 * @return the exchangeTransaction
	 */
	public boolean isOriginalExchangeTransaction() {
		return originalExchangeTransaction;
	}

	/**
	 * @param exchangeTransaction
	 *            the exchangeTransaction to set
	 */
	public void setOriginalExchangeTransaction(
			boolean originalExchangeTransaction) {
		this.originalExchangeTransaction = originalExchangeTransaction;
	}

	/**
	 * Call parent constructor to complete the process.
	 */
	public GDYNSaleReturnTransaction() {
		super();

	}

	/**
	 * Constructs GDYNSaleReturnTransaction object.
	 * 
	 * @param station
	 *            The workstation(register) to create a transaction for
	 */
	public GDYNSaleReturnTransaction(WorkstationIfc station) {
		super(station);
	}

	/**
	 * Constructs GDYNSaleReturnTransaction object.
	 * 
	 * @param transactionID
	 *            id for the transaction
	 */
	public GDYNSaleReturnTransaction(String transactionID) {
		super(transactionID);
	}

	/**
	 * Get the Customer Survey Invitation ID.
	 */
	public String getSurveyCustomerInviteID() {
		return this.surveyCustomerInviteID;
	}

	/**
	 * Set the Customer Survey Invitation ID.
	 */
	public void setSurveyCustomerInviteID(String surveyCustomerInviteID) {
		this.surveyCustomerInviteID = surveyCustomerInviteID;
	}

	/**
	 * Get the customer survey invitation URL
	 * 
	 * @return String surveyCustomerInviteURL
	 */
	public String getSurveyCustomerInviteURL() {
		return this.surveyCustomerInviteURL;
	}

	/**
	 * Set the Customer Survey Invitation URL
	 * 
	 * @param String
	 *            surveyCustomerInviteURL
	 */
	public void setSurveyCustomerInviteURL(String surveyCustomerInviteURL) {
		this.surveyCustomerInviteURL = surveyCustomerInviteURL;
	}

	/**
	 * Applies a GDYN customer tax exemption.
	 * 
	 * @param newTax
	 *            a tax object to extract data from
	 */
	public void setCustomerTaxExempt(GDYNTransactionTax newTax) {
		((GDYNItemContainerProxyIfc) itemProxy).setCustomerTaxExempt(newTax);
		// update totals
		updateTransactionTotals(itemProxy.getLineItems(),
				itemProxy.getTransactionDiscounts(),
				itemProxy.getTransactionTax());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc#
	 * clearCustomerTaxExempt()
	 */
	public void clearCustomerTaxExempt() {
		((GDYNItemContainerProxyIfc) itemProxy).clearCustomerTaxExempt();

		updateTransactionTotals(itemProxy.getLineItems(),
				itemProxy.getTransactionDiscounts(),
				itemProxy.getTransactionTax());
	}

	/**
	 * Check to see if the transaction is a tax exempt transaction.
	 * 
	 * @return taxExemptFound
	 */
	public boolean isCustomerTaxExemptTransaction() {
		boolean taxExemptFound = false;

		if (getTransactionTax() != null) {
			taxExemptFound = ((GDYNTransactionTaxIfc) getTransactionTax())
					.hasCustomerTaxExemption();
		}
		return taxExemptFound;
	}

	/**
	 * Checks if the transaction contains any customer tax exempt eligible
	 * items. If there is a customer tax exemption on the transaction and at
	 * least one item that is not non-taxable or has an exception, then this
	 * returns true.
	 */
	public boolean hasTaxExemptEligibleItems() {
		boolean result = false;

		if (isCustomerTaxExemptTransaction()) {
			AbstractTransactionLineItemIfc[] items = itemProxy.getLineItems();

			for (AbstractTransactionLineItemIfc item : items) {
				if (item instanceof SaleReturnLineItemIfc) {
					SaleReturnLineItemIfc srli = (SaleReturnLineItemIfc) item;

					if (srli.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_EXEMPT
							|| srli.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Clones SaleReturnTransaction object using the parent
	 * 
	 * @return instance of SaleReturnTransaction object
	 */
	public Object clone() {
		GDYNSaleReturnTransaction srt = new GDYNSaleReturnTransaction();
		setCloneAttributes(srt);

		return srt;
	}

	/**
	 * Set the attributes for the clone.
	 * 
	 * GDYNSaleReturnTransaction void
	 * 
	 * @param newClass
	 */
	protected void setCloneAttributes(GDYNSaleReturnTransaction newClass) {
		super.setCloneAttributes(newClass);
		newClass.setSurveyCustomerInviteID(this.surveyCustomerInviteID);
		newClass.setSurveyCustomerInviteURL(this.surveyCustomerInviteURL);
		newClass.setOriginalExchangeTransaction(this.originalExchangeTransaction);
		// Pak add this
		// 559938 - ORPOS Tax Exemption with Retrieved Transaction - incorrect
		// tax amount
		// 01-22-2015
		newClass.setTransactionTotals(this.totals);
	}

	/**
	 * @return the employeeDiscountName
	 */
	public String getEmployeeDiscountName() {
		return employeeDiscountName;
	}

	/**
	 * @param employeeDiscountName
	 *            the employeeDiscountName to set
	 */
	public void setEmployeeDiscountName(String employeeDiscountName) {
		this.employeeDiscountName = employeeDiscountName;
	}

	/**
	 * Adds a PLU item to the transaction.
	 *
	 * @param pItem
	 *            PLU item
	 * @param qty
	 *            quantity
	 * @return transaction line item
	 */
	public SaleReturnLineItemIfc addPLUItem(PLUItemIfc pItem, BigDecimal qty) {
		if (this.getEmployeeDiscountID() != null)
			pItem.clearAdvancedPricingRules();
		// Pak add this here to set new customer exemption to items
		setCustomerTaxExempt((GDYNTransactionTax) getItemContainerProxy()
				.getTransactionTax());

		return (super.addPLUItem(pItem, qty));
	}

	/**
	 * Updates transaction totals and resets transaction type.
	 *
	 * @param lineItems
	 *            array of line items
	 * @param discounts
	 *            array of transaction discounts
	 * @param tax
	 *            transaction tax object
	 */
	/*
	 * protected void updateTransactionTotals(AbstractTransactionLineItemIfc[]
	 * lineItems, TransactionDiscountStrategyIfc[] discounts, TransactionTaxIfc
	 * tax) { totals.updateTransactionTotals(lineItems, discounts, tax);
	 * resetTransactionType(); }
	 */

	/**
	 * @return the loyaltyID
	 */
	public String getLoyaltyID() {
		return loyaltyID;
	}

	/**
	 * @param loyaltyID
	 *            the loyaltyID to set
	 */
	public void setLoyaltyID(String loyaltyID) {
		this.loyaltyID = loyaltyID;
	}

	/**
	 * @return the loyaltyEmailID
	 */
	public String getLoyaltyEmailID() {
		return loyaltyEmailID;
	}

	/**
	 * @param loyaltyEmailID
	 *            the loyaltyEmailID to set
	 */
	public void setLoyaltyEmailID(String loyaltyEmailID) {
		this.loyaltyEmailID = loyaltyEmailID;
	}

	/**
	 * @return the originalLoyaltyID
	 */
	public String getOriginalLoyaltyID() {
		return originalLoyaltyID;
	}

	/**
	 * @param originalLoyaltyID the originalLoyaltyID to set
	 */
	public void setOriginalLoyaltyID(String originalLoyaltyID) {
		this.originalLoyaltyID = originalLoyaltyID;
	}

	/**
	 * @return the isLoyaltyEnable
	 */
	public boolean isLoyaltyEnable() {
		return isLoyaltyEnable;
	}

	/**
	 * @param isLoyaltyEnable the isLoyaltyEnable to set
	 */
	public void setLoyaltyEnable(boolean isLoyaltyEnable) {
		this.isLoyaltyEnable = isLoyaltyEnable;
	}

	/**
	 * @return the loyaltyPhoneNo
	 */
	public String getLoyaltyPhoneNo() {
		return loyaltyPhoneNo;
	}

	/**
	 * @param loyaltyPhoneNo the loyaltyPhoneNo to set
	 */
	public void setLoyaltyPhoneNo(String loyaltyPhoneNo) {
		this.loyaltyPhoneNo = loyaltyPhoneNo;
	}

}
