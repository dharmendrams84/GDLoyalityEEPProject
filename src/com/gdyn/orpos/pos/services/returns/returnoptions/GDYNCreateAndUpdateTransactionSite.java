//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.services.returns.returnoptions;

import java.math.BigDecimal;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.commerceservices.externalorder.ExternalOrderItemIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.lineitem.ReturnItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.WorkstationIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.utility.LocaleUtilities;
import oracle.retail.stores.foundation.manager.ifc.JournalManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.config.bundles.BundleConstantsIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.common.CPOIPaymentUtility;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.services.returns.returncommon.ReturnData;
import oracle.retail.stores.pos.services.returns.returnoptions.CreateAndUpdateTransactionSite;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;

import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
/**
 * This creates a transaction if needed and updates the transaction with the
 * return items.
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/9 $
 */

// // GD-287 Tender button not disabled when selecting an item from an "exchange" transaction
// or if balance is negative in non-retrieval
public class GDYNCreateAndUpdateTransactionSite extends CreateAndUpdateTransactionSite
{
    /**
     * Generated serialVersionUID
     */
    private static final long serialVersionUID = -980758344975890155L;
    /**
     * site name constant
     */
    public static final String SITENAME = "GDYNCreateAndUpdateTransactionSite";
    /**
     * Tells if transaction has both sale and return items
     */
    protected boolean exchangeTransaction = false;

    @Override
    public void arrive(BusIfc bus)
    {
        // Get the cargo and current transaction
        GDYNReturnOptionsCargo cargo = (GDYNReturnOptionsCargo) bus.getCargo();
        Letter letter = new Letter(CommonLetterIfc.FAILURE);

        // Get the return item information from the cargo
        ReturnData returnData = cargo.getReturnData();

        ReturnItemIfc[] returnItems = null;
        PLUItemIfc[] pluItems = null;
        SaleReturnLineItemIfc[] saleReturnLineItems = null;

        if (returnData != null)
        {
            returnItems = returnData.getReturnItems();
            pluItems = returnData.getPLUItems();
            saleReturnLineItems = returnData.getSaleReturnLineItems();
        }
        
        // added by Mansi for fixing RTlog TaxID : Expiration date issue
        GDYNSaleReturnTransactionIfc originalTransaction = (GDYNSaleReturnTransactionIfc) cargo.getOriginalTransaction();

        if (returnItems != null && returnItems.length != 0)
        {
            GDYNSaleReturnTransactionIfc transaction = (GDYNSaleReturnTransactionIfc) cargo.getTransaction();
            
            // If there is no transaction ....
            if (transaction == null)
            {
                UtilityManagerIfc utility =
                        (UtilityManagerIfc) bus.getManager(UtilityManagerIfc.TYPE);

                transaction =
                        (GDYNSaleReturnTransactionIfc) DomainGateway.getFactory().getSaleReturnTransactionInstance();

                /*
                 * Modified by vivek to add the original loyalty ID and the loyalty Enable to the transaction object.
                 */
                /*else if block added by Dharmendra to fix POS-371*/
                if(originalTransaction!=null && originalTransaction.getOriginalLoyaltyID()!=null)
                {
                transaction.setOriginalLoyaltyID(originalTransaction.getOriginalLoyaltyID());
   
                transaction.setLoyaltyEnable(cargo.isLoyaltyEnable());
                }else if(originalTransaction==null &&!GDYNLoyalityConstants.isEmpty(cargo.getOriginalLoyaltyID())){
                	transaction.setOriginalLoyaltyID(cargo.getOriginalLoyaltyID());
                }
                // end modification by vivek
                
                transaction.setSalesAssociate(cargo.getSalesAssociate());
                utility.initializeTransaction(transaction, bus, GENERATE_SEQUENCE_NUMBER);
                
                if ((cargo.getOriginalTransaction() != null) 
                		&& (cargo.getOriginalTransaction().getTransactionTax() != null) 
                		&& (cargo.getOriginalTransaction() instanceof GDYNSaleReturnTransactionIfc)
                		&& ((cargo.getOriginalTransaction().getTransactionTax().getTaxMode() == TaxIfc.TAX_MODE_EXEMPT) 
                			|| (cargo.getOriginalTransaction().getTransactionTax().getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)))
                {
                		((GDYNTransactionTaxIfc)transaction.getTransactionTax()).setBandRegistryId(((GDYNTransactionTaxIfc)originalTransaction.getTransactionTax()).getBandRegistryId());
                		((GDYNTransactionTaxIfc)transaction.getTransactionTax()).setIdExpirationDate(((GDYNTransactionTaxIfc)originalTransaction.getTransactionTax()).getIdExpirationDate());

                		//Ashwini Added: to get correct reson code on DB and RTlogs for retrieved returns with tax exempts
                		((GDYNTransactionTaxIfc)transaction.getTransactionTax()).getReason().setCode(((GDYNTransactionTaxIfc)originalTransaction.getTransactionTax()).getReason().getCode());
                		
                		transaction.setOriginalTransactionTax(cargo.getOriginalTransaction().getTransactionTax());
                	}

                	cargo.setTransaction(transaction);
                }

            // Get the line items from the retrieve transaction
            // Process each return line item
            for (int i = 0; i < returnData.getReturnItems().length; i++)
            {
                // Prepare line item.
                SaleReturnLineItemIfc srli = null;

                ReturnItemIfc returnItem = returnItems[i];

                if (returnItem != null)
                {
                    BigDecimal quantityReturned = returnItems[i].getItemQuantity().negate();
                    BigDecimal quantityReturnable = returnItems[i].getQuantityReturnable();

                    // If this is a manual return ...
                    if (!returnItem.isFromRetrievedTransaction())
                    {
                        // Add the line item
                        if (cargo.isExternalOrder())
                        {
                            ExternalOrderItemIfc eoi = pluItems[i].getReturnExternalOrderItem();
                            srli = transaction.addReturnItem(pluItems[i], returnItem, eoi);
                            srli.setExternalOrderItemID(eoi.getId());
                            srli.setExternalOrderParentItemID(eoi.getParentId());
                        }
                        else
                        {
                            srli = transaction.addReturnItem(pluItems[i], returnItem, quantityReturned);
                        }

                        if (!Util.isEmpty(saleReturnLineItems[i].getItemSerial()))
                        {
                            srli.setItemSerial(saleReturnLineItems[i].getItemSerial());
                        }
                        
                        // copy entry method from return item
                        srli.setEntryMethod(returnItem.getEntryMethod());
                        journalLine(bus, cargo, pluItems, i, srli);
                    }
                    // If this is a return based on a retrieved transaction
                    else
                    {
                        // Use the Sale Return item from the transaction
                        srli = saleReturnLineItems[i];

                        // This fix was made to make sure the price reaching RM from POS is Discounted Price and not
                        // Selling Price.
                        // Makinf sure there is no null pointer and Deivide by Zero.
                        if (srli.getExtendedDiscountedSellingPrice() != null
                                && srli.getItemPrice().getItemQuantityDecimal() != null
                                && srli.getItemPrice().getItemQuantityDecimal().intValue() > 0)
                        {
                            CurrencyIfc price = srli.getExtendedDiscountedSellingPrice().abs()
                                    .divide(srli.getItemPrice().getItemQuantityDecimal());
                            returnItem.setPrice(price);
                        }

                        if (srli.isGiftReceiptItem())
                        {
                            returnItem.setFromGiftReceipt(srli.isGiftReceiptItem());
                        }

                        srli.setReturnItem(returnItem);
                        // Set the send label count to zero. Shipment addresses do not print on
                        // the receipt for returns.
                        srli.setSendLabelCount(0);

                        // set the restocking fee from the return item in the item price
                        srli.getItemPrice().setRestockingFee(returnItem.getRestockingFee());

                        srli.modifyItemQuantity(quantityReturned);

                        // convert transaction discounts to item discounts (if needed)
                        SaleReturnLineItemIfc srliRemainder = convertTransactionDiscounts(transaction, srli,
                                returnData, i, quantityReturned, quantityReturnable);

                        // the srli was split up to account for some rounding issues, new elements were added to the
                        // returnData's arrays by
                        // convertTransactionDiscounts(), so, updated references
                        if (srliRemainder != null)
                        {
                            returnItems = returnData.getReturnItems();
                            pluItems = returnData.getPLUItems();
                            saleReturnLineItems = returnData.getSaleReturnLineItems();
                        }

                        transaction.addLineItem(srli);
                        addTax(transaction, srli, returnItem);
                        addItemToDevice(bus, transaction, srli);
                        journalLine(bus, cargo, pluItems, i, srli);

                        // the srli was split up, add the new srli to the transaction, increment the counter so it
                        // not processed twice
                        if (srliRemainder != null)
                        {
                            i++;
                            transaction.addLineItem(srliRemainder);
                            addTax(transaction, srliRemainder, returnItem);
                            addItemToDevice(bus, transaction, srliRemainder);
                            journalLine(bus, cargo, pluItems, i, srliRemainder);
                        }
                    } // no receipt (manual) vs. receipt (isFromRetrievedTransaction)
                } // returnItem != null
            } // loop over returnItems

            // Begin GD-287: Tender button not disabled when selecting an item from an "exchange"
            // transaction or if balance is negative in non-retrieval.
            // true if the original transaction was an exchange
            transaction.setOriginalExchangeTransaction(cargo.isOriginalExchangeTransaction());
            // End GD-287

            // Add retrieved tranaction(s) to the array of original transactions.
            updateOriginalTransactionArray(cargo);

            // Add tenders from the retrieved tranaction(s) to the array of
            // original tenders.
            updateOriginalTendersArray(cargo);

            // Set the customer to the transaction and the status panel.
            CustomerIfc customer = getCustomer(transaction, cargo);

            if (customer != null)
            {
                transaction.setCustomer(customer);
                // set the customer's name in the status area
                POSUIManagerIfc ui = (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);

                UtilityManagerIfc utility =
                        (UtilityManagerIfc) bus.getManager(UtilityManagerIfc.TYPE);
                Object parms[] = { customer.getFirstName(), customer.getLastName() };
                String pattern = utility.retrieveText("CustomerAddressSpec",
                        BundleConstantsIfc.CUSTOMER_BUNDLE_NAME,
                        CUSTOMER_NAME_TAG,
                        CUSTOMER_NAME_TEXT);
                String customerName = LocaleUtilities.formatComplexMessage(pattern, parms);

                ui.customerNameChanged(customerName);
            }

            // Indicate to the return shuttle that data should be transfered to the
            // calling service.
            cargo.setTransferCargo(true);
            letter = new Letter(CommonLetterIfc.SUCCESS);
        }

        bus.mail(letter, BusIfc.CURRENT);
    }

    /**
     * journal the new srli
     * 
     * 
     * @param bus
     * @param cargo
     * @param pluItems
     * @param index
     * @param srli
     */
    private void journalLine(BusIfc bus, GDYNReturnOptionsCargo cargo, PLUItemIfc[] pluItems, int index,
            SaleReturnLineItemIfc srli)
    {
        // Journal each item.
        JournalManagerIfc jm = (JournalManagerIfc) Gateway.getDispatcher().
                getManager(JournalManagerIfc.TYPE);
        if (jm != null)
        {
            UtilityManagerIfc utility = (UtilityManagerIfc) bus.getManager(UtilityManagerIfc.TYPE);
            journalLineItem(jm, srli, pluItems[index], cargo, utility);
        }
    }

    /**
     * display new srli on the device
     * 
     * 
     * @param bus
     * @param srli
     */
    private void addItemToDevice(BusIfc bus, SaleReturnTransactionIfc transaction, SaleReturnLineItemIfc srli)
    {
        GDYNReturnOptionsCargo cargo = (GDYNReturnOptionsCargo) bus.getCargo();
        WorkstationIfc workstation = cargo.getRegister().getWorkstation();
        CPOIPaymentUtility.getInstance().addItem(workstation, srli, transaction);

    }

    /**
     * add tax to the new srli
     * 
     * @param transaction
     * @param srli
     * @param ri
     */
    private void addTax(SaleReturnTransactionIfc transaction, SaleReturnLineItemIfc srli,
            ReturnItemIfc ri)
    {
        // Add the line item to the transaction. When line items are added to the list
        // the transaction tax is recalculated. We will need to negate the tax values at this point.
        SaleReturnLineItemIfc[] liList = (SaleReturnLineItemIfc[]) transaction.getLineItems();
        srli = liList[liList.length - 1];
        if (srli.getItemTax().getTaxMode() != TaxIfc.TAX_MODE_EXEMPT)
        {
            srli.getItemTax().setDefaultRate(ri.getTaxRate());
            if (srli.getItemTax().getTaxScope() == TaxIfc.TAX_SCOPE_TRANSACTION)
            {
                // force item scope here
                srli.getItemTax().setTaxScope(TaxIfc.TAX_SCOPE_ITEM);
            }
        }
    }
}
