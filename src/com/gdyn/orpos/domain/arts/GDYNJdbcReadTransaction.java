package com.gdyn.orpos.domain.arts;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.DataTransactionFactory;
import oracle.retail.stores.domain.arts.JdbcPLUOperation;
import oracle.retail.stores.domain.arts.JdbcReadTransaction;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.customer.CustomerInfoIfc;
import oracle.retail.stores.domain.customer.IRSCustomerIfc;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.DiscountTargetIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.discount.PromotionLineItemIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItem;
import oracle.retail.stores.domain.lineitem.ItemPriceIfc;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.lineitem.KitComponentLineItemIfc;
import oracle.retail.stores.domain.lineitem.ReturnItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.registry.RegistryIDIfc;
import oracle.retail.stores.domain.returns.ReturnTenderDataElementIfc;
import oracle.retail.stores.domain.stock.AlterationPLUItemIfc;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.stock.ItemClassificationIfc;
import oracle.retail.stores.domain.stock.KitComponentIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.stock.ProductGroupIfc;
import oracle.retail.stores.domain.stock.UnitOfMeasureIfc;
import oracle.retail.stores.domain.store.StoreIfc;
import oracle.retail.stores.domain.tax.NewTaxRuleIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;
import oracle.retail.stores.domain.tax.TaxRateCalculatorIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.transaction.OrderTransaction;
import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIDIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;
import oracle.retail.stores.domain.utility.CodeConstantsIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.EntryMethod;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.domain.utility.PersonNameIfc;
import oracle.retail.stores.domain.utility.SecurityOverrideIfc;
import oracle.retail.stores.foundation.factory.FoundationObjectFactory;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.device.EncipheredDataIfc;
import oracle.retail.stores.foundation.utility.Util;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc;
import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.taxexempt.GDYNHandleReturnPOJO;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCategory;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

public class GDYNJdbcReadTransaction
  extends JdbcReadTransaction
  implements GDYNARTSDatabaseIfc
{
  private static final long serialVersionUID = -4335057678565257140L;
  private static Logger logger = Logger.getLogger(GDYNJdbcReadTransaction.class);
  private String taxExemptCategory;
  private String taxIdImageName;
  private EYSDate taxIDExpiryDate;
  private String taxExemptBandRegistry;
  private String loyaltyID;
  private String loyaltyEmailID;
  
  
  protected void selectSaleReturnTransaction(JdbcDataConnection dataConnection, SaleReturnTransactionIfc transaction, LocaleRequestor localeRequestor, boolean retrieveStoreCoupons) throws DataException
  {
      if (logger.isDebugEnabled())
      {
          logger.debug("GDYNJdbcReadTransaction.selectSaleReturnTransaction()");
      }
    SQLSelectStatement sql = new SQLSelectStatement();
    
    /*
     * Add Table(s)
     */
    sql.addTable(TABLE_RETAIL_TRANSACTION, ALIAS_RETAIL_TRANSACTION); 
    sql.addTable(TABLE_ADDRESS, ALIAS_ADDRESS);
    sql.addTable(TABLE_RETAIL_STORE, ALIAS_RETAIL_STORE);

    /*
     * Add Columns
     */
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_CUSTOMER_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_IRS_CUSTOMER_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_GIFT_REGISTRY_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SUSPENDED_TRANSACTION_REASON_CODE);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_EMPLOYEE_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SEND_PACKAGE_COUNT);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SEND_CUSTOMER_TYPE);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SEND_CUSTOMER_PHYSICALLY_PRESENT);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_LEVEL_SEND);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_ORDER_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_ENCRYPTED_PERSONAL_ID_NUMBER);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_MASKED_PERSONAL_ID_NUMBER);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_PERSONAL_ID_REQUIRED_TYPE);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_PERSONAL_ID_STATE);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_PERSONAL_ID_COUNTRY);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_AGE_RESTRICTED_DOB);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_EXTERNAL_ORDER_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_EXTERNAL_ORDER_NUMBER);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_CONTRACT_SIGNATURE_REQUIRED_FLAG);
    sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_CITY);
    sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_STATE);
    sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_COUNTRY);
    sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_POSTAL_CODE);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_RETURN_TICKET);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_LEVEL_GIFT_RECEIPT_FLAG);
    // Begin GD-50: CSAT
    // Moises Solis (Starmount) Dec 18, 2012
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SURVEY_CUSTOMER_INVITE_ID);
    sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SURVEY_CUSTOMER_INVITE_URL);
    // End GD-50: CSAT

    /*
     * Add Qualifier(s)
     */
    sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
    sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
    sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_BUSINESS_DAY_DATE + " = "
            + getBusinessDayString(transaction));
    sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_SEQUENCE_NUMBER + " = "
            + getTransactionSequenceNumber(transaction));
    sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_RETAIL_STORE + "."
            + FIELD_RETAIL_STORE_ID);
    sql.addQualifier(ALIAS_RETAIL_STORE + "." + FIELD_PARTY_ID + " = " + ALIAS_ADDRESS + "." + FIELD_PARTY_ID);

    try
    {
	logger.debug(sql.getSQLString()); 
        dataConnection.execute(sql.getSQLString());

        ResultSet rs = (ResultSet) dataConnection.getResult();

        if (!rs.next())
        {
            logger.warn("retail transaction not found!");
            throw new DataException(DataException.NO_DATA, "transaction not found");
        }

        int index = 0;
        String customerId = getSafeString(rs, ++index);
        String irsCustomerId = getSafeString(rs, ++index);
        String giftRegistryID = getSafeString(rs, ++index);
        String suspendReasonCode = getSafeString(rs, ++index);
        String salesAssociateID = getSafeString(rs, ++index);
        int sendPackagesCount = rs.getInt(++index);
        String sendCustomerType = getSafeString(rs, ++index);
        String sendCustomerPhysicallyPresent = getSafeString(rs, ++index);
        String transactionLevelSend = getSafeString(rs, ++index);
        String orderID = getSafeString(rs, ++index);
        String encryptedPersonalIDNumber = getSafeString(rs, ++index);
        String maskedPersonalIDNumber = getSafeString(rs, ++index);
        String personalIDType = getSafeString(rs, ++index);
        String personalIDState = getSafeString(rs, ++index);
        String personalIDCountry = getSafeString(rs, ++index);
        EYSDate ageRestrictedDOB = getEYSDateFromString(rs, ++index);
        String externalOrderID = getSafeString(rs, ++index);
        String externalOrderNumber = getSafeString(rs, ++index);
        boolean requireServiceContractFlag = getBooleanFromString(rs, ++index);
        String storeCity = getSafeString(rs, ++index);
        String storeState = getSafeString(rs, ++index);
        String storeCountry = getSafeString(rs, ++index);
        String storePostalCode = getSafeString(rs, ++index);
        String returnTicket = getSafeString(rs, ++index);
        String transactionGiftReceiptAssigned = getSafeString(rs, ++index);
        // Begin GD-50: CSAT
        // Moises Solis (Starmount) Dec 18, 2012
        String customerSurveryID = getSafeString(rs, ++index);
        String customerSurveryURL = getSafeString(rs, ++index);
        // End GD-50: CSAT

        rs.close();
        // If there is a customer Id associated with the transaction,
        // read the customer information and attach the Customer to the
        // transaction
        if (customerId != null && customerId.length() > 0)
        {
            // linkCustomer(customer)
            boolean isLayawayTransaction = (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_LAYAWAY_INITIATE || transaction
                    .getTransactionType() == TransactionConstantsIfc.TYPE_LAYAWAY_PAYMENT);
            CustomerIfc customer = readCustomer(dataConnection, customerId, isLayawayTransaction, localeRequestor);
            transaction.setCustomer(customer);
            transaction.setCustomerId(customerId);
        }
        if (irsCustomerId != null && irsCustomerId.length() > 0)
        {
            IRSCustomerIfc irsCustomer = readIRSCustomer(dataConnection, irsCustomerId);

            // Read Localized personald ID Code
            irsCustomer.setLocalizedPersonalIDCode(getInitializedLocalizedReasonCode(dataConnection, transaction
                    .getTransactionIdentifier().getStoreID(), irsCustomer.getLocalizedPersonalIDCode().getCode(),
                    CodeConstantsIfc.CODE_LIST_PAT_CUSTOMER_ID_TYPES, localeRequestor));

            transaction.setIRSCustomer(irsCustomer);
        }

        // If there is a default gift registry associated with the
        // transaction, instantiate the GiftRegistry
        if (!(Util.isEmpty(giftRegistryID)))
        {
            RegistryIDIfc registry = instantiateGiftRegistry();
            registry.setID(giftRegistryID);
            transaction.setDefaultRegistry(registry);
        }

        // Read Localized Reason Code
        transaction.setSuspendReason(getInitializedLocalizedReasonCode(dataConnection, transaction
                .getTransactionIdentifier()
                .getStoreID(), suspendReasonCode,
                CodeConstantsIfc.CODE_LIST_TRANSACTION_SUSPEND_REASON_CODES, localeRequestor));

      try
      {
        transaction.setSalesAssociate(getEmployee(dataConnection, salesAssociateID));
      }
      catch (DataException checkEmployeeNotFound)
      {
                // Since empty/not found Sales Associate id could exist in
                // transaction and the
                // sales associate id here is retrieved from particular
                // transaction saved,
                // transaction is set with employee object using the sales
                // associate id
                // retrieved. For error codes other than for not found, data
                // exception is thrown
                if (checkEmployeeNotFound.getErrorCode() == DataException.NO_DATA)
        {
          PersonNameIfc name = DomainGateway.getFactory().getPersonNameInstance();
          EmployeeIfc employee = DomainGateway.getFactory().getEmployeeInstance();
          employee.setEmployeeID(salesAssociateID);
          name.setFirstName(salesAssociateID);
          employee.setPersonName(name);
          transaction.setSalesAssociate(employee);
        }
        else
        {
          throw checkEmployeeNotFound;
        }
      }
     
      TransactionTaxIfc transactionTax = selectTaxLineItem(dataConnection, transaction, 
        getLocaleRequestor(transaction));
            if (transactionTax.getTaxMode() == TaxIfc.TAX_MODE_EXEMPT
                    || transactionTax.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
            {
        selectTaxExemptionModifier(dataConnection, transaction, transactionTax);
      }
      transaction.setTransactionTax(transactionTax);
      if (sendPackagesCount > 0)
      {
        transaction.setSendPackageCount(sendPackagesCount);
        readTransactionShippings(dataConnection, transaction, localeRequestor);
      }
      if (sendCustomerType.equals("0")) {
        transaction.setSendCustomerLinked(true);
      } else {
        transaction.setSendCustomerLinked(false);
      }
      if (sendCustomerPhysicallyPresent.equals("1")) {
        transaction.setCustomerPhysicallyPresent(true);
      } else {
        transaction.setCustomerPhysicallyPresent(false);
      }
      if (transactionLevelSend.equals("1")) {
        transaction.getTransactionTotals().setTransactionLevelSendAssigned(true);
      } else {
        transaction.getTransactionTotals().setTransactionLevelSendAssigned(false);
      }
            // Set the store address information
      transaction.getWorkstation().getStore().getAddress().setCity(storeCity);
      transaction.getWorkstation().getStore().getAddress().setState(storeState);
      transaction.getWorkstation().getStore().getAddress().setCountry(storeCountry);
      transaction.getWorkstation().getStore().getAddress().setPostalCode(storePostalCode);
      
      transaction.setReturnTicket(returnTicket);
      if (transactionGiftReceiptAssigned.equals("1")) {
        transaction.setTransactionGiftReceiptAssigned(true);
      } else {
        transaction.setTransactionGiftReceiptAssigned(false);
      }
            // Set the personal ID information
      if (!Util.isEmpty(maskedPersonalIDNumber))
      {
        CustomerInfoIfc customerInfo = transaction.getCustomerInfo();
        if (customerInfo == null) {
          customerInfo = DomainGateway.getFactory().getCustomerInfoInstance();
        }
                // Read Localized Reason Code
        if (!Util.isEmpty(personalIDType)) {
          customerInfo.setLocalizedPersonalIDType(getInitializedLocalizedReasonCode(dataConnection, 
            transaction
            .getTransactionIdentifier().getStoreID(), personalIDType, 
                            CodeConstantsIfc.CODE_LIST_CHECK_ID_TYPES, localeRequestor));
        }
        EncipheredDataIfc personalIDNumber = FoundationObjectFactory.getFactory()
          .createEncipheredDataInstance(encryptedPersonalIDNumber, maskedPersonalIDNumber);
        customerInfo.setPersonalID(personalIDNumber);
        customerInfo.setPersonalIDState(personalIDState);
        customerInfo.setPersonalIDCountry(personalIDCountry);
        transaction.setCustomerInfo(customerInfo);
      }
            // Set the age restricted DOB
      transaction.setAgeRestrictedDOB(ageRestrictedDOB);
      

            // Set external order info
      transaction.setExternalOrderID(externalOrderID);
      transaction.setExternalOrderNumber(externalOrderNumber);
      transaction.setRequireServiceContractFlag(requireServiceContractFlag);
      

            // Read Transaction Discounts
            TransactionDiscountStrategyIfc[] transactionDiscounts;
            transactionDiscounts = selectDiscountLineItems(dataConnection, transaction, localeRequestor);
      transaction.addTransactionDiscounts(transactionDiscounts);
      

            // Read sale return line items

      SaleReturnLineItemIfc[] lineItems = selectSaleReturnLineItems(dataConnection, transaction, localeRequestor, 
        retrieveStoreCoupons);
            if (transaction instanceof OrderTransaction &&
                    transaction.getTransactionStatus() != TransactionConstantsIfc.STATUS_SUSPENDED)// logic added to
            // eliminate kitItem.
      {
                ArrayList<SaleReturnLineItemIfc> arrayOfLineItem = new ArrayList<SaleReturnLineItemIfc>();
                for (int i = 0; i < lineItems.length; i++)
                {
                    if (!(lineItems[i].getPLUItem().isKitHeader()))
                    {
            arrayOfLineItem.add(lineItems[i]);
          }
        }
        SaleReturnLineItemIfc[] pdoLineItems = new SaleReturnLineItemIfc[arrayOfLineItem.size()];
        arrayOfLineItem.toArray(pdoLineItems);
        lineItems = pdoLineItems;
      }
      SaleReturnLineItemIfc[] deletedLineItems = selectDeletedSaleReturnLineItems(dataConnection, transaction, 
        localeRequestor);
      
      if ((transaction.getTransactionTax() != null) && 
        ((transaction.getTransactionTax().getTaxMode() == TaxIfc.TAX_MODE_EXEMPT) || transaction.getTransactionTax().getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)) {
        setPLUItemsTaxRules(lineItems);
      }
      
      transaction.setLineItems(lineItems);
      if (deletedLineItems != null) {
        if (deletedLineItems.length > 0) {
          for (int i = 0; i < deletedLineItems.length; i++) {
            transaction.addDeletedLineItems(deletedLineItems[i]);
          }
        }
      }
            // if the transaction is an order transaction
            if (transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_CANCEL
                    || transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_PARTIAL
                    || transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_COMPLETE
                    || transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_INITIATE)
      {
        OrderTransactionIfc orderTransaction = (OrderTransactionIfc)transaction;
        orderTransaction.setOrderID(orderID);
        orderTransaction.getOrderStatus().setTrainingModeFlag(transaction.isTrainingMode());
        selectOrderStatusForTransaction(dataConnection, orderTransaction);
        selectOrderLineItemsByRef(dataConnection, transaction);
        selectDeliveryDetails(dataConnection, transaction);
        selectRecipientDetail(dataConnection, transaction);
      }
            // Read tender line items
      TenderLineItemIfc[] tenderLineItems = selectTenderLineItems(dataConnection, transaction);
      transaction.setTenderLineItems(tenderLineItems);
            // Read tenders for return items in the trans
      if (transaction.hasReturnItems())
      {
        ReturnTenderDataElementIfc[] returnTenders = readReturnTenders(dataConnection, transaction);
        transaction.appendReturnTenderElements(returnTenders);
      }
            // Begin GD-50: CSAT
            // Moises Solis (Starmount) Dec 18, 2012
            if (transaction instanceof GDYNSaleReturnTransactionIfc)
      {
        GDYNSaleReturnTransactionIfc txn = (GDYNSaleReturnTransactionIfc)transaction;
        txn.setSurveyCustomerInviteID(customerSurveryID);
        txn.setSurveyCustomerInviteURL(customerSurveryURL);
        
    	logger.debug("txn.getEmployeeDiscountID() "+txn.getEmployeeDiscountID());
        
    	/*code changes  added by Dharmendra on 22/08/2016 to fix issue POS-209*/
			if (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_SALE && txn.getEmployeeDiscountID()!=null) {
				logger.debug("before calling readTransactionEepDtls for transaction id "+transaction.getTransactionID());				
				readTransactionEepDtls(dataConnection, transaction);
				
			}
			
        //Added by Monica to fix CO return retrieval issue for Loyalty Registration
        if (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_SALE ) 
        {
        Vector<SaleReturnLineItemIfc>  lineItemsVector  =  transaction.getItemContainerProxy().getLineItemsVector();
        logger.info("discount details of transaction "+transaction.getTransactionID());
        for(SaleReturnLineItemIfc srli: lineItemsVector){
        	logger.info("Item "+srli.getPLUItemID()+" with line number "+srli.getLineNumber()+" discount amount "+srli.getItemPrice().getItemDiscountAmount().getDecimalValue()
        	+" discount total "+srli.getItemPrice().getItemDiscountTotal().getDecimalValue());
        }
        	
        logger.info("Entered Loyalty Transaction");
        selectLoyaltyTransaction(dataConnection, txn);
        logger.info("Loyalty ID" +txn.getLoyaltyID());
        }
      }
            // End GD-50: CSAT
    }
    catch (DataException de)
    {
            logger.error("" + de + "");
      throw de;
    }
    catch (SQLException se)
    {
      dataConnection.logSQLException(se, "retail transaction table");
            throw new DataException(DataException.SQL_ERROR, "retail transaction table", se);
    }
    catch (Exception e)
    {
            logger.error("" + Util.throwableToString(e) + "");
            throw new DataException(DataException.UNKNOWN, "retail transaction table", e);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectSaleReturnTransaction()");
    }
  }
    /**
     * Get the tax information for an individual line item.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param transaction
     *            the retail transaction
     * @param lineItemSequenceNumber
     * @return The Tax Information
     * @throws DataException
     */
    protected TaxInformationIfc[] selectSaleReturnLineItemTaxInformation(JdbcDataConnection dataConnection,
            SaleReturnTransactionIfc transaction, int lineItemSequenceNumber) throws DataException
    {
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectSaleReturnLineItemTaxInformation()");
    }
    SQLSelectStatement sql = new SQLSelectStatement();
    
        sql.addTable(TABLE_SALE_RETURN_TAX_LINE_ITEM, ALIAS_SALE_RETURN_TAX_LINE_ITEM);

        sql.setDistinctFlag(true);
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_SALE_RETURN_TAX_AMOUNT); // MO_TX_RTN_SLS
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_AUTHORITY_ID); // ID_ATHY_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_GROUP_ID); // ID_GP_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_TYPE); // TY_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_HOLIDAY); // FL_TX_HDY
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_RULE_NAME); // NM_RU_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_PERCENTAGE); // PE_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_SALE_RETURN_TAX_AMOUNT); // MO_TX_RTN_SLS
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAXABLE_SALE_RETURN_AMOUNT); // MO_TXBL_RTN_SLS
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_UNIQUE_ID); // ID_UNQ
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_MODE); // TX_MOD
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_FLG_TAX_INCLUSIVE); // FL_TX_INC

        // Tax Exemptions
        sql.addColumn(FIELD_TAX_ID_IMAGE_NAME);
        sql.addColumn(FIELD_TAX_EXEMPT_BAND_COUNCIL_REGISTRY);
        sql.addColumn(FIELD_TAX_ID_EXPIRY_DATE);
        sql.addColumn(FIELD_CATEGORY_CODE);

        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_RETAIL_STORE_ID + "=" + getStoreID(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_WORKSTATION_ID + " = "
                + getWorkstationID(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_BUSINESS_DAY_DATE + " = "
                + getBusinessDayString(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_TRANSACTION_SEQUENCE_NUMBER + " = "
                + getTransactionSequenceNumber(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER
                + " = " + lineItemSequenceNumber);

        ArrayList<TaxInformationIfc> taxInfoList = new ArrayList<TaxInformationIfc>();
    try
    {
      dataConnection.execute(sql.getSQLString());
      ResultSet rs = (ResultSet)dataConnection.getResult();
      while (rs.next())
      {
        TaxInformationIfc taxInformation = DomainGateway.getFactory().getTaxInformationInstance();
        int index = 0;
        taxInformation.setTaxAmount(getCurrencyFromDecimal(rs, ++index));
        taxInformation.setTaxAuthorityID(rs.getInt(++index));
        taxInformation.setTaxGroupID(rs.getInt(++index));
        taxInformation.setTaxTypeCode(rs.getInt(++index));
        taxInformation.setTaxHoliday(getBooleanFromString(rs, ++index));
        taxInformation.setTaxRuleName(getSafeString(rs, ++index));
        
                BigDecimal perc = getBigDecimal(rs, ++index, TAX_PERCENTAGE_SCALE);
        taxInformation.setTaxPercentage(perc);
        
        taxInformation.setTaxAmount(getCurrencyFromDecimal(rs, ++index));
        taxInformation.setTaxableAmount(getCurrencyFromDecimal(rs, ++index));
        taxInformation.setUniqueID(getSafeString(rs, ++index));
        taxInformation.setTaxMode(rs.getInt(++index));
        taxInformation.setInclusiveTaxFlag(getBooleanFromString(rs, ++index));
        taxInfoList.add(taxInformation);
        

        this.taxIdImageName = getSafeString(rs, ++index);
        this.taxExemptBandRegistry = getSafeString(rs, ++index);
        this.taxIDExpiryDate = getEYSDateFromString(rs, ++index);
        this.taxExemptCategory = getSafeString(rs, ++index);
      }
      rs.close();
            // Tax Exemptions
      if (transaction.getTransactionTax() != null)
      {
        TransactionTaxIfc transactionTax = transaction.getTransactionTax();
                if (transactionTax instanceof GDYNTransactionTaxIfc)
        {
          GDYNTransactionTaxIfc transactionTaxExemptions = (GDYNTransactionTaxIfc)transactionTax;
          transactionTaxExemptions.setBandRegistryId(this.taxExemptBandRegistry);
          transactionTaxExemptions.setIdExpirationDate(this.taxIDExpiryDate);
          String taxArea = transaction.getWorkstation().getStore().getAddress().getState();

          populateTaxExemption(dataConnection, 
            (GDYNTransactionTaxIfc)transactionTax, 
            this.taxExemptCategory, 
            this.taxIdImageName, getStoreID(transaction), taxArea);
        }
      }
    }
    catch (SQLException se)
    {
            throw new DataException(DataException.SQL_ERROR, "selectSaleReturnLineItemTaxByTaxAuthority", se);
    }
    catch (DataException de)
    {
      throw de;
    }
    catch (Exception e)
    {
            throw new DataException(DataException.UNKNOWN, "selectSaleReturnLineItemTaxByTaxAuthority", e);
    }
    TaxInformationIfc[] results = new TaxInformationIfc[taxInfoList.size()];
    for (int i = 0; i < results.length; i++) {
      results[i] = ((TaxInformationIfc)taxInfoList.get(i));
    }
    return results;
  }
  
    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param tax
     * @param categoryCode
     * @param imageName
     * @throws DataException
     * @throws SQLException 
     */
  protected void populateTaxExemption(JdbcDataConnection dataConnection, GDYNTransactionTaxIfc tax, String categoryCode, 
		  String imageName, String storeNum, String taxArea)
    throws DataException, SQLException
  {
        Locale locale = LocaleMap.getLocale(LocaleConstantsIfc.DATABASE);
    String countryCode = locale.getCountry();
    
    // For issue 681146- Getting methods
    GDYNHandleReturnPOJO searchObj = new GDYNHandleReturnPOJO();
    searchObj.setTaxExemptCategoryCode(categoryCode);
    searchObj.setTxnTax(tax);
    searchObj.setStoreNum(storeNum);
    searchObj.setPopulate(true);
        
  //Ashwini For issue 681146: Regular with with no tax exempt will have categoryCode as null
    if(searchObj.getTaxExemptCategoryCode() != null && searchObj.getTaxExemptCategoryCode().isEmpty() == false)
    {  
    	GDYNTaxExemptDataTransaction dataTransaction = (GDYNTaxExemptDataTransaction) DataTransactionFactory.create(GDYNDataTransactionKeys.TAX_EXEMPT_DATA_TRANSACTION);
    	GDYNTransactionTaxIfc taxNew = (GDYNTransactionTaxIfc) dataTransaction.getCustomerCategoriesCat(searchObj);

    	tax.setCustomerCategory(taxNew.getCustomerCategory());

    } else {
    	GDYNJdbcReadTaxExemptCustomer customerOp = 
    			new GDYNJdbcReadTaxExemptCustomer();

    	GDYNTaxExemptCustomerCategory category = 
    			(GDYNTaxExemptCustomerCategory)customerOp.selectCategoryByCode(
    					dataConnection, categoryCode, countryCode, taxArea, locale, true);

    	tax.setCustomerCategory(category);
    }
    
    if ((imageName != null) && (!imageName.isEmpty()))
    {
      GDYNJdbcReadTaxExemptIdImage imageOp = new GDYNJdbcReadTaxExemptIdImage();
      
      GDYNTaxExemptIdImage idImage = 
        (GDYNTaxExemptIdImage)imageOp.selectIdImageByName(
        dataConnection, imageName, countryCode);
      
      tax.setTaxExemptIdImage(idImage);
    }
  }
    // Begin GD-49: Develop Employee Discount Module
    // lcatania (Starmount) Mar 7, 2013
    /**
     * Reads from the retail price modifier table.
     *
     * @param dataConnection the connection to the data source
     * @param transaction the transaction coming from business logic
     * @param lineItem the sale/return line item
     * @param localeRequestor the requested locales
     * @return Array of discount strategies
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    protected ItemDiscountStrategyIfc[] selectRetailPriceModifiers(JdbcDataConnection dataConnection,
            TransactionIfc transaction, SaleReturnLineItemIfc lineItem, LocaleRequestor localeRequestor)
            throws DataException
    {

        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcReadTransaction.selectRetailPriceModifiers()");

        SQLSelectStatement sql = new SQLSelectStatement();
        /*
         * Add Table(s)
         */
        sql.addTable(TABLE_RETAIL_PRICE_MODIFIER);
        /*
         * Add Column(s)
         */
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_PERCENT);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER);
        sql.addColumn(FIELD_PRICE_DERIVATION_RULE_METHOD_CODE);
        sql.addColumn(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DAMAGE_DISCOUNT);
        sql.addColumn(FIELD_PCD_INCLUDED_IN_BEST_DEAL);
        sql.addColumn(FIELD_ADVANCED_PRICING_RULE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID_TYPE_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_TYPE_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_EMPLOYEE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_ENTRY_METHOD_CODE);
        sql.addColumn(FIELD_PROMOTION_ID);
        sql.addColumn(FIELD_PROMOTION_COMPONENT_ID);
        sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID);

        /*
         * Add Qualifier(s)
         */
        sql.addQualifier(FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
        sql.addQualifier(FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
        sql.addQualifier(FIELD_BUSINESS_DAY_DATE + " = " + getBusinessDayString(transaction));
        sql.addQualifier(FIELD_TRANSACTION_SEQUENCE_NUMBER + " = " + getTransactionSequenceNumber(transaction));
        sql.addQualifier(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER + " = " + lineItem.getLineNumber());
        /*
         * Add Ordering
         */
        sql.addOrdering(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER + " ASC");

        Vector<ItemDiscountStrategyIfc> itemDiscounts = new Vector<ItemDiscountStrategyIfc>();
        String reasonCodeString = "";
        try
        {
            dataConnection.execute(sql.getSQLString());
            logger.debug("GDYNJdbcReadTransaction.selectRetailPriceModifiers() SQL:  " + sql.getSQLString());
            ResultSet rs = (ResultSet)dataConnection.getResult();

            while (rs.next())
            {
                int index = 0;
                int ruleID = rs.getInt(++index);
                reasonCodeString = getSafeString(rs, ++index);
                BigDecimal percent = getBigDecimal(rs, ++index);
                // CurrencyIfc amount = getCurrencyFromDecimal(rs, ++index);
                CurrencyIfc amount = getLongerCurrencyFromDecimal(rs, ++index);
                index = index + 1;
                int methodCode = rs.getInt(++index);
                int assignmentBasis = rs.getInt(++index);
                String discountEmployeeID = getSafeString(rs, ++index);
                boolean isDamageDiscount = getBooleanFromString(rs, ++index);
                boolean isIncludedInBestDealFlag = getBooleanFromString(rs, ++index);
                boolean isAdvancedPricingRuleFlag = getBooleanFromString(rs, ++index);
                String referenceID = rs.getString(++index);
                String referenceIDCodeStr = getSafeString(rs, ++index);
                int typeCode = rs.getInt(++index);
                int accountingCode = rs.getInt(++index);
                String overrideEmployeeID = getSafeString(rs, ++index);
                int overrideEntryMethod = rs.getInt(++index);
                int promotionId = rs.getInt(++index);
                int promotionComponentId = rs.getInt(++index);
                int promotionComponentDetailId = rs.getInt(++index);

                LocalizedCodeIfc localizedCode = DomainGateway.getFactory().getLocalizedCode();

                // Determine type
                if (ruleID == 0) // price override
                {
                    localizedCode = getInitializedLocalizedReasonCode(dataConnection, transaction.getTransactionIdentifier()
                            .getStoreID(), reasonCodeString, CodeConstantsIfc.CODE_LIST_PRICE_OVERRIDE_REASON_CODES,
                            localeRequestor);
                    lineItem.modifyItemPrice(amount, localizedCode);
                    if (!Util.isEmpty(overrideEmployeeID))
                    {
                        SecurityOverrideIfc override = DomainGateway.getFactory().getSecurityOverrideInstance();
                        override.setAuthorizingEmployee(overrideEmployeeID);
                        override.setEntryMethod(EntryMethod.getEntryMethod(overrideEntryMethod));
                        lineItem.getItemPrice().setPriceOverrideAuthorization(override);
                    }
                }
                else
                // item discount
                {
                    // Determine type of discount
                    ItemDiscountStrategyIfc itemDiscount = null;
                    String ruleIDString = Integer.toString(ruleID);
                    String codeListType = CodeConstantsIfc.CODE_LIST_ITEM_DISCOUNT_BY_AMOUNT;
                    if (isDamageDiscount)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_DAMAGE_DISCOUNT_REASON_CODES;
                    }
                    else if (assignmentBasis == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_EMPLOYEE_DISCOUNT_REASON_CODES;
                    }
                    else if (isAdvancedPricingRuleFlag)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_ADVANCED_PRICING_REASON_CODES;
                    }
                    else if (accountingCode == DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_MARKDOWN_AMOUNT_REASON_CODES;
                    }

                    switch (methodCode)
                    {
                        case DISCOUNT_METHOD_PERCENTAGE:
                        {

                            if (isDamageDiscount)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_DAMAGE_DISCOUNT_REASON_CODES;
                            }
                            else if (assignmentBasis == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_EMPLOYEE_DISCOUNT_REASON_CODES;
                            }
                            else if (isAdvancedPricingRuleFlag)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_ADVANCED_PRICING_REASON_CODES;
                            }
                            else if (accountingCode == DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_MARKDOWN_PERCENT_REASON_CODES;
                            }
                            else
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_ITEM_DISCOUNT_BY_PERCENTAGE;
                            }
            itemDiscount = DomainGateway.getFactory().getItemDiscountByPercentageInstance();
            itemDiscount.setRuleID(ruleIDString);
            itemDiscount.setDiscountRate(percent.movePointLeft(2));
            localizedCode = getLocalizedReasonCode(dataConnection, transaction
              .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType, 
              localeRequestor, ruleIDString);
            if (localizedCode != null)
            {
              itemDiscount.setLocalizedNames(localizedCode.getText());
            }
            else
            {
              localizedCode = DomainGateway.getFactory().getLocalizedCode();
              localizedCode.setCode(reasonCodeString);
            }
            itemDiscount.setReason(localizedCode);
            
            itemDiscount.setAssignmentBasis(assignmentBasis);
            itemDiscount.setDiscountEmployee(discountEmployeeID);
            
            itemDiscount.setDamageDiscount(isDamageDiscount);
            itemDiscount.setTypeCode(typeCode);
            itemDiscount.setAccountingMethod(accountingCode);
            break;
                        }
                        case DISCOUNT_METHOD_AMOUNT:
                        {
                            if (amount.signum() == CurrencyIfc.POSITIVE || amount.signum() == CurrencyIfc.ZERO)
            {
              itemDiscount = DomainGateway.getFactory().getItemDiscountByAmountInstance();
              itemDiscount.setDiscountAmount(amount);
              itemDiscount.setRuleID(ruleIDString);
              localizedCode = getLocalizedReasonCode(dataConnection, transaction
                .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType, 
                localeRequestor, ruleIDString);
              if (localizedCode != null)
              {
                itemDiscount.setLocalizedNames(localizedCode.getText());
              }
              else
              {
                localizedCode = DomainGateway.getFactory().getLocalizedCode();
                localizedCode.setCode(reasonCodeString);
              }
              itemDiscount.setReason(localizedCode);
              
              itemDiscount.setAssignmentBasis(assignmentBasis);
              itemDiscount.setDiscountEmployee(discountEmployeeID);
              
              itemDiscount.setDamageDiscount(isDamageDiscount);
              itemDiscount.setTypeCode(typeCode);
              itemDiscount.setAccountingMethod(accountingCode);
            }
                            else if (amount.signum() == CurrencyIfc.NEGATIVE)
                            {
                                itemDiscount = DomainGateway.getFactory()
                                        .getReturnItemTransactionDiscountAuditInstance();
              itemDiscount.setDiscountAmount(amount);
              itemDiscount.setRuleID(ruleIDString);
                                itemDiscount.setDiscountMethod(DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT);
              localizedCode = getLocalizedReasonCode(dataConnection, transaction
                .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType, 
                localeRequestor, ruleIDString);
                                // we do not have a way to distinguish between
                                // manual item discounts and markdowns, so if
                                // above call failed try
                                // to get the reason codes for the markdowns
              if (localizedCode == null) {
                localizedCode = getLocalizedReasonCode(dataConnection, transaction
                  .getTransactionIdentifier().getStoreID(), reasonCodeString, 
                                            CodeConstantsIfc.CODE_LIST_MARKDOWN_AMOUNT_REASON_CODES, localeRequestor,
                  ruleIDString);
              }
              if (localizedCode != null)
                                {// discount names and reason code names are the
                                 // same, so set it here for manual discounts.
                                 // for
                                 // adv. pricing rule, we already retieved
                                 // the localized names through jdbc plu
                                 // operation
                itemDiscount.setLocalizedNames(localizedCode.getText());
              }
              else
              {
                localizedCode = DomainGateway.getFactory().getLocalizedCode();
                localizedCode.setCode(reasonCodeString);
              }
              itemDiscount.setReason(localizedCode);
              
              itemDiscount.setAssignmentBasis(assignmentBasis);
              itemDiscount.setDiscountEmployee(discountEmployeeID);
              
              itemDiscount.setDamageDiscount(isDamageDiscount);
              itemDiscount.setTypeCode(typeCode);
              itemDiscount.setAccountingMethod(accountingCode);
            }
            break;
                        }
                        case DISCOUNT_METHOD_FIXED_PRICE:
                        {
            itemDiscount = DomainGateway.getFactory().getItemDiscountByFixedPriceStrategyInstance();
            itemDiscount.setDiscountAmount(amount);
            itemDiscount.setRuleID(ruleIDString);
            localizedCode = getLocalizedReasonCode(dataConnection, transaction
              .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType, 
              localeRequestor, ruleIDString);
            if (localizedCode != null)
                            {// discount names and reason code names are the
                             // same, so set it here for manual discounts. for
                             // adv. pricing rule, we already retieved the
                             // localized names through jdbc plu operation
              itemDiscount.setLocalizedNames(localizedCode.getText());
              localizedCode.setCode(reasonCodeString);
            }
            else
            {
              localizedCode = DomainGateway.getFactory().getLocalizedCode();
              localizedCode.setCode(reasonCodeString);
            }
            itemDiscount.setReason(localizedCode);
            
            itemDiscount.setAssignmentBasis(assignmentBasis);
            itemDiscount.setTypeCode(typeCode);
            itemDiscount.setAccountingMethod(accountingCode);
                            break;
                        }
                    }// end switch methodCode

                    // ReferenceID and TypeCode
          if (itemDiscount != null)
          {
            itemDiscount.setReferenceID(referenceID);
            if (referenceIDCodeStr == null) {
              itemDiscount.setReferenceIDCode(0);
            } else {
              for (int i = 0; i < DiscountRuleConstantsIfc.REFERENCE_ID_TYPE_CODE.length; i++) {
                if (referenceIDCodeStr.equalsIgnoreCase(DiscountRuleConstantsIfc.REFERENCE_ID_TYPE_CODE[i])) {
                  itemDiscount.setReferenceIDCode(i);
                }
              }
            }
            itemDiscount.setAdvancedPricingRule(isAdvancedPricingRuleFlag);
            if (isAdvancedPricingRuleFlag) {
              ((DiscountTargetIfc)lineItem).applyAdvancedPricingDiscount(itemDiscount);
            }
            itemDiscount.setIncludedInBestDeal(isIncludedInBestDealFlag);
            

                        // Set Temporary Price Change Promotion IDs
            itemDiscount.setPromotionId(promotionId);
            itemDiscount.setPromotionComponentId(promotionComponentId);
            itemDiscount.setPromotionComponentDetailId(promotionComponentDetailId);
            
            itemDiscounts.addElement(itemDiscount);
          }
          else
                    // itemDiscount == null
          {
                        logger.error("Unknown type of itemDiscount:  reasonCode=" + reasonCodeString
                                + " percent=" + percent + " amount=" + amount + "");
          }
        }
            }// end while (rs.next())
      rs.close();
    }
    catch (SQLException exc)
    {
      dataConnection.logSQLException(exc, "Processing result set.");
            throw new DataException(DataException.SQL_ERROR, "selectRetailPriceModifiers", exc);
    }
        // put vector into array
        ItemDiscountStrategyIfc[] discounts = null;
    int numDiscounts = itemDiscounts.size();
    if (numDiscounts > 0)
    {
      discounts = new ItemDiscountStrategyIfc[numDiscounts];
      itemDiscounts.copyInto(discounts);
      setDiscountEmployeeIDOnTransaction(discounts, transaction, dataConnection);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectRetailPriceModifiers()");
    }
        return (discounts);
  }
  
    /**
     * 
     * GDYNJdbcReadTransaction
     * void
     * 
     * @param transaction
     * @param discountEmployee
     * @throws DataException 
     */
  protected void setDiscountEmployeeIDOnTransaction(ItemDiscountStrategyIfc[] discounts, TransactionIfc transaction, JdbcDataConnection dataConnection)
    throws DataException
  {
    if ((discounts != null) && (discounts.length > 0)) {
            // find the percent discount stategy that is a discount.
      for (int j = 0; j < discounts.length; j++)
      {
        ItemDiscountStrategyIfc itemDiscount = discounts[j];
        String discountEmployee = itemDiscount.getDiscountEmployeeID();
        EmployeeIfc employee = getEmployee(dataConnection, discountEmployee);
        if ((!Util.isEmpty(employee.getEmployeeID())) && ((transaction instanceof SaleReturnTransactionIfc)) && 
          (((SaleReturnTransactionIfc)transaction).getEmployeeDiscountID() == null))
        {
          ((SaleReturnTransactionIfc)transaction).setEmployeeDiscountID(employee.getEmployeeID());
          if ((transaction instanceof GDYNSaleReturnTransactionIfc)) {
        	  /*code changes added by Dharmendra to fix POS-195 on 16/08/2016
        	   null value check for person before retrieving the full name of the employee*/ 
							if (employee != null
									&& employee.getPersonName() != null
									&& employee.getPersonName().getFullName() != null
									&& !"".equalsIgnoreCase(employee
											.getPersonName().getFullName())) {
								((GDYNSaleReturnTransactionIfc) transaction)
										.setEmployeeDiscountName(employee
												.getPersonName().getFullName());
							}
						}
        }
      }
    }
  
    logger.debug("setDiscountEmployeeIDOnTransaction method exited");
  }
  
    /**
     * 
     * GDYNJdbcReadTransaction
     * EmployeeIfc
     * 
     * @param connection
     * @param rs
     * @param index
     * @return
     * @throws DataException
     */
  protected EmployeeIfc readEmployee(JdbcDataConnection connection, ResultSet rs, int index)
    throws DataException
  {
    EmployeeIfc employee = DomainGateway.getFactory().getEmployeeInstance();
    PersonNameIfc employeeName = DomainGateway.getFactory().getPersonNameInstance();
    try
    {
      employee.setEmployeeID(getSafeString(rs, ++index));
      employeeName.setFullName(getSafeString(rs, ++index));
      employeeName.setLastName(getSafeString(rs, ++index));
      employeeName.setFirstName(getSafeString(rs, ++index));
      employeeName.setMiddleName(getSafeString(rs, ++index));
      employee.setPersonName(employeeName);
    }
    catch (SQLException e)
    {
            ((JdbcDataConnection) connection).logSQLException(
        e, 
        "Processing result set.");
      throw new DataException(
                    DataException.SQL_ERROR,
        "An SQL Error occurred proccessing the result set from reading an employee in GDYNJdbcReadTransaction.", 
        e);
    }
    catch (NumberFormatException e)
    {
      logger.error("Error occurred reading employee information.", e);
      throw new DataException(
                    DataException.DATA_FORMAT,
        "Found an unexpected numeric data format in GDYNJdbcReadTransaction.", 
        e);
    }
    return employee;
  }
  
  protected SaleReturnLineItemIfc[] selectSaleReturnLineItems(JdbcDataConnection dataConnection, SaleReturnTransactionIfc transaction, LocaleRequestor localeRequestor, boolean retrieveStoreCoupons)
    throws DataException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectSaleReturnLineItems()");
    }
    SQLSelectStatement sql = new SQLSelectStatement();
    


    sql.addTable("TR_LTM_SLS_RTN", "SRLI");
    sql.addTable("TR_LTM_RTL_TRN", "RTLI");
    


    sql.addColumn("SRLI.ID_REGISTRY");
    sql.addColumn("SRLI.ID_ITM_POS");
    sql.addColumn("SRLI.ID_ITM");
    sql.addColumn("SRLI.QU_ITM_LM_RTN_SLS");
    sql.addColumn("SRLI.MO_EXTN_LN_ITM_RTN");
    sql.addColumn("SRLI.MO_VAT_LN_ITM_RTN");
    sql.addColumn("SRLI.MO_TAX_INC_LN_ITM_RTN");
    sql.addColumn("SRLI.AI_LN_ITM");
    sql.addColumn("SRLI.ID_NMB_SRZ");
    sql.addColumn("SRLI.QU_ITM_LN_RTN");
    sql.addColumn("SRLI.ID_TRN_ORG");
    sql.addColumn("SRLI.DC_DY_BSN_ORG");
    sql.addColumn("SRLI.AI_LN_ITM_ORG");
    sql.addColumn("SRLI.ID_STR_RT_ORG");
    sql.addColumn("SRLI.FL_RTN_MR");
    sql.addColumn("SRLI.RC_RTN_MR");
    sql.addColumn("SRLI.MO_FE_RSTK");
    sql.addColumn("SRLI.LU_KT_ST");
    sql.addColumn("SRLI.ID_CLN");
    sql.addColumn("SRLI.LU_KT_HDR_RFN_ID");
    sql.addColumn("SRLI.FL_SND");
    sql.addColumn("SRLI.CNT_SND_LAB");
    sql.addColumn("SRLI.FL_RCV_GF");
    sql.addColumn("SRLI.OR_ID_REF");
    sql.addColumn("SRLI.LU_MTH_ID_ENR");
    sql.addColumn("SRLI.ED_SZ");
    sql.addColumn("RTLI.FL_VD_LN_ITM");
    sql.addColumn("SRLI.FL_ITM_PRC_ADJ");
    sql.addColumn("SRLI.LU_PRC_ADJ_RFN_ID");
    sql.addColumn("SRLI.FL_RLTD_ITM_RTN");
    sql.addColumn("SRLI.AI_LN_ITM_RLTD");
    sql.addColumn("SRLI.FL_RLTD_ITM_RM");
    sql.addColumn("SRLI.FL_RTRVD_TRN");
    sql.addColumn("SRLI.FL_SLS_ASSC_MDF");
    sql.addColumn("SRLI.MO_PRN_PRC");
    sql.addColumn("SRLI.DE_ITM_SHRT_RCPT");
    sql.addColumn("SRLI.DE_ITM_LCL");
    sql.addColumn("SRLI.FL_FE_RSTK");
    sql.addColumn("SRLI.LU_HRC_MR_LV");
    
    sql.addColumn("SRLI.FL_ITM_SZ_REQ");
    sql.addColumn("SRLI.LU_UOM_SLS");
    sql.addColumn("SRLI.ID_DPT_POS");
    sql.addColumn("SRLI.TY_ITM");
    sql.addColumn("SRLI.FL_RTN_PRH");
    sql.addColumn("SRLI.FL_DSC_EM_ALW");
    
    sql.addColumn("SRLI.ID_GP_TX");
    sql.addColumn("SRLI.FL_TX");
    sql.addColumn("SRLI.FL_ITM_DSC");
    sql.addColumn("SRLI.FL_ITM_DSC_DMG");
    sql.addColumn("SRLI.ID_MRHRC_GP");
    
    sql.addColumn("SRLI.ID_ITM_MF_UPC");
    




    sql.addQualifier("SRLI.ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("SRLI.ID_WS = " + 
      getWorkstationID(transaction));
    sql.addQualifier("SRLI.DC_DY_BSN = " + 
      getBusinessDayString(transaction));
    sql.addQualifier("SRLI.AI_TRN = " + 
      getTransactionSequenceNumber(transaction));
    sql.addQualifier("RTLI.ID_STR_RT=SRLI.ID_STR_RT");
    
    sql.addQualifier("RTLI.ID_WS = SRLI.ID_WS");
    
    sql.addQualifier("RTLI.DC_DY_BSN = SRLI.DC_DY_BSN");
    
    sql.addQualifier("RTLI.AI_TRN = SRLI.AI_TRN");
    
    sql.addQualifier("RTLI.AI_LN_ITM = SRLI.AI_LN_ITM");
    


    sql.addOrdering("SRLI.AI_LN_ITM ASC");
    

    Vector<SaleReturnLineItemIfc> saleReturnLineItems = new Vector();
    try
    {
      dataConnection.execute(sql.getSQLString());
      ResultSet rs = (ResultSet)dataConnection.getResult();
      TransactionTaxIfc transactionTax = transaction.getTransactionTax();
      
      HashMap<Integer, String> reasonCodeMap = new HashMap();
      while (rs.next())
      {
        int index = 0;
        String giftRegistryID = getSafeString(rs, ++index);
        String posItemID = getSafeString(rs, ++index);
        String itemID = getSafeString(rs, ++index);
        BigDecimal quantity = getBigDecimal(rs, ++index);
        CurrencyIfc amount = getCurrencyFromDecimal(rs, ++index);
        index++;
        index++;
        int sequenceNumber = rs.getInt(++index);
        String serialNumber = getSafeString(rs, ++index);
        BigDecimal quantityReturned = getBigDecimal(rs, ++index);
        String originalTransactionID = getSafeString(rs, ++index);
        EYSDate originalTransactionBusinessDay = getEYSDateFromString(rs, ++index);
        int originalTransactionLineNumber = rs.getInt(++index);
        String originalStoreID = getSafeString(rs, ++index);
        boolean returnFlag = getBooleanFromString(rs, ++index);
        String returnReasonCode = getSafeString(rs, ++index);
        CurrencyIfc restockingFee = getCurrencyFromDecimal(rs, ++index);
        int kitCode = rs.getInt(++index);
        String itemKitID = getSafeString(rs, ++index);
        int kitReference = rs.getInt(++index);
        String sendFlag = getSafeString(rs, ++index);
        int sendLabelCount = rs.getInt(++index);
        String giftReceiptStr = getSafeString(rs, ++index);
        int orderLineReference = rs.getInt(++index);
        String entryMethodCode = getSafeString(rs, ++index);
        String sizeCode = getSafeString(rs, ++index);
        String itemVoidFlag = getSafeString(rs, ++index);
        boolean isPriceAdjLineItem = rs.getBoolean(++index);
        int priceAdjReferenceID = rs.getInt(++index);
        boolean returnRelatedItemFlag = rs.getBoolean(++index);
        int relatedSeqNumber = rs.getInt(++index);
        boolean deleteRelatedItemFlag = rs.getBoolean(++index);
        boolean retrievedFlag = rs.getBoolean(++index);
        boolean saleAsscModifiedFlag = getBooleanFromString(rs, ++index);
        CurrencyIfc beforeOverride = getCurrencyFromDecimal(rs, ++index);
        String receiptDescription = getSafeString(rs, ++index);
        Locale receiptDescriptionLocale = LocaleUtilities.getLocaleFromString(getSafeString(rs, ++index));
        boolean restockingFeeFlag = rs.getBoolean(++index);
        String productGroupID = getSafeString(rs, ++index);
        boolean sizeRequiredFlag = rs.getBoolean(++index);
        String unitOfMeasureCode = getSafeString(rs, ++index);
        String posDepartmentID = getSafeString(rs, ++index);
        int itemTypeID = rs.getInt(++index);
        boolean returnEligible = !rs.getBoolean(++index);
        boolean employeeDiscountEligible = rs.getBoolean(++index);
        int taxGroupId = rs.getInt(++index);
        boolean taxable = rs.getBoolean(++index);
        boolean discountable = rs.getBoolean(++index);
        boolean damageDiscountable = rs.getBoolean(++index);
        String merchandiseHierarchyGroupID = getSafeString(rs, ++index);
        String manufacturerItemUPC = getSafeString(rs, ++index);
        
        CurrencyIfc lineItemTaxAmount = DomainGateway.getBaseCurrencyInstance(BigDecimal.ZERO);
        CurrencyIfc lineItemIncTaxAmount = DomainGateway.getBaseCurrencyInstance(BigDecimal.ZERO);
        

        ItemPriceIfc price = DomainGateway.getFactory().getItemPriceInstance();
        ItemTaxIfc itemTax = DomainGateway.getFactory().getItemTaxInstance();
        price.setExtendedSellingPrice(amount);
        price.setDiscountEligible(discountable);
        price.setExtendedRestockingFee(restockingFee);
        if (quantity.signum() != 0)
        {
          amount = amount.divide(new BigDecimal(quantity.toString()));
          if (restockingFee != null) {
            restockingFee = restockingFee.divide(new BigDecimal(quantity.toString()));
          }
        }
        price.setSellingPrice(amount);
        price.setPermanentSellingPrice(beforeOverride);
        price.setRestockingFee(restockingFee);
        

        itemTax = price.getItemTax();
        itemTax.setDefaultRate(transactionTax.getDefaultRate());
        itemTax.setDefaultTaxRules(transactionTax.getDefaultTaxRules());
        itemTax.setItemTaxAmount(lineItemTaxAmount);
        itemTax.setItemInclusiveTaxAmount(lineItemIncTaxAmount);
        



        itemTax.setTaxMode(-1);
        price.setItemTax(itemTax);
        

        price.setItemTaxAmount(lineItemTaxAmount);
        price.setItemInclusiveTaxAmount(lineItemIncTaxAmount);
        

        SaleReturnLineItemIfc lineItem;
        
        if ((transaction.getTransactionType() == 25) || 
          (transaction.getTransactionType() == 24) || 
          (transaction.getTransactionType() == 26) || 
          (transaction.getTransactionType() == 23))
        {
          lineItem = DomainGateway.getFactory().getOrderLineItemInstance();
        }
        else
        {          
          switch (kitCode)
          {
          case 1: 
            lineItem = DomainGateway.getFactory().getKitHeaderLineItemInstance();
            break;
          case 2: 
              lineItem = DomainGateway.getFactory().getKitComponentLineItemInstance();
            ((KitComponentLineItemIfc)lineItem).setItemKitID(itemKitID);
            break;
          default: 
            lineItem = DomainGateway.getFactory().getSaleReturnLineItemInstance();
          }
        }
        
        lineItem.setPLUItemID(itemID);
        lineItem.setItemPrice(price);
        lineItem.setItemTaxAmount(lineItemTaxAmount);
        lineItem.setItemInclusiveTaxAmount(lineItemIncTaxAmount);
        lineItem.modifyItemQuantity(quantity);
        lineItem.setLineNumber(sequenceNumber);
        

        lineItem.setOrderLineReference(orderLineReference);
        
        lineItem.setReceiptDescription(receiptDescription);
        lineItem.setReceiptDescriptionLocale(receiptDescriptionLocale);
        lineItem.getItemPrice().setEmployeeDiscountEligible(employeeDiscountEligible);
        

        lineItem.setKitHeaderReference(kitReference);
        if ((serialNumber != null) && (serialNumber.length() > 0)) {
          lineItem.setItemSerial(serialNumber);
        }
        lineItem.setQuantityReturned(quantityReturned);
        if (giftRegistryID.length() > 0)
        {
          RegistryIDIfc registry = instantiateGiftRegistry();
          registry.setID(giftRegistryID);
          lineItem.modifyItemRegistry(registry, true);
        }
        if (returnFlag)
        {
          ReturnItemIfc ri = DomainGateway.getFactory().getReturnItemInstance();
          if ((originalTransactionID != null) && (originalTransactionID.length() > 0))
          {
            TransactionIDIfc id = DomainGateway.getFactory().getTransactionIDInstance();
            id.setTransactionID(originalTransactionID);
            ri.setOriginalTransactionID(id);
            ri.setHaveReceipt(true);
          }
          if (originalTransactionBusinessDay != null) {
            ri.setOriginalTransactionBusinessDate(originalTransactionBusinessDay);
          }
          ri.setOriginalLineNumber(originalTransactionLineNumber);
          



          reasonCodeMap.put(Integer.valueOf(sequenceNumber), returnReasonCode);
          if (originalStoreID.equals(transaction.getWorkstation().getStoreID()))
          {
            ri.setStore(transaction.getWorkstation().getStore());
          }
          else
          {
            StoreIfc store = DomainGateway.getFactory().getStoreInstance();
            store.setStoreID(originalStoreID);
            ri.setStore(store);
          }
          if (transaction.getTransactionStatus() == 4) {
            ri.setFromRetrievedTransaction(retrievedFlag);
          }
          lineItem.setReturnItem(ri);
        }
        if (transaction.getSalesAssociate() != null) {
          lineItem.setSalesAssociate(transaction.getSalesAssociate());
        } else {
          lineItem.setSalesAssociate(transaction.getCashier());
        }
        if (transaction.getTransactionType() != 2)
        {
          if (sendFlag.equals("0")) {
            lineItem.setItemSendFlag(false);
          } else if (sendFlag.equals("1")) {
            lineItem.setItemSendFlag(true);
          }
          lineItem.setSendLabelCount(sendLabelCount);
        }
        if (giftReceiptStr.equals("1")) {
          lineItem.setGiftReceiptItem(true);
        }
        lineItem.setPriceAdjustmentReference(priceAdjReferenceID);
        lineItem.setIsPriceAdjustmentLineItem(isPriceAdjLineItem);
        

        EntryMethod entryMethod = EntryMethod.Manual;
        if (!Util.isEmpty(entryMethodCode)) {
          for (EntryMethod code : EntryMethod.values()) {
            if ((entryMethod.equals(code.getIxRetailCode())) || 
              (entryMethod.equals(String.valueOf(code.getLegacyCode()))))
            {
              entryMethod = code;
              break;
            }
          }
        }
        lineItem.setEntryMethod(entryMethod);
        lineItem.setItemSizeCode(sizeCode);
        if (!itemVoidFlag.equals("1")) {
          saleReturnLineItems.addElement(lineItem);
        }
        lineItem.setRelatedItemReturnable(returnRelatedItemFlag);
        lineItem.setRelatedItemSequenceNumber(relatedSeqNumber);
        lineItem.setRelatedItemDeleteable(deleteRelatedItemFlag);
        lineItem.setSalesAssociateModifiedFlag(saleAsscModifiedFlag);
        

        PLUItemIfc pluItem = instantiatePLUItem(productGroupID, kitCode, transaction.isTrainingMode());
        pluItem.setItemID(itemID);
        pluItem.setPosItemID(posItemID);
        pluItem.setItemSizeRequired(sizeRequiredFlag);
        pluItem.setDepartmentID(posDepartmentID);
        pluItem.setTaxable(taxable);
        pluItem.setTaxGroupID(taxGroupId);
        pluItem.setManufacturerItemUPC(manufacturerItemUPC);
        if (lineItem.isKitComponent()) {
          ((KitComponentIfc)pluItem).setItemKitID(((KitComponentLineItemIfc)lineItem).getItemKitID());
        }
        ItemClassificationIfc itemClassification = DomainGateway.getFactory().getItemClassificationInstance();
        itemClassification.setRestockingFeeFlag(restockingFeeFlag);
        ProductGroupIfc pg = DomainGateway.getFactory().getProductGroupInstance();
        pg.setGroupID(productGroupID);
        itemClassification.setGroup(pg);
        itemClassification.setItemType(itemTypeID);
        itemClassification.setReturnEligible(returnEligible);
        itemClassification.setEmployeeDiscountAllowedFlag(employeeDiscountEligible);
        itemClassification.setDiscountEligible(discountable);
        itemClassification.setDamageDiscountEligible(damageDiscountable);
        itemClassification.setMerchandiseHierarchyGroup(merchandiseHierarchyGroupID);
        pluItem.setItemClassification(itemClassification);
        pluItem.setSellingPrice(lineItem.getItemPrice().getPermanentSellingPrice());
        UnitOfMeasureIfc pluUOM = DomainGateway.getFactory().getUnitOfMeasureInstance();
        pluUOM.setUnitID(unitOfMeasureCode);
        pluItem.setUnitOfMeasure(pluUOM);
        selectOptionalI18NPLUData(dataConnection, pluItem, localeRequestor, lineItem);
        lineItem.setPLUItem(pluItem);
      }
      for (int lineItemCounter = 0; lineItemCounter < saleReturnLineItems.size(); lineItemCounter++) {
        if (((SaleReturnLineItemIfc)saleReturnLineItems.get(lineItemCounter)).isReturnLineItem())
        {
          int sequenceNumber = ((SaleReturnLineItemIfc)saleReturnLineItems.get(lineItemCounter)).getLineNumber();
          

          String reasonCode = (String)reasonCodeMap.get(Integer.valueOf(sequenceNumber));
          


          ((SaleReturnLineItemIfc)saleReturnLineItems.get(lineItemCounter))
            .getReturnItem()
            .setReason(
            getInitializedLocalizedReasonCode(dataConnection, transaction
            .getTransactionIdentifier().getStoreID(), 
            reasonCode, "ReturnReasonCodes", localeRequestor));
        }
      }
      for (int k = 0; k < saleReturnLineItems.size(); k++) {
        if (((SaleReturnLineItemIfc)saleReturnLineItems.get(k)).getRelatedItemSequenceNumber() != -1) {
          for (int l = 0; l < saleReturnLineItems.size(); l++) {
            if (((SaleReturnLineItemIfc)saleReturnLineItems.get(l))
              .getRelatedItemSequenceNumber() == -1) {
              if (((SaleReturnLineItemIfc)saleReturnLineItems.get(k))
                .getRelatedItemSequenceNumber() == 
                ((SaleReturnLineItemIfc)saleReturnLineItems.get(l)).getLineNumber()) {
                if (((SaleReturnLineItemIfc)saleReturnLineItems.get(l))
                  .getRelatedItemLineItems() != null)
                {
                  SaleReturnLineItemIfc[] relatedItms = 
                    ((SaleReturnLineItemIfc)saleReturnLineItems.get(l)).getRelatedItemLineItems();
                  SaleReturnLineItemIfc[] newRelatedItms = new SaleReturnLineItem[relatedItms.length + 1];
                  newRelatedItms[relatedItms.length] = 
                    ((SaleReturnLineItemIfc)saleReturnLineItems.get(k));
                  
                  ((SaleReturnLineItemIfc)saleReturnLineItems.get(l))
                    .setRelatedItemLineItems(newRelatedItms);
                }
                else
                {
                  SaleReturnLineItemIfc[] relatedItms = new SaleReturnLineItem[1];
                  relatedItms[0] = ((SaleReturnLineItemIfc)saleReturnLineItems.get(k));
                  ((SaleReturnLineItemIfc)saleReturnLineItems.get(l))
                    .setRelatedItemLineItems(relatedItms);
                }
              }
            }
          }
        }
      }
      int lineItemSequenceNumber;
      for (int i = 0; i < saleReturnLineItems.size(); i++)
      {
        SaleReturnLineItemIfc srli = (SaleReturnLineItemIfc)saleReturnLineItems.elementAt(i);
        lineItemSequenceNumber = srli.getLineNumber();
        TaxInformationIfc[] taxInfoArray = selectSaleReturnLineItemTaxInformation(dataConnection, transaction, 
          lineItemSequenceNumber);
        TaxInformationContainerIfc container = DomainGateway.getFactory().getTaxInformationContainerInstance();
        for (int j = 0; j < taxInfoArray.length; j++)
        {
          container.addTaxInformation(taxInfoArray[j]);
          srli.getItemPrice().getItemTax().setTaxMode(taxInfoArray[j].getTaxMode());
          if (srli.getReturnItem() != null) {
            srli.getReturnItem().setTaxRate(
              taxInfoArray[j].getTaxPercentage().movePointLeft(2).doubleValue());
          }
        }
        if (((transactionTax instanceof GDYNTransactionTaxIfc)) && 
          (((GDYNTransactionTaxIfc)transactionTax).getCustomerCategory() != null)) {
          ((GDYNItemTaxIfc)srli.getItemPrice().getItemTax()).setTaxExemptCustomerCode(((GDYNTransactionTaxIfc)transactionTax).getCustomerCategory()
            .getCustomerCode());
        }
        srli.getItemPrice().getItemTax().setTaxInformationContainer(container);
        CurrencyIfc[] taxAmount = selectSaleReturnLineItemTaxAmount(dataConnection, transaction, 
          lineItemSequenceNumber);
        srli.setItemTaxAmount(taxAmount[0]);
        srli.setItemInclusiveTaxAmount(taxAmount[1]);
        if (transaction.getTransactionStatus() == 4)
        {
          if ((srli.getReturnItem() != null) && (srli.getReturnItem().isFromRetrievedTransaction())) {
            srli.setFromTransaction(true);
          } else {
            srli.setFromTransaction(false);
          }
        }
        else {
          srli.setFromTransaction(true);
        }
      }
      ItemDiscountStrategyIfc[] itemDiscounts = (ItemDiscountStrategyIfc[])null;
      for (SaleReturnLineItemIfc lineItem : saleReturnLineItems)
      {
        PLUItemIfc pluItem = null;
        if (transaction.getTransactionStatus() == 4) {
          pluItem = selectPLUItemForSuspenedLineItem(dataConnection, transaction, lineItem, 
            saleReturnLineItems, 
            localeRequestor, retrieveStoreCoupons);
        }
        if (pluItem == null)
        {
          pluItem = lineItem.getPLUItem();
          new JdbcPLUOperation().getItemLevelMessages(dataConnection, pluItem);
        }
        if ((lineItem.isKitComponent()) && ((pluItem instanceof KitComponentIfc)))
        {
          ((KitComponentIfc)pluItem).setKitComponent(true);
          pluItem.setItemID(lineItem.getPLUItemID());
          ((KitComponentIfc)pluItem).setItemKitID(((KitComponentLineItemIfc)lineItem).getItemKitID());
        }
        if ((pluItem instanceof GiftCardPLUItemIfc)) {
          selectGiftCard(dataConnection, transaction, lineItem.getLineNumber(), (GiftCardPLUItemIfc)pluItem, 
            lineItem);
        }
        if ((pluItem instanceof AlterationPLUItemIfc))
        {
          lineItem.setAlterationItemFlag(true);
          AlterationPLUItemIfc altItem = (AlterationPLUItemIfc)pluItem;
          altItem.setPrice(lineItem.getSellingPrice());
          selectAlteration(dataConnection, transaction, lineItem.getLineNumber(), altItem);
        }
        lineItem.setPLUItem(pluItem);
        lineItem.getItemTax().setTaxGroupId(pluItem.getTaxGroupID());
        if (lineItem.getReturnItem() != null)
        {
          lineItem.getReturnItem().setPLUItem(pluItem);
          lineItem.getReturnItem().setPrice(pluItem.getPrice());
        }
        int sequenceNumber = lineItem.getLineNumber();
        try
        {
          String employeeID = selectCommissionModifier(dataConnection, transaction, sequenceNumber);
          String transactionLevelSalesAssociateEmployeeID = lineItem.getSalesAssociate().getEmployeeID();
          lineItem.setSalesAssociate(getEmployee(dataConnection, employeeID));
          if (!transactionLevelSalesAssociateEmployeeID.equals(employeeID)) {
            lineItem.setSalesAssociateModifiedAtLineItem(true);
          }
        }
        catch (DataException localDataException) {}
        itemDiscounts = selectRetailPriceModifiers(dataConnection, transaction, lineItem, localeRequestor);
        lineItem.getItemPrice().setItemDiscounts(itemDiscounts);
        

        PromotionLineItemIfc[] promotionLineItems = selectPromotionLineItems(dataConnection, transaction, 
          lineItem);
        lineItem.getItemPrice().setPromotionLineItems(promotionLineItems);
        

        ItemTaxIfc tax = selectSaleReturnTaxModifier(dataConnection, transaction, lineItem, localeRequestor);
        if (tax != null)
        {
          if (lineItem.getItemTaxMethod() == 1)
          {
            tax.setItemTaxAmount(lineItem.getItemPrice().getItemTaxAmount());
            tax.setItemInclusiveTaxAmount(lineItem.getItemPrice().getItemInclusiveTaxAmount());
          }
          lineItem.getItemPrice().setItemTax(tax);
          if (lineItem.getReturnItem() != null) {
            lineItem.getReturnItem().setTaxRate(tax.getDefaultRate());
          }
        }
        if (lineItem.getItemPrice().getItemTax().getTaxMode() == -1) {
          if (pluItem.getTaxable()) {
            lineItem.getItemPrice().getItemTax().setTaxMode(0);
          } else {
            lineItem.getItemPrice().getItemTax().setTaxMode(6);
          }
        }
        selectExternalOrderLineItem(dataConnection, transaction, lineItem);
        
        lineItem.getItemPrice().calculateItemTotal();
      }
    }
    catch (SQLException exc)
    {
      dataConnection.logSQLException(exc, "Processing result set.");
      throw new DataException(1, "error processing sale return line items", exc);
    }
    associateKitComponents(saleReturnLineItems);
    
    int numItems = saleReturnLineItems.size();
    SaleReturnLineItemIfc[] lineItems = new SaleReturnLineItemIfc[numItems];
    saleReturnLineItems.copyInto(lineItems);
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectSaleReturnLineItems");
    }
    return lineItems;
  }
  
  protected SaleReturnLineItemIfc[] selectSaleReturnLineItems(JdbcDataConnection dataConnection, SaleReturnTransactionIfc transaction, LocaleRequestor localeRequestor)
    throws DataException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectSaleReturnLineItems()");
    }
    SQLSelectStatement sql = new SQLSelectStatement();
    


    sql.addTable("TR_LTM_SLS_RTN", "SRLI");
    sql.addTable("TR_LTM_RTL_TRN", "RTLI");
    



    sql.addColumn("SRLI.ID_REGISTRY");
    sql.addColumn("SRLI.ID_ITM_POS");
    sql.addColumn("SRLI.ID_ITM");
    sql.addColumn("SRLI.QU_ITM_LM_RTN_SLS");
    sql.addColumn("SRLI.MO_EXTN_LN_ITM_RTN");
    sql.addColumn("SRLI.MO_VAT_LN_ITM_RTN");
    sql.addColumn("SRLI.MO_TAX_INC_LN_ITM_RTN");
    sql.addColumn("SRLI.AI_LN_ITM");
    sql.addColumn("SRLI.ID_NMB_SRZ");
    sql.addColumn("SRLI.QU_ITM_LN_RTN");
    sql.addColumn("SRLI.ID_TRN_ORG");
    sql.addColumn("SRLI.DC_DY_BSN_ORG");
    sql.addColumn("SRLI.AI_LN_ITM_ORG");
    sql.addColumn("SRLI.ID_STR_RT_ORG");
    sql.addColumn("SRLI.FL_RTN_MR");
    sql.addColumn("SRLI.RC_RTN_MR");
    sql.addColumn("SRLI.MO_FE_RSTK");
    sql.addColumn("SRLI.LU_KT_ST");
    sql.addColumn("SRLI.ID_CLN");
    sql.addColumn("SRLI.LU_KT_HDR_RFN_ID");
    sql.addColumn("SRLI.FL_SND");
    sql.addColumn("SRLI.CNT_SND_LAB");
    sql.addColumn("SRLI.FL_RCV_GF");
    sql.addColumn("SRLI.OR_ID_REF");
    sql.addColumn("SRLI.LU_MTH_ID_ENR");
    sql.addColumn("SRLI.ED_SZ");
    sql.addColumn("RTLI.FL_VD_LN_ITM");
    sql.addColumn("SRLI.FL_ITM_PRC_ADJ");
    sql.addColumn("SRLI.LU_PRC_ADJ_RFN_ID");
    sql.addColumn("SRLI.FL_RLTD_ITM_RTN");
    sql.addColumn("SRLI.AI_LN_ITM_RLTD");
    sql.addColumn("SRLI.FL_RLTD_ITM_RM");
    sql.addColumn("SRLI.FL_SLS_ASSC_MDF");
    sql.addColumn("SRLI.MO_PRN_PRC");
    sql.addColumn("SRLI.DE_ITM_SHRT_RCPT");
    sql.addColumn("SRLI.DE_ITM_LCL");
    sql.addColumn("SRLI.FL_FE_RSTK");
    sql.addColumn("SRLI.LU_HRC_MR_LV");
    
    sql.addColumn("SRLI.FL_ITM_SZ_REQ");
    sql.addColumn("SRLI.LU_UOM_SLS");
    sql.addColumn("SRLI.ID_DPT_POS");
    sql.addColumn("SRLI.TY_ITM");
    sql.addColumn("SRLI.FL_RTN_PRH");
    sql.addColumn("SRLI.FL_DSC_EM_ALW");
    
    sql.addColumn("SRLI.ID_GP_TX");
    sql.addColumn("SRLI.FL_TX");
    sql.addColumn("SRLI.FL_ITM_DSC");
    sql.addColumn("SRLI.FL_ITM_DSC_DMG");
    sql.addColumn("SRLI.ID_MRHRC_GP");
    
    sql.addColumn("SRLI.ID_ITM_MF_UPC");
    




    sql.addQualifier("SRLI.ID_STR_RT = " + getStoreID(transaction));
    sql.addQualifier("SRLI.ID_WS = " + 
      getWorkstationID(transaction));
    sql.addQualifier("SRLI.DC_DY_BSN = " + 
      getBusinessDayString(transaction));
    sql.addQualifier("SRLI.AI_TRN = " + 
      getTransactionSequenceNumber(transaction));
    sql.addQualifier("RTLI.ID_STR_RT=SRLI.ID_STR_RT");
    
    sql.addQualifier("RTLI.ID_WS = SRLI.ID_WS");
    
    sql.addQualifier("RTLI.DC_DY_BSN = SRLI.DC_DY_BSN");
    
    sql.addQualifier("RTLI.AI_TRN = SRLI.AI_TRN");
    
    sql.addQualifier("RTLI.AI_LN_ITM = SRLI.AI_LN_ITM");
    


    sql
      .addOrdering("SRLI.AI_LN_ITM ASC");
    

    Vector<SaleReturnLineItemIfc> saleReturnLineItems = new Vector();
    try
    {
      dataConnection.execute(sql.getSQLString());
      ResultSet rs = (ResultSet)dataConnection.getResult();
      TransactionTaxIfc transactionTax = transaction.getTransactionTax();
      
      HashMap<Integer, String> reasonCodeMap = new HashMap();
      while (rs.next())
      {
        int index = 0;
        String giftRegistryID = getSafeString(rs, ++index);
        String posItemID = getSafeString(rs, ++index);
        String itemID = getSafeString(rs, ++index);
        BigDecimal quantity = getBigDecimal(rs, ++index);
        CurrencyIfc amount = getCurrencyFromDecimal(rs, ++index);
        index++;
        


        index++;
        int sequenceNumber = rs.getInt(++index);
        String serialNumber = getSafeString(rs, ++index);
        BigDecimal quantityReturned = getBigDecimal(rs, ++index);
        String originalTransactionID = getSafeString(rs, ++index);
        EYSDate originalTransactionBusinessDay = getEYSDateFromString(rs, ++index);
        int originalTransactionLineNumber = rs.getInt(++index);
        String originalStoreID = getSafeString(rs, ++index);
        boolean returnFlag = getBooleanFromString(rs, ++index);
        String returnReasonCode = getSafeString(rs, ++index);
        CurrencyIfc restockingFee = getCurrencyFromDecimal(rs, ++index);
        int kitCode = rs.getInt(++index);
        String itemKitID = getSafeString(rs, ++index);
        int kitReference = rs.getInt(++index);
        String sendFlag = getSafeString(rs, ++index);
        int sendLabelCount = rs.getInt(++index);
        String giftReceiptStr = getSafeString(rs, ++index);
        int orderLineReference = rs.getInt(++index);
        String entryMethod = getSafeString(rs, ++index);
        String sizeCode = getSafeString(rs, ++index);
        String itemVoidFlag = getSafeString(rs, ++index);
        boolean isPriceAdjLineItem = rs.getBoolean(++index);
        int priceAdjReferenceID = rs.getInt(++index);
        boolean returnRelatedItemFlag = rs.getBoolean(++index);
        int relatedSeqNumber = rs.getInt(++index);
        boolean deleteRelatedItemFlag = rs.getBoolean(++index);
        boolean saleAsscModifiedFlag = getBooleanFromString(rs, ++index);
        CurrencyIfc beforeOverride = getCurrencyFromDecimal(rs, ++index);
        String receiptDescription = getSafeString(rs, ++index);
        Locale receiptDescriptionLocale = LocaleUtilities.getLocaleFromString(getSafeString(rs, ++index));
        boolean restockingFeeFlag = rs.getBoolean(++index);
        String productGroupID = getSafeString(rs, ++index);
        boolean sizeRequiredFlag = rs.getBoolean(++index);
        String unitOfMeasureCode = getSafeString(rs, ++index);
        String posDepartmentID = getSafeString(rs, ++index);
        int itemTypeID = rs.getInt(++index);
        boolean returnEligible = !rs.getBoolean(++index);
        boolean employeeDiscountEligible = rs.getBoolean(++index);
        int taxGroupId = rs.getInt(++index);
        boolean taxable = rs.getBoolean(++index);
        boolean discountable = rs.getBoolean(++index);
        boolean damageDiscountable = rs.getBoolean(++index);
        String merchandiseHierarchyGroupID = getSafeString(rs, ++index);
        String manufacturerItemUPC = getSafeString(rs, ++index);
        
        CurrencyIfc lineItemTaxAmount = DomainGateway.getBaseCurrencyInstance(BigDecimal.ZERO);
        CurrencyIfc lineItemIncTaxAmount = DomainGateway.getBaseCurrencyInstance(BigDecimal.ZERO);
        

        ItemPriceIfc price = DomainGateway.getFactory().getItemPriceInstance();
        ItemTaxIfc itemTax = DomainGateway.getFactory().getItemTaxInstance();
        price.setExtendedSellingPrice(amount);
        price.setDiscountEligible(discountable);
        price.setExtendedRestockingFee(restockingFee);
        if (quantity.signum() != 0)
        {
          amount = amount.divide(new BigDecimal(quantity.toString()));
          if (restockingFee != null) {
            restockingFee = restockingFee.divide(new BigDecimal(quantity.toString()));
          }
        }
        price.setSellingPrice(amount);
        price.setPermanentSellingPrice(beforeOverride);
        price.setRestockingFee(restockingFee);
        

        itemTax = price.getItemTax();
        itemTax.setDefaultRate(transactionTax.getDefaultRate());
        itemTax.setDefaultTaxRules(transactionTax.getDefaultTaxRules());
        itemTax.setItemTaxAmount(lineItemTaxAmount);
        itemTax.setItemInclusiveTaxAmount(lineItemIncTaxAmount);
        itemTax.setTaxMode(-1);
        price.setItemTax(itemTax);
        

        price.setItemTaxAmount(lineItemTaxAmount);
        price.setItemTaxAmount(lineItemIncTaxAmount);
        

        price.setItemQuantity(quantity);
        SaleReturnLineItemIfc lineItem;
        
        if ((transaction.getTransactionType() == 25) || 
          (transaction.getTransactionType() == 24) || 
          (transaction.getTransactionType() == 26) || 
          (transaction.getTransactionType() == 23))
        {
          lineItem = DomainGateway.getFactory().getOrderLineItemInstance();
        }
        else
        {
          
          switch (kitCode)
          {
          case 1: 
            lineItem = DomainGateway.getFactory().getKitHeaderLineItemInstance();
            break;
          case 2: 
              lineItem = DomainGateway.getFactory().getKitComponentLineItemInstance();
            ((KitComponentLineItemIfc)lineItem).setItemKitID(itemKitID);
            break;
          default: 
            lineItem = DomainGateway.getFactory().getSaleReturnLineItemInstance();
          }
        }
        
        lineItem.setPLUItemID(itemID);
        lineItem.setItemPrice(price);
        lineItem.setItemTaxAmount(lineItemTaxAmount);
        lineItem.setItemInclusiveTaxAmount(lineItemIncTaxAmount);
        lineItem.modifyItemQuantity(quantity);
        lineItem.setLineNumber(sequenceNumber);
        lineItem.setItemSizeCode(sizeCode);
        

        lineItem.setOrderLineReference(orderLineReference);
        
        lineItem.setReceiptDescription(receiptDescription);
        lineItem.setReceiptDescriptionLocale(receiptDescriptionLocale);
        lineItem.getItemPrice().setEmployeeDiscountEligible(employeeDiscountEligible);
        

        lineItem.setKitHeaderReference(kitReference);
        if ((serialNumber != null) && (serialNumber.length() > 0)) {
          lineItem.setItemSerial(serialNumber);
        }
        lineItem.setQuantityReturned(quantityReturned);
        if (giftRegistryID.length() > 0)
        {
          RegistryIDIfc registry = instantiateGiftRegistry();
          registry.setID(giftRegistryID);
          lineItem.modifyItemRegistry(registry, true);
        }
        if (returnFlag)
        {
          ReturnItemIfc ri = DomainGateway.getFactory().getReturnItemInstance();
          if ((originalTransactionID != null) && (originalTransactionID.length() > 0))
          {
            TransactionIDIfc id = DomainGateway.getFactory().getTransactionIDInstance();
            id.setTransactionID(originalTransactionID);
            ri.setOriginalTransactionID(id);
          }
          if (originalTransactionBusinessDay != null) {
            ri.setOriginalTransactionBusinessDate(originalTransactionBusinessDay);
          }
          ri.setOriginalLineNumber(originalTransactionLineNumber);
          



          reasonCodeMap.put(Integer.valueOf(sequenceNumber), returnReasonCode);
          if (originalStoreID.equals(transaction.getWorkstation().getStoreID()))
          {
            ri.setStore(transaction.getWorkstation().getStore());
          }
          else
          {
            StoreIfc store = DomainGateway.getFactory().getStoreInstance();
            store.setStoreID(originalStoreID);
            ri.setStore(store);
          }
          lineItem.setReturnItem(ri);
        }
        if (transaction.getSalesAssociate() != null) {
          lineItem.setSalesAssociate(transaction.getSalesAssociate());
        } else {
          lineItem.setSalesAssociate(transaction.getCashier());
        }
        if (transaction.getTransactionType() != 2)
        {
          if (sendFlag.equals("0")) {
            lineItem.setItemSendFlag(false);
          } else if (sendFlag.equals("1")) {
            lineItem.setItemSendFlag(true);
          }
          lineItem.setSendLabelCount(sendLabelCount);
        }
        if (giftReceiptStr.equals("1")) {
          lineItem.setGiftReceiptItem(true);
        }
        lineItem.setPriceAdjustmentReference(priceAdjReferenceID);
        lineItem.setIsPriceAdjustmentLineItem(isPriceAdjLineItem);
        if (!itemVoidFlag.equals("1")) {
          saleReturnLineItems.addElement(lineItem);
        }
        lineItem.setRelatedItemReturnable(returnRelatedItemFlag);
        lineItem.setRelatedItemSequenceNumber(relatedSeqNumber);
        lineItem.setRelatedItemDeleteable(deleteRelatedItemFlag);
        lineItem.setSalesAssociateModifiedFlag(saleAsscModifiedFlag);
        

        PLUItemIfc pluItem = instantiatePLUItem(productGroupID, kitCode, transaction.isTrainingMode());
        pluItem.setItemID(itemID);
        pluItem.setPosItemID(posItemID);
        pluItem.setItemSizeRequired(sizeRequiredFlag);
        pluItem.setDepartmentID(posDepartmentID);
        pluItem.setTaxable(taxable);
        pluItem.setTaxGroupID(taxGroupId);
        pluItem.setManufacturerItemUPC(manufacturerItemUPC);
        ItemClassificationIfc itemClassification = DomainGateway.getFactory().getItemClassificationInstance();
        itemClassification.setRestockingFeeFlag(restockingFeeFlag);
        ProductGroupIfc pg = DomainGateway.getFactory().getProductGroupInstance();
        pg.setGroupID(productGroupID);
        itemClassification.setGroup(pg);
        itemClassification.setItemType(itemTypeID);
        itemClassification.setReturnEligible(returnEligible);
        itemClassification.setEmployeeDiscountAllowedFlag(employeeDiscountEligible);
        itemClassification.setDiscountEligible(discountable);
        itemClassification.setDamageDiscountEligible(damageDiscountable);
        itemClassification.setMerchandiseHierarchyGroup(merchandiseHierarchyGroupID);
        pluItem.setItemClassification(itemClassification);
        pluItem.setSellingPrice(lineItem.getItemPrice().getPermanentSellingPrice());
        UnitOfMeasureIfc pluUOM = DomainGateway.getFactory().getUnitOfMeasureInstance();
        pluUOM.setUnitID(unitOfMeasureCode);
        pluItem.setUnitOfMeasure(pluUOM);
        selectOptionalI18NPLUData(dataConnection, pluItem, localeRequestor, lineItem);
        lineItem.setPLUItem(pluItem);
      }
      rs.close();
      for (int lineItemCounter = 0; lineItemCounter < saleReturnLineItems.size(); lineItemCounter++) {
        if (((SaleReturnLineItemIfc)saleReturnLineItems.get(lineItemCounter)).isReturnLineItem())
        {
          int sequenceNumber = ((SaleReturnLineItemIfc)saleReturnLineItems.get(lineItemCounter)).getLineNumber();
          

          String reasonCode = (String)reasonCodeMap.get(Integer.valueOf(sequenceNumber));
          


          ((SaleReturnLineItemIfc)saleReturnLineItems.get(lineItemCounter))
            .getReturnItem()
            .setReason(
            getInitializedLocalizedReasonCode(dataConnection, transaction
            .getTransactionIdentifier().getStoreID(), 
            reasonCode, "ReturnReasonCodes", localeRequestor));
        }
      }
      int lineItemSequenceNumber;
      for (int i = 0; i < saleReturnLineItems.size(); i++)
      {
        SaleReturnLineItemIfc srli = (SaleReturnLineItemIfc)saleReturnLineItems.elementAt(i);
        lineItemSequenceNumber = srli.getLineNumber();
        TaxInformationIfc[] taxInfoArray = selectSaleReturnLineItemTaxInformation(dataConnection, transaction, 
          lineItemSequenceNumber);
        TaxInformationContainerIfc container = DomainGateway.getFactory().getTaxInformationContainerInstance();
        for (int j = 0; j < taxInfoArray.length; j++)
        {
          container.addTaxInformation(taxInfoArray[j]);
          srli.getItemPrice().getItemTax().setTaxMode(taxInfoArray[j].getTaxMode());
        }
        srli.getItemPrice().getItemTax().setTaxInformationContainer(container);
        if (((transactionTax instanceof GDYNTransactionTaxIfc)) && 
          (((GDYNTransactionTaxIfc)transactionTax).getCustomerCategory() != null)) {
          ((GDYNItemTaxIfc)srli.getItemPrice().getItemTax()).setTaxExemptCustomerCode(((GDYNTransactionTaxIfc)transactionTax).getCustomerCategory()
            .getCustomerCode());
        }
        CurrencyIfc[] taxAmount = selectSaleReturnLineItemTaxAmount(dataConnection, transaction, 
          lineItemSequenceNumber);
        srli.setItemTaxAmount(taxAmount[0]);
        
        srli.setItemInclusiveTaxAmount(taxAmount[1]);
        


        srli.setFromTransaction(true);
      }
      ItemDiscountStrategyIfc[] itemDiscounts = (ItemDiscountStrategyIfc[])null;
      for (SaleReturnLineItemIfc lineItem : saleReturnLineItems)
      {
        PLUItemIfc pluItem = null;
        if (transaction.getTransactionStatus() == 4) {
          pluItem = selectPLUItemForSuspenedLineItem(dataConnection, transaction, lineItem, 
            saleReturnLineItems, 
            localeRequestor, false);
        }
        if (pluItem == null)
        {
          pluItem = lineItem.getPLUItem();
          new JdbcPLUOperation().getItemLevelMessages(dataConnection, pluItem);
        }
        if ((lineItem.isKitComponent()) && (pluItem.isKitComponent()))
        {
          pluItem.setItemID(lineItem.getPLUItemID());
          ((KitComponentIfc)pluItem).setItemKitID(((KitComponentLineItemIfc)lineItem).getItemKitID());
        }
        if ((pluItem instanceof GiftCardPLUItemIfc)) {
          selectGiftCard(dataConnection, transaction, lineItem.getLineNumber(), (GiftCardPLUItemIfc)pluItem, 
            lineItem);
        }
        if ((pluItem instanceof AlterationPLUItemIfc))
        {
          lineItem.setAlterationItemFlag(true);
          AlterationPLUItemIfc altItem = (AlterationPLUItemIfc)pluItem;
          altItem.setPrice(lineItem.getSellingPrice());
          selectAlteration(dataConnection, transaction, lineItem.getLineNumber(), altItem);
        }
        lineItem.setPLUItem(pluItem);
        lineItem.getItemTax().setTaxGroupId(pluItem.getTaxGroupID());
        if (lineItem.getReturnItem() != null)
        {
          lineItem.getReturnItem().setPLUItem(pluItem);
          lineItem.getReturnItem().setPrice(pluItem.getPrice());
        }
        int sequenceNumber = lineItem.getLineNumber();
        try
        {
          String employeeID = selectCommissionModifier(dataConnection, transaction, sequenceNumber);
          if (!employeeID.equals(transaction.getCashier().getEmployeeID())) {
            ((AbstractTransactionLineItem)lineItem).setSalesAssociateModifiedFlag(true);
          }
          lineItem.setSalesAssociate(getEmployee(dataConnection, employeeID));
          if (!employeeID.equals(transaction.getSalesAssociate())) {
            lineItem.setSalesAssociateModifiedAtLineItem(true);
          }
        }
        catch (DataException localDataException) {}
        itemDiscounts = selectRetailPriceModifiers(dataConnection, transaction, lineItem, localeRequestor);
        lineItem.getItemPrice().setItemDiscounts(itemDiscounts);
        



        ItemTaxIfc tax = selectSaleReturnTaxModifier(dataConnection, transaction, lineItem, localeRequestor);
        if (tax != null)
        {
          if (lineItem.getItemTaxMethod() == 1)
          {
            tax.setItemTaxAmount(lineItem.getItemPrice().getItemTaxAmount());
            tax.setItemInclusiveTaxAmount(lineItem.getItemPrice().getItemInclusiveTaxAmount());
          }
          lineItem.getItemPrice().setItemTax(tax);
          if (lineItem.getReturnItem() != null) {
            lineItem.getReturnItem().setTaxRate(tax.getDefaultRate());
          }
        }
        if (lineItem.getItemPrice().getItemTax().getTaxMode() == -1) {
          if (pluItem.getTaxable()) {
            lineItem.getItemPrice().getItemTax().setTaxMode(0);
          } else {
            lineItem.getItemPrice().getItemTax().setTaxMode(6);
          }
        }
        selectExternalOrderLineItem(dataConnection, transaction, lineItem);
        
        lineItem.getItemPrice().calculateItemTotal();
      }
    }
    catch (SQLException exc)
    {
      dataConnection.logSQLException(exc, "Processing result set.");
      throw new DataException(1, "error processing sale return line items", exc);
    }
    associateKitComponents(saleReturnLineItems);
    
    int numItems = saleReturnLineItems.size();
    SaleReturnLineItemIfc[] lineItems = new SaleReturnLineItemIfc[numItems];
    saleReturnLineItems.copyInto(lineItems);
    if (logger.isDebugEnabled()) {
      logger.debug("JdbcReadTransaction.selectSaleReturnLineItems");
    }
    return lineItems;
  }
  
  protected void setPLUItemsTaxRules(SaleReturnLineItemIfc[] srlis)
  {
    if ((srlis != null) && (srlis.length > 0)) {
      for (SaleReturnLineItemIfc srli : srlis) {
        if ((srli.isSaleLineItem()) && 
          (srli.getTaxInformationContainer() != null) && 
          (srli.getTaxInformationContainer().getTaxInformation() != null) && 
          (srli.getTaxInformationContainer().getTaxInformation().length > 0))
        {
          int i = 0;
          List<NewTaxRuleIfc> taxRulesList = new ArrayList(srli.getTaxInformationContainer().getTaxInformation().length);
          for (TaxInformationIfc taxInformation : srli.getTaxInformationContainer().getTaxInformation()) {
            if (!taxInformation.getUniqueID().equals("Tax exempt tax toggle off"))
            {
              NewTaxRuleIfc taxRule = DomainGateway.getFactory().getTaxByLineRuleInstance();
              
              TaxRateCalculatorIfc taxRateCalC = DomainGateway.getFactory().getTaxRateCalculatorInstance(taxInformation.getInclusiveTaxFlag());
              BigDecimal taxRate = new BigDecimal(taxInformation.getTaxPercentage().doubleValue());
              

              taxRateCalC.setTaxRate(taxRate.movePointLeft(2));
              taxRateCalC.setScale(taxInformation.getTaxPercentage().scale());
              taxRule.setOrder(i);
              taxRule.setInclusiveTaxFlag(taxInformation.getInclusiveTaxFlag());
              taxRule.setTaxAuthorityID(taxInformation.getTaxAuthorityID());
              taxRule.setTaxCalculator(taxRateCalC);
              taxRule.setTaxGroupID(taxInformation.getTaxGroupID());
              taxRule.setTaxHoliday(taxInformation.getTaxHoliday());
              taxRule.setTaxRuleName(taxInformation.getTaxRuleName());
              taxRule.setTaxTypeCode(taxInformation.getTaxTypeCode());
              taxRule.setUniqueID(taxInformation.getUniqueID());
              taxRule.setUseBasePrice(false);
              taxRulesList.add(i, taxRule);
              i++;
            }
          }
          if (taxRulesList.size() > 0) {
            srli.getPLUItem().setTaxRules((NewTaxRuleIfc[])taxRulesList.toArray(new NewTaxRuleIfc[taxRulesList.size()]));
          }
        }
      }
    }
  }
  
  /**
   * Reads the sale return line item exclusive and inclusive tax amount from
   * SaleReturnLineItemTax table.
   *
   * @param dataConnection a connection to the database
   * @param transaction the retail transaction
   * @param lineItemSequenceNumber the line item sequence number
   * @return exclusive and inclusive tax amount for this line item, across all
   *         jurisdictions
   * @exception DataException thrown when an error occurs executing the SQL
   *                against the DataConnection, or when processing the
   *                ResultSet
   */
  protected CurrencyIfc[] selectSaleReturnLineItemTaxAmount(JdbcDataConnection dataConnection,
          SaleReturnTransactionIfc transaction, int lineItemSequenceNumber) throws DataException
  {
      CurrencyIfc[] taxAmount = { DomainGateway.getBaseCurrencyInstance(BigDecimal.ZERO),
              DomainGateway.getBaseCurrencyInstance(BigDecimal.ZERO) };
      if (logger.isDebugEnabled())
          logger.debug("JdbcReadTransaction.selectSaleReturnLineItemTaxAmount()");

      SQLSelectStatement sql = new SQLSelectStatement();

      sql.addTable(TABLE_SALE_RETURN_TAX_LINE_ITEM, ALIAS_SALE_RETURN_TAX_LINE_ITEM);
//Pak change to use the MO_TX_RTN_SLS instead MO_TX_RTN_SLS_TOT
      //sql.addColumn("DISTINCT " + ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_ITEM_TAX_AMOUNT_TOTAL);
      sql.addColumn("DISTINCT " + ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_SALE_RETURN_TAX_AMOUNT);
      
      sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_ITEM_TAX_INC_AMOUNT_TOTAL);

      sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_RETAIL_STORE_ID + "=" + getStoreID(transaction));
      sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_WORKSTATION_ID + " = "
              + getWorkstationID(transaction));
      sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_BUSINESS_DAY_DATE + " = "
              + getBusinessDayString(transaction));
      sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_TRANSACTION_SEQUENCE_NUMBER + " = "
              + getTransactionSequenceNumber(transaction));
      sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER
              + " = " + lineItemSequenceNumber);

      try
      {
          dataConnection.execute(sql.getSQLString());
          ResultSet rs = (ResultSet)dataConnection.getResult();

          while (rs.next())
          {
              int index = 0;
              taxAmount[0] = getCurrencyFromDecimal(rs, ++index);
              taxAmount[1] = getCurrencyFromDecimal(rs, ++index);
          }
      }
      catch (SQLException se)
      {
          throw new DataException(DataException.SQL_ERROR, "selectSaleReturnLineItemTaxAmount", se);
      }
      catch (DataException de)
      {
          throw de;
      }
      catch (Exception e)
      {
          throw new DataException(DataException.UNKNOWN, "selectSaleReturnLineItemTaxAmount", e);
      }
      return (taxAmount);
  }
  
  /**
   * Reads from the sale return tax modifier table.
   *
   * @param dataConnection the connection to the data source
   * @param transaction the retail transaction
   * @param lineItem the sale/return line item
   * @return sale return tax modifier
   * @exception DataException thrown when an error occurs executing the SQL
   *                against the DataConnection, or when processing the
   *                ResultSet
   */
  protected ItemTaxIfc selectSaleReturnTaxModifier(JdbcDataConnection dataConnection,
          SaleReturnTransactionIfc transaction, SaleReturnLineItemIfc lineItem, LocaleRequestor localeRequestor)
          throws DataException
  {
      if (logger.isDebugEnabled())
          logger.debug("JdbcReadTransaction.selectSaleReturnTaxModifier()");
      SQLSelectStatement sql = new SQLSelectStatement();
      /*
       * Add Table(s)
       */
      sql.addTable(TABLE_SALE_RETURN_TAX_MODIFIER);
      /*
       * Add Column(s)
       */
      sql.addColumn(FIELD_SALE_RETURN_TAX_AMOUNT);
      sql.addColumn(FIELD_SALE_RETURN_TAX_EXEMPTION_REASON_CODE);
      sql.addColumn(FIELD_TAX_TYPE_CODE);
      sql.addColumn(FIELD_TAX_PERCENT);
      sql.addColumn(FIELD_TAX_OVERRIDE_PERCENT);
      sql.addColumn(FIELD_TAX_OVERRIDE_AMOUNT);
      sql.addColumn(FIELD_TAX_SCOPE_ID);
      sql.addColumn(FIELD_TAX_GROUP_ID);
      sql.addColumn(FIELD_TAX_MODIFIER_SEQUENCE_NUMBER);

      // sql.addColumn(FIELD_TAX_METHOD_ID); - external tax mgr
      /*
       * Add Qualifier(s)
       */
      sql.addQualifier(FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
      sql.addQualifier(FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
      sql.addQualifier(FIELD_BUSINESS_DAY_DATE + " = " + getBusinessDayString(transaction));
      sql.addQualifier(FIELD_TRANSACTION_SEQUENCE_NUMBER + " = " + getTransactionSequenceNumber(transaction));
      sql.addQualifier(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER + " = " + lineItem.getLineNumber());
      /*
       * Add Ordering
       */
      sql.addOrdering(FIELD_TAX_MODIFIER_SEQUENCE_NUMBER + " ASC");

      ItemTaxIfc itemTax = null;

      try
      {
          dataConnection.execute(sql.getSQLString());
          ResultSet rs = (ResultSet)dataConnection.getResult();

          TaxInformationContainerIfc taxInformationContainer = lineItem.getTaxInformationContainer();
          if (taxInformationContainer == null)
          {
              taxInformationContainer = DomainGateway.getFactory().getTaxInformationContainerInstance();
          }
          if (rs.next())
          {
              int index = 0;
              // The tax amount is now saved in longer precision to
              // resolve the rounding problem. So, use the newly
              // introduced getLongerCurrencyFromDeciaml during read
              // so that it will keep the item level precision.
              CurrencyIfc amount = getLongerCurrencyFromDecimal(rs, ++index);
              String reasonCodeString = getSafeString(rs, ++index);
              int taxMode = rs.getInt(++index);
              BigDecimal defaultPercent = getBigDecimal(rs, ++index, TAX_PERCENTAGE_SCALE);
              BigDecimal overridePercent = getBigDecimal(rs, ++index, TAX_PERCENTAGE_SCALE);
              CurrencyIfc overrideAmount = getCurrencyFromDecimal(rs, ++index);
              int taxScope = rs.getInt(++index);
              int taxGroupID = rs.getInt(++index);
              // int taxMethod = rs.getInt(++index); - external tax mgr

              itemTax = DomainGateway.getFactory().getItemTaxInstance();
              itemTax.setDefaultRate(defaultPercent.movePointLeft(2).doubleValue());
              itemTax.setOverrideRate(overridePercent.movePointLeft(2).doubleValue());
              itemTax.setOverrideAmount(overrideAmount);
              itemTax.setTaxScope(taxScope);
              boolean taxable = false;
              if (lineItem.getPLUItem() != null)
              {
                  taxable = lineItem.getPLUItem().getTaxable();
              }
              itemTax.setTaxable(taxable);
              itemTax.setTaxMode(taxMode);
              itemTax.setItemTaxAmount(amount); // external tax mgr
              itemTax.setTaxGroupId(taxGroupID);
              taxInformationContainer.setTaxScope(taxScope);
            
             /* if (taxInformationContainer.getTaxInformation() != null)
              {
                  TaxInformationIfc[] taxInformation = taxInformationContainer.getTaxInformation();
                  for (int i = 0; i < taxInformation.length; i++)
                  {
                      taxInformation[i].setTaxAmount(amount);
                      taxInformation[i].setTaxMode(taxMode);
                      taxInformation[i].setTaxGroupID(taxGroupID);
                  }
              }
              else*/
              if (taxInformationContainer.getTaxInformation() == null)
              {
                  TaxInformationIfc taxInformation = DomainGateway.getFactory().getTaxInformationInstance();
                  taxInformation.setTaxAmount(amount);
                  taxInformation.setTaxMode(taxMode);
                  taxInformation.setTaxGroupID(taxGroupID);
                  // Transaction Read In
                  // taxInformation.setUniqueID("TRI"+taxGroupID);
                  taxInformation.setTaxPercentage(overridePercent);
                  taxInformationContainer.addTaxInformation(taxInformation);
              }
              itemTax.setTaxInformationContainer(taxInformationContainer);
              // set default tax rules into item tax
              itemTax.setDefaultTaxRules(transaction.getTransactionTax().getDefaultTaxRules());
              String codeListType = "";
              if (itemTax.getTaxMode() == TaxIfc.TAX_MODE_OVERRIDE_AMOUNT)
              {
                  codeListType = CodeConstantsIfc.CODE_LIST_ITEM_TAX_AMOUNT_OVERRIDE_REASON_CODES;
              }
              else if (itemTax.getTaxMode() == TaxIfc.TAX_MODE_OVERRIDE_RATE)
              {
                  codeListType = CodeConstantsIfc.CODE_LIST_ITEM_TAX_RATE_OVERRIDE_REASON_CODES;
              }
              else
              {
                  codeListType = CodeConstantsIfc.CODE_LIST_ON_OFF_REASON_CODES;
              }

              LocalizedCodeIfc localizedCode = getInitializedLocalizedReasonCode(dataConnection, transaction
                      .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType, localeRequestor);
              itemTax.setReason(localizedCode);
          }
          rs.close();
      }
      catch (SQLException exc)
      {
          dataConnection.logSQLException(exc, "Processing result set.");
          throw new DataException(DataException.SQL_ERROR, "selectSaleReturnTaxModifier", exc);
      }

      if (logger.isDebugEnabled())
          logger.debug("JdbcReadTransaction.selectSaleReturnTaxModifier()");

      return (itemTax);
  }
  /**
   * Reads the loyalty transaction details.
   *
   * @param dataConnection a connection to the database
   * @param transaction the sale return transaction
   * @return TransactionTax
   * @throws DataException when there is an error reading from the DB or
   *             processing the result set
   */
	public void selectLoyaltyTransaction(JdbcDataConnection dataConnection,
          GDYNSaleReturnTransactionIfc txn) throws DataException
  {

      if (logger.isDebugEnabled())
          logger.debug("GDYNJdbcReadTransaction.selectLoyaltyTransaction()");

      SQLSelectStatement sql = new SQLSelectStatement();
      /*
       * Add Table(s)
       */
      sql.addTable(TABLE_LOYALTY_TRANSACTION_RECORD);
      /*
       * Add Column(s)
       */
      sql.addColumn(FIELD_LOYALTY_ID);
      //sql.addColumn(FIELD_LOYALTY_EMAIL_ID);
          
      /*
       * Add Qualifier(s)
       */
      sql.addQualifier(FIELD_RETAIL_STORE_ID + " = " + getStoreID(txn));
      sql.addQualifier(FIELD_WORKSTATION_ID + " = " + getWorkstationID(txn));
      sql.addQualifier(FIELD_BUSINESS_DAY_DATE + " = " + getBusinessDayString(txn));
      sql.addQualifier(FIELD_TRANSACTION_SEQUENCE_NUMBER + " = " + getTransactionSequenceNumber(txn));

    
      try
      {
    	  logger.debug(sql.getSQLString()); 
          dataConnection.execute(sql.getSQLString());
          ResultSet rs = (ResultSet)dataConnection.getResult();

          while(rs.next())
          {
        	   /*int index = 0;
               loyaltyID = getSafeString(rs, ++index);
			  loyaltyEmailID = getSafeString(rs, ++index); */	
        	  loyaltyID = rs.getString(FIELD_LOYALTY_ID);
			 // loyaltyEmailID = rs.getString(FIELD_LOYALTY_EMAIL_ID);
			  logger.info("Loyalty ID" +loyaltyID);
			  txn.setLoyaltyID(loyaltyID) ;
			  //txn.setLoyaltyEmailID(loyaltyEmailID);            
			  txn.setOriginalLoyaltyID(loyaltyID);
            
        }
          rs.close();
          
      }
      catch (SQLException exc)
      {
          dataConnection.logSQLException(exc, "Processing result set.");
          throw new DataException(DataException.SQL_ERROR, "selectLoyaltyTransaction", exc);
      }
      if (logger.isDebugEnabled())
          logger.debug("GDYNJdbcReadTransaction.selectLoyaltyTransaction()");
      
    // return transaction;
      
  }
	
	//Add new method here to read original transaction details from ct_eep_item table
	
	public void readTransactionEepDtls(JdbcDataConnection dataConnection,
			SaleReturnTransactionIfc transaction) {
		logger.debug("readTransactionEepDtls method entered for transaction id "
				+ transaction.getTransactionID());
		/*String id_ws = transaction.getWorkstation().getWorkstationID();
		String storeId = getStoreID(transaction);
		String transactionId = transaction.getTransactionID();
		String businessDay = getBusinessDayString(transaction);*/
		// logger.debug("retrieving details for transaction "+transactionId);
		
		Map<String, Integer> entitlementMap = new HashMap<String, Integer>();
		SQLSelectStatement sql = new SQLSelectStatement();

		// * Add Table(s)

		sql.addTable("CT_EEP_ITEM");

		// * Add Column(s)

		sql.addColumn("PERIOD_ID");
		sql.addColumn("EMPL_GROUP_ID");
		sql.addColumn("ENTITLEMENT_ID");
		sql.addColumn("EMPL_ID_SRC");
		sql.addColumn("EMPL_NUMBER");
		sql.addColumn("ID_ITM");
		
		sql.addQualifier("ID_STR_RT = " + getStoreID(transaction));
		sql.addQualifier("ID_WS = " + getWorkstationID(transaction));
		sql.addQualifier("DC_DY_BSN = " + getBusinessDayString(transaction));
		sql.addQualifier("AI_TRN = "
				+ getTransactionSequenceNumber(transaction));

		try {
			logger.debug("Query to be executed " + sql.getSQLString());
			Boolean isEepReturn = Boolean.FALSE;
			dataConnection.execute(sql.getSQLString());
			ResultSet rs = (ResultSet) dataConnection.getResult();
			String periodId = "";
			String emplGrpId = "";
			String entitlementId = "";
			String emplIdSrc = "";
			String discountEmployeeId = "";
			while (rs.next()) {
				isEepReturn = Boolean.TRUE;
				periodId = rs.getString("PERIOD_ID");
				emplGrpId = rs.getString("EMPL_GROUP_ID");
				entitlementId = rs.getString("ENTITLEMENT_ID");
				emplIdSrc = rs.getString("EMPL_ID_SRC");
				discountEmployeeId = rs.getString("EMPL_NUMBER");
				String itemId = rs.getString("ID_ITM");
				entitlementMap.put(itemId,Integer.parseInt(entitlementId));
				logger.debug("periodId " + periodId + " emplGrpId " + emplGrpId
						+ " entitlementId " + entitlementId + " emplIdSrc "
						+ emplIdSrc);
				
			}
			logger.debug("Transaction with seq number "
					+ getTransactionSequenceNumber(transaction)
					+ " is an eep transaction " + isEepReturn);
			if (isEepReturn
					&& transaction.getItemContainerProxy().getLineItemsVector() != null
					&& transaction.getItemContainerProxy().getLineItemsVector()
							.size() != 0) {

				Vector vector = transaction.getItemContainerProxy()
						.getLineItemsVector();
				Iterator iterator = vector.iterator();
				while (iterator.hasNext()) {
					
					
					SaleReturnLineItem srli = (SaleReturnLineItem) iterator
							.next();
					int itemEntitlementId= entitlementMap.get(srli.getPLUItemID());
					srli.getPLUItem().setPeriodId(Integer.parseInt(periodId));
					srli.getPLUItem().setEntitlementId(itemEntitlementId);
					srli.getPLUItem().setEmplGrpId(Integer.parseInt(emplGrpId));
					srli.getPLUItem().setEmplIdSrc(emplIdSrc);
					logger.debug("Before setting periodId " + periodId
							+ " emplGrpId " + emplGrpId + " entitlementId "
							+ entitlementId + " emplIdSrc " + emplIdSrc);
				}
				if (Util.isEmpty(transaction.getEmployeeDiscountID())) {
					logger.debug("setting discount employee id of transaction "
							+ getTransactionSequenceNumber(transaction));
					transaction.setEmployeeDiscountID(discountEmployeeId);
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}
