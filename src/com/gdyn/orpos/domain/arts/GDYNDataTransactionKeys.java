//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import oracle.retail.stores.domain.arts.DataTransactionKeys;


//------------------------------------------------------------------------------
/**
 * Extended for new Groupe Dynamite data transaction constants.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public interface GDYNDataTransactionKeys extends DataTransactionKeys
{
    public static final String TAX_EXEMPT_DATA_TRANSACTION = "persistence_TaxExemptDataTransaction";
    public static final String TAX_AUTHORTIY_DATA_TRANSACTION = "persistence_TaxAuthorityDataTransaction";
    public static final String COUPON_ATTRIBUTE_DATA_TRANSACTION = "persistence_CouponAttributeDataTransaction";
    
}
