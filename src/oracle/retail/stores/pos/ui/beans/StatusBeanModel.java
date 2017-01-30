/* ===========================================================================
* Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved. 
* ===========================================================================
* $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/ui/beans/StatusBeanModel.java /rgbustores_13.4x_generic_branch/1 2011/05/05 14:06:52 mszekely Exp $
* ===========================================================================
* NOTES
* <other useful comments, qualifications, etc.>
*
* MODIFIED (MM/DD/YY)
*    cgreen 05/28/10 - convert to oracle packaging
*    abonda 01/03/10 - update header date
*    nkgaut 09/18/08 - Added a boolean for Cash Drawer UNDER warning message.

* ===========================================================================

    Header:   $KW=@(#); $FN=oracle/retail/stores/pos/ui/beans/StatusBeanModel.java; $EKW;
    Revision: $KW=@(#); $Ver=pos_4.5.0:2; $EKW;
    Date:     $KW=@(#); $ChkD=2001/05/09 11:21:30; $EKW;
    Author:   $KW=@(#); $Own=Builder; $EKW;

* ===================================================
*/
package oracle.retail.stores.pos.ui.beans;

// Java imports
import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.financial.RegisterIfc;
import oracle.retail.stores.foundation.manager.gui.UIModelIfc;
import oracle.retail.stores.pos.ui.OnlineStatusContainer;

//---------------------------------------------------------------------------
/**
    The model for communicating with the StatusBean
    @version $KW=@(#); $Ver=pos_4.5.0:2; $EKW;
*/
//---------------------------------------------------------------------------
public class StatusBeanModel implements UIModelIfc
{
    // This id is used to tell
    // the compiler not to generate a
    // new serialVersionUID.
    //
    static final long serialVersionUID = -3181460046153188347L;

    /** revision number supplied by Team Connection */
    public static final String revisionNumber = "$KW=@(#); $Ver=pos_4.5.0:2; $EKW;";

    /** Register number to display to the user   */
    protected String registerId = null;

    /** Cashier name to display to the user      */
    protected String cashierName = null;

    /** Sales associate name to display to the user */
    protected String salesAssociateName = null;

    /** Customer name to display to the user        */
    protected String customerName = null;

    /** Screen name        */
    protected String screenName = null;

    /** The register */
    protected RegisterIfc register = null;

    // Holds the status for various devices that may be online or offline
    protected OnlineStatusContainer statusContainer = null;

    /**
        boolean variable for Cash drawer warning message
     */
    public boolean bCashDrawerWarningReqd = false;

    /**
        Gets the boolean for Cash drawer warning.
    **/
    
    
    public static boolean empDiscountApplied = false;
    
    
    
    /**
   	 * @return the isEmpDiscountApplied
   	 */
   	public boolean isEmpDiscountApplied() {
   		return empDiscountApplied;
   	}

   	/**
   	 * @param isEmpDiscountApplied the isEmpDiscountApplied to set
   	 */
   	public void setEmpDiscountApplied(boolean isEmpDiscountApplied) {
   		this.empDiscountApplied = isEmpDiscountApplied;
   	}

    
   
	public boolean isBCashDrawerWarningReqd()
    {
      return bCashDrawerWarningReqd;
    }

    /**
        Returns the boolean for Cash drawer warning.
        @return boolean
    **/
    public void setBCashDrawerWarningReqd(boolean cashDrawerWarningReqd)
    {
      bCashDrawerWarningReqd = cashDrawerWarningReqd;
    }

    //-----------------------------------------------------------------------
    /**
       Default constructor.
    */
    //-----------------------------------------------------------------------
    public StatusBeanModel()
    {
    }

    //-----------------------------------------------------------------------
    /**
       Convenience constructor for setting the display attributes of the
       StatusBean. Call this constructor using null for any of the attributes
       you do not wish to modify.

       @param register        Register Id
       @param cashier         Cashier name
       @param salesAssociate  Sales associate name
       @param customer        Customer name
       @param screen          Screen name
    */
    //-----------------------------------------------------------------------
    public StatusBeanModel(String register, String cashier,
                           String salesAssociate, String customer, String screen)
    {
        registerId    = register;
        cashierName   = cashier;
        salesAssociateName  = salesAssociate;
        customerName  = customer;
        screenName    = screen;
        empDiscountApplied = Boolean.FALSE;
    }

    //-----------------------------------------------------------------------
    /**
       Gets the Register Id.
       @return String registerId
    */
    //-----------------------------------------------------------------------
    public String getRegisterId()
    {
        return registerId;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the Register Id.
       @param value String registerId
    */
    //-----------------------------------------------------------------------
    public void setRegisterId(String value)
    {
        registerId = value;
    }

    //-----------------------------------------------------------------------
    /**
       Gets the Cashier Name.
       @return String cashierName
    */
    //-----------------------------------------------------------------------
    public String getCashierName()
    {
        return cashierName;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the Cashier Name.
       @param value String cashierName
    */
    //-----------------------------------------------------------------------
    public void setCashierName(String value)
    {
        cashierName = value;
    }

    //-----------------------------------------------------------------------
    /**
       Gets the Sales Associate Name
       @return String salesAssociateName
    */
    //-----------------------------------------------------------------------
    public String getSalesAssociateName()
    {
        return salesAssociateName;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the Sales Associate Name
       @param value String salesAssociateName
    */
    //-----------------------------------------------------------------------
    public void setSalesAssociateName(String value)
    {
        salesAssociateName = value;
    }

    //-----------------------------------------------------------------------
    /**
       Gets the Customer Name
       @return String customerName
    */
    //-----------------------------------------------------------------------
    public String getCustomerName()
    {
        return customerName;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the Customer Name
       @param value String customerName
    */
    //-----------------------------------------------------------------------
    public void setCustomerName(String value)
    {
        customerName = value;
    }

    //-----------------------------------------------------------------------
    /**
       Gets the screen name
       @return String screenName
    */
    //-----------------------------------------------------------------------
    public String getScreenName()
    {
        return screenName;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the screen Name
       @param value String screenName
    */
    //-----------------------------------------------------------------------
    public void setScreenName(String value)
    {
        screenName = value;
    }

    //-----------------------------------------------------------------------
    /**
       Gets the online container
       @return OnlineStatusContainer
    */
    //-----------------------------------------------------------------------
    public OnlineStatusContainer getStatusContainer()
    {
        return statusContainer;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the status of the specified device
       @param ID int device
       @param online boolean status
    */
    //-----------------------------------------------------------------------
    public void setStatus(int ID, boolean online)
    {
        if (statusContainer == null)
        {
            statusContainer = new OnlineStatusContainer();
        }

        statusContainer.getStatusHash().put(new Integer(ID), new Boolean(online));
    }

    //-----------------------------------------------------------------------
    /**
       Sets the status container
       @param value OnlineStatusContainer status container
    */
    //-----------------------------------------------------------------------
    public void setStatusContainer(OnlineStatusContainer value)
    {
        statusContainer = value;
    }

    //-----------------------------------------------------------------------
    /**
       Returns the register associated with this model.
       @return The register associated with this model.
       @see #hasNegativeBalance()
     */
    //-----------------------------------------------------------------------
    public RegisterIfc getRegister()
    {
        return register;
    }

    //-----------------------------------------------------------------------
    /**
       Sets the register associated with this model.
       @param register  The register.
     */
    //-----------------------------------------------------------------------
    public void setRegister(RegisterIfc register)
    {
        this.register = register;
    }

    //-----------------------------------------------------------------------
    /**
       Returns whether the current till has a negative balance.
       If there is no {@link #getRegister() register} associated with this
       model, then the assumption is that the till balance is postive or zero.
       @return Whether the current till has a negative balance.
     */
    //-----------------------------------------------------------------------
    public boolean hasNegativeBalance()
    {
        // Assume till is positive or zero.
        boolean isAmountNegative = false;

        // If we don't have a register, we cannot play the game.
        if (register == null)
        {
            return isAmountNegative;
        }

        // Determine the proper answer.
        CurrencyIfc amount = null;
        // (register.getCurrentTill() != null) represents a start-time condition
        if (register.getCurrentTill() != null)
        {
            amount = register.getCurrentTill().getTotals().getCombinedCount().getExpected().getAmount();
        }
        // (amount != null) represents a start-time condition
        if ((amount != null) && (amount.signum() == CurrencyIfc.NEGATIVE))
        {
            isAmountNegative = true;
        }

        return isAmountNegative;
    }

    //-----------------------------------------------------------------------
    /**
       Updates the current model with valid data from the parameter.
       @param model a StatusBeanModel
    */
    //-----------------------------------------------------------------------
    public void update(StatusBeanModel model)
    {
        if(model.getRegisterId() != null)
        {
            registerId = model.getRegisterId();
        }

        if(model.getCashierName() != null)
        {
            cashierName = model.getCashierName();
        }

        if(model.getSalesAssociateName() != null)
        {
            salesAssociateName = model.getSalesAssociateName();
        }

        if(model.getCustomerName() != null)
        {
            customerName = model.getCustomerName();
        }

        if(model.getScreenName() != null)
        {
            screenName = model.getScreenName();
        }

        if(model.getStatusContainer() != null)
        {
            statusContainer = model.getStatusContainer();
        }

        if(model.getRegister() != null)
        {
            register = model.getRegister();
        }
    }
}
