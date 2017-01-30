package oracle.retail.stores.domain.arts;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.commerceservices.common.currency.CurrencyTypeIfc;
import oracle.retail.stores.common.sql.SQLDeleteStatement;
import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.common.sql.SQLUpdateStatement;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.discount.CustomerDiscountByPercentageIfc;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.DiscountRuleIfc;
import oracle.retail.stores.domain.discount.ItemDiscountByAmountIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.discount.ItemTransactionDiscountAuditIfc;
import oracle.retail.stores.domain.discount.PromotionLineItemIfc;
import oracle.retail.stores.domain.discount.ReturnItemTransactionDiscountAuditIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountByPercentageIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.financial.PaymentIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.ItemContainerProxyIfc;
import oracle.retail.stores.domain.lineitem.ItemPriceIfc;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.lineitem.KitComponentLineItemIfc;
import oracle.retail.stores.domain.lineitem.OrderItemStatusIfc;
import oracle.retail.stores.domain.lineitem.OrderLineItemIfc;
import oracle.retail.stores.domain.lineitem.ReturnItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.order.OrderDeliveryDetailIfc;
import oracle.retail.stores.domain.order.OrderRecipientIfc;
import oracle.retail.stores.domain.order.OrderStatusIfc;
import oracle.retail.stores.domain.registry.RegistryIDIfc;
import oracle.retail.stores.domain.returns.ReturnTenderDataElementIfc;
import oracle.retail.stores.domain.stock.AlterationPLUItemIfc;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.stock.GiftCertificateItemIfc;
import oracle.retail.stores.domain.stock.ItemClassificationIfc;
import oracle.retail.stores.domain.stock.ItemIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.stock.UnitOfMeasureIfc;
import oracle.retail.stores.domain.stock.UnknownItemIfc;
import oracle.retail.stores.domain.store.StoreIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;
import oracle.retail.stores.domain.transaction.LayawayTransactionIfc;
import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.PaymentTransactionIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionIDIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;
import oracle.retail.stores.domain.transaction.TransactionTotalsIfc;
import oracle.retail.stores.domain.utility.AlterationIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.EYSStatusIfc;
import oracle.retail.stores.domain.utility.EntryMethod;
import oracle.retail.stores.domain.utility.GiftCardIfc;
import oracle.retail.stores.domain.utility.SecurityOverrideIfc;
import oracle.retail.stores.foundation.factory.FoundationObjectFactory;
import oracle.retail.stores.foundation.factory.FoundationObjectFactoryIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.device.EncipheredCardDataIfc;
import oracle.retail.stores.foundation.manager.device.EncipheredDataIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.utility.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class JdbcSaveRetailTransactionLineItems
  extends JdbcSaveRetailTransaction
  implements DiscountRuleConstantsIfc
{
  private static final long serialVersionUID = -3385250416729096336L;
  private static final Logger logger = Logger.getLogger(JdbcSaveRetailTransactionLineItems.class);
  protected static final String TYPE_SALE_RETURN = "SR";
  protected static final String TYPE_DISCOUNT = "DS";
  protected static final String TYPE_TAX = "TX";
  protected static final String TYPE_HOUSE_PAYMENT = "HP";
  protected static final String TYPE_ORDER = "OR";
  protected static final String EXTERNAL_ORDER_ITEM_ID_TO_BE_FILLED_IN = "-1";
  
  public JdbcSaveRetailTransactionLineItems()
  {
    setName("JdbcSaveRetailTransactionLineItems");
  }
  
  public void execute(DataTransactionIfc dataTransaction, DataConnectionIfc dataConnection, DataActionIfc action)
    throws DataException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcSaveRetailTransactionLineItems.execute()");
    }
    JdbcDataConnection connection = (JdbcDataConnection)dataConnection;
    
    ARTSTransaction artsTransaction = (ARTSTransaction)action.getDataObject();
    TenderableTransactionIfc transaction = (TenderableTransactionIfc)artsTransaction.getPosTransaction();
    saveRetailTransactionLineItems(connection, transaction);
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcSaveRetailTransactionLineItems.execute()");
    }
  }
  
  public void saveRetailTransactionLineItems(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction)
    throws DataException
  {
    if (((transaction instanceof PaymentTransactionIfc)) && (!(transaction instanceof LayawayTransactionIfc)))
    {
      try
      {
        savePaymentLineItem(dataConnection, transaction);
      }
      catch (DataException de)
      {
        throw de;
      }
    }
    else
    {
      if ((transaction instanceof RetailTransactionIfc))
      {
        RetailTransactionIfc rt = (RetailTransactionIfc)transaction;
        int lineItemSequenceNumber = rt.getLineItems().length;
        saveSaleReturnLineItems(dataConnection, rt);
        saveTaxLineItem(dataConnection, rt, lineItemSequenceNumber);
        saveDiscountLineItems(dataConnection, rt, ++lineItemSequenceNumber);
        if ((transaction instanceof SaleReturnTransactionIfc)) {
          saveReturnTendersData(dataConnection, (SaleReturnTransactionIfc)transaction);
        }
      }
      if ((transaction instanceof OrderTransactionIfc)) {
        saveOrderLineItems(dataConnection, (OrderTransactionIfc)transaction);
      }
    }
  }
  
  public void savePaymentLineItem(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction)
    throws DataException
  {
    try
    {
      insertPaymentLineItem(dataConnection, (PaymentTransactionIfc)transaction);
    }
    catch (DataException e)
    {
      updatePaymentLineItem(dataConnection, (PaymentTransactionIfc)transaction);
    }
  }
  
  public void saveSaleReturnLineItems(JdbcDataConnection dataConnection, RetailTransactionIfc transaction)
    throws DataException
  {
    AbstractTransactionLineItemIfc[] lineItems = transaction.getLineItems();
    
    ((SaleReturnTransactionIfc)transaction).addItemByTaxGroup();
    Vector v = ((SaleReturnTransactionIfc)transaction).getItemContainerProxy().getLineItemsVector();
    v.copyInto(lineItems);
    
    int numItems = 0;
    int maxItemSequenceNumber = 0;
    SaleReturnLineItemIfc srli = null;
    if (lineItems != null) {
      numItems = lineItems.length;
    }
    for (int i = 0; i < numItems; i++)
    {
      SaleReturnLineItemIfc lineItem = (SaleReturnLineItemIfc)lineItems[i];
      if ((lineItem.getPLUItem() instanceof GiftCardPLUItemIfc)) {
        insertGiftCard(dataConnection, transaction, lineItem);
      } else if (lineItem.isAlterationItem()) {
        saveAlteration(dataConnection, transaction, lineItem);
      }
      try
      {
        insertSaleReturnLineItem(dataConnection, transaction, lineItem);
      }
      catch (DataException e)
      {
        updateSaleReturnLineItem(dataConnection, transaction, lineItem);
      }
    }
    Vector deletedLineItems = ((SaleReturnTransactionIfc)transaction).getDeletedLineItems();
    if (deletedLineItems != null) {
      if (deletedLineItems.size() > 0)
      {
        if (transaction.getTransactionStatus() != 4) {
          maxItemSequenceNumber = numItems + 1;
        }
        if (transaction.getTransactionStatus() == 4) {
          maxItemSequenceNumber = numItems + 1;
        }
        if (transaction.containsOrderLineItems()) {
          maxItemSequenceNumber++;
        }
        if (transaction.getTransactionDiscounts() != null) {
          maxItemSequenceNumber++;
        }
        maxItemSequenceNumber += transaction.getTenderLineItemsSize();
        if ((transaction.getTransactionStatus() == 2) && (transaction.getTenderTransactionTotals().getBalanceDue().signum() != 0)) {
          maxItemSequenceNumber++;
        }
        if (transaction.getTransactionDiscounts() != null) {
          if ((deletedLineItems.size() > 0) && (transaction.getTransactionDiscounts().length > 1)) {
            maxItemSequenceNumber += transaction.getTransactionDiscounts().length - 1;
          }
        }
        if ((transaction.getTransactionStatus() == 2) && (transaction.getTenderTransactionTotals().getChangeDue().signum() != 0)) {
          maxItemSequenceNumber++;
        }
        for (int i = 0; i < deletedLineItems.size(); i++)
        {
          insertRetailTransactionLineItem(dataConnection, transaction, maxItemSequenceNumber, "SR", "1");
          
          srli = (SaleReturnLineItemIfc)deletedLineItems.elementAt(i);
          srli.setLineNumber(maxItemSequenceNumber);
          insertDeletedSaleReturnLineItem(dataConnection, transaction, srli);
          
          maxItemSequenceNumber += 1;
        }
      }
    }
  }
  
  public void saveReturnTendersData(JdbcDataConnection dataConnection, SaleReturnTransactionIfc transaction)
    throws DataException
  {
    ReturnTenderDataElementIfc[] returnTenders = transaction.getReturnTenderElements();
    int numItems = 0;
    if (returnTenders != null) {
      numItems = returnTenders.length;
    }
    for (int i = 0; i < numItems; i++) {
      try
      {
        insertReturnTendersData(dataConnection, transaction, returnTenders[i]);
      }
      catch (DataException e)
      {
        updateReturnTendersData(dataConnection, transaction, returnTenders[i]);
      }
    }
  }
  
  public void saveUnknownItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    try
    {
      insertUnknownItem(dataConnection, transaction, lineItem);
    }
    catch (DataException e)
    {
      updateUnknownItem(dataConnection, transaction, lineItem);
    }
  }
  
  public void saveCommissionModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    try
    {
      insertCommissionModifier(dataConnection, transaction, lineItem);
    }
    catch (DataException e)
    {
      updateCommissionModifier(dataConnection, transaction, lineItem);
    }
  }
  
  public void saveRetailPriceModifiers(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    int discountSequenceNumber = 0;
    if (lineItem.getItemPrice().isPriceOverride())
    {
      try
      {
        insertRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, null);
      }
      catch (DataException e)
      {
        updateRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, null);
      }
      discountSequenceNumber++;
    }
    ItemDiscountStrategyIfc[] modifiers = lineItem.getItemPrice().getItemDiscounts();
    
    int numDiscounts = 0;
    if (modifiers != null) {
      numDiscounts = modifiers.length;
    }
    for (int i = 0; i < numDiscounts; i++)
    {
      ItemDiscountStrategyIfc discountLineItem = modifiers[i];
      
      boolean saveRetailPriceModifier = false;
      if (discountLineItem.getDiscountScope() == 0)
      {
        if (lineItem.isReturnLineItem()) {
          saveRetailPriceModifier = true;
        }
      }
      else {
        saveRetailPriceModifier = true;
      }
      if (saveRetailPriceModifier) {
        try
        {
          insertRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, discountLineItem);
        }
        catch (DataException e)
        {
          updateRetailPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, discountLineItem);
        }
      } else {
        try
        {
          insertSaleReturnPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, discountLineItem);
        }
        catch (DataException e)
        {
          updateSaleReturnPriceModifier(dataConnection, transaction, lineItem, discountSequenceNumber, discountLineItem);
        }
      }
      discountSequenceNumber++;
    }
    PromotionLineItemIfc[] promotionLineItems = lineItem.getItemPrice().getPromotionLineItems();
    if ((promotionLineItems != null) && (promotionLineItems.length > 0) && (!(lineItem.getPLUItem() instanceof UnknownItemIfc)) && (!lineItem.getPLUItem().getItemClassification().isPriceEntryRequired())) {
      for (int sequenceNumber = 0; sequenceNumber < promotionLineItems.length; sequenceNumber++)
      {
        PromotionLineItemIfc promotionLineItem = promotionLineItems[sequenceNumber];
        try
        {
          insertPromotionLineItem(dataConnection, transaction, lineItem, promotionLineItem, sequenceNumber);
        }
        catch (DataException e)
        {
          updatePromotionLineItem(dataConnection, transaction, lineItem, promotionLineItem);
        }
      }
    }
  }
  
  public void saveSaleReturnTaxModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    ItemTaxIfc tax = lineItem.getItemPrice().getItemTax();
    if ((tax.getTaxMode() == 2) || (tax.getTaxMode() == 3) || (tax.getTaxMode() == 5) || (tax.getTaxMode() == 4)) {
      try
      {
        insertSaleReturnTaxModifier(dataConnection, transaction, lineItem, 0, tax);
      }
      catch (DataException e)
      {
        updateSaleReturnTaxModifier(dataConnection, transaction, lineItem, 0, tax);
      }
    }
  }
  
  public void saveTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber)
    throws DataException
  {
    try
    {
      insertTaxLineItem(dataConnection, transaction, lineItemSequenceNumber);
    }
    catch (DataException e)
    {
      updateTaxLineItem(dataConnection, transaction, lineItemSequenceNumber);
    }
    if (transaction.getTransactionTax().getTaxMode() == 1) {
      try
      {
        insertTaxExemptionModifier(dataConnection, transaction, lineItemSequenceNumber);
      }
      catch (DataException e)
      {
        updateTaxExemptionModifier(dataConnection, transaction, lineItemSequenceNumber);
      }
    }
  }
  
  public void saveDiscountLineItems(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber)
    throws DataException
  {
    if (((transaction instanceof SaleReturnTransactionIfc)) && (((SaleReturnTransactionIfc)transaction).hasDiscountableItems()))
    {
      TransactionDiscountStrategyIfc[] discountLineItems = transaction.getTransactionDiscounts();
      int numDiscounts = 0;
      if (discountLineItems != null) {
        numDiscounts = discountLineItems.length;
      }
      for (int i = 0; i < numDiscounts; i++)
      {
        TransactionDiscountStrategyIfc lineItem = discountLineItems[i];
        try
        {
          insertDiscountLineItem(dataConnection, transaction, lineItemSequenceNumber, lineItem);
        }
        catch (DataException e)
        {
          updateDiscountLineItem(dataConnection, transaction, lineItemSequenceNumber, lineItem);
        }
        lineItemSequenceNumber++;
      }
    }
  }
  
  public void saveOrderLineItems(JdbcDataConnection dataConnection, OrderTransactionIfc orderTransaction)
    throws DataException
  {
    AbstractTransactionLineItemIfc[] lineItems = orderTransaction.getLineItems();
    int numItems = 0;
    if (lineItems != null) {
      numItems = lineItems.length;
    }
    for (int i = 0; i < numItems; i++)
    {
      SaleReturnLineItemIfc lineItem = (SaleReturnLineItemIfc)lineItems[i];
      if (orderTransaction.getTransactionType() != 23) {
        updateOrderLineItem(dataConnection, orderTransaction, lineItem, i);
      } else {
        insertOrderLineItem(dataConnection, orderTransaction, lineItem, i);
      }
    }
  }
  
  public void updateRetailTransactionLineItem(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction, int lineItemSequenceNumber, String lineItemType)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_RTL_TRN");
    
    sql.addColumn("TY_LN_ITM", getLineItemType(lineItemType));
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getSequenceNumber(lineItemSequenceNumber));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateRetailTransactionLineItem", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update RetailTransactionLineItem");
    }
  }
  
  public void updatePaymentLineItem(JdbcDataConnection dataConnection, PaymentTransactionIfc transaction)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_PYAN");
    
    PaymentIfc payment = transaction.getPayment();
    if (payment != null) {
      if (payment.getEncipheredCardData() != null)
      {
        sql.addColumn("ID_NCRPT_ACNT_CRD", inQuotes(payment.getEncipheredCardData().getEncryptedAcctNumber()));
        sql.addColumn("ID_MSK_ACNT_CRD", inQuotes(payment.getEncipheredCardData().getMaskedAcctNumber()));
      }
      else
      {
        sql.addColumn("ID_ACNT_NMB", inQuotes(payment.getReferenceNumber()));
      }
    }
    sql.addColumn("LU_ACNT_PYMAGT_RCV", inQuotes(transaction.getPayment().getPaymentAccountType()));
    sql.addColumn("MO_PYM_AGT_RCV", transaction.getPaymentAmount().getStringValue());
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateHousePaymentLineItem", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update HousePaymentLineItem");
    }
  }
  
  public void updateSaleReturnLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    updateSaleReturnLineItem(dataConnection, transaction, lineItem, "SR");
  }
  
  public void updateSaleReturnLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, String lineItemTypeCode)
    throws DataException
  {
    updateRetailTransactionLineItem(dataConnection, transaction, lineItem.getLineNumber(), lineItemTypeCode);
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_SLS_RTN");
    
    sql.addColumn("ID_REGISTRY", getGiftRegistryString(lineItem));
    sql.addColumn("ID_ITM", getItemID(lineItem));
    sql.addColumn("ID_ITM_POS", inQuotes(lineItem.getPosItemID()));
    sql.addColumn("ID_NMB_SRZ", getItemSerial(lineItem));
    sql.addColumn("ID_GP_TX", getTaxGroupID(lineItem));
    sql.addColumn("QU_ITM_LM_RTN_SLS", getItemQuantity(lineItem));
    sql.addColumn("MO_EXTN_LN_ITM_RTN", getItemExtendedAmount(lineItem));
    sql.addColumn("MO_VAT_LN_ITM_RTN", lineItem.getItemTaxAmount().getStringValue());
    sql.addColumn("MO_TAX_INC_LN_ITM_RTN", lineItem.getItemInclusiveTaxAmount().getStringValue());
    
    sql.addColumn("FL_RTN_MR", getReturnFlag(lineItem));
    sql.addColumn("RC_RTN_MR", getReturnReasonCode(lineItem));
    sql.addColumn("ID_TRN_ORG", getOriginalTransactionId(lineItem));
    sql.addColumn("DC_DY_BSN_ORG", getOriginalDate(lineItem));
    sql.addColumn("AI_LN_ITM_ORG", getOriginalLineNumber(lineItem));
    sql.addColumn("ID_STR_RT_ORG", getOriginalStoreID(lineItem));
    sql.addColumn("ID_DPT_POS", getDepartmentID(lineItem));
    sql.addColumn("FL_SND", getSendFlag(lineItem));
    sql.addColumn("CNT_SND_LAB", getSendLabelCount(lineItem));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("FL_RCV_GF", getGiftReceiptFlag(lineItem));
    sql.addColumn("OR_ID_REF", lineItem.getOrderLineReference());
    sql.addColumn("LU_MTH_ID_ENR", inQuotes(lineItem.getEntryMethod().getIxRetailCode()));
    sql.addColumn("FL_RLTD_ITM_RTN", getReturnRelatedItemFlag(lineItem));
    sql.addColumn("AI_LN_ITM_RLTD", getRelatedSeqNum(lineItem));
    sql.addColumn("FL_RLTD_ITM_RM", getRemoveRelatedItemFlag(lineItem));
    sql.addColumn("FL_SLS_ASSC_MDF", getSalesAssociateModifiedFlag(lineItem));
    sql.addColumn("MO_PRN_PRC", getPermanentSellingPrice(lineItem));
    sql.addColumn("DE_ITM_SHRT_RCPT", getReceiptDescription(lineItem));
    sql.addColumn("DE_ITM_LCL", getReceiptDescriptionLocal(lineItem));
    sql.addColumn("FL_FE_RSTK", getRestockingFeeFlag(lineItem));
    sql.addColumn("LU_HRC_MR_LV", getProductGroupID(lineItem));
    sql.addColumn("FL_ITM_SZ_REQ", getSizeRequiredFlag(lineItem));
    sql.addColumn("LU_UOM_SLS", getLineItemUOMCode(lineItem));
    sql.addColumn("ID_DPT_POS", getPosDepartmentID(lineItem));
    sql.addColumn("TY_ITM", getItemTypeID(lineItem));
    sql.addColumn("FL_RTN_PRH", getReturnProhibited(lineItem));
    sql.addColumn("FL_DSC_EM_ALW", getEmployeeDiscountAllowed(lineItem));
    sql.addColumn("FL_TX", getTaxable(lineItem));
    sql.addColumn("FL_ITM_DSC", getDiscountable(lineItem));
    sql.addColumn("FL_ITM_DSC_DMG", getDamageDiscountable(lineItem));
    sql.addColumn("ID_MRHRC_GP", getMerchandiseHierarchyGroupID(lineItem));
    sql.addColumn("ID_ITM_MF_UPC", getManufacturerItemUPC(lineItem));
    
    String extendedRestockingFee = getItemExtendedRestockingFee(lineItem);
    if (extendedRestockingFee != null) {
      sql.addColumn("MO_FE_RSTK", extendedRestockingFee);
    }
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    if (lineItem.isPriceAdjustmentLineItem())
    {
      sql.addColumn("FL_ITM_PRC_ADJ", makeCharFromBoolean(true));
      sql.addColumn("LU_PRC_ADJ_RFN_ID", lineItem.getPriceAdjustmentReference());
    }
    else if (lineItem.isPartOfPriceAdjustment())
    {
      sql.addColumn("LU_PRC_ADJ_RFN_ID", lineItem.getPriceAdjustmentReference());
    }
    if ((transaction instanceof SaleReturnTransactionIfc)) {
      if (transaction.getTransactionStatus() == 4)
      {
        ReturnItemIfc theReturnItem = lineItem.getReturnItem();
        if (theReturnItem != null)
        {
          boolean wasRetrieved = theReturnItem.isFromRetrievedTransaction();
          if (wasRetrieved) {
            sql.addColumn("FL_RTRVD_TRN", "'1'");
          } else {
            sql.addColumn("FL_RTRVD_TRN", "'0'");
          }
        }
      }
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateSaleReturnLineItem", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update SaleReturnLineItem");
    }
    saveRetailPriceModifiers(dataConnection, transaction, lineItem);
    saveSaleReturnTaxModifier(dataConnection, transaction, lineItem);
    saveExternalOrderLineItem(dataConnection, transaction, lineItem);
    
    String employee = transaction.getSalesAssociate().getEmployeeID();
    if ((lineItem.getSalesAssociate() != null) && (!employee.equals(lineItem.getSalesAssociate().getEmployeeID()))) {
      saveCommissionModifier(dataConnection, transaction, lineItem);
    }
  }
  
  private String getDamageDiscountable(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.isDamageDiscountEligible()) {
      value = "'1'";
    }
    return value;
  }
  
  private String getDiscountable(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.isDiscountEligible()) {
      value = "'1'";
    }
    return value;
  }
  
  public void updateUnknownItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("AS_ITM_UNK");
    
    sql.addColumn("ID_ITM_POS", getItemID(lineItem));
    sql.addColumn("RP_SLS_POS_CRT", getItemPrice(lineItem));
    sql.addColumn("DE_ITM", getItemDescription(lineItem));
    sql.addColumn("ID_GP_TX", getTaxGroupID(lineItem));
    sql.addColumn("LU_EXM_TX", inQuotes(getItemTaxable(lineItem)));
    if (!Util.isEmpty(lineItem.getPLUItem().getDepartmentID())) {
      sql.addColumn("ID_DPT_POS", inQuotes(lineItem.getPLUItem().getDepartmentID()));
    }
    sql.addColumn("LU_UOM", getUOMCode(lineItem));
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateUnknownItem", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update UnknownItem");
    }
  }
  
  public void updateCommissionModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("CO_MDFR_CMN");
    
    sql.addColumn("ID_EM", getEmployeeID(lineItem));
    sql.addColumn("FL_PE_CMN_AMT", "'P'");
    sql.addColumn("PE_MDFR_CMN", "100");
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_MDFR_CMN = " + getSequenceNumber(0));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateCommissionModifier", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update CommissionModifier");
    }
  }
  
  public void updateRetailPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("CO_MDFR_RTL_PRC");
    if (discountLineItem != null)
    {
      sql.addColumn("ID_RU_MDFR", getDiscountRuleID(discountLineItem));
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(discountLineItem));
      sql.addColumn("PE_MDFR_RT_PRC", getPriceModifierPercent(discountLineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(discountLineItem));
      sql.addColumn("CD_MTH_PRDV", getPriceModifierMethodCode(discountLineItem));
      sql.addColumn("CD_BAS_PRDV", getPriceModifierAssignmentBasis(discountLineItem));
      
      sql.addColumn("ID_DSC_EM", makeSafeString(discountLineItem.getDiscountEmployeeID()));
      
      sql.addColumn("FL_DSC_DMG", getPriceModifierDamageDiscountFlag(discountLineItem));
      
      sql.addColumn("FL_PCD_DL_ADVN_APLY", getIncludedInBestDealFlag(discountLineItem));
      sql.addColumn("FL_DSC_ADVN_PRC", getAdvancedPricingRuleFlag(discountLineItem));
      sql.addColumn("ID_DSC_REF", getPriceModifierReferenceID(discountLineItem));
      
      sql.addColumn("CO_ID_DSC_REF", getPriceModifierReferenceIDTypeCode(discountLineItem));
      
      sql.addColumn("CD_TY_PRDV", discountLineItem.getTypeCode());
      sql.addColumn("DP_LDG_STK_MDFR", inQuotes(discountLineItem.getAccountingMethod()));
      
      sql.addColumn("ID_PRM", discountLineItem.getPromotionId());
      sql.addColumn("ID_PRM_CMP", discountLineItem.getPromotionComponentId());
      sql.addColumn("ID_PRM_CMP_DTL", discountLineItem.getPromotionComponentDetailId());
      sql.addColumn("ID_PRCGP", discountLineItem.getPricingGroupID());
    }
    else
    {
      sql.addColumn("ID_RU_MDFR", "0");
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(lineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(lineItem));
      
      SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
      if (priceOverrideAuthorization != null)
      {
        sql.addColumn("ID_EM_AZN_OVRD", makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
        
        sql.addColumn("CD_MTH_ENR_OVRD", priceOverrideAuthorization.getEntryMethod().getIxRetailCode());
      }
    }
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_MDFR_RT_PRC = " + getSequenceNumber(sequenceNumber));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateRetailPriceModifier", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update RetailPriceModifier");
    }
  }
  
  public void updateSaleReturnTaxModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemTaxIfc taxLineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("CO_MDFR_SLS_RTN_TX");
    
    sql.addColumn("MO_TX_RTN_SLS", getItemTaxAmount(lineItem));
    sql.addColumn("TY_TX", getItemTaxMode(taxLineItem));
    sql.addColumn("PE_TX", getItemTaxPercent(taxLineItem));
    sql.addColumn("PE_TX_OVRD", getItemTaxOverridePercent(taxLineItem));
    sql.addColumn("MO_TX_OVRD", getItemTaxOverrideAmount(taxLineItem));
    sql.addColumn("RC_TX_EXM_RTN_SLS", getItemTaxReasonCode(taxLineItem));
    sql.addColumn("ID_SCP_TX", getTaxScope(taxLineItem));
    sql.addColumn("ID_MTHD_TX", getItemTaxMethod(taxLineItem));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_MDFR_TX = " + getSequenceNumber(sequenceNumber));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateSaleReturnTaxModifier", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update SaleReturnTaxModifier");
    }
  }
  
  public void updateTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber)
    throws DataException
  {
    updateRetailTransactionLineItem(dataConnection, transaction, lineItemSequenceNumber, "TX");
    
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_TX");
    
    sql.addColumn("MO_TX", getTaxAmount(transaction));
    sql.addColumn("MO_TX_INC", getInclusiveTaxAmount(transaction));
    sql.addColumn("TY_TX", getTaxMode(transaction));
    sql.addColumn("PE_TX", getTaxPercent(transaction));
    sql.addColumn("PE_TX_OVRD", getTaxOverridePercent(transaction));
    sql.addColumn("MO_TX_OVRD", getTaxOverrideAmount(transaction));
    sql.addColumn("RC_TX", getTaxReasonCode(transaction));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getSequenceNumber(lineItemSequenceNumber));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateTaxLineItem", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update TaxLineItem");
    }
  }
  
  public void updateTaxExemptionModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("CO_MDFR_TX_EXM");
    
    sql.addColumn("ID_NCRPT_CF_TX_EXM", getTaxExemptCertificateID(transaction));
    sql.addColumn("ID_MSK_CF_TX_EXM", getMaskedTaxExemptCertificateID(transaction));
    sql.addColumn("RC_EXM_TX", getTaxReasonCode(transaction));
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getSequenceNumber(lineItemSequenceNumber));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateTaxExemptionModifier", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update TaxExemptionModifier");
    }
  }
  
  public void updateDiscountLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber, TransactionDiscountStrategyIfc lineItem)
    throws DataException
  {
    updateRetailTransactionLineItem(dataConnection, transaction, lineItemSequenceNumber, "DS");
    
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_DSC");
    
    sql.addColumn("RC_DSC", getDiscountReasonCode(lineItem));
    sql.addColumn("TY_DSC", getDiscountType(lineItem));
    sql.addColumn("MO_DSC", getDiscountAmount(lineItem));
    sql.addColumn("PE_DSC", getDiscountPercent(lineItem));
    sql.addColumn("LU_BAS_ASGN", getDiscountAssignmentBasis(lineItem));
    sql.addColumn("ID_DSC_EM", makeSafeString(lineItem.getDiscountEmployeeID()));
    
    sql.addColumn("FL_DSC_ENA", getDiscountEnabled(lineItem));
    sql.addColumn("FL_DL_ADVN_APLY", getIncludedInBestDealFlag(lineItem));
    sql.addColumn("ID_RU_DSC", getDiscountRuleID(lineItem));
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getSequenceNumber(lineItemSequenceNumber));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    
    sql.addColumn("ID_DSC_REF", getDiscountReferenceID(lineItem));
    sql.addColumn("CO_ID_DSC_REF", getDiscountReferenceIDTypeCode(lineItem));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateDiscountLineItem", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update DiscountLineItem");
    }
  }
  
  public void insertRetailTransactionLineItem(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction, int lineItemSequenceNumber, String lineItemType)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_RTL_TRN");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItemSequenceNumber));
    sql.addColumn("TY_LN_ITM", getLineItemType(lineItemType));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertRetailTransactionLineItem", e);
    }
  }
  
  public void insertRetailTransactionLineItem(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction, int lineItemSequenceNumber, String lineItemType, String voidFlag)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_RTL_TRN");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItemSequenceNumber));
    sql.addColumn("TY_LN_ITM", getLineItemType(lineItemType));
    sql.addColumn("FL_VD_LN_ITM", makeSafeString(voidFlag));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertRetailTransactionLineItem", e);
    }
  }
  
  public void insertPaymentLineItem(JdbcDataConnection dataConnection, PaymentTransactionIfc transaction, String tableName)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable(tableName);
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    PaymentIfc payment = transaction.getPayment();
    if (payment != null) {
      if (payment.getEncipheredCardData() != null)
      {
        sql.addColumn("ID_NCRPT_ACNT_CRD", inQuotes(payment.getEncipheredCardData().getEncryptedAcctNumber()));
        sql.addColumn("ID_MSK_ACNT_CRD", inQuotes(payment.getEncipheredCardData().getMaskedAcctNumber()));
      }
      else
      {
        sql.addColumn("ID_ACNT_NMB", inQuotes(payment.getReferenceNumber()));
      }
    }
    sql.addColumn("LU_ACNT_PYMAGT_RCV", inQuotes(transaction.getPayment().getPaymentAccountType()));
    
    sql.addColumn("MO_PYM_AGT_RCV", transaction.getPaymentAmount().getStringValue());
    sql.addColumn("MO_BLNC_ACNT", transaction.getPayment().getBalanceDue().getStringValue());
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertPaymentLineItem", e);
    }
  }
  
  public void insertPaymentLineItem(JdbcDataConnection dataConnection, PaymentTransactionIfc transaction)
    throws DataException
  {
    insertPaymentLineItem(dataConnection, transaction, "TR_LTM_PYAN");
  }
  
  public void insertSaleReturnLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    insertSaleReturnLineItem(dataConnection, transaction, lineItem, "SR");
  }
  
  public void insertSaleReturnLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, String lineItemTypeCode)
    throws DataException
  {
    insertRetailTransactionLineItem(dataConnection, transaction, lineItem.getLineNumber(), lineItemTypeCode);
    if (lineItem.isPriceAdjustmentLineItem()) {
      return;
    }
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_SLS_RTN");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_REGISTRY", getGiftRegistryString(lineItem));
    sql.addColumn("ID_ITM", getItemID(lineItem));
    sql.addColumn("ID_ITM_POS", inQuotes(lineItem.getPosItemID()));
    sql.addColumn("ID_NMB_SRZ", getItemSerial(lineItem));
    sql.addColumn("ID_GP_TX", getTaxGroupID(lineItem));
    sql.addColumn("QU_ITM_LM_RTN_SLS", getItemQuantity(lineItem));
    sql.addColumn("MO_EXTN_LN_ITM_RTN", getItemExtendedAmount(lineItem));
    sql.addColumn("MO_EXTN_DSC_LN_ITM", getItemExtendedDiscountedAmount(lineItem));
    
    sql.addColumn("MO_VAT_LN_ITM_RTN", lineItem.getItemTaxAmount().getStringValue());
    sql.addColumn("MO_TAX_INC_LN_ITM_RTN", lineItem.getItemInclusiveTaxAmount().getStringValue());
    
    sql.addColumn("CNT_SND_LAB", getSendLabelCount(lineItem));
    sql.addColumn("FL_RTN_MR", getReturnFlag(lineItem));
    sql.addColumn("RC_RTN_MR", getReturnReasonCode(lineItem));
    sql.addColumn("ID_TRN_ORG", getOriginalTransactionId(lineItem));
    sql.addColumn("DC_DY_BSN_ORG", getOriginalDate(lineItem));
    sql.addColumn("AI_LN_ITM_ORG", getOriginalLineNumber(lineItem));
    sql.addColumn("ID_STR_RT_ORG", getOriginalStoreID(lineItem));
    sql.addColumn("ID_DPT_POS", getDepartmentID(lineItem));
    sql.addColumn("FL_SND", getSendFlag(lineItem));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("FL_RCV_GF", getGiftReceiptFlag(lineItem));
    sql.addColumn("OR_ID_REF", lineItem.getOrderLineReference());
    sql.addColumn("LU_MTH_ID_ENR", inQuotes(lineItem.getEntryMethod().getIxRetailCode()));
    sql.addColumn("ED_SZ", getItemSizeCode(lineItem));
    
    sql.addColumn("FL_RLTD_ITM_RTN", getReturnRelatedItemFlag(lineItem));
    sql.addColumn("AI_LN_ITM_RLTD", getRelatedSeqNum(lineItem));
    sql.addColumn("FL_RLTD_ITM_RM", getRemoveRelatedItemFlag(lineItem));
    sql.addColumn("FL_SLS_ASSC_MDF", getSalesAssociateModifiedFlag(lineItem));
    sql.addColumn("MO_PRN_PRC", getPermanentSellingPrice(lineItem));
    sql.addColumn("DE_ITM_SHRT_RCPT", getReceiptDescription(lineItem));
    sql.addColumn("DE_ITM_LCL", getReceiptDescriptionLocal(lineItem));
    sql.addColumn("FL_FE_RSTK", getRestockingFeeFlag(lineItem));
    sql.addColumn("LU_HRC_MR_LV", getProductGroupID(lineItem));
    sql.addColumn("FL_ITM_SZ_REQ", getSizeRequiredFlag(lineItem));
    sql.addColumn("LU_UOM_SLS", getLineItemUOMCode(lineItem));
    sql.addColumn("ID_DPT_POS", getPosDepartmentID(lineItem));
    sql.addColumn("TY_ITM", getItemTypeID(lineItem));
    sql.addColumn("FL_RTN_PRH", getReturnProhibited(lineItem));
    sql.addColumn("FL_DSC_EM_ALW", getEmployeeDiscountAllowed(lineItem));
    sql.addColumn("FL_TX", getTaxable(lineItem));
    sql.addColumn("FL_ITM_DSC", getDiscountable(lineItem));
    sql.addColumn("FL_ITM_DSC_DMG", getDamageDiscountable(lineItem));
    sql.addColumn("ID_MRHRC_GP", getMerchandiseHierarchyGroupID(lineItem));
    sql.addColumn("ID_ITM_MF_UPC", getManufacturerItemUPC(lineItem));
    
    String extendedRestockingFee = getItemExtendedRestockingFee(lineItem);
    if (extendedRestockingFee != null) {
      sql.addColumn("MO_FE_RSTK", extendedRestockingFee);
    }
    if (lineItem.isKitHeader())
    {
      sql.addColumn("LU_KT_ST", inQuotes(1));
      sql.addColumn("ID_CLN", getItemID(lineItem));
      sql.addColumn("LU_KT_HDR_RFN_ID", lineItem.getKitHeaderReference());
    }
    else if (lineItem.isKitComponent())
    {
      sql.addColumn("LU_KT_ST", inQuotes(2));
      sql.addColumn("ID_CLN", getItemKitID((KitComponentLineItemIfc)lineItem));
      sql.addColumn("LU_KT_HDR_RFN_ID", lineItem.getKitHeaderReference());
    }
    if (lineItem.isPriceAdjustmentLineItem())
    {
      sql.addColumn("FL_ITM_PRC_ADJ", makeCharFromBoolean(true));
      sql.addColumn("LU_PRC_ADJ_RFN_ID", lineItem.getPriceAdjustmentReference());
    }
    else if (lineItem.isPartOfPriceAdjustment())
    {
      sql.addColumn("LU_PRC_ADJ_RFN_ID", lineItem.getPriceAdjustmentReference());
    }
    if ((transaction instanceof SaleReturnTransactionIfc)) {
      if (transaction.getTransactionStatus() == 4)
      {
        ReturnItemIfc theReturnItem = lineItem.getReturnItem();
        if (theReturnItem != null)
        {
          boolean wasRetrieved = theReturnItem.isFromRetrievedTransaction();
          if (wasRetrieved) {
            sql.addColumn("FL_RTRVD_TRN", "'1'");
          } else {
            sql.addColumn("FL_RTRVD_TRN", "'0'");
          }
        }
      }
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertSaleReturnLineItem", e);
    }
    saveRetailPriceModifiers(dataConnection, transaction, lineItem);
    saveSaleReturnTaxModifier(dataConnection, transaction, lineItem);
    
    saveSaleReturnLineItemTaxInformation(dataConnection, transaction, lineItem);
    saveExternalOrderLineItem(dataConnection, transaction, lineItem);
    
    String employee = transaction.getSalesAssociate().getEmployeeID();
    if ((lineItem.getSalesAssociate() != null) && (!employee.equals(lineItem.getSalesAssociate().getEmployeeID()))) {
      saveCommissionModifier(dataConnection, transaction, lineItem);
    }
    if ((lineItem.getPLUItem() instanceof UnknownItemIfc)) {
      saveUnknownItem(dataConnection, transaction, lineItem);
    }
    if (((lineItem.getPLUItem() instanceof GiftCertificateItemIfc)) && (transaction.getTransactionStatus() == 2)) {
      insertGiftCertificate(dataConnection, transaction, lineItem);
    }
  }
  
  public void insertDeletedSaleReturnLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_SLS_RTN");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_REGISTRY", getGiftRegistryString(lineItem));
    sql.addColumn("ID_ITM", getItemID(lineItem));
    sql.addColumn("ID_ITM_POS", inQuotes(lineItem.getPosItemID()));
    sql.addColumn("ID_NMB_SRZ", getItemSerial(lineItem));
    sql.addColumn("ID_GP_TX", getTaxGroupID(lineItem));
    sql.addColumn("QU_ITM_LM_RTN_SLS", getItemQuantity(lineItem));
    sql.addColumn("MO_EXTN_LN_ITM_RTN", getItemExtendedAmount(lineItem));
    sql.addColumn("MO_EXTN_DSC_LN_ITM", getItemExtendedDiscountedAmount(lineItem));
    
    sql.addColumn("MO_VAT_LN_ITM_RTN", lineItem.getItemTaxAmount().getStringValue());
    sql.addColumn("MO_TAX_INC_LN_ITM_RTN", lineItem.getItemInclusiveTaxAmount().getStringValue());
    
    sql.addColumn("FL_RTN_MR", getReturnFlag(lineItem));
    sql.addColumn("RC_RTN_MR", getReturnReasonCode(lineItem));
    sql.addColumn("ID_TRN_ORG", getOriginalTransactionId(lineItem));
    sql.addColumn("DC_DY_BSN_ORG", getOriginalDate(lineItem));
    sql.addColumn("AI_LN_ITM_ORG", getOriginalLineNumber(lineItem));
    sql.addColumn("ID_STR_RT_ORG", getOriginalStoreID(lineItem));
    sql.addColumn("ID_DPT_POS", getDepartmentID(lineItem));
    sql.addColumn("FL_SND", getSendFlag(lineItem));
    sql.addColumn("CNT_SND_LAB", getSendLabelCount(lineItem));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("FL_RCV_GF", getGiftReceiptFlag(lineItem));
    sql.addColumn("OR_ID_REF", lineItem.getOrderLineReference());
    sql.addColumn("LU_MTH_ID_ENR", inQuotes(lineItem.getEntryMethod().getIxRetailCode()));
    sql.addColumn("ED_SZ", getItemSizeCode(lineItem));
    
    sql.addColumn("FL_RLTD_ITM_RTN", getReturnRelatedItemFlag(lineItem));
    sql.addColumn("AI_LN_ITM_RLTD", getRelatedSeqNum(lineItem));
    sql.addColumn("FL_RLTD_ITM_RM", getRemoveRelatedItemFlag(lineItem));
    sql.addColumn("MO_PRN_PRC", getPermanentSellingPrice(lineItem));
    sql.addColumn("DE_ITM_SHRT_RCPT", getReceiptDescription(lineItem));
    sql.addColumn("DE_ITM_LCL", getReceiptDescriptionLocal(lineItem));
    sql.addColumn("FL_FE_RSTK", getRestockingFeeFlag(lineItem));
    sql.addColumn("LU_HRC_MR_LV", getProductGroupID(lineItem));
    sql.addColumn("FL_ITM_SZ_REQ", getSizeRequiredFlag(lineItem));
    sql.addColumn("LU_UOM_SLS", getLineItemUOMCode(lineItem));
    sql.addColumn("ID_DPT_POS", getPosDepartmentID(lineItem));
    sql.addColumn("TY_ITM", getItemTypeID(lineItem));
    sql.addColumn("FL_RTN_PRH", getReturnProhibited(lineItem));
    sql.addColumn("FL_DSC_EM_ALW", getEmployeeDiscountAllowed(lineItem));
    sql.addColumn("FL_TX", getTaxable(lineItem));
    sql.addColumn("FL_ITM_DSC", getDiscountable(lineItem));
    sql.addColumn("FL_ITM_DSC_DMG", getDamageDiscountable(lineItem));
    sql.addColumn("FL_VD_LN_ITM", makeSafeString("1"));
    sql.addColumn("ID_MRHRC_GP", getMerchandiseHierarchyGroupID(lineItem));
    sql.addColumn("ID_ITM_MF_UPC", getManufacturerItemUPC(lineItem));
    
    String extendedRestockingFee = getItemExtendedRestockingFee(lineItem);
    if (extendedRestockingFee != null) {
      sql.addColumn("MO_FE_RSTK", extendedRestockingFee);
    }
    if (lineItem.isKitHeader())
    {
      sql.addColumn("LU_KT_ST", inQuotes(1));
      sql.addColumn("ID_CLN", getItemID(lineItem));
      sql.addColumn("LU_KT_HDR_RFN_ID", lineItem.getKitHeaderReference());
    }
    else if (lineItem.isKitComponent())
    {
      sql.addColumn("LU_KT_ST", inQuotes(2));
      sql.addColumn("ID_CLN", getItemKitID((KitComponentLineItemIfc)lineItem));
      sql.addColumn("LU_KT_HDR_RFN_ID", lineItem.getKitHeaderReference());
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertDeletedSaleReturnLineItem", e);
    }
    saveRetailPriceModifiers(dataConnection, transaction, lineItem);
    saveSaleReturnTaxModifier(dataConnection, transaction, lineItem);
    saveExternalOrderLineItem(dataConnection, transaction, lineItem);
    
    String employee = transaction.getSalesAssociate().getEmployeeID();
    if ((lineItem.getSalesAssociate() != null) && (!employee.equals(lineItem.getSalesAssociate().getEmployeeID()))) {
      saveCommissionModifier(dataConnection, transaction, lineItem);
    }
    if ((lineItem.getPLUItem() instanceof UnknownItemIfc)) {
      saveUnknownItem(dataConnection, transaction, lineItem);
    }
    if ((lineItem.getPLUItem() instanceof GiftCardPLUItemIfc)) {
      insertGiftCard(dataConnection, transaction, lineItem);
    }
  }
  
  public void insertUnknownItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("AS_ITM_UNK");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_ITM_POS", getItemID(lineItem));
    sql.addColumn("RP_SLS_POS_CRT", getItemPrice(lineItem));
    sql.addColumn("DE_ITM", getItemDescription(lineItem));
    sql.addColumn("ID_GP_TX", getTaxGroupID(lineItem));
    sql.addColumn("LU_EXM_TX", inQuotes(getItemTaxable(lineItem)));
    String deptID = lineItem.getPLUItem().getDepartmentID();
    if (!Util.isEmpty(deptID)) {
      sql.addColumn("ID_DPT_POS", inQuotes(deptID));
    }
    sql.addColumn("LU_UOM", getUOMCode(lineItem));
    if ((lineItem.getPLUItem() instanceof GiftCertificateItemIfc)) {
      sql.addColumn("TY_ITM_UNK", makeSafeString("GiftCertificate"));
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertUnknownItem", e);
    }
  }
  
  public void insertGiftCertificate(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("DO_CF_GF");
    String boolValue = makeStringFromBoolean(((GiftCertificateItemIfc)lineItem.getPLUItem()).isTrainingMode());
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_NMB_SRZ_GF_CF", getItemID(lineItem));
    sql.addColumn("MO_VL_FC_GF_CF", getItemPrice(lineItem));
    sql.addColumn("FL_MOD_TRG", boolValue);
    sql.addColumn("ID_CNY_ICD", lineItem.getExtendedSellingPrice().getType().getCurrencyId());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertGiftCertificate", e);
    }
  }
  
  public void insertCommissionModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("CO_MDFR_CMN");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_EM", getEmployeeID(lineItem));
    sql.addColumn("AI_MDFR_CMN", getSequenceNumber(0));
    sql.addColumn("FL_PE_CMN_AMT", "'P'");
    sql.addColumn("PE_MDFR_CMN", "100");
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertCommissionModifier", e);
    }
  }
  
  public void insertRetailPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("CO_MDFR_RTL_PRC");
    
    String lineNumber = getLineItemSequenceNumber(lineItem);
    String tranNumber = getTransactionSequenceNumber(transaction);
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", tranNumber);
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", lineNumber);
    sql.addColumn("AI_MDFR_RT_PRC", getSequenceNumber(sequenceNumber));
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    if (discountLineItem != null)
    {
      sql.addColumn("ID_RU_MDFR", getDiscountRuleID(discountLineItem));
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(discountLineItem));
      sql.addColumn("PE_MDFR_RT_PRC", getPriceModifierPercent(discountLineItem));
      
      CurrencyIfc amount = DomainGateway.getBaseCurrencyInstance(getPriceModifierAmount(discountLineItem));
      sql.addColumn("MO_MDFR_RT_PRC", amount.abs().getStringValue());
      sql.addColumn("CD_MTH_PRDV", getPriceModifierMethodCode(discountLineItem));
      sql.addColumn("CD_BAS_PRDV", getPriceModifierAssignmentBasis(discountLineItem));
      
      sql.addColumn("ID_DSC_EM", makeSafeString(discountLineItem.getDiscountEmployeeID()));
      
      sql.addColumn("FL_DSC_DMG", getPriceModifierDamageDiscountFlag(discountLineItem));
      
      sql.addColumn("FL_PCD_DL_ADVN_APLY", getIncludedInBestDealFlag(discountLineItem));
      sql.addColumn("FL_DSC_ADVN_PRC", getAdvancedPricingRuleFlag(discountLineItem));
      sql.addColumn("ID_DSC_REF", getPriceModifierReferenceID(discountLineItem));
      
      sql.addColumn("CO_ID_DSC_REF", getPriceModifierReferenceIDTypeCode(discountLineItem));
      
      sql.addColumn("CD_TY_PRDV", discountLineItem.getTypeCode());
      sql.addColumn("DP_LDG_STK_MDFR", inQuotes(discountLineItem.getAccountingMethod()));
      
      sql.addColumn("ID_PRM", discountLineItem.getPromotionId());
      sql.addColumn("ID_PRM_CMP", discountLineItem.getPromotionComponentId());
      sql.addColumn("ID_PRM_CMP_DTL", discountLineItem.getPromotionComponentDetailId());
      sql.addColumn("ID_PRCGP", discountLineItem.getPricingGroupID());
    }
    else
    {
      sql.addColumn("ID_RU_MDFR", "0");
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(lineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(lineItem));
      
      SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
      if (priceOverrideAuthorization != null)
      {
        sql.addColumn("ID_EM_AZN_OVRD", makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
        
        sql.addColumn("CD_MTH_ENR_OVRD", priceOverrideAuthorization.getEntryMethod().getIxRetailCode());
      }
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertRetailPriceModifier", e);
    }
  }
  
  public void insertSaleReturnTaxModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int taxSequenceNumber, ItemTaxIfc taxLineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("CO_MDFR_SLS_RTN_TX");
    
    int taxGroupId = taxLineItem.getTaxGroupId();
    if (taxLineItem.getTaxInformationContainer() != null) {
      if (taxLineItem.getTaxInformationContainer().getTaxInformation() != null) {
        if (taxLineItem.getTaxInformationContainer().getTaxInformation().length > 0) {
          taxGroupId = taxLineItem.getTaxInformationContainer().getTaxInformation()[0].getTaxGroupID();
        }
      }
    }
    if ((taxLineItem.getExternalTaxEnabled()) && (taxLineItem.getOverrideRate() == 0.0D) && (taxLineItem.getOverrideAmount().signum() == 0))
    {
      taxLineItem.getReason().setCode(Integer.toString(9));
      taxLineItem.setExternalOverrideRate(taxLineItem.getDefaultRate());
      taxLineItem.setDefaultRate(0.0D);
    }
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("AI_MDFR_TX", getSequenceNumber(taxSequenceNumber));
    sql.addColumn("MO_TX_RTN_SLS", getItemTaxAmount(lineItem));
    sql.addColumn("ID_GP_TX", taxGroupId);
    sql.addColumn("TY_TX", getItemTaxMode(taxLineItem));
    sql.addColumn("PE_TX", getItemTaxPercent(taxLineItem));
    sql.addColumn("PE_TX_OVRD", getItemTaxOverridePercent(taxLineItem));
    sql.addColumn("MO_TX_OVRD", getItemTaxOverrideAmount(taxLineItem));
    sql.addColumn("RC_TX_EXM_RTN_SLS", getItemTaxReasonCode(taxLineItem));
    sql.addColumn("ID_SCP_TX", getTaxScope(taxLineItem));
    
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertSaleReturnTaxModifier", e);
    }
  }
  
  public void saveSaleReturnLineItemTaxInformation(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    TaxInformationIfc[] taxInfo = lineItem.getTaxInformationContainer().getTaxInformation();
    if (taxInfo != null) {
      for (int i = 0; i < taxInfo.length; i++) {
        insertSaleReturnTaxLineItem(dataConnection, transaction, lineItem, taxInfo[i]);
      }
    }
  }
  
  public void insertSaleReturnTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, TaxInformationIfc taxInfo)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_SLS_RTN_TX");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_ATHY_TX", taxInfo.getTaxAuthorityID());
    sql.addColumn("ID_GP_TX", taxInfo.getTaxGroupID());
    sql.addColumn("TY_TX", taxInfo.getTaxTypeCode());
    sql.addColumn("FLG_TX_HDY", makeStringFromBoolean(taxInfo.getTaxHoliday()));
    sql.addColumn("MO_TXBL_RTN_SLS", taxInfo.getTaxableAmount().toString());
    sql.addColumn("MO_TX_RTN_SLS", taxInfo.getTaxAmount().toString());
    sql.addColumn("MO_TX_RTN_SLS_TOT", getItemTaxAmount(lineItem));
    sql.addColumn("MO_TX_INC_RTN_SLS_TOT", getItemInclusiveTaxAmount(lineItem));
    sql.addColumn("NM_RU_TX", makeSafeString(taxInfo.getTaxRuleName()));
    sql.addColumn("PE_TX", String.valueOf(taxInfo.getTaxPercentage().floatValue()));
    sql.addColumn("TX_MOD", taxInfo.getTaxMode());
    sql.addColumn("FL_TX_INC", makeStringFromBoolean(taxInfo.getInclusiveTaxFlag()));
    if ((taxInfo.getUniqueID() != null) && (!taxInfo.getUniqueID().equals(""))) {
      sql.addColumn("ID_UNQ", makeSafeString(taxInfo.getUniqueID()));
    }
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertSaleReturnTaxLineItem", e);
    }
  }
  
  /**
   * @deprecated
   */
  public void insertSaleReturnTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int taxAuthorityID, CurrencyIfc taxByTaxAuthority, String taxRuleName, BigDecimal taxRate)
    throws DataException
  {
    ItemTaxIfc taxLineItem = lineItem.getItemPrice().getItemTax();
    
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_SLS_RTN_TX");
    if ((taxLineItem.getExternalTaxEnabled()) && (taxLineItem.getOverrideRate() == 0.0D) && (taxLineItem.getOverrideAmount().signum() == 0))
    {
      taxLineItem.getReason().setCode(Integer.toString(9));
      taxLineItem.setExternalOverrideRate(taxLineItem.getDefaultRate());
      taxLineItem.setDefaultRate(0.0D);
    }
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_ATHY_TX", taxAuthorityID);
    sql.addColumn("ID_GP_TX", getTaxGroupID(lineItem));
    
    sql.addColumn("MO_TXBL_RTN_SLS", lineItem.getExtendedDiscountedSellingPrice().getStringValue());
    sql.addColumn("MO_TX_RTN_SLS", taxByTaxAuthority.getStringValue());
    sql.addColumn("MO_TX_RTN_SLS_TOT", getItemTaxAmount(lineItem));
    sql.addColumn("NM_RU_TX", makeSafeString(taxRuleName));
    sql.addColumn("PE_TX", taxRate.toString());
    
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertSaleReturnTaxLineItem", e);
    }
  }
  
  public void insertTaxLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber)
    throws DataException
  {
    insertRetailTransactionLineItem(dataConnection, transaction, lineItemSequenceNumber, "TX");
    
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_TX");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItemSequenceNumber));
    sql.addColumn("MO_TX", getTaxAmount(transaction));
    sql.addColumn("MO_TX_INC", getInclusiveTaxAmount(transaction));
    sql.addColumn("TY_TX", getTaxMode(transaction));
    sql.addColumn("PE_TX", getTaxPercent(transaction));
    sql.addColumn("PE_TX_OVRD", getTaxOverridePercent(transaction));
    sql.addColumn("MO_TX_OVRD", getTaxOverrideAmount(transaction));
    sql.addColumn("RC_TX", getTaxReasonCode(transaction));
    
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertTaxLineItem", e);
    }
  }
  
  public void insertTaxExemptionModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("CO_MDFR_TX_EXM");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItemSequenceNumber));
    sql.addColumn("ID_NCRPT_CF_TX_EXM", getTaxExemptCertificateID(transaction));
    sql.addColumn("ID_MSK_CF_TX_EXM", getMaskedTaxExemptCertificateID(transaction));
    sql.addColumn("RC_EXM_TX", getTaxReasonCode(transaction));
    sql.addColumn("MO_EXM_TX", getTaxExemptAmount(transaction));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertTaxExemptionModifier", e);
    }
  }
  
  public void insertDiscountLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, int lineItemSequenceNumber, TransactionDiscountStrategyIfc lineItem)
    throws DataException
  {
    insertRetailTransactionLineItem(dataConnection, transaction, lineItemSequenceNumber, "DS");
    
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_DSC");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItemSequenceNumber));
    sql.addColumn("RC_DSC", getDiscountReasonCode(lineItem));
    sql.addColumn("TY_DSC", getDiscountType(lineItem));
    sql.addColumn("MO_DSC", getDiscountAmount(lineItem));
    sql.addColumn("PE_DSC", getDiscountPercent(lineItem));
    sql.addColumn("LU_BAS_ASGN", getDiscountAssignmentBasis(lineItem));
    sql.addColumn("ID_DSC_EM", makeSafeString(lineItem.getDiscountEmployeeID()));
    
    sql.addColumn("FL_DSC_ENA", getDiscountEnabled(lineItem));
    sql.addColumn("FL_DL_ADVN_APLY", getIncludedInBestDealFlag(lineItem));
    sql.addColumn("ID_RU_DSC", getDiscountRuleID(lineItem));
    sql.addColumn("ID_DSC_REF", getDiscountReferenceID(lineItem));
    sql.addColumn("CO_ID_DSC_REF", getDiscountReferenceIDTypeCode(lineItem));
    sql.addColumn("ID_PRM", getPromotionID(lineItem));
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("ID_PRCGP", getPricingGroupID(transaction));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertDiscountLineItem", e);
    }
  }
  
  public void removeOrderLineItems(JdbcDataConnection dataConnection, OrderTransactionIfc orderTransaction)
    throws DataException
  {
    SQLDeleteStatement sql = new SQLDeleteStatement();
    
    sql.setTable("OR_LTM");
    
    sql.addQualifier("ID_ORD", inQuotes(orderTransaction.getOrderID()));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      if (de.getErrorCode() != 6)
      {
        logger.error(de.toString());
        throw de;
      }
    }
    catch (Exception e)
    {
      throw new DataException(0, "removeOrderLineItems", e);
    }
  }
  
  public void insertOrderLineItem(JdbcDataConnection dataConnection, OrderTransactionIfc orderTransaction, SaleReturnLineItemIfc lineItem, int lineItemSequenceNumber)
    throws DataException
  {
    OrderStatusIfc orderStatus = orderTransaction.getOrderStatus();
    OrderItemStatusIfc itemStatus = lineItem.getOrderItemStatus();
    
    SQLInsertStatement sql = new SQLInsertStatement();
    sql.setTable("OR_LTM");
    
    sql.addColumn("ID_ORD", inQuotes(orderTransaction.getOrderID()));
    sql.addColumn("DC_DY_BSN", dateToSQLDateString(orderStatus.getTimestampBegin().dateValue()));
    sql.addColumn("ST_ITM_ORD", Integer.toString(itemStatus.getStatus().getStatus()));
    sql.addColumn("ST_PREV_ITM", Integer.toString(itemStatus.getStatus().getPreviousStatus()));
    sql.addColumn("LN_ITM_REF", lineItem.getOrderLineReference());
    sql.addColumn("ID_PRTY", "0");
    sql.addColumn("ID_ITM", inQuotes(lineItem.getItemID()));
    sql.addColumn("ID_ITM_POS", inQuotes(lineItem.getPosItemID()));
    sql.addColumn("ID_DPT_POS", inQuotes(lineItem.getPLUItem().getDepartmentID()));
    sql.addColumn("ID_GP_TX", Integer.toString(lineItem.getTaxGroupID()));
    sql.addColumn("QU_ITM_LM_RTN_SLS", getItemQuantity(lineItem));
    sql.addColumn("MO_EXTN_LN_ITM_RTN", getItemExtendedAmount(lineItem));
    sql.addColumn("MO_VAT_LN_ITM_RTN", lineItem.getItemTaxAmount().getStringValue());
    sql.addColumn("MO_TAX_INC_LN_ITM_RTN", lineItem.getItemInclusiveTaxAmount().getStringValue());
    
    sql.addColumn("AI_LN_ITM", Integer.toString(lineItemSequenceNumber));
    sql.addColumn("DE_ITM", getItemDescription(lineItem));
    if (itemStatus.getStatus().getLastStatusChange() != null) {
      sql.addColumn("DC_DY_BSN_CHG", dateToSQLDateString(itemStatus.getStatus().getLastStatusChange()));
    }
    sql.addColumn("QU_ITM_PCK", itemStatus.getQuantityPicked().toString());
    sql.addColumn("QU_ITM_SHP", itemStatus.getQuantityShipped().toString());
    sql.addColumn("MO_ORD_DS", itemStatus.getDepositAmount().toString());
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(orderTransaction));
    
    sql.addColumn("ID_STR_RT", getStoreID(orderTransaction));
    sql.addColumn("ID_WS", getWorkstationID(orderTransaction));
    if (orderTransaction.getOrderType() == 2)
    {
      sql.addColumn("DP_ORD_LN_ITM", inQuotes(String.valueOf(itemStatus.getItemDispositionCode())));
      if (itemStatus.getItemDispositionCode() == 3)
      {
        int deliveryId = lineItem.getOrderItemStatus().getDeliveryDetails().getDeliveryDetailID();
        sql.addColumn("AI_ORD_DEL_DTL", Integer.toString(deliveryId));
      }
      if (itemStatus.getItemDispositionCode() == 2) {
        sql.addColumn("DC_ORD_PCKP", dateToDateFormatString(getPickupDate(itemStatus).dateValue()));
      }
    }
    insertPickupDeliveryOrderLineItemStatus(dataConnection, orderTransaction, lineItem, lineItemSequenceNumber);
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      logger.error("" + Util.throwableToString(e) + "");
      
      throw new DataException(0, "insertOrderLineItem", e);
    }
  }
  
  public void insertPickupDeliveryOrderLineItemStatus(JdbcDataConnection dataConnection, OrderTransactionIfc orderTransaction, SaleReturnLineItemIfc lineItem, int lineItemSequenceNumber)
    throws DataException
  {
    OrderItemStatusIfc itemStatus = lineItem.getOrderItemStatus();
    
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_SLS_RTN_ORD");
    
    sql.addColumn("ID_STR_RT", getStoreID(orderTransaction));
    sql.addColumn("ID_WS", getWorkstationID(orderTransaction));
    sql.addColumn("DC_DY_BSN", dateToSQLDateString(orderTransaction.getBusinessDay()));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(orderTransaction));
    sql.addColumn("AI_LN_ITM", Integer.toString(lineItemSequenceNumber));
    sql.addColumn("ID_ORD", inQuotes(orderTransaction.getOrderID()));
    sql.addColumn("AI_ORD_LN_ITM", getOrderItemSequeceNumber(lineItem));
    sql.addColumn("DC_ORD_DY_BSN", getOrderBusinessDate(orderTransaction));
    sql.addColumn("ST_ITM_ORD", getItemStatus(itemStatus));
    sql.addColumn("ST_PREV_ITM", getItemPreviousStatus(itemStatus));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertPickupOrderLineItem", e);
    }
  }
  
  public void updateOrderLineItem(JdbcDataConnection dataConnection, OrderTransactionIfc orderTransaction, SaleReturnLineItemIfc lineItem, int lineItemSequenceNumber)
    throws DataException
  {
    OrderItemStatusIfc itemStatus = lineItem.getOrderItemStatus();
    int recipientDetailId = 0;
    Collection<OrderRecipientIfc> orderRecipientCollection = orderTransaction.getOrderRecipients();
    if (orderRecipientCollection != null)
    {
      Iterator<OrderRecipientIfc> orderRecipientsIterator = orderRecipientCollection.iterator();
      while (orderRecipientsIterator.hasNext())
      {
        OrderRecipientIfc orderRecipient = (OrderRecipientIfc)orderRecipientsIterator.next();
        recipientDetailId = orderRecipient.getRecipientDetailID();
      }
    }
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("OR_LTM");
    
    sql.addColumn("ST_ITM_ORD", Integer.toString(itemStatus.getStatus().getStatus()));
    sql.addColumn("ST_PREV_ITM", Integer.toString(itemStatus.getStatus().getPreviousStatus()));
    sql.addColumn("ID_GP_TX", Integer.toString(lineItem.getTaxGroupID()));
    sql.addColumn("QU_ITM_LM_RTN_SLS", getItemQuantity(lineItem));
    sql.addColumn("MO_EXTN_LN_ITM_RTN", getItemExtendedAmount(lineItem));
    sql.addColumn("MO_VAT_LN_ITM_RTN", lineItem.getItemTaxAmount().getStringValue());
    sql.addColumn("MO_TAX_INC_LN_ITM_RTN", lineItem.getItemInclusiveTaxAmount().getStringValue());
    
    sql.addColumn("DE_ITM", getItemDescription(lineItem));
    if (itemStatus.getStatus().getLastStatusChange() != null) {
      sql.addColumn("DC_DY_BSN_CHG", dateToSQLDateString(itemStatus.getStatus().getLastStatusChange()));
    }
    sql.addColumn("QU_ITM_PCK", itemStatus.getQuantityPicked().toString());
    sql.addColumn("QU_ITM_SHP", itemStatus.getQuantityShipped().toString());
    sql.addColumn("MO_ORD_DS", itemStatus.getDepositAmount().toString());
    if (itemStatus.getStatus().getStatus() == 4) {
      sql.addColumn("AI_ORD_RCPNT", recipientDetailId);
    }
    sql.addQualifier("ID_ORD = '" + orderTransaction.getOrderID() + "'");
    sql.addQualifier("LN_ITM_REF = " + lineItem.getOrderLineReference());
    
    insertPickupDeliveryOrderLineItemStatus(dataConnection, orderTransaction, lineItem, lineItem.getLineNumber());
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      logger.error("" + Util.throwableToString(e) + "");
      
      throw new DataException(0, "insertOrderLineItem", e);
    }
  }
  
  public void insertGiftCard(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    GiftCardIfc giftCard = ((GiftCardPLUItemIfc)lineItem.getPLUItem()).getGiftCard();
    
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("DO_CRD_GF");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItem.getLineNumber()));
    EncipheredCardDataIfc cardData = giftCard.getEncipheredCardData();
    if (cardData != null)
    {
      sql.addColumn("ID_NCRPT_ACNT_CRD", inQuotes(cardData.getEncryptedAcctNumber()));
      sql.addColumn("ID_MSK_ACNT_CRD", inQuotes(cardData.getMaskedAcctNumber()));
    }
    sql.addColumn("LU_AJD_ACTV_GF", makeSafeString(giftCard.getApprovalCode()));
    sql.addColumn("LU_NMB_CRD_SWP_KY", getEntryMethod(giftCard.getEntryMethod()));
    sql.addColumn("TY_RQST_GF_CRD", inQuotes(String.valueOf(giftCard.getRequestType())));
    if (giftCard.getCurrentBalance() != null) {
      sql.addColumn("BLNC_GF_CRD", giftCard.getCurrentBalance().getStringValue());
    }
    if (giftCard.getInitialBalance() != null) {
      sql.addColumn("BLNC_GF_CRD_ORIG", giftCard.getInitialBalance().getStringValue());
    }
    sql.addColumn("ID_CNY_ICD", lineItem.getExtendedSellingPrice().getType().getCurrencyId());
    
    sql.addColumn("LU_AZN_STLM_DT", makeSafeString(giftCard.getSettlementData()));
    
    String authDateTime = getAuthorizationDateTime(giftCard.getAuthorizedDateTime());
    if ((authDateTime == null) || (authDateTime.equals("")) || (authDateTime.equals("null"))) {
      sql.addColumn("TS_AZN", getSQLCurrentTimestampFunction());
    } else {
      sql.addColumn("TS_AZN", authDateTime);
    }
    sql.addColumn("LU_AZN_JL_KY", makeSafeString(giftCard.getJournalKey()));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertGiftCard", e);
    }
  }
  
  protected String getEntryMethod(EntryMethod entryMethod)
  {
    String returnValue = inQuotes("");
    if (entryMethod != null) {
      returnValue = inQuotes(entryMethod.toString());
    }
    return returnValue;
  }
  
  public void saveAlteration(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    try
    {
      insertAlteration(dataConnection, transaction, lineItem);
    }
    catch (DataException de)
    {
      updateAlteration(dataConnection, transaction, lineItem);
    }
  }
  
  public void insertAlteration(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    AlterationIfc alteration = ((AlterationPLUItemIfc)lineItem.getPLUItem()).getAlteration();
    
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_ALTR");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItem.getLineNumber()));
    sql.addColumn("TY_ALTR", inQuotes(alteration.getAlterationType()));
    sql.addColumn("DE_ITM", makeSafeString(alteration.getItemDescription()));
    sql.addColumn("ID_ITM", makeSafeString(alteration.getItemNumber()));
    sql.addColumn("DE_VL1", makeSafeString(alteration.getValue1()));
    sql.addColumn("DE_VL2", makeSafeString(alteration.getValue2()));
    sql.addColumn("DE_VL3", makeSafeString(alteration.getValue3()));
    sql.addColumn("DE_VL4", makeSafeString(alteration.getValue4()));
    sql.addColumn("DE_VL5", makeSafeString(alteration.getValue5()));
    sql.addColumn("DE_VL6", makeSafeString(alteration.getValue6()));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertAlteration", e);
    }
  }
  
  public void updateAlteration(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    AlterationIfc alteration = ((AlterationPLUItemIfc)lineItem.getPLUItem()).getAlteration();
    
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_ALTR");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItem.getLineNumber()));
    sql.addColumn("TY_ALTR", inQuotes(alteration.getAlterationType()));
    sql.addColumn("DE_ITM", makeSafeString(alteration.getItemDescription()));
    sql.addColumn("ID_ITM", makeSafeString(alteration.getItemNumber()));
    sql.addColumn("DE_VL1", makeSafeString(alteration.getValue1()));
    sql.addColumn("DE_VL2", makeSafeString(alteration.getValue2()));
    sql.addColumn("DE_VL3", makeSafeString(alteration.getValue3()));
    sql.addColumn("DE_VL4", makeSafeString(alteration.getValue4()));
    sql.addColumn("DE_VL5", makeSafeString(alteration.getValue5()));
    sql.addColumn("DE_VL6", makeSafeString(alteration.getValue6()));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateAlteration", e);
    }
  }
  
  public String getDiscountReferenceID(DiscountRuleIfc lineItem)
  {
    String returnString = null;
    if (lineItem.getReferenceID() != null) {
      returnString = "'" + lineItem.getReferenceID() + "'";
    }
    return returnString;
  }
  
  public String getDiscountReferenceIDTypeCode(DiscountRuleIfc lineItem)
  {
    return "'" + DiscountRuleConstantsIfc.REFERENCE_ID_TYPE_CODE[lineItem.getReferenceIDCode()] + "'";
  }
  
  public String getPriceModifierReferenceID(DiscountRuleIfc lineItem)
  {
    String returnString = null;
    if (lineItem.getReferenceID() != null) {
      returnString = "'" + lineItem.getReferenceID() + "'";
    }
    return returnString;
  }
  
  public String getPriceModifierReferenceIDTypeCode(DiscountRuleIfc lineItem)
  {
    return "'" + DiscountRuleConstantsIfc.REFERENCE_ID_TYPE_CODE[lineItem.getReferenceIDCode()] + "'";
  }
  
  public String getPriceModifierPercent(ItemDiscountStrategyIfc lineItem)
  {
    BigDecimal percent = BigDecimal.ZERO;
    switch (lineItem.getDiscountMethod())
    {
    case 1: 
      percent = lineItem.getDiscountRate().movePointRight(2);
      break;
    }
    String result = percent.toString();
    int pos = result.indexOf(".");
    if (pos != -1)
    {
      int fracPlaces = result.length() - (pos + 1);
      if (fracPlaces >= 2) {
        result = result.substring(0, pos + 3);
      }
    }
    return result;
  }
  
  public String getPriceModifierAmount(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getItemPrice().getSellingPrice().getStringValue();
  }
  
  public String getPriceModifierAmount(ItemDiscountStrategyIfc lineItem)
  {
	  logger.info("From getPriceModifierAmount "+lineItem.getName()+" : "+lineItem.getDiscountMethod()+" : "+lineItem.getDescription()); 
    String amount = null;
    switch (lineItem.getDiscountMethod())
    {
    case 2: 
    case 3: 
      if ((lineItem instanceof ItemDiscountByAmountIfc))
      {
        ItemDiscountByAmountIfc discount = (ItemDiscountByAmountIfc)lineItem;
        
        amount = discount.getDiscountAmount().abs().getStringValue();
      }
      else if ((lineItem instanceof ReturnItemTransactionDiscountAuditIfc))
      {
        ReturnItemTransactionDiscountAuditIfc discount = (ReturnItemTransactionDiscountAuditIfc)lineItem;
        amount = discount.getDiscountAmount().getStringValue();
      }
      else if ((lineItem instanceof ItemTransactionDiscountAuditIfc))
      {
        ItemTransactionDiscountAuditIfc discount = (ItemTransactionDiscountAuditIfc)lineItem;
        amount = discount.getDiscountAmount().getStringValue();
      }
      break;
    case 1: 
      amount = lineItem.getDiscountAmount().abs().getStringValue();
      break;
    default: 
      amount = "0";
    }
    return amount;
  }
  
  public String getDiscountType(TransactionDiscountStrategyIfc discount)
  {
    String typeCode = DiscountRuleConstantsIfc.DISCOUNT_METHOD_CODE[discount.getDiscountMethod()];
    return "'" + typeCode + "'";
  }
  
  public String getDiscountReasonCode(TransactionDiscountStrategyIfc discount)
  {
    return makeSafeString(discount.getReason().getCode());
  }
  
  public String getDiscountAmount(TransactionDiscountStrategyIfc lineItem)
  {
    BigDecimal amount = BigDecimal.ZERO;
    switch (lineItem.getDiscountMethod())
    {
    case 1: 
    case 2: 
    case 3: 
      amount = new BigDecimal(lineItem.getDiscountAmount().getStringValue());
      break;
    }
    return amount.toString();
  }
  
  public int getPriceModifierMethodCode(ItemDiscountStrategyIfc lineItem)
  {
    return lineItem.getDiscountMethod();
  }
  
  public int getPriceModifierAssignmentBasis(ItemDiscountStrategyIfc lineItem)
  {
    return lineItem.getAssignmentBasis();
  }
  
  public String getDiscountRuleID(DiscountRuleIfc lineItem)
  {
    String ruleID = "1";
    if (!lineItem.getRuleID().equals("")) {
      ruleID = lineItem.getRuleID();
    }
    return ruleID;
  }
  
  public String getDiscountPercent(TransactionDiscountStrategyIfc lineItem)
  {
    BigDecimal percent = BigDecimal.ZERO;
    switch (lineItem.getDiscountMethod())
    {
    case 1: 
      TransactionDiscountByPercentageIfc discount = (TransactionDiscountByPercentageIfc)lineItem;
      
      percent = discount.getDiscountRate();
      if (percent.toString().length() >= 5)
      {
        BigDecimal scaleOne = new BigDecimal(1);
        
        percent = percent.divide(scaleOne, 4, 4);
      }
      percent = percent.movePointRight(2);
      break;
    }
    return percent.toString();
  }
  
  public String getDiscountAssignmentBasis(TransactionDiscountStrategyIfc lineItem)
  {
    StringBuilder strResult = new StringBuilder("'");
    try
    {
      strResult.append(ASSIGNMENT_BASIS_CODES[lineItem.getAssignmentBasis()]);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      strResult.append(ASSIGNMENT_BASIS_CODES[0]);
    }
    strResult.append("'");
    
    return strResult.toString();
  }
  
  public String getDiscountEnabled(TransactionDiscountStrategyIfc lineItem)
  {
    return makeStringFromBoolean(lineItem.getEnabled());
  }
  
  public String getIncludedInBestDealFlag(DiscountRuleIfc lineItem)
  {
    boolean flag = false;
    if ((lineItem instanceof CustomerDiscountByPercentageIfc)) {
      flag = ((CustomerDiscountByPercentageIfc)lineItem).isIncludedInBestDeal();
    }
    return makeStringFromBoolean(flag);
  }
  
  public String getPriceModifierDamageDiscountFlag(ItemDiscountStrategyIfc lineItem)
  {
    boolean flag = lineItem.isDamageDiscount();
    
    return makeStringFromBoolean(flag);
  }
  
  protected String getEmployeeID(SaleReturnLineItemIfc lineItem)
  {
    return "'" + lineItem.getSalesAssociate().getEmployeeID() + "'";
  }
  
  protected String getOriginalDate(SaleReturnLineItemIfc lineItem)
  {
    String ret = "null";
    if ((lineItem.getReturnItem() != null) && (lineItem.getReturnItem().getOriginalTransactionBusinessDate() != null)) {
      ret = dateToSQLDateString(lineItem.getReturnItem().getOriginalTransactionBusinessDate().dateValue());
    }
    return ret;
  }
  
  protected String getOriginalTransactionId(SaleReturnLineItemIfc lineItem)
  {
    String ret = "null";
    if ((lineItem.getReturnItem() != null) && (lineItem.getReturnItem().getOriginalTransactionID() != null)) {
      ret = "'" + lineItem.getReturnItem().getOriginalTransactionID().getTransactionIDString() + "'";
    }
    return ret;
  }
  
  protected String getOriginalStoreID(SaleReturnLineItemIfc lineItem)
  {
    String ret = "null";
    if ((lineItem.getReturnItem() != null) && (lineItem.getReturnItem().getStore() != null)) {
      ret = "'" + lineItem.getReturnItem().getStore().getStoreID() + "'";
    }
    return ret;
  }
  
  protected String getOriginalLineNumber(SaleReturnLineItemIfc lineItem)
  {
    String ret = "-1";
    if (lineItem.getReturnItem() != null) {
      ret = String.valueOf(lineItem.getReturnItem().getOriginalLineNumber());
    } else if ((lineItem instanceof OrderLineItemIfc)) {
      ret = String.valueOf(lineItem.getOriginalLineNumber());
    }
    return ret;
  }
  
  protected String getLineItemType(String lineItemType)
  {
    return "'" + lineItemType + "'";
  }
  
  public String getGiftRegistryString(SaleReturnLineItemIfc lineItem)
  {
    String value = "null";
    if (lineItem.getRegistry() != null) {
      value = "'" + lineItem.getRegistry().getID() + "'";
    }
    return value;
  }
  
  protected String getLineItemSequenceNumber(AbstractTransactionLineItemIfc lineItem)
  {
    return String.valueOf(lineItem.getLineNumber());
  }
  
  protected String getItemID(SaleReturnLineItemIfc lineItem)
  {
    return "'" + lineItem.getPLUItem().getItemID() + "'";
  }
  
  protected String getItemKitID(KitComponentLineItemIfc lineItem)
  {
    return "'" + lineItem.getItemKitID() + "'";
  }
  
  protected String getItemSerial(SaleReturnLineItemIfc lineItem)
  {
    String ret = "null";
    if (lineItem.getItemSerial() != null) {
      ret = "'" + lineItem.getItemSerial() + "'";
    }
    return ret;
  }
  
  protected String getTaxGroupID(SaleReturnLineItemIfc lineItem)
  {
    return Integer.toString(lineItem.getPLUItem().getTaxGroupID());
  }
  
  protected String getTaxAmount(RetailTransactionIfc transaction)
  {
    return transaction.getTransactionTotals().getTaxInformationContainer().getTaxAmount().toString();
  }
  
  protected String getInclusiveTaxAmount(RetailTransactionIfc transaction)
  {
    return transaction.getTransactionTotals().getTaxInformationContainer().getInclusiveTaxAmount().toString();
  }
  
  protected String getTaxMode(RetailTransactionIfc transaction)
  {
    return String.valueOf(transaction.getTransactionTax().getTaxMode());
  }
  
  protected String getTaxPercent(RetailTransactionIfc transaction)
  {
    return String.valueOf(transaction.getTransactionTax().getDefaultRate() * 100.0D);
  }
  
  protected String getTaxOverridePercent(RetailTransactionIfc transaction)
  {
    return String.valueOf(transaction.getTransactionTax().getOverrideRate() * 100.0D);
  }
  
  protected String getTaxOverrideAmount(RetailTransactionIfc transaction)
  {
    return transaction.getTransactionTax().getOverrideAmount().getStringValue();
  }
  
  protected String getTaxReasonCode(RetailTransactionIfc transaction)
  {
    return makeSafeString(transaction.getTransactionTax().getReason().getCode());
  }
  
  protected String getTaxExemptCertificateID(RetailTransactionIfc transaction)
  {
    return makeSafeString(transaction.getTransactionTax().getTaxExemptCertificateID());
  }
  
  protected String getMaskedTaxExemptCertificateID(RetailTransactionIfc transaction)
  {
    EncipheredDataIfc taxCertificate = FoundationObjectFactory.getFactory().createEncipheredDataInstance(transaction.getTransactionTax().getTaxExemptCertificateID());
    
    return makeSafeString(taxCertificate.getMaskedNumber());
  }
  
  protected String getItemQuantity(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getItemQuantityDecimal().toString();
  }
  
  protected String getUOMCode(SaleReturnLineItemIfc lineItem)
  {
    String value = "null";
    PLUItemIfc pluItem = lineItem.getPLUItem();
    if (pluItem.getUnitOfMeasure() != null)
    {
      value = "'" + lineItem.getPLUItem().getUnitOfMeasure().getUnitID() + "'";
    }
    else if ((pluItem instanceof UnknownItemIfc))
    {
      UnknownItemIfc item = (UnknownItemIfc)pluItem;
      value = "'" + item.getUOMCode() + "'";
    }
    return value;
  }
  
  protected String getItemDescription(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getPLUItem().getDescription(LocaleMap.getLocale("locale_Default")));
  }
  
  protected String getItemPrice(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getSellingPrice().getStringValue();
  }
  
  protected String getItemExtendedAmount(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getExtendedSellingPrice().getStringValue();
  }
  
  protected String getPermanentSellingPrice(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getItemPrice().getPermanentSellingPrice().getStringValue();
  }
  
  protected String getReceiptDescription(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getReceiptDescription());
  }
  
  protected String getReceiptDescriptionLocal(SaleReturnLineItemIfc lineItem)
  {
    Locale bestLocale = LocaleMap.getBestMatch(lineItem.getReceiptDescriptionLocale());
    return makeSafeString(bestLocale.toString());
  }
  
  protected String getRestockingFeeFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.getPLUItem().getItemClassification().getRestockingFeeFlag()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getProductGroupID(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getPLUItem().getProductGroupID());
  }
  
  protected String getSizeRequiredFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.getPLUItem().isItemSizeRequired()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getLineItemUOMCode(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getPLUItem().getUnitOfMeasure().getUnitID());
  }
  
  protected String getPosDepartmentID(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getPLUItem().getDepartmentID());
  }
  
  protected String getItemTypeID(SaleReturnLineItemIfc lineItem)
  {
    return String.valueOf(lineItem.getPLUItem().getItemClassification().getItemType());
  }
  
  protected String getReturnProhibited(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (!lineItem.getPLUItem().getItemClassification().isReturnEligible()) {
      value = "'1'";
    }
    return value;
  }
  
  private String getEmployeeDiscountAllowed(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.isEmployeeDiscountEligible()) {
      value = "'1'";
    }
    return value;
  }
  
  private String getTaxable(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.getPLUItem().getTaxable()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getItemExtendedDiscountedAmount(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getExtendedDiscountedSellingPrice().getStringValue();
  }
  
  protected String getItemExtendedRestockingFee(SaleReturnLineItemIfc lineItem)
  {
    String restockingFeeString = null;
    
    ItemPriceIfc price = lineItem.getItemPrice();
    if (price != null)
    {
      CurrencyIfc restockingFee = price.getExtendedRestockingFee();
      if (restockingFee != null) {
        restockingFeeString = restockingFee.getStringValue();
      }
    }
    return restockingFeeString;
  }
  
  protected String getItemTaxAmount(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getItemPrice().getItemTaxAmount().getStringValue();
  }
  
  protected String getItemInclusiveTaxAmount(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getItemPrice().getItemInclusiveTaxAmount().getStringValue();
  }
  
  protected String getItemTaxPercent(ItemTaxIfc itemTax)
  {
    return String.valueOf(itemTax.getDefaultRate() * 100.0D);
  }
  
  protected String getItemTaxOverridePercent(ItemTaxIfc itemTax)
  {
    return String.valueOf(itemTax.getOverrideRate() * 100.0D);
  }
  
  protected String getItemTaxOverrideAmount(ItemTaxIfc itemTax)
  {
    return itemTax.getOverrideAmount().getStringValue();
  }
  
  protected String getItemTaxMode(ItemTaxIfc itemTax)
  {
    return String.valueOf(itemTax.getTaxMode());
  }
  
  protected String getItemTaxable(SaleReturnLineItemIfc lineItem)
  {
    String value = "0";
    if (lineItem.getItemPrice().getItemTax().getTaxable()) {
      value = "1";
    }
    return value;
  }
  
  protected String getTaxScope(ItemTaxIfc itemTax)
  {
    return String.valueOf(itemTax.getTaxScope());
  }
  
  protected String getItemTaxReasonCode(ItemTaxIfc itemTax)
  {
    return makeSafeString(itemTax.getReason().getCode());
  }
  
  protected String getReturnFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.getItemQuantityDecimal().signum() < 0) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getAdvancedPricingRuleFlag(DiscountRuleIfc lineItem)
  {
    boolean flag = lineItem.isAdvancedPricingRule();
    
    return makeStringFromBoolean(flag);
  }
  
  protected String getReturnReasonCode(SaleReturnLineItemIfc lineItem)
  {
    String value = "null";
    if (lineItem.getReturnItem() != null) {
      value = makeSafeString(lineItem.getReturnItem().getReason().getCode());
    }
    return value;
  }
  
  protected String getDepartmentID(SaleReturnLineItemIfc lineItem)
  {
    return "'" + lineItem.getPLUItem().getDepartmentID() + "'";
  }
  
  protected String getSequenceNumber(int sequenceNumber)
  {
    return String.valueOf(sequenceNumber);
  }
  
  protected String getPriceModifierReasonCode(ItemDiscountStrategyIfc discount)
  {
    return makeSafeString(discount.getReason().getCode());
  }
  
  protected String getPriceModifierReasonCode(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getItemPrice().getItemPriceOverrideReason().getCode());
  }
  
  protected String getItemTaxMethod(ItemTaxIfc itemTax)
  {
    String value = "0";
    if (itemTax.getExternalTaxEnabled()) {
      value = "1";
    }
    return value;
  }
  
  protected String getSendFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.getItemSendFlag()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getSendLabelCount(SaleReturnLineItemIfc lineItem)
  {
    return String.valueOf(lineItem.getSendLabelCount());
  }
  
  protected String getGiftReceiptFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.isGiftReceiptItem()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getReturnRelatedItemFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.isRelatedItemReturnable()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getRelatedSeqNum(SaleReturnLineItemIfc lineItem)
  {
    return String.valueOf(lineItem.getRelatedItemSequenceNumber());
  }
  
  protected String getRemoveRelatedItemFlag(SaleReturnLineItemIfc lineItem)
  {
    String value = "'0'";
    if (lineItem.isRelatedItemDeleteable()) {
      value = "'1'";
    }
    return value;
  }
  
  protected String getItemSizeCode(SaleReturnLineItemIfc lineItem)
  {
    String value = "";
    if (lineItem.getItemSizeCode() != null) {
      value = lineItem.getItemSizeCode();
    }
    value = makeSafeString(value);
    return value;
  }
  
  public int getPromotionID(DiscountRuleIfc lineItem)
  {
    return lineItem.getPromotionId();
  }
  
  public void insertReturnTendersData(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction, ReturnTenderDataElementIfc returnTender)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_RTN_TND");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("TY_TND", inQuotes(returnTender.getTenderType()));
    sql.addColumn("ID_ISSR_TND_MD", inQuotes(returnTender.getCardType()));
    sql.addColumn("MO_ITM_LN_TND", returnTender.getTenderAmount().getStringValue());
    if ((returnTender.getApprovalCode() == null) || (!returnTender.getApprovalCode().equals(""))) {
      sql.addColumn("RSPS_AZN", returnTender.getApprovalCode());
    }
    if (returnTender.getExpirationDate() != null) {
      sql.addColumn("DC_EP", dateToSQLDateString(returnTender.getExpirationDate().dateValue()));
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.debug(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertTenderLineItem", e);
    }
  }
  
  public void updateReturnTendersData(JdbcDataConnection dataConnection, TenderableTransactionIfc transaction, ReturnTenderDataElementIfc returnTender)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_RTN_TND");
    
    sql.addColumn("TY_TND", inQuotes(returnTender.getTenderType()));
    sql.addColumn("ID_ISSR_TND_MD", inQuotes(returnTender.getCardType()));
    sql.addColumn("MO_ITM_LN_TND", returnTender.getTenderAmount().getStringValue());
    sql.addColumn("RSPS_AZN", returnTender.getApprovalCode());
    if (returnTender.getExpirationDate() != null) {
      sql.addColumn("DC_EP", dateToSQLDateString(returnTender.getExpirationDate().dateValue()));
    }
    sql.addQualifier("ID_STR_RT", getStoreID(transaction));
    sql.addQualifier("ID_WS", getWorkstationID(transaction));
    sql.addQualifier("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addQualifier("AI_TRN", getTransactionSequenceNumber(transaction));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.debug(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateTenderLineItem", e);
    }
  }
  
  protected String getTaxExemptAmount(RetailTransactionIfc transaction)
  {
    return transaction.getTransactionTotals().getTaxInformationContainer().getTaxExemptAmount().toString();
  }
  
  public void insertSaleReturnPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_MDFR_SLS_RTN_PRC");
    
    String lineNumber = getLineItemSequenceNumber(lineItem);
    String tranNumber = getTransactionSequenceNumber(transaction);
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", tranNumber);
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", lineNumber);
    sql.addColumn("AI_MDFR_RT_PRC", getSequenceNumber(sequenceNumber));
    sql.addColumn("TS_CRT_RCRD", getSQLCurrentTimestampFunction());
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    if (discountLineItem != null)
    {
      int promotionId = 0;
      if ((transaction.getTransactionDiscounts() != null) && (transaction.getTransactionDiscounts().length > 0)) {
        promotionId = transaction.getTransactionDiscounts()[0].getPromotionId();
      } else {
        promotionId = discountLineItem.getPromotionId();
      }
      sql.addColumn("ID_RU_MDFR", getDiscountRuleID(discountLineItem));
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(discountLineItem));
      sql.addColumn("PE_MDFR_RT_PRC", getPriceModifierPercent(discountLineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(discountLineItem));
      sql.addColumn("CD_MTH_PRDV", getPriceModifierMethodCode(discountLineItem));
      sql.addColumn("CD_BAS_PRDV", getPriceModifierAssignmentBasis(discountLineItem));
      
      sql.addColumn("ID_DSC_EM", makeSafeString(discountLineItem.getDiscountEmployeeID()));
      
      sql.addColumn("FL_DSC_DMG", getPriceModifierDamageDiscountFlag(discountLineItem));
      
      sql.addColumn("FL_PCD_DL_ADVN_APLY", getIncludedInBestDealFlag(discountLineItem));
      sql.addColumn("FL_DSC_ADVN_PRC", getAdvancedPricingRuleFlag(discountLineItem));
      sql.addColumn("ID_DSC_REF", getPriceModifierReferenceID(discountLineItem));
      
      sql.addColumn("CO_ID_DSC_REF", getPriceModifierReferenceIDTypeCode(discountLineItem));
      
      sql.addColumn("CD_TY_PRDV", discountLineItem.getTypeCode());
      sql.addColumn("DP_LDG_STK_MDFR", inQuotes(discountLineItem.getAccountingMethod()));
      
      sql.addColumn("ID_PRM", promotionId);
      sql.addColumn("ID_PRM_CMP", discountLineItem.getPromotionComponentId());
      sql.addColumn("ID_PRM_CMP_DTL", discountLineItem.getPromotionComponentDetailId());
      sql.addColumn("ID_PRCGP", discountLineItem.getPricingGroupID());
    }
    else
    {
      sql.addColumn("ID_RU_MDFR", "0");
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(lineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(lineItem));
      
      SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
      if (priceOverrideAuthorization != null)
      {
        sql.addColumn("ID_EM_AZN_OVRD", makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
        
        sql.addColumn("CD_MTH_ENR_OVRD", priceOverrideAuthorization.getEntryMethod().getIxRetailCode());
      }
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "Insert SaleReturnPriceModifier", e);
    }
  }
  
  public void updateSaleReturnPriceModifier(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, int sequenceNumber, ItemDiscountStrategyIfc discountLineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_MDFR_SLS_RTN_PRC");
    if (discountLineItem != null)
    {
      int promotionId = 0;
      if ((transaction.getTransactionDiscounts() != null) && (transaction.getTransactionDiscounts().length > 0)) {
        promotionId = transaction.getTransactionDiscounts()[0].getPromotionId();
      } else {
        promotionId = discountLineItem.getPromotionId();
      }
      sql.addColumn("ID_RU_MDFR", getDiscountRuleID(discountLineItem));
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(discountLineItem));
      sql.addColumn("PE_MDFR_RT_PRC", getPriceModifierPercent(discountLineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(discountLineItem));
      sql.addColumn("CD_MTH_PRDV", getPriceModifierMethodCode(discountLineItem));
      sql.addColumn("CD_BAS_PRDV", getPriceModifierAssignmentBasis(discountLineItem));
      
      sql.addColumn("ID_DSC_EM", makeSafeString(discountLineItem.getDiscountEmployeeID()));
      
      sql.addColumn("FL_DSC_DMG", getPriceModifierDamageDiscountFlag(discountLineItem));
      
      sql.addColumn("FL_PCD_DL_ADVN_APLY", getIncludedInBestDealFlag(discountLineItem));
      sql.addColumn("FL_DSC_ADVN_PRC", getAdvancedPricingRuleFlag(discountLineItem));
      sql.addColumn("ID_DSC_REF", getPriceModifierReferenceID(discountLineItem));
      
      sql.addColumn("CO_ID_DSC_REF", getPriceModifierReferenceIDTypeCode(discountLineItem));
      
      sql.addColumn("CD_TY_PRDV", discountLineItem.getTypeCode());
      sql.addColumn("DP_LDG_STK_MDFR", inQuotes(discountLineItem.getAccountingMethod()));
      
      sql.addColumn("ID_PRM", promotionId);
      sql.addColumn("ID_PRM_CMP", discountLineItem.getPromotionComponentId());
      sql.addColumn("ID_PRM_CMP_DTL", discountLineItem.getPromotionComponentDetailId());
      sql.addColumn("ID_PRCGP", discountLineItem.getPricingGroupID());
    }
    else
    {
      sql.addColumn("ID_RU_MDFR", "0");
      sql.addColumn("RC_MDFR_RT_PRC", getPriceModifierReasonCode(lineItem));
      sql.addColumn("MO_MDFR_RT_PRC", getPriceModifierAmount(lineItem));
      
      SecurityOverrideIfc priceOverrideAuthorization = lineItem.getItemPrice().getPriceOverrideAuthorization();
      if (priceOverrideAuthorization != null)
      {
        sql.addColumn("ID_EM_AZN_OVRD", makeSafeString(priceOverrideAuthorization.getAuthorizingEmployeeID()));
        
        sql.addColumn("CD_MTH_ENR_OVRD", priceOverrideAuthorization.getEntryMethod().getIxRetailCode());
      }
    }
    sql.addColumn("TS_MDF_RCRD", getSQLCurrentTimestampFunction());
    
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_MDFR_RT_PRC = " + getSequenceNumber(sequenceNumber));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "UpdateSaleReturnPriceModifier", e);
    }
    if (0 >= dataConnection.getUpdateCount()) {
      throw new DataException(6, "Update SaleReturnPriceModifier");
    }
  }
  
  public void insertPromotionLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, PromotionLineItemIfc promotionLineItem, int promotionLineItemSequenceNumber)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_PRM");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("AI_LTM_PRM", promotionLineItemSequenceNumber);
    sql.addColumn("ID_PRM", promotionLineItem.getPromotionId());
    sql.addColumn("ID_PRM_CMP", promotionLineItem.getPromotionComponentId());
    sql.addColumn("ID_PRM_CMP_DTL", promotionLineItem.getPromotionComponentDetailId());
    sql.addColumn("MO_MDFR_RT_PRC", promotionLineItem.getDiscountAmount().toString());
    sql.addColumn("ID_PRCGP", promotionLineItem.getPricingGroupID());
    sql.addColumn("NM_EV_LCL", inQuotes(String.valueOf(promotionLineItem.getReceiptLocale())));
    if (promotionLineItem.getPromotionName() != null) {
      sql.addColumn("NM_EV_RCPT", inQuotes(promotionLineItem.getPromotionName()));
    }
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertPromotionLineItem", e);
    }
  }
  
  public void updatePromotionLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem, PromotionLineItemIfc promotionLineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_PRM");
    
    sql.addColumn("AI_LTM_PRM", promotionLineItem.getPromotionLineItemSequenceNumber());
    sql.addColumn("ID_PRM", promotionLineItem.getPromotionId());
    sql.addColumn("ID_PRM_CMP", promotionLineItem.getPromotionComponentId());
    sql.addColumn("ID_PRM_CMP_DTL", promotionLineItem.getPromotionComponentDetailId());
    sql.addColumn("MO_MDFR_RT_PRC", promotionLineItem.getDiscountAmount().getStringValue());
    sql.addColumn("ID_PRCGP", promotionLineItem.getPricingGroupID());
    sql.addColumn("NM_EV_LCL", inQuotes(String.valueOf(promotionLineItem.getReceiptLocale())));
    if (promotionLineItem.getPromotionName() != null) {
      sql.addColumn("NM_EV_RCPT", makeSafeString(promotionLineItem.getPromotionName()));
    } else {
      sql.addColumn("NM_EV_RCPT", null);
    }
    sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
    sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
    sql.addQualifier("AI_TRN = " + getTransactionSequenceNumber(transaction));
    sql.addQualifier("AI_LN_ITM = " + getLineItemSequenceNumber(lineItem));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updatePromotionLineItem", e);
    }
  }
  
  protected String getItemPermanentPrice(SaleReturnLineItemIfc lineItem)
  {
    return lineItem.getPLUItem().getItem().getPermanentPrice().getStringValue();
  }
  
  public String getAuthorizationDateTime(EYSDate dateTime)
  {
    String date = "null";
    if (dateTime != null) {
      date = dateToSQLTimestampString(dateTime.dateValue());
    }
    return date;
  }
  
  public String getSalesAssociateModifiedFlag(SaleReturnLineItemIfc lineItem)
  {
    if (lineItem.getSalesAssociateModifiedFlag()) {
      return "'1'";
    }
    return "'0'";
  }
  
  /**
   * @deprecated
   */
  protected boolean isGiftCertificateDeleted(SaleReturnTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
  {
    Vector<SaleReturnLineItemIfc> deletedLines = transaction.getDeletedLineItems();
    for (SaleReturnLineItemIfc deletedLine : deletedLines) {
      if ((lineItem == deletedLine) || (lineItem.equals(deletedLine))) {
        return true;
      }
    }
    return false;
  }
  
  public String getBusinessDate(OrderTransactionIfc orderTransaction)
  {
    String businessDate = dateToSQLDateString(orderTransaction.getTimestampBegin().dateValue());
    
    return businessDate;
  }
  
  public String getOrderBusinessDate(OrderTransactionIfc orderTransaction)
  {
    String orderBusinessDate = dateToSQLDateString(orderTransaction.getOrderStatus().getTimestampBegin().dateValue());
    
    return orderBusinessDate;
  }
  
  public int getItemStatus(OrderItemStatusIfc iStatus)
  {
    int itemStatus = iStatus.getStatus().getStatus();
    return itemStatus;
  }
  
  public int getItemPreviousStatus(OrderItemStatusIfc iStatus)
  {
    int itemPreviousStatus = iStatus.getStatus().getPreviousStatus();
    return itemPreviousStatus;
  }
  
  public String getOrderItemSequeceNumber(SaleReturnLineItemIfc lineItem)
  {
    return Integer.toString(lineItem.getOrderLineReference() - 1);
  }
  
  public EYSDate getPickupDate(OrderItemStatusIfc itemStatus)
  {
    EYSDate pickupDate = itemStatus.getPickupDate();
    return pickupDate;
  }
  
  private int getPricingGroupID(RetailTransactionIfc transaction)
  {
    Vector v = ((SaleReturnTransactionIfc)transaction).getItemContainerProxy().getLineItemsVector();
    for (int i = v.size() - 1; i >= 0; i--)
    {
      SaleReturnLineItemIfc lineItem = (SaleReturnLineItemIfc)v.get(i);
      ItemDiscountStrategyIfc[] modifiers = lineItem.getItemPrice().getItemDiscounts();
      if (modifiers != null) {
        for (int j = modifiers.length - 1; j >= 0; j--) {
          if (modifiers[j].getPricingGroupID() != -1) {
            return modifiers[j].getPricingGroupID();
          }
        }
      }
    }
    return -1;
  }
  
  public void insertExternalOrderLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLInsertStatement sql = new SQLInsertStatement();
    
    sql.setTable("TR_LTM_ORD_EXT");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getLineItemSequenceNumber(lineItem));
    sql.addColumn("ID_LTM_ORD_EXT", getExternalOrderItemID(lineItem));
    sql.addColumn("ID_LTM_ORD_EXT_PRNT", getExternalOrderParentItemID(lineItem));
    sql.addColumn("FL_EXT_PRC", getExternalPricingFlag(lineItem));
    sql.addColumn("FL_LTM_ORD_EXT_UPD_SRC", getExternalOrderItemUpdateFlag(lineItem));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "insertExternalOrderLineItem", e);
    }
  }
  
  public void updateExternalOrderLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    SQLUpdateStatement sql = new SQLUpdateStatement();
    
    sql.setTable("TR_LTM_ORD_EXT");
    
    sql.addColumn("ID_STR_RT", getStoreID(transaction));
    sql.addColumn("ID_WS", getWorkstationID(transaction));
    sql.addColumn("AI_TRN", getTransactionSequenceNumber(transaction));
    sql.addColumn("DC_DY_BSN", getBusinessDayString(transaction));
    sql.addColumn("AI_LN_ITM", getSequenceNumber(lineItem.getLineNumber()));
    sql.addColumn("ID_LTM_ORD_EXT", getExternalOrderItemID(lineItem));
    sql.addColumn("ID_LTM_ORD_EXT_PRNT", getExternalOrderParentItemID(lineItem));
    sql.addColumn("FL_EXT_PRC", getExternalPricingFlag(lineItem));
    sql.addColumn("FL_LTM_ORD_EXT_UPD_SRC", getExternalOrderItemUpdateFlag(lineItem));
    try
    {
      dataConnection.execute(sql.getSQLString());
    }
    catch (DataException de)
    {
      logger.error(de.toString());
      throw de;
    }
    catch (Exception e)
    {
      throw new DataException(0, "updateExternalOrderLineItem", e);
    }
  }
  
  public void saveExternalOrderLineItem(JdbcDataConnection dataConnection, RetailTransactionIfc transaction, SaleReturnLineItemIfc lineItem)
    throws DataException
  {
    if ((transaction instanceof SaleReturnTransactionIfc))
    {
      SaleReturnTransactionIfc srTxn = (SaleReturnTransactionIfc)transaction;
      if (srTxn.hasExternalOrder()) {
        try
        {
          insertExternalOrderLineItem(dataConnection, transaction, lineItem);
        }
        catch (DataException e)
        {
          updateExternalOrderLineItem(dataConnection, transaction, lineItem);
        }
      }
    }
  }
  
  protected String getExternalOrderItemID(SaleReturnLineItemIfc lineItem)
  {
    String ret = makeSafeString("-1");
    if (lineItem.isFromExternalOrder()) {
      ret = makeSafeString(lineItem.getExternalOrderItemID());
    }
    return ret;
  }
  
  protected String getExternalOrderParentItemID(SaleReturnLineItemIfc lineItem)
  {
    String ret = "null";
    if (!StringUtils.isBlank(lineItem.getExternalOrderParentItemID())) {
      ret = makeSafeString(lineItem.getExternalOrderParentItemID());
    }
    return ret;
  }
  
  protected String getExternalPricingFlag(SaleReturnLineItemIfc lineItem)
  {
    return makeStringFromBoolean(lineItem.hasExternalPricing());
  }
  
  protected String getExternalOrderItemUpdateFlag(SaleReturnLineItemIfc lineItem)
  {
    return makeStringFromBoolean(lineItem.isExternalOrderItemUpdateSourceFlag());
  }
  
  protected String getMerchandiseHierarchyGroupID(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getPLUItem().getItemClassification().getMerchandiseHierarchyGroup());
  }
  
  protected String getManufacturerItemUPC(SaleReturnLineItemIfc lineItem)
  {
    return makeSafeString(lineItem.getPLUItem().getManufacturerItemUPC());
  }
}
