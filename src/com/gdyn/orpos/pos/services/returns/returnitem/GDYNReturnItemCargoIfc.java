package com.gdyn.orpos.pos.services.returns.returnitem;

import oracle.retail.stores.pos.services.returns.returncommon.ReturnItemCargoIfc;

/**
 * This interface defines Provides a common interface from the manual item and
 * transaction return return cargos into the sites that manage the return of
 * individual items.
 * 
 * @version $Revision: /main/13 $
 */
public interface GDYNReturnItemCargoIfc
    extends ReturnItemCargoIfc
{
	/**
	 * @return the isLoyaltyEnable
	 */
	public boolean isLoyaltyEnable();

	/**
	 * @param isLoyaltyEnable the isLoyaltyEnable to set
	 */
	public void setLoyaltyEnable(boolean isLoyaltyEnable);

	/**
	 * @return the originalLoyaltyID
	 */
	public String getOriginalLoyaltyID();

	/**
	 * @param originalLoyaltyID the originalLoyaltyID to set
	 */
	public void setOriginalLoyaltyID(String originalLoyaltyID);

}
