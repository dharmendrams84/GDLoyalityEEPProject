//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.services.sale.complete;

import java.text.SimpleDateFormat;
import java.util.Date;

import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.foundation.manager.ifc.JournalManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.service.SessionBusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.appmanager.ManagerException;
import oracle.retail.stores.pos.appmanager.ManagerFactory;
import oracle.retail.stores.pos.services.sale.complete.WriteTransactionSite;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;
import oracle.retail.stores.pos.ui.beans.StatusBeanModel;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.customer.GDYNCustomerConstantsIfc;
import com.gdyn.orpos.domain.manager.printing.GDYNCustomerSurveyManagerIfc;
import com.gdyn.orpos.domain.manager.printing.GDYNUniqueCustomerInvitationID;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransaction;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.printing.GDYNCustomerSurveyReward;
import com.gdyn.orpos.pos.services.printing.GDYNCustomerSurveyRewardIfc;
import com.gdyn.orpos.pos.services.sale.GDYNSaleCargoIfc;

/**
 * GD-50 CSAT NOTE: During printing, we will need access to the same manager
 * 
 * @author MSolis
 * 
 */
public class GDYNWriteTransactionSite extends WriteTransactionSite implements GDYNCustomerConstantsIfc
{
    private static final long serialVersionUID = -3967719493821486710L;

    /** The logger to which log messages will be sent */
    private static final Logger logger = Logger.getLogger(GDYNWriteTransactionSite.class);

    /**
     * Override default behavior. Before saving, attempt to see if
     * we can acquire the CSAT UIC and shortened URL. If so, save
     * the two values with the transaction.
     * 
     * NOTE, during printing when the GDYNCustomerSurveyReward object
     * is created, it will determine if the survey receipt should print.
     * 
     * Send the Save letter (the work is done in the aisles)
     * 
     * @param bus
     *            Service Bus
     */
    public void arrive(BusIfc bus)
    {
        POSUIManagerIfc ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
        GDYNSaleCargoIfc cargo = (GDYNSaleCargoIfc) bus.getCargo();
        RetailTransactionIfc trans = cargo.getRetailTransaction();
        
        if (trans instanceof GDYNSaleReturnTransaction)
        {

            GDYNSaleReturnTransactionIfc gdynTrans = (GDYNSaleReturnTransaction) trans;

            //modified by Vivek : setting the Loyalty and Email ID to the transaction Object
            if (!cargo.getLoyaltyIdNumber().trim().equals("")) {
            	gdynTrans.setLoyaltyID(cargo.getLoyaltyIdNumber());
    		}
    		if (!cargo.getLoyaltyEmailId().trim().equals("")) {
    			gdynTrans.setLoyaltyEmailID(cargo.getLoyaltyEmailId());
    		}
    		if (!cargo.getOriginalLoyaltyID().trim().equals("")) {
            	gdynTrans.setOriginalLoyaltyID(cargo.getOriginalLoyaltyID());
    		}
    		
    		//modified by Dharmendra : setting the Loyalty phone no to the transaction Object
			String loyalityPhoneNo = cargo.getLoyaltyPhoneNo();
			if (!GDYNLoyalityConstants.isEmpty(loyalityPhoneNo)) {
				gdynTrans.setLoyaltyPhoneNo(loyalityPhoneNo.trim());
			} else {
				gdynTrans.setLoyaltyPhoneNo("");
			}
			cargo.setLoyaltyPhoneNo("");
    		updateEJournalLoyalty(gdynTrans);
        }
        // clear the customer's name in the status area
        StatusBeanModel statusModel = new StatusBeanModel();
        statusModel.setCustomerName("");
        POSBaseBeanModel baseModel = new POSBaseBeanModel();
        baseModel.setStatusBeanModel(statusModel);
        ui.setModel(POSUIManagerIfc.SHOW_STATUS_ONLY, baseModel);

        updateTransWithCSATSupport(bus);

        bus.mail(new Letter("Save"), BusIfc.CURRENT);
    }

    /**
     * Make the web service call.
     * 
     * GDYNWriteTransactionSite
     * void
     * 
     * @param bus
     */
    protected void updateTransWithCSATSupport(BusIfc bus)
    {
        // 1) Get the parameter manager.
        ParameterManagerIfc pm = (ParameterManagerIfc) bus.getManager(ParameterManagerIfc.TYPE);

        GDYNSaleCargoIfc cargo = (GDYNSaleCargoIfc) bus.getCargo();
        RetailTransactionIfc trans = cargo.getRetailTransaction();

        // 2) See if we are even doing surveys.
        boolean surveying = false;
        String incentiveType = null;
        String brand = null;

        try
        {
            incentiveType = pm.getStringValue(CUSTOMER_SURVEY_INCENTIVE_TYPE);
            brand = pm.getStringValue(CUSTOMER_SURVEY_BASE_URL);
        }
        catch (ParameterException e)
        {
            logger.warn("Unable to properly return customer survey/reward text: " + e.getMessage());
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Customer survey perform the web call: " + surveying);
        }

        // No need for a web call if the survey is turned off.
        if (!isSurveyExpected(bus, trans))
        {
            return;
        }


        String storeID = trans.getFormattedStoreID();
        String registerID = trans.getFormattedWorkstationID();

        // EYSDate date = DomainGateway.getFactory().getEYSDateInstance();
        EYSDate eysDate = new EYSDate();
        String today = formatDate(eysDate);

        String rightNow = formatTime();

        String associate = trans.getSalesAssociate().getEmployeeID();
        String transType = getGDYNTransactionType(trans.getTransactionType());

        // This object wrapper the UIC
        GDYNUniqueCustomerInvitationID customerInvitationId = new GDYNUniqueCustomerInvitationID();
        customerInvitationId.setStoreId(storeID);
        customerInvitationId.setRegisterId(registerID);
        customerInvitationId.setTransDate(today);
        customerInvitationId.setTransTime(rightNow);
        customerInvitationId.setAssociate(associate);
        customerInvitationId.setTransType(transType);
        customerInvitationId.setIncentiveType(incentiveType);
        // This will calculate the check-digit.
        customerInvitationId.setCheckDigit();

        if (trans instanceof GDYNSaleReturnTransaction)
        {
            GDYNSaleReturnTransactionIfc gdynTrans = (GDYNSaleReturnTransaction) trans;
            // Store the UIC in the transaction object.
            gdynTrans.setSurveyCustomerInviteID(customerInvitationId.getUniqueInvitationCode());

            //modified by Vivek : setting the Loyalty and Email ID to the transaction Object
            /*if (cargo.getLoyaltyIdNumber() != null) {
            	gdynTrans.setLoyaltyID(cargo.getLoyaltyIdNumber());
    		}
    		if (cargo.getLoyaltyEmailId() != null) {
    			gdynTrans.setLoyaltyEmailID(cargo.getLoyaltyEmailId());
    		}
    		if (cargo.getOriginalLoyaltyID() != null) {
            	gdynTrans.setOriginalLoyaltyID(cargo.getOriginalLoyaltyID());
    		}
    		
    		updateEJournalLoyalty(gdynTrans);*/
            /**
             * Make the web call using a manager.
             */
            GDYNCustomerSurveyManagerIfc csm = (GDYNCustomerSurveyManagerIfc) bus
                    .getManager(GDYNCustomerSurveyManagerIfc.TYPE);
            csm.setCustomerInvitationID(customerInvitationId);
            csm.setBaseURLByBrand(brand);
            /**
             * We cannot proceed until the web call returns. The next step
             * is to save the transaction object.
             */
            csm.makeWebCallNonThreading();
            if (csm.getShortURL() != null)
            {
                // Store the shortened URL in the transaction object.
                gdynTrans.setSurveyCustomerInviteURL(csm.getShortURL());
            }

          //Added below condition by Dharmendra to resolve pos-54 issue(CSAT invitation code and Short URL being generated for EE purchase) on 20/08/2015
            if(!(trans != null && trans.getCustomer() != null && trans.getCustomer().getEmployeeID() != null && ! trans.getCustomer().getEmployeeID().isEmpty())){
         	   updateEJournal(gdynTrans);
                }
            //updateEJournal(gdynTrans);
        }
    }

    /**
     * Write to the EJournal
     * 
     * GDYNWriteTransactionSite
     * void
     * 
     * @param trans
     */
    private void updateEJournal(GDYNSaleReturnTransactionIfc trans)
    {
        // Make a journal entry
        JournalManagerIfc journal = (JournalManagerIfc) Gateway.getDispatcher().getManager(JournalManagerIfc.TYPE);

        if (journal != null)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(Util.EOL);
            sb.append("Invitation Code: " + Util.EOL);
            sb.append(trans.getSurveyCustomerInviteID() + Util.EOL);

            if (trans.getSurveyCustomerInviteURL() != null)
            {
                sb.append(Util.EOL);
                sb.append("Shorter URL: " + Util.EOL);
                sb.append(trans.getSurveyCustomerInviteURL() + Util.EOL);
            }
           
            journal.journal(sb.toString());
        }
    }

    
    /**
     * Write to the EJournal
     * 
     * GDYNWriteTransactionSite
     * void
     * 
     * @param trans
     */
    private void updateEJournalLoyalty(GDYNSaleReturnTransactionIfc trans)
    {
        // Make a journal entry
        JournalManagerIfc journal = (JournalManagerIfc) Gateway.getDispatcher().getManager(JournalManagerIfc.TYPE);

        if (journal != null)
        {
            StringBuffer sb = new StringBuffer();
                       
            //modified by Vivek : set it the loyalty to email ID to Journel
            if(trans.getLoyaltyID() != null)
            {
                sb.append(Util.EOL);
                sb.append("Loyalty Id: " + Util.EOL);
                sb.append(trans.getLoyaltyID() + Util.EOL);
            }
            
            if(trans.getLoyaltyEmailID() != null)
            {
                sb.append(Util.EOL);
                sb.append("Loyalty Email Id: " + Util.EOL);
                sb.append(trans.getLoyaltyEmailID() + Util.EOL);
            }
            
            if(trans.getLoyaltyEmailID() != null)
            {
                sb.append(Util.EOL);
                sb.append("Original Loyalty ID: " + Util.EOL);
                sb.append(trans.getOriginalLoyaltyID() + Util.EOL);
            }
            
            //code changes done by Dharmendra to add loyality phone no to journal entry(POS-317)
            if(trans.getLoyaltyPhoneNo() != null)
            {
                sb.append(Util.EOL);
                sb.append("Loyalty Phone No: " + Util.EOL);
                sb.append(trans.getLoyaltyPhoneNo() + Util.EOL);
            }

            journal.journal(sb.toString());
        }
    }
    /**
     * Format the time according to the FES.
     * 
     * GDYNWriteTransactionSite
     * String
     * 
     * @return
     */
    private String formatTime()
    {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String formatNow;
        formatNow = sdf.format(now);
        return formatNow;
    }

    /**
     * The format of the date string must not vary. The EYSDate will
     * take the locale into account. The format of the date string is:
     * yymmdd - December 19, 2012 - 121219
     * 
     * GDYNWriteTransactionSite
     * String
     * 
     * @param date
     * @return
     */
    protected String formatDate(EYSDate date)
    {
        int year = date.getYear();
        int day = date.getDay();
        int month = date.getMonth();

        StringBuilder builder = new StringBuilder();
        String sYear = Integer.toString(year);
        // Grab only the last two digits of the year.
        for (int i = sYear.length() - 2; i < sYear.length(); i++)
        {
            builder.append(sYear.charAt(i));
        }

        if (month < 10)
        {
            builder.append('0');
        }
        builder.append(month);

        if (day < 10)
        {
            builder.append('0');
        }
        builder.append(day);

        return builder.toString();
    }

    /**
     * The transaction type is represented as a string.
     * 1 = sale, 2 = return.
     */
    protected String getGDYNTransactionType(int transType)
    {
        String result = null;

        if (transType == TransactionIfc.TYPE_SALE)
        {
            result = TRANS_SALE;
        }

        if (transType == TransactionIfc.TYPE_RETURN)
        {
            result = TRANS_RETURN;
        }

        return result;
    }
    
    /**
     * Implement Customer Reward Survey FES. This method overrides base behavior using the
     * new manager.
     * 
     * @param bus
     * @param trans
     *            - The transaction.
     */
    protected boolean isSurveyExpected(BusIfc bus, TenderableTransactionIfc trans)
    {
        GDYNCustomerSurveyRewardIfc customerSurveyReward = null;
        // Ensure we have the Customer Survey / Reward manager
        try
        {
            customerSurveyReward = (GDYNCustomerSurveyRewardIfc) ManagerFactory
                    .create(GDYNCustomerSurveyRewardIfc.MANAGER_NAME);
        }
        catch (ManagerException e)
        {
            // default to product version
            customerSurveyReward = new GDYNCustomerSurveyReward();
        }
        catch (Throwable t)
        {
            // assume no survey
            return false;
        }

        // Do the work
        return customerSurveyReward.isSurveyExpected((SessionBusIfc) bus, trans, false);
    }

}
