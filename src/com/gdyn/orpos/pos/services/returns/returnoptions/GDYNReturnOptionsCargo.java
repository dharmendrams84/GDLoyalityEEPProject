//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.services.returns.returnoptions;

// java imports
import oracle.retail.stores.pos.services.returns.returnoptions.ReturnOptionsCargo;

//--------------------------------------------------------------------------
/**
 * Cargo for the Return Options service.
 */
// --------------------------------------------------------------------------
public class GDYNReturnOptionsCargo extends ReturnOptionsCargo
{
    /**
     * 
     */
    private static final long serialVersionUID = -113032780539750190L;
    /**
     * Tells if transaction has both sale and return items
     */
    protected boolean originalExchangeTransaction = false;
   
    // modified by vivek for creating new variable to original loyalty Id
    protected boolean isLoyaltyEnable = true;
    
    protected String originalLoyaltyID = "";

    // ----------------------------------------------------------------------
    /**
     * Class Constructor.
     * <p>
     * Initializes the reason code list for item returns.
     * <P>
     */
    // ----------------------------------------------------------------------
    public GDYNReturnOptionsCargo()
    {
    }

    /**
     * @return the exchangeTransaction
     */
    public boolean isOriginalExchangeTransaction()
    {
        return originalExchangeTransaction;
    }

    /**
     * @param exchangeTransaction
     *            the exchangeTransaction to set
     */
    public void setOriginalExchangeTransaction(boolean originalExchangeTransaction)
    {
        this.originalExchangeTransaction = originalExchangeTransaction;
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
}
