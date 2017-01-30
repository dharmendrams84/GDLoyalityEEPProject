//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.persistence.utility;

import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

/**
 * Extension for table and columns for Groupe Dynamite. 
 * - CSAT:  Storing the unique invitation code and the shortened URL.
 * - Tax Exemption
 * 
 * @author msolis
 *
 */
public interface GDYNARTSDatabaseIfc extends ARTSDatabaseIfc
{
    // TR_RTL Retail Transaction table additions
    public static final String FIELD_SURVEY_CUSTOMER_INVITE_ID = "ID_INVT_CT_SRVY";    
    public static final String FIELD_SURVEY_CUSTOMER_INVITE_URL = "URL_INVT_CT_SRVY";
    
    // Tax Exempt tables, fields, aliases, and OR Constants
    public static final String TABLE_TAX_EXEMPT_CUST_CATEGORY = "TAX_EXEMPT_CUST_CATEGORY";
    public static final String FIELD_CATEGORY_CODE            = "category_code";
    public static final String FIELD_COUNTRY_CODE             = "country_code";
    public static final String FIELD_TAX_AREA_CODE            = "tax_area_code";
    public static final String FIELD_CATEGORY_MSG_ID          = "category_msg_id";
    public static final String FIELD_ACTIVE_FLAG              = "active_flag";
    public static final String FIELD_EXPIRE_DATE_REQ_FLAG     = "expire_date_req_flag";
    public static final String FIELD_BAND_REGISTRY_REQ_FLAG   = "band_registry_req_flag";
    public static final String FIELD_TAX_ID_IMAGE_REQ_FLAG    = "tax_id_image_req_flag";
    public static final String TAX_PRODUCT_AREA_ALL           = "ALL";
        
    public static final String TABLE_TAX_EXEMPT_CUST_CATEGORY_I18N = "TAX_EXEMPT_CUST_CATEGORY_I18N";
    // public static final String FIELD_CATEGORY_MSG_ID            = "category_msg_id";
    public static final String FIELD_CUST_CATEGORY_LOCALE          = "locale";
    public static final String FIELD_CATEGORY_NAME                 = "category_name";
    public static final String FIELD_RECEIPT_MSG                   = "receipt_msg";
        
    public static final String TABLE_TAX_EXEMPT_CUST_ID_IMAGE = "TAX_EXEMPT_CUST_ID_IMAGE";
    public static final String FIELD_TAX_ID_IMAGE_NAME        = "tax_id_image_name";
    // public static final String FIELD_COUNTRY_CODE          = "country_code";
    public static final String FIELD_EFFECTIVE_DATE           = "effective_date";
        
    public static final String TABLE_TAX_EXEMPT_CUST_CODE = "TAX_EXEMPT_CUST_CODE";
    // public static final String FIELD_CATEGORY_CODE     = "category_code";
    // public static final String FIELD_COUNTRY_CODE      = "country_code";
    // public static final String FIELD_TAX_AREA_CODE     = "tax_area_code";
    // public static final String FIELD_EFFECTIVE_DATE    = "effective_date";
    public static final String FIELD_APPLICATION_METHOD   = "application_method";
    public static final String FIELD_PARTIAL_RATE         = "partial_rate";
    
    public static final String TABLE_TAX_EXEMPT_EXCEPTION_CODE = "TAX_EXEMPT_EXCEPTION_CODE";
    // public static final String FIELD_CATEGORY_CODE          = "category_code";
    // public static final String FIELD_COUNTRY_CODE           = "country_code";
    // public static final String FIELD_TAX_AREA_CODE          = "tax_area_code";
    public static final String FIELD_IMAGE_CODE                = "image_code";
    public static final String FIELD_TAX_PRODUCT_CODE          = "tax_product_code";
    // public static final String FIELD_EFFECTIVE_DATE         = "effective_date";
    
    // Adding columns to table CO_MDFR_TX_EXM (Tax Exemption Modifier)
    public static final String FIELD_TAX_EXEMPT_BAND_COUNCIL_REGISTRY = "TX_EXM_BND_REGISTRY";
    public static final String FIELD_TAX_ID_EXPIRY_DATE               = "DC_ID_TX_EXP";
    
    public static final String ALIAS_TAX_EXEMPT_CUST_CATEGORY      = "CAT";
    public static final String ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N = "CAT18";
    public static final String ALIAS_TAX_EXEMPT_CUST_ID_IMAGE      = "IMG";
    public static final String ALIAS_TAX_EXEMPT_CUST_CODE          = "CCODE";
    public static final String ALIAS_TAX_EXEMPT_EXCEPTION_CODE     = "ECODE";
    
    // Begin GD-49: Develop Employee Discount Module
    // lcatania (Starmount) Mar 7, 2013
    public static final String FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_NAME = "NM_EM";
    // End GD-49: Develop Employee Discount Module
    
    // Begin GD-343: Modify customer search/add screens for customer capture fields
    // lcatania (Starmount) Apr 5, 2013
    public static final String ALIAS_EMAIL_ADDRESS = "EML";
    // End GD-343: Modify customer search/add screens for customer capture fields
    
    // New field added to store coupon reference number in tr_ltm_dsc table
    public static final String COUPON_REF_NUMBER = "COUPON_REF_NUMBER";
    
    //modified by Vivek for Loyalty Enhancement. New fieds for Loyalty table.
    public static final String TABLE_LOYALTY_TRANSACTION_RECORD = "CT_TRN_LYLT";
    public static final String ALIAS_LOYALTY_TABLE = "LYLT";
    public static final String FIELD_LOYALTY_ID = "LYLT_ID";
    public static final String FIELD_LOYALTY_EMAIL_ID = "LYLT_EML";
    public static final String FIELD_LOYALTY_PHONE_NO = "LYLT_PHN";
    public static final String FIELD_TRANSACTION_EXPORT_FLAG = "FL_LYLT_EXPORT";
    public static final String FIELD_TRANSACTION_EXPORT_RECORD = "TS_EXPT_RCRD";
    public static final String FIELD_ORIGINAL_LOYALTY_ID = "LYLT_ID_ORG";
    public static final String FIELD_SALES_CHANNEL_RECORD = "ID_CHNL_SLS";
    
    //modified by Ajay for Loyalty Coupon. New fields and table def for 
    //custom loyalty coupon.
    public static final String TABLE_LOYALTY_COUPON_ATTRIBUTE= "CT_COUPON_ATTR";
    public static final String FIELD_LOYALTY_COUPON_ID = "COUPON_ID";
    public static final String FIELD_LOYALTY_COUPON_TYPE = "COUPON_TYPE";
    public static final String FIELD_ITEM_APPLY_TO = "ITM_APPLY_TO";
    public static final String FIELD_MINIMUM_THRESHOLD_AMOUNT = "MIN_MO_TH";
    public static final String FIELD_MAXIMUM_THRESHOLD_AMOUNT = "MAX_MO_TH";
    public static final String FIELD_MAXIMUM_LOYATY_DISCOUNT_AMOUNT = "MAX_DISC_AMT";
    public static final String FIELD_WEBSERVICE_VALIDATION_FLAG = "WS_VLDT_FLAG";
    
    public static final String TABLE_LOYALTY_COUPON_HIERARCHY= "CT_COUPON_HIER";
    public static final String FIELD_LOYALTY_DISCOUNT_DIVISION = "DISCOUNT_DIVISION";
    public static final String FIELD_LOYALTY_DISCOUNT_GROUP = "DISCOUNT_GROUP";
    public static final String FIELD_LOYALTY_DISCOUNT_DPT = "DISCOUNT_DPT";
    public static final String FIELD_LOYALTY_DISCOUNT_CLASS = "DISCOUNT_CLASS";
    public static final String FIELD_LOYALTY_DISCOUNT_SUBCLASS = "DISCOUNT_SUBCLS";
    /*new view and column added by dharmendra on 08/09/2016 to fix issue POS-233*/
    public static final String CT_VW_EEP_ITM_REG_PRC =  "CT_VW_EEP_ITM_REG_PRC";
    
    public static final String MO_EMP_PRC = "MO_EMP_PRC";

}
