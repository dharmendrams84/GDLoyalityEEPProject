//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.modifytransaction;

import java.util.Vector;

import com.gdyn.orpos.pos.common.GDYNCommonActionsIfc;

import oracle.retail.stores.domain.employee.RoleFunctionIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.manager.ifc.SecurityManagerIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.NavigationButtonBeanModel;
import oracle.retail.stores.pos.services.common.CommonActionsIfc;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;

/**
 * Extending this site for Groupe Dynamite enhancements.
 * - Tax Exemptions: Enable/disable button per non retrieved return
 * 
 */
public class GDYNModifyTransactionOptionsSite extends PosSiteActionAdapter
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -4788945885571894144L;

    /**
     * Tax inclusive flag
     */
    protected boolean taxInclusiveFlag = Gateway.getBooleanProperty("application", "InclusiveTaxEnabled", false);

    /**
     * Shows the screen for all the options for ModifyTransaction
     * 
     * @param bus
     *            Service Bus
     */
    @Override
    public void arrive(BusIfc bus)
    {
        // get the POS UI manager
        POSUIManagerIfc uiManager = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
        GDYNModifyTransactionCargo cargo = (GDYNModifyTransactionCargo) bus.getCargo();

        // This is the void rule:
        //
        // 1. If there is a transaction of any sort, transaction void cannot be perfomed.
        //
        // These are the suspend/retrieve rules:
        //
        // 1. If the system is not running POS (i.e. CrossReach),
        // the suspend/retrieve buttons will be disabled.
        //
        // 2. If there is a transaction in the cargo, then the suspend
        // will be enabled and the retrieve disabled.
        //
        // 3. If there is NOT a transaction in the cargo, then the suspend
        // will be disabled and the retrieve enabled.
        //
        // This code sets the booleans to the correct vales before setting up the
        // models.

        // Initialize the booleans
        boolean voidEnabled = false;
        boolean suspendEnabled = false;
        boolean retrieveEnabled = false;
        boolean layawayEnabled = true;
        boolean giftRegistryEnabled = true;
        boolean giftReceiptEnabled = true;
        boolean sendEnabled = true;
        boolean specialOrderEnabled = false;
        boolean itemBasketEnabled = false; // to be changed to True
        boolean externalOrderEnabled = false; // to be changed to True
        boolean taxExemptEnabled = false;

        RetailTransactionIfc transaction = cargo.getTransaction();

        boolean isReEntryMode = cargo.getRegister().getWorkstation().isTransReentryMode();

        itemBasketEnabled = Gateway.getBooleanProperty("application", "ItemBasketEnabled", false);
        externalOrderEnabled = Gateway.getBooleanProperty("application", "ExternalOrderEnabled", false);
        boolean billPayEnabled = false;
        if (isBillPaySupported(bus))
        {
            billPayEnabled = true;
        }

        // If there is not a transaction, set void to true
        if (transaction == null)
        {
            voidEnabled = true;
            specialOrderEnabled = true;
        }

       /* Code changes added by Dharmendra to fix the POS-193 issue on 11-Aug-2016*/
		if (transaction != null) {
			SaleReturnTransactionIfc saleReturnTransactionIfc = (SaleReturnTransactionIfc) transaction;
			String discountEmployeeId = saleReturnTransactionIfc
					.getEmployeeDiscountID();

			if (discountEmployeeId != null
					&& !"".equalsIgnoreCase(discountEmployeeId)) {
				giftReceiptEnabled = false;
			}
		}
        // If there is not a transaction set retrieve to true
        if (transaction == null)
        {
            retrieveEnabled = true;
            giftReceiptEnabled = false;
        }
        // If there is a transaction in progress with atleast one line item present,
        // then set suspend to true
        else if (transaction.getLineItemsVector() != null && transaction.getLineItemsVector().size() > 0)
        {
            suspendEnabled = true;
        }

        if (transaction != null)
        {
            // If return items, send items, CrossReach items, or layaway in progress
            // disable Layaway button
            if ((transaction.containsOrderLineItems() ||
                    transaction.getTransactionType() != TransactionIfc.TYPE_SALE ||
                    transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_INITIATE ||
                    ((SaleReturnTransactionIfc) transaction).hasSendItems() ||
                    transaction.getTransactionTotals().isTransactionLevelSendAssigned() || ((SaleReturnTransactionIfc) transaction)
                        .containsReturnLineItems()))
            {
                layawayEnabled = false;
            }

            // do not allow gift registration on returned items or new orders
            if (((SaleReturnTransactionIfc) transaction).containsReturnLineItems() ||
                    transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_INITIATE)
            {
                giftRegistryEnabled = false;
                giftReceiptEnabled = false;
            }

            if ((transaction.getTransactionTotals().isTransactionLevelSendAssigned()) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_INITIATE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_PAYMENT) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_COMPLETE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_DELETE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_INITIATE))
            {
                sendEnabled = false;
            }

            if ((transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_INITIATE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_PAYMENT) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_COMPLETE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_LAYAWAY_DELETE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_RETURN) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_INITIATE))
            {
                externalOrderEnabled = false;
            }

            if (transaction.getIsItemBasketTransactionComplete())
            {
                itemBasketEnabled = false; // it Item basket trn done once, Donot enable the Item Basket Button
            }
            specialOrderEnabled = false;
            layawayEnabled = false;
            billPayEnabled = false;

            // If transaction has already external order, disable the external order button
            if (((SaleReturnTransactionIfc) transaction).hasExternalOrder())
            {
                externalOrderEnabled = false;
                // if externalorder has send package disable send button
                if (((SaleReturnTransactionIfc) transaction).hasExternalSendPackage())
                {
                    sendEnabled = false;
                }
            }

            // Tax Exemptions
            /**
             * Enable the tax exempt button when the transaction contains a retrieved return item
             * with one positive tax amount.
             * Enable the tax exempt button when the transaction contains only sale items or the 
             * return items have not been retrieved from a receipt.
             */
            if ((transaction.getTransactionType() == TransactionIfc.TYPE_SALE) ||
                    (transaction.getTransactionType() == TransactionIfc.TYPE_RETURN))
            {
                if (((SaleReturnTransactionIfc) transaction).containsReturnLineItems())
                {
                    SaleReturnTransactionIfc saleTransaction = (SaleReturnTransactionIfc) transaction;

                    if (saleTransaction.getLineItemsVector() != null)
                    {
                        Vector<AbstractTransactionLineItemIfc> saleLineItems = saleTransaction.getLineItemsVector();
                        for (int i = 0; i < saleLineItems.size(); i++)
                        {
                            SaleReturnLineItemIfc lineItem = (SaleReturnLineItemIfc) saleLineItems.get(i);
                            if (lineItem.isReturnLineItem())
                            {
                                if (lineItem.getReturnItem() != null)
                                {
                                    if (!lineItem.getReturnItem().isFromRetrievedTransaction())
                                    {
                                        taxExemptEnabled = true;
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                taxExemptEnabled = true;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    taxExemptEnabled = true;
                }
            }
        }

        // limit functionality if in reentry mode
        if (isReEntryMode)
        {
            itemBasketEnabled = false;
            suspendEnabled = false;
            retrieveEnabled = false;
            billPayEnabled = false;
            externalOrderEnabled = false;
        }

        if (cargo.getRegister().getWorkstation().isTrainingMode())
        {
            specialOrderEnabled = false;
            layawayEnabled = false;
        }
        
        SaleReturnTransactionIfc saleReturnTransactionIfc = (SaleReturnTransactionIfc)transaction;
        
        /*code changes added by Dharmendra on 08/09/2016 to fix issue POS-228*/
        
        if(saleReturnTransactionIfc!=null&&!Util.isEmpty(saleReturnTransactionIfc.getEmployeeDiscountID())){
        	suspendEnabled = false;
        }
        // Setup the models.
        POSBaseBeanModel pModel = new POSBaseBeanModel();
        NavigationButtonBeanModel nModel = new NavigationButtonBeanModel();
        nModel.setButtonEnabled(CommonActionsIfc.VOID, voidEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.SUSPEND, suspendEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.RETRIEVE, retrieveEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.LAYAWAY, layawayEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.GIFT_REGISTRY, giftRegistryEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.GIFT_RECEIPT, giftReceiptEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.SEND, sendEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.SPECIAL_ORDER, specialOrderEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.ITEM_BASKET, itemBasketEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.EXTERNAL_ORDER, externalOrderEnabled);
        nModel.setButtonEnabled(CommonActionsIfc.BILL_PAY, billPayEnabled);
        nModel.setButtonEnabled(GDYNCommonActionsIfc.EXEMPT, taxExemptEnabled);
        // disable the button in the VAT enabled environment
        // This is no longer necessary due to the exemption flow changes.
        // if (taxInclusiveFlag)
        // {
        // nModel.setButtonEnabled(CommonActionsIfc.TAX, false);
        // }
        pModel.setLocalButtonBeanModel(nModel);

        uiManager.showScreen(POSUIManagerIfc.TRANS_OPTIONS, pModel);
    }

    /**
     * This method checks whether the Billpay feature is supported or not.
     */
    public boolean isBillPaySupported(BusIfc bus)
    {
        boolean supported = false;

        try
        {
            GDYNModifyTransactionCargo cargo = (GDYNModifyTransactionCargo) bus.getCargo();

            // 1. Check whether user has access to billpay function
            SecurityManagerIfc securityManager = (SecurityManagerIfc) Gateway.getDispatcher().getManager(
                    SecurityManagerIfc.TYPE);
            boolean access = securityManager.checkAccess(cargo.getAppID(), RoleFunctionIfc.BILLPAY);

            // 2. Check wthether billpay is enabled or not
            Boolean installedFlag = new Boolean(Gateway.getProperty("application", "BillPayEnabled", "false"));

            // 3. Check whether the training mode option is on or off
            boolean isTrainingMode = cargo.getRegister().getWorkstation().isTrainingMode();

            if (access && installedFlag.booleanValue() && !isTrainingMode)
            {
                supported = true;
            }
        }
        catch (Exception e)
        {
            logger.warn("Error while getting Billpay Supported Flags");
            supported = false;
        }

        return supported;
    }

}
