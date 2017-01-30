/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  oracle.retail.stores.commerceservices.common.currency.CurrencyIfc
 *  oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc
 *  oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc
 *  oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc
 *  oracle.retail.stores.domain.tax.TaxEngineIfc
 *  oracle.retail.stores.domain.tax.TaxInformationContainerIfc
 *  oracle.retail.stores.domain.tender.TenderLineItemIfc
 *  oracle.retail.stores.domain.transaction.TransactionTaxIfc
 *  oracle.retail.stores.domain.utility.EYSDomainIfc
 */
package oracle.retail.stores.domain.transaction;

import java.math.BigDecimal;
import java.util.Vector;
import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc;
import oracle.retail.stores.domain.tax.TaxEngineIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;
import oracle.retail.stores.domain.utility.EYSDomainIfc;

public interface TransactionTotalsIfc
extends EYSDomainIfc {
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/4 $";
    public static final int UI_PRINT_TAX_DISPLAY_SCALE = 2;

    public void addSendPackage(SendPackageLineItemIfc var1);

    public CurrencyIfc adjustRounding(CurrencyIfc var1, int var2);

    public void calculateGrandTotal();

    public boolean equals(Object var1);

    public CurrencyIfc getAmountTender();

    public CurrencyIfc getAmountOffTotal();

    public CurrencyIfc getBalanceDue();

    public CurrencyIfc getCalculatedShippingCharge();

    public CurrencyIfc getDiscountEligibleSubtotal();

    public CurrencyIfc getDiscountTotal();

    public CurrencyIfc getExemptTaxTotal();

    public CurrencyIfc getGrandTotal();

    public CurrencyIfc getInclusiveTaxTotal();

    public CurrencyIfc getItemDiscountTotal();

    public int getItemSendPackagesCount();

    public CurrencyIfc getLayawayFee();

    public int getNumItems();

    public CurrencyIfc getPreTaxSubtotal();

    public BigDecimal getQuantitySale();

    public BigDecimal getQuantityTotal();

    public CurrencyIfc getRestockingFeeTotal();

    public CurrencyIfc getReturnDiscountTotal();

    public CurrencyIfc getReturnSubtotal();

    public CurrencyIfc getSaleDiscountTotal();

    public CurrencyIfc getSaleDiscountAndPromotionTotal();

    public CurrencyIfc getSaleSubtotal();

    public SendPackageLineItemIfc getSendPackage(int var1);

    public SendPackageLineItemIfc[] getSendPackages();

    public Vector<SendPackageLineItemIfc> getSendPackageVector();

    public CurrencyIfc getSubtotal();

    public TaxEngineIfc getTaxEngine();

    public CurrencyIfc getTaxExceptionsTotal();

    public TaxInformationContainerIfc getTaxInformationContainer();

    public CurrencyIfc getTaxTotal();

    public CurrencyIfc getTaxTotalUI();

    public CurrencyIfc getTransactionDiscountTotal();

    public CurrencyIfc getUISubtotalAsConfigured();

    public void initialize(CurrencyIfc var1, CurrencyIfc var2, CurrencyIfc var3, CurrencyIfc var4, CurrencyIfc var5, CurrencyIfc var6, CurrencyIfc var7, CurrencyIfc var8, CurrencyIfc var9, CurrencyIfc var10);

    public boolean isAllItemUOMUnits();

    public boolean isTransactionLevelSendAssigned();

    public SendPackageLineItemIfc removeSendPackage(int var1);

    public void setAllItemUOMUnits(boolean var1);

    public void setAmountTender(CurrencyIfc var1);

    public void setBalanceDue(CurrencyIfc var1);

    public CurrencyIfc getChangeDue();

    public void setChangeDue(CurrencyIfc var1);

    public void setDiscountEligibleSubtotal(CurrencyIfc var1);

    public void setDiscountTotal(CurrencyIfc var1);

    public void setExemptTaxTotal(CurrencyIfc var1);

    public void setGrandTotal(CurrencyIfc var1);

    public void setInclusiveTaxTotal(CurrencyIfc var1);

    public void setItemDiscountTotal(CurrencyIfc var1);

    public void setLayawayFee(CurrencyIfc var1);

    public void setNumItems(int var1);

    public void setQuantitySale(BigDecimal var1);

    public void setQuantityTotal(BigDecimal var1);

    public void setRestockingFeeTotal(CurrencyIfc var1);

    public void setReturnDiscountTotal(CurrencyIfc var1);

    public void setReturnSubtotal(CurrencyIfc var1);

    public void setSaleDiscountTotal(CurrencyIfc var1);

    public void setSaleDiscountAndPromotionTotal(CurrencyIfc var1);

    public void setSaleSubtotal(CurrencyIfc var1);

    public void setSubtotal(CurrencyIfc var1);

    public void setTaxEngine(TaxEngineIfc var1);

    public void setTaxExceptionsTotal(CurrencyIfc var1);

    public void setTaxInformationContainer(TaxInformationContainerIfc var1);

    public void setTaxTotal(CurrencyIfc var1);

    public void setTaxTotalUI(CurrencyIfc var1);

    public void setTransactionDiscountTotal(CurrencyIfc var1);

    public void setTransactionLevelSendAssigned(boolean var1);

    public void updateTenderTotals(TenderLineItemIfc[] var1);

    public void updateTransactionTotals(AbstractTransactionLineItemIfc[] var1, TransactionDiscountStrategyIfc[] var2, TransactionTaxIfc var3);

    public void updateTransactionTotalsForBillPayment(CurrencyIfc var1);

    public void updateTransactionTotalsForPayment(CurrencyIfc var1);
    
    
}
