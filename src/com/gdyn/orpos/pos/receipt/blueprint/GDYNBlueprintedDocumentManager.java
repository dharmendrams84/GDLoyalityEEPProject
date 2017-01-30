//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.receipt.blueprint;

import java.math.BigDecimal;

import oracle.retail.stores.common.context.BeanLocator;
import oracle.retail.stores.common.parameter.ParameterConstantsIfc;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.order.OrderConstantsIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.transaction.VoidTransactionIfc;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.service.SessionBusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.appmanager.ManagerException;
import oracle.retail.stores.pos.appmanager.ManagerFactory;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc;
import oracle.retail.stores.pos.receipt.ReceiptTypeConstantsIfc;
import oracle.retail.stores.pos.receipt.blueprint.BlueprintedDocumentManager;
import oracle.retail.stores.receipts.model.Blueprint;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNEmployeeDiscountUtility;
import com.gdyn.orpos.pos.services.printing.GDYNCustomerSurveyReward;
import com.gdyn.orpos.pos.services.printing.GDYNCustomerSurveyRewardIfc;
import com.gdyn.orpos.pos.services.sale.GDYNShowSaleScreenSite;

/**
 * A printable document manager that uses {@link Blueprint}s to print the
 * desired receipt or report. The blueprints are retrieved from the file
 * system and cached for reuse.
 * 
 */
public class GDYNBlueprintedDocumentManager extends BlueprintedDocumentManager implements
        GDYNBlueprintedDocumentManagerIfc
{
    private static final String BLANK_STRING = "";
    /*  code changes added by Dharmendra to fix issue POS-203 on 17/08/2016*/
    

	/*private static final String DYNAMITE = "Dynamite";

	private static final String GARAGE = "Garage";*/

	/**
     * The logger to which log messages will be sent.
     */
    private static Logger logger = Logger.getLogger(com.gdyn.orpos.pos.receipt.blueprint.GDYNBlueprintedDocumentManager.class);

    /**
     * The number of (return) receipts printed since the last customer survey printed.
     * 
     * This is a redefinition for printReceiptCount. This variable only tracks sales.
     * This variable only tracks returns.
     */
    protected static int printReceiptReturnCount = 0;

    public GDYNBlueprintedDocumentManager()
    {

    }

    /**
     * Returns the number of (return) receipts printed since the last customer survey printed.
     * 
     * @return The number of times the return receipt has printed.
     */
    public int getPrintReceiptReturnCount()
    {
        return GDYNBlueprintedDocumentManager.printReceiptReturnCount;
    }

    /**
     * Sets the number of (return) receipts printed since the last customer survey.
     * 
     * @param count
     */
    public void setPrintReceiptReturnCount(int count)
    {
        GDYNBlueprintedDocumentManager.printReceiptReturnCount = count;
    }

    /**
     * Implement Customer Reward Survey FES. This method overrides base behavior using the
     * new manager.
     * 
     * @param bus
     * @param trans
     *            - The transaction.
     */
    protected boolean isSurveyExpected(SessionBusIfc bus, TenderableTransactionIfc trans)
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
        return customerSurveyReward.isSurveyExpected(bus, trans, true);
    }

    /**
     * This method is overridden due to a bug with returns. Returns were not using the
     * mechanism to print surveys.
     * 
     * Returns an instance of the ReceiptParameterBean initialized for the given transaction.
     * 
     * @param bus
     * @param transaction
     * @return An instance of the ReceiptParameterBean initialized for the given transaciton.
     * @throws ParameterException
     */
    public ReceiptParameterBeanIfc getReceiptParameterBeanInstance(SessionBusIfc bus,
            TenderableTransactionIfc transaction)
            throws ParameterException
    {
    	
        // locale should be set by Spring
        ReceiptParameterBeanIfc receiptParameter = (ReceiptParameterBeanIfc) BeanLocator
                .getApplicationBean(ReceiptParameterBeanIfc.BEAN_KEY);
        ParameterManagerIfc pm = (ParameterManagerIfc) bus.getManager(ParameterManagerIfc.TYPE);
        UtilityManagerIfc utility = (UtilityManagerIfc) bus.getManager(UtilityManagerIfc.TYPE);
       
         String discountEmployeeNumber = null;
        
         
          if (transaction instanceof SaleReturnTransactionIfc)
        {
        	 
        	  
			SaleReturnTransactionIfc saleReturnTransaction = (SaleReturnTransactionIfc) transaction;

			/*
			 * code changes added by Dharmendra to fix issue POS-194 on
			 * 11/08/2016
			 */
			
			 //discountEmployeeNumber = utility.getEmployeeIDForEmployeeDiscountReceipt(saleReturnTransaction);
			discountEmployeeNumber = saleReturnTransaction
					.getEmployeeDiscountID();
			//logger.info("discountEmployeeNumber "+discountEmployeeNumber);
			/*
			 * code changes added by Dharmendra to fix issue POS-203 on
			 * 17/08/2016
			 */
			logger.info("Transaction Type "
					+ saleReturnTransaction.getTransactionType());

			if (Util.isEmpty(discountEmployeeNumber) == false) {
				//logger.info("before calling setRemainingSpend method");
				setRemainingSpend(saleReturnTransaction, receiptParameter);
			}

        SaleReturnLineItemIfc[] lineItems = (SaleReturnLineItemIfc[]) saleReturnTransaction.getLineItems();
            for (SaleReturnLineItemIfc lineItem : lineItems)
            {                lineItem.setReceiptDescriptionFromPLUItem(LocaleMap.getBestMatch(LocaleMap
                        .getLocale(LocaleConstantsIfc.RECEIPT)));
            }
        }
                boolean autoPrintGiftReceiptGiftRegistry =
                pm.getStringValue("AutoPrintGiftReceiptForGiftRegistry").equalsIgnoreCase("Y");
        boolean autoPrintGiftReceiptItemSend =
                pm.getStringValue("AutoPrintGiftReceiptForSend").equalsIgnoreCase("Y");
        
        receiptParameter.setTransaction(transaction);
        receiptParameter.setLocale(LocaleMap.getLocale(LocaleConstantsIfc.RECEIPT));
        
        boolean printItemTax = pm.getBooleanValue("PrintItemTax").booleanValue();
        boolean vatEnabled = Gateway.getBooleanProperty("application", "InclusiveTaxEnabled", false);
        
        receiptParameter.setPrintItemTax(printItemTax && !vatEnabled);
        receiptParameter.setAutoPrintGiftReceiptGiftRegistry(autoPrintGiftReceiptGiftRegistry);
        receiptParameter.setAutoPrintGiftReceiptItemSend(autoPrintGiftReceiptItemSend);
        
        String vatNumber = pm.getStringValue("StoresVATNumber");
        if (vatEnabled)
        {
            receiptParameter.setVATNumber(vatNumber);
        }
        else
        {
            receiptParameter.setVATNumber(null);
        }
        receiptParameter.setReceiptStyle(pm.getStringValue("VATReceiptType"));
        receiptParameter.setVATCodeReceiptPrinting(pm.getStringValue("VATCodeReceiptPrinting").equalsIgnoreCase("Y"));
        receiptParameter.setVATEnabled(vatEnabled);

        int transactionType = transaction.getTransactionType();
        switch (transactionType)
        {
            case TransactionIfc.TYPE_SALE:
            {
            	//Added below condition by Dharmendra to resolve pos-54 issue(CSAT invitation code and Short URL being generated for EE purchase) on 20/08/2015
            	if((transaction != null && transaction.getCustomer() != null && transaction.getCustomer().getEmployeeID() != null && ! transaction.getCustomer().getEmployeeID().isEmpty())){
            		receiptParameter.setSurveyShouldPrint(false);
                    }else{
                receiptParameter.setSurveyShouldPrint(isSurveyExpected(bus, transaction));
                    }
                if (Util.isEmpty(discountEmployeeNumber) == false)
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.EMPLOYEE_DISCOUNT);
                    receiptParameter.setDiscountEmployeeNumber(discountEmployeeNumber);
                }
                else
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.SALE);
                }
                SaleReturnTransactionIfc saleReturnTransaction = (SaleReturnTransactionIfc) transaction;
                receiptParameter.setPrintGiftReceipt(
                        calculatePrintGiftReceiptFlag(transaction,
                                autoPrintGiftReceiptGiftRegistry,
                                autoPrintGiftReceiptItemSend));
                receiptParameter.setPrintAlterationReceipt(saleReturnTransaction.hasAlterationItems());
                formatCreditCardPromotionalInformation(receiptParameter, saleReturnTransaction);
                receiptParameter.setTransactionHasSendItem(saleReturnTransaction.hasSendItems());
                break;
            }
            case TransactionIfc.TYPE_RETURN:
            {
                /**
                 * GD-50 CSAT: This next line is a bug in base. Returns should practice in
                 * surveys.
                 */
            	receiptParameter.setSurveyShouldPrint(isSurveyExpected(bus, transaction));
                if (Util.isEmpty(discountEmployeeNumber) == false)
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.EMPLOYEE_DISCOUNT);
                    receiptParameter.setDiscountEmployeeNumber(discountEmployeeNumber);
                }
                else
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.RETURN);
                }
                receiptParameter.setPrintGiftReceipt(
                        calculatePrintGiftReceiptFlag(transaction,
                                autoPrintGiftReceiptGiftRegistry,
                                autoPrintGiftReceiptItemSend));
                receiptParameter.setPrintAlterationReceipt(((SaleReturnTransactionIfc) transaction)
                        .hasAlterationItems());
                break;
            }
            case TransactionIfc.TYPE_REDEEM:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.REDEEM);
                break;
            }
            case TransactionIfc.TYPE_LAYAWAY_COMPLETE:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.LAYAWAY_PICKUP);
                SaleReturnTransactionIfc saleReturnTransaction = (SaleReturnTransactionIfc) transaction;
                receiptParameter.setPrintGiftReceipt(
                        saleReturnTransaction.hasGiftReceiptItems() ||
                                (saleReturnTransaction.hasSendItems() && autoPrintGiftReceiptItemSend));
                formatCreditCardPromotionalInformation(receiptParameter, saleReturnTransaction);
                break;
            }
            case TransactionIfc.TYPE_LAYAWAY_INITIATE:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.LAYAWAY);
                formatCreditCardPromotionalInformation(receiptParameter, transaction);
                break;
            }
            case TransactionIfc.TYPE_LAYAWAY_PAYMENT:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.LAYAWAY_PAYMENT);
                formatCreditCardPromotionalInformation(receiptParameter, transaction);
                break;
            }
            case TransactionIfc.TYPE_LAYAWAY_DELETE:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.LAYAWAY_DELETE);
                break;
            }
            case TransactionIfc.TYPE_HOUSE_PAYMENT:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.HOUSE_PAYMENT);
                break;
            }
            case TransactionIfc.TYPE_ORDER_COMPLETE:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.SPECIAL_ORDER_COMPLETE);
                formatCreditCardPromotionalInformation(receiptParameter, transaction);
                break;
            }
            case TransactionIfc.TYPE_ORDER_CANCEL:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.SPECIAL_ORDER_CANCEL);
                break;
            }
            case TransactionIfc.TYPE_ORDER_INITIATE:
            {
                OrderTransactionIfc orderTransaction = (OrderTransactionIfc) transaction;
                if (orderTransaction.getOrderType() == OrderConstantsIfc.ORDER_TYPE_ON_HAND)
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.PICKUP_DELIVERY_ORDER);
                    receiptParameter.setPrintGiftReceipt(
                            calculatePrintGiftReceiptFlag(transaction,
                                    autoPrintGiftReceiptGiftRegistry,
                                    autoPrintGiftReceiptItemSend));
                }
                else
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.SPECIAL_ORDER);
                }
                formatCreditCardPromotionalInformation(receiptParameter, orderTransaction);
                break;
            }
            case TransactionIfc.TYPE_ORDER_PARTIAL:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.SPECIAL_ORDER);
                formatCreditCardPromotionalInformation(receiptParameter, transaction);
                break;
            }
            case TransactionIfc.TYPE_BILL_PAY:
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.BILL_PAY);
                formatCreditCardPromotionalInformation(receiptParameter, transaction);
                break;
            }
            case TransactionIfc.TYPE_VOID:
            {
                VoidTransactionIfc voidTrans = (VoidTransactionIfc) transaction;
                TenderableTransactionIfc origTrans = voidTrans.getOriginalTransaction();
                int origTransType = origTrans.getTransactionType();

                switch (origTransType)
                {
                    case TransactionIfc.TYPE_LAYAWAY_PAYMENT:
                    case TransactionIfc.TYPE_LAYAWAY_INITIATE:
                    case TransactionIfc.TYPE_LAYAWAY_COMPLETE:
                    case TransactionIfc.TYPE_LAYAWAY_DELETE:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_LAYAWAY);
                        break;
                    }
                    case TransactionIfc.TYPE_SALE:
                    case TransactionIfc.TYPE_RETURN:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_SALE);
                        break;
                    }
                    case TransactionIfc.TYPE_REDEEM:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_REDEEM);
                        break;
                    }
                    case TransactionIfc.TYPE_PAYIN_TILL:
                    case TransactionIfc.TYPE_PAYOUT_TILL:
                    case TransactionIfc.TYPE_PAYROLL_PAYOUT_TILL:
                    case TransactionIfc.TYPE_PICKUP_TILL:
                    case TransactionIfc.TYPE_LOAN_TILL:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_TILL_ADJUSTMENTS);
                        break;
                    }
                    case TransactionIfc.TYPE_HOUSE_PAYMENT:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_HOUSE_PAYMENT);
                        break;
                    }
                    case TransactionIfc.TYPE_ORDER_INITIATE:
                    case TransactionIfc.TYPE_ORDER_CANCEL:
                    case TransactionIfc.TYPE_ORDER_PARTIAL:
                    case TransactionIfc.TYPE_ORDER_COMPLETE:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_SPECIAL_ORDER);
                        break;
                    }
                    case TransactionIfc.TYPE_BILL_PAY:
                    {
                        receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.VOID_BILL_PAY);
                    }
                    default:
                    {
                        logger.warn("No Void Receipt Found");
                        break;
                    }
                }
                break;
            }
            default:
            {
                logger.warn("No Receipt Found");
                break;
            }
        }
        // account for transactions of status STATUS_CANCELED
        if (transaction.getTransactionStatus() == TransactionIfc.STATUS_CANCELED)
        {
            if (transaction.getTransactionType() == TransactionIfc.TYPE_PICKUP_TILL)
            {
                // till pickup and loan knows how to prints its own cancel
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.TILLPICKUP);
            }
            else if (transaction.getTransactionType() == TransactionIfc.TYPE_LOAN_TILL)
            {
                // till pickup and loan knows how to prints its own cancel
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.TILLLOAN);
            }
            else
            {
                // canceled transactions should all use this receipt
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.CANCELED);
            }
            // no gift receipt for canceled transactions
            receiptParameter.setAutoPrintGiftReceiptGiftRegistry(false);
            receiptParameter.setAutoPrintGiftReceiptItemSend(false);
            receiptParameter.setPrintGiftReceipt(false);
        }
        else if (transaction instanceof RetailTransactionIfc)
        {
            if (((RetailTransactionIfc) transaction).getTransactionTax() != null
                    && ((((RetailTransactionIfc) transaction).getTransactionTax().getTaxMode() == TaxIfc.TAX_MODE_EXEMPT) ||
                    (isPartialTaxExempt((RetailTransactionIfc)transaction))))
            {
                if (!(transactionType == TransactionIfc.TYPE_ORDER_INITIATE
                        || transactionType == TransactionIfc.TYPE_ORDER_PARTIAL
                        || transactionType == TransactionIfc.TYPE_LAYAWAY_INITIATE
                        || transactionType == TransactionIfc.TYPE_LAYAWAY_COMPLETE
                        // IGNITIV: BUG#683752 ­ Unretrieved returns with Full Tax Exemption - missing store receipt copy
                        || transactionType == TransactionIfc.TYPE_RETURN)
                        // BUG#10186755 TaxExemption receipt printed for voided transactions
                        && !(transaction instanceof VoidTransactionIfc))
                {
                    receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.TAX_EXEMPT);
                }
            }
        }
        // account for transactions of TYPE_EXCHANGE
        if (receiptParameter.getTransactionType() == TransactionConstantsIfc.TYPE_EXCHANGE)
        {
            if (Util.isEmpty(discountEmployeeNumber) == false)
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.EMPLOYEE_DISCOUNT);
                receiptParameter.setDiscountEmployeeNumber(discountEmployeeNumber);
            }
            else
            {
                receiptParameter.setDocumentType(ReceiptTypeConstantsIfc.EXCHANGE);
            }
        }
        // Read Group Like Items on Receipt parameter value.
        receiptParameter.setGroupLikeItems(pm.getBooleanValue(ParameterConstantsIfc.PRINTING_GroupLikeItemsOnReceipt));
        
        GDYNShowSaleScreenSite.REMAINING_SPEND_STRING="";
        GDYNShowSaleScreenSite.responseObjectsEmp = null;
        GDYNLoyalityConstants.isLoyalityCpnExists = false;
        GDYNLoyalityConstants.minThreshHoldAmt = BigDecimal.ZERO;
        
       return receiptParameter;
    }
    
    protected boolean isPartialTaxExempt(RetailTransactionIfc transaction) {
    	
    	boolean isPartialTaxExempt = false;
    	
    	if ((transaction.getTransactionTax() != null) && 
    			((transaction.getTransactionTax() instanceof GDYNTransactionTaxIfc)))
    	{
    		GDYNTransactionTaxIfc transactionTax = (GDYNTransactionTaxIfc)transaction.getTransactionTax();
    		if ((transactionTax.getCustomerCode() != null) && 
    				(!Util.isEmpty(transactionTax.getCustomerCode().getApplicationMethod()))) {
    			isPartialTaxExempt = transactionTax.getCustomerCode().getApplicationMethod().equalsIgnoreCase("partial");
    		}
    	}
    	return isPartialTaxExempt;
    }
    
	public void setRemainingSpend(SaleReturnTransactionIfc transaction,
			ReceiptParameterBeanIfc receiptParameter) {
		//logger.info("setRemainingSpend From Blueprint Document Manager Entered"+ GDYNShowSaleScreenSite.REMAINING_SPEND_STRING);
		
		
		receiptParameter.setRemainingSpend(GDYNShowSaleScreenSite.REMAINING_SPEND_STRING);
		GDYNShowSaleScreenSite.REMAINING_SPEND_STRING = "";
		GDYNEmployeeDiscountUtility.responseObjects= null;
	}
	

    /*public String configureRemSpendString(BigDecimal grySpendBD,BigDecimal dynSpendBD){
    	String remSpendString = " GRG "+grySpendBD+"  "+"DYN "+dynSpendBD ;
    	return remSpendString;
    	
    } */
}
