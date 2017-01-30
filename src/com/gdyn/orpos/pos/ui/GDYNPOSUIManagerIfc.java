//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.ui;

import oracle.retail.stores.pos.ui.POSUIManagerIfc;

/**
 * Extending the base POSUIManagerIfc for Groupe Dyamite enhancements
 * - Tax Exempt
 * 
 * @author mlawrence
 *
 */
public interface GDYNPOSUIManagerIfc extends POSUIManagerIfc
{
    public static final String TAX_EXEMPT_CUST_INFO = "TAX_EXEMPT_CUST_INFO";
    
    //modified for loyalty enhancement. Creating new POSUIconstant for Loyalty Screen.
    public static final String ENTER_LOYALTY_ID = "ENTER_LOYALTY_ID";
    
    //modified for loyalty enhancement. Creating new POSUIconstant for EmailId Screen
    public static final String ENTER_EMAIL_ID = "ENTER_EMAIL_ID";
    
  //modified for loyalty enhancement. Creating new POSUIconstant for PhoneNo Screen
    public static final String ENTER_PHONE_NO = "ENTER_PHONE_NO";
    

    public static final String ENTER_ORIGINAL_LOYALTY_ID = "ENTER_ORIGINAL_LOYALTY_ID";

    
}
