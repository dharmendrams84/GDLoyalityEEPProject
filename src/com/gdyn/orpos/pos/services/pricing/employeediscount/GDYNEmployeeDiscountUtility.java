//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.pricing.employeediscount;

import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.foundation.manager.data.DataException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.arts.DataTransactionFactory;
import oracle.retail.stores.domain.arts.DataTransactionKeys;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.ItemDiscountByPercentageIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.employee.Employee;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.factory.DomainObjectFactoryIfc;
import oracle.retail.stores.domain.lineitem.ItemPriceIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.SearchCriteriaIfc;
import oracle.retail.stores.domain.utility.CodeConstantsIfc;
import oracle.retail.stores.domain.utility.CodeEntryIfc;
import oracle.retail.stores.domain.utility.CodeListIfc;
import oracle.retail.stores.domain.utility.DiscountUtility;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.ifc.JournalManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.ado.ADOException;
import oracle.retail.stores.pos.ado.utility.Utility;
import oracle.retail.stores.pos.ado.utility.UtilityIfc;
import oracle.retail.stores.pos.journal.JournalFormatterManagerIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.admin.security.common.UserAccessCargoIfc;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.services.giftcard.SetIssueRequestTypeAisle;
import oracle.retail.stores.pos.services.pricing.PricingCargo;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;
import com.gdyn.orpos.domain.arts.GDYNPLUTransaction;
import com.gdyn.orpos.domain.manager.GDYNEmployeeDiscountManager;
import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;
import com.gdyn.orpos.pos.services.pricing.GDYNPricingCargo;
import com.gdyn.orpos.pos.services.sale.GDYNSaleCargo;
import com.gdyn.orpos.pos.services.sale.GDYNShowSaleScreenSite;

import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import sun.nio.cs.ext.Big5;

import com.gdyn.orpos.pos.eep.GDYNEepConstants;

/**
 * Utilities for Employee Discount operations
 * 
 * @author lcatania
 * 
 */
@SuppressWarnings("deprecation")
public class GDYNEmployeeDiscountUtility implements GDYNParameterConstantsIfc {
	/*
	 * public static final String DYNAMITE = "Dynamite"; public static final
	 * String GARAGE = "Garage";
	 */

	private static Map<String, BigDecimal> EMPLOYEE_DISCOUNT_CLASSES = null;
	private static BigDecimal DEFAULT_EMPLOYEE_DISCOUNT = null;
	private static final String DISCOUNT_CLASSES_SEPARATOR = "\\|";
	private static final String DEPARTMENT_CLASS_PERCENT_SEPARATOR = "_";
	private static final String ONE_HUNDRED = "100.0";

	public static GDYNEmployeeDiscResponseObject[] responseObjects = null;

	public static Boolean isDefEmplDiscApplied = Boolean.TRUE;

	protected static Logger logger = Logger
			.getLogger("com.gdyn.orpos.pos.services.pricing.employeediscount.GDYNEmployeeDiscountUtility");

	// Begin GD-331: Changes to the new employee discount parameters are not
	// taking effect unless the client is
	// restarted
	// lcatania (Starmount) Mar 19, 2013
	private static String DEFAULT_EMPLOYEE_DISCOUNT_OLD_VALUE = null;
	// End GD-331: Changes to the new employee discount parameters are not
	// taking effect unless the client is restarted

	// Begin GD-384: Only applies employee discount to items posted to sell item
	// screen at time employee discount
	// applied - not to items added afterwards
	// lcatania (Starmount) Apr 26, 2013
	protected static final String EMPLOYEE_DISCOUNT_TAG = "EmployeeDiscount";
	protected static final String EMPLOYEE_DISCOUNT_TEXT = "employee discount";

	// protected String Garage = "Garage";
	// End GD-384: Only applies employee discount to items posted to sell item
	// screen...

	/**
	 * Calculates employee discount for every item in the transaction
	 * GDYNEmployeeDiscountUtility String - Dialog to display if there was
	 * something wrong. Null if everything was ok.
	 * 
	 * @param bus
	 * @param cargo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String calculateTransactionDiscount(BusIfc bus) {
		GDYNPricingCargo cargo = (GDYNPricingCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = (SaleReturnTransactionIfc) cargo
				.getTransaction();

		// Pak addedt this here
		transaction.clearAdvancedPricingRules();
       //By Monica
	//transaction.clearTransactionDiscounts(true);
		
		SaleReturnLineItemIfc[] lineItems = (SaleReturnLineItemIfc[]) transaction
				.getLineItems();
		for (int i = 0; i < lineItems.length; i++) {
			SaleReturnLineItemIfc srli = lineItems[i];
			if (srli.isGiftItem()) {
				transaction.retrieveItemByIndex(i);
			}
		}
		EmployeeIfc discountEmployee = cargo.getDiscountEmployee();

		Map<Integer, ItemDiscountStrategyIfc> discountHash = cargo
				.getValidDiscounts();
		discountHash.clear();

		LocalizedCodeIfc reasonCode = getEmployeeDiscountReasonCode(bus);
		// Commented below condition and added new line by Monica to get
		// original price on a subsequent scan of the item when employee
		// discount is performed.
		// String storeID = cargo.getOperator().getStoreID();
		String storeID = cargo.getRegister().getWorkstation().getStoreID();
		boolean hasInvalidDiscounts = false;

		for (int i = 0; i < lineItems.length; i++) {
			SaleReturnLineItemIfc lineItem = lineItems[i];
		
			// Begin GD-384: Only applies employee discount to items posted to
			// sell item screen at time employee
			// discount applied - not to items added afterwards
			// lcatania (Starmount) Apr 25, 2013

			ItemDiscountStrategyIfc discountStrategy = getItemDiscountStrategy(
					lineItem, discountEmployee, reasonCode);

			if (discountStrategy != null) {
				if (isDiscountValid(lineItem, discountStrategy, storeID)) {
					discountHash.put(new Integer(i), discountStrategy);
				} else {
					hasInvalidDiscounts = true;
				}
			}
			// End GD-384: Only applies employee discount to items posted to
			// sell item screen...
		}

		String dialog = null;
		if (hasInvalidDiscounts) {
			if (lineItems.length > 1) {
				dialog = PricingCargo.MULTI_ITEM_INVALID_DISC;
			} else if (lineItems.length == 1) {
				dialog = PricingCargo.INVALID_DISC;
			}
		}
		
		Vector<SaleReturnLineItem> saleLineItems = transaction
				.getItemContainerProxy().getLineItemsVector();
		
	
		
	
		/*	new method added by dharmendra on 24/08/2016 to fix POS-209 issue. */
		setItemsElligibilityDetails(saleLineItems);
		
		
		BigDecimal grgDiscPerc = new BigDecimal(0);
		BigDecimal dynDiscPerc = new BigDecimal(0);
		BigDecimal maxSpendLimitGRG = new BigDecimal(0);
		BigDecimal maxSpendLimitDYN = new BigDecimal(0);

		logger.info("before applying employee discount");
		if (responseObjects != null && responseObjects.length == 1
				&& responseObjects[0].getDiscDivision() == null) {
			BigDecimal maxSpendLimit = responseObjects[0].getMaxSpendLimit();
			grgDiscPerc = responseObjects[0].getDiscPercentage();

			logger.info("maxSpendLmit for store employee "
					+ discountEmployee.getEmployeeID() + " : " + maxSpendLimit);
			logger.info("grgDiscPerc for store employee "
					+ discountEmployee.getEmployeeID() + " : " + grgDiscPerc);

			applyStoreEmployeeDiscount(saleLineItems, maxSpendLimit,
					grgDiscPerc, responseObjects[0].getEmpIdSrc(),
					responseObjects[0].getEmpGroupId(),
					responseObjects[0].getPeriodId(),
					responseObjects[0].getEntitlementId());

		} else {
			logger.info("applying employee disocunt for HO employee");
			for (GDYNEmployeeDiscResponseObject resp : responseObjects) {
				logger.info("response object details " + resp.getDiscDivision()
						+ " : " + resp.getEmployeeNumber());
				// if
				// (GDYNEepConstants.DYNAMITE.equalsIgnoreCase(resp.getDiscDivision()))
				// {
				if (Integer.parseInt(GDYNEepConstants.DYNAMITE) == Integer
						.parseInt(resp.getDiscDivision())) {
					logger.info("applying Dynamite disocunt for HO employee");
					dynDiscPerc = resp.getDiscPercentage();
					maxSpendLimitDYN = resp.getMaxSpendLimit();
					
					applyHOEmployeeDiscount(saleLineItems, maxSpendLimitDYN,
							GDYNEepConstants.DYNAMITE, dynDiscPerc,
							resp.getEmpIdSrc(), resp.getEmpGroupId(),
							resp.getPeriodId(), resp.getEntitlementId());
				}
				// else
				// if(GDYNEepConstants.GARAGE.equalsIgnoreCase(resp.getDiscDivision()))
				// {
				else if (Integer.parseInt(GDYNEepConstants.GARAGE) == Integer
						.parseInt(resp.getDiscDivision())) {
					logger.info("applying Dynamite disocunt for HO employee "
							+ grgDiscPerc + " : " + maxSpendLimitGRG);
					grgDiscPerc = resp.getDiscPercentage();
					maxSpendLimitGRG = resp.getMaxSpendLimit();
					logger.info("Going to apply garage employee discount "
							+ grgDiscPerc + " : " + maxSpendLimitGRG);
					applyHOEmployeeDiscount(saleLineItems, maxSpendLimitGRG,
							GDYNEepConstants.GARAGE, grgDiscPerc,
							resp.getEmpIdSrc(), resp.getEmpGroupId(),
							resp.getPeriodId(), resp.getEntitlementId());
				}
			}

		}

		return dialog;
	}

	// Begin GD-384: Only applies employee discount to items posted to sell item
	// screen
	// at time employee discount applied - not to items added afterwards
	// lcatania (Starmount) Apr 25, 2013
	/**
	 * Crates a discount strategy for an specific item
	 * GDYNEmployeeDiscountUtility ItemDiscountStrategyIfc
	 * 
	 * @param lineItem
	 * @param discountEmployee
	 * @param reasonCode
	 * @return discount strategy created
	 */
	protected static ItemDiscountStrategyIfc getItemDiscountStrategy(
			SaleReturnLineItemIfc lineItem, EmployeeIfc discountEmployee,
			LocalizedCodeIfc reasonCode) {
		ItemDiscountStrategyIfc discountStrategy = null;

		logger.info("Elligibility Lineitem  "+lineItem.getPLUItemID()+" for employee discount "+" : "+ isEligibleForDiscount(lineItem) );
		if (isEligibleForDiscount(lineItem)) {
			// Get item discount percentage based on two parameters
			BigDecimal itemDiscount = getItemEmployeeDiscount(lineItem
					.getPLUItem());
			// Format item discount percentage
			itemDiscount = formatItemDiscount(itemDiscount);
			// Create Item Discount Strategy
			discountStrategy = createDiscountStrategy(discountEmployee,
					itemDiscount, reasonCode);
		}
		return discountStrategy;
	}

	/**
	 * Evaluates if the given dicount is valid to apply to the item
	 * GDYNEmployeeDiscountUtility boolean
	 * 
	 * @param lineItem
	 * @param discountStrategy
	 * @param storeID
	 * @return true if the given dicount is valid to apply to the item, false if
	 *         not
	 */
	protected static boolean isDiscountValid(SaleReturnLineItemIfc lineItem,
			ItemDiscountStrategyIfc discountStrategy, String storeID) {
		boolean isDiscountValid = true;

		SaleReturnLineItemIfc clonedLineItem = (SaleReturnLineItemIfc) lineItem
				.clone();
		
		CurrencyIfc itemOriginalPrice = getItemOriginalPrice(
				lineItem.getItemID(), storeID);
		
		/*code changes done by dharmendra on 06/09/2016 to fix issue POS-219*/
		
		//CurrencyIfc itemOriginalPrice = lineItem.getItemPrice().getSellingPrice();
		
		/*itemOriginalPrice = getItemOriginalPrice(
				lineItem.getItemID(), storeID);*/
	
		
		if (itemOriginalPrice != null) {
			ItemPriceIfc itemPrice = (ItemPriceIfc) clonedLineItem
					.getItemPrice();
			itemPrice.setSellingPrice(itemOriginalPrice);

			lineItem.getItemPrice().getItemDiscountAmount().setZero();
			lineItem.getItemPrice().setSellingPrice(itemOriginalPrice);
		}

		lineItem.getItemPrice().clearItemDiscounts();
		lineItem.getItemPrice().setExtendedSellingPrice(itemOriginalPrice);
		lineItem.getItemPrice().getItemTransactionDiscountAmount().setZero();

		lineItem.addItemDiscount(discountStrategy);
		// Pak commented out the following origianl code
		// clonedLineItem.addItemDiscount(discountStrategy);
		// clonedLineItem.calculateLineItemPrice();

		// check to see if adding this discount will make the item's price go
		// negative (or positive if it is a return item)
		if ((clonedLineItem.isSaleLineItem() && clonedLineItem
				.getExtendedDiscountedSellingPrice().signum() < 0)
				|| (clonedLineItem.isReturnLineItem() && clonedLineItem
						.getExtendedDiscountedSellingPrice().signum() > 0)) {
			isDiscountValid = false;
		} else {
			if (itemOriginalPrice != null) {
				ItemPriceIfc itemPrice = (ItemPriceIfc) lineItem.getItemPrice();
				itemPrice.setSellingPrice(itemOriginalPrice);
			}
		}
		logger.info("is Discount valid for lineitem "+lineItem.getPLUItemID()+ " :  "+isDiscountValid);
		return isDiscountValid;
	}

	// End GD-384: Only applies employee discount to items posted to sell item
	// screen...

	/**
	 * Get item discount percentage depending on the department and class
	 * GDYNEmployeeDiscountUtility BigDecimal - the item discount percentage
	 * 
	 * @param pluItem
	 *            - item to get the discount
	 * @return
	 */
	private static BigDecimal getItemEmployeeDiscount(PLUItemIfc pluItem) {
		BigDecimal itemEmployeeDiscount = null;

		if (pluItem != null) {
			// get the department
			String departmentID = "";
			String classID = "";
			// get the classes
			try {
				departmentID = pluItem.getDepartmentID();
				String merchHier = pluItem.getItemClassification()
						.getMerchandiseHierarchyGroup();
				// merchandise hierarchy string is:
				// 5:0DDD0CCC0SSS, DDD = department, CCC = class, SSS = subclass
				classID = merchHier.substring(7, 10);
			} catch (NullPointerException npe) {
				logger.warn("Unable to read dept/class info from item: "
						+ pluItem.getItemID());
			} catch (StringIndexOutOfBoundsException sie) {
				logger.warn("Unable to read dept/class info from item: "
						+ pluItem.getItemID());
			}

			if (!Util.isEmpty(departmentID) && !Util.isEmpty(classID)) {
				String deptClassKey = departmentID
						+ DEPARTMENT_CLASS_PERCENT_SEPARATOR + classID;
				if (getEmployeeDiscountClasses().containsKey(deptClassKey)) {
					// if department ID + class ID is found, stop the search
					itemEmployeeDiscount = getEmployeeDiscountClasses().get(
							deptClassKey);
				}
			}

			if (itemEmployeeDiscount == null) {
				// if a discount for the item was not found, get default
				// employee discount
				itemEmployeeDiscount = getDefaultEmployeeDiscount();

				isDefEmplDiscApplied = Boolean.TRUE;
				/*
				 * logger.info("Default employee discount applied for item "+pluItem
				 * .getItemID());
				 * System.out.println("Default employee discount applied for item "
				 * +pluItem.getItemID());
				 */
			} else {
				logger.info("Default employee discount not applied for item "
								+ pluItem.getItemID());
				isDefEmplDiscApplied = Boolean.FALSE;
			}
		}
		logger.info("Employee discount percentage for item id "
				+ pluItem.getItemID() + " " + itemEmployeeDiscount);
		return itemEmployeeDiscount;
	}

	/**
	 * Creates a Map containing discount percentages indexed by department ID +
	 * class ID, based on DISCOUNT_Employee_Discount_Classes parameter and
	 * return it GDYNEmployeeDiscountUtility Map<String,BigDecimal> - Map
	 * containing discount percentages indexed by department ID + class ID
	 * 
	 * @return
	 */
	private static Map<String, BigDecimal> getEmployeeDiscountClasses() {
		// Begin GD-331: Changes to the new employee discount parameters are not
		// taking effect unless the client is
		// restarted
		// lcatania (Starmount) Mar 19, 2013

		// Read the parameter

		String paramName = DISCOUNT_Employee_Discount_Classes;
		String paramDefValue = "";
		String paramValue = getUtilityInstance().getParameterValue(paramName,
				paramDefValue);

		if (paramValue == null) {
			EMPLOYEE_DISCOUNT_CLASSES = new HashMap<String, BigDecimal>();
		} else if (!paramValue.equals(DEFAULT_EMPLOYEE_DISCOUNT_OLD_VALUE)) {
			// if the map was not already created or if it has changed, it is
			// created and populated
			EMPLOYEE_DISCOUNT_CLASSES = new HashMap<String, BigDecimal>();

			// If parameter is not empty
			if (!paramValue.isEmpty()) {
				// Creates a list with employee discount classes definition
				String[] discountClassesList = paramValue
						.split(DISCOUNT_CLASSES_SEPARATOR);
				// for every class definition
				for (int i = 0; i < discountClassesList.length; i++) {
					String discountClass = discountClassesList[i];
					// Separates department ID + class ID from percentage
					// discount
					int indexOfLastSeparator = discountClass
							.lastIndexOf(DEPARTMENT_CLASS_PERCENT_SEPARATOR);
					// Get department ID + class ID to be used as key
					String discountClassKey = discountClass.substring(0,
							indexOfLastSeparator);
					// Get percentage discount to be used as value
					String discountClassValue = discountClass
							.substring(indexOfLastSeparator + 1);
					// Convert String to BigDecimal
					BigDecimal discountClassValuePercent = new BigDecimal(
							discountClassValue);
					// Add the pair to the map
					EMPLOYEE_DISCOUNT_CLASSES.put(discountClassKey,
							discountClassValuePercent);
				}
			}

			DEFAULT_EMPLOYEE_DISCOUNT_OLD_VALUE = paramValue;
		}
		// End GD-331: Changes to the new employee discount parameters are not
		// taking effect unless the client is
		// restarted

		return EMPLOYEE_DISCOUNT_CLASSES;
	}

	/**
	 * Read the default employee discount from a parameter
	 * GDYNEmployeeDiscountUtility BigDecimal - default employee discount
	 * 
	 * @return
	 */
	private static BigDecimal getDefaultEmployeeDiscount() {
		// Read the parameter
		String paramName = DISCOUNT_Default_Employee_Discount_Percent;
		String paramDefValue = "";
		String paramValue = getUtilityInstance().getParameterValue(paramName,
				paramDefValue);
		// if it is not empty
		if (paramValue != null && !paramValue.isEmpty()) {
			// converts to BigDecimal
			DEFAULT_EMPLOYEE_DISCOUNT = new BigDecimal(paramValue);
		}
		return DEFAULT_EMPLOYEE_DISCOUNT;
	}

	/**
	 * Format item discount as a percent rate GDYNEmployeeDiscountUtility
	 * BigDecimal - Item discount converted on a percent rate
	 * 
	 * @param itemDiscount
	 *            - item discount to be converted
	 * @return
	 */
	private static BigDecimal formatItemDiscount(BigDecimal itemDiscount) {
		if (itemDiscount != null) {
			// is divided by 100
			itemDiscount = itemDiscount.divide(new BigDecimal(ONE_HUNDRED));
			// if has more than 2 decimals
			if (itemDiscount.toString().length() > 5) {
				// is truncated to only 2 decimal places
				BigDecimal scaleOne = new BigDecimal(1);
				itemDiscount = itemDiscount.divide(scaleOne, 2);
			}
		}
		return itemDiscount;
	}

	/**
	 * Get reason code value for employee discount GDYNEmployeeDiscountUtility
	 * LocalizedCodeIfc - reason code value for employee discount
	 * 
	 * @param bus
	 * @param cargo
	 * @return
	 */
	protected static LocalizedCodeIfc getEmployeeDiscountReasonCode(BusIfc bus) {
		// get reason from db
		UtilityManagerIfc utility = (UtilityManagerIfc) bus
				.getManager(UtilityManagerIfc.TYPE);
		UserAccessCargoIfc cargo = (UserAccessCargoIfc) bus.getCargo();
		CodeListIfc reasonCodes = utility.getReasonCodes(cargo.getOperator()
				.getStoreID(),
				CodeConstantsIfc.CODE_LIST_EMPLOYEE_DISCOUNT_REASON_CODES);
		LocalizedCodeIfc reason = DomainGateway.getFactory().getLocalizedCode();
		if (reasonCodes != null) {
			String defaultReason = reasonCodes.getDefaultCodeString();
			reason.setCode(defaultReason);
			CodeEntryIfc entry = reasonCodes.findListEntryByCode(defaultReason);
			if (entry != null) {
				reason.setText(entry.getLocalizedText());
			}
		} else {
			reason.setCode(CodeConstantsIfc.CODE_UNDEFINED);
		}
		reason.setCodeName(DiscountRuleConstantsIfc.ASSIGNMENT_BASIS_DESCRIPTORS[DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE]);
		return reason;
	}

	/**
	 * Creates discount strategy object
	 * 
	 * @param cargo
	 *            The pricing cargo
	 * @param percent
	 *            The discount percent
	 * @param reason
	 *            The reason code
	 * @return the discount
	 */
	private static ItemDiscountByPercentageIfc createDiscountStrategy(
			EmployeeIfc discountEmployee, BigDecimal percent,
			LocalizedCodeIfc reason) {
		ItemDiscountByPercentageIfc sgy = DomainGateway.getFactory()
				.getItemDiscountByPercentageInstance();
		sgy.setReason(reason);
		sgy.setLocalizedNames(reason.getText());
		sgy.setDiscountRate(percent);
		sgy.setMarkdownFlag(false);
		sgy.setDamageDiscount(false);
		sgy.setAccountingMethod(DiscountRuleConstantsIfc.ACCOUNTING_METHOD_DISCOUNT);
		sgy.setAssignmentBasis(DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE);
		sgy.setDiscountEmployee(discountEmployee);

		return sgy;
	}

	/**
	 * Determines if the item is eligible for the discount
	 * 
	 * @param srli
	 *            The line item
	 * @return true if the item is eligible for the discount
	 */
	private static boolean isEligibleForDiscount(SaleReturnLineItemIfc srli) {
		return DiscountUtility.isEmployeeDiscountEligible(srli);
	}

	/**
	 * Returns an instance of Utility
	 * 
	 * @return UtilityIfc - the instance of Utility
	 */
	protected static UtilityIfc getUtilityInstance() {
		UtilityIfc instance = null;
		try {
			instance = Utility.createInstance();
		} catch (ADOException e) {
			String message = "Configuration problem: could not instantiate UtilityIfc instance";
			throw new RuntimeException(message, e);
		}
		return instance;
	}

	// Begin GD-384: Only applies employee discount to items posted to sell item
	// screen at time employee discount
	// applied - not to items added afterwards
	// lcatania (Starmount) Apr 26, 2013
	/**
	 * Displays the selected dialog screen.
	 * 
	 * @param dialog
	 *            The selected dialog
	 * @param discountTag
	 *            The tag for the discount argument
	 * @param discountText
	 *            The default text for the discount argument
	 * @param bus
	 *            The sevice bus
	 */
	public static void showDialog(String dialog, BusIfc bus) {
		showDialog(dialog, EMPLOYEE_DISCOUNT_TAG, EMPLOYEE_DISCOUNT_TEXT, bus);
	}

	// End GD-384: Only applies employee discount to items posted to sell item
	// screen...

	/**
	 * Displays the selected dialog screen.
	 * 
	 * @param dialog
	 *            The selected dialog
	 * @param discountTag
	 *            The tag for the discount argument
	 * @param discountText
	 *            The default text for the discount argument
	 * @param bus
	 *            The sevice bus
	 */
	public static void showDialog(String dialog, String discountTag,
			String discountText, BusIfc bus) {
		POSUIManagerIfc ui = (POSUIManagerIfc) bus
				.getManager(UIManagerIfc.TYPE);
		UtilityManagerIfc utility = (UtilityManagerIfc) bus
				.getManager(UtilityManagerIfc.TYPE);
		// Get the locale for the user interface.
		Locale uiLocale = LocaleMap
				.getLocale(LocaleConstantsIfc.USER_INTERFACE);

		// show multiple selection with some invalid discounts confirmation
		// dialog screen
		if (dialog.equals(PricingCargo.MULTI_ITEM_INVALID_DISC)) {
			String arg = utility.retrieveCommonText(discountTag, discountText,
					uiLocale);
			// display the invalid discount error screen
			String[] msg = new String[4];
			msg[0] = msg[1] = msg[2] = msg[3] = arg;
			showMultiInvalidDiscountDialog(ui, msg);
		}
		// show no valid discounts error dialog screen
		else if (dialog.equals(PricingCargo.INVALID_DISC)) {
			// display the invalid discount error screen
			String[] msg = new String[1];
			msg[0] = utility.retrieveCommonText(discountTag, discountText,
					uiLocale);
			showInvalidDiscountDialog(ui, msg);
		}
		// Unexpected problem
		else {
			logger.error("Unexpected dialog requested: " + dialog);
		}
	}

	/**
	 * Displays the multiple selection with some invalid discounts confirmation
	 * dialog screen.
	 * 
	 * @param ui
	 *            The POSUIManager
	 * @param msg
	 *            The string array representing the arguments for the dialog
	 */
	protected static void showMultiInvalidDiscountDialog(POSUIManagerIfc ui,
			String[] msg) {
		DialogBeanModel dialogModel = new DialogBeanModel();
		dialogModel.setResourceID(PricingCargo.MULTI_ITEM_INVALID_DISC);
		dialogModel.setType(DialogScreensIfc.CONFIRMATION);
		dialogModel.setArgs(msg);
		dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_YES,
				CommonLetterIfc.CONTINUE);
		dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_NO,
				CommonLetterIfc.CANCEL);
		ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
	}

	/**
	 * Retrieve the original price of an item from database and return it
	 * GDYNEmployeeDiscountUtility CurrencyIfc - original price
	 * 
	 * @param itemNumber
	 *            - item to get the original price
	 * @param storeNumber
	 *            - current store number
	 * @return
	 */
	private static CurrencyIfc getItemOriginalPrice(String itemNumber,
			String storeNumber) {
		CurrencyIfc itemOriginalPrice = null;

		// creates instance of PLU transaction
		GDYNPLUTransaction pluTransaction = null;
		pluTransaction = (GDYNPLUTransaction) DataTransactionFactory
				.create(DataTransactionKeys.PLU_TRANSACTION);

		// set search criteria
		SearchCriteriaIfc searchInquiry = ((DomainObjectFactoryIfc) DomainGateway
				.getFactory()).getSearchCriteriaInstance();
		searchInquiry.setItemNumber(itemNumber);
		searchInquiry.setStoreNumber(storeNumber);

		try {
			// call to database operation and retrieve the original price
			itemOriginalPrice = pluTransaction
					.getItemOriginalPrice(searchInquiry);
		} catch (DataException de) {
			logger.warn("ItemNo: " + searchInquiry.getItemNumber());

			logger.warn("Error: " + de.getMessage() + " \n " + de + "");
		}

		return itemOriginalPrice;
	}

	/**
	 * Displays the no valid discounts error dialog screen.
	 * 
	 * @param ui
	 *            The POSUIManager
	 * @param msg
	 *            The string array representing the arguments for the dialog
	 */
	private static void showInvalidDiscountDialog(POSUIManagerIfc ui,
			String[] msg) {
		DialogBeanModel dialogModel = new DialogBeanModel();
		dialogModel.setResourceID(PricingCargo.INVALID_DISC);
		dialogModel.setType(DialogScreensIfc.ERROR);
		dialogModel.setArgs(msg);
		ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
	}

	// Begin GD-384: Only applies employee discount to items posted to sell item
	// screen at time employee discount applied - not to items added afterwards
	// lcatania (Starmount) Apr 26, 2013
	/**
	 * Calculates item discount and apply it if it is valid
	 * GDYNEmployeeDiscountUtility void
	 * 
	 * @param bus
	 */
	public static void calculateDiscountItemAdded(BusIfc bus) {

		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();
		
		if (transaction.getEmployeeDiscountID() != null && !"".equalsIgnoreCase(transaction.getEmployeeDiscountID())) {
			transaction.clearAdvancedPricingRules();
			transaction.clearTransactionDiscounts(true);
			BigDecimal grgDiscPerc = new BigDecimal(0);
			BigDecimal dynDiscPerc = new BigDecimal(0);

			SaleReturnLineItemIfc lineItem = cargo.getLineItem();

			EmployeeIfc discountEmployee = cargo.getDiscountEmployee();
			LocalizedCodeIfc reasonCode = getEmployeeDiscountReasonCode(bus);
			// Commented below condition and added new line by Monica to get
			// original price on a subsequent scan of the item when employee
			// discount is performed.
			// String storeID = cargo.getOperator().getStoreID();
			String storeID = cargo.getRegister().getWorkstation().getStoreID();

			ItemDiscountStrategyIfc discountStrategy = getItemDiscountStrategy(
					lineItem, discountEmployee, reasonCode);

			if (discountStrategy != null) {
				if (isDiscountValid(lineItem, discountStrategy, storeID)) {
					applyDiscountItemAdded(bus, lineItem, discountStrategy);
				} else {
					logger.warn("Invalid Discount for Item: "
							+ lineItem.getItemID());
				}
			}

			Vector<SaleReturnLineItem> saleLineItems = transaction
					.getItemContainerProxy().getLineItemsVector();
		
			/*	new method added by dharmendra on 24/08/2016 to fix POS-209 issue. */
			setItemsElligibilityDetails(saleLineItems);
			
			BigDecimal maxSpendLimitGRG = new BigDecimal(0);
			BigDecimal maxSpendLimitDYN = new BigDecimal(0);

			if (responseObjects != null && responseObjects.length == 1
					&& responseObjects[0].getDiscDivision() == null) {
				BigDecimal maxSpendLimit = responseObjects[0]
						.getMaxSpendLimit();
				grgDiscPerc = responseObjects[0].getDiscPercentage();
				applyStoreEmployeeDiscount(saleLineItems, maxSpendLimit,
						grgDiscPerc, responseObjects[0].getEmpIdSrc(),
						responseObjects[0].getEmpGroupId(),
						responseObjects[0].getPeriodId(),
						responseObjects[0].getEntitlementId());

			} else {

				for (GDYNEmployeeDiscResponseObject resp : responseObjects) {
					if (GDYNEepConstants.DYNAMITE.equalsIgnoreCase(resp
							.getDiscDivision())) {
						dynDiscPerc = resp.getDiscPercentage();

						maxSpendLimitDYN = resp.getMaxSpendLimit();
						applyHOEmployeeDiscount(saleLineItems,
								maxSpendLimitDYN, GDYNEepConstants.DYNAMITE,
								dynDiscPerc, resp.getEmpIdSrc(),
								resp.getEmpGroupId(), resp.getPeriodId(),
								resp.getEntitlementId());
					} else {

						grgDiscPerc = resp.getDiscPercentage();
						maxSpendLimitGRG = resp.getMaxSpendLimit();
						applyHOEmployeeDiscount(saleLineItems,
								maxSpendLimitGRG, GDYNEepConstants.GARAGE,
								grgDiscPerc, resp.getEmpIdSrc(),
								resp.getEmpGroupId(), resp.getPeriodId(),
								resp.getEntitlementId());
					}
				}
			}

			transaction.updateTransactionTotals();
		}else{
			logger.info("transaction "+transaction.getTransactionID()+" is not an employee transaction so setting response object null");
			GDYNEmployeeDiscountUtility.responseObjects=null;
			GDYNShowSaleScreenSite.REMAINING_SPEND_STRING = "";
		}

	}

	public static void applyStoreEmployeeDiscount(
			Vector<SaleReturnLineItem> saleLineItems, BigDecimal maxSpendLimit,
			BigDecimal discountPercent, String emplIdSrc, int emplGrpId,
			int periodId, int entitlementId) {
		Set<BigDecimal> itemPriceSet = new TreeSet<BigDecimal>(
				Collections.reverseOrder());

		logger.info("Method applyStoreEmployeeDiscount entered  "
				+ "emplGrpId " + emplGrpId + " periodId " + periodId
				+ " entitlementId " + entitlementId);
		logger.info("maxSpendLimit " + maxSpendLimit + " discountPercent "
				+ discountPercent + " emplIdSrc " + emplIdSrc);
		setStoreItemDefaultValues(itemPriceSet, saleLineItems, discountPercent,
				emplIdSrc, emplGrpId, periodId, entitlementId);
		List<String> list = new ArrayList<String>();

		addStoreItemsIdList(list, itemPriceSet, saleLineItems);
		Boolean isFullOrZeroDiscount = Boolean.FALSE;
		/*if((discountPercent.compareTo(new BigDecimal(100))==0) ||(discountPercent.compareTo(BigDecimal.ZERO)==0)){
			isFullOrZeroDiscount = Boolean.TRUE;
		}
		if(isFullOrZeroDiscount){
			
		}*/
		calculateStoreEmployeeDiscount(list, saleLineItems, maxSpendLimit,
				discountPercent);

	}

	public static void setStoreItemDefaultValues(Set<BigDecimal> itemPriceSet,
			Vector<SaleReturnLineItem> saleLineItems,
			BigDecimal discountPercent, String emplIdSrc, int emplGrpId,
			int periodId, int entitlementId) {
		for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {

			getItemEmployeeDiscount(saleReturnLineItem.getPLUItem());
			saleReturnLineItem.getPLUItem().setDiscountConsidered(Boolean.TRUE);
			itemPriceSet.add(saleReturnLineItem.getItemPrice()
					.getSellingPrice().getDecimalValue());
			saleReturnLineItem.getPLUItem()
					.setDiscountConsidered(Boolean.FALSE);
			saleReturnLineItem.getItemPrice().getItemDiscountAmount()
					.setDecimalValue(new BigDecimal(0));
			setItemOtherDefaultValues(saleReturnLineItem, emplIdSrc, emplGrpId,
					periodId, entitlementId, responseObjects);
		}
	}
	
	public static void addStoreItemsIdList(List<String> list,
			Set<BigDecimal> itemPriceSet, 
			Vector<SaleReturnLineItem> saleLineItems) {
		for (BigDecimal price : itemPriceSet) {
			for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {
				if (saleReturnLineItem.getItemPrice().getSellingPrice()
						.getDecimalValue().equals(price)) {
					list.add(saleReturnLineItem.getPLUItemID());
				}
			}
		}
		
		
	}
	
	/*public static void calculateFullEmployeeDiscount(List<String> list,
			Vector<SaleReturnLineItem> saleLineItems, BigDecimal maxSpendLimit,
			BigDecimal discountPercent) {

		for (String s : list) {
			for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {
				BigDecimal sellingPrice = saleReturnLineItem.getItemPrice()
						.getSellingPrice().getDecimalValue();
				BigDecimal nonDefDiscountPercent = getItemEmployeeDiscount(saleReturnLineItem
						.getPLUItem());

				logger.info("selling price of item "
						+ saleReturnLineItem.getPLUItemID()
						+ "  "
						+ sellingPrice
						+ " empl discount allowed "
						+ saleReturnLineItem.getPLUItem()
								.getItemClassification()
								.getEmployeeDiscountAllowedFlag()
						+ "Item default discount " + isDefEmplDiscApplied
						+ " discountPercent " + discountPercent
						+ " nonDefDiscountPercent " + nonDefDiscountPercent);

				Boolean isItemElligible = saleReturnLineItem.getPLUItem()
						.getItemClassification()
						.getEmployeeDiscountAllowedFlag();
			BigDecimal	discountAmount = BigDecimal.ZERO;
				if(!isItemElligible){
					setItemDiscountAmount(BigDecimal.ZERO, sellingPrice,
							saleReturnLineItem);
					printItemsDiscountByPercent(saleReturnLineItem,
							BigDecimal.ZERO, discountAmount);
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.FALSE);
				} else if (isItemElligible
						&& !isDefEmplDiscApplied
						&& saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) >= 0) {
					setItemDiscountAmount(nonDefDiscountPercent, sellingPrice,
							saleReturnLineItem);
					maxSpendLimit=maxSpendLimit.subtract(sellingPrice);
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.TRUE);
					printItemsDiscountByPercent(saleReturnLineItem,
							nonDefDiscountPercent, discountAmount);
				} else if (saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()) {
					setItemDiscountAmount(discountPercent, sellingPrice,
							saleReturnLineItem);
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.FALSE);
					printItemsDiscountByPercent(saleReturnLineItem,
							discountPercent, discountAmount);
				}
			}
		}
	}
*/	
	public static void calculateStoreEmployeeDiscount(List<String> list,
			Vector<SaleReturnLineItem> saleLineItems, BigDecimal maxSpendLimit,
			BigDecimal discountPercent) {

		for (String s : list) {
			for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {
				BigDecimal sellingPrice = saleReturnLineItem.getItemPrice()
						.getSellingPrice().getDecimalValue();
				BigDecimal nonDefDiscountPercent = getItemEmployeeDiscount(saleReturnLineItem
						.getPLUItem());
				BigDecimal discountAmount = BigDecimal.ZERO;
				// BigDecimal discountPercent = BigDecimal.ZERO;
				logger.info("selling price of item "
						+ saleReturnLineItem.getPLUItemID()
						+ "  "
						+ sellingPrice
						+ " empl discount allowed "
						+ saleReturnLineItem.getPLUItem()
								.getItemClassification()
								.getEmployeeDiscountAllowedFlag()
						+ "Item default discount " + isDefEmplDiscApplied+" discountPercent "+discountPercent+" nonDefDiscountPercent "+nonDefDiscountPercent);

				Boolean isItemElligible = saleReturnLineItem.getPLUItem()
						.getItemClassification()
						.getEmployeeDiscountAllowedFlag();
				
				if (!isDefEmplDiscApplied
						&& isItemElligible
						&& saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) >= 0) {
					if(nonDefDiscountPercent.compareTo(BigDecimal.ZERO)!=0){
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.TRUE);
					setItemDiscountAmount(nonDefDiscountPercent, sellingPrice, saleReturnLineItem);
					maxSpendLimit = maxSpendLimit.subtract(sellingPrice);
					}else{
						saleReturnLineItem.getPLUItem().setDiscountConsidered(
								Boolean.FALSE);
					}
					
					
					printItemsDiscountByPercent(saleReturnLineItem,
							nonDefDiscountPercent, discountAmount);

				} else if(((discountPercent.compareTo(new BigDecimal(100)) == 0)
						|| (discountPercent.equals(BigDecimal.ZERO)))
						&&saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()) {
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.FALSE);
					if (isItemElligible) {
						setItemDiscountAmount(discountPercent, sellingPrice,
								saleReturnLineItem);
						printItemsDiscountByPercent(saleReturnLineItem,
								discountPercent, discountAmount);
					} else if (!isItemElligible) {
						setItemDiscountAmount(BigDecimal.ZERO, sellingPrice,
								saleReturnLineItem);
						printItemsDiscountByPercent(saleReturnLineItem,
								BigDecimal.ZERO, discountAmount);

					}
				} else if (saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) >= 0
						&& isItemElligible) {

					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.TRUE);

					setItemDiscountAmount(discountPercent, sellingPrice,
							saleReturnLineItem);
					maxSpendLimit = maxSpendLimit.subtract(sellingPrice);

					if (saleReturnLineItem.getItemDiscountsByPercentage() != null
							&& saleReturnLineItem
									.getItemDiscountsByPercentage().length != 0) {
						saleReturnLineItem.getItemDiscountsByPercentage()[0]
								.setDiscountRate(discountPercent
										.divide(new BigDecimal(100)));

						discountAmount = (saleReturnLineItem.getItemPrice()
								.getSellingPrice().getDecimalValue()
								.multiply(discountPercent))
								.divide(new BigDecimal(100));
						discountAmount.setScale(1, BigDecimal.ROUND_HALF_UP);
						saleReturnLineItem.getItemDiscountsByPercentage()[0]
								.getDiscountAmount().setDecimalValue(
										discountAmount);
						logger.info(" printItemsDiscountByPercent  "
								+ saleReturnLineItem.getPLUItemID()
								+ " : entdiscount Amount " + discountAmount
								+ " : discount perc " + discountPercent);
					}

				} else if (saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) < 0
						&& isItemElligible) {
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.TRUE);
					printItemsDiscountByPercent(saleReturnLineItem,
							BigDecimal.ZERO, discountAmount);
				} else if (!isItemElligible) {
					logger.info("in Elligible item "
							+ saleReturnLineItem.getPLUItemID());
					printItemsDiscountByPercent(saleReturnLineItem,
							BigDecimal.ZERO, discountAmount);
				}				

			}

		}
	}
	
	
	
	public static void setItemDiscountAmount(BigDecimal discountPercent,BigDecimal sellingPrice, SaleReturnLineItem saleReturnLineItem){
	 	BigDecimal discountAmount = (sellingPrice.multiply(discountPercent))
				.divide(new BigDecimal(100));
		discountAmount.setScale(1, BigDecimal.ROUND_HALF_UP);
		saleReturnLineItem.getItemPrice().getItemDiscountAmount()
				.setDecimalValue(discountAmount);

	}
	public static void applyHOEmployeeDiscount(
			Vector<SaleReturnLineItem> saleLineItems, BigDecimal maxSpendLimit,
			String divisionName, BigDecimal discountPercent, String emplIdSrc,
			int emplGrpId, int periodId, int entitlementId) {
		logger.info(" inside applyHOEmployeeDiscount emplIdSrc " + emplIdSrc
				+ "discount percent " + discountPercent + "  maxSpendLimit "
				+ maxSpendLimit);
		logger.info("discountPercent  " + discountPercent + " : emplGrpId "
				+ emplGrpId + " periodId " + periodId + " entitlementId "
				+ entitlementId);

		Set<BigDecimal> itemPriceSet = new TreeSet<BigDecimal>(
				Collections.reverseOrder());

		setHOItemsDefaultValues(itemPriceSet, saleLineItems, divisionName,
				discountPercent, emplIdSrc, emplGrpId, periodId, entitlementId);
		List<String> list = new ArrayList<String>();
		addHOItemsIdList(list, itemPriceSet, divisionName, saleLineItems);
		calculateHOEmployeeDiscount(list, saleLineItems, maxSpendLimit,
				discountPercent, divisionName);

	}

	public static void setHOItemsDefaultValues(Set<BigDecimal> itemPriceSet,
			Vector<SaleReturnLineItem> saleLineItems, String divisionName,
			BigDecimal discountPercent, String emplIdSrc, int emplGrpId,
			int periodId, int entitlementId) {
		for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {
			logger.info(("before compare Division Name for item id "
					+ saleReturnLineItem.getPLUItemID() + " : " + saleReturnLineItem
					.getPLUItem().getItemDivision()));
			if(Util.isEmpty(saleReturnLineItem.getPLUItem()
					.getItemDivision())){
				itemPriceSet.add(saleReturnLineItem.getItemPrice()
						.getSellingPrice().getDecimalValue());
				saleReturnLineItem.getPLUItem().setDiscountConsidered(
						Boolean.FALSE);

				saleReturnLineItem.getItemPrice().getItemDiscountAmount()
						.setDecimalValue(new BigDecimal(0));
			}else if (divisionName.equalsIgnoreCase(saleReturnLineItem.getPLUItem()
					.getItemDivision())) {

				getItemEmployeeDiscount(saleReturnLineItem.getPLUItem());

				itemPriceSet.add(saleReturnLineItem.getItemPrice()
						.getSellingPrice().getDecimalValue());
				saleReturnLineItem.getPLUItem().setDiscountConsidered(
						Boolean.FALSE);

				saleReturnLineItem.getItemPrice().getItemDiscountAmount()
						.setDecimalValue(new BigDecimal(0));

			}
						
			setItemOtherDefaultValues(saleReturnLineItem, emplIdSrc, emplGrpId, periodId, entitlementId,responseObjects);
			
		}
	}
	

	public static void setItemOtherDefaultValues(SaleReturnLineItem saleReturnLineItem,
			 String emplIdSrc, int emplGrpId,
			int periodId, int entitlementId, GDYNEmployeeDiscResponseObject[] responseObjectsArray) {
		
		logger.info("inside setItemOtherDefaultValues "+saleReturnLineItem.getPLUItemID() 
				+ " item is returnable "+saleReturnLineItem.isReturnable()
				+" saleReturnLineItem.isReturnLineItem() "+saleReturnLineItem.isReturnLineItem());
		

		logger.info("ietm discount considered "+ saleReturnLineItem.getPLUItemID()+" : "+saleReturnLineItem.getPLUItem().isDiscountConsidered());
		saleReturnLineItem.getPLUItem().clearTemporaryPriceChanges();	
		saleReturnLineItem.getPLUItem().setEmplIdSrc(emplIdSrc);
		saleReturnLineItem.getPLUItem().setEmplGrpId(emplGrpId);
		saleReturnLineItem.getPLUItem().setPeriodId(periodId);
		if (responseObjectsArray != null && responseObjectsArray.length != 0) {
			
			if (responseObjectsArray.length == 1) {
				logger.info("EntitlementId for item "+saleReturnLineItem.getPLUItemID()+" : "+responseObjectsArray[0].getEntitlementId());
				saleReturnLineItem.getPLUItem().setEntitlementId(
						responseObjectsArray[0].getEntitlementId());
			} else {
				for (GDYNEmployeeDiscResponseObject resp : responseObjectsArray) {
					if (!Util.isEmpty(saleReturnLineItem.getPLUItem()
									.getItemDivision()) && (Integer.parseInt(resp.getDiscDivision()) == Integer
							.parseInt(saleReturnLineItem.getPLUItem()
									.getItemDivision()))) {
						
						saleReturnLineItem.getPLUItem().setEntitlementId(
								resp.getEntitlementId());
						logger.info("EntitlementId for item "
								+ saleReturnLineItem.getPLUItemID()
								+ " : "
								+ resp.getEntitlementId()
								+ " : "
								+ saleReturnLineItem.getPLUItem()
										.getEntitlementId());

					}
				}
			}
		}
		
	}
	
	
/*	new method added by dharmendra on 24/08/2016 to fix POS-209 issue. 
	This method set employee discount flag to false and 
	ItemDiscountsByPercentage as null for unknown items*/
	
	protected static void setItemsElligibilityDetails(Vector<SaleReturnLineItem> saleLineItems){
		if(saleLineItems!=null&&saleLineItems.size()!=0){
			for(SaleReturnLineItem srli : saleLineItems){
				/*if(srli.getPLUItem().getTemporaryPriceChanges()!=null && srli.getPLUItem().getTemporaryPriceChanges().length!=0){
					srli.getPLUItem().setTemporaryPriceChanges(null);
				}*/
				if(srli.getPLUItem()!=null && srli.getPLUItem()
						.getItemClassification()!=null &&Util.isEmpty(srli.getPLUItem().getItemDivision())){
					srli.setItemDiscountsByPercentage(null);
					srli.getPLUItem()
					.getItemClassification().setEmployeeDiscountAllowedFlag(Boolean.FALSE);
					logger.info("setting item elligibility to false for item "+srli.getPLUItemID());
				}
			}
		}
	}
	
	
	public static void addHOItemsIdList(List<String> list,
			Set<BigDecimal> itemPriceSet, String divisionName,
			Vector<SaleReturnLineItem> saleLineItems) {
		for (BigDecimal price : itemPriceSet) {
			for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {
				 if (divisionName.equalsIgnoreCase(saleReturnLineItem
						.getPLUItem().getItemDivision())
						&& saleReturnLineItem.getItemPrice().getSellingPrice()
								.getDecimalValue().equals(price)) {
					list.add(saleReturnLineItem.getPLUItemID());
				}
			}
		}
	}

	public static void printItemsDiscountByPercent(
			SaleReturnLineItem saleReturnLineItem, BigDecimal discountPercent,
			BigDecimal discountAmount) {
		
		
		if (saleReturnLineItem.getItemDiscountsByPercentage() != null
				&& saleReturnLineItem.getItemDiscountsByPercentage().length != 0) {
			saleReturnLineItem.getItemDiscountsByPercentage()[0]
					.setDiscountRate(discountPercent
							.divide(new BigDecimal(100)));
			
			 discountAmount = (saleReturnLineItem.getItemPrice().getSellingPrice().getDecimalValue().multiply(discountPercent))
					.divide(new BigDecimal(100));
			discountAmount.setScale(1, BigDecimal.ROUND_HALF_UP);
			saleReturnLineItem.getItemDiscountsByPercentage()[0]
					.getDiscountAmount().setDecimalValue(discountAmount);
			//System.out.println("From method printItemsDiscountByPercent  "+saleReturnLineItem.getPLUItemID()+ " : entdiscount Amount "+discountAmount+ " : discount perc "+discountPercent);
		}
		
	}

	public static void calculateHOEmployeeDiscount(List<String> list,
			Vector<SaleReturnLineItem> saleLineItems, BigDecimal maxSpendLimit,
			BigDecimal discountPercent, String divisionName) {
		for (String s : list) {
			for (SaleReturnLineItem saleReturnLineItem : saleLineItems) {

				BigDecimal nonDefDiscountPercent = getItemEmployeeDiscount(saleReturnLineItem
						.getPLUItem());

				logger.info("ITEM ID "
						+ saleReturnLineItem.getPLUItemID()
						+ " : Item division "
						+ saleReturnLineItem.getPLUItem().getItemDivision()
						+ " : isEmplDiscElligible  "
						+ saleReturnLineItem.getPLUItem()
								.getItemClassification()
								.getEmployeeDiscountAllowedFlag()
						+ " : isDefEmplDiscApplied : " + isDefEmplDiscApplied
						+ " discountPercent " + discountPercent
						+ " nonDefDiscountPercent " + nonDefDiscountPercent);

				Boolean isItemElligible = saleReturnLineItem.getPLUItem()
						.getItemClassification()
						.getEmployeeDiscountAllowedFlag();

				BigDecimal discountAmount = BigDecimal.ZERO;
				BigDecimal sellingPrice = saleReturnLineItem.getItemPrice()
						.getSellingPrice().getDecimalValue();
				 if (!isDefEmplDiscApplied
						&& isItemElligible
						&& saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) >= 0) {
					if(nonDefDiscountPercent.compareTo(BigDecimal.ZERO)==0){
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.FALSE);
					printItemsDiscountByPercent(saleReturnLineItem,
							nonDefDiscountPercent, discountAmount);
					}else{
						saleReturnLineItem.getPLUItem().setDiscountConsidered(
								Boolean.TRUE);
						setItemDiscountAmount(nonDefDiscountPercent, sellingPrice,
								saleReturnLineItem);
						maxSpendLimit = maxSpendLimit.subtract(sellingPrice);
						printItemsDiscountByPercent(saleReturnLineItem,
								nonDefDiscountPercent, discountAmount);
					}
					logger.info("Applying non default discount percent "
							+ nonDefDiscountPercent + "to Item "
							+ saleReturnLineItem.getPLUItemID());
					

				} else if (((discountPercent.compareTo(new BigDecimal(100)) == 0) || discountPercent
						.equals(BigDecimal.ZERO))
						&& divisionName.equalsIgnoreCase(saleReturnLineItem
								.getPLUItem().getItemDivision())
						&& saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()) {
					logger.info("Applying  default discount percent "
							+ discountPercent + "to Item "
							+ saleReturnLineItem.getPLUItemID());
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.FALSE);
					if (isItemElligible) {
						setItemDiscountAmount(discountPercent, sellingPrice,
								saleReturnLineItem);
						printItemsDiscountByPercent(saleReturnLineItem,
								discountPercent, discountAmount);
					} else if (!isItemElligible) {
						setItemDiscountAmount(BigDecimal.ZERO, sellingPrice,
								saleReturnLineItem);
						printItemsDiscountByPercent(saleReturnLineItem,
								BigDecimal.ZERO, discountAmount);

					}
				}

				else if (divisionName.equalsIgnoreCase(saleReturnLineItem
						.getPLUItem().getItemDivision())
						&& saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) >= 0
						&& saleReturnLineItem.getPLUItem()
								.getItemClassification()
								.getEmployeeDiscountAllowedFlag()) {

					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.TRUE);

					setItemDiscountAmount(discountPercent, sellingPrice,
							saleReturnLineItem);
					maxSpendLimit = maxSpendLimit.subtract(sellingPrice);

					printItemsDiscountByPercent(saleReturnLineItem,
							discountPercent, discountAmount);

				} else if (saleReturnLineItem.getPLUItemID().equals(s)
						&& !saleReturnLineItem.getPLUItem()
								.isDiscountConsidered()
						&& maxSpendLimit.compareTo(sellingPrice) < 0
						&& isItemElligible) {
					saleReturnLineItem.getPLUItem().setDiscountConsidered(
							Boolean.TRUE);
					printItemsDiscountByPercent(saleReturnLineItem,
							BigDecimal.ZERO, discountAmount);
				} /*else if (!isItemElligible) {
					System.out.println("inelligible item "
							+ saleReturnLineItem.getPLUItemID());
					
					printItemsDiscountByPercent(saleReturnLineItem,
							BigDecimal.ZERO, discountAmount);
				}*/

			}
				
		}
	}
	
	/**
	 * Applies the discount to an item GDYNEmployeeDiscountUtility void
	 * 
	 * @param bus
	 * @param srli
	 * @param discountStrategy
	 */
	protected static void applyDiscountItemAdded(BusIfc bus,
			SaleReturnLineItemIfc srli, ItemDiscountStrategyIfc discountStrategy) {
		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		JournalFormatterManagerIfc formatter = (JournalFormatterManagerIfc) bus
				.getManager(JournalFormatterManagerIfc.TYPE);

		// journal removal of previous percent discount
		ItemDiscountStrategyIfc[] currentDiscounts = srli.getItemPrice()
				.getItemDiscountsByPercentage();
		if ((currentDiscounts != null) && (currentDiscounts.length > 0)) {
			// find the percent discount stategy that is a discount.
			for (int j = 0; j < currentDiscounts.length; j++) {
				if (currentDiscounts[j].getAccountingMethod() == DiscountRuleConstantsIfc.ACCOUNTING_METHOD_DISCOUNT
						&& currentDiscounts[j].getAssignmentBasis() == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE) {
					journalDiscount(cargo.getOperator().getEmployeeID(), cargo
							.getTransaction().getTransactionID(),
							formatter.toJournalManualDiscount(srli,
									currentDiscounts[j], true),
							bus.getServiceName());
				}
			}
		}

		srli.clearItemDiscountsByPercentage(
				DiscountRuleConstantsIfc.DISCOUNT_APPLICATION_TYPE_ITEM,
				DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE, false);

		// add discount
		srli.addItemDiscount(discountStrategy);
		srli.calculateLineItemPrice();

		// journal it here
		journalDiscount(
				cargo.getOperator().getEmployeeID(),
				cargo.getTransaction().getTransactionID(),
				formatter
						.toJournalManualDiscount(srli, discountStrategy, false),
				bus.getServiceName());
	}

	/**
	 * Journals discount
	 * 
	 * @param employeeID
	 *            The Employee ID
	 * @param transactionID
	 *            The Transaction ID
	 * @param journalString
	 *            The string to journal
	 * @param serviceName
	 *            debugging info
	 */
	protected static void journalDiscount(String employeeID,
			String transactionID, String journalString, String serviceName) {
		JournalManagerIfc journal = (JournalManagerIfc) Gateway.getDispatcher()
				.getManager(JournalManagerIfc.TYPE);
		if (journal != null) {
			// write to the journal
			journal.journal(employeeID, transactionID, journalString);
		} else {
			logger.warn("No journal manager found!");
		}
	}
	// End GD-384: Only applies employee discount to items posted to sell item
	// screen...

	
}

// End GD-49: Develop Employee Discount Module