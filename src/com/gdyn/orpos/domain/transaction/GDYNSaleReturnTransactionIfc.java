//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.transaction;

import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

/**
 * Extend the SaleReturnTransactionIfc for Groupe Dynamite enhancements -
 * Customer Survey Reward - Tax Exemption - Return: Disable Tender button
 * 
 * @author mlawrence
 *
 */
public interface GDYNSaleReturnTransactionIfc extends SaleReturnTransactionIfc {
	public String getSurveyCustomerInviteID();

	public String getSurveyCustomerInviteURL();

	/**
	 * Set the unique customer invitation code
	 * 
	 * GDYNSaleReturnTransactionIfc void
	 * 
	 * @param uic
	 */
	public void setSurveyCustomerInviteID(String surveyCustomerInviteID);

	/**
	 * The shortened URL
	 * 
	 * GDYNSaleReturnTransactionIfc void
	 * 
	 * @param url
	 */
	public void setSurveyCustomerInviteURL(String surveyCustomerInviteURL);

	/**
	 * Check to see if the transaction is taxable. A transaction is taxable is
	 * assumed taxable, unless every item in the transaction is a non-taxable
	 * item.
	 * 
	 * @return nonTaxableItemFound
	 */
	public boolean isCustomerTaxExemptTransaction();

	/**
	 * Checks if the transaction contains any customer tax exempt eligible
	 * items. If there is a customer tax exemption on the transaction and at
	 * least one item that is not non-taxable or has an exception, then this
	 * returns true.
	 */
	boolean hasTaxExemptEligibleItems();

	/**
	 * Applies a GDYN customer tax exemption.
	 * 
	 * @param newTax
	 *            a tax object to extract data from
	 */
	void setCustomerTaxExempt(GDYNTransactionTax newTax);

	/**
	 * Clears a customer tax exemption.
	 */
	void clearCustomerTaxExempt();

	/**
	 * @return the employeeDiscountName
	 */
	String getEmployeeDiscountName();

	/**
	 * @param employeeDiscountName
	 *            the employeeDiscountName to set
	 */
	void setEmployeeDiscountName(String employeeDiscountName);

	/**
	 * @return the originalExchangeTransaction
	 */
	public boolean isOriginalExchangeTransaction();

	/**
	 * @param originalExchangeTransaction
	 *            the exchangeTransaction to set
	 */
	public void setOriginalExchangeTransaction(
			boolean originalExchangeTransaction);

	public abstract TransactionTaxIfc getOriginalTransactionTax();

	public abstract void setOriginalTransactionTax(
			TransactionTaxIfc paramTransactionTaxIfc);

	/*
	 * Modified by Vivek for Loyalty Enhancement. Creating two variable (Loyalty
	 * and Email ID).
	 */
	/**
	 * @return the loyaltyID
	 */
	public String getLoyaltyID();

	/**
	 * @param loyaltyID
	 *            the loyaltyID to set
	 */
	public void setLoyaltyID(String loyaltyID);

	/**
	 * @return the loyaltyEmailID
	 */
	public String getLoyaltyEmailID();

	/**
	 * @param loyaltyEmailID
	 *            the loyaltyEmailID to set
	 */
	public void setLoyaltyEmailID(String loyaltyEmailID);
	
	/**
	 * @return the originalLoyaltyID
	 */
	public String getOriginalLoyaltyID();

	/**
	 * @param originalLoyaltyID the originalLoyaltyID to set
	 */
	public void setOriginalLoyaltyID(String originalLoyaltyID);

	/**
	 * @return the isLoyaltyEnable
	 */
	public boolean isLoyaltyEnable();

	/**
	 * @param isLoyaltyEnable the isLoyaltyEnable to set
	 */
	public void setLoyaltyEnable(boolean isLoyaltyEnable);
	
	
	
	/**
	 * @return the loyalityPhoneNo
	 */
	public String getLoyaltyPhoneNo();
	
	/**
	 * @param loyaltyPhoneNo
	 */
	public void setLoyaltyPhoneNo(String loyaltyPhoneNo);
}
