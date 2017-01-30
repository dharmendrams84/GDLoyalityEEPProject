//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.sale;

import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.pos.services.sale.SaleCargo;

/**
 * Extending the sale cargo for enhancements.
 * - Tax Exemption 
 * 
 */
public class GDYNSaleCargo extends SaleCargo implements GDYNSaleCargoIfc
{

    /**
     * Serial Id
     */
    private static final long serialVersionUID = -3774833046369885115L;

    /**
     * The missing field names for a tax exempt customer.
     */
    protected List<Integer> missingTaxExemptFields = new ArrayList<Integer>(6);
    
    protected SaleReturnLineItemIfc lineItem;
    
    /**
     * Hold coupon refernce number
     */
    
	// Begin GD-384: Only applies employee discount to items posted to sell item screen 
    // at time employee discount applied - not to items added afterwards
    // lcatania (Starmount) Apr 25, 2013
    /**
     * Discount Employee domain object
     */
    protected EmployeeIfc discountEmployee;
    // End GD-384: Only applies employee discount to items posted to sell item screen...

    // modified by vivek for creating new variable to Customer loyalty Id
    protected String loyaltyIdNumber = "";
    
    protected String originalLoyaltyID = "";
    
    // modified by vivek for creating new variable to Customer Email Id
    protected String loyaltyEmailId = "";
    
    
    // modified by Dharmendra for creating new variable to Customer phone number
    protected String loyaltyPhoneNo = "";
    
    
    // modified by ajay for tracking coupon redemption flow inside loyalty capture flow.
    
   
	protected boolean couponRedemptionFlow = false; 
  
     // track if coupon is already addded to transaction
    
  
    protected boolean loyaltyCouponPresntinTrxn = false; 
  
    public boolean isLoyaltyCouponPresntinTrxn() {
		return loyaltyCouponPresntinTrxn;
	}

	public void setLoyaltyCouponPresntinTrxn(boolean loyaltyCouponPresntinTrxn) {
		this.loyaltyCouponPresntinTrxn = loyaltyCouponPresntinTrxn;
	}

	/**
     * The list of missing required tax exempt fields.
     * 
     * @return List<Integer> missingTaxExemptFields
     */
    public List<Integer> getMissingTaxExemptFields()
    {
        return missingTaxExemptFields;
    }

    /**
     * Called by the GUI to set the missing tax exempt required fields.
     * 
     * @param List<Integer> missingTaxExemptFields 
     */
    public void setMissingTaxExemptFields(int fieldId)
    {
        missingTaxExemptFields.add(Integer.valueOf(fieldId));

    }
    
    // Begin GD-384: Only applies employee discount to items posted to sell item screen 
    // at time employee discount applied - not to items added afterwards
    // lcatania (Starmount) Apr 25, 2013
    /**
     * @return the discountEmployee
     */
    public EmployeeIfc getDiscountEmployee()
    {
        return discountEmployee;
    }

    
    /**
     * @param discountEmployee the discountEmployee to set
     */
    public void setDiscountEmployee(EmployeeIfc discountEmployee)
    {
        this.discountEmployee = discountEmployee;
    }
    // End GD-384: Only applies employee discount to items posted to sell item screen...
 
	public PLUItemIfc getPLUItemForSizePrompt() {
		return null;
	}
	/**
	 * @return the loyaltyIdNumber
	 */
	public String getLoyaltyIdNumber() {
		return loyaltyIdNumber;
	}

	/**
	 * @param loyaltyIdNumber the loyaltyIdNumber to set
	 */
	public void setLoyaltyIdNumber(String loyaltyIdNumber) {
		this.loyaltyIdNumber = loyaltyIdNumber;
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
	 * @return the loyaltyEmailId
	 */
	public String getLoyaltyEmailId() {
		return loyaltyEmailId;
	}

	/**
	 * @param loyaltyEmailId the loyaltyEmailId to set
	 */
	public void setLoyaltyEmailId(String loyaltyEmailId) {
		this.loyaltyEmailId = loyaltyEmailId;
	}
	
	@Override
	public boolean isCouponRedemptionFlow() 
	{
		return couponRedemptionFlow;
	}

	@Override
	public void setCouponRedemptionFlow(boolean couponRedemptionFlow) 
	{
		this.couponRedemptionFlow = couponRedemptionFlow;
		
	}


		/**
		 * @return the loyaltyPhoneNo
		 */
	//new method added by dharmendra to return loyality phone no
		public String getLoyaltyPhoneNo() {
			return loyaltyPhoneNo;
		}

		/**
		 * @param loyaltyPhoneNo the loyaltyPhoneNo to set
		 */
		//new method added by dharmendra to set loyality phone no
		public void setLoyaltyPhoneNo(String loyaltyPhoneNo) {
			this.loyaltyPhoneNo = loyaltyPhoneNo;
		}

}
