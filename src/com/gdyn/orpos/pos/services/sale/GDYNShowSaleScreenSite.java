//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.services.sale;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.comparators.Comparators;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.financial.ShippingMethodIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.ReturnItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.manager.ifc.PaymentManagerIfc;
import oracle.retail.stores.domain.order.OrderConstantsIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.WorkstationIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.transaction.OrderTransaction;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionTotalsIfc;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.gui.UIException;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.service.SessionBusIfc;
import oracle.retail.stores.foundation.utility.LocaleMap;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.config.bundles.BundleConstantsIfc;
import oracle.retail.stores.pos.device.POSDeviceActions;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.common.CPOIPaymentUtility;
import oracle.retail.stores.pos.services.common.CommonActionsIfc;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.services.sale.SaleCargoIfc;
import oracle.retail.stores.pos.services.sale.ShowSaleScreenSite;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.UIUtilities;
import oracle.retail.stores.pos.ui.beans.LineItemsModel;
import oracle.retail.stores.pos.ui.beans.NavigationButtonBeanModel;
import oracle.retail.stores.pos.ui.beans.PromptAndResponseModel;
import oracle.retail.stores.pos.ui.beans.StatusBeanModel;
import oracle.retail.stores.pos.ui.beans.TotalsBeanModel;
import oracle.retail.stores.pos.ui.timer.DefaultTimerModel;

import org.apache.log4j.Logger;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.pos.common.GDYNCommonActionsIfc;
import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;

import com.gdyn.orpos.pos.eep.GDYNEepConstants;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.pricing.GDYNPricingCargo;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNEmployeeDiscountUtility;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNFindEmployeeNumberAisle;
import com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNLoyalityDiscountUtility;

/**
 * This site displays the SELL_ITEM screen.
 */
@SuppressWarnings("deprecation")
public class GDYNShowSaleScreenSite extends ShowSaleScreenSite {
	private static final String BLANK_STRING = "";
       public static  String REMAINING_SPEND_STRING = "";
	/*
	 * private static final String DYNAMITE = "Dynamite";
	 * 
	 * private static final String GARAGE = "Garage";
	 */

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3890372067179784543L;
	
	public static  GDYNEmployeeDiscResponseObject[] responseObjectsEmp = null;

	/**
	 * Logger for debugging this site.
	 */
	protected static final Logger logger = Logger
			.getLogger(GDYNShowSaleScreenSite.class);

	/**
	 * This timestamp is recorded when this site leaves. When this site arrives
	 * again, the current timestamp is compared against this one for a duration.
	 * This will effectively denote how much time it takes to scan an item.
	 */
	private long debugRoundtripTimestamp;

	/**
	 * Displays the SELL_ITEM screen.
	 * 
	 * @param bus
	 *            Service Bus
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public void arrive(BusIfc bus) {

		boolean reEntryOn = false;
		// Begin GD-31 Prevent any tender refund for returns without a receipt
		boolean disallowReturn = false;
		boolean disallowNoReceiptReturnTender = false;

		// End GD-31
		// grab the transaction (if it exists)
		SaleCargoIfc cargo = (SaleCargoIfc) bus.getCargo();
		// get device actions for linedisplay and msr controls
		POSDeviceActions pda = new POSDeviceActions((SessionBusIfc) bus);

		UtilityManagerIfc utility = (UtilityManagerIfc) bus
				.getManager(UtilityManagerIfc.TYPE);
		ParameterManagerIfc pm = (ParameterManagerIfc) bus
				.getManager(ParameterManagerIfc.TYPE);

		boolean imeiEnabled = utility.getIMEIProperty();
		boolean serializationEnabled = utility.getSerialisationProperty();
		String imeiResponseFieldLength = utility.getIMEIFieldLengthProperty();

		// Setup bean models information for the UI to display
		LineItemsModel beanModel = new LineItemsModel();
		reEntryOn = cargo.getRegister().getWorkstation().isTransReentryMode();
		// beanModel.setMoveHighlightToTop(true); - CR 4801
		NavigationButtonBeanModel localModel = new NavigationButtonBeanModel();
		NavigationButtonBeanModel globalModel = new NavigationButtonBeanModel();
		StatusBeanModel statusModel = new StatusBeanModel();
		TotalsBeanModel tbm = new TotalsBeanModel();
		GDYNSaleReturnTransactionIfc transaction = (GDYNSaleReturnTransactionIfc) cargo
				.getTransaction();

		// Reset the transaction status to "In Progress" on returning of 'No'
		// from cancel transaction prompt
		if (bus.getCurrentLetter().getName() != null
				&& bus.getCurrentLetter().getName()
						.equals(CommonLetterIfc.FAILURE)
				&& cargo.getTransaction() != null
				&& cargo.getTransaction().getTransactionStatus() == TransactionConstantsIfc.STATUS_CANCELED
				&& cargo.getTransaction().getPreviousTransactionStatus() == TransactionConstantsIfc.STATUS_IN_PROGRESS) {
			cargo.getTransaction().setTransactionStatus(
					TransactionConstantsIfc.STATUS_IN_PROGRESS);
		}

		AbstractTransactionLineItemIfc[] lineItems = null;
		ArrayList<AbstractTransactionLineItemIfc> itemList = new ArrayList<AbstractTransactionLineItemIfc>();
		OrderTransaction orderTransaction = null;
		if (transaction instanceof OrderTransaction) {
			orderTransaction = (OrderTransaction) transaction;
			if (orderTransaction.getOrderType() == OrderConstantsIfc.ORDER_TYPE_SPECIAL) {
				localModel.setButtonEnabled(CommonActionsIfc.GIFT_CARD_CERT,
						false);
			}
		}

		// Check for Cash drawer UNDER Warning
		if (cargo.isCashDrawerUnderWarning()) {
			statusModel.setBCashDrawerWarningReqd(true);
			cargo.setCashDrawerUnderWarning(false);
		}
		/*
		 * if (transaction == null || transaction.getEmployeeDiscountID() ==
		 * null) { System.out.println(
		 * "transaction == null || transaction.getEmployeeDiscountID() == null  "
		 * +new java.util.Date());
		 * statusModel.setEmpDiscountApplied(Boolean.FALSE);
		 * //statusModel.setSalesAssociateName(""); }
		 * 
		 * if (transaction != null) {
		 * System.out.println(transaction.getTransactionID()
		 * +"transaction != null  "+new java.util.Date());
		 * if(transaction.getEmployeeDiscountID() != null){
		 * System.out.println(transaction
		 * .getEmployeeDiscountID()+" transaction.getEmployeeDiscountID() != null  "
		 * +new java.util.Date()); if(transaction.getLineItemsVector()== null ||
		 * transaction.getLineItemsVector().size()==0){
		 * System.out.println("no line items for an employee transaction "+new
		 * java.util.Date()); //statusModel.setSalesAssociateName(""); } } }
		 */
		/*if (transaction == null || transaction.getEmployeeDiscountID() == null) {
			// System.out.println("transaction == null || transaction.getEmployeeDiscountID() == null  "+new
			// java.util.Date());
			statusModel.setEmpDiscountApplied(Boolean.FALSE);
			statusModel.setSalesAssociateName(BLANK_STRING);
		}

		if (transaction != null && transaction.getEmployeeDiscountID() != null) {
			// System.out.println("transaction != null && transaction.getEmployeeDiscountID() != null  "+new
			// java.util.Date());
			statusModel.setEmpDiscountApplied(Boolean.TRUE);
		}
*/
		
		if (transaction == null || transaction.getEmployeeDiscountID() == null
				|| transaction.getEmployeeDiscountID().length() == 0) {
			statusModel.setEmpDiscountApplied(Boolean.FALSE);

		}
	       
		
		if(transaction!=null &&(transaction.getEmployeeDiscountID() == null || transaction.getEmployeeDiscountID().length()==0)){
        	if(transaction.getSalesAssociate()!=null && transaction.getSalesAssociate().getFullName()!=null&&
        			transaction.getSalesAssociate().getFullName().length()!=0){
        		statusModel.setSalesAssociateName(transaction.getSalesAssociate().getFullName());
        	}
        }
        if (transaction != null && transaction.getEmployeeDiscountID() != null) {
        //	System.out.println("transaction != null && transaction.getEmployeeDiscountID() != null  "+new java.util.Date());
			statusModel.setEmpDiscountApplied(Boolean.TRUE);
		}
		// Reset locale to default values
		if (transaction == null || transaction.getCustomer() == null) {
			Locale defaultLocale = LocaleMap
					.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);
			UIUtilities.setUILocaleForCustomer(defaultLocale);
		}

		if (reEntryOn) {
			localModel.setButtonEnabled(CommonActionsIfc.REDEEM, false);
		}

		if (transaction != null) {
			// Disable Redeem button when transaction != null
			localModel.setButtonEnabled(CommonActionsIfc.REDEEM, false);
			itemList.addAll(Arrays.asList(transaction
					.getLineItemsExceptExclusions()));

			// sort the line items list by line number
			Collections.sort(itemList, Comparators.lineNumberAscending);
		}
		beanModel
				.setTimerModel(new DefaultTimerModel(bus, transaction != null));

		if (transaction != null && transaction.getSalesAssociate() != null) {
			if (transaction.getSalesAssociateModifiedFlag()) {
				for (int i = itemList.size() - 1; i >= 0; i--) {
					itemList.get(i).setSalesAssociateModifiedFlag(true);

					if (!Util.isObjectEqual(
							itemList.get(i).getSalesAssociate(),
							transaction.getSalesAssociate())) {
						itemList.get(i).setSalesAssociateModifiedAtLineItem(
								true);
					}
				}
			}

		}

		lineItems = new AbstractTransactionLineItemIfc[itemList.size()];
		itemList.toArray(lineItems);

		if (islineItemContainReturnableItem(lineItems)) {
			disallowReturn = true;
			for (int i = 0; i < lineItems.length; i++) {
				SaleReturnLineItemIfc saleReturnItem = (SaleReturnLineItemIfc) lineItems[i];
				saleReturnItem.setHasReturnItem(true);
				// Begin GD-31 Prevent any tender refund for returns without a
				// receipt
				// disable any other returns
				ReturnItemIfc returnItem = saleReturnItem.getReturnItem();
				if (returnItem != null && !returnItem.haveReceipt()) {
					disallowNoReceiptReturnTender = true;
				}
				// End GD-31
			}
		}

		if (islineItemContainSendItem(lineItems)) {
			for (int i = 0; i < lineItems.length; i++) {
				SaleReturnLineItemIfc saleReturnItem = (SaleReturnLineItemIfc) lineItems[i];
				saleReturnItem.setHasSendItem(true);
			}
		}

		if (lineItems.length > 0) {
			beanModel.setLineItems(lineItems);
		}

		// Set the training and customer buttons enabled state based on
		// flags from the cargo. Training Mode is a single toggle on/off
		// button.
		boolean trainingModeOn = cargo.getRegister().getWorkstation()
				.isTrainingMode();

		localModel.setButtonEnabled(CommonActionsIfc.CUSTOMER,
				cargo.isCustomerEnabled());

		localModel.setButtonEnabled(CommonActionsIfc.SCANSHEET, Gateway
				.getBooleanProperty(APPLICATION_PROPERTY_GROUP_NAME,
						"enableScanSheet", false));

		// If training mode is turned on, then put Training Mode
		// indication in status panel. Otherwise, return status
		// to online/offline status.
		statusModel.setStatus(POSUIManagerIfc.TRAINING_MODE_STATUS,
				trainingModeOn);

		// For the Condition in which the user prefered lanaguage is different
		// from the default locale.
		// the statusBean was getting cleared in the
		// AssignmentSpec.getBean(BeanSpec beanSpec, String assignmentID,
		// Locale lcl) method.
		// So the Statusbar didnt have the cashierName and SalesAssociate Name.
		// Added this code to set the cashier name and Sales Associate Name.

		statusModel.setCashierName(cargo.getOperator().getPersonName()
				.getFirstLastName());
		if (transaction == null || transaction.getSalesAssociate() == null) {
			statusModel.setSalesAssociateName(BLANK_STRING);

			try {
				Serializable[] values;

				/*
				 * Corrected base issue. Base code flipped order parameters were
				 * read. This meant that DefaultToCashier = "N" caused the
				 * IDSalesAssociateEveryTransaction parameter to be ignored, the
				 * blocked to be skipped, and no sales associate to be set. Fix:
				 * Check IDSaleAssociate parameter; if set to yes, get value
				 * from cargo; otherwise, check DefaultToCashier block and check
				 * that. Also, UI was holding value when backing out and
				 * changing parameters, so start with an empty string.
				 */
				values = pm
						.getParameterValues("IdentifySalesAssociateEveryTransaction");
				String parameterValue = (String) values[0];
				if (parameterValue.equalsIgnoreCase("Y")
						&& cargo.getEmployee() != null) {
					statusModel.setSalesAssociateName(cargo.getEmployee()
							.getPersonName().getFirstLastName());
				} else {
					values = pm.getParameterValues("DefaultToCashier");
					parameterValue = (String) values[0];
					if (parameterValue.equalsIgnoreCase("Y")) {
						statusModel.setSalesAssociateName(cargo.getOperator()
								.getPersonName().getFirstLastName());
					}
				}
			} catch (ParameterException e) {
				logger.error(BLANK_STRING + Util.throwableToString(e)
						+ BLANK_STRING);
			}

		}

		// Set the undo, cancel and tender buttons enabled state based on
		// transaction from cargo.
		if (transaction == null) {
			// initialize lineitem list on cargo
			globalModel.setButtonEnabled(CommonActionsIfc.UNDO, true);
			localModel.setButtonEnabled(CommonActionsIfc.NOSALE, true);
			globalModel.setButtonEnabled(CommonActionsIfc.CANCEL, false);
			localModel.setButtonEnabled(CommonActionsIfc.TENDER, false);
			localModel.setButtonEnabled(CommonActionsIfc.HOUSEACCOUNT, true);
			localModel.setButtonEnabled(CommonActionsIfc.TILLFUNCTIONS, true);
			localModel.setButtonEnabled(CommonActionsIfc.RETURN, true);
			localModel.setButtonEnabled(CommonActionsIfc.REPRINT_RECEIPT, true);
			if (!reEntryOn) {
				localModel.setButtonEnabled(CommonActionsIfc.REDEEM, true);
			}
			localModel.setButtonEnabled(CommonActionsIfc.GIFT_CARD_CERT, true);

			// House Account button should be disabled if house account is not a
			// supported card type
			try {
				String houseAccountAccepted = pm
						.getStringValue("HouseCardsAccepted");
				boolean enableHouseAccount = "Y"
						.equalsIgnoreCase(houseAccountAccepted);
				localModel.setButtonEnabled(CommonActionsIfc.HOUSEACCOUNT,
						enableHouseAccount);
			} catch (ParameterException pe) {
				logger.error("Unable to get parameter values for CreditCardTypes"
						+ Util.throwableToString(pe) + BLANK_STRING);
			}

			// Begin GD-49: Develop Employee Discount Module
			// lcatania (Starmount) Feb 26, 2013
			localModel.setButtonEnabled(GDYNCommonActionsIfc.PRICING, true);
			// End GD-49: Develop Employee Discount Module

			cargo.setLineItems(null);
		} else {
			TransactionTotalsIfc totals = transaction.getTransactionTotals();
			if (lineItems.length > 0) {
				if (totals.isTransactionLevelSendAssigned()) {
					if (transaction.hasSendItems()) {
						// enabled only when there is at least one send item
						// in transaction level send (as per reqs.)
						localModel.setButtonEnabled(CommonActionsIfc.TENDER,
								true);
					} else {
						localModel.setButtonEnabled(CommonActionsIfc.TENDER,
								false);
					}
				} else {
					localModel.setButtonEnabled(CommonActionsIfc.TENDER, true);
				}
				beanModel.setSelectedRow(lineItems.length - 1);
			}
			localModel
					.setButtonEnabled(CommonActionsIfc.TRAINING_ON_OFF, false);
			localModel.setButtonEnabled(CommonActionsIfc.NOSALE, false);
			globalModel.setButtonEnabled(CommonActionsIfc.UNDO, false);
			globalModel.setButtonEnabled(CommonActionsIfc.CANCEL, true);
			localModel.setButtonEnabled(CommonActionsIfc.HOUSEACCOUNT, false);
			localModel.setButtonEnabled(CommonActionsIfc.TILLFUNCTIONS, false);
			localModel
					.setButtonEnabled(CommonActionsIfc.REPRINT_RECEIPT, false);

			// Begin GD-287 When selecting an item from an "exchange"
			// transaction or entering
			// a non-retrieved return item should have the Tender button
			// disabled until the
			// transaction balance equals 0 or greater. Once it is 0 or greater
			// by adding sale items,
			// then the normal tender options (like in a sale) are displayed.
			CurrencyIfc zero = DomainGateway.getBaseCurrencyInstance();
			CurrencyIfc total = totals.getBalanceDue();

			if (transaction.isOriginalExchangeTransaction()
					|| disallowNoReceiptReturnTender) {
				if (total.compareTo(zero) == CurrencyIfc.EQUALS
						|| total.compareTo(zero) == CurrencyIfc.GREATER_THAN) {
					localModel.setButtonEnabled(CommonActionsIfc.TENDER, true);
				} else if (total.compareTo(zero) == CurrencyIfc.LESS_THAN) {
					localModel.setButtonEnabled(CommonActionsIfc.TENDER, false);
				}
			}

			// Vivek added the following logic to give a return or exchange
			// receipt transaction
			// if this is the receipt return we should allow the tender button
			// enable.
			// since the customer is not always will buy something else to make
			// the totla amount to be zero or positive
			if (transaction.isOriginalExchangeTransaction()
					&& (disallowNoReceiptReturnTender == false))
				localModel.setButtonEnabled(CommonActionsIfc.TENDER, true);

			// End GD-287

			// Check for Shipping Method in Transaction level send
			// Discard all shipping method info if required (as per reqs.)
			if (totals.isTransactionLevelSendAssigned()) {
				ShippingMethodIfc sendShippingMethod = totals.getSendPackages()[0]
						.getShippingMethod();
				CustomerIfc sendCustomer = totals.getSendPackages()[0]
						.getCustomer();
				ShippingMethodIfc shippingMethod = DomainGateway.getFactory()
						.getShippingMethodInstance();
				if (sendShippingMethod != null
						&& !sendShippingMethod.equals(shippingMethod)) {
					transaction.updateSendPackageInfo(0, shippingMethod,
							sendCustomer);
					// Must do this to force tax recalculation
					transaction.updateTransactionTotals();
				}
			}

			// If there is a transaction, send the transaction totals that
			// can be displayed to the UI.

			// Before display taxTotals, need to convert the longer precision
			// calculated total tax amount back to shorter precision tax total
			// amount for UI display.
			transaction.getTransactionTotals().setTaxTotal(
					transaction.getTransactionTotals().getTaxTotalUI());

			// Now, display on the UI.
			tbm.setTotals(transaction.getTransactionTotals());

			// Set screen name to Layaway Item if transaction is a layaway
			// disable no sale, customer, and return buttons
			int transType = transaction.getTransactionType();
			if (transType == TransactionIfc.TYPE_LAYAWAY_INITIATE
					|| transType == TransactionIfc.TYPE_ORDER_INITIATE) {
				localModel.setButtonEnabled(CommonActionsIfc.NOSALE, false);
				localModel.setButtonEnabled(CommonActionsIfc.CUSTOMER, false);
				localModel.setButtonEnabled(CommonActionsIfc.RETURN, false);

				if (transType == TransactionIfc.TYPE_ORDER_INITIATE
						&& orderTransaction.getOrderType() != OrderConstantsIfc.ORDER_TYPE_ON_HAND) {
					String spOrdItem = utility.retrieveText(
							POSUIManagerIfc.STATUS_SPEC,
							BundleConstantsIfc.POS_BUNDLE_NAME,
							SP_ORD_ITEM_SCREEN_NAME_TAG,
							SP_ORD_ITEM_SCREEN_NAME_TEXT,
							LocaleConstantsIfc.USER_INTERFACE);
					statusModel.setScreenName(spOrdItem);
					localModel.setButtonEnabled(
							CommonActionsIfc.TRAINING_ON_OFF, false);
				} else if (transType == TransactionIfc.TYPE_ORDER_INITIATE
						&& orderTransaction.getOrderType() == OrderConstantsIfc.ORDER_TYPE_ON_HAND) {
					String PDOOrdItem = utility.retrieveText(
							POSUIManagerIfc.STATUS_SPEC,
							BundleConstantsIfc.POS_BUNDLE_NAME,
							PDO_ORD_ITEM_SCREEN_NAME_TAG,
							PDO_ORD_ITEM_SCREEN_NAME_TEXT,
							LocaleConstantsIfc.USER_INTERFACE);
					statusModel.setScreenName(PDOOrdItem);
				} else {
					String layawayItem = utility.retrieveText(
							POSUIManagerIfc.STATUS_SPEC,
							BundleConstantsIfc.POS_BUNDLE_NAME,
							LAYAWAY_ITEM_SCREEN_NAME_TAG,
							LAYAWAY_ITEM_SCREEN_NAME_TEXT,
							LocaleConstantsIfc.USER_INTERFACE);
					statusModel.setScreenName(layawayItem);
				}
			} else if (transaction.hasExternalOrder()) {
				localModel.setButtonEnabled(CommonActionsIfc.RETURN, false);
			} else {
				localModel.setButtonEnabled(CommonActionsIfc.RETURN, true);
			}

			if (transaction.getTransactionTax().getTaxMode() == TaxIfc.TAX_MODE_EXEMPT) {
				localModel.setButtonEnabled(CommonActionsIfc.CUSTOMER, false);
			}

			// Begin GD-31 Prevent any tender refund for returns without a
			// receipt
			// disable any other returns
			if (disallowReturn) {
				localModel.setButtonEnabled(CommonActionsIfc.RETURN, false);
			}
			// End GD-31

			// Begin GD-356: Line item deleting in a transaction with employee
			// discount
			// does not re-enable Pricing option or continue to apply the
			// employee discount
			// to new items added after line item deleting
			// lcatania (Starmount) Apr 19, 2013
			boolean pricingButtonEnabled = false;
			if (lineItems.length <= 0) {
				pricingButtonEnabled = true;
				cargo.getTransaction().setEmployeeDiscountID(null);
			} else {
				pricingButtonEnabled = (cargo.getTransaction()
						.getEmployeeDiscountID() == null);
			}
			localModel.setButtonEnabled(GDYNCommonActionsIfc.PRICING,
					pricingButtonEnabled);
			// End GD-356: Line item deleting in a transaction with employee
			// discount...
		}

		// House Account button should be disabled in transaction reentry mode
		boolean transactionReentryModeOn = cargo.getRegister().getWorkstation()
				.isTransReentryMode();
		if (transactionReentryModeOn) {
			localModel.setButtonEnabled(CommonActionsIfc.HOUSEACCOUNT, false);
		}

		// modified by Vivek for Loyalty enhancement : disable the Customer "F7"
		// button if the Loyalty Enable parameter is true.
		try {
			// setting the loyalty parameter to the transaction object.
			if(transaction != null) {
				transaction.setLoyaltyEnable(pm.getBooleanValue(GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable));
			}
			if (pm.getBooleanValue(GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable)) {
				localModel.setButtonEnabled(CommonActionsIfc.CUSTOMER, false);
			}
		} catch (ParameterException pe) {
			logger.error("Unable to get parameter value for "
					+ GDYNParameterConstantsIfc.CUSTOMER_loyalty_Enable + " "
					+ Util.throwableToString(pe) + "");
		}

		// Set the local button, global button, totals and status bean
		// models on the sale bean model.
		beanModel.setTotalsBeanModel(tbm);
		beanModel.setGlobalButtonBeanModel(globalModel);
		beanModel.setLocalButtonBeanModel(localModel);
		beanModel.setStatusBeanModel(statusModel);

		// Side-effect: Allow the status model to dynamically compute a negative
		// till balance.
		statusModel.setRegister(cargo.getRegister());

		// Side-effect: Show linked customer, if applicable.
		// If no customer, clear the customer field. Customer history, for one,
		// can leave wrong name in field.
		CustomerIfc customer = null;
		if (transaction != null && cargo.isCustomerEnabled()
				&& (customer = transaction.getCustomer()) != null) {
			String customerName = customer.getFirstLastName();
			if (customerName != null) {
				statusModel.setCustomerName(customerName);
			} else {
				statusModel.setCustomerName(BLANK_STRING);
			}

		} else {
			statusModel.setCustomerName(BLANK_STRING);
		}
		
		  if(statusModel.isEmpDiscountApplied()){
        	setRemainingSpend(cargo, statusModel);
        	 /*code changes done by dharmendra on 16/09/2016 to disable return button for employee purchase transaction*/
            localModel.setButtonEnabled(CommonActionsIfc.RETURN, false);
       
        }
		
		// Display the screen
		POSUIManagerIfc ui = (POSUIManagerIfc) bus
				.getManager(UIManagerIfc.TYPE);

		// Don't call showScreen() if SELL_ITEM is the current screen
		// because showScreen() will result in resetting the scanner session.
		try {
			if (ui.getActiveScreenID() == POSUIManagerIfc.SELL_ITEM) {
				ui.setModel(POSUIManagerIfc.SELL_ITEM, beanModel);
			} else {
				// If both property true, change the prompt reponse field length
				if (imeiEnabled && serializationEnabled) {
					PromptAndResponseModel promptModel = new PromptAndResponseModel();
					promptModel.setMaxLength(imeiResponseFieldLength);
					beanModel.setPromptAndResponseModel(promptModel);
				}
				ui.showScreen(POSUIManagerIfc.SELL_ITEM, beanModel);
				try {
					LineItemsModel tempBeanModel = (LineItemsModel) ui
							.getModel(POSUIManagerIfc.SELL_ITEM);
					cargo.setMaxPLUItemIDLength(Integer.valueOf(tempBeanModel
							.getPromptAndResponseModel().getMaxLength()));
				} catch (Exception e) {
					logger.warn("Unable to get the maximum PLU item ID length",
							e);
				}
			}
		} catch (UIException uie) {
			logger.warn("Unable to get the active screen ID");
		}
		if (lineItems.length <= 0) {
			localModel.setButtonEnabled(CommonActionsIfc.TENDER, false);
			beanModel.setSelectedRow(-1);
			CPOIPaymentUtility cpoiPaymentUtility = CPOIPaymentUtility
					.getInstance();
			WorkstationIfc workstation = cargo.getRegister().getWorkstation();
			PaymentManagerIfc paymentManager = (PaymentManagerIfc) bus
					.getManager(PaymentManagerIfc.TYPE);
			paymentManager.clearSwipeAheadData(workstation);
			cpoiPaymentUtility.endScrollingReceipt(workstation);
		}
		// line display part
		setLineDisplay(bus, cargo, pda, transaction, utility);

		// cpoi part
		setCPOIDisplay(bus, cargo, pda, transaction, itemList);

		// set debug timestamp and print to console
		if (debugRoundtripTimestamp != 0 && logger.isDebugEnabled()) {
			String message = "Roundtrip to ShowSaleScreen is "
					+ (System.currentTimeMillis() - debugRoundtripTimestamp)
					+ "ms";
			logger.debug(message);
			System.out.println(message);
		}
		//POS-334 code changes added by Dharmendra to apply multiple item discounts to a transaction
		if (transaction != null) {
			List<PLUItemIfc> couponItemsList = GDYNLoyalityDiscountUtility
					.getAllCouponItemsList(transaction);
			if (couponItemsList != null 
					&& GDYNLoyalityConstants.recalculateMultipleCpnDiscount) {
				GDYNLoyalityDiscountUtility.applyMultipleItemLoyalityDiscount(
						bus, couponItemsList);
				GDYNLoyalityConstants.recalculateMultipleCpnDiscount = false;

			}else if (couponItemsList != null 
					&& GDYNLoyalityConstants.recalculateTransactionDiscount){
				GDYNLoyalityDiscountUtility.applyLoyalityTransactionDiscount(bus, couponItemsList);
				GDYNLoyalityConstants.recalculateTransactionDiscount=false;
				
				transaction.getTransactionTotals().getAmountOffTotal().setDecimalValue(
						transaction.getTransactionTotals().getDiscountTotal().getDecimalValue());
				transaction.updateTransactionTotals();
				transaction.updateTenderTotals();
				
				
			}
		}
		//POS-334 End code changes by Dharmendra to apply multiple item discounts to a transaction
		// hide the on screen keyboard if the transaction just started.
		bus.mail("HideOnScreenKeyboard");
	}
	
		public void setRemainingSpend(SaleCargoIfc cargo,
			StatusBeanModel statusModel) {
		logger.info("setRemainingSpend Entered");
		GDYNSaleReturnTransactionIfc transaction = (GDYNSaleReturnTransactionIfc) cargo
				.getTransaction();
		
		BigDecimal totalGRGSpendLimit = BigDecimal.ZERO;
		BigDecimal totalDYNSpendLimit = BigDecimal.ZERO;
		String salesAssociateName = BLANK_STRING;
		GDYNEmployeeDiscResponseObject[] responseObjectsEmp = GDYNEmployeeDiscountUtility.responseObjects;
		BigDecimal maxSpendDyn = BigDecimal.ZERO;
		BigDecimal maxSpendGrg = BigDecimal.ZERO;
		if (transaction != null
				&& transaction.getEmployeeDiscountID() != null
				&& cargo.getTransaction() != null
				&& cargo.getTransaction().getItemContainerProxy() != null
				&& cargo.getTransaction().getItemContainerProxy()
						.getLineItems() != null
				&& cargo.getTransaction().getItemContainerProxy()
						.getLineItems().length != 0) {

			SaleReturnLineItemIfc[] saleLineItems = (SaleReturnLineItemIfc[]) cargo
					.getTransaction().getItemContainerProxy().getLineItems();

			if (responseObjectsEmp != null && responseObjectsEmp.length == 2) {
				if (saleLineItems != null && saleLineItems.length != 0) {
					for (SaleReturnLineItemIfc saleReturnLineItem : saleLineItems) {
						String itemDivision = saleReturnLineItem.getPLUItem()
								.getItemDivision();

						if ((itemDivision != null)
								&& !(BLANK_STRING
										.equalsIgnoreCase(itemDivision))
								/*
								 * && (GDYNEepConstants.GARAGE.compareTo(
								 * saleReturnLineItem
								 * .getPLUItem().getItemDivision())==0)
								 */
								&& Integer.parseInt(GDYNEepConstants.GARAGE) == Integer
										.parseInt(saleReturnLineItem
												.getPLUItem().getItemDivision())
								&& saleReturnLineItem.getPLUItem()
										.isDiscountConsidered()
										&&(saleReturnLineItem
												.getItemPrice().getItemDiscountAmount().getDecimalValue().compareTo(BigDecimal.ZERO)!=0)) {
							totalGRGSpendLimit = totalGRGSpendLimit
									.add(saleReturnLineItem.getItemPrice()
											.getSellingPrice()
											.getDecimalValue());
						}
						if ((itemDivision != null)
								&& !(BLANK_STRING
										.equalsIgnoreCase(itemDivision))
								/*
								 * &&
								 * (GDYNEepConstants.DYNAMITE.equalsIgnoreCase
								 * (saleReturnLineItem
								 * .getPLUItem().getItemDivision()))
								 */
								&& Integer.parseInt(GDYNEepConstants.DYNAMITE) == Integer
										.parseInt(saleReturnLineItem
												.getPLUItem().getItemDivision())
								&& saleReturnLineItem.getPLUItem()
										.isDiscountConsidered()
										&&(saleReturnLineItem
												.getItemPrice().getItemDiscountAmount().getDecimalValue().compareTo(BigDecimal.ZERO)!=0)) {
							totalDYNSpendLimit = totalDYNSpendLimit
									.add(saleReturnLineItem.getItemPrice()
											.getSellingPrice()
											.getDecimalValue());
						}
					}

				}
			}

			BigDecimal totalSpend = BigDecimal.ZERO;
			if (responseObjectsEmp != null && responseObjectsEmp.length == 1
					&& responseObjectsEmp[0].getDiscDivision() == null) {
				BigDecimal maxSpendAmt = responseObjectsEmp[0]
						.getMaxSpendLimit();
				if (saleLineItems != null && saleLineItems.length != 0) {
					for (SaleReturnLineItemIfc saleReturnLineItem : saleLineItems) {
						if (saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
								&& saleReturnLineItem.getPLUItem()
										.getItemClassification()
										.getEmployeeDiscountAllowedFlag()
								&&(saleReturnLineItem
								.getItemPrice().getItemDiscountAmount().getDecimalValue().compareTo(BigDecimal.ZERO)!=0)		
								) {
							logger.info("totalSpend "+totalSpend+ " :  saleReturnLineItem.getPLUItemID() "+saleReturnLineItem.getPLUItemID());
							totalSpend = totalSpend.add(saleReturnLineItem
									.getItemPrice().getSellingPrice()
									.getDecimalValue());
						}
					}
				}

				salesAssociateName = maxSpendAmt.subtract(totalSpend)
						.toString();
			} else {
				if(responseObjectsEmp!=null && responseObjectsEmp.length!=0){
				for (GDYNEmployeeDiscResponseObject response : responseObjectsEmp) {
					/*
					 * if(GDYNEepConstants.DYNAMITE.equalsIgnoreCase(response.
					 * getDiscDivision())){
					 */
					if (Integer.parseInt(GDYNEepConstants.DYNAMITE) == Integer
							.parseInt(response.getDiscDivision())) {
						maxSpendDyn = response.getMaxSpendLimit();
					} else {
						maxSpendGrg = response.getMaxSpendLimit();
					}
				}

				logger.info("maxSpendDyn  " + maxSpendDyn + " maxSpendGrg "
						+ maxSpendGrg);
				/*
				 * salesAssociateName = maxSpendGrg
				 * .subtract(totalGRGSpendLimit) + "(GRG) : " +
				 * maxSpendDyn.subtract(totalDYNSpendLimit) + "(DYN)";
				 */
				salesAssociateName = configureRemSpendString(
						maxSpendGrg.subtract(totalGRGSpendLimit),
						maxSpendDyn.subtract(totalDYNSpendLimit));
			}
			}

		}
		if ((transaction==null|| transaction.getItemContainerProxy() == null
				|| transaction.getItemContainerProxy().getLineItems() == null || transaction
				.getItemContainerProxy().getLineItems().length == 0)) {
			if(GDYNEmployeeDiscountUtility.responseObjects!=null && GDYNEmployeeDiscountUtility.responseObjects.length!=0){
			for (GDYNEmployeeDiscResponseObject response : GDYNEmployeeDiscountUtility.responseObjects) {
				/*
				 * if(GDYNEepConstants.GARAGE).equalsIgnoreCase(response.
				 * getDiscDivision())){
				 */
				if (response.getDiscDivision() == null
						&& responseObjectsEmp != null
						&& responseObjectsEmp.length == 1) {
					salesAssociateName += response.getMaxSpendLimit()
							.toString();

				} else {

					if (response.getDiscDivision() != null
							&& Integer.parseInt(GDYNEepConstants.GARAGE) == Integer
									.parseInt(response.getDiscDivision())) {
						// salesAssociateName+=
						// response.getMaxSpendLimit().toString()+ "(GRG) : ";
						maxSpendGrg = response.getMaxSpendLimit();
					}

					if (response.getDiscDivision() != null
							&& Integer.parseInt(GDYNEepConstants.DYNAMITE) == Integer
									.parseInt(response.getDiscDivision())) {
						// salesAssociateName+=
						// response.getMaxSpendLimit().toString()+ "(DYN) : ";
						maxSpendDyn = response.getMaxSpendLimit();
					}

					salesAssociateName = configureRemSpendString(maxSpendGrg,
							maxSpendDyn);

				}
			}
		}

		}
		if(responseObjectsEmp!=null && responseObjectsEmp.length!=0){
		statusModel.setCustomerName(responseObjectsEmp[0].getFirstName() + " "
				+ responseObjectsEmp[0].getLastName());
		statusModel.setCashierName(transaction.getEmployeeDiscountID());
		REMAINING_SPEND_STRING =salesAssociateName;
		statusModel.setSalesAssociateName(salesAssociateName);
		}
		
		

	}
    public String configureRemSpendString(BigDecimal grySpendBD,BigDecimal dynSpendBD){
    	String remSpendString = " GRG "+grySpendBD+"  "+"DYN "+dynSpendBD ;
    	System.out.println("remSpendString2 "+remSpendString);
    	return remSpendString;
    }
}
