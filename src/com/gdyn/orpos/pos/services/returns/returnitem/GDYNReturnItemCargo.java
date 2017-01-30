package com.gdyn.orpos.pos.services.returns.returnitem;
// java imports
import oracle.retail.stores.pos.services.returns.returnitem.ReturnItemCargo;

//--------------------------------------------------------------------------
/**
 * Cargo for the Return Options service.
 */
// --------------------------------------------------------------------------
public class GDYNReturnItemCargo extends ReturnItemCargo implements GDYNReturnItemCargoIfc
{
    /**
     * 
     */
    private static final long serialVersionUID = -113032780539750190L;

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
    public GDYNReturnItemCargo()
    {
    	super();
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
