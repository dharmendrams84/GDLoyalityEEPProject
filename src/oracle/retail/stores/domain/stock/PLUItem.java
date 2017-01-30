/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package oracle.retail.stores.domain.stock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.commerceservices.externalorder.ExternalOrderItemIfc;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocalizedTextIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.discount.AdvancedPricingRuleIfc;
import oracle.retail.stores.domain.event.ItemPriceMaintenanceEventIfc;
import oracle.retail.stores.domain.event.PriceChangeIfc;
import oracle.retail.stores.domain.factory.DomainObjectFactoryIfc;
import oracle.retail.stores.domain.lineitem.ItemContainerProxyIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.store.DepartmentIfc;
import oracle.retail.stores.domain.tax.NewTaxRuleIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.utility.Util;

import org.apache.log4j.Logger;

import oracle.retail.stores.domain.stock.GDYNLoyalityCpnAttrDtls;


public class PLUItem implements PLUItemIfc {
	private static Logger logger = Logger.getLogger(PLUItem.class);
	static final long serialVersionUID = 7321887861981917535L;
	protected ItemIfc item = null;

	protected RelatedItemContainerIfc relatedItemContainer = null;
	protected boolean itemSizeRequired;
	protected List<PriceChangeIfc> priceChangesTemporary;
	protected List<PriceChangeIfc> priceChangesPermanent;
	protected List<PriceChangeIfc> priceChangesTemporaryForReturns;
	protected ArrayList<AdvancedPricingRuleIfc> advancedPricingRules;
	protected ArrayList<NewTaxRuleIfc> taxRules;
	protected String storeID = "";

	protected String taxGroupName = "";

	protected String manuForCustomerLocale = "";

	protected String manuForDefaultLocale = "";

	protected int restrictiveAge = 0;
	protected int manufacturerID;
	protected String manufacturerItemUPC;
	protected int returnPriceDays = 0;

	EYSDate returnPriceCutoffDate = null;

	protected ExternalOrderItemIfc returnExternalOrderItem = null;

		protected String itemDivision;
		
	protected boolean isDiscountConsidered;
	
	protected String emplIdSrc;
	
	protected int emplGrpId;
	
	protected int periodId;
	
	protected int entitlementId;

	protected GDYNLoyalityCpnAttrDtls loyalityCpnAttrDtls = null;
	
	
	protected List<GDYNLoyalityCpnHrchyDtls> loyalityCpnHrchyDtlsList = null;
	
	protected GDYNLoyalityMerchHrchyDtls loyalityMerchHrchyDtls = null;
	
	protected Boolean isLoylDiscountElligible = null;
	
	protected Boolean isLoylHrchyElligible = null;
	
	/*start POS-334 new variable addeed by Dharmendra to check is loyality discount applied to an item*/
	protected Boolean loyalityDiscountAppliedFlag = Boolean.FALSE;
	
	/*start POS-334 new methods addeed by Dharmendra to check is loyality discount applied to an item*/
	/**
	 * @return the loyalityDiscountAppliedFlag
	 */
	public Boolean getLoyalityDiscountAppliedFlag() {
		return this.loyalityDiscountAppliedFlag;
	}

	/**
	 * @param loyalityDiscountAppliedFlag the loyalityDiscountAppliedFlag to set
	 */
	public void setLoyalityDiscountAppliedFlag(Boolean loyalityDiscountAppliedFlag) {
		this.loyalityDiscountAppliedFlag = loyalityDiscountAppliedFlag;
	}

	/*end POS-334 new methods addeed by Dharmendra to check is loyality discount applied to an item*/
	/**
	 * @return the isLoylHrchyElligible
	 */
	public Boolean getIsLoylHrchyElligible() {
		return isLoylHrchyElligible;
	}

	/**
	 * @param isLoylHrchyElligible the isLoylHrchyElligible to set
	 */
	public void setIsLoylHrchyElligible(Boolean isLoylHrchyElligible) {
		this.isLoylHrchyElligible = isLoylHrchyElligible;
	}

	/**
	 * @return the emplGrpId
	 */
	public int getEmplGrpId() {
		return emplGrpId;
	}

	/**
	 * @return the entitlementId
	 */
	public int getEntitlementId() {
		return entitlementId;
	}

	/**
	 * @param entitlementId the entitlementId to set
	 */
	public void setEntitlementId(int entitlementId) {
		this.entitlementId = entitlementId;
	}

	/**
	 * @param emplGrpId the emplGrpId to set
	 */
	public void setEmplGrpId(int emplGrpId) {
		this.emplGrpId = emplGrpId;
	}

	/**
	 * @return the periodId
	 */
	public int getPeriodId() {
		return periodId;
	}

	/**
	 * @param periodId the periodId to set
	 */
	public void setPeriodId(int periodId) {
		this.periodId = periodId;
	}

	/**
	 * @return the emplIdSrc
	 */
	public String getEmplIdSrc() {
		return emplIdSrc;
	}

	/**
	 * @param emplIdSrc the emplIdSrc to set
	 */
	public void setEmplIdSrc(String emplIdSrc) {
		this.emplIdSrc = emplIdSrc;
	}
	/**
	 * @return the isLoylDiscountElligible
	 */
	public Boolean getIsLoylDiscountElligible() {
		return isLoylDiscountElligible;
	}

	/**
	 * @param isLoylDiscountElligible the isLoylDiscountElligible to set
	 */
	public void setIsLoylDiscountElligible(Boolean isLoylDiscountElligible) {
		this.isLoylDiscountElligible = isLoylDiscountElligible;
	}

	/**
	 * @return the loyalityCpnHrchyDtlsList
	 */
	public List<GDYNLoyalityCpnHrchyDtls> getLoyalityCpnHrchyDtlsList() {
		return loyalityCpnHrchyDtlsList;
	}

	/**
	 * @param loyalityCpnHrchyDtlsList the loyalityCpnHrchyDtlsList to set
	 */
	public void setLoyalityCpnHrchyDtlsList(
			List<GDYNLoyalityCpnHrchyDtls> loyalityCpnHrchyDtlsList) {
		this.loyalityCpnHrchyDtlsList = loyalityCpnHrchyDtlsList;
	}

	/**
	 * @return the loyalityMerchHrchyDtls
	 */
	public GDYNLoyalityMerchHrchyDtls getLoyalityMerchHrchyDtls() {
		return loyalityMerchHrchyDtls;
	}

	/**
	 * @param loyalityMerchHrchyDtls the loyalityMerchHrchyDtls to set
	 */
	public void setLoyalityMerchHrchyDtls(
			GDYNLoyalityMerchHrchyDtls loyalityMerchHrchyDtls) {
		this.loyalityMerchHrchyDtls = loyalityMerchHrchyDtls;
	}

	/**
	 * @return the loyalityCpnAttrDtls
	 */
	public GDYNLoyalityCpnAttrDtls getLoyalityCpnAttrDtls() {
		return loyalityCpnAttrDtls;
	}

	/**
	 * @param loyalityCpnAttrDtls the loyalityCpnAttrDtls to set
	 */
	public void setLoyalityCpnAttrDtls(GDYNLoyalityCpnAttrDtls loyalityCpnAttrDtls) {
		this.loyalityCpnAttrDtls = loyalityCpnAttrDtls;
	}



	
	
  
	public PLUItem() {
		this.item = DomainGateway.getFactory().getItemInstance();
		this.advancedPricingRules = new ArrayList();
		this.priceChangesTemporary = new ArrayList();
		this.priceChangesTemporaryForReturns = new ArrayList();
		this.priceChangesPermanent = new ArrayList();
		this.taxRules = new ArrayList();
		
		retrieveReturnPriceDays();
				
		this.loyalityCpnAttrDtls = new GDYNLoyalityCpnAttrDtls();
		this.loyalityCpnHrchyDtlsList =  new ArrayList<GDYNLoyalityCpnHrchyDtls>();
		this.loyalityMerchHrchyDtls = new GDYNLoyalityMerchHrchyDtls();
		this.isLoylDiscountElligible= Boolean.FALSE;
		this.isLoylHrchyElligible= Boolean.FALSE;
		this.loyalityDiscountAppliedFlag = Boolean.FALSE;
	}

	public List<String> getAdvancedPricingRuleIDs() {
		List ruleIDArray = new ArrayList();
		for (int i = 0; i < this.advancedPricingRules.size(); ++i) {
			AdvancedPricingRuleIfc rule = (AdvancedPricingRuleIfc) this.advancedPricingRules
					.get(i);
			ruleIDArray.add(rule.getRuleID());
		}
		return ruleIDArray;
	}

	public void addAdvancedPricingRule(AdvancedPricingRuleIfc rule) {
		if (!(this.advancedPricingRules.contains(rule)))
			this.advancedPricingRules.add(rule);
	}

	public void addAdvancedPricingRules(AdvancedPricingRuleIfc[] rules) {
		List ruleIDArray = getAdvancedPricingRuleIDs();
		for (int i = 0; i < rules.length; ++i) {
			if (!(ruleIDArray.contains(rules[i].getRuleID())))
				this.advancedPricingRules.add(rules[i]);
		}
	}

	public void addPermanentPriceChange(PriceChangeIfc priceChange) {
		this.priceChangesPermanent.add(priceChange);
		Collections.sort(this.priceChangesPermanent);
	}

	public void addPermanentPriceChanges(PriceChangeIfc[] changes) {
		this.priceChangesPermanent.addAll(Arrays.asList(changes));
		Collections.sort(this.priceChangesPermanent);
	}

	public void addTemporaryPriceChange(PriceChangeIfc priceChange) {
		this.priceChangesTemporary.add(priceChange);
		Collections.sort(this.priceChangesTemporary);
	}

	public void addTemporaryPriceChangeForReturns(PriceChangeIfc priceChange) {
		this.priceChangesTemporaryForReturns.add(priceChange);
		Collections.sort(this.priceChangesTemporaryForReturns);
	}

	public void addTemporaryPriceChanges(PriceChangeIfc[] changes) {
		this.priceChangesTemporary.addAll(Arrays.asList(changes));
		Collections.sort(this.priceChangesTemporary);
	}

	public void addTaxRule(NewTaxRuleIfc rule) {
		if (this.taxRules == null) {
			this.taxRules = new ArrayList();
		}
		this.taxRules.add(rule);
	}

	public void addTaxRules(NewTaxRuleIfc[] rules) {
		if (this.taxRules == null) {
			this.taxRules = new ArrayList();
		}

		if (rules == null)
			return;
		this.taxRules.addAll(Arrays.asList(rules));
	}

	public Iterator<AdvancedPricingRuleIfc> advancedPricingRules() {
		return this.advancedPricingRules.iterator();
	}

	public void clearAdvancedPricingRules() {
		this.advancedPricingRules.clear();
	}

	public void clearPermanentPriceChanges() {
		this.priceChangesPermanent.clear();
	}

	public void clearTemporaryPriceChanges() {
		this.priceChangesTemporary.clear();
	}

	public void clearTaxRules() {
		if (this.taxRules == null)
			return;
		this.taxRules.clear();
	}

	public Object clone() {
		PLUItem newItem = new PLUItem();
		setCloneAttributes(newItem);
		return newItem;
	}

	public boolean equals(Object obj) {
		boolean result = false;
		PLUItem itemIn = null;
		try {
			if (this == obj) {
				result = true;
			} else {
				itemIn = (PLUItem) obj;

				if ((Util.isObjectEqual(this.storeID, itemIn.storeID))
						&& (Util.isObjectEqual(this.taxGroupName,
								itemIn.taxGroupName))
						&& (Util.isObjectEqual(getRelatedItemContainer(),
								itemIn.getRelatedItemContainer()))
						&& (Util.isObjectEqual(this.item, itemIn.item))
						&& (Util.isObjectEqual(this.priceChangesTemporary,
								itemIn.priceChangesTemporary))
						&& (Util.isObjectEqual(
								this.priceChangesTemporaryForReturns,
								itemIn.priceChangesTemporaryForReturns))
						&& (Util.isObjectEqual(this.priceChangesPermanent,
								itemIn.priceChangesPermanent))
						&& (Util.isObjectEqual(this.advancedPricingRules,
								itemIn.advancedPricingRules))
						&& (Util.isObjectEqual(this.taxRules, itemIn.taxRules))
						&& (Util.isObjectEqual(this.taxGroupName,
								itemIn.taxGroupName))
						&& (Util.isObjectEqual(this.taxGroupName,
								itemIn.taxGroupName))
						&& (Util.isObjectEqual(this.returnPriceCutoffDate,
								itemIn.returnPriceCutoffDate))
						&& (Util.isObjectEqual(this.returnExternalOrderItem,
								itemIn.returnExternalOrderItem))
						&& (this.restrictiveAge == itemIn.restrictiveAge)
						&& (this.manufacturerID == itemIn.manufacturerID)
						&& (this.returnPriceDays == itemIn.returnPriceDays)
						&& (this.itemSizeRequired == itemIn.itemSizeRequired)) {
					result = true;
				}
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public AdvancedPricingRuleIfc[] getAdvancedPricingRules() {
		AdvancedPricingRuleIfc[] apr = new AdvancedPricingRuleIfc[this.advancedPricingRules
				.size()];
		this.advancedPricingRules.toArray(apr);
		return apr;
	}

	public CurrencyIfc getCompareAtPrice() {
		CurrencyIfc comparePrice = null;

		comparePrice = this.item.getCompareAtPrice();

		if (comparePrice == null) {
			comparePrice = DomainGateway.getBaseCurrencyInstance();
		}

		return comparePrice;
	}

	public boolean getDamageDiscountEligible() {
		return this.item.getItemClassification().isDamageDiscountEligible();
	}

	public DepartmentIfc getDepartment() {
		return this.item.getDepartment();
	}

	public String getDepartmentID() {
		String id = null;
		if ((this.item != null) && (this.item.getDepartment() != null)) {
			id = this.item.getDepartment().getDepartmentID();
		}
		return id;
	}

	public String getDescription(Locale locale) {
		return this.item.getDescription(locale);
	}

	public LocalizedTextIfc getLocalizedDescriptions() {
		return this.item.getLocalizedDescriptions();
	}

	public String getDescription() {
		return getDescription(LocaleMap.getLocale("locale_Default"));
	}

	public String getDescriptionForCustomerLocale() {
		return getDescription(LocaleMap.getLocale("locale_Default"));
	}

	public String getDescriptionForDefaultLocale() {
		return getDescription(LocaleMap.getLocale("locale_Default"));
	}

	public boolean getDiscountEligible() {
		return this.item.getItemClassification().isDiscountEligible();
	}

	public boolean getEmployeeDiscountEligible() {
		return this.item.getItemClassification()
				.getEmployeeDiscountAllowedFlag();
	}

	public ItemIfc getItem() {
		return this.item;
	}

	public ItemClassificationIfc getItemClassification() {
		return this.item.getItemClassification();
	}

	public CurrencyIfc getItemCost() {
		return this.item.getItemCost();
	}

	public String getItemID() {
		return this.item.getItemID();
	}

	public BigDecimal getItemWeight() {
		return this.item.getItemWeight();
	}

	/** @deprecated */
	public String getManufacturer() {
		return getManufacturer(LocaleMap.getLocale("locale_Default"));
	}

	public String getManufacturer(Locale lcl) {
		Locale bestMatch = LocaleMap.getBestMatch(lcl);

		return this.item.getManufacturer(bestMatch);
	}

	/** @deprecated */
	public String getManufacturerForCustomerLocale() {
		return getManufacturer(LocaleMap.getLocale("locale_Receipts"));
	}

	/** @deprecated */
	public String getManufacturerForDefaultLocale() {
		return getManufacturer(LocaleMap.getLocale("locale_Default"));
	}

	public int getManufacturerID() {
		return this.manufacturerID;
	}

	public String getMerchandiseCodesString() {
		return getItemClassification().getMerchandiseCodesString();
	}

	public CurrencyIfc getPermanentPrice() {
		return getPermanentPrice(new EYSDate());
	}

	public CurrencyIfc getPermanentPrice(EYSDate when) {
		CurrencyIfc price = null;
		Iterator iter;
		if (hasPermanentPriceChanges()) {
			for (iter = priceChangesPermanent(); iter.hasNext();) {
				PriceChangeIfc priceChange = (PriceChangeIfc) iter.next();
				if (priceChange.isInEffect(when)) {
					price = priceChange.getNewPrice();
					break;
				}
			}
		}
		return ((price != null) ? price : this.item.getPermanentPrice());
	}

	public String[] getPlanogramID() {
		return this.item.getPlanogramID();
	}

	public String getPosItemID() {
		return this.item.getPosItemID();
	}

	public CurrencyIfc getPrice() {
		return getPrice(new EYSDate(), -1);
	}

	public CurrencyIfc getPrice(EYSDate when, int pricingGroupID) {
		CurrencyIfc price = null;
		PriceChangeIfc priceChange = getEffectiveTemporaryPriceChange(when,
				pricingGroupID);
		if (priceChange != null) {
			price = priceChange.getNewPrice();
		}
		if (price == null) {
			price = getPermanentPrice();
		}
		return ((price != null) ? price : this.item.getSellingPrice());
	}

	public CurrencyIfc getReturnPrice(int pricingGroupID) {
		CurrencyIfc price = null;
		CurrencyIfc lpp = getLowestPermanentPrice();
		CurrencyIfc ltp = getLowestTemporaryPrice(pricingGroupID);

		if ((ltp == null) && (lpp == null)) {
			price = this.item.getSellingPrice();
		} else if (ltp == null) {
			price = lpp;
		} else if (lpp == null) {
			price = ltp;
		} else if (ltp.compareTo(lpp) < 0) {
			price = ltp;
		} else {
			price = lpp;
		}

		return price;
	}

	protected CurrencyIfc getLowestPermanentPrice() {
		CurrencyIfc lowestPrice = null;
		EYSDate[] endDates = getPermanentPriceEndDates();
		int index = 0;

		for (PriceChangeIfc priceChange : this.priceChangesPermanent) {
			if (endDates[index].compareTo(getReturnPriceCutoffDate()) >= 0) {
				CurrencyIfc newPrice = priceChange.getNewPrice();
				if (lowestPrice == null) {
					lowestPrice = newPrice;
				} else if (newPrice.compareTo(lowestPrice) < 0) {
					lowestPrice = newPrice;
				}
			}
			++index;
		}

		return lowestPrice;
	}

	protected CurrencyIfc getLowestTemporaryPrice(int pricingGroupID) {
		CurrencyIfc lowestPrice = null;
		EYSDate[] endDates = getPermanentPriceEndDates();

		if (!(this.priceChangesTemporaryForReturns.isEmpty())) {
			PriceChangeIfc[] expanded = getExpandedPriceChangesTemporaryForReturns(endDates);
			for (PriceChangeIfc priceChange : expanded) {
				CurrencyIfc price = priceChange.getNewPrice();
				if (priceChange.getPricingGroupID() < 0) {
					if ((lowestPrice != null)
							&& (price.compareTo(lowestPrice) >= 0))
						continue;
					lowestPrice = price;
				} else {
					if ((priceChange.getPricingGroupID() != pricingGroupID)
							|| ((lowestPrice != null) && (price
									.compareTo(lowestPrice) >= 0)))
						continue;
					lowestPrice = price;
				}
			}

		}

		return lowestPrice;
	}

	protected PriceChangeIfc[] getExpandedPriceChangesTemporaryForReturns(
			EYSDate[] endDates) {
		ArrayList ExpandedPriceChanges = new ArrayList();

		for (PriceChangeIfc priceChange : this.priceChangesTemporaryForReturns) {
			ExpandedPriceChanges.addAll(getAssociatedPermanentPrices(
					priceChange, endDates));
		}
		PriceChangeIfc[] expanded = new PriceChangeIfc[ExpandedPriceChanges
				.size()];
		return ((PriceChangeIfc[]) ExpandedPriceChanges.toArray(expanded));
	}

	protected Collection<PriceChangeIfc> getAssociatedPermanentPrices(
			PriceChangeIfc priceChange, EYSDate[] endDates) {
		ArrayList expandedPriceChanges = new ArrayList();
		for (int i = 0; i < endDates.length; ++i) {
			PriceChangeIfc permPriceChange = (PriceChangeIfc) this.priceChangesPermanent
					.get(i);
			if (!(isPermanentPriceValidForTemporaryPrice(priceChange,
					permPriceChange.getPriceMaintenanceEvent()
							.getEffectiveDateTimestamp(), endDates[i])))
				continue;
			PLUItemIfc dummy = DomainGateway.getFactory().getPLUItemInstance();
			dummy.addPermanentPriceChange(permPriceChange);
			PriceChangeIfc newPriceChange = (PriceChangeIfc) priceChange
					.clone();
			newPriceChange.setItem(dummy);
			expandedPriceChanges.add(newPriceChange);
		}

		return expandedPriceChanges;
	}

	private boolean isPermanentPriceValidForTemporaryPrice(
			PriceChangeIfc priceChange, EYSDate permStartDate,
			EYSDate permEndDate) {
		boolean isPriceValid = false;
		EYSDate tempEffDate = priceChange.getPriceMaintenanceEvent()
				.getEffectiveDateTimestamp();
		EYSDate tempExpDate = priceChange.getPriceMaintenanceEvent()
				.getExpirationDateTimestamp();

		if ((tempEffDate.compareTo(permStartDate) >= 0)
				&& (tempEffDate.compareTo(permEndDate) <= 0)) {
			isPriceValid = true;
		} else if ((tempExpDate.compareTo(permStartDate) >= 0)
				&& (tempExpDate.compareTo(permEndDate) <= 0)) {
			isPriceValid = true;
		}

		return isPriceValid;
	}

	private EYSDate[] getPermanentPriceEndDates() {
		EYSDate[] endDates = new EYSDate[this.priceChangesPermanent.size()];
		int index = 0;
		EYSDate lastDate = null;

		for (PriceChangeIfc priceChange : this.priceChangesPermanent) {
			if (index == 0) {
				EYSDate firstEndDate = new EYSDate();

				firstEndDate.add(1, 10000);
				endDates[index] = firstEndDate;
			} else {
				endDates[index] = lastDate;
			}
			lastDate = priceChange.getPriceMaintenanceEvent()
					.getEffectiveDateTimestamp();
			++index;
		}

		return endDates;
	}

	public PriceChangeIfc[] getPermanentPriceChanges() {
		PriceChangeIfc[] array = new PriceChangeIfc[this.priceChangesPermanent
				.size()];
		this.priceChangesPermanent.toArray(array);
		return array;
	}

	public PriceChangeIfc[] getTemporaryPriceChanges() {
		PriceChangeIfc[] array = new PriceChangeIfc[this.priceChangesTemporary
				.size()];
		this.priceChangesTemporary.toArray(array);
		return array;
	}

	public PriceChangeIfc getEffectiveTemporaryPriceChange() {
		return getEffectiveTemporaryPriceChange(new EYSDate());
	}

	public PriceChangeIfc getEffectiveTemporaryPriceChange(EYSDate when) {
		return getEffectiveTemporaryPriceChange(when, -1);
	}

	public PriceChangeIfc getEffectiveTemporaryPriceChange(EYSDate when,
			int pricingGroupID) {
		PriceChangeIfc effectivePriceChange = null;
		Iterator iter;
		if (hasTemporaryPriceChanges()) {
			for (iter = priceChangesTemporary(); iter.hasNext();) {
				PriceChangeIfc currentPriceChange = (PriceChangeIfc) iter
						.next();
				if (currentPriceChange.isInEffect(when)) {
					if ((effectivePriceChange == null)
							&& (currentPriceChange.getPricingGroupID() < 1)) {
						effectivePriceChange = currentPriceChange;
					}

					if ((pricingGroupID < 1)
							&& (currentPriceChange.getPricingGroupID() < 1)) {
						return effectivePriceChange;
					}
					if (currentPriceChange.getPricingGroupID() == pricingGroupID) {
						return currentPriceChange;
					}
				}
			}

		}

		return effectivePriceChange;
	}

	public int getPricingEventID() {
		return this.item.getPricingEventID();
	}

	public ProductIfc getProduct() {
		return this.item.getProduct();
	}

	public String getProductGroupID() {
		return this.item.getItemClassification().getGroup().getGroupID();
	}

	public RelatedItemContainerIfc getRelatedItemContainer() {
		return this.relatedItemContainer;
	}

	public int getRestrictiveAge() {
		return this.restrictiveAge;
	}

	public String getRevisionNumber() {
		return Util
				.parseRevisionNumber("$Revision: /rgbustores_13.4x_generic_branch/2 $");
	}

	public CurrencyIfc getSellingPrice() {
		CurrencyIfc sellingPrice = null;

		if (hasAdvancedPricingRules()) {
			ItemContainerProxyIfc proxy = DomainGateway.getFactory()
					.getItemContainerProxyInstance();

			sellingPrice = proxy.addPLUItem(this)
					.getExtendedDiscountedSellingPrice();
		} else {
			sellingPrice = getPrice();
		}
		return sellingPrice;
	}

	public String getShortDescription(Locale locale) {
		return this.item.getShortDescription(locale);
	}

	public LocalizedTextIfc getShortLocalizedDescriptions() {
		return this.item.getShortLocalizedDescriptions();
	}

	public String getShortDescription() {
		return getShortDescription(LocaleMap.getLocale("locale_Default"));
	}

	public StockItemIfc getStockItem() {
		StockItemIfc stockItem = DomainGateway.getFactory()
				.getStockItemInstance();
		stockItem.initialize(getItemID(), getLocalizedDescriptions(),
				(CurrencyIfc) getPrice().clone(), getTaxGroupID(),
				getTaxable(), getDepartmentID());

		return stockItem;
	}

	public String getStoreID() {
		return this.storeID;
	}

	public boolean getTaxable() {
		return this.item.getTaxable();
	}

	public int getTaxGroupID() {
		return this.item.getTaxGroupID();
	}

	public String getTaxGroupName() {
		return this.taxGroupName;
	}

	public NewTaxRuleIfc[] getTaxRules() {
		NewTaxRuleIfc[] tr = null;
		if ((this.taxRules != null) && (this.taxRules.size() > 0)) {
			tr = (NewTaxRuleIfc[]) this.taxRules
					.toArray(new NewTaxRuleIfc[this.taxRules.size()]);
		}
		return tr;
	}

	public UnitOfMeasureIfc getUnitOfMeasure() {
		return this.item.getUnitOfMeasure();
	}

	public boolean hasAdvancedPricingRules() {
		return (!(this.advancedPricingRules.isEmpty()));
	}

	public boolean hasMerchandiseClassifications() {
		return getItemClassification().hasMerchandiseClassifications();
	}

	public boolean hasPermanentPriceChanges() {
		return (!(this.priceChangesPermanent.isEmpty()));
	}

	public boolean hasTemporaryPriceChanges() {
		return (!(this.priceChangesTemporary.isEmpty()));
	}

	public boolean hasTaxRules() {
		if (this.taxRules == null) {
			this.taxRules = new ArrayList();
		}

		return (!(this.taxRules.isEmpty()));
	}

	public boolean hasRelatedItems() {
		return ((getRelatedItemContainer() != null) && (!(getRelatedItemContainer()
				.isEmpty())));
	}

	public void initialize(String id, LocalizedTextIfc desc, CurrencyIfc prc,
			int taxGroup, boolean taxFlag, String dept) {
		setItemID(id);
		setLocalizedDescriptions(desc);
		setPrice(prc);
		setTaxGroupID(taxGroup);
		setDepartmentID(dept);
		setTaxable(taxFlag);
	}

	/** @deprecated */
	public void initialize(String id, String desc, CurrencyIfc prc,
			int taxGroup, boolean taxFlag, String dept) {
		LocalizedTextIfc tempDesc = DomainGateway.getFactory()
				.getLocalizedText();
		tempDesc.initialize(LocaleMap.getSupportedLocales(), desc);
		initialize(id, tempDesc, prc, taxGroup, taxFlag, dept);
	}

	public boolean isAlterationItem() {
		return false;
	}

	public boolean isEmployeeDiscountEligible() {
		return getEmployeeDiscountEligible();
	}

	public boolean isDamageDiscountEligible() {
		return getDamageDiscountEligible();
	}

	public boolean isDiscountEligible() {
		return getDiscountEligible();
	}

	public boolean isItemSizeRequired() {
		return this.itemSizeRequired;
	}

	public boolean isKitComponent() {
		return this.item.isKitComponent();
	}

	public boolean isKitHeader() {
		return this.item.isKitHeader();
	}

	public boolean isSerializedItem() {
		return getItemClassification().isSerializedItem();
	}

	public boolean isSpecialOrderEligible() {
		return this.item.isSpecialOrderEligible();
	}

	public boolean isStoreCoupon() {
		return this.item.isStoreCoupon();
	}

	public Iterator<PriceChangeIfc> priceChangesPermanent() {
		return this.priceChangesPermanent.iterator();
	}

	public Iterator<PriceChangeIfc> priceChangesTemporary() {
		return this.priceChangesTemporary.iterator();
	}

	public void setAdvancedPricingRules(AdvancedPricingRuleIfc[] rules) {
		this.advancedPricingRules.clear();
		this.advancedPricingRules.addAll(Arrays.asList(rules));
	}

	public void setCloneAttributes(PLUItem newClass) {
		if (this.item != null) {
			newClass.setItem((ItemIfc) this.item.clone());
		}

		PriceChangeIfc priceChange = null;
		for (Iterator i = priceChangesTemporary(); i.hasNext();) {
			priceChange = (PriceChangeIfc) i.next();
			if (priceChange != null)
				;
			newClass.addTemporaryPriceChange((PriceChangeIfc) priceChange
					.clone());
		}
		for (Iterator i = this.priceChangesTemporaryForReturns.iterator(); i
				.hasNext();) {
			priceChange = (PriceChangeIfc) i.next();
			if (priceChange != null)
				;
			newClass.addTemporaryPriceChangeForReturns((PriceChangeIfc) priceChange
					.clone());
		}
		for (Iterator i = priceChangesPermanent(); i.hasNext();) {
			priceChange = (PriceChangeIfc) i.next();
			if (priceChange != null)
				;
			newClass.addPermanentPriceChange((PriceChangeIfc) priceChange
					.clone());
		}

		newClass.setTaxGroupName(getTaxGroupName());

		NewTaxRuleIfc txRule = null;
		for (Iterator i = taxRules(); i.hasNext();) {
			txRule = (NewTaxRuleIfc) i.next();
			newClass.addTaxRule((txRule == null) ? null
					: (NewTaxRuleIfc) txRule.clone());
		}
		newClass.setPosItemID(getPosItemID());

		AdvancedPricingRuleIfc rule = null;
		for (Iterator i = advancedPricingRules(); i.hasNext();) {
			rule = (AdvancedPricingRuleIfc) i.next();
			newClass.addAdvancedPricingRule((rule == null) ? null
					: (AdvancedPricingRuleIfc) rule.clone());
		}

		if (this.relatedItemContainer != null) {
			newClass.setRelatedItemContainer((RelatedItemContainerIfc) this.relatedItemContainer
					.clone());
		}

		if (this.returnPriceCutoffDate != null) {
			newClass.returnPriceCutoffDate = ((EYSDate) this.returnPriceCutoffDate
					.clone());
		}

		newClass.itemSizeRequired = this.itemSizeRequired;
		newClass.storeID = this.storeID;
		newClass.manuForCustomerLocale = this.manuForCustomerLocale;
		newClass.manuForDefaultLocale = this.manuForDefaultLocale;
		newClass.restrictiveAge = this.restrictiveAge;
		newClass.manufacturerID = this.manufacturerID;
		
		
			
		newClass.loyalityCpnAttrDtls = loyalityCpnAttrDtls;
		
		newClass.loyalityCpnHrchyDtlsList = loyalityCpnHrchyDtlsList;
		
		newClass.loyalityMerchHrchyDtls = loyalityMerchHrchyDtls;
		
		newClass.isLoylHrchyElligible = isLoylHrchyElligible;
		
		newClass.itemDivision = this.itemDivision;
		
		newClass.isDiscountConsidered = isDiscountConsidered;
		

		newClass.emplGrpId = this.emplGrpId;

		newClass.emplIdSrc = this.emplIdSrc;

		newClass.periodId = this.periodId;

		newClass.entitlementId = this.entitlementId;
		
		newClass.loyalityDiscountAppliedFlag = this.loyalityDiscountAppliedFlag;
		if (this.returnExternalOrderItem == null)
			return;
		newClass.setReturnExternalOrderItem((ExternalOrderItemIfc) this.returnExternalOrderItem
				.clone());
		
	
		
	}

	public void setCompareAtPrice(CurrencyIfc value) {
		this.item.setCompareAtPrice(value);
	}

	public void setDamageDiscountEligible(boolean value) {
		this.item.getItemClassification().setDamageDiscountEligible(value);
	}

	public void setDepartment(DepartmentIfc value) {
		this.item.setDepartment(value);
	}

	public void setDepartmentID(String value) {
		this.item.getDepartment().setDepartmentID(value);
	}

	public void setDescription(Locale locale, String value) {
		this.item.setDescription(locale, value);
	}

	public void setLocalizedDescriptions(LocalizedTextIfc value) {
		this.item.setLocalizedDescriptions(value);
	}

	public void setDescription(String value) {
		setDescription(LocaleMap.getLocale("locale_Default"), value);
	}

	public void setDescriptionForCustomerLocale(String value) {
		setDescription(LocaleMap.getLocale("locale_Default"), value);
	}

	public void setDescriptionForDefaultLocale(String value) {
		setDescription(LocaleMap.getLocale("locale_Default"), value);
	}

	public void setDiscountEligible(boolean value) {
		this.item.getItemClassification().setDiscountEligible(value);
	}

	public void setItem(ItemIfc value) {
		this.item = value;
	}

	public void setItemClassification(ItemClassificationIfc value) {
		this.item.setItemClassification(value);
	}

	public void setItemCost(CurrencyIfc value) {
		this.item.setItemCost(value);
	}

	public void setItemID(String value) {
		this.item.setItemID(value);
	}

	public void setItemSizeRequired(boolean required) {
		this.itemSizeRequired = required;
	}

	public void setItemWeight(BigDecimal value) {
		this.item.setItemWeight(value);
	}

	/** @deprecated */
	public void setManufacturer(String value) {
		setManufacturer(LocaleMap.getLocale("locale_Default"), value);
	}

	public void setManufacturer(Locale lcl, String value) {
		Locale bestMatch = LocaleMap.getBestMatch(lcl);
		this.item.setManufacturer(bestMatch, value);
	}

	/** @deprecated */
	public void setManufacturerForCustomerLocale(String value) {
		setManufacturer(LocaleMap.getLocale("locale_Receipts"), value);
	}

	/** @deprecated */
	public void setManufacturerForDefaultLocale(String value) {
		setManufacturer(LocaleMap.getLocale("locale_Default"), value);
	}

	public void setManufacturerID(int value) {
		this.manufacturerID = value;
	}

	public void setPermanentPrice(CurrencyIfc value) {
		this.item.setPermanentPrice(value);
	}

	public void setPlanogramID(String[] value) {
		this.item.setPlanogramID(value);
	}

	public void setPosItemID(String value) {
		this.item.setPosItemID(value);
	}

	public void setPrice(CurrencyIfc value) {
		setSellingPrice(value);
	}

	public void setPermanentPriceChanges(PriceChangeIfc[] changes) {
		this.priceChangesPermanent.clear();
		Arrays.sort(changes);
		this.priceChangesPermanent.addAll(Arrays.asList(changes));
	}

	public void setTemporaryPriceChanges(PriceChangeIfc[] changes) {
		this.priceChangesTemporary.clear();
		Arrays.sort(changes);
		this.priceChangesTemporary.addAll(Arrays.asList(changes));
	}

	public void setTemporaryPriceChangesAndTemporaryPriceChangesForReturns(
			PriceChangeIfc[] changes) {
		this.priceChangesTemporary.clear();
		this.priceChangesTemporaryForReturns.clear();
		EYSDate now = new EYSDate();
		EYSDate returnsDate = getReturnPriceCutoffDate();
		Arrays.sort(changes);

		for (PriceChangeIfc priceChange : changes) {
			if (priceChange.getPriceMaintenanceEvent()
					.getExpirationDateTimestamp().compareTo(now) >= 0) {
				this.priceChangesTemporary.add(priceChange);
			}

			if (priceChange.getPriceMaintenanceEvent()
					.getExpirationDateTimestamp().compareTo(returnsDate) < 0) {
				continue;
			}
			this.priceChangesTemporaryForReturns.add(priceChange);
		}
	}

	public void setPricingEventID(int value) {
		this.item.setPricingEventID(value);
	}

	public void setProduct(ProductIfc value) {
		this.item.setProduct(value);
	}

	public void setRelatedItemContainer(
			RelatedItemContainerIfc relatedItemContainer) {
		this.relatedItemContainer = relatedItemContainer;
	}

	public void setRestrictiveAge(int restrictiveAge) {
		this.restrictiveAge = restrictiveAge;
	}

	public void setSellingPrice(CurrencyIfc value) {
		this.item.setSellingPrice(value);

		PriceChangeIfc pc = DomainGateway.getFactory().getPriceChangeInstance();
		pc.setOverridePriceAmount(value);
		addTemporaryPriceChange(pc);
	}

	public void setShortDescription(Locale locale, String value) {
		this.item.setShortDescription(locale, value);
	}

	public void setShortLocalizedDescriptions(LocalizedTextIfc value) {
		this.item.setShortLocalizedDescriptions(value);
	}

	public void setShortDescription(String value) {
		setShortDescription(LocaleMap.getLocale("locale_Default"), value);
	}

	public void setStoreID(String storeID) {
		this.storeID = storeID;
	}

	public void setTaxable(boolean value) {
		this.item.setTaxable(value);
	}

	public void setTaxGroupID(int value) {
		this.item.setTaxGroupID(value);
	}

	public void setTaxGroupName(String name) {
		this.taxGroupName = name;
	}

	public void setTaxRules(NewTaxRuleIfc[] rules) {
		if (rules != null) {
			if (this.taxRules != null) {
				this.taxRules.clear();
			} else {
				this.taxRules = new ArrayList();
			}
			this.taxRules.addAll(Arrays.asList(rules));
		} else if (this.taxRules == null) {
			this.taxRules = new ArrayList();
		} else {
			this.taxRules.clear();
		}
	}

	public void setUnitOfMeasure(UnitOfMeasureIfc value) {
		this.item.setUnitOfMeasure(value);
	}

	public Iterator<NewTaxRuleIfc> taxRules() {
		if (this.taxRules == null) {
			this.taxRules = new ArrayList();
		}

		return this.taxRules.iterator();
	}

	public LocalizedTextIfc getLocalizedManufacturer() {
		return this.item.getLocalizedManufacturer();
	}

	public void setLocalizedManufacturer(LocalizedTextIfc localizedManufacturer) {
		this.item.setLocalizedManufacturer(localizedManufacturer);
	}

	public Map<String, List<MessageDTO>> getAllItemLevelMessages() {
		return this.item.getAllItemLevelMessages();
	}

	public List<MessageDTO> getAllItemMessagesInTransaction(
			String transactionName) {
		return this.item.getAllItemMessagesInTransaction(transactionName);
	}

	public ItemImageIfc getItemImage() {
		return this.item.getItemImage();
	}

	public String getItemLevelMessage(String transactionName,
			String msgTypName, Locale language) {
		return this.item.getItemLevelMessage(transactionName, msgTypName,
				language);
	}

	public String getItemLevelMessage(String transactionName, String msgTypName) {
		return this.item.getItemLevelMessage(transactionName, msgTypName);
	}

	public String getItemLevelMessageCodeID(String transactionType,
			String MessageType) {
		return this.item
				.getItemLevelMessageCodeID(transactionType, MessageType);
	}

	public void setAllItemLevelMessages(
			Map<String, List<MessageDTO>> itmMessageCollectionMap) {
		this.item.setAllItemLevelMessages(itmMessageCollectionMap);
	}

	public void setItemImage(ItemImageIfc itemImage) {
		this.item.setItemImage(itemImage);
	}

	public String toString() {
		StringBuffer strResult = new StringBuffer("Class:  PLUItem ");
		strResult.append("(Revision ").append(getRevisionNumber())
				.append(") @").append(super.hashCode()).append(Util.EOL)
				.append("\titemID:        [").append(getItemID()).append("]")
				.append(Util.EOL).append("\tiPosItemID:    [")
				.append(getPosItemID()).append("]").append(Util.EOL)
				.append("\tdescription:   [")
				.append(getDescription(LocaleMap.getLocale("locale_Default")))
				.append("]").append(Util.EOL).append("\tprice:         [")
				.append(getPrice()).append("]").append(Util.EOL)
				.append("\ttaxGroupID:    [").append(getTaxGroupID())
				.append("]").append(Util.EOL).append("\ttaxable:       [")
				.append(getTaxable()).append("]").append(Util.EOL)
				.append("\titemWeight:    [").append(getItemWeight())
				.append("]").append(Util.EOL);

		if (getItemClassification() == null) {
			strResult.append("\tclassification:[null]").append(Util.EOL);
		} else {
			strResult.append("\tclassification:").append(Util.EOL)
					.append(getItemClassification().toString());
		}

		if (getRelatedItemContainer() == null) {
			strResult.append("\tRelatedItemContainer:[null]").append(Util.EOL);
		} else {
			strResult.append("\tRelatedItemContainer:").append(Util.EOL);
			strResult.append(getRelatedItemContainer().toString());
		}
		strResult.append("\treturnPriceDays:[").append(this.returnPriceDays)
				.append("]").append(Util.EOL);

		if (this.returnPriceCutoffDate == null) {
			strResult.append("\treturnPriceCutoffDate:[null]").append(Util.EOL);
		} else {
			strResult.append("\treturnPriceCutoffDate:[")
					.append(this.returnPriceCutoffDate).append("]")
					.append(Util.EOL);
		}

		if (this.returnExternalOrderItem == null) {
			strResult.append("\returnExternalOrderItem:[null]")
					.append(Util.EOL);
		} else {
			strResult.append("\returnExternalOrderItem:[").append(Util.EOL);
			strResult.append(this.returnExternalOrderItem.toString());
		}

		return strResult.toString();
	}

	public int getReturnPriceDays() {
		return this.returnPriceDays;
	}

	public void setReturnPriceDays(int returnPriceDays) {
		this.returnPriceDays = returnPriceDays;
	}

	protected void retrieveReturnPriceDays() {
		try {
			String days = Gateway.getProperty("application", "ReturnPriceDays",
					"0");
			this.returnPriceDays = Integer.parseInt(days);
		} catch (Exception e) {
			this.returnPriceDays = 0;
			logger.error(
					"Failed to retrieve or convert to integer the 'ReturnPriceDays'.",
					e);
		}
	}

	protected EYSDate getReturnPriceCutoffDate() {
		if (this.returnPriceCutoffDate == null) {
			this.returnPriceCutoffDate = new EYSDate();
			this.returnPriceCutoffDate.add(6, this.returnPriceDays * -1);
			this.returnPriceCutoffDate.setHour(0);
			this.returnPriceCutoffDate.setMinute(0);
			this.returnPriceCutoffDate.setSecond(0);
			this.returnPriceCutoffDate.setMillisecond(0);
		}

		return this.returnPriceCutoffDate;
	}

	public ExternalOrderItemIfc getReturnExternalOrderItem() {
		return this.returnExternalOrderItem;
	}

	public void setReturnExternalOrderItem(
			ExternalOrderItemIfc returnExternalOrderItem) {
		this.returnExternalOrderItem = returnExternalOrderItem;
	}

	public String getManufacturerItemUPC() {
		return this.manufacturerItemUPC;
	}

	public void setManufacturerItemUPC(String manufacturerItemUPC) {
		this.manufacturerItemUPC = manufacturerItemUPC;
	}

	
	@Override
	public void setItemDivision(String itemDivision) {
		
		this.itemDivision=itemDivision;
		
	}
    
	@Override
	public String getItemDivision(){
		return itemDivision;
	}

	@Override
	public boolean isDiscountConsidered() {
		// TODO Auto-generated method stub
		return isDiscountConsidered;
	}

	@Override
	public void setDiscountConsidered(boolean isDiscountConsidered) {
		// TODO Auto-generated method stub
		this.isDiscountConsidered =isDiscountConsidered;
	}
}