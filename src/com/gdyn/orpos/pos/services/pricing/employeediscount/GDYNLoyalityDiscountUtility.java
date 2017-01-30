/*
 * Added by Dharmendra to execute Loyalty Coupon Discount Logic 
 * 
 * 
 */

package com.gdyn.orpos.pos.services.pricing.employeediscount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.util.Vector;

import oracle.retail.stores.domain.discount.AdvancedPricingRuleIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.discount.ItemTransactionDiscountAudit;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.stock.GDYNLoyalityCpnHrchyDtls;
import oracle.retail.stores.domain.stock.GDYNLoyalityMerchHrchyDtls;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;
import com.gdyn.orpos.pos.services.sale.GDYNSaleCargo;

public class GDYNLoyalityDiscountUtility {

	protected static final Logger logger = Logger
			.getLogger(GDYNLoyalityDiscountUtility.class);

	@SuppressWarnings("unchecked")
	// Method is used to Apply Item Level Loyalty Discount
	public static void applyItemLoyalityDiscount(BusIfc bus,
			List<PLUItemIfc> couponItemsList) {
		
		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();

		logger.debug("applyItemLoyalityDiscount method called for transaction "+transaction.getTransactionID());
		
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();

		BigDecimal totalDiscAmtApplied = BigDecimal.ZERO;

		BigDecimal minimumThreshHoldAmt = BigDecimal.ZERO;
		BigDecimal maximumDiscountAmt = BigDecimal.ZERO;
		BigDecimal loyalityDiscountRate = BigDecimal.ZERO;
		String cpnItemApplyTo = "";
		
		for (PLUItemIfc cpnPluItem : couponItemsList) {
			cpnItemApplyTo = cpnPluItem.getLoyalityCpnAttrDtls()
					.getItmApplyTo();

			maximumDiscountAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMaxDiscAmount();
			minimumThreshHoldAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMinMonthlyThrhold();
			if (maximumDiscountAmt == null) {
				maximumDiscountAmt = BigDecimal.ZERO;
			}
			if (minimumThreshHoldAmt == null) {
				minimumThreshHoldAmt = BigDecimal.ZERO;
			}
			if (cpnPluItem.getAdvancedPricingRules() != null
					&& cpnPluItem.getAdvancedPricingRules().length != 0) {
				loyalityDiscountRate = cpnPluItem.getAdvancedPricingRules()[0]
						.getDiscountRate();
			}

			break;
		}
		BigDecimal subtotal = new BigDecimal(0);
		setLoyalityEligibilityFlag(cpnItemApplyTo, lineItemsvector);
		

		lineItemsvector = transaction.getItemContainerProxy()
				.getLineItemsVector();
		subtotal = transaction.getTransactionTotals().getSubtotal()
				.getDecimalValue();
		subtotal = subtotal.multiply(loyalityDiscountRate);

		subtotal.setScale(2, RoundingMode.HALF_UP);
		Boolean searchNextEligibleItem = true;
		PLUItemIfc cpnPLUItemIfc= couponItemsList.get(0);
		for (SaleReturnLineItem srli : lineItemsvector) {
			Boolean hrchyValidityFlag = validateHierarchyDetails(
					srli.getPLUItem(), couponItemsList.get(0));
			BigDecimal itemSellingPrice = srli.getItemPrice().getSellingPrice()
					.getDecimalValue();
			
			
			
			if (!srli.getPLUItem().isStoreCoupon()
					&& !srli.isGiftItem()
					&& srli.getPLUItem().getIsLoylDiscountElligible()
					&& srli.getItemPrice().getSellingPrice().getDecimalValue()
							.compareTo(minimumThreshHoldAmt) >= 0
					&& hrchyValidityFlag) {

				BigDecimal itemDiscountTotal = srli.getItemPrice()
						.getSellingPrice().getDecimalValue();
				if (srli.getItemPrice().getItemDiscounts() != null
						&& srli.getItemPrice().getItemDiscounts().length != 0) {
					ItemDiscountStrategyIfc itemDiscountStrategyIfcs[] = srli.getItemPrice().getItemDiscounts();
					
				 IDS:	for (ItemDiscountStrategyIfc ids : itemDiscountStrategyIfcs) {
						if (!GDYNLoyalityDiscountUtility.isStringEmpty(ids
								.getReferenceID())
								&& cpnPLUItemIfc.getItemID().equalsIgnoreCase(
										ids.getReferenceID())) {
							srli.getPLUItem().setLoyalityDiscountAppliedFlag(true);
							break IDS;
						}
					}
					if ((maximumDiscountAmt.compareTo(BigDecimal.ZERO) == 0)
							|| itemSellingPrice.compareTo(maximumDiscountAmt) <= 0) {
						logger.debug("base logic applied to "
								+ srli.getPLUItemID() + " with line number "
								+ srli.getLineNumber());

					} else {
						logger.debug("else block executed recalculating discount for item "
								+ srli.getPLUItemID()
								+ " with line number "
								+ srli.getLineNumber());
						itemDiscountTotal = maximumDiscountAmt
								.multiply(loyalityDiscountRate);
						itemDiscountTotal.setScale(2, RoundingMode.HALF_UP);
						srli.getItemPrice().getItemDiscountAmount()
								.setDecimalValue(new BigDecimal(0));
						srli.getItemPrice().getItemDiscountTotal()
								.setDecimalValue(itemDiscountTotal);
						srli.getItemPrice()
								.getExtendedDiscountedSellingPrice()
								.setDecimalValue(
										itemSellingPrice
												.subtract(itemDiscountTotal));

					}
					searchNextEligibleItem = false;
				} else {
					logger.debug("2nd else block executed setting discount for item to zero "+srli.getPLUItemID()+
							" with line number "+srli.getLineNumber());
					srli.getItemPrice().getItemDiscountAmount()
							.setDecimalValue(BigDecimal.ZERO);
					srli.getItemPrice().getItemDiscountTotal()
							.setDecimalValue(BigDecimal.ZERO);
					srli.getItemPrice().getExtendedDiscountedSellingPrice()
							.setDecimalValue(itemSellingPrice);
					
				}

			} else {
				logger.debug("3rd else block executed setting discount for item to zero "+srli.getPLUItemID()+
						" with line number "+srli.getLineNumber());
				if (!(srli.getPLUItem().isStoreCoupon() || srli.isGiftItem())) {
					srli.getItemPrice().getItemDiscountAmount()
							.setDecimalValue(BigDecimal.ZERO);
					srli.getItemPrice().getItemDiscountTotal()
							.setDecimalValue(BigDecimal.ZERO);
					srli.getItemPrice().getExtendedDiscountedSellingPrice()
							.setDecimalValue(itemSellingPrice);
				}
				
			}
			logger.debug("discount amount for item "+srli.getPLUItem().getItemID()+" Discount amount : "+srli.getItemPrice().getItemDiscountAmount().getDecimalValue()+
					" Discount total : "+srli.getItemPrice().getItemDiscountTotal().getDecimalValue());
		}

		
		lineItemsvector = transaction.getItemContainerProxy()
				.getLineItemsVector();
		for (SaleReturnLineItemIfc srli : lineItemsvector) {
			totalDiscAmtApplied = totalDiscAmtApplied.add(srli.getItemPrice()
					.getItemDiscountTotal().getDecimalValue());
		}
		if(searchNextEligibleItem){
			BigDecimal nextEligibleDiscAmt = getNextElligibleItem(transaction, maximumDiscountAmt, loyalityDiscountRate,minimumThreshHoldAmt);
			
			totalDiscAmtApplied = totalDiscAmtApplied.add(nextEligibleDiscAmt);
			logger.debug("totalDiscAmtApplied for next eligible item "+totalDiscAmtApplied+ " nextEligibleDiscAmt "+nextEligibleDiscAmt);
		}

		transaction.getTransactionTotals().getDiscountTotal()
				.setDecimalValue(totalDiscAmtApplied);

		BigDecimal transactionTaxTotal = transaction.getTransactionTotals()
				.getTaxTotal().getDecimalValue();
		BigDecimal transactionSubTotal = transaction.getTransactionTotals()
				.getSubtotal().getDecimalValue();
		transactionSubTotal = transactionSubTotal.add(transactionTaxTotal);
		transactionSubTotal = transactionSubTotal.subtract(totalDiscAmtApplied);

		transaction.getTransactionTotals().getGrandTotal()
				.setDecimalValue(transactionSubTotal);
		transaction.updateTenderTotals();
		setItemLoyalityreceiptParameters(transaction);
		setLoyalityTransactionAmtOffTotal(transaction);
	}
	
	
	/*protected static void setItemEligibilityFalse(SaleReturnLineItemIfc srli){
		srli.getPLUItem().setIsLoylDiscountElligible(false);
	}*/
	
	
	@SuppressWarnings("unchecked")
	//POS-334 Method added by Dharmendra to apply multiple Item Level coupons to a single transaction
	public static Boolean applyMultipleItemLoyalityDiscount(BusIfc bus,
			List<PLUItemIfc> couponItemsList) {

		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();
		Vector<SaleReturnLineItemIfc> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		for(SaleReturnLineItemIfc srli:lineItemsvector){
			srli.getPLUItem().setLoyalityDiscountAppliedFlag(false);
		}
		
		logger.debug("applyMultipleItemLoyalityDiscount method called for transaction "
				+ transaction.getTransactionID());
		Boolean eligibleItemsExistFlag = true;
		for(PLUItemIfc cpnPluItem:couponItemsList){
		 eligibleItemsExistFlag = applyMultipleLoyalityCoupons(transaction, cpnPluItem);
		 if(!eligibleItemsExistFlag)
			 break;
		}
		setLoyalityTransactionAmtOffTotal(transaction);
		setLoyalityTransactionGrandTotal(transaction);
		setLoyalityreceiptParameters(transaction);
		transaction.updateTenderTotals();
		return eligibleItemsExistFlag;
	}
	//POS-334 End modifications by Dharmendra to apply multiple Item Level coupons to a single transaction
	
	//POS-334 Method created by Dharmendra to apply item level discount to an item in the transaction
	public static Boolean applyMultipleLoyalityCoupons(
			SaleReturnTransactionIfc transaction, PLUItemIfc cpnPluItem) {
		String cpnItemApplyTo = cpnPluItem.getLoyalityCpnAttrDtls()
				.getItmApplyTo();

		BigDecimal maximumDiscountAmt = cpnPluItem.getLoyalityCpnAttrDtls()
				.getMaxDiscAmount();
		BigDecimal minimumThreshHoldAmt = cpnPluItem.getLoyalityCpnAttrDtls()
				.getMinMonthlyThrhold();
		BigDecimal loyalityDiscountRate = BigDecimal.ZERO;

		Vector<SaleReturnLineItemIfc> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		if (maximumDiscountAmt == null) {
			maximumDiscountAmt = BigDecimal.ZERO;
		}

		if (minimumThreshHoldAmt == null) {
			minimumThreshHoldAmt = BigDecimal.ZERO;
		}

		if (cpnPluItem.getAdvancedPricingRules() != null
				&& cpnPluItem.getAdvancedPricingRules().length != 0) {
			loyalityDiscountRate = cpnPluItem.getAdvancedPricingRules()[0]
					.getDiscountRate();

		}

		Boolean searchNextEligibleItem = true;
		OUTER: for (SaleReturnLineItemIfc srli : lineItemsvector) {

			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
				PLUItemIfc pluItemIfc = srli.getPLUItem();

				if (srli.getItemPrice().getItemDiscounts() != null
						&& srli.getItemPrice().getItemDiscounts().length != 0) {
					BigDecimal itemSellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();
					String referenceId = srli.getItemPrice().getItemDiscounts()[0]
							.getReferenceID();
					if (referenceId != null
							&& referenceId.equalsIgnoreCase(cpnPluItem
									.getItemID())) {
						Boolean isItemEligible = isItemEligible(srli,
								cpnItemApplyTo);
						Boolean hrcyValidityFlag = validateHierarchyDetails(
								pluItemIfc, cpnPluItem);
						logger.debug("Base coupon discount applied to item "
								+ srli.getPLUItemID()
								+ " with line number "
								+ srli.getLineNumber()
								+ " referenceId "
								+ referenceId
								+ " isItemEligible "
								+ isItemEligible
								+ " : hrcyValidityFlag "
								+ hrcyValidityFlag
								+ " pluItemIfc.getLoyalityDiscountAppliedFlag() "
								+ pluItemIfc.getLoyalityDiscountAppliedFlag()
								+ " : itemSellingPrice " + itemSellingPrice
								+ " : minimumThreshHoldAmt "
								+ minimumThreshHoldAmt);
						if (isItemEligible
								&& !pluItemIfc.getLoyalityDiscountAppliedFlag()
								&& hrcyValidityFlag
								&& itemSellingPrice
										.compareTo(minimumThreshHoldAmt) > 0) {
							pluItemIfc.setLoyalityDiscountAppliedFlag(true);
							pluItemIfc.setIsLoylDiscountElligible(true);
							if ((maximumDiscountAmt.compareTo(BigDecimal.ZERO) == 0)
									|| itemSellingPrice
											.compareTo(maximumDiscountAmt) <= 0) {
								logger.debug("base logic applied to "
										+ srli.getPLUItemID()
										+ " with line number "
										+ srli.getLineNumber() + " for coupon "
										+ cpnPluItem.getItemID());

							} else {
								logger.debug("else block executed recalculating discount for item "
										+ srli.getPLUItemID()
										+ " with line number "
										+ srli.getLineNumber()
										+ " for coupon "
										+ cpnPluItem.getItemID());
								BigDecimal itemDiscountTotal = maximumDiscountAmt
										.multiply(loyalityDiscountRate);
								itemDiscountTotal.setScale(2,
										RoundingMode.HALF_UP);
								srli.getItemPrice().getItemDiscountAmount()
										.setDecimalValue(new BigDecimal(0));
								srli.getItemPrice().getItemDiscountTotal()
										.setDecimalValue(itemDiscountTotal);
								srli.getItemPrice()
										.getExtendedDiscountedSellingPrice()
										.setDecimalValue(
												itemSellingPrice
														.subtract(itemDiscountTotal));
							}
							searchNextEligibleItem = false;
							break OUTER;
						} else if (!pluItemIfc.isStoreCoupon()
								&& !srli.isGiftItem()
								&& !pluItemIfc.getLoyalityDiscountAppliedFlag()) {

							pluItemIfc.setIsLoylDiscountElligible(false);
							logger.debug("setting item discount zero for item "
									+ pluItemIfc.getItemID()
									+ " with line number "
									+ srli.getLineNumber() + " for coupon "
									+ cpnPluItem.getItemID());
							setItemDiscountZero(srli);
							searchNextEligibleItem = true;

						}

					}

				}
			}
		}

		Boolean eligibleItemsExist = false;

		if (searchNextEligibleItem) {
			eligibleItemsExist = getNextElligibleItem(transaction, cpnPluItem);
		} else {

			eligibleItemsExist = true;
		}
		logger.debug("Eligible items exists for coupoun id "
				+ cpnPluItem.getItemID() + " : " + eligibleItemsExist);
		return eligibleItemsExist;
	}
	//POS-334 End modifications by Dharmendra to calculate discounts when 

	//POS-334 start modifications by Dharmendra to set discount amount of an item zero 
	public static void setItemDiscountZero(SaleReturnLineItemIfc srli) {
		srli.getItemPrice().getItemDiscountAmount()
				.setDecimalValue(new BigDecimal(0));
		srli.getItemPrice().getItemDiscountTotal()
				.setDecimalValue(new BigDecimal(0));
		srli.getItemPrice()
				.getExtendedDiscountedSellingPrice()
				.setDecimalValue(
						srli.getItemPrice().getSellingPrice().getDecimalValue());
	}
	//POS-334 end modifications by Dharmendra to set discount amount of an item zero
	
	//POS-334 method added by Dharmendra to check if item is eligible or not
	public static Boolean isItemEligible(SaleReturnLineItemIfc srli, String cpnItemApplyTo){
		Boolean isLoyaityDiscountElligible = false;
		if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {

			PLUItemIfc pluItemIfc = srli.getPLUItem();
			
			if (cpnItemApplyTo == null
					|| GDYNLoyalityConstants.blankString
							.equalsIgnoreCase(cpnItemApplyTo)
					|| GDYNLoyalityConstants.itemApplyToB
							.equalsIgnoreCase(cpnItemApplyTo)) {
				isLoyaityDiscountElligible = Boolean.TRUE;
			} else if (GDYNLoyalityConstants.itemApplyToC
					.equalsIgnoreCase(cpnItemApplyTo)
					&& (!pluItemIfc.getItemClassification()
							.getEmployeeDiscountAllowedFlag())) {
				isLoyaityDiscountElligible = Boolean.TRUE;

			} else if (GDYNLoyalityConstants.itemApplyToR
					.equalsIgnoreCase(cpnItemApplyTo)
					&& (pluItemIfc.getItemClassification()
							.getEmployeeDiscountAllowedFlag())) {
				isLoyaityDiscountElligible = Boolean.TRUE;
			}


		}
		return isLoyaityDiscountElligible;
	}
	//POS-334 End of method 
	
	//POS-365 Method created by Dharmendra to get the next eligible item in the transaction and apply discount 
	public static BigDecimal getNextElligibleItem(SaleReturnTransactionIfc transaction , BigDecimal maximumDiscountAmt, BigDecimal loyalityDiscountRate,
			BigDecimal minimumThreshHoldAmt){
		logger.debug("getNextElligibleItem maximumDiscountAmt "+maximumDiscountAmt+" loyalityDiscountRate "+loyalityDiscountRate +" minimumThreshHoldAmt "+minimumThreshHoldAmt);
		Vector<SaleReturnLineItem> lineItemsvector = transaction.getItemContainerProxy().getLineItemsVector();
		Set<BigDecimal> itemPriceSet = new TreeSet<BigDecimal>(Collections.reverseOrder());
		for(SaleReturnLineItemIfc srli: lineItemsvector){
			if(!srli.isGiftItem()&&!srli.getPLUItem().isStoreCoupon()){
			BigDecimal itemSellingPrice =srli.getItemPrice().getSellingPrice().getDecimalValue();
			itemPriceSet.add(itemSellingPrice);
			}
		}
		
		Boolean nextEligibleItemExists = false;
		BigDecimal itemDiscountTotalApplied = BigDecimal.ZERO;
		logger.debug("itemPriceSet for transaction "+transaction.getTransactionID()+" : "+itemPriceSet);
	OUTER:	for (BigDecimal itemPrice : itemPriceSet) {
			for(SaleReturnLineItemIfc srli: lineItemsvector){
				BigDecimal itemSellingPrice =srli.getItemPrice().getSellingPrice().getDecimalValue();
				logger.debug(srli.getPLUItemID()+" : is Store Coupon "+srli.getPLUItem().isStoreCoupon()+ " : is gift Item "+srli.isGiftItem()+" : is Item loyality Eligible "+
						srli.getPLUItem().getIsLoylDiscountElligible()+" : is Loyality Hierarchy Eligible "+ srli.getPLUItem().getIsLoylHrchyElligible()
						+" itemSellingPrice "+itemSellingPrice);
				if(!srli.isGiftItem()&&!srli.getPLUItem().isStoreCoupon()&&srli.getPLUItem().getIsLoylDiscountElligible()&&srli.getPLUItem().getIsLoylHrchyElligible()
						&&(itemPrice.compareTo(itemSellingPrice)==0) 
						&& itemSellingPrice.compareTo(minimumThreshHoldAmt)>=0){
					srli.getPLUItem().setLoyalityDiscountAppliedFlag(true);
					if ((maximumDiscountAmt.compareTo(BigDecimal.ZERO) == 0)
							|| itemSellingPrice.compareTo(maximumDiscountAmt) <= 0) {
						BigDecimal	itemDiscountTotal = itemSellingPrice.multiply(loyalityDiscountRate);
						itemDiscountTotal = itemDiscountTotal.setScale(2, RoundingMode.HALF_UP);
						srli.getItemPrice().getItemDiscountAmount()
								.setDecimalValue(new BigDecimal(0));
						srli.getItemPrice().getItemDiscountTotal()
								.setDecimalValue(itemDiscountTotal);
						srli.getItemPrice()
								.getExtendedDiscountedSellingPrice()
								.setDecimalValue(
										itemSellingPrice
												.subtract(itemDiscountTotal));

						
					} else {
						
					BigDecimal	itemDiscountTotal = maximumDiscountAmt
								.multiply(loyalityDiscountRate);
						itemDiscountTotal.setScale(2, RoundingMode.HALF_UP);
						srli.getItemPrice().getItemDiscountAmount()
								.setDecimalValue(new BigDecimal(0));
						srli.getItemPrice().getItemDiscountTotal()
								.setDecimalValue(itemDiscountTotal);
						srli.getItemPrice()
								.getExtendedDiscountedSellingPrice()
								.setDecimalValue(
										itemSellingPrice
												.subtract(itemDiscountTotal));
					}
					itemDiscountTotalApplied = srli.getItemPrice().getItemDiscountTotal().getDecimalValue();
					nextEligibleItemExists = true;
					logger.debug("Item : "+srli.getPLUItemID()+" with line number "+srli.getLineNumber()+ " is the next eligible item "+srli.getItemPrice().getItemDiscountTotal().getDecimalValue() );
					break OUTER;
				}
				
			}
		}
		
		if(nextEligibleItemExists){
			logger.debug("Next eligible item Exists for transaction "+transaction.getTransactionID());
		}else{
			logger.debug("No eligible item found for transaction "+transaction.getTransactionID());
		}
		return itemDiscountTotalApplied ;
	}
	
	
	//POS-334 Method created by Dharmendra to get the next eligible item in the transaction and apply discount when multiple coupons applied to a transaction
	public static Boolean getNextElligibleItem(
			SaleReturnTransactionIfc transaction, PLUItemIfc cpnPLUItem) {
		logger.debug("getNextElligibleItem called for transaction "
				+ transaction.getTransactionID() + " for coupon item "
				+ cpnPLUItem.getItemID());
		BigDecimal minimumThreshHoldAmt = BigDecimal.ZERO;
		BigDecimal maximumDiscountAmt = BigDecimal.ZERO;
		BigDecimal loyalityDiscountRate = BigDecimal.ZERO;
		String cpnItemApplyTo = "";

		cpnItemApplyTo = cpnPLUItem.getLoyalityCpnAttrDtls().getItmApplyTo();

		maximumDiscountAmt = cpnPLUItem.getLoyalityCpnAttrDtls()
				.getMaxDiscAmount();
		minimumThreshHoldAmt = cpnPLUItem.getLoyalityCpnAttrDtls()
				.getMinMonthlyThrhold();
		if (maximumDiscountAmt == null) {
			maximumDiscountAmt = BigDecimal.ZERO;
		}

		if (minimumThreshHoldAmt == null) {
			minimumThreshHoldAmt = BigDecimal.ZERO;
		}

		if (cpnPLUItem.getAdvancedPricingRules() != null
				&& cpnPLUItem.getAdvancedPricingRules().length != 0) {
			loyalityDiscountRate = cpnPLUItem.getAdvancedPricingRules()[0]
					.getDiscountRate();

		}

		if (GDYNLoyalityDiscountUtility.isStringEmpty(cpnItemApplyTo)) {
			cpnItemApplyTo = "";
		}
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		Set<BigDecimal> itemPriceSet = new TreeSet<BigDecimal>(
				Collections.reverseOrder());
		for (SaleReturnLineItemIfc srli : lineItemsvector) {
			if (!srli.isGiftItem() && !srli.getPLUItem().isStoreCoupon()) {
				BigDecimal itemSellingPrice = srli.getItemPrice()
						.getSellingPrice().getDecimalValue();
				itemPriceSet.add(itemSellingPrice);
			}
		}

		Boolean nextEligibleItemExists = false;

		logger.debug("itemPriceSet for transaction "
				+ transaction.getTransactionID() + " : " + itemPriceSet);
		OUTER: for (BigDecimal itemPrice : itemPriceSet) {
			for (SaleReturnLineItemIfc srli : lineItemsvector) {
				BigDecimal itemSellingPrice = srli.getItemPrice()
						.getSellingPrice().getDecimalValue();
				logger.debug(srli.getPLUItemID() + " : with line number"
						+ srli.getLineNumber() + " : is Store Coupon "
						+ srli.getPLUItem().isStoreCoupon()

						+ " : is gift Item " + srli.isGiftItem()
						+ " : is Item loyality Eligible "
						+ srli.getPLUItem().getIsLoylDiscountElligible()
						+ " : is Loyality Hierarchy Eligible "
						+ srli.getPLUItem().getIsLoylHrchyElligible()
						+ " itemSellingPrice " + itemSellingPrice);
				PLUItemIfc pluItemIfc = srli.getPLUItem();
				Boolean isItemEligible = isItemEligible(srli, cpnItemApplyTo);
				Boolean hrcyValidityFlag = validateHierarchyDetails(pluItemIfc,
						cpnPLUItem);
				if (!srli.isGiftItem()
						&& !pluItemIfc.isStoreCoupon()
						&& isItemEligible
						&& hrcyValidityFlag
						&& (itemPrice.compareTo(itemSellingPrice) == 0)
						&& itemSellingPrice.compareTo(minimumThreshHoldAmt) >= 0
						&& !pluItemIfc.getLoyalityDiscountAppliedFlag()) {
					pluItemIfc.setIsLoylDiscountElligible(true);
					pluItemIfc.setLoyalityDiscountAppliedFlag(true);
					if ((maximumDiscountAmt.compareTo(BigDecimal.ZERO) == 0)
							|| itemSellingPrice.compareTo(maximumDiscountAmt) <= 0) {
						BigDecimal itemDiscountTotal = itemSellingPrice
								.multiply(loyalityDiscountRate);
						itemDiscountTotal = itemDiscountTotal.setScale(2,
								RoundingMode.HALF_UP);
						srli.getItemPrice().getItemDiscountAmount()
								.setDecimalValue(new BigDecimal(0));
						srli.getItemPrice().getItemDiscountTotal()
								.setDecimalValue(itemDiscountTotal);
						srli.getItemPrice()
								.getExtendedDiscountedSellingPrice()
								.setDecimalValue(
										itemSellingPrice
												.subtract(itemDiscountTotal));

					} else {

						BigDecimal itemDiscountTotal = maximumDiscountAmt
								.multiply(loyalityDiscountRate);
						itemDiscountTotal.setScale(2, RoundingMode.HALF_UP);
						srli.getItemPrice().getItemDiscountAmount()
								.setDecimalValue(new BigDecimal(0));
						srli.getItemPrice().getItemDiscountTotal()
								.setDecimalValue(itemDiscountTotal);
						srli.getItemPrice()
								.getExtendedDiscountedSellingPrice()
								.setDecimalValue(
										itemSellingPrice
												.subtract(itemDiscountTotal));
					}

					nextEligibleItemExists = true;
					logger.debug("Item : "
							+ srli.getPLUItemID()
							+ " with line number "
							+ srli.getLineNumber()
							+ " is the next eligible item "
							+ srli.getItemPrice().getItemDiscountTotal()
									.getDecimalValue() + " for coupon id "
							+ cpnPLUItem.getItemID());
					break OUTER;
				}

			}
		}

		if (nextEligibleItemExists) {
			logger.debug("Next eligible item Exists for coupon "
					+ cpnPLUItem.getItemID());
		} else {
			logger.debug("No eligible item found for coupon "
					+ cpnPLUItem.getItemID());
		}
		return nextEligibleItemExists;
	}
	
	//POS-334 End Method to get next eligible item and apply discount

	// Method is used to validate coupon hierarchy details

	public static Boolean validateHierarchyDetails(PLUItemIfc pluItemIfc,
			PLUItemIfc cpnPLUItemIfc) {

		Boolean validatehrchyStatus = Boolean.FALSE;

		GDYNLoyalityMerchHrchyDtls merchandiseHrchyViewDtls = pluItemIfc
				.getLoyalityMerchHrchyDtls();
		List<GDYNLoyalityCpnHrchyDtls> gdynLoyalityCpnHrchyDtls = cpnPLUItemIfc
				.getLoyalityCpnHrchyDtlsList();
		if (gdynLoyalityCpnHrchyDtls == null
				|| gdynLoyalityCpnHrchyDtls.size() == 0) {
			validatehrchyStatus = true;
		} else if (gdynLoyalityCpnHrchyDtls != null
				&& gdynLoyalityCpnHrchyDtls.size() != 0) {
			for (GDYNLoyalityCpnHrchyDtls loyalityCpnHrchyDtls : gdynLoyalityCpnHrchyDtls) {
				String couponDivisionId = loyalityCpnHrchyDtls
						.getDiscountDivision();
				String couponGroupId = loyalityCpnHrchyDtls.getDiscountGroup();
				String couponSubClassId = loyalityCpnHrchyDtls
						.getDiscountSubclass();
				String couponClassId = loyalityCpnHrchyDtls.getDiscountClass();
				String couponDeptId = loyalityCpnHrchyDtls.getDiscountDept();

				

				Boolean deptMatchFlag = Boolean.FALSE;
				if (isStringEmpty(couponDeptId)) {
					deptMatchFlag = Boolean.TRUE;
				} else {
					if ((!isStringEmpty(merchandiseHrchyViewDtls.getDeptId()))
							&& (Integer.parseInt(merchandiseHrchyViewDtls
									.getDeptId()) == Integer
									.parseInt(couponDeptId))) {
						deptMatchFlag = Boolean.TRUE;
					} else {
						deptMatchFlag = Boolean.FALSE;
					}
				}

				Boolean divisionMatchFlag = Boolean.FALSE;
				if (isStringEmpty(couponDivisionId)) {
					divisionMatchFlag = Boolean.TRUE;
				} else {
					if ((!isStringEmpty(merchandiseHrchyViewDtls
							.getDivisionId()))
							&& (Integer.parseInt(merchandiseHrchyViewDtls
									.getDivisionId()) == Integer
									.parseInt(couponDivisionId))) {
						divisionMatchFlag = Boolean.TRUE;
					} else {
						divisionMatchFlag = Boolean.FALSE;
					}
				}

				Boolean grpMatchFlag = Boolean.FALSE;
				if (isStringEmpty(couponGroupId)) {
					grpMatchFlag = Boolean.TRUE;
				} else {
					if ((!isStringEmpty(merchandiseHrchyViewDtls.getGroupId()))
							&& (Integer.parseInt(merchandiseHrchyViewDtls
									.getGroupId()) == Integer
									.parseInt(couponGroupId))) {
						grpMatchFlag = Boolean.TRUE;
					} else {
						grpMatchFlag = Boolean.FALSE;
					}
				}

				Boolean classMatchFlag = Boolean.FALSE;
				if (isStringEmpty(couponClassId)) {
					classMatchFlag = Boolean.TRUE;
				} else {
					if ((!isStringEmpty(merchandiseHrchyViewDtls.getClassId()))
							&& (Integer.parseInt(merchandiseHrchyViewDtls
									.getClassId()) == Integer
									.parseInt(couponClassId))) {
						classMatchFlag = Boolean.TRUE;
					} else {
						classMatchFlag = Boolean.FALSE;
					}
				}

				Boolean subClassMatchFlag = Boolean.FALSE;
				if (isStringEmpty(couponSubClassId)) {
					subClassMatchFlag = Boolean.TRUE;
				} else {
					if ((!isStringEmpty(merchandiseHrchyViewDtls
							.getSubClassId()))
							&& (Integer.parseInt(merchandiseHrchyViewDtls
									.getSubClassId()) == Integer
									.parseInt(couponSubClassId))) {
						subClassMatchFlag = Boolean.TRUE;
					} else {
						subClassMatchFlag = Boolean.FALSE;
					}
				}

				logger.debug("Flag Details for item " + pluItemIfc.getItemID()
						+ " are deptMatchFlag " + deptMatchFlag
						+ " divisionMatchFlag " + divisionMatchFlag
						+ " grpMatchFlag " + grpMatchFlag + " classMatchFlag "
						+ classMatchFlag + " subClassMatchFlag "
						+ subClassMatchFlag);
				if (deptMatchFlag && divisionMatchFlag && grpMatchFlag
						&& classMatchFlag && subClassMatchFlag) {
					validatehrchyStatus = true;
				} else {
					validatehrchyStatus = false;
				}
				if (validatehrchyStatus) {
					break;
				}
			}
		}
		logger.debug("validatehrchyStatus for item "+pluItemIfc.getItemID()+" is " +validatehrchyStatus);
		pluItemIfc.setIsLoylHrchyElligible(validatehrchyStatus);

		return validatehrchyStatus;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	// Method is used to Apply Transaction Level Loyalty Discount, check the
	// hierarchy of coupon
	public static void applyLoyalityDiscount(BusIfc bus,
			List<PLUItemIfc> couponItemsList) {
		logger.debug("inside applyLoyalityDiscount");
		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();

		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();

		BigDecimal totalDiscAmtApplied = BigDecimal.ZERO;
		BigDecimal totalDiscItemSellingPrice = BigDecimal.ZERO;

		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsvector
				.iterator();
		BigDecimal minimumThreshHoldAmt = BigDecimal.ZERO;
		BigDecimal maximumDiscountAmt = BigDecimal.ZERO;
		BigDecimal loyalityDiscountRate = BigDecimal.ZERO;
		String cpnItemApplyTo = "";
		Boolean isAmountOffCoupon = false;
		PLUItemIfc cpnItem = couponItemsList.get(0);
		for (PLUItemIfc cpnPluItem : couponItemsList) {

			maximumDiscountAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMaxDiscAmount();
			minimumThreshHoldAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMinMonthlyThrhold();
			cpnItemApplyTo = cpnPluItem.getLoyalityCpnAttrDtls()
					.getItmApplyTo();
			if (cpnPluItem.getAdvancedPricingRules() != null
					&& cpnPluItem.getAdvancedPricingRules().length != 0) {
				loyalityDiscountRate = cpnPluItem.getAdvancedPricingRules()[0]
						.getDiscountRate();

			}

			AdvancedPricingRuleIfc[] advancedPricingRuleIfcs = cpnPluItem
					.getAdvancedPricingRules();
			if (advancedPricingRuleIfcs != null
					&& advancedPricingRuleIfcs.length != 0
					&& advancedPricingRuleIfcs[0].getDiscountMethod() == 2) {
				isAmountOffCoupon = true;
				maximumDiscountAmt = advancedPricingRuleIfcs[0]
						.getDiscountAmount().getDecimalValue();
				logger.debug("Coupun item "
						+ cpnPluItem.getItemID()
						+ " is an Amount Off Coupon of maximum discount amount "
						+ advancedPricingRuleIfcs[0].getDiscountAmount()
								.getDecimalValue());
			}

		}

		if (maximumDiscountAmt == null) {
			maximumDiscountAmt = BigDecimal.ZERO;
		}

		if (minimumThreshHoldAmt == null) {
			minimumThreshHoldAmt = BigDecimal.ZERO;
		}

		BigDecimal subtotal = new BigDecimal(0);
		BigDecimal totalElligibleItemsAmount = new BigDecimal(0);
		BigDecimal totalElligibleDiscAmount = new BigDecimal(0);
		BigDecimal totalNoOfElligibleitems = new BigDecimal(0);
		BigDecimal transactionSubtotal = transaction.getTransactionTotals()
				.getSubtotal().getDecimalValue();
		setLoyalityEligibilityFlag(cpnItemApplyTo, lineItemsvector);
		BigDecimal totalGiftCardAmount = BigDecimal.ZERO;
		for (SaleReturnLineItem srli : lineItemsvector) {
			if (srli.isGiftItem()) {
				totalGiftCardAmount = totalGiftCardAmount.add(srli
						.getItemPrice().getSellingPrice().getDecimalValue());
			}
		}

		BigDecimal netSubTotal = transactionSubtotal
				.subtract(totalGiftCardAmount);

		logger.debug("totalElligibleDiscAmount  " + totalElligibleDiscAmount
				+ "  : totalElligibleItemsAmount : "
				+ totalElligibleItemsAmount + " : netSubTotal : " + netSubTotal
				+ " minimumThreshHoldAmt " + minimumThreshHoldAmt
				+ " : loyalityDiscountRate : " + loyalityDiscountRate);

		if (isAmountOffCoupon) {

			if (minimumThreshHoldAmt != null
					&& !minimumThreshHoldAmt.equals(BigDecimal.ZERO)
					&& netSubTotal.compareTo(minimumThreshHoldAmt) < 0) {
				clearAmountOffLoyalityDiscount(lineItemsvector);
				transaction.getTransactionTotals().getDiscountTotal()
						.setDecimalValue(BigDecimal.ZERO);

			} else {

				applyAmtOffLoyalityDiscountAmounts(lineItemsvector,
						maximumDiscountAmt, couponItemsList.get(0));
				totalDiscAmtApplied = BigDecimal.ZERO;
				lineItemsvector = transaction.getItemContainerProxy()
						.getLineItemsVector();
				lineItemsVectorIterator = lineItemsvector.iterator();
				while (lineItemsVectorIterator.hasNext()) {
					SaleReturnLineItem srli = lineItemsVectorIterator.next();
					if (!srli.getPLUItem().isStoreCoupon()
							&& !srli.isGiftItem()) {
						totalDiscAmtApplied = totalDiscAmtApplied.add(srli
								.getItemPrice().getItemDiscountTotal()
								.getDecimalValue());
					}

				}
				transaction.getTransactionTotals().getDiscountTotal()
						.setDecimalValue(totalDiscAmtApplied);

			}
		}

		else if ((minimumThreshHoldAmt == null || minimumThreshHoldAmt
				.equals(BigDecimal.ZERO))
				||

				(minimumThreshHoldAmt != null && netSubTotal
						.compareTo(minimumThreshHoldAmt) >= 0)) {

			applyLoyalityPercentOffDiscount(bus, couponItemsList);

		} else if (minimumThreshHoldAmt.compareTo(netSubTotal) > 0) {

			logger.info("!!Minimum threshhold is not met");
			lineItemsvector = transaction.getItemContainerProxy()
					.getLineItemsVector();
			for (SaleReturnLineItem srli : lineItemsvector) {

				if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
					BigDecimal sellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();

					BigDecimal loyalityDiscountAmt = sellingPrice
							.multiply(loyalityDiscountRate);
					loyalityDiscountAmt.setScale(2, RoundingMode.HALF_UP);
					BigDecimal itemDiscountTotal = srli.getItemPrice()
							.getItemDiscountTotal().getDecimalValue();
					itemDiscountTotal.setScale(2, RoundingMode.HALF_UP);
					itemDiscountTotal = itemDiscountTotal
							.subtract(loyalityDiscountAmt);
					srli.getItemPrice().getItemDiscountTotal()
							.setDecimalValue(itemDiscountTotal);
					srli.getItemPrice()
							.getExtendedDiscountedSellingPrice()
							.setDecimalValue(
									sellingPrice.subtract(itemDiscountTotal));
					srli.getItemPrice().getItemDiscountAmount()
							.setDecimalValue(new BigDecimal(0));
				}
			}

			BigDecimal totalTransactionDiscount = new BigDecimal(0);
			lineItemsvector = transaction.getItemContainerProxy()
					.getLineItemsVector();
			for (SaleReturnLineItem srli : lineItemsvector) {
				if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
					totalTransactionDiscount = totalTransactionDiscount
							.add(srli.getItemPrice().getItemDiscountTotal()
									.getDecimalValue());
				}
			}

			logger.debug("total transaction discount amount "
					+ totalTransactionDiscount);
			transaction.getTransactionTotals().getDiscountTotal()
					.setDecimalValue(totalTransactionDiscount);

		}

		setLoyalityTransactionAmtOffTotal(transaction);

		setLoyalityTransactionGrandTotal(transaction);

		setLoyalityreceiptParameters(transaction);

		transaction.updateTenderTotals();

	}

	@SuppressWarnings("unchecked")
	// Method is used to set Loyalty Transaction Amount off total
	public static void setLoyalityTransactionAmtOffTotal(
			SaleReturnTransactionIfc transaction) {
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		BigDecimal trnsDiscountTotal = BigDecimal.ZERO;
		for (SaleReturnLineItemIfc srli : lineItemsvector) {
			BigDecimal itemDiscountTotal = srli.getItemPrice()
					.getItemDiscountTotal().getDecimalValue();
			trnsDiscountTotal = trnsDiscountTotal.add(itemDiscountTotal);
		}
		transaction.getTransactionTotals().getDiscountTotal()
				.setDecimalValue(trnsDiscountTotal);
		transaction.getTransactionTotals().getSaleDiscountAndPromotionTotal()
				.setDecimalValue(trnsDiscountTotal);
	}

	// Method is used to set Loyalty Transaction grand total
	public static void setLoyalityTransactionGrandTotal(
			SaleReturnTransactionIfc transaction) {
		BigDecimal transactionSubTotal = transaction.getTransactionTotals()
				.getSubtotal().getDecimalValue();
		BigDecimal transactiontaxTotal = transaction.getTransactionTotals()
				.getTaxTotalUI().getDecimalValue();
		BigDecimal transactionDiscountTotal = transaction
				.getTransactionTotals().getDiscountTotal().getDecimalValue();
		BigDecimal transactionGrandTotal = transactionSubTotal
				.add(transactiontaxTotal);
		transactionGrandTotal = transactionGrandTotal
				.subtract(transactionDiscountTotal);
		transaction.getTransactionTotals().getGrandTotal()
				.setDecimalValue(transactionGrandTotal);

	}

	
	
	
	
	// Method is used to validate eligibility of loyalty coupon based on
	// ItemApplyTO
	public static void setLoyalityEligibilityFlag(String cpnItemApplyTo,
			Vector<SaleReturnLineItem> lineItemsvector) {

		for (SaleReturnLineItem srli : lineItemsvector) {
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {

				PLUItemIfc pluItemIfc = srli.getPLUItem();
				Boolean isLoyaityDiscountElligible = false;
				if (cpnItemApplyTo == null
						|| GDYNLoyalityConstants.blankString
								.equalsIgnoreCase(cpnItemApplyTo)
						|| GDYNLoyalityConstants.itemApplyToB
								.equalsIgnoreCase(cpnItemApplyTo)) {
					isLoyaityDiscountElligible = Boolean.TRUE;
				} else if (GDYNLoyalityConstants.itemApplyToC
						.equalsIgnoreCase(cpnItemApplyTo)
						&& (!pluItemIfc.getItemClassification()
								.getEmployeeDiscountAllowedFlag())) {
					isLoyaityDiscountElligible = Boolean.TRUE;

				} else if (GDYNLoyalityConstants.itemApplyToR
						.equalsIgnoreCase(cpnItemApplyTo)
						&& (pluItemIfc.getItemClassification()
								.getEmployeeDiscountAllowedFlag())) {
					isLoyaityDiscountElligible = Boolean.TRUE;
				}
				if (isLoyaityDiscountElligible) {
					srli.getPLUItem().setIsLoylDiscountElligible(
							isLoyaityDiscountElligible);

				} else {
					srli.getPLUItem().setIsLoylDiscountElligible(
							isLoyaityDiscountElligible);
				}

			}

		}

	}

	// Method is used to set amount off loyalty discount amount
	public static void setAmtOffLoyalityDiscountAmounts(
			BigDecimal discountAmount, SaleReturnLineItemIfc srli,
			BigDecimal itemSellingPrice) {

		BigDecimal itemDiscountAmount = srli.getItemPrice()
				.getItemDiscountAmount().getDecimalValue();
		itemDiscountAmount = itemDiscountAmount.add(discountAmount);
		srli.getItemPrice().getItemDiscountTotal()
				.setDecimalValue(itemDiscountAmount);
		srli.getItemPrice().getExtendedDiscountedSellingPrice()
				.setDecimalValue(itemSellingPrice.subtract(itemDiscountAmount));

	}

	// Method is used to set amount off loyalty discount coupon
	public static void applyAmtOffLoyalityDiscountAmounts(
			Vector<SaleReturnLineItem> lineItemsvector,
			BigDecimal maximumDiscountAmt, PLUItemIfc cpnPLUItemIfc) {
		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsvector
				.iterator();
		BigDecimal totalNoOfElligibleItem = BigDecimal.ZERO;
		BigDecimal totalElligibleItemsAmt = BigDecimal.ZERO;
		BigDecimal discountPercent = BigDecimal.ZERO;
		BigDecimal totalNonLoyalityDiscAmt = BigDecimal.ZERO;
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			BigDecimal itemSellingPrice = srli.getItemPrice().getSellingPrice()
					.getDecimalValue();
			PLUItemIfc pluItemIfc = srli.getPLUItem();
			Boolean hrchyValidityFlag = Boolean.FALSE;

			validateHierarchyDetails(pluItemIfc, cpnPLUItemIfc);

			logger.debug("Hierarchy Validity of Item  " + pluItemIfc.getItemID()
					+ " : " + pluItemIfc.getIsLoylHrchyElligible() + " : "
					+ hrchyValidityFlag);
			if (!pluItemIfc.isStoreCoupon() && !srli.isGiftItem()
					&& srli.getPLUItem().getIsLoylDiscountElligible()
					&& pluItemIfc.getIsLoylHrchyElligible()) {
				totalNonLoyalityDiscAmt = totalNonLoyalityDiscAmt.add(srli
						.getItemPrice().getItemDiscountAmount()
						.getDecimalValue());
				totalNoOfElligibleItem = totalNoOfElligibleItem
						.add(BigDecimal.ONE);
				totalElligibleItemsAmt = totalElligibleItemsAmt
						.add(itemSellingPrice);
			}

		}

		if (totalElligibleItemsAmt.compareTo(BigDecimal.ZERO) > 0) {
			discountPercent = maximumDiscountAmt.divide(totalElligibleItemsAmt,
					4, RoundingMode.HALF_UP);
			discountPercent = discountPercent.multiply(new BigDecimal(100));

		} else {
			discountPercent = BigDecimal.ZERO;
		}

		BigDecimal totalItemsScanned = BigDecimal.ZERO;
		BigDecimal totalDiscountAmtApplied = BigDecimal.ZERO;
		totalDiscountAmtApplied.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal availableDiscountAmt = BigDecimal.ZERO;
		lineItemsVectorIterator = lineItemsvector.iterator();

		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();

			BigDecimal itemSellingPrice = srli.getItemPrice().getSellingPrice()
					.getDecimalValue();
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()
					&& srli.getPLUItem().getIsLoylDiscountElligible()
					&& srli.getPLUItem().getIsLoylHrchyElligible()) {

				totalItemsScanned = totalItemsScanned.add(BigDecimal.ONE);

				if (totalItemsScanned.compareTo(totalNoOfElligibleItem) < 0) {
					BigDecimal discountAmount = BigDecimal.ZERO;
					discountAmount = itemSellingPrice.multiply(discountPercent);

					discountAmount = discountAmount
							.setScale(2, RoundingMode.UP);
					discountAmount = discountAmount.divide(new BigDecimal(100));
					setAmtOffLoyalityDiscountAmounts(discountAmount, srli,
							itemSellingPrice);
					totalDiscountAmtApplied = totalDiscountAmtApplied.add(srli
							.getItemPrice().getItemDiscountTotal()
							.getDecimalValue());
				} else {

					maximumDiscountAmt = maximumDiscountAmt
							.add(totalNonLoyalityDiscAmt);
					availableDiscountAmt = maximumDiscountAmt
							.subtract(totalDiscountAmtApplied);

					srli.getItemPrice().getItemDiscountTotal()
							.setDecimalValue(availableDiscountAmt);
				}

			} else {
				setAmtOffLoyalityDiscountAmounts(BigDecimal.ZERO, srli,
						itemSellingPrice);
			}
		}

	}

	@SuppressWarnings("unchecked")
	// Method is added to get loyalty coupon item list
	public static List<PLUItemIfc> getCouponItemsList(BusIfc bus) {
		List<PLUItemIfc> pluItemsList = new ArrayList<PLUItemIfc>();
		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();
		Vector lineItemsvector = transaction.getItemContainerProxy()
				.getLineItemsVector();
		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsvector
				.iterator();
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItemIfc srli = lineItemsVectorIterator.next();
			PLUItemIfc pluItemIfc = srli.getPLUItem();
			if (pluItemIfc.isStoreCoupon()
					&& pluItemIfc.getLoyalityCpnAttrDtls() != null
					&& pluItemIfc.getLoyalityCpnAttrDtls().getLoylCpnId() != null
					&& !"".equalsIgnoreCase(pluItemIfc.getLoyalityCpnAttrDtls()
							.getLoylCpnId())

					&& pluItemIfc.getLoyalityCpnAttrDtls().getLoylCpnType() != null
					&& ("LOYLTY".equalsIgnoreCase(pluItemIfc
							.getLoyalityCpnAttrDtls().getLoylCpnType()) || "TARGET"
							.equalsIgnoreCase(pluItemIfc
									.getLoyalityCpnAttrDtls().getLoylCpnType()))) {
				pluItemsList.add(pluItemIfc);
				GDYNLoyalityConstants.minThreshHoldAmt = pluItemIfc
						.getLoyalityCpnAttrDtls().getMinMonthlyThrhold();
			}
		}
		if (pluItemsList != null && pluItemsList.size() != 0) {
			GDYNLoyalityConstants.isLoyalityCpnExists = Boolean.TRUE;
		} else {
			GDYNLoyalityConstants.isLoyalityCpnExists = Boolean.FALSE;
		}
		return pluItemsList;
	}
	
	
	@SuppressWarnings("unchecked")
	// Method is added to get loyalty coupon item list
	public static List<PLUItemIfc> getAllCouponItemsList(SaleReturnTransactionIfc transaction) {
		List<PLUItemIfc> pluItemsList = new ArrayList<PLUItemIfc>();
		if (transaction != null
				&& transaction.getItemContainerProxy() != null
				&& transaction.getItemContainerProxy().getLineItemsVector() != null
				&& transaction.getItemContainerProxy().getLineItemsVector()
						.size() != 0) {
			Vector lineItemsvector = transaction.getItemContainerProxy()
					.getLineItemsVector();
			Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsvector
					.iterator();
			while (lineItemsVectorIterator.hasNext()) {
				SaleReturnLineItemIfc srli = lineItemsVectorIterator.next();
				PLUItemIfc pluItemIfc = srli.getPLUItem();
				if (pluItemIfc.isStoreCoupon()) {
					pluItemsList.add(pluItemIfc);
					
				}
			}
			if (pluItemsList != null && pluItemsList.size() != 0) {
				GDYNLoyalityConstants.isLoyalityCpnExists = Boolean.TRUE;
			} else {
				GDYNLoyalityConstants.isLoyalityCpnExists = Boolean.FALSE;
			}
		}
		return pluItemsList;
	}


	// Method is added to check the discount scope of the coupon
	public static Boolean isDiscountScopeItem(List<PLUItemIfc> cpnPluItemsList) {
		int discountScope = 0;
		if (cpnPluItemsList != null && cpnPluItemsList.size() != 0) {
			AdvancedPricingRuleIfc[] advancedPricingRuleIfcs = cpnPluItemsList
					.get(0).getAdvancedPricingRules();
			if (advancedPricingRuleIfcs != null
					&& advancedPricingRuleIfcs.length != 0) {
				discountScope = advancedPricingRuleIfcs[0].getDiscountScope();
			}
		}
		if (discountScope == 1) {
			return true;
		} else {
			return false;
		}
	}
	//method is added to check if all coupons in the transaction are of item type
	public static Boolean areAllItemLevelCoupons(
			List<PLUItemIfc> cpnPluItemsList) {
		boolean areAllItemLevelCpns = Boolean.TRUE;
		for (PLUItemIfc pluItemIfc : cpnPluItemsList) {
			boolean isItemLevelCoupon = validateItemLeveCoupon(pluItemIfc);
			if (!isItemLevelCoupon) {

				areAllItemLevelCpns = false;
				break;
			}
		}
		return areAllItemLevelCpns;
	}
	
	//method is added to check if all coupons in the transaction are of item type
		public static Boolean areAllTransactionLevelCoupons(
				List<PLUItemIfc> cpnPluItemsList) {
			boolean areAllTransLevelCpns = Boolean.TRUE;
			for (PLUItemIfc pluItemIfc : cpnPluItemsList) {
				boolean isItemLevelCoupon = validateItemLeveCoupon(pluItemIfc);
				if (isItemLevelCoupon) {

					areAllTransLevelCpns = false;
					break;
				}
			}
			return areAllTransLevelCpns;
		}

	
	// Method is added to check the discount scope of the coupon
		public static Boolean validateItemLeveCoupon(PLUItemIfc cpnPluItem) {
			int discountScope = 0;
			if (cpnPluItem != null) {
				AdvancedPricingRuleIfc[] advancedPricingRuleIfcs = cpnPluItem
						.getAdvancedPricingRules();
				if (advancedPricingRuleIfcs != null
						&& advancedPricingRuleIfcs.length != 0) {
					discountScope = advancedPricingRuleIfcs[0].getDiscountScope();
				}
			}
			if (discountScope == 1) {
				return true;
			} else {
				return false;
			}
		}

	@SuppressWarnings("unchecked")
	// Method is added to apply loyalty percent off discount
	public static void applyLoyalityPercentOffDiscount(BusIfc bus,
			List<PLUItemIfc> couponItemsList) {

		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();
		PLUItemIfc cpnPluItem = couponItemsList.get(0);
		AdvancedPricingRuleIfc[] advancedPricingRuleIfcs = cpnPluItem
				.getAdvancedPricingRules();
		BigDecimal loyalityDiscountRate = BigDecimal.ZERO;

		if (advancedPricingRuleIfcs != null
				&& advancedPricingRuleIfcs.length != 0
				&& advancedPricingRuleIfcs[0].getDiscountRate() != null) {
			loyalityDiscountRate = advancedPricingRuleIfcs[0].getDiscountRate();
			loyalityDiscountRate = loyalityDiscountRate
					.multiply(new BigDecimal(100));
		}

		BigDecimal maximumDiscountAmt = BigDecimal.ZERO;
		BigDecimal minimumThresholdAmt = BigDecimal.ZERO;
		if (cpnPluItem.getLoyalityCpnAttrDtls() != null) {
			// Added by Monica to fix POS-253 issue
			maximumDiscountAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMaxDiscAmount();
			minimumThresholdAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMinMonthlyThrhold();
			if (maximumDiscountAmt == null) {
				maximumDiscountAmt = BigDecimal.ZERO;
			}
			if (minimumThresholdAmt == null) {
				minimumThresholdAmt = BigDecimal.ZERO;
			}
		}
		
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();

		String couponItemApplyTo = cpnPluItem.getLoyalityCpnAttrDtls()
				.getItmApplyTo();
		logger.debug("inside applyLoyalityPercentOffDiscount method for transaction "+transaction.getTransactionID()+" : maximumDiscountAmt "+maximumDiscountAmt+" : minimumThresholdAmt : "
				+minimumThresholdAmt+ " loyalityDiscountRate "+loyalityDiscountRate+ " cpnPluItem.getItemID() "+cpnPluItem.getItemID()+" : couponItemApplyTo : "+couponItemApplyTo);
		BigDecimal transactionSubTotal = transaction.getTransactionTotals()
				.getSubtotal().getDecimalValue();
		lineItemsvector = transaction.getItemContainerProxy()
				.getLineItemsVector();
		BigDecimal totalGiftCardAmount = BigDecimal.ZERO;
		for (SaleReturnLineItem srli : lineItemsvector) {
			if (srli.isGiftItem()) {
				totalGiftCardAmount = totalGiftCardAmount.add(srli
						.getItemPrice().getSellingPrice().getDecimalValue());
				srli.getPLUItem().setIsLoylDiscountElligible(false);
			}
		}
		Map<String, BigDecimal> nonLoyalityDiscountMap = new HashMap<String, BigDecimal>();

		BigDecimal subTotalLessGiftCard = transactionSubTotal
				.subtract(totalGiftCardAmount);
		if (minimumThresholdAmt == null
				|| minimumThresholdAmt.equals(BigDecimal.ZERO)
				|| (minimumThresholdAmt != null && subTotalLessGiftCard
						.compareTo(minimumThresholdAmt) >= 0)) {

			BigDecimal totalLoyalityDiscount = new BigDecimal(0);
			BigDecimal totalNonLoyalityDiscount = new BigDecimal(0);

			for (SaleReturnLineItemIfc srli : lineItemsvector) {
				Boolean hrchyValidityFlag = Boolean.FALSE;
				if (!srli.getPLUItem().isStoreCoupon()) {
					hrchyValidityFlag = validateHierarchyDetails(
							srli.getPLUItem(), cpnPluItem);
				}
				if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()
						&& srli.getPLUItem().getIsLoylDiscountElligible()
						&& hrchyValidityFlag) {

					BigDecimal itemDiscountTotal = srli.getItemPrice()
							.getItemDiscountTotal().getDecimalValue();
					BigDecimal nonLoyalityDiscountAmount = srli.getItemPrice()
							.getItemDiscountAmount().getDecimalValue();
					BigDecimal loyalityDiscountAmt = itemDiscountTotal
							.subtract(nonLoyalityDiscountAmount);
					// BigDecimal totalNonLoyalityDiscountTemp =
					// nonLoyalityDiscountMap.get("totalNonLoyalityDiscount");
					// totalNonLoyalityDiscountTemp =
					// totalNonLoyalityDiscountTemp.add(nonLoyalityDiscountAmount);
					totalNonLoyalityDiscount = totalNonLoyalityDiscount
							.add(nonLoyalityDiscountAmount);
					totalLoyalityDiscount = totalLoyalityDiscount
							.add(loyalityDiscountAmt);
					// nonLoyalityDiscountMap.put("totalNonLoyalityDiscount",
					// totalNonLoyalityDiscountTemp);

				} else {
					removeLoyalityDiscount(srli, loyalityDiscountRate, null);

				}

			}

			nonLoyalityDiscountMap.put("totalNonLoyalityDiscount",
					totalNonLoyalityDiscount);
			totalNonLoyalityDiscount = nonLoyalityDiscountMap
					.get("totalNonLoyalityDiscount");
			
			logger.debug("For transaction "+transaction.getTransactionID()+" totalNonLoyalityDiscount "+totalNonLoyalityDiscount+" : totalLoyalityDiscount "+totalLoyalityDiscount);
			if (maximumDiscountAmt.compareTo(BigDecimal.ZERO) == 0) {

			} else if (totalLoyalityDiscount.compareTo(maximumDiscountAmt) < 0) {
				logger.debug("Applying base discount logic for transaction "+transaction.getTransactionID());
			} else {

				resetLoyalityPercentOffDiscount(transaction, transaction
						.getItemContainerProxy().getLineItemsVector(),
						maximumDiscountAmt, couponItemApplyTo,
						loyalityDiscountRate, cpnPluItem,
						totalNonLoyalityDiscount);
			}
		}
		Vector<SaleReturnLineItemIfc> lineItemsvectorIfc = transaction
				.getItemContainerProxy().getLineItemsVector();
		setLoyalityTransactionTaxDetails(lineItemsvectorIfc);
	}

	public static void setLoyalityTransactionTaxDetails(
			Vector<SaleReturnLineItemIfc> lineItemsvector) {
		
		for (SaleReturnLineItemIfc srli : lineItemsvector) {
			if (srli.getTaxInformationContainer() != null
					&& srli.getTaxInformationContainer().getTaxInformation() != null
					&& srli.getTaxInformationContainer().getTaxInformation().length != 0) {
				// srli.getTaxInformationContainer().getTaxInformation()[0].getTaxableAmount().setDecimalValue(srli.getItemPrice().getExtendedDiscountedSellingPrice().getDecimalValue());
				// srli.getTaxInformationContainer().getTaxInformation()[0].getEffectiveTaxableAmount().setDecimalValue(srli.getItemPrice().getExtendedDiscountedSellingPrice().getDecimalValue());
				srli.getTaxInformationContainer().getTaxInformation()[0]
						.getPositiveTaxableAmount().setDecimalValue(
								srli.getItemPrice()
										.getExtendedDiscountedSellingPrice()
										.getDecimalValue());
			}
		}
	}

	// ///--------------------------------------------------------------------------------------------------------------------------

	// Method is added to apply loyalty transaction discount
	@SuppressWarnings("unchecked")
	public static void applyLoyalityTransactionDiscount(BusIfc bus,
			List<PLUItemIfc> couponItemsList) {

		GDYNSaleCargo cargo = (GDYNSaleCargo) bus.getCargo();
		SaleReturnTransactionIfc transaction = cargo.getTransaction();

		Vector<SaleReturnLineItem> lineItemsvectorTmp = transaction
				.getItemContainerProxy().getLineItemsVector();
		BigDecimal totalItemDiscountAmt = BigDecimal.ZERO;

		logger.debug("Total item discount amount for the transaction "
				+ transaction.getTransactionID() + " with sequnce number "
				+ transaction.getTransactionSequenceNumber()
				+ " totalItemDiscountAmt : " + totalItemDiscountAmt);
		PLUItemIfc cpnPluItem = couponItemsList.get(0);
		AdvancedPricingRuleIfc[] advancedPricingRuleIfcs = cpnPluItem
				.getAdvancedPricingRules();
		BigDecimal loyalityDiscountRate = BigDecimal.ZERO;
		Boolean isAmountOffCoupon = false;
		BigDecimal maxAmtOffDiscount = BigDecimal.ZERO;
		if (advancedPricingRuleIfcs != null
				&& advancedPricingRuleIfcs.length != 0
				&& advancedPricingRuleIfcs[0].getDiscountRate() != null) {
			loyalityDiscountRate = advancedPricingRuleIfcs[0].getDiscountRate();
			loyalityDiscountRate = loyalityDiscountRate
					.multiply(new BigDecimal(100));
			if (advancedPricingRuleIfcs[0].getDiscountMethod() == 2) {
				isAmountOffCoupon = true;
				maxAmtOffDiscount = advancedPricingRuleIfcs[0]
						.getDiscountAmount().getDecimalValue();
				logger.debug("Coupun item "
						+ cpnPluItem.getItemID()
						+ " is an Amount Off Coupon of maximum discount amount "
						+ advancedPricingRuleIfcs[0].getDiscountAmount()
								.getDecimalValue());
			}
		}

		BigDecimal maximumDiscountAmt = BigDecimal.ZERO;

		if (cpnPluItem.getLoyalityCpnAttrDtls() != null) {

			maximumDiscountAmt = cpnPluItem.getLoyalityCpnAttrDtls()
					.getMaxDiscAmount();

			if (maximumDiscountAmt == null) {
				maximumDiscountAmt = BigDecimal.ZERO;
			}
		}

		logger.debug("inside applyLoyalityTransactionDiscount maximumDiscountAmt "
				+ maximumDiscountAmt);
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();

		setLoyalityEligibilityFlag(cpnPluItem.getLoyalityCpnAttrDtls()
				.getItmApplyTo(), lineItemsvector);
		BigDecimal transactionSubTotal = transaction.getTransactionTotals()
				.getSubtotal().getDecimalValue();
		BigDecimal totalGiftCardAmount = BigDecimal.ZERO;
		for (SaleReturnLineItem srli : lineItemsvector) {
			if (srli.isGiftItem()) {
				totalGiftCardAmount = totalGiftCardAmount.add(srli
						.getItemPrice().getSellingPrice().getDecimalValue());
			}
		}
		BigDecimal netSubTotal = transactionSubTotal
				.subtract(totalGiftCardAmount);
		BigDecimal totalDiscountAmtApplied = BigDecimal.ZERO;
		totalDiscountAmtApplied.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal minThreshHoldAmt = BigDecimal.ZERO;
		minThreshHoldAmt = cpnPluItem.getLoyalityCpnAttrDtls()
				.getMinMonthlyThrhold();
		if (isAmountOffCoupon) {
			maximumDiscountAmt = maxAmtOffDiscount;
			if (minThreshHoldAmt != null
					&& !minThreshHoldAmt.equals(BigDecimal.ZERO)
					&& netSubTotal.compareTo(minThreshHoldAmt) < 0) {
				clearAmountOffLoyalityDiscount(lineItemsvector);
				transaction.getTransactionTotals().getDiscountTotal()
						.setDecimalValue(BigDecimal.ZERO);

			} else {
				if (cpnPluItem.getLoyalityCpnAttrDtls().getItmApplyTo() == null
						|| cpnPluItem.getLoyalityCpnAttrDtls().getItmApplyTo()
								.length() == 0
						|| "B".equalsIgnoreCase(cpnPluItem
								.getLoyalityCpnAttrDtls().getItmApplyTo())) {

				} else {

					applyAmtOffLoyalityDiscountAmounts(lineItemsvector,
							maximumDiscountAmt, couponItemsList.get(0));
					BigDecimal totalDiscAmtApplied = BigDecimal.ZERO;
					lineItemsvector = transaction.getItemContainerProxy()
							.getLineItemsVector();
					Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsvector
							.iterator();
					while (lineItemsVectorIterator.hasNext()) {
						SaleReturnLineItem srli = lineItemsVectorIterator
								.next();
						if (!srli.getPLUItem().isStoreCoupon()
								&& !srli.isGiftItem()) {
							totalDiscAmtApplied = totalDiscAmtApplied.add(srli
									.getItemPrice().getItemDiscountTotal()
									.getDecimalValue());
						}

					}
					transaction.getTransactionTotals().getDiscountTotal()
							.setDecimalValue(totalDiscAmtApplied);
				}
			}
		}

		else if (minThreshHoldAmt == null
				|| minThreshHoldAmt.equals(BigDecimal.ZERO)
				|| (minThreshHoldAmt != null && netSubTotal
						.compareTo(minThreshHoldAmt) >= 0)) {

			applyLoyalityPercentOffDiscount(bus, couponItemsList);
		} else if (minThreshHoldAmt.compareTo(netSubTotal) > 0) {
			lineItemsvector = transaction.getItemContainerProxy()
					.getLineItemsVector();
			logger.debug("Minimum threshhold is not met");
			lineItemsvector = transaction.getItemContainerProxy()
					.getLineItemsVector();
			for (SaleReturnLineItem srli : lineItemsvector) {

				if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {

					removeLoyalityDiscount(srli, loyalityDiscountRate, null);
				}
			}

		}

		setLoyalityTransactionAmtOffTotal(transaction);
		setLoyalityTransactionGrandTotal(transaction);
		setLoyalityreceiptParameters(transaction);
		transaction.updateTenderTotals();

	}

	public static Boolean isStringEmpty(String strVal) {
		if (strVal == null || "".equalsIgnoreCase(strVal)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public static void removeLoyalityDiscount(SaleReturnLineItemIfc srli,
			BigDecimal loyalityDiscountRate,
			Map<String, BigDecimal> nonLoyalityDiscountMap) {
		logger.debug("removeLoyalityDiscount method called for item "+srli.getPLUItem().getItemID()+" item line number "+srli.getLineNumber());
		if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
			
			BigDecimal itemSellingPrice = srli.getItemPrice().getSellingPrice()
					.getDecimalValue();
			BigDecimal itemDiscountAmount = srli.getItemPrice()
					.getItemDiscountAmount().getDecimalValue();
			srli.getItemPrice().getItemDiscountTotal()
					.setDecimalValue(itemDiscountAmount);
			srli.getItemPrice()
					.getExtendedDiscountedSellingPrice()
					.setDecimalValue(
							itemSellingPrice.subtract(itemDiscountAmount));
			if (nonLoyalityDiscountMap != null) {
				BigDecimal totalNonLoyalityDiscountTemp = nonLoyalityDiscountMap
						.get("totalNonLoyalityDiscount");
				totalNonLoyalityDiscountTemp = totalNonLoyalityDiscountTemp
						.add(itemDiscountAmount);
				nonLoyalityDiscountMap.put("totalNonLoyalityDiscount",
						totalNonLoyalityDiscountTemp);
			}
		}
		
	}

	public static void clearAllLoyalityDiscount(SaleReturnLineItemIfc srli,
			BigDecimal loyalityDiscountRate,
			BigDecimal totalNonLoyalityDiscount, Map<String, BigDecimal> map) {

		if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
			BigDecimal itemSellingPrice = srli.getItemPrice().getSellingPrice()
					.getDecimalValue();
			BigDecimal itemExtDiscSellingPrice = srli.getItemPrice()
					.getExtendedDiscountedSellingPrice().getDecimalValue();
			itemExtDiscSellingPrice = itemExtDiscSellingPrice
					.multiply(new BigDecimal(100));
			BigDecimal netDiscountRate = new BigDecimal(100)
					.subtract(loyalityDiscountRate);
			itemExtDiscSellingPrice = itemExtDiscSellingPrice.divide(
					netDiscountRate, 3);
			itemExtDiscSellingPrice = itemExtDiscSellingPrice.setScale(2,
					RoundingMode.FLOOR);

			BigDecimal nonLoyalityDiscountAmount = itemSellingPrice
					.subtract(itemExtDiscSellingPrice);

			if (map != null) {
				BigDecimal totalNonLoyalityDiscountTemp = map
						.get("totalNonLoyalityDiscount");
				totalNonLoyalityDiscountTemp = totalNonLoyalityDiscountTemp
						.add(nonLoyalityDiscountAmount);
				map.put("totalNonLoyalityDiscount",
						totalNonLoyalityDiscountTemp);
			}

			srli.getItemPrice().getExtendedDiscountedSellingPrice()
					.setDecimalValue(itemExtDiscSellingPrice);
			srli.getItemPrice().getItemDiscountTotal()
					.setDecimalValue(nonLoyalityDiscountAmount);
		}

	}

	public static void setLoyalityItemDiscount(SaleReturnLineItemIfc srli,
			BigDecimal loyalityDiscountRate,
			BigDecimal totalNonLoyalityDiscount, Map<String, BigDecimal> map) {

		if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {

			BigDecimal itemExtDiscSellingPrice = srli.getItemPrice()
					.getExtendedDiscountedSellingPrice().getDecimalValue();
			itemExtDiscSellingPrice = itemExtDiscSellingPrice
					.multiply(new BigDecimal(100));
			BigDecimal netDiscountRate = new BigDecimal(100)
					.subtract(loyalityDiscountRate);
			itemExtDiscSellingPrice = itemExtDiscSellingPrice.divide(
					netDiscountRate, 3);

			itemExtDiscSellingPrice = itemExtDiscSellingPrice.setScale(2,
					RoundingMode.FLOOR);

			BigDecimal itemDiscountTotal = srli.getItemPrice()
					.getItemDiscountTotal().getDecimalValue();
			srli.getItemPrice().getItemDiscountTotal()
					.setDecimalValue(itemDiscountTotal);
			totalNonLoyalityDiscount = totalNonLoyalityDiscount
					.add(itemDiscountTotal);
			srli.getItemPrice().getExtendedDiscountedSellingPrice()
					.setDecimalValue(itemExtDiscSellingPrice);
		}
	}

	public static void clearAllLoyalityDiscount(
			Vector<SaleReturnLineItem> lineItemsVector) {
		logger.debug("inside clearAllLoyalityDiscount");
		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsVector
				.iterator();
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			if (srli.getPLUItem().getIsLoylDiscountElligible()) {
				srli.getItemPrice().getItemDiscountAmount()
						.setDecimalValue(new BigDecimal(0));
				srli.getPLUItem().setIsLoylDiscountElligible(Boolean.FALSE);
			}
		}
	}

	public static void setInelligibleItemsDiscount(
			Vector<SaleReturnLineItem> lineItemsVector,
			BigDecimal loyalityDiscountRate) {
		logger.debug("inside clearAllLoyalityDiscount");
		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsVector
				.iterator();
		while (lineItemsVectorIterator.hasNext()) {

			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()
					&& !srli.getPLUItem().getIsLoylDiscountElligible()) {
				BigDecimal itemExtDiscSellingPrice = srli.getItemPrice()
						.getExtendedDiscountedSellingPrice().getDecimalValue();
				itemExtDiscSellingPrice = itemExtDiscSellingPrice
						.multiply(new BigDecimal(100));
				BigDecimal netDiscountRate = new BigDecimal(100)
						.subtract(loyalityDiscountRate);
				itemExtDiscSellingPrice = itemExtDiscSellingPrice.divide(
						netDiscountRate, 2);
				itemExtDiscSellingPrice.setScale(2, RoundingMode.HALF_UP);

				srli.getItemPrice().getItemDiscountAmount()
						.setDecimalValue(new BigDecimal(0));

				srli.getItemPrice()
						.getItemDiscountTotal()
						.setDecimalValue(
								srli.getItemPrice().getSellingPrice()
										.getDecimalValue()
										.subtract(itemExtDiscSellingPrice));
				srli.getItemPrice().getExtendedDiscountedSellingPrice()
						.setDecimalValue(itemExtDiscSellingPrice);
			}
		}
	}

	public static void clearAmountOffLoyalityDiscount(
			Vector<SaleReturnLineItem> lineItemsVector) {

		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsVector
				.iterator();
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			BigDecimal itemSellingPrice = srli.getItemPrice().getSellingPrice()
					.getDecimalValue();
			BigDecimal itemDiscountAmount = srli.getItemPrice()
					.getItemDiscountAmount().getDecimalValue();

			srli.getPLUItem().setIsLoylDiscountElligible(Boolean.FALSE);
			srli.getItemPrice().getItemDiscountTotal()
					.setDecimalValue(itemDiscountAmount);
			srli.getItemPrice()
					.getExtendedDiscountedSellingPrice()
					.setDecimalValue(
							itemSellingPrice.subtract(itemDiscountAmount));

		}
	}

	public static void resetLoyalityDiscount(
			SaleReturnTransactionIfc transaction,
			Vector<SaleReturnLineItem> lineItemsVector,
			BigDecimal totalDiscItemSellingPrice,
			BigDecimal maximumDiscountAmt, int numberOfItemsDiscounted) {
		
		logger.debug("totalDiscItemSellingPrice " + totalDiscItemSellingPrice
				+ " maximumDiscountAmt " + maximumDiscountAmt
				+ " numberOfItemsDiscounted " + numberOfItemsDiscounted);
		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsVector
				.iterator();
		BigDecimal totalItemsSize = new BigDecimal("" + numberOfItemsDiscounted
				+ "");
		BigDecimal discountPercentVal = BigDecimal.ZERO;

		if (totalDiscItemSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
			discountPercentVal = maximumDiscountAmt.divide(
					totalDiscItemSellingPrice, 3, RoundingMode.HALF_UP);
			discountPercentVal = discountPercentVal
					.multiply(new BigDecimal(100));

		} else {
			discountPercentVal = BigDecimal.ZERO;
		}
		logger.debug("discountPercentVal after calculation" + discountPercentVal);
		BigDecimal totalItemsScanned = BigDecimal.ZERO;
		BigDecimal totalDiscountAmtApplied = BigDecimal.ZERO;
		totalDiscountAmtApplied.setScale(2, RoundingMode.HALF_DOWN);
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			BigDecimal availableDiscountAmt = BigDecimal.ZERO;
			if (!srli.getPLUItem().isStoreCoupon()
					&& srli.getPLUItem().getIsLoylDiscountElligible()) {
				logger.debug("totalItemsScanned" + totalItemsScanned);
				totalItemsScanned = totalItemsScanned.add(BigDecimal.ONE);
				BigDecimal discountAmount = BigDecimal.ZERO;
				if (totalItemsScanned.compareTo(totalItemsSize) < 0) {
					BigDecimal itemSellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();

					discountAmount = discountAmount.setScale(2,
							RoundingMode.HALF_DOWN);
					discountAmount = itemSellingPrice
							.multiply(discountPercentVal);

					discountAmount = discountAmount.divide(new BigDecimal(100));
					discountAmount = discountAmount.setScale(2,
							RoundingMode.HALF_DOWN);
					logger.debug("Before Applying discount to item  "
							+ srli.getPLUItemID() + " srli.getLineNumber() "
							+ srli.getLineNumber() + " discountAmount "
							+ discountAmount + " maximumDiscountAmt "
							+ maximumDiscountAmt + " availableDiscountAmt "
							+ availableDiscountAmt);
					availableDiscountAmt = maximumDiscountAmt
							.subtract(totalDiscountAmtApplied);
					if (discountAmount.compareTo(availableDiscountAmt) <= 0) {
						srli.getItemPrice().getItemDiscountAmount()
								.setDecimalValue(discountAmount);
						totalDiscountAmtApplied = totalDiscountAmtApplied
								.add(discountAmount.setScale(2,
										RoundingMode.HALF_DOWN));
						logger.debug("After Applying discount to item  "
								+ srli.getPLUItemID()
								+ " srli.getLineNumber() "
								+ srli.getLineNumber() + " discountAmount "
								+ discountAmount + " maximumDiscountAmt "
								+ maximumDiscountAmt + " availableDiscountAmt "
								+ availableDiscountAmt);

					}

				} else if (srli.getPLUItem().getIsLoylDiscountElligible()
						&& (discountAmount.compareTo(availableDiscountAmt) <= 0)) {
					logger.debug("Applying discount to last item  "
							+ srli.getPLUItemID() + " srli.getLineNumber() "
							+ srli.getLineNumber() + " discountAmount "
							+ discountAmount + " maximumDiscountAmt "
							+ maximumDiscountAmt + " availableDiscountAmt "
							+ availableDiscountAmt);

					BigDecimal sellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();
					if (discountAmount.compareTo(sellingPrice) <= 0) {
						srli.getItemPrice()
								.getItemDiscountAmount()
								.setDecimalValue(
										maximumDiscountAmt
												.subtract(totalDiscountAmtApplied));
					}
				}
			}
		}
	}

	// -------------------------------------------------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public static void resetLoyalityPercentOffDiscount(
			SaleReturnTransactionIfc transaction,
			Vector<SaleReturnLineItem> lineItemsVector,
			BigDecimal maximumDiscountAmt, String couponItemApplyTo,
			BigDecimal loyalityDiscountRate, PLUItemIfc cpnPLUItemIfc,
			BigDecimal totalNonLoyalityDiscount) {
		
		 logger.debug("inside resetLoyalityPercentOffDiscount transaction "+transaction.getTransactionID()+" totalNonLoyalityDiscount : "+totalNonLoyalityDiscount+
		" maximumDiscountAmt : "+maximumDiscountAmt);
		Iterator<SaleReturnLineItem> lineItemsVectorIterator = lineItemsVector
				.iterator();

		lineItemsVectorIterator = lineItemsVector.iterator();
		BigDecimal transactionSubTotal = transaction.getTransactionTotals()
				.getSubtotal().getDecimalValue();

		BigDecimal totalElligibleItemsSize = BigDecimal.ZERO;
		BigDecimal totalElligibleItemsPrice = BigDecimal.ZERO;
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			validateHierarchyDetails(srli.getPLUItem(), cpnPLUItemIfc);
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()
					&& srli.getPLUItem().getIsLoylDiscountElligible()
					&& srli.getPLUItem().getIsLoylHrchyElligible()) {
				totalElligibleItemsSize = totalElligibleItemsSize
						.add(new BigDecimal(1));
				totalElligibleItemsPrice = totalElligibleItemsPrice.add(srli
						.getItemPrice().getSellingPrice().getDecimalValue());

				
			}
		}

		totalElligibleItemsPrice = totalElligibleItemsPrice
				.subtract(totalNonLoyalityDiscount);

		lineItemsVector = transaction.getItemContainerProxy()
				.getLineItemsVector();
		lineItemsVectorIterator = lineItemsVector.iterator();
		BigDecimal totalItemsSize = totalElligibleItemsSize;
		BigDecimal totalGiftItemPrice = BigDecimal.ZERO;
		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			if (srli.isGiftItem()) {
				totalGiftItemPrice = totalGiftItemPrice.add(srli.getItemPrice()
						.getSellingPrice().getDecimalValue());
			}
		}

		transactionSubTotal = totalElligibleItemsPrice;

		lineItemsVectorIterator = lineItemsVector.iterator();
		BigDecimal discountPercentVal = BigDecimal.ZERO;
		discountPercentVal.setScale(3, RoundingMode.HALF_DOWN);
		discountPercentVal = maximumDiscountAmt.divide(transactionSubTotal, 4,
				RoundingMode.HALF_UP);
		discountPercentVal = discountPercentVal.multiply(new BigDecimal(100));

		logger.debug("transaction "+transaction.getTransactionID()+" discountPercentVal after calculation "+discountPercentVal+" totalElligibleItemsPrice "+totalElligibleItemsPrice+" total no of elligible items "+totalElligibleItemsSize);
		BigDecimal totalItemsScanned = BigDecimal.ZERO;
		BigDecimal totalDiscountAmtApplied = BigDecimal.ZERO;
		totalDiscountAmtApplied.setScale(2, RoundingMode.HALF_DOWN);

		while (lineItemsVectorIterator.hasNext()) {
			SaleReturnLineItem srli = lineItemsVectorIterator.next();
			Boolean hrchyValidityFlag = validateHierarchyDetails(
					srli.getPLUItem(), cpnPLUItemIfc);
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()
					&& srli.getPLUItem().getIsLoylDiscountElligible()
					&& hrchyValidityFlag) {
				totalItemsScanned = totalItemsScanned.add(BigDecimal.ONE);
				BigDecimal discountAmount = BigDecimal.ZERO;
				// removeLoyalityDiscount(srli, loyalityDiscountRate, null);
				if (totalItemsScanned.compareTo(totalItemsSize) < 0) {

					BigDecimal itemExtendedSellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();
					BigDecimal itemDiscountAmount = srli.getItemPrice()
							.getItemDiscountAmount().getDecimalValue();
					itemExtendedSellingPrice = itemExtendedSellingPrice
							.subtract(itemDiscountAmount);
					discountAmount = discountAmount.setScale(2,
							RoundingMode.HALF_DOWN);
					discountAmount = itemExtendedSellingPrice
							.multiply(discountPercentVal);

					discountAmount = discountAmount.divide(new BigDecimal(100));
					discountAmount = discountAmount.setScale(2,
							RoundingMode.HALF_DOWN);

					discountAmount = discountAmount.add(itemDiscountAmount);

					srli.getItemPrice().getItemDiscountAmount()
							.setDecimalValue(BigDecimal.ZERO);
					srli.getItemPrice().getItemDiscountTotal()
							.setDecimalValue(discountAmount);
					BigDecimal itesmSellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();
					srli.getItemPrice()
							.getExtendedDiscountedSellingPrice()
							.setDecimalValue(
									itesmSellingPrice.subtract(discountAmount));
					totalDiscountAmtApplied = totalDiscountAmtApplied.add(srli
							.getItemPrice().getItemDiscountTotal()
							.getDecimalValue());
					logger.debug("discountAmount applied for item  "+srli.getPLUItem().getItemID()+ " with line number "+srli.getLineNumber()+" : " + discountAmount+ " totalDiscountAmtApplied "+totalDiscountAmtApplied);
				} else {

					BigDecimal sellingPrice = srli.getItemPrice()
							.getSellingPrice().getDecimalValue();
					maximumDiscountAmt = maximumDiscountAmt
							.add(totalNonLoyalityDiscount);

					BigDecimal netDiscAmtLeft = maximumDiscountAmt
							.subtract(totalDiscountAmtApplied);
					netDiscAmtLeft = maximumDiscountAmt
							.subtract(totalDiscountAmtApplied);
					
					srli.getItemPrice().getItemDiscountAmount()
							.setDecimalValue(BigDecimal.ZERO);
					srli.getItemPrice().getItemDiscountTotal()
							.setDecimalValue(netDiscAmtLeft);
					srli.getItemPrice()
							.getExtendedDiscountedSellingPrice()
							.setDecimalValue(
									sellingPrice.subtract(netDiscAmtLeft));
					totalDiscountAmtApplied = totalDiscountAmtApplied
							.add(netDiscAmtLeft);
					totalDiscountAmtApplied = totalDiscountAmtApplied
							.add(netDiscAmtLeft);
					logger.info("netDiscAmtLeft applied to last elligible item "+srli.getPLUItem().getItemID()+ " with line number "+srli.getLineNumber() +" : "+ netDiscAmtLeft);
				}

				logger.info("Discount details for item "
						+ srli.getPLUItemID()
						+ " line no "
						+ srli.getLineNumber()
						+ " discount amount "
						+ srli.getItemPrice().getItemDiscountAmount()
								.getDecimalValue()
						+ " Discount total "
						+ srli.getItemPrice().getItemDiscountTotal()
								.getDecimalValue());
			}

		}

	}

	// Method is added to set loyalty receipt parameters for transaction level
	// coupon
	@SuppressWarnings("unchecked")
	public static void setLoyalityreceiptParameters(
			SaleReturnTransactionIfc transaction) {
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		for (SaleReturnLineItem srli : lineItemsvector) {
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
				ItemDiscountStrategyIfc[] itemDiscounts = srli.getItemPrice()
						.getItemDiscounts();
				srli.getItemPrice().getItemDiscountsByPercentage();
				BigDecimal promoDiscAmount = BigDecimal.ZERO;

				if (itemDiscounts != null && itemDiscounts.length != 0) {
					for (ItemDiscountStrategyIfc ids : itemDiscounts) {
						if (!(ids instanceof ItemTransactionDiscountAudit)) {
							promoDiscAmount = promoDiscAmount.add(ids
									.getDiscountAmount().getDecimalValue());
						}
					}
					itemDiscounts = srli.getItemPrice().getItemDiscounts();
					for (ItemDiscountStrategyIfc ids : itemDiscounts) {
						if (ids instanceof ItemTransactionDiscountAudit) {
							BigDecimal itemDiscountTotal = srli.getItemPrice()
									.getItemDiscountTotal().getDecimalValue();
							BigDecimal netDiscountAmount = itemDiscountTotal
									.subtract(promoDiscAmount);
							ids.getDiscountAmount().setDecimalValue(
									netDiscountAmount);
						}
					}

				}
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static Boolean checkIfCouponAlreadyExits(
			SaleReturnTransactionIfc transaction, PLUItemIfc cpnPluItem) {
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		int count = 0;
		for (SaleReturnLineItem srli : lineItemsvector) {
			if (srli.getPLUItem().isStoreCoupon()
					&& cpnPluItem.getItemID().equalsIgnoreCase(
							srli.getPLUItem().getItemID())) {
				count++;
			}
		}
		if (count > 1) {
			return true;
		} else {
			return false;
		}
	}


	// Method is added to set loyalty receipt parameters for item level coupon
	@SuppressWarnings("unchecked")
	public static void setItemLoyalityreceiptParameters(
			SaleReturnTransactionIfc transaction) {
		Vector<SaleReturnLineItem> lineItemsvector = transaction
				.getItemContainerProxy().getLineItemsVector();
		for (SaleReturnLineItem srli : lineItemsvector) {
			if (!srli.getPLUItem().isStoreCoupon() && !srli.isGiftItem()) {
				BigDecimal sellingPrice = srli.getItemPrice().getSellingPrice()
						.getDecimalValue();
				ItemDiscountStrategyIfc[] itemDiscounts = srli.getItemPrice()
						.getItemDiscounts();
				srli.getItemPrice().getItemDiscountsByPercentage();

				if (itemDiscounts != null && itemDiscounts.length != 0) {

					BigDecimal itemDiscountTotal = srli.getItemPrice()
							.getItemDiscountTotal().getDecimalValue();
					BigDecimal discountRate = itemDiscountTotal.divide(
							sellingPrice, 3);
					discountRate = discountRate.multiply(new BigDecimal(100));
					itemDiscounts[0].getDiscountAmount().setDecimalValue(
							itemDiscountTotal);

				}
			}
		}
	}

}
