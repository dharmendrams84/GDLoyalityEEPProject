/* ===========================================================================
* Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/receipt/ReceiptParameterBeanIfc.java /rgbustores_13.4x_generic_branch/3 2011/10/25 17:27:05 hyin Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    hyin      10/25/11 - add checking before printing customer send info
 *    cgreene   06/28/11 - removed deprecated code
 *    asinton   09/29/10 - Adding Credit Card Accountability Responsibility and
 *                         Disclosure Act of 2009 changes.
 *    cgreene   05/26/10 - convert to oracle packaging
 *    jswan     01/28/10 - Modifications to support emailing rebate, gift and
 *                         alteration receipts with the sale reciept.
 *    abondala  01/03/10 - update header date
 *    asinton   05/15/09 - Use calculated tender total amount from the receipt
 *                         parameter bean.
 *    cgreene   04/30/09 - add getValidReturnExpirationDate
 *    cgreene   04/22/09 - remove reentryMode from parameter bean since
 *                         transaction knows and call transaction method from
 *                         ankle and header
 *    cgreene   04/05/09 - change method name to getLineItems for sorting line
 *                         items
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
 *    atirkey   01/15/09 - methods for flags and place holders
 *    sgu       11/20/08 - fix header
 *    sgu       11/20/08 - refres to latest label
 *    sgu       11/20/08 - use space instead of tab
 *    sgu       11/19/08 - add VAT enabled flag to receipt parameter bean 
 *    cgreene   11/18/08 - moved isReturn to SaleReturnTransactionIfc
 *    cgreene   11/17/08 - dded isReturn method 
 *    cgreene   11/17/08 - added getTransactionType method
 *    cgreene   11/13/08 - deprecate getSurveyText in favor of isSurveyExpected
 *                         and editing Survey.bpt
 *    cgreene   11/12/08 - deprecated method for parameters that were removed
 *
 * ===========================================================================
 * $Log:
 *  2    360Commerce 1.1         6/19/2007 5:12:07 PM   Maisa De Camargo Added
 *       vatCodeReceiptPrinting field and access methods. This field indicates
 *        if the vatCode should be printed in the receipt for the item level.
 *  1    360Commerce 1.0         4/30/2007 7:00:39 PM   Alan N. Sinton  CR
 *       26485 - Merge from v12.0_temp.
 * $
 * ===========================================================================
 */
package oracle.retail.stores.pos.receipt;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.utility._360DateIfc;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.financial.FinancialCountIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc;
import oracle.retail.stores.domain.stock.ReceiptFooterMessageDTO;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.utility.PhoneIfc;

/**
 * Interface for the ReceiptParametersBean.
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/3 $
 */
public interface ReceiptParameterBeanIfc extends PrintableDocumentParameterBeanIfc
{

    /**
     * Key used to locate this in Spring context. Equals
     * "application_ReceiptParameterBean".
     */
    public static final String BEAN_KEY = "application_ReceiptParameterBean";

    /**
     * Sets the transaction for this ReceiptParameterBean.
     * 
     * @param transaction
     */
    public void setTransaction(TenderableTransactionIfc transaction);

    /**
     * Gets the transaction from this ReceiptParameterBean.
     * 
     * @return The transaction value.
     */
    public TenderableTransactionIfc getTransaction();

    /**
     * Returns a more print-friendly code to represent the type of the attached
     * transaction. E.g., the transaction may return RETURN when the total
     * amount is negative, but this will return EXCHANGE if there are any SALE
     * items.
     * 
     * @return transaction type
     * @see TransactionConstantsIfc
     */
    public int getTransactionType();

    /**
     * Get the financial count for this bean.
     * 
     * @return the financial count
     */
    public FinancialCountIfc getFinancialCount();

    /**
     * Set the financial count for this bean.
     * 
     * @param the financial count
     */
    public void setFinancialCount(FinancialCountIfc count);

    /**
     * Returns the tender.
     * 
     * @return Returns the tender.
     */
    public TenderLineItemIfc getTender();

    /**
     * Sets the tender.
     * 
     * @param tender The tender to set.
     */
    public void setTender(TenderLineItemIfc tender);

    /**
     * Flag for setting whether the signature capture image should be printed.
     * 
     * @return
     * @see #isCreditSignatureLineRequired()
     */
    public void setSignatureCaptureImage(boolean sigCapImageAvailable);


    /**
     * Gets flag for whether the signature capture image should be printed.
     * 
     * @return
     * @see #isCreditSignatureLineRequired()
     */
    public boolean hasSignatureCaptureImage();

    /**
     * Flag for setting whether the signature line should be printed
     * for credit slips.
     * 
     * @return
     */
    public void setCreditSignatureLineRequired(boolean signatureLineRequired);

    /**
     * Gets flag for keeping track of whether the signature line should be
     * printed for credit slips.
     * 
     * @return
     */
    public boolean isCreditSignatureLineRequired();

    /**
     * Sets the duplicate for this this ReceiptParameterBean.
     * 
     * @param duplicate
     */
    public void setDuplicateReceipt(boolean duplicate);

    /**
     * Gets the duplicate from this ReceiptParameterBean.
     * 
     * @return The duplicate value.
     */
    public boolean isDuplicateReceipt();

    /**
     * Gets the customer phone number. Returns null if no customer is linked to
     * the transaction or if the linked customer has no phone number.
     */
    public PhoneIfc getCustomerPhone();

    /**
     * Get the customer from the transaction. May return the capture customer if
     * send customer is not linked.
     * 
     * @return
     */
    public CustomerIfc getCustomer();

    /**
     * Get the customer from the transaction for billing address. Returns the capture customer if
     * send customer or IRS customer for PAT.
     * 
     * @return
     */
    public CustomerIfc getBillingCustomer();

    /**
     * Return the line items that should be printed. These line items usually
     * come from the {@link #getTransaction()} but are arranged due to printing
     * requirements.
     * <p>
     * For example:
     * <ul>
     * <li>If {@link #isGroupLikeItems()} is true, then line
     * items that are the same id are combined into one.
     * <li>Return items should appear before sale items.
     * <li>Price override line items are split apart into their individual
     * return and sale line item.
     * <li>Kit component line items are added to the list behind their kit
     * headers.
     * </ul>
     * 
     * @return array of AbstractTransactionLineItemIfc
     */
    public AbstractTransactionLineItemIfc[] getLineItems();
    
    /**
     * Returns the subset of {@link TenderableTransactionIfc#getTenderLineItems()}
     * that should be printed on the receipt. In most cases this is simply
     * {@link TenderableTransactionIfc#getCollectedTenderLineItems()} but in
     * cases of canceled orders or canceled layaways, this will likely be all
     * the tenders.
     * 
     * @return tender line items vector
     */
    public TenderLineItemIfc[] getTenders();

    /**
     * Gets whether a survey should print for this parameter bean.
     * 
     * @return
     * @since 13.1
     */
    public boolean isSurveyShouldPrint();

    /**
     * Sets whether a survey should print for this parameter bean.
     * 
     * @param surveyToPrint
     * @since 13.1
     */
    public void setSurveyShouldPrint(boolean surveyShouldPrint);

    /**
     * Sets the autoPrint for this this ReceiptParameterBean.
     * 
     * @param autoPrint
     */
    public void setAutoPrintCustomerCopy(boolean autoPrint);

    /**
     * Gets the autoPrint from this ReceiptParameterBean.
     * 
     * @return The autoPrint value.
     */
    public boolean isAutoPrintCustomerCopy();

    /**
     * Sets the discountEmployeeNumber for this this ReceiptParameterBean.
     * 
     * @param discountEmployeeNumber
     */
    public void setDiscountEmployeeNumber(String discountEmployeeNumber);

    /**
     * Gets the discountEmployeeNumber from this ReceiptParameterBean.
     * 
     * @return The discountEmployeeNumber value.
     */
    public String getDiscountEmployeeNumber();

    /**
     * Returns the autoPrintGiftReceiptGiftRegistry flag.
     * 
     * @return Returns the autoPrintGiftReceiptGiftRegistry.
     */
    public boolean isAutoPrintGiftReceiptGiftRegistry();

    /**
     * Sets the autoPrintGiftReceiptGiftRegistry flag.
     * 
     * @param autoPrintGiftReceiptGiftRegistry The
     *            autoPrintGiftReceiptGiftRegistry to set.
     */
    public void setAutoPrintGiftReceiptGiftRegistry(boolean autoPrintGiftReceiptGiftRegistry);

    /**
     * Returns the autoPrintGiftReceiptItemSend flag.
     * 
     * @return Returns the autoPrintGiftReceiptItemSend.
     */
    public boolean isAutoPrintGiftReceiptItemSend();

    /**
     * Sets the autoPrintGiftReceiptItemSend flag.
     * 
     * @param autoPrintGiftReceiptItemSend The autoPrintGiftReceiptItemSend to
     *            set.
     */
    public void setAutoPrintGiftReceiptItemSend(boolean autoPrintGiftReceiptItemSend);

    /**
     * Returns the transactionHasSendItem flag.
     * 
     * @return Returns the transactionHasSendItem.
     */
    public boolean isTransactionHasSendItem();
    
    /**
     * Sets the transactionHasSendItem flag.
     * 
     * @param transactionHasSendItem The transactionHasSendItem to set.
     */
    public void setTransactionHasSendItem(boolean transactionHasSendItem);
    
    /**
     * Returns teh printAlterationReceipt flag.
     * 
     * @return Returns the printAlterationReceipt.
     */
    public boolean isPrintAlterationReceipt();

    /**
     * Sets the printAlterationReceipt flag.
     * 
     * @param printAlterationReceipt The printAlterationReceipt to set.
     */
    public void setPrintAlterationReceipt(boolean printAlterationReceipt);

    /**
     * Returns the printGiftReceipt flag.
     * 
     * @return Returns the printGiftReceipt.
     */
    public boolean isPrintGiftReceipt();

    /**
     * Sets the printGiftReceipt flag.
     * 
     * @param printGiftReceipt The printGiftReceipt to set.
     */
    public void setPrintGiftReceipt(boolean printGiftReceipt);

    /**
     * Returns the printItemTax flag.
     * 
     * @return Returns the printItemTax.
     */
    public boolean isPrintItemTax();

    /**
     * Sets the printItemTax flag.
     * 
     * @param printItemTax The printItemTax to set.
     */
    public void setPrintItemTax(boolean printItemTax);

    /**
     * Returns the receipt style.
     * 
     * @return The receipt style.
     */
    public String getReceiptStyle();

    /**
     * Sets the receipt style.
     * 
     * @param style The receipt style.
     */
    public void setReceiptStyle(String style);

    /**
     * Returns the Store VAT number.
     * 
     * @return The Store VAT number.
     */
    public String getVATNumber();

    /**
     * Returns the VATHelper.
     * 
     * @return the VATHelper.
     */
    public VATHelper getVATHelper();
    
    

    /**
     * Sets the Store VAT number.
     * 
     * @param number
     */
    public void setVATNumber(String number);

    /**
     * Returns the vatCodeReceiptPrinting Flag
     * 
     * @return Returns the vatCodeReceiptPrinting.
     */
    public boolean isVATCodeReceiptPrinting();

    /**
     * Whether the VAT Summary should be printed on the receipt. This should
     * depend on is the {@link #setReceiptStyle(String)} is set to "VATType2".
     * 
     * @return true if summary is supposed to print.
     */
    public boolean isVATSummaryShouldPrint();

    /**
     * Sets the vatCodeReceiptPrinting Flag
     * 
     * @param vatCodeReceiptPrinting The vatCodeReceiptPrinting to set.
     */
    public void setVATCodeReceiptPrinting(boolean vatCodeReceiptPrinting);

    /**
     * Returns true if this bean has {@link SendPackageLineItemIfc}s that have
     * yet to be printed. A result of true means that a subsequent call to
     * {@link #getNextSendPackageInfo()} will return a non-null. Any additional
     * calls to this method will change the reference returned by
     * {@link #getNextSendPackageInfo()}.
     * 
     * @return
     */
    public boolean hasSendPackages();

    /**
     * Returns the current send pack info to print.
     * 
     * @return
     */
    public SendPackInfo getNextSendPackageInfo();

    /**
     * Sets the Group Like Items On Receipt.
     * 
     * @param number
     */
    public void setGroupLikeItems(boolean groupLikeItems);

    /**
     * Returns the groupLikeItems Flag
     * 
     * @return Returns the groupLikeItems.
     */
    public boolean isGroupLikeItems();

    /**
     * Returns the VAT enabled Flag
     * 
     * @return Returns the VAT enabled flag.
     */
    public boolean isVATEnabled();

    /**
     * Sets the VAT enabled flag
     * 
     * @param vatEnabled the VAT enabled flag
     */
    public void setVATEnabled(boolean vatEnabled);

    /**
     * Returns the eReceipt Flag
     * 
     * @return Returns the eReceipt flag.
     */
    public boolean isEreceipt();

    /**
     * Sets the eReceipt flag
     * 
     * @param eReceipt flag
     */
    public void setEreceipt(boolean ereceipt);

    /**
     * Returns the printStoreReceipt Flag
     * 
     * @return Returns the printStoreReceipt flag.
     */
    public boolean isPrintStoreReceipt();

    /**
     * Sets the printStoreReceipt flag
     * 
     * @param printStoreReceipt flag
     */
    public void setPrintStoreReceipt(boolean print);

    /**
     * sets the grouped footerMessages returned from Returns Management
     * 
     * @param msgs
     */
    public void setReturnReceiptFooterMsgs(ReceiptFooterMessageDTO[] msgs);

    /**
     * gets the grouped footerMessages returned from Returns Management
     * 
     * @return rmFooterMsgs
     */
    public ReceiptFooterMessageDTO[] getReturnReceiptFooterMessages();

    /**
     * Return a date at which time the sale item being printed will no longer
     * be valid for return. This will return null if there are no sale line
     * items found.
     * 
     * @return a date 90 days after the transaction's business day.
     */
    public _360DateIfc getValidReturnExpirationDate();

    /**
     * Returns the total tender amount for display on the receipt.  This value is derived
     * from the TenderableTransactionIfc returned by getTransaction(). 
     */
    public CurrencyIfc getTotalTenderAmount();

    /**
     * @return Returns the eReceiptFileNameAddition.
     */
    public String getEReceiptFileNameAddition();

    /**
     * @param receiptFileNameAddition The eReceiptFileNameAddition to set.
     */
    public void setEReceiptFileNameAddition(String receiptFileNameAddition);

    /**
     * Returns the 1st part of the credit card promotion description for display
     * on the receipt.
     * 
     * @return
     */
    public String getCreditCardPromotionDescriptionPart1();

    /**
     * Sets the 1st part of the credit card promotion description for display
     * on the receipt.
     * 
     * @param creditCardPromotionDescriptionPart1
     */
    public void setCreditCardPromotionDescriptionPart1(String firstPart);

    /**
     * Returns the 2nd part of the credit card promotion description for display
     * on the receipt.
     * 
     * @return The 2nd part of the credit card promotion description for display
     * on the receipt.
     */
    public String getCreditCardPromotionDescriptionPart2();

    /**
     * Sets the 2nd part of the credit card promotion description for display
     * on the receipt.
     * 
     * @param creditCardPromotionDescriptionPart2
     */
    public void setCreditCardPromotionDescriptionPart2(String creditCardPromotionDescriptionPart2);

    /**
     * Returns the credit card promotion duration.
     * 
     * @return The credit card promotion duration.
     */
    public String getCreditCardPromotionDuration();

    /**
     * Sets the credit card promotion duration.
     * 
     * @param creditCardPromotionDuration
     */
    public void setCreditCardPromotionDuration(String creditCardPromotionDuration);

    /**
     * Returns the formatted credit card account rate information.
     * 
     * @return The formatted credit card account rate information.
     */
    public String getFormattedCreditCardAccountRate();

    /**
     * Sets the formatted credit card account rate information.
     * 
     * @param formattedCreditCardAccountRate
     * @return The formatted credit card account rate information.
     */
    public void setFormattedCreditCardAccountRate(String formattedCreditCardAccountRate);

    /**
     * Returns the formatted credit card promotion rate information.
     * 
     * @return The formatted credit card promotion rate information.
     */
    public String getFormattedCreditCardPromotionRate();

    /**
     * Returns the formatted credit card promotion rate information.
     * 
     * @param formattedCreditCardPromotionRate
     */
    public void setFormattedCreditCardPromotionRate(String formattedCreditCardPromotionRate);

    
  public String  getEmployeeName();
  
  public String getEmployeeId() ;

	/**
	 * @param employeeId the employeeId to set
	 */
	public void setEmployeeId(String employeeId) ;

	/**
	 * @return the remainingSpend
	 */
	public String getRemainingSpend() ;

	/**
	 * @param remainingSpend the remainingSpend to set
	 */
	public void setRemainingSpend(String remainingSpend);

	/**
	 * @param employeeName the employeeName to set
	 */
	public void setEmployeeName(String employeeName); 
}
