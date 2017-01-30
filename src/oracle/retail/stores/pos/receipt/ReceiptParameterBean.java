/* ===========================================================================
* Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/receipt/ReceiptParameterBean.java /rgbustores_13.4x_generic_branch/5 2011/10/25 17:27:05 hyin Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    hyin      10/25/11 - add checking before printing customer send info
 *    rsnayak   08/29/11 - Forward Port: SHIPING SLIP IS PRINTED FOR UNPAID
 *                         SUSPEND TRANSACTION
 *    rsnayak   08/29/11 - fix for different send addresses for item level send
 *    cgreene   06/28/11 - removed deprecated code
 *    asinton   10/26/10 - Print credit disclosure statement on signature slip
 *                         only for credit tender containing disclosure data.
 *    asinton   10/05/10 - Made getTotalTenderAmount return zero currency
 *                         amount when the transaction is suspended.
 *    asinton   09/28/10 - More updates for credit card promotion disclosure.
 *    asinton   09/24/10 - Adding Credit Card Accountability Responsibility and
 *                         Disclosure Act of 2009 changes.
 *    rsnayak   09/16/10 - Layaway Delete Receipt Fix for store credit
 *    jswan     08/31/10 - Fixed problems with print tender change and totals
 *                         on the receipt.
 *    sgu       07/07/10 - fix tab
 *    sgu       07/07/10 - fix tab
 *    sgu       07/07/10 - add external order to receipts
 *    nkgautam  06/23/10 - bill pay changes
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   04/28/10 - updating deprecated names
 *    jswan     03/17/10 - Fixed an issue with mutli quantity items with
 *                         transaction discounts that have not been compressed
 *                         by the clean receipt process.
 *    abondala  02/22/10 - fix the return denied receipt
 *    jswan     02/15/10 - Code review change.
 *    jswan     02/15/10 - Make all receipts go with the email.
 *    cgreene   02/10/10 - check the transaction discount and multiply it by
 *                         the quantity when clean receipt is turned on
 *    vapartha  02/02/10 - Checking in after merging.
 *    vapartha  02/02/10 - Added condition not to group if the tax has been
 *                         changed for a item
 *    vapartha  02/02/10 - Added a condition not to group an item if the item
 *                         has a serialnumber associated.
 *    jswan     01/28/10 - Modifications to support emailing rebate, gift and
 *                         alteration receipts with the sale reciept.
 *    abondala  01/03/10 - update header date
 *    asinton   06/23/09 - Exclude alternate currencies when combining cash in
 *                         the getTenders method.
 *    asinton   05/15/09 - Added method getTenderTotalsAmount for the
 *                         derivation of said value for sales, returns, and
 *                         voids.
 *    cgreene   04/28/09 - add getValidReturnExpirationDate
 *    cgreene   04/22/09 - remove reentryMode from parameter bean since
 *                         transaction knows and call transaction method from
 *                         ankle and header
 *    cgreene   04/21/09 - added code to not print store coupons per 3455
 *    cgreene   04/09/09 - fixed but in indexing of rearranged clean receipt
 *                         items
 *    cgreene   04/07/09 - corrected bugs in getLineItems around cloning for
 *                         clean receipt and unnecesary re-adding of kit
 *                         components
 *    cgreene   04/05/09 - change method name to getLineItems for sorting line
 *                         items
 *    djenning  04/03/09 - correct refactoring
 *    djenning  04/03/09 - cleanup
 *    djenning  04/03/09 - handle kit components (gift cards/gift certs cannot
 *                         be components)
 *    djenning  04/01/09 - creating a separate getBillingCustomer() which
 *                         returns a value if there is a send customer or an
 *                         IRS customer associated with the transaction. used
 *                         for printing the billing address info in those two
 *                         cases.
 *    cgreene   03/16/09 - add flags for printing sigcap image and signature
 *                         line
 *    vikini    03/01/09 - Incorporate CodeReview Comments
 *    vikini    02/28/09 - Fixing Error in display of RM Footer Messages in
 *                         Receipt
 *    atirkey   02/19/09 - trans re entry
 *    acadar    02/12/09 - convert tab to spaces
 *    acadar    02/12/09 - use default locale for date/time printing in the
 *                         receipts
 *    glwang    02/09/09 - changes per code review
 *    glwang    02/06/09 - add isTrainingMode into
 *                         PrintableDocumentParameterBeanIfc
 *    atirkey   01/15/09 - added flags and place holders for display
 *    cgreene   12/05/08 - add isVATEnabled to isVATSummaryShouldPrint
 *    sgu       11/20/08 - refres to latest label
 *    sgu       11/20/08 - use space
 *    sgu       11/20/08 - use space instead of tab
 *    sgu       11/19/08 - add VAT enabled flag to receipt parameter bean
 *    cgreene   11/18/08 - moved isReturn to SaleReturnTransactionIfc
 *    arathore  11/17/08 - updated for ereceipt feature
 *    cgreene   11/17/08 - added isReturn method
 *    cgreene   11/17/08 - added getTransactionType method
 *    cgreene   11/13/08 - deprecate getSurveyText in favor of isSurveyExpected
 *                         and editing Survey.bpt
 *    cgreene   11/12/08 - deprecated method for parameters that were removed
 *    cgreene   11/03/08 - fix some HousePaymentReceipt formatting and
 *                         implement blueprint copies
 *    cgreene   10/30/08 - fix equals comparision that was checking -1 against
 *                         LocalizedText
 *    acadar    10/25/08 - localization of price override reason codes
 *    cgreene   10/17/08 - added getVATHelper and isVATummaryPrint
 *    arathore  10/16/08 - Changes for clean receipt feature.
 *
 * ===========================================================================
 */

package oracle.retail.stores.pos.receipt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.common.utility._360DateIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.financial.BillIfc;
import oracle.retail.stores.domain.financial.BillPayIfc;
import oracle.retail.stores.domain.financial.FinancialCountIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.PriceAdjustmentLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc;
import oracle.retail.stores.domain.stock.ReceiptFooterMessageDTO;
import oracle.retail.stores.domain.tender.TenderAlternateCurrencyIfc;
import oracle.retail.stores.domain.tender.TenderCashIfc;
import oracle.retail.stores.domain.tender.TenderChargeIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.transaction.BillPayTransaction;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransaction;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionTotalsIfc;
import oracle.retail.stores.domain.transaction.VoidTransaction;
import oracle.retail.stores.domain.utility.PhoneConstantsIfc;
import oracle.retail.stores.domain.utility.PhoneIfc;

/**
 * Simple bean used to pass receipt controlling parameters from Site code
 *
 * @author epd
 */
public class ReceiptParameterBean implements ReceiptParameterBeanIfc
{
    private static final long serialVersionUID = 85896578261L;

    /**
     * The reference to the next {@link SendPackInfo} to print
     */
    private transient SendPackInfo nextSendPackInfo;

    /**
     * Determines whether we should print a customer copy
     */
    protected boolean autoPrintCustomerCopy = false;

    /**
     * Boolean flag for auto print gift receipt gift registry.
     */
    protected boolean autoPrintGiftReceiptGiftRegistry = false;

    /**
     * Boolean flag for auto print gift receipt item send.
     */
    protected boolean autoPrintGiftReceiptItemSend = false;

    /**
     * Boolean flag for transactionHasSendItem.
     */
    protected boolean transactionHasSendItem = false;
    
    /**
     * Flag for keeping track of whether the signature line should be printed
     * for credit slips.
     */
    protected boolean creditSignatureLineRequired = true;

    /**
     * The locale used to print date and time on the receipt.
     */
    protected Locale defaultLocale;

    /**
     * Employee number associated with discount
     */
    protected String discountEmployeeNumber = "";

    /**
     * Document type from {@link ReceiptTypeConstantsIfc}.
     */
    protected String documentType;

    /**
     * flag indicating receipt is duplicate copy
     */
    protected boolean duplicateReceipt = false;

    /**
     * Indicates to print eReceipt.
     */
    protected boolean ereceipt = false;

    /**
     * The specific count for which a receipt is being printed.
     */
    protected FinancialCountIfc financialCount;

    /**
     * Group Like Items on Receipt Indicates if the Item with same id and same
     * price grouped together on receipt
     */
    protected boolean groupLikeItems = false;

    /**
     * The specific line items that are being reported on. These usually come
     * from the {@link #transaction}.
     */
    protected AbstractTransactionLineItemIfc[] lineItems;

    /**
     * The locale used to print the receipt.
     */
    protected Locale locale;

    /**
     * Boolean flag for print alteration receipt.
     */
    protected boolean printAlterationReceipt = false;

    /**
     * Boolean flag for print gift receipt.
     */
    protected boolean printGiftReceipt = false;

    /**
     * Boolean flag for print item tax.
     */
    protected boolean printItemTax = false;

    /**
     * In the case of multiples receipts, Indicates to print paper receipts for
     * store copies.
     */
    protected boolean printStoreReceipt = false;

    /**
     * The receipt style. Use PrintableDocumentManagerIfc.STYLE_NORMAL by
     * default.
     */
    protected String receiptStyle = PrintableDocumentManagerIfc.STYLE_NORMAL;

    /**
     * Add grouped RM Footer Messages
     */
    protected ReceiptFooterMessageDTO[] returnedFooterMsgs = null;

    /**
     * Flag for keeping track of whether the signature capture image should be
     * printed.
     */
    protected boolean signatureCaptureImage;

    /**
     * Whether a survey should be included in the printing.
     */
    protected boolean surveyShouldPrint;

    /**
     * The specific tender for which a receipt is being printed
     */
    protected TenderLineItemIfc tender;

    /**
     * The specific tenders that are being reported on. These usually come
     * from the {@link #transaction}.
     */
    protected TenderLineItemIfc[] tenders;

    /**
     * The transaction for which a receipt is being printed
     */
    protected TenderableTransactionIfc transaction;

    /**
     * VAT Code Receipt Printing Indicates if the vatCode should be printed in
     * the receipt for the item level
     */
    protected boolean vatCodeReceiptPrinting = true;

    /**
     * Indicates if VAT is enabled
     */
    protected boolean vatEnabled = false;

    /**
     * VAT Helper. Does not need to be persisted.
     */
    protected transient VATHelper vatHelper = null;

    /**
     * Store VAT number
     */
    protected String vatNumber = null;

    /**
     * An eReceipt can have multiple attachments; Sales receipt, gift receipt,
     * rebate receipt and so on.  This data member contains text to differentiate
     * those files.
     */
    protected String eReceiptFileNameAddition = ReceiptConstantsIfc.NO_FILE_NAME_ADDITION;

    /**
     * Array of bills for bill pay transaction
     */
    protected BillIfc[] billedItems;

    /**
     * Credit card promotion description part 1
     */
    protected String creditCardPromotionDescriptionPart1;

    /**
     * Credit card promotion description part 2
     */
    protected String creditCardPromotionDescriptionPart2;

    /**
     * Credit card promotion duration
     */
    protected String creditCardPromotionDuration;

    /**
     * Formatted Credit Card Account Rate Information.
     */
    protected String formattedCreditCardAccountRate;

    /**
     * Formatted Credit Card Promotion Rate Information.
     */
    protected String formattedCreditCardPromotionRate;
    
    protected String employeeName;
    
    protected String employeeId;
    
    protected String remainingSpend;
    

    /**
	 * @return the employeeId
	 */
	public String getEmployeeId() {
		return employeeId;
	}

	/**
	 * @param employeeId the employeeId to set
	 */
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	/**
	 * @return the remainingSpend
	 */
	public String getRemainingSpend() {
		return remainingSpend;
	}

	/**
	 * @param remainingSpend the remainingSpend to set
	 */
	public void setRemainingSpend(String remainingSpend) {
		this.remainingSpend = remainingSpend;
	}

	/**
	 * @param employeeName the employeeName to set
	 */
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	/* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getBillingCustomer()
     */
    public CustomerIfc getBillingCustomer()
    {
        CustomerIfc customer = null;
        if (transaction instanceof SaleReturnTransaction)
        {
            SaleReturnTransaction srTransaction = (SaleReturnTransaction)transaction;
            if (!srTransaction.isSendCustomerLinked())
            {
                customer = srTransaction.getCaptureCustomer();
            }
        }
        if (customer == null)
            customer = transaction.getIRSCustomer();
        return customer;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getCustomer()
     */
    public CustomerIfc getCustomer()
    {
        CustomerIfc customer = null;
        if (transaction instanceof SaleReturnTransaction)
        {
            SaleReturnTransaction srTransaction = (SaleReturnTransaction)transaction;
            if (!srTransaction.isSendCustomerLinked())
            {
                customer = srTransaction.getCaptureCustomer();
            }
        }
        if (customer == null)
            customer = transaction.getCustomer();
        return customer;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getCustomerPhone()
     */
    public PhoneIfc getCustomerPhone()
    {
        PhoneIfc phone = null;
        CustomerIfc customer = getCustomer();

        if (customer != null)
        {
            if (customer.isBusinessCustomer())
            {
                phone = customer.getPhoneByType(PhoneConstantsIfc.PHONE_TYPE_WORK);
            }
            else
            {
                phone = customer.getPhoneByType(PhoneConstantsIfc.PHONE_TYPE_HOME);
            }
        }
        if (phone == null)
        {
            if (customer != null)
            {
                if (customer.getPhones().size() > 0)
                {
                    phone = customer.getPhones().get(0);
                }
            }
        }

        return phone;
    }

    /**
     * @return the locale
     */
    public Locale getDefaultLocale()
    {
        return defaultLocale;
    }

    /**
     * Returns employee number associated with discount.
     *
     * @return Returns the employee number.
     */
    public String getDiscountEmployeeNumber()
    {
        return discountEmployeeNumber;
    }

    /**
     * Returns the document type for the desired receipt.
     *
     * @return
     * @see oracle.retail.stores.pos.receipt.PrintableDocumentParameterBeanIfc#getDocumentType()
     */
    public String getDocumentType()
    {
        return this.documentType;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getFinancialCount()
     */
    public FinancialCountIfc getFinancialCount()
    {
        return financialCount;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getLineItems()
     */
    @SuppressWarnings("unchecked")
    public AbstractTransactionLineItemIfc[] getLineItems()
    {
        // lazily init the line items
        if (lineItems == null && getTransaction() instanceof RetailTransactionIfc)
        {
            RetailTransactionIfc retailTransaction = (RetailTransactionIfc)getTransaction();
            Vector<AbstractTransactionLineItemIfc> actualLineItems = retailTransaction.getLineItemsVector();
            @SuppressWarnings("rawtypes")
            List<AbstractTransactionLineItemIfc> rearrangedLineItems = (List)actualLineItems.clone();
            ArrayList<Integer> compressedLineItemsIndexes = new ArrayList<Integer>();

            // Check if Parameter is set to group Like Items on receipt.
            if (isGroupLikeItems())
            {
                // create a list of first occurrences of unique items
                List<CleanReceiptItem> uniqueItems = new ArrayList<CleanReceiptItem>();

                for (int i = 0; i < actualLineItems.size(); i++)
                {
                    if (actualLineItems.get(i) instanceof SaleReturnLineItemIfc)
                    {
                        SaleReturnLineItemIfc saleLineItem = (SaleReturnLineItemIfc)actualLineItems.get(i);
                        // continue if serialized items and gift cards
                        if (saleLineItem.isSerializedItem()
								|| saleLineItem.isGiftItem()
								|| (saleLineItem.getItemSerial() != null && saleLineItem.getItemSerial().length() > 0)
								|| saleLineItem.isTaxChanged())
                        {
                            continue;
                        }

                        // find this sale item in the list of unique items
                        int indexInUniques = uniqueItems.indexOf(new CleanReceiptItem(saleLineItem));

                        // check if we've found an item that equals this unique item
                        if (indexInUniques == -1)
                        {
                            int indexInRearranged = rearrangedLineItems.indexOf(saleLineItem);
                            // clone this lineitem so we can modify its quantity
                            saleLineItem = (SaleReturnLineItemIfc)saleLineItem.clone();
                            // replace its double in the cloned list
                            rearrangedLineItems.set(indexInRearranged, saleLineItem);
                            // add this first unique item
                            uniqueItems.add(new CleanReceiptItem(saleLineItem));
                        }
                        else
                        {
                            // get the previous item and update its quantity
                            CleanReceiptItem previousReceiptItem = uniqueItems.get(indexInUniques);
                            SaleReturnLineItemIfc uniqueItem = previousReceiptItem.getSaleReturnLineItem();
                            BigDecimal oldQuantity = uniqueItem.getItemQuantityDecimal();
                            BigDecimal addQuantity = saleLineItem.getItemQuantityDecimal();
                            BigDecimal newQuantity = oldQuantity.add(addQuantity);
                            uniqueItem.modifyItemQuantity(newQuantity);
                            compressedLineItemsIndexes.add(indexInUniques);

                            // remove the now obsolete line item from the clone vector
                            rearrangedLineItems.remove(saleLineItem);
                        }
                    }
                }
            } // groupLikeItems

            // complete line item arrangement starting end to beginning
            for (int i = rearrangedLineItems.size() - 1; i >= 0; i--)
            {
                AbstractTransactionLineItemIfc lineItem = rearrangedLineItems.get(i);
                if (lineItem instanceof SaleReturnLineItemIfc)
                {
                    SaleReturnLineItemIfc saleLineItem = (SaleReturnLineItemIfc)lineItem;
                    if ((saleLineItem.isReturnLineItem() && saleLineItem.isKitHeader()) // don't include any return kit headers
                            || (saleLineItem.isPriceAdjustmentLineItem() && saleLineItem.isKitHeader()) // don't include any adj kit headers
                            || saleLineItem.getPLUItem().isStoreCoupon()) // don't print coupons
                    {
                        rearrangedLineItems.remove(i);
                        continue;
                    }

                    CurrencyIfc transDisc = saleLineItem.getItemTransactionDiscountAmount();
                    // Perform this calculation only if there is a transaction discount amount and if
                    // this line item has been compressed by the group like items/clean receipt process.
                    if (transDisc.signum() != CurrencyIfc.ZERO && compressedLineItemsIndexes.contains(i))
                    {
                        transDisc = transDisc.multiply(saleLineItem.getItemQuantityDecimal());
                        saleLineItem.getItemPrice().setItemTransactionDiscountAmount(transDisc);
                    }

                    if (lineItem instanceof PriceAdjustmentLineItemIfc)
                    {
                        // remove the price override wrapper so we just print the two adjustments
                        PriceAdjustmentLineItemIfc priceAdjustment = (PriceAdjustmentLineItemIfc)lineItem;
                        rearrangedLineItems.remove(i);
                        // set sale to false so an extra linefeed prints after this item
                        priceAdjustment.getPriceAdjustSaleItem().setIsPartOfPriceAdjustment(false);
                    }
                }
            }

            lineItems = rearrangedLineItems.toArray(new AbstractTransactionLineItemIfc[rearrangedLineItems.size()]);
        }

        return lineItems;
    }

    /**
     * @return the locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getNextSendPackageInfo()
     */
    public SendPackInfo getNextSendPackageInfo()
    {
        return nextSendPackInfo;
    }

    /**
     * Returns the receipt style.
     *
     * @return The receipt style.
     */
    public String getReceiptStyle()
    {
        return this.receiptStyle;
    }

    /**
     * gets the grouped Footer RM Messages
     * @return rmFooterMsgs
     */
    public ReceiptFooterMessageDTO[] getReturnReceiptFooterMessages()
    {
      return returnedFooterMsgs;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getValidReturnExpirationDate()
     */
    public _360DateIfc getValidReturnExpirationDate()
    {
        _360DateIfc expiryDate = null;
        AbstractTransactionLineItemIfc[] lineItems = getLineItems();
        for (int i = lineItems.length - 1; i >= 0; i--)
        {
            if (lineItems[i] instanceof SaleReturnLineItemIfc)
            {
                SaleReturnLineItemIfc lineItem = (SaleReturnLineItemIfc)lineItems[i];
                if (lineItem.isSaleLineItem())
                {
                    expiryDate = (_360DateIfc)getTransaction().getBusinessDay().clone();
                    expiryDate.add(Calendar.DATE, 90);
                    break;
                }
            }
        }

        return expiryDate;
    }

    /**
     * Returns the tender.
     *
     * @return Returns the tender.
     */
    public TenderLineItemIfc getTender()
    {
        return tender;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.domain.transaction.TenderableTransactionIfc#getReceiptTenderLineItems()
     */
    public TenderLineItemIfc[] getTenders()
    {
        if (tenders == null)
        {
            //return all the tender line items upon a canceled order.
            if (getTransaction().getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_CANCEL)
            {
                tenders = getTransaction().getTenderLineItems();
            }
            else
            {
                TenderCashIfc cash = null;
                Vector<TenderLineItemIfc> tenderLineItemsVector = getTransaction().getTenderLineItemsVector();
                List<TenderLineItemIfc> list = new ArrayList<TenderLineItemIfc>(tenderLineItemsVector.size());
                for (TenderLineItemIfc tender : tenderLineItemsVector)
                {
                    if (tender.isCollected())
                    {
                        //Verify that amount is cash, positive, and not an alternate currency.
                        if (tender instanceof TenderCashIfc &&
                                tender.getAmountTender().signum() > -1 &&
                                tender instanceof TenderAlternateCurrencyIfc &&
                                ((TenderAlternateCurrencyIfc)tender).getAlternateCurrencyTendered() == null)
                        {
                            if (cash != null)
                            {
                                cash.setAmountTender(cash.getAmountTender().add(tender.getAmountTender()));
                                continue;
                            }
                            tender = (TenderLineItemIfc)tender.clone();
                            cash = (TenderCashIfc)tender;
                        }
                        list.add(tender);
                    }
                    else if (getTransaction().getTransactionType() == TransactionConstantsIfc.TYPE_LAYAWAY_DELETE
                            && !tender.isCollected())
                    {
                        list.add(tender);
                    }
                }
                tenders = list.toArray(new TenderLineItemIfc[list.size()]);
            }
        }
        return tenders;
    }

    /**
     * Returns the transaction.
     *
     * @return Returns the transaction.
     */
    public TenderableTransactionIfc getTransaction()
    {
        return transaction;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getTransactionType()
     */
    public int getTransactionType()
    {
        int tt = TransactionIfc.TYPE_UNKNOWN;

        // if the transaction is a sale or return transaction,
        // check to see if it really is an exchange.
        if (getTransaction().getTransactionType() == TransactionIfc.TYPE_SALE
                || getTransaction().getTransactionType() == TransactionIfc.TYPE_RETURN)
        {
            AbstractTransactionLineItemIfc[] lineItems = ((RetailTransactionIfc) getTransaction())
                    .getLineItems();
            boolean saleItems = false;
            boolean returnItems = false;

            // loop through line items
            for (int i = 0; i < lineItems.length; i++)
            {
                if (((SaleReturnLineItemIfc)lineItems[i]).isReturnLineItem())
                {
                    returnItems = true;
                }
                else
                {
                    saleItems = true;
                }
            }

            // if there are sale and return line items
            // the transaction is an exchange.
            if (saleItems && returnItems)
            {
                tt = TransactionIfc.TYPE_EXCHANGE;
            }
            else if (saleItems)
            {
                tt = TransactionIfc.TYPE_SALE;
            }
            else
            {
                tt = TransactionIfc.TYPE_RETURN;
            }
        }
        else
        {
            tt = getTransaction().getTransactionType();
        }
        return tt;
    }

    /**
     * Returns the VATHelper. Assumes the transaction is a
     * {@link RetailTransactionIfc}.
     *
     * @return the VATHelper.
     */
    public VATHelper getVATHelper()
    {
        if (vatHelper == null)
        {
            vatHelper = new VATHelper((RetailTransactionIfc)transaction);
        }
        return vatHelper;
    }

    /**
     * Returns the Store VAT number.
     *
     * @return The Store VAT number.
     */
    public String getVATNumber()
    {
        return this.vatNumber;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#hasSendPackages()
     */
    public boolean hasSendPackages()
    {
        SendPackageLineItemIfc[] sendPackages = transaction.getTransactionTotals().getSendPackages();
        if(!transaction.isSuspended())
        {
         for (int i = 0; i < sendPackages.length; i++)
         {
            //donot print shipping slip for send initiated by external system (For example, Siebel)
        	if (sendPackages[i].isExternalSend())
        		continue;

            if (nextSendPackInfo == null)
            {
                nextSendPackInfo = new SendPackInfo(sendPackages[i], i + 1, transaction);
                return true;
            }
            else if (nextSendPackInfo.getSendPackage().equals(sendPackages[i])&& nextSendPackInfo.getSendLabelCount() == (i + 1))
            {
                nextSendPackInfo = null;
            }
         } 
        }
        return false;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#isSignatureCaptureImage()
     */
    public boolean hasSignatureCaptureImage()
    {
        return signatureCaptureImage;
    }

    /**
     * Returns the autoPrintCustomerCopy value.
     *
     * @return Returns the autoPrintCustomerCopy.
     */
    public boolean isAutoPrintCustomerCopy()
    {
        return autoPrintCustomerCopy;
    }

    /**
     * Returns the autoPrintGiftReceiptGiftRegistry flag.
     *
     * @return Returns the autoPrintGiftReceiptGiftRegistry.
     */
    public boolean isAutoPrintGiftReceiptGiftRegistry()
    {
        return autoPrintGiftReceiptGiftRegistry;
    }

    /**
     * Returns the autoPrintGiftReceiptItemSend flag.
     *
     * @return Returns the autoPrintGiftReceiptItemSend.
     */
    public boolean isAutoPrintGiftReceiptItemSend()
    {
        return autoPrintGiftReceiptItemSend;
    }

    /**
     * Returns the transactionHasSendItem flag.
     *
     * @return Returns the transactionHasSendItem.
     */
    public boolean isTransactionHasSendItem()
    {
        return transactionHasSendItem;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#isCreditSignatureLineRequired()
     */
    public boolean isCreditSignatureLineRequired()
    {
        // do not show credit signature line if credit card promotion data is present.
        return creditSignatureLineRequired && Util.isEmpty(this.creditCardPromotionDescriptionPart1);
    }

    /**
     * @return Returns the duplicateReceipt.
     */
    public boolean isDuplicateReceipt()
    {
        return duplicateReceipt;
    }

    /**
     * Returns the eReceipt Flag
     *
     * @return Returns the eReceipt flag.
     */
    public boolean isEreceipt()
    {
        return ereceipt;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#isGroupLikeItems()
     */
    public boolean isGroupLikeItems()
    {
        return groupLikeItems;
    }

    /**
     * Returns the printAlterationReceipt flag.
     *
     * @return Returns the printAlterationReceipt.
     */
    public boolean isPrintAlterationReceipt()
    {
        return printAlterationReceipt;
    }

    /**
     * Returns the printGiftReceipt flag.
     *
     * @return Returns the printGiftReceipt.
     */
    public boolean isPrintGiftReceipt()
    {
        return printGiftReceipt;
    }

    /**
     * Returns the printItemTax flag.
     *
     * @return Returns the printItemTax.
     */
    public boolean isPrintItemTax()
    {
        return printItemTax;
    }

    /**
     * Returns the printStoreReceipt Flag
     *
     * @return Returns the printStoreReceipt flag.
     */
    public boolean isPrintStoreReceipt()
    {
        return printStoreReceipt;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#isSurveyShouldPrint()
     */
    public boolean isSurveyShouldPrint()
    {
        return surveyShouldPrint;
    }

    /*
     * Return the training mode in the transaction.
     * @see oracle.retail.stores.pos.receipt.PrintableDocumentParameterBeanIfc#isTrainingMode()
     */
    public boolean isTrainingMode()
    {
        return getTransaction().isTrainingMode();
    }

    /**
     * @return Returns the vatCodeReceiptPrinting.
     */
    public boolean isVATCodeReceiptPrinting()
    {
        return vatCodeReceiptPrinting;
    }

    /**
     * Returns the VAT enabled Flag
     *
     * @return Returns the VAT enabled flag.
     */
    public boolean isVATEnabled()
    {
        return vatEnabled;
    }

    /**
     * Whether the VAT Summary should be printed on the receipt. This should
     * depend on whether {@link #isVATEnabled()} true and that the
     * {@link #setReceiptStyle(String)} is set to "VATType2".
     *
     * @return true if summary is supposed to print.
     * @see PrintableDocumentManagerIfc#STYLE_VAT_TYPE_2
     */
    public boolean isVATSummaryShouldPrint()
    {
        return isVATEnabled()
                && PrintableDocumentManagerIfc.STYLE_VAT_TYPE_2
                        .equals(getReceiptStyle());
    }


    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getTotalTenderAmount()
     */
    public CurrencyIfc getTotalTenderAmount()
    {
        CurrencyIfc amount = null;
        /*
         * if the result is a refund use the amount tender as there are no
         * collected tenders in this case.  Unfortunately, amount tender does
         * not hold the amount of over tendered cash.
         */
        int transactionType = getTransactionType();
        if(transactionType == TransactionConstantsIfc.TYPE_RETURN ||
                (transactionType == TransactionConstantsIfc.TYPE_VOID &&
                        ((VoidTransaction)getTransaction()).getOriginalTransactionType() == TransactionConstantsIfc.TYPE_RETURN))
        {
            TransactionTotalsIfc totals = getTransaction().getTransactionTotals();
            amount = totals.getAmountTender();
        }
        else if(getTransaction().getTransactionStatus() == TransactionConstantsIfc.STATUS_SUSPENDED)
        {
            // A suspended transaction will not contain tenders so output
            // should be zero. The getTenderTotalAmountPlusChangeDue will
            // return a negative amount because ChangeDue hasn't been
            // calculated with any tenders paid.
            amount = DomainGateway.getBaseCurrencyInstance("0");
        }
        else
        {
            amount = transaction.getTenderTotalAmountPlusChangeDue();
        }
        return amount;
    }

    /**
     * @param autoPrintCustomerCopy The autoPrintCustomerCopy to set.
     */
    public void setAutoPrintCustomerCopy(boolean autoPrintCustomerCopy)
    {
        this.autoPrintCustomerCopy = autoPrintCustomerCopy;
    }

    /**
     * Sets the autoPrintGiftReceiptGiftRegistry flag.
     *
     * @param autoPrintGiftReceiptGiftRegistry The
     *            autoPrintGiftReceiptGiftRegistry to set.
     */
    public void setAutoPrintGiftReceiptGiftRegistry(boolean autoPrintGiftReceiptGiftRegistry)
    {
        this.autoPrintGiftReceiptGiftRegistry = autoPrintGiftReceiptGiftRegistry;
    }

    /**
     * Sets the autoPrintGiftReceiptItemSend flag.
     *
     * @param autoPrintGiftReceiptItemSend The autoPrintGiftReceiptItemSend to
     *            set.
     */
    public void setAutoPrintGiftReceiptItemSend(boolean autoPrintGiftReceiptItemSend)
    {
        this.autoPrintGiftReceiptItemSend = autoPrintGiftReceiptItemSend;
    }

    /**
     * Sets the TransactionHasSendItem flag.
     *
     * @param TransactionHasSendItem The transactionHasSendItem to
     *            set.
     */
    public void setTransactionHasSendItem(boolean transactionHasSendItem)
    {
        this.transactionHasSendItem = transactionHasSendItem;
    }
    
    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setCreditSignatureLineRequired(boolean)
     */
    public void setCreditSignatureLineRequired(boolean signatureLineRequired)
    {
        this.creditSignatureLineRequired = signatureLineRequired;
    }

    /**
     * @param locale
     *            the locale to set
     */
    public void setDefaultLocale(Locale defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Sets employee number associated with discount.
     *
     * @param discountEmployeeNumber
     *            The employee number.
     */
    public void setDiscountEmployeeNumber(String discountEmployeeNumber)
    {
        this.discountEmployeeNumber = discountEmployeeNumber;
    }

    /**
     * Returns the document type.
     *
     * @param docType
     * @see oracle.retail.stores.pos.receipt.PrintableDocumentParameterBeanIfc#setDocumentType(java.lang.String)
     */
    public void setDocumentType(String docType)
    {
        this.documentType = docType;
    }

    /**
     * @param duplicateReceipt
     *            The duplicateReceipt to set.
     */
    public void setDuplicateReceipt(boolean duplicateReceipt)
    {
        this.duplicateReceipt = duplicateReceipt;
    }

    /**
     * Sets the eReceipt flag
     *
     * @param eReceipt flag
     */
    public void setEreceipt(boolean ereceipt)
    {
        this.ereceipt = ereceipt;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setFinancialCount
     * (oracle.retail.stores.domain.financial.FinancialCountIfc)
     */
    public void setFinancialCount(FinancialCountIfc count)
    {
        this.financialCount = count;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setGroupLikeItems(boolean)
     */
    public void setGroupLikeItems(boolean groupLikeItems)
    {
        this.groupLikeItems = groupLikeItems;
    }

    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Sets the printAlterationReceipt flag.
     *
     * @param printAlterationReceipt The printAlterationReceipt to set.
     */
    public void setPrintAlterationReceipt(boolean printAlterationReceipt)
    {
        this.printAlterationReceipt = printAlterationReceipt;
    }

    /**
     * Sets the printGiftReceipt flag.
     *
     * @param printGiftReceipt The printGiftReceipt to set.
     */
    public void setPrintGiftReceipt(boolean printGiftReceipt)
    {
        this.printGiftReceipt = printGiftReceipt;
    }

    /**
     * Sets the printItemTax flag.
     *
     * @param printItemTax The printItemTax to set.
     */
    public void setPrintItemTax(boolean printItemTax)
    {
        this.printItemTax = printItemTax;
    }

    /**
     * Sets the printStoreReceipt flag
     *
     * @param printStoreReceipt flag
     */
    public void setPrintStoreReceipt(boolean print)
    {
        this.printStoreReceipt = print;
    }

    /**
     * Sets the receipt style.
     *
     * @param style The receipt style.
     */
    public void setReceiptStyle(String style)
    {
        this.receiptStyle = style;
    }

    /**
     * sets the footerRMMessages
     * @param msgs
     */
    public void setReturnReceiptFooterMsgs(ReceiptFooterMessageDTO[] msgs)
    {
      this.returnedFooterMsgs = msgs;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setSignatureCaptureImage(boolean)
     */
    public void setSignatureCaptureImage(boolean sigCapImageAvailable)
    {
        this.signatureCaptureImage = sigCapImageAvailable;
    }

    /* (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setSurveyShouldPrint(boolean)
     */
    public void setSurveyShouldPrint(boolean surveyShouldPrint)
    {
        this.surveyShouldPrint = surveyShouldPrint;
    }

    /**
     * Sets the tender.
     *
     * @param tender The tender to set.
     */
    public void setTender(TenderLineItemIfc tender)
    {
        this.tender = tender;
    }

    /**
     * Do nothing. The training mode should be already in the transaction.
     *
     * @see oracle.retail.stores.pos.receipt.PrintableDocumentParameterBeanIfc#setTrainingMode(boolean)
     */
    public void setTrainingMode(boolean flag)
    {
    }

    /**
     * Sets the transaction.
     *
     * @param transaction The transaction to set.
     */
    public void setTransaction(TenderableTransactionIfc transaction)
    {
        this.transaction = transaction;
        clearCache();
    }

    /**
     * @param vatCodeReceiptPrinting
     *            The vatCodeReceiptPrinting to set.
     */
    public void setVATCodeReceiptPrinting(boolean vatCodeReceiptPrinting)
    {
        this.vatCodeReceiptPrinting = vatCodeReceiptPrinting;
    }

    /**
     * Sets the VAT enabled flag
     *
     * @param vatEnabled the VAT enabled flag
     */
    public void setVATEnabled(boolean vatEnabled)
    {
        this.vatEnabled = vatEnabled;
    }

    /**
     * Sets the Store VAT number.
     *
     * @param number
     */
    public void setVATNumber(String number)
    {
        this.vatNumber = number;
    }

    /**
     * This method should be called any time something significant about this
     * bean changes that may affect the objects that it contains, like setting
     * the transaction.
     * @see #setTransaction(TenderableTransactionIfc)
     */
    protected void clearCache()
    {
        lineItems = null;
    }

    /**
     * @return Returns the eReceiptFileNameAddition.
     */
    public String getEReceiptFileNameAddition()
    {
        return eReceiptFileNameAddition;
    }

    /**
     * @param receiptFileNameAddition The eReceiptFileNameAddition to set.
     */
    public void setEReceiptFileNameAddition(String receiptFileNameAddition)
    {
        eReceiptFileNameAddition = receiptFileNameAddition;
    }

    /**
     * Gets the Array of bills paid
     * @return
     */
    @SuppressWarnings("unchecked")
    public BillIfc[] getBills()
    {
        ArrayList<BillIfc> billsList = new ArrayList<BillIfc>();
        ArrayList<BillIfc> rearrangedBills = new ArrayList<BillIfc>();
        if(billedItems == null && getTransaction() instanceof BillPayTransaction)
        {
            BillPayTransaction billPayTransaction = (BillPayTransaction)getTransaction();
            BillPayIfc billInfo = billPayTransaction.getBillPayInfo();
            billsList = billInfo.getBillsList();
            rearrangedBills = (ArrayList<BillIfc>)billsList.clone();

            for(int i= 0; i < billsList.size(); i++)
            {
                BillIfc bill = billsList.get(i);
                if(bill.getBillAmountPaid() == null)
                {
                    rearrangedBills.remove(i);
                }
            }
            billedItems = rearrangedBills.toArray(new BillIfc[rearrangedBills.size()]);
        }
        else if(billedItems == null && getTransaction() instanceof VoidTransaction)
        {
        	VoidTransaction voidTrans = (VoidTransaction) getTransaction();
        	TenderableTransactionIfc origTrans = voidTrans.getOriginalTransaction();
        	if(origTrans!=null && origTrans instanceof BillPayTransaction)
        	{
                BillPayTransaction billPayTransaction = (BillPayTransaction)origTrans;
                BillPayIfc billInfo = billPayTransaction.getBillPayInfo();
                billsList = billInfo.getBillsList();
                billedItems = billsList.toArray(new BillIfc[billsList.size()]);
        	}
        }
        return  billedItems;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getCreditCardPromotionDescriptionPart1()
     */
    public String getCreditCardPromotionDescriptionPart1()
    {
        // It is possible that with a split tender where house account containing the disclosure data
        // and another credit card without disclosure data will require printing of 2 signature slips.
        // Prevent the printing of the disclosure data on the credit slip by returning null when the
        // current tender is a credit line item but does not contain the disclosure data.
        String returnValue = this.creditCardPromotionDescriptionPart1;
        if(getDocumentType() == ReceiptTypeConstantsIfc.CREDIT_SIGNATURE && tender instanceof TenderChargeIfc)
        {
            if(Util.isEmpty(((TenderChargeIfc)tender).getPromotionDescription()))
            {
                returnValue = null;
            }
        }
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setCreditCardPromotionDescriptionPart1(java.lang.String)
     */
    public void setCreditCardPromotionDescriptionPart1(String creditCardPromotionDescriptionPart1)
    {
        this.creditCardPromotionDescriptionPart1 = creditCardPromotionDescriptionPart1;
    }
  
    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getCreditCardPromotionDescriptionPart2()
     */
    public String getCreditCardPromotionDescriptionPart2()
    {
        // It is possible that with a split tender where house account containing the disclosure data
        // and another credit card without disclosure data will require printing of 2 signature slips.
        // Prevent the printing of the disclosure data on the credit slip by returning null when the
        // current tender is a credit line item but does not contain the disclosure data.
        String returnValue = this.creditCardPromotionDescriptionPart2;
        if(getDocumentType() == ReceiptTypeConstantsIfc.CREDIT_SIGNATURE && tender instanceof TenderChargeIfc)
        {
            if(Util.isEmpty(((TenderChargeIfc)tender).getPromotionDescription()))
            {
                returnValue = null;
            }
        }
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setCreditCardPromotionDescriptionPart2(java.lang.String)
     */
    public void setCreditCardPromotionDescriptionPart2(String creditCardPromotionDescriptionPart2)
    {
        this.creditCardPromotionDescriptionPart2 = creditCardPromotionDescriptionPart2;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getCreditCardPromotionDuration()
     */
    public String getCreditCardPromotionDuration()
    {
        // It is possible that with a split tender where house account containing the disclosure data
        // and another credit card without disclosure data will require printing of 2 signature slips.
        // Prevent the printing of the disclosure data on the credit slip by returning null when the
        // current tender is a credit line item but does not contain the disclosure data.
        String returnValue = this.creditCardPromotionDuration;
        if(getDocumentType() == ReceiptTypeConstantsIfc.CREDIT_SIGNATURE && tender instanceof TenderChargeIfc)
        {
            if(Util.isEmpty(((TenderChargeIfc)tender).getPromotionDuration()))
            {
                returnValue = null;
            }
        }
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setCreditCardPromotionDuration(java.lang.String)
     */
    public void setCreditCardPromotionDuration(String creditCardPromotionDuration)
    {
        this.creditCardPromotionDuration = creditCardPromotionDuration;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getFormattedCreditCardAccountRate()
     */
    public String getFormattedCreditCardAccountRate()
    {
        // It is possible that with a split tender where house account containing the disclosure data
        // and another credit card without disclosure data will require printing of 2 signature slips.
        // Prevent the printing of the disclosure data on the credit slip by returning null when the
        // current tender is a credit line item but does not contain the disclosure data.
        String returnValue = this.formattedCreditCardAccountRate;
        if(getDocumentType() == ReceiptTypeConstantsIfc.CREDIT_SIGNATURE && tender instanceof TenderChargeIfc)
        {
            if(Util.isEmpty(((TenderChargeIfc)tender).getAccountAPR()))
            {
                returnValue = null;
            }
        }
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setFormattedCreditCardAccountRate(java.lang.String)
     */
    public void setFormattedCreditCardAccountRate(String formattedCreditCardAccountRate)
    {
        this.formattedCreditCardAccountRate = formattedCreditCardAccountRate;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#getFormattedCreditCardPromotionRate()
     */
    public String getFormattedCreditCardPromotionRate()
    {
        // It is possible that with a split tender where house account containing the disclosure data
        // and another credit card without disclosure data will require printing of 2 signature slips.
        // Prevent the printing of the disclosure data on the credit slip by returning null when the
        // current tender is a credit line item but does not contain the disclosure data.
        String returnValue = this.formattedCreditCardPromotionRate;
        if(getDocumentType() == ReceiptTypeConstantsIfc.CREDIT_SIGNATURE && tender instanceof TenderChargeIfc)
        {
            if(Util.isEmpty(((TenderChargeIfc)tender).getPromotionAPR()))
            {
                returnValue = null;
            }
        }
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * @see oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc#setFormattedCreditCardPromotionRate(java.lang.String)
     */
    public void setFormattedCreditCardPromotionRate(String formattedCreditCardPromotionRate)
    {
        this.formattedCreditCardPromotionRate = formattedCreditCardPromotionRate;
    }

    public String  getEmployeeName(){
  	  return employeeName ;
    }
}
