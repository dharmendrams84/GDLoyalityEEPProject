//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.pos.services.sale.SaleCargoIfc;

// Begin GD-384: Only applies employee discount to items posted to sell item screen at time employee discount applied - not to items added afterwards
// lcatania (Starmount) Apr 26, 2013

/**
 * Created to be able to get and set employee discount to sale cargo
 * @author lcatania
 *
 */
public interface GDYNSaleCargoIfc extends SaleCargoIfc
{
    /**
     * Gets the discountEmployee
     * @return the discountEmployee
     */
    public EmployeeIfc getDiscountEmployee();

    /**
     * Sets the discountEmployee
     * @param discountEmployee the discountEmployee to set
     */
    public void setDiscountEmployee(EmployeeIfc discountEmployee);
    
    /**
	 * @return the loyaltyIdNumber
	 */
	public String getLoyaltyIdNumber();
	/**
	 * @param loyaltyIdNumber the loyaltyIdNumber to set
	 */
    public void setLoyaltyIdNumber(String loyaltyIdNumber);
    
    /**
	 * @return the originalLoyaltyID
	 */
	public String getOriginalLoyaltyID();

	/**
	 * @param originalLoyaltyID the originalLoyaltyID to set
	 */
	public void setOriginalLoyaltyID(String originalLoyaltyID);
    
	/**
	 * @return the loyaltyEmailId
	 */
	public String getLoyaltyEmailId();

	/**
	 * @param loyaltyEmailId the loyaltyEmailId to set
	 */
	public void setLoyaltyEmailId(String loyaltyEmailId);
	
	//new method added by ajay
	
	public boolean isCouponRedemptionFlow();
	
	public void setCouponRedemptionFlow(boolean couponRedemptionFlow);
	
    public boolean isLoyaltyCouponPresntinTrxn();
	public void setLoyaltyCouponPresntinTrxn(boolean loyaltyCouponPresntinTrxn);
	
	//new method added by dharmendra to return loyality phone no
	public String getLoyaltyPhoneNo();
	
	//new method added by dharmendra to set loyality phone no
	public void setLoyaltyPhoneNo(String loyaltyPhoneNo);
}

// End GD-384: Only applies employee discount to items posted to sell item screen...