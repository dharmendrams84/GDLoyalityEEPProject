//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.common.parameter;

import oracle.retail.stores.common.parameter.ParameterConstantsIfc;

/**
 * New Parameters defined by Customer Survey Reward Enhancement. CustomerSurveyRewardTransactionType is defined
 * in the parameter group Customer. The rest are part of the CustomerSurvey group.
 * 
 * @author MSolis
 * 
 */
public interface GDYNParameterConstantsIfc extends ParameterConstantsIfc
{
    /**
     * This can be set to sale, return, or both. It cannot be empty. Refer
     * to the unique invitation code. The transaction type is a parameter.
     */
    public static final String Customer_CustomerSurveyTransactionType = "CustomerSurveyRewardTransactionType";
    public static final String CustomerSurvey_CustomerSurveyIncentiveType = "CustomerSurveyRewardIncentiveType";
    public static final String CustomerSurvey_CustomerSurveyBaseURL = "CustomerSurveyRewardBaseURL";
    
    // Begin GD-150: Modify UI based on PLAF settings
    // lcatania (Starmount) Feb 8, 2013
    public static final String Store_Website = "StoreWebsite";
    // End GD-150: Modify UI based on PLAF settings
    
    // Begin GD-49: Develop Employee Discount Module
    // lcatania (Starmount) Feb 22, 2013
    public static final String DISCOUNT_Employee_Discount_Classes = "EmployeeDiscountClasses";
    public static final String DISCOUNT_Default_Employee_Discount_Percent = "DefaultEmployeeDiscountPercent";
    // End GD-49: Develop Employee Discount Module
    
    // Begin GD-341: Add parameter to bypass customer capture in POS
    // lcatania (Starmount) Apr 9, 2013
    public static final String CUSTOMER_Bypass_Customer_Options_Screen = "BypassCustomerOptionsScreen";
    // End GD-341: Add parameter to bypass customer capture in POS
    
    // modified by Vivek to add new parameter for loyalty enable and prompt for loyalty screes
    public static final String CUSTOMER_loyalty_Enable = "customerLoyaltyEnabled";
    public static final String CUSTOMER_Prompt_For_Loyalty_Screen = "customerPromptForLoyalty";
    
}
