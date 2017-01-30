//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.services.printing;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
//NDE START - Add functionality to allow HTML in e-receipt text
import java.util.regex.PatternSyntaxException;
//NDE END - Add functionality to allow HTML in e-receipt text


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import oracle.retail.stores.common.constants.ItemLevelMessageConstants;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc;
import oracle.retail.stores.domain.registry.RegistryIDIfc;
import oracle.retail.stores.domain.stock.MessageDTO;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.stock.ReceiptFooterMessageDTO;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.device.DeviceException;
import oracle.retail.stores.foundation.manager.device.LocalizedDeviceException;
import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.foundation.tour.service.SessionBusIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.receipt.AlterationReceiptParameterBeanIfc;
import oracle.retail.stores.pos.receipt.GiftReceiptParameterBeanIfc;
import oracle.retail.stores.pos.receipt.PrintableDocumentException;
import oracle.retail.stores.pos.receipt.PrintableDocumentManager;
import oracle.retail.stores.pos.receipt.PrintableDocumentManagerIfc;
import oracle.retail.stores.pos.receipt.ReceiptConstantsIfc;
import oracle.retail.stores.pos.receipt.ReceiptParameterBeanIfc;
import oracle.retail.stores.pos.receipt.ReceiptTypeConstantsIfc;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.services.printing.PrintTransactionReceiptAisle;
import oracle.retail.stores.pos.services.printing.PrintingCargo;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.UIUtilities;
import oracle.retail.stores.pos.ui.beans.DataInputBeanModel;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;
import oracle.retail.stores.pos.ui.beans.POSBaseBeanModel;
import oracle.retail.stores.pos.ui.beans.StatusBeanModel;
import oracle.retail.stores.utility.EmailInfo;


import com.gdyn.orpos.pos.common.parameter.GDYNParameterConstantsIfc;
import com.gdyn.orpos.pos.receipt.GDYNReceiptParameterBeanIfc;
import com.gdyn.orpos.pos.receipt.GDYNReceiptTypeConstantsIfc;
import com.gdyn.orpos.pos.receipt.blueprint.GDYNBlueprintedDocumentManagerIfc;

/**
* Print the receipt.
*
* Overridden to print according to GDYN rules.
* @author LSlepetis
*/
public class GDYNPrintTransactionReceiptAisle extends PrintTransactionReceiptAisle implements
        GDYNReceiptTypeConstantsIfc
{
   private static final long serialVersionUID = 2465321947356780336L;

   /**
    * Print the receipt and send a letter
    *
    * @param bus the bus traversing this lane
    */
   @Override
   public void traverse(BusIfc bus)
   {
       // get transaction from cargo
       PrintingCargo cargo = (PrintingCargo)bus.getCargo();
       TenderableTransactionIfc trans = cargo.getTransaction();
       List<ReceiptFooterMessageDTO> returnedFooterMessages = null; // Grouped Return Messages from Returns Management
       List<ReceiptFooterMessageDTO> itemLevelReceiptFooterMessages = null; // ILRM Footer Messages
       ReceiptFooterMessageDTO[] receiptFooterMessages = null; // Group the above 2 into 1 array

       // Ashwini commented the below lines of code 
       // to prevent setting customer locale as preferred locale
       //during receipt printing
       
      /* // Create the object of customer.
       CustomerIfc cust = trans.getCustomer();
       Locale locale = null;
       // If customer is already selected, object of Locale should be stored in locale reference
       if (cust != null)
       {
           locale = cust.getPreferredLocale();
       }
       // If locale is not null, put the object into the LocaleMap.
       if (locale != null)
       {
           UIUtilities.setUILocaleForCustomer(locale);
       }*/

       // Ashwini added: to set store's default locale as preferred locale
       Locale locale  = LocaleMap.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);
       // If locale is not null, put the object into the LocaleMap.
       if (locale != null)
       {
           UIUtilities.setUILocaleForCustomer(locale);
       }
       // Ashwini's changes end
       
       // initialize variables
       boolean duplicateReceipt = cargo.isDuplicateReceipt();
       boolean sendMail = true;
       boolean mailServerOffline = false;
       boolean groupLikeFooterMessages = !(Gateway.getBooleanProperty("domain","DuplicateReceiptFooterMessages", false));

       cargo.setReceiptPrinted(false);
       // get ui and utility manager
       POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
       UtilityManagerIfc utility = (UtilityManagerIfc)bus.getManager(UtilityManagerIfc.TYPE);

       if (trans instanceof SaleReturnTransactionIfc)
       {
           SaleReturnTransactionIfc saleReturnTransactionIfc = (SaleReturnTransactionIfc)trans;
           checkforDuplicateLineItems((SaleReturnLineItemIfc[])saleReturnTransactionIfc.getLineItems());
           itemLevelReceiptFooterMessages = groupLikeFooterMessages((SaleReturnLineItemIfc[])saleReturnTransactionIfc.getLineItems(), groupLikeFooterMessages);
           returnedFooterMessages = groupDuplicateReturnMessages(trans, groupLikeFooterMessages);
           returnedFooterMessages.addAll(itemLevelReceiptFooterMessages);
           receiptFooterMessages = new ReceiptFooterMessageDTO[returnedFooterMessages.size()];
           receiptFooterMessages = returnedFooterMessages.toArray(receiptFooterMessages);
       }

       try
       {
           GDYNBlueprintedDocumentManagerIfc manager = (GDYNBlueprintedDocumentManagerIfc)bus.
                   getManager(GDYNBlueprintedDocumentManagerIfc.TYPE);
          
           GDYNReceiptParameterBeanIfc parameters = (GDYNReceiptParameterBeanIfc)manager.getReceiptParameterBeanInstance((SessionBusIfc)bus, trans);
           
           if (receiptFooterMessages != null && receiptFooterMessages.length > 0)
           {
               parameters.setReturnReceiptFooterMsgs(receiptFooterMessages);
           }
           parameters.setDuplicateReceipt(duplicateReceipt);
           // set only if Type 2
           if (PrintableDocumentManagerIfc.STYLE_VAT_TYPE_2.equalsIgnoreCase(cargo.getReceiptStyle()))
           {
               parameters.setReceiptStyle(cargo.getReceiptStyle());
           }
           // reset cargo receipt style
           cargo.setReceiptStyle(PrintableDocumentManagerIfc.STYLE_NORMAL);

           // If SaleReturnTransaction and
           // if the is a return ticket (i.e. used returns management) and
           // if there are no line items...
           if (trans instanceof SaleReturnTransactionIfc &&
               trans.getReturnTicket() != null           &&
               ((SaleReturnTransactionIfc)trans).getLineItemsSize() == 0)
           {
               // The returns denied document
               parameters.setDocumentType(ReceiptTypeConstantsIfc.RETURNS_DENIED);
           }

           // Begin GD-150: Modify UI based on PLAF settings
           // lcatania (Starmount) Feb 8, 2013
           ParameterManagerIfc pm = (ParameterManagerIfc)bus.getManager("ParameterManager");            
           String[] storeWebSiteArray = pm.getStringValues(GDYNParameterConstantsIfc.Store_Website);
           parameters.setStoreWebSiteArray(storeWebSiteArray);
           // End GD-150: Modify UI based on PLAF settings
           
           // Initialize this

           // Print eReceipt; this will be true only for Sale Return Transactions
           if (cargo.isPrintEreceipt())
           {
               parameters.setEReceiptFileNameAddition(ReceiptConstantsIfc.SALE_RETURN_FILE_NAME_ADDITION);
               try
               {
                   // Set flag to print eReceipt
                   parameters.setEreceipt(true);
                   // Print Receipt
                   ArrayList<String> fileAdditions = printAllReciepts((SessionBusIfc)bus, parameters);
                   // Email the printed receipt
                   emailEreceipt(bus, fileAdditions);
               }
               catch (Exception e)
               {
                   logger.warn("Unable to send eReceipt:", e);
                   // set mail server offline.
                   mailServerOffline = true;
                   // set to print paper copy
                   cargo.setPrintPaperReceipt(true);
               }

               // Reset the ereceipt flag.
               parameters.setEreceipt(false);
           }

           // Print paper receipt
           if (cargo.isPrintPaperReceipt())
           {
               printAllReciepts((SessionBusIfc)bus, parameters);
               // reset PrintStoreReceipt flag to print all receipts.
               parameters.setPrintStoreReceipt(false);
           }
           
           if (parameters.isPrintStoreReceipt() || parameters.isShouldPrintTaxExemptInfo()) // Print only store receipts.
           {
               // Print Receipt if customer gets eReceipt and/or this is a tax exempt txn
               PrintableDocumentManagerIfc printManager = (PrintableDocumentManagerIfc)bus.
                   getManager(PrintableDocumentManagerIfc.TYPE);
               printStoreCopyReceipt(printManager, (SessionBusIfc) bus, parameters);
               
                // Begin GD-334: When applying both a tax exemption and an employee discount to the same tran - no
                // employee signature line prints
                // lcatania (Starmount) Mar 26, 2013
                if (!Util.isEmpty(parameters.getDiscountEmployeeNumber()))
                {
                    printEmployeeDiscountStoreCopyReceipt(printManager, (SessionBusIfc) bus, parameters);
                }
                // End GD-334: When applying both a tax exemption and an employee discount to the same tran - no
                // employee signature line prints
               
               // reset PrintStoreReceipt flag
               parameters.setPrintStoreReceipt(false);
           }
           
           cargo.setReceiptPrinted(true);
           // save the count of reprinted receipts for the reprint receipt
           // service
           cargo.setReprintReceiptCount(cargo.getReprintReceiptCount() + 1);
           // Update printer status
           setPrinterStatus(true, bus);

           // display MailServerOffline mesasge if email server is down.
           if (mailServerOffline)
           {
               displayEmailServerOfflineDialog(ui);
               sendMail = false;
           }
       }
       // handle device exception
       catch (PrintableDocumentException e)
       {
           logger.error("Unable to print receipt: " + e.getMessage());
           // Update printer status
           setPrinterStatus(false, bus);

           if (e.getCause() != null)
           {
               logger.error("DeviceException.NestedException:\n" + Util.throwableToString(e.getCause()));
           }

           String msg[] = new String[1];

           if (e.getCause() instanceof LocalizedDeviceException)
           {
               msg[0] = e.getCause().getLocalizedMessage();
           }
           else if (e.getCause() instanceof DeviceException
                   && ((DeviceException)e.getCause()).getErrorCode() != DeviceException.UNKNOWN)
           {
               msg[0] = utility.retrieveDialogText("RetryContinue.PrinterOffline", "Printer is offline.");
           }
           else
           {
               msg[0] = utility.retrieveDialogText("RetryContinue.UnknownPrintingError",
                       "An unknown error occurred while printing.");
           }

           DialogBeanModel model = new DialogBeanModel();
           model.setResourceID("RetryContinue");
           model.setType(DialogScreensIfc.RETRY_CONTINUE);
           model.setArgs(msg);
           // display dialog
           ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, model);

           sendMail = false;
       }
       // parameter exception handled in utility manager
       catch (ParameterException pe)
       {
           logger.error("A receipt parameter could not be retrieved from the ParameterManager.  "
                   + "The following exception occurred: " + pe.getMessage());
       }

       if (sendMail)
       {
           bus.mail(new Letter(CommonLetterIfc.CONTINUE), BusIfc.CURRENT);
       }
   }

   private void printStoreCopyReceipt(PrintableDocumentManagerIfc printableDocumentManager, SessionBusIfc bus,
           GDYNReceiptParameterBeanIfc parameters)           
   {
       parameters.setDocumentType(STORE_COPY_RECEIPT);
       try
       {
           printableDocumentManager.printReceipt(bus, parameters);
       }
       catch (PrintableDocumentException e)
       {
           // Update printer status
           logger.error("Unable to print GDYN Store Copy Receipt: ", e);
       }

   }
   
    // Begin GD-334: When applying both a tax exemption and an employee discount to the same tran - no employee
    // signature line prints
    // lcatania (Starmount) Mar 26, 2013
    private void printEmployeeDiscountStoreCopyReceipt(PrintableDocumentManagerIfc printableDocumentManager,
            SessionBusIfc bus,
            GDYNReceiptParameterBeanIfc parameters)
    {
        parameters.setDocumentType(EMPLOYEE_DISCOUNT);
        try
        {
            printableDocumentManager.printReceipt(bus, parameters);
        }
        catch (PrintableDocumentException e)
        {
            // Update printer status
            logger.error("Unable to print GDYN Employee Discount Store Copy Receipt: ", e);
        }

    }
    // End GD-334: When applying both a tax exemption and an employee discount to the same tran - no employee signature
    // line prints

   /**
    * Print all the reciepts that can accompany a sale or return.
    * @param bus
    * @param parameters
    * @return the list of additons to file name.  This list only matters to email reciept code;
    *         each different type of
    * @throws PrintableDocumentException
    */
   protected ArrayList<String> printAllReciepts(SessionBusIfc bus,
           ReceiptParameterBeanIfc parameters) throws PrintableDocumentException
   {
       ArrayList<String> fileAdditions = new ArrayList<String>();
       // Print the regular sale return reciept

       fileAdditions.add(parameters.getEReceiptFileNameAddition());
       PrintableDocumentManagerIfc printManager = (PrintableDocumentManagerIfc)bus.
           getManager(PrintableDocumentManagerIfc.TYPE);
       printManager.printReceipt(bus, parameters);

       if (parameters.isPrintGiftReceipt())
       {
           ArrayList<String> additions = printGiftReceipts(bus, parameters);
           if (!additions.isEmpty())
           {
               fileAdditions.addAll(additions);
           }
       }

       if (parameters.getTransaction().getTransactionType() == TransactionIfc.TYPE_SALE ||
           parameters.getTransaction().getTransactionType() == TransactionIfc.TYPE_RETURN ||
           parameters.getTransaction().getTransactionType() == TransactionIfc.TYPE_ORDER_INITIATE)
       {
           String addition = printRebateReceipts(bus, parameters);
           if (addition != null)
           {
               fileAdditions.add(addition);
           }
       }

       if (parameters.isPrintAlterationReceipt())
       {
           String addition = printAlterationReceipts(bus, parameters);
           if (addition != null)
           {
               fileAdditions.add(addition);
           }
       }

       return fileAdditions;
   }

   /**
    * Print Alteration Receipts
    * @param bus
    * @param parameters
    * @return String additional file name text
    * @throws PrintableDocumentException
    */
   protected String printAlterationReceipts(SessionBusIfc bus,
           ReceiptParameterBeanIfc parameters) throws PrintableDocumentException
   {
       // Get the print mannger
       PrintableDocumentManagerIfc printManager =
           (PrintableDocumentManagerIfc)bus.
               getManager(PrintableDocumentManagerIfc.TYPE);

       // Get the aleration bean and initialize it.
       AlterationReceiptParameterBeanIfc altBean =
           (AlterationReceiptParameterBeanIfc)printManager.
               getParameterBeanInstance(ReceiptTypeConstantsIfc.ALTERATION);
       altBean.setLocale(parameters.getLocale());
       altBean.setDefaultLocale(parameters.getDefaultLocale());
       altBean.setTransaction(parameters.getTransaction());
       altBean.setEreceipt(parameters.isEreceipt());
       altBean.setEReceiptFileNameAddition(
               ReceiptConstantsIfc.ALTERATION_FILE_NAME_ADDITION);

       // Print the receipt
       printManager.printReceipt(bus, altBean);

       return altBean.getEReceiptFileNameAddition();
   }

   /**
    * Print Rebate Receipts
    * @param bus
    * @param parameters
    * @return String additional file name text
    * @throws PrintableDocumentException
    */
   protected String printRebateReceipts(SessionBusIfc bus,
           ReceiptParameterBeanIfc parameters) throws PrintableDocumentException
   {
       String addition = null;

       AbstractTransactionLineItemIfc[] li = parameters.getLineItems();
       for (int i = li.length - 1; i >= 0; i--)
       {
           if (li[i] instanceof SaleReturnLineItemIfc)
           {
               String rebateMessage = ((SaleReturnLineItemIfc)li[i]).getItemRebateMessage();
               if (!Util.isEmpty(rebateMessage))
               {
                   parameters.setEReceiptFileNameAddition(ReceiptConstantsIfc.REBATE_FILE_NAME_ADDITION);
                   addition = parameters.getEReceiptFileNameAddition();
                   String documentType = parameters.getDocumentType();
                   parameters.setDocumentType(ReceiptTypeConstantsIfc.REBATE);

                   // Get the print mannger and print the rebates
                   PrintableDocumentManagerIfc printManager =
                       (PrintableDocumentManagerIfc)bus.
                           getManager(PrintableDocumentManagerIfc.TYPE);

                   // print a rebate for each item in the line
                   for (int j = 0; j < li[i].getItemQuantityDecimal().intValue(); j++)
                   {
                       printManager.printReceipt(bus, parameters);
                   }

                   // Reset the document type the original type
                   parameters.setDocumentType(documentType);

                   // For the document type REBATE, the printManager prints
                   // all the rebates at once.  This breaks out of the loop.
                   break;
               }
           }
       }

       return addition;
   }

   /**
    * Method to print gift receipts
    *
    * @param bus
    * @param receiptParameters
    * @throws PrintableDocumentException
    */
   protected ArrayList<String> printGiftReceipts(SessionBusIfc bus, ReceiptParameterBeanIfc receiptParameters)
           throws PrintableDocumentException
   {
       // Initial return
       ArrayList<String> additions = new ArrayList<String>();
       int giftReceiptCounter = 1;

       PrintableDocumentManagerIfc printManager = (PrintableDocumentManagerIfc)bus
               .getManager(PrintableDocumentManagerIfc.TYPE);
       // Get the lineItems from the transaction.
       SaleReturnTransactionIfc srTrans = (SaleReturnTransactionIfc)receiptParameters.getTransaction();
       AbstractTransactionLineItemIfc[] lineItems = srTrans.getLineItems();
       GiftReceiptParameterBeanIfc giftReceiptParameterBean = printManager.getGiftReceiptParameterBeanInstance(bus,
               receiptParameters);
       giftReceiptParameterBean.setEreceipt(receiptParameters.isEreceipt());

       if (srTrans.isTransactionGiftReceiptAssigned())
       {
           ArrayList<SaleReturnLineItemIfc> srliList = new ArrayList<SaleReturnLineItemIfc>();
           for (int i = 0; i < lineItems.length; i++)
           {
               if (lineItems[i] instanceof SaleReturnLineItemIfc &&
                   !((SaleReturnLineItemIfc)lineItems[i]).isReturnLineItem())
               {
                   srliList.add((SaleReturnLineItemIfc)lineItems[i]);
               }
           }

           if (!srliList.isEmpty())
           {
               // set only the salereturnlineitems
               giftReceiptParameterBean.setSaleReturnLineItems(srliList
                       .toArray(new SaleReturnLineItemIfc[srliList.size()]));
               // print the transaction
               giftReceiptParameterBean.
                   setEReceiptFileNameAddition(getGiftCardAddition(giftReceiptCounter));
               printManager.printReceipt(bus, giftReceiptParameterBean);
               additions.add(giftReceiptParameterBean.getEReceiptFileNameAddition());
               giftReceiptCounter++;
           }
       }
       else
       // else not transaction receipt, print a receipt for each line item
       {
           Map<CustomerIfc, List<SaleReturnLineItemIfc>> mapSendGifts = new HashMap<CustomerIfc, List<SaleReturnLineItemIfc>>();
           for (int i = 0; i < srTrans.getLineItemsSize(); i++)
           {
               if (lineItems[i] instanceof SaleReturnLineItemIfc &&
                       !((SaleReturnLineItemIfc)lineItems[i]).isReturnLineItem())
               {
                   SaleReturnLineItemIfc srli = (SaleReturnLineItemIfc)lineItems[i];

                   if (!((PrintableDocumentManager)printManager).hasDamageDiscounts(srli))
                   {
                       // Get gift registry for this item
                       RegistryIDIfc giftRegistry = srli.getRegistry();

                       // If item is marked for a gift receipt, or the item is
                       // linked to a gift
                       // registry and the AutoPrintGiftReceipt parameter is
                       // set, then print a gift receipt.
                       if (srli.isGiftReceiptItem()
                               || (giftRegistry != null && receiptParameters.isAutoPrintGiftReceiptGiftRegistry())
                               || (srli.getItemSendFlag() && receiptParameters.isAutoPrintGiftReceiptItemSend()))
                       {
                           if (srli.getItemSendFlag())
                           {
                               SendPackageLineItemIfc spli = srTrans.getTransactionTotals().getSendPackage(
                                       srli.getSendLabelCount() - 1);
                               // add the item to list mapped by send
                               // destination
                               List<SaleReturnLineItemIfc> list = mapSendGifts.get(spli.getCustomer());
                               if (list == null)
                               {
                                   list = new ArrayList<SaleReturnLineItemIfc>();
                                   mapSendGifts.put(spli.getCustomer(), list);
                               }
                               list.add(srli);
                           }
                           else
                           // if not send gift, print normal gift receipt
                           {
                               // set only the current lineitem
                               giftReceiptParameterBean.setSaleReturnLineItems(new SaleReturnLineItemIfc[] { srli });
                               giftReceiptParameterBean.
                                   setEReceiptFileNameAddition(getGiftCardAddition(giftReceiptCounter));
                               printManager.printReceipt(bus, giftReceiptParameterBean);
                               additions.add(giftReceiptParameterBean.getEReceiptFileNameAddition());
                               giftReceiptCounter++;
                           }
                       }
                   } // end if no damage discounts
               }
           } // end for
           // if any send gifts were found, print them in groups
           if (mapSendGifts.size() > 0)
           {
               for (List<SaleReturnLineItemIfc> items : mapSendGifts.values())
               {
                   giftReceiptParameterBean.setSaleReturnLineItems(items.toArray(new SaleReturnLineItemIfc[items
                           .size()]));
                   giftReceiptParameterBean.
                       setEReceiptFileNameAddition(getGiftCardAddition(giftReceiptCounter));
                   printManager.printReceipt(bus, giftReceiptParameterBean);
                   additions.add(giftReceiptParameterBean.getEReceiptFileNameAddition());
                   giftReceiptCounter++;
               }
           }
       }

       return additions;
   }

   /*
    * Build and return the gift card addtion based on the counter
    */
   private String getGiftCardAddition(int giftReceiptCounter)
   {
       String addition = null;
       if (giftReceiptCounter == 1)
       {
           addition = ReceiptConstantsIfc.GIFT_FILE_NAME_ADDITION;
       }
       else
       {
           addition = ReceiptConstantsIfc.GIFT_FILE_NAME_ADDITION + giftReceiptCounter;
       }

       return addition;
   }

   /**
    * This method displays the Email Server Offline dialog.
    *
    * @param ui
    */
   private void displayEmailServerOfflineDialog(POSUIManagerIfc ui)
   {
       DialogBeanModel model = new DialogBeanModel();
       model.setResourceID("MailServerOffline");
       model.setType(DialogScreensIfc.ACKNOWLEDGEMENT);
       model.setButtonLetter(DialogScreensIfc.BUTTON_OK, CommonLetterIfc.CONTINUE);
       ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, model);
   }

   /**
    * This method email the eReceipt.
    *
    * @param bus
    * @throws PrintableDocumentException
    */
   private void emailEreceipt(BusIfc bus, ArrayList<String> fileAdditions) throws PrintableDocumentException
   {
       PrintingCargo cargo = (PrintingCargo)bus.getCargo();
       TenderableTransactionIfc trans = cargo.getTransaction();

       POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
       // get the UIModel
       DataInputBeanModel model = (DataInputBeanModel)ui.getModel(POSUIManagerIfc.ERECEIPT_EMAIL_SCREEN);
       // get the email id from model.
       String email = (String)model.getValue("email");

       ParameterManagerIfc pm = (ParameterManagerIfc)bus.getManager(ParameterManagerIfc.TYPE);
       // read the email subject from parameter
       String[] subject = cargo.getReceiptText(pm, "eReceiptSubject");
       StringBuilder completeSubect = new StringBuilder();
       for (String i : subject)
       {
           completeSubect.append(i).append(" ");
       }
       // read the email text from parameter
       String[] message = cargo.getReceiptText(pm, "eReceiptText");
       StringBuilder completeMsg = new StringBuilder();
       for (String i : message)
       {
    	   // NDE START - Add functionality to allow HTML in e-receipt text
	   	   try 
	   	   {
	   		   i = i.replaceAll("&amp;", "&"); // replace all encoded ampersands with &
	   		   i = i.replaceAll("&gt;", ">"); // Replace all encoded greater thans with >
	   		   i = i.replaceAll("&lt;", "<"); // Replace all encoded less thans with <
	   		   i = i.replaceAll("&quot;", "\""); // Replace all encoded double quotes with "
	   	    }
	   		catch (PatternSyntaxException pe)
	   		{
	   			throw new PrintableDocumentException("Unable to decode html for e-receipt.", pe);
	        }
	        finally
	        {
	        }
    	    // NDE END - Add functionality to allow HTML in e-receipt text
           completeMsg.append(i).append("\n");
       }
       // read the mail server host from application.properties
       String host = Gateway.getProperty("application", "mail.smtp.host", "");
       // read the mail server port from application.properties
       String port = Gateway.getProperty("application", "mail.smtp.port", "");
       // read the eReceipt sender email id from from application.properties
       String user = Gateway.getProperty("application", "mail.ereceipt.sender", "");
       // read the email server timeout from application.properties
       String mailServerTimeout = Gateway.getProperty("application", "mail.smtp.timeout", "1000");
       // read the email server connection timeout from application.properties
       String mailServerConnectionTimeout = Gateway.getProperty("application", "mail.smtp.connection.timeout", "1000");

       // create the eReceipt pdf file name from transaction id
       ArrayList<String> fileNames = new ArrayList<String>();
       for(String addition: fileAdditions)
       {
           fileNames.add(trans.getTransactionID() + addition + ".pdf");
       }

       String[] fileList = new String[fileNames.size()];
       fileNames.toArray(fileList);

       // create EmailInfo object with all email information.
       EmailInfo info = new EmailInfo(host, port, mailServerConnectionTimeout, mailServerTimeout, user, email, fileList,
               completeSubect.toString(), completeMsg.toString());
       try
       {
           // send the Email.
    	   //System.out.println("Before SendEmail.send not called");
           // SendEmail.send(info);
          // System.out.println("After SendEmail.send not called");
           // NDE send 2nd email
          // System.out.println("Before sendEmail");
    	   sendEmail(info);
           //System.out.println("After sendEmail");
           
       }
       catch (Exception e)
       {
           throw new PrintableDocumentException("Unable to email pdf receipt.", e);
       }
       finally
       {
           for(String fileName: fileNames)
           {
               // delete the pdf file if exists.
               File pdfFile = new File(fileName);
               if (pdfFile.exists())
               {
                   pdfFile.delete();
               }
           }
       }
   }
   
   public void sendEmail(EmailInfo info) throws PrintableDocumentException {
	   
	   try {

           Properties props = new Properties();
           // e.g. props.put("mail.smtp.host", "128.127.125.130");
           props.put("mail.smtp.host", info.getMailServer());
           // e.g. props.put("mail.smtp.port", "25");
           props.put("mail.smtp.port", info.getMailServerPort());
           // e.g. props.put("mail.smtp.timeout", "2500");
           props.put("mail.smtp.timeout", info.getMailServerTimeout());
           // e.g. props.put("mail.smtp.connectiontimeout", "2500");
           props.put("mail.smtp.connectiontimeout", info.getMailServerConnectionTimeout());
           //Added by Monica on 20/11/2015 for Email Server Error
           String domainName = Gateway.getProperty("application", "mail.smtp.domainname", "");
           props.put("mail.smtp.localhost", InetAddress.getLocalHost().getHostName().concat(".").concat(domainName));           
           //System.out.println("SMTP Server Used is" +InetAddress.getLocalHost().getHostName().concat(".").concat(domainName));
           logger.info("SMTP Server Used is" +InetAddress.getLocalHost().getHostName().concat(".").concat(domainName));
           //Changes end here
           
           Session session = Session.getDefaultInstance(props);
           MimeMessage message = new MimeMessage(session);
           message.setSubject(info.getSubject(), "UTF-8"); // e.g. "Your receipt from Dynamite"
          
           message.setFrom(new InternetAddress(info.getFrom())); // e.g. ereceipts@dynamite.ca
           message.setReplyTo(new Address[]{new InternetAddress(info.getFrom())}); // e.g. ereceipts@dynamite.ca
           String toAddress = info.getTo(); // e.g. "nedmundson@dynamite.ca";
           message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
    
           // Cover wrap
           MimeBodyPart wrap = new MimeBodyPart();
    
           MimeMultipart cover = new MimeMultipart("alternative");
           MimeBodyPart html = new MimeBodyPart();
           cover.addBodyPart(html);
           
           wrap.setContent(cover);
    
           MimeMultipart content = new MimeMultipart("related");
           message.setContent(content);
           content.addBodyPart(wrap);
    
           String[] attachmentsFiles = info.getAttachments();
           
           //StringBuilder sb  = new StringBuilder();
    
           for (String attachmentFileName : attachmentsFiles) {
           	   //System.out.println("attachment: " + attachmentFileName);
               
               MimeBodyPart attachment = new MimeBodyPart();
    
               DataSource fds = new FileDataSource(attachmentFileName);
               attachment.setDataHandler(new DataHandler(fds));
               attachment.setFileName(fds.getName());
    
               content.addBodyPart(attachment);
           }
    
           html.setContent(info.getMessage(), "text/html");
           // Send the message
           message.setSentDate(new Date());
           //System.out.println("Before HTML message send.");
           Transport.send(message);
           //System.out.println("HTML Message sent...");
       } catch(Exception ex) {
           //System.out.println("Failed to send email receipt.");
           logger.error("Unable to send eReceipt.", ex);
       	   ex.printStackTrace();
       	   throw new PrintableDocumentException("Unable to email pdf receipt.", ex); 
       }	   
	  
   }

   /**
    * Help create a clean receipt.
    *
    * @param lineItemArray
    */
   protected void checkforDuplicateLineItems(SaleReturnLineItemIfc[] lineItemArray)
   {
       int lengthOfLineItems = lineItemArray.length;
       PLUItemIfc item = null;
       Map<String, List<MessageDTO>> itemMessages = null;
       List<MessageDTO> messageDTOList = null;
       Set<String> keyset = null;
       Iterator<String> iter = null;
       MessageDTO msg = null;
       List<String> itemIds = new ArrayList<String>(lengthOfLineItems);

       for (int ctr = 0; ctr < lengthOfLineItems; ctr++)
       {

           item = lineItemArray[ctr].getPLUItem();
           if (itemIds.contains(item.getItemID()))
           {
               itemMessages = item.getAllItemLevelMessages();
               if (itemMessages != null)
               {
                   keyset = itemMessages.keySet();
                   iter = keyset.iterator();
                   while (iter.hasNext())
                   {
                       messageDTOList = itemMessages.get(iter.next());
                       if (messageDTOList != null)
                       {
                           for (int msgctr = 0; msgctr < messageDTOList.size(); msgctr++)
                           {
                               msg = messageDTOList.get(msgctr);
                               if (msg != null)
                               {
                                   msg.setDuplicate(true);
                               }
                           }
                       }
                   }
               }
               else
               {
                   itemIds.add(item.getItemID());
               }
           }
           else
           {
               itemIds.add(item.getItemID());
           }
       }
   }


   /**
    * Groups all the like footer messages. Also this
    * method stops the printing of duplicate Item IDs on the receipt footer.
    *
    * @param ReceiptFooterMessageDTO[]
    */
   protected List<ReceiptFooterMessageDTO> groupLikeFooterMessages(SaleReturnLineItemIfc[] lineItemArray, boolean isGroupMsgs)
   {
     int lengthOfLineItems = lineItemArray.length;
     Map<String, String> footerMessages = new HashMap<String, String>();
     Map<String, List<String>> itemIds = new HashMap<String, List<String>>();
     String msgID = null;
     String saleFooterMsg = null;
     String returnFooterMsg = null;
     List<String> itemList = null;
     List<String> msgIdlst = new ArrayList<String>(lengthOfLineItems);
     List<ReceiptFooterMessageDTO> rmFooterMsgs = null;
     ReceiptFooterMessageDTO footerMsg = null;


     if(isGroupMsgs)
     {
       for (int ctr = 0; ctr < lengthOfLineItems; ctr++)
       {
          saleFooterMsg = lineItemArray[ctr].getItemMessageID(ItemLevelMessageConstants.SALE, ItemLevelMessageConstants.FOOTER);
          returnFooterMsg  = lineItemArray[ctr].getItemMessageID(ItemLevelMessageConstants.RETURN, ItemLevelMessageConstants.FOOTER);


           if ((saleFooterMsg != null && saleFooterMsg.length() > 0) || (returnFooterMsg != null && returnFooterMsg.length() > 0))
           {
               msgID = lineItemArray[ctr].getItemMessageID(ItemLevelMessageConstants.SALE, ItemLevelMessageConstants.FOOTER);
               if(msgID == null || msgID.length() == 0)
               {
                msgID = lineItemArray[ctr].getItemMessageID(ItemLevelMessageConstants.RETURN, ItemLevelMessageConstants.FOOTER);
               }
               if (msgIdlst.contains(msgID))
               {
                   itemList = itemIds.get(msgID);
                   if (!itemList.contains(lineItemArray[ctr].getItemID()))
                   {
                       itemList.add(lineItemArray[ctr].getItemID());
                   }
               }
               else
               {
                   footerMessages.put(msgID, lineItemArray[ctr].getItemFooterMessage());
                   itemList = new ArrayList<String>();
                   itemList.add(lineItemArray[ctr].getItemID());
                   itemIds.put(msgID, itemList);
                   msgIdlst.add(msgID);
               }
           }
       }

       // Now returnMessages has unique Messages and itemIDs has unique item
       // IDs
       // both bound by message ID -- msgID and unique MessageIDs in the list
       rmFooterMsgs = new ArrayList<ReceiptFooterMessageDTO>(msgIdlst.size());

       for (int msgIdCtr = 0; msgIdCtr < msgIdlst.size(); msgIdCtr++)
       {
           footerMsg = new ReceiptFooterMessageDTO();
           itemList = itemIds.get(msgIdlst.get(msgIdCtr));
           footerMsg.setItemIds(getStringFromList(itemList));
           footerMsg.setItemMessage(footerMessages.get(msgIdlst.get(msgIdCtr)));
           rmFooterMsgs.add(footerMsg);
       }
     }
     else // Do not Group , just Add item ID and The Item Footer Message
     {
       rmFooterMsgs = new ArrayList<ReceiptFooterMessageDTO>(msgIdlst.size());

       for (int  ctr = 0; ctr < lineItemArray.length; ctr++)
       {
           footerMsg = new ReceiptFooterMessageDTO();
           footerMsg.setItemIds(lineItemArray[ctr].getItemID());
           footerMsg.setItemMessage(lineItemArray[ctr].getItemFooterMessage());
           rmFooterMsgs.add(footerMsg);
       }
     }

     // cleanup
     {
         footerMessages = null;
         itemIds = null;
         msgID = null;
         itemList = null;
         msgIdlst = null;
     }

     return rmFooterMsgs;// This is an Array of Messages along with comma
                         // separated grouped Item IDs
   }


   /**
    * Groups all the like messages which have been returned from RM. Also this
    * method stops the printing of duplicate Item IDs on the receipt footer.
    *
    * @param ReceiptFooterMessageDTO[]
    */
   protected List<ReceiptFooterMessageDTO> groupDuplicateReturnMessages(TenderableTransactionIfc trans, boolean isGroupMsgs)
   {

       SaleReturnTransactionIfc saleReturnTransaction = (SaleReturnTransactionIfc)trans;
       SaleReturnLineItemIfc[] lineItemArray = (SaleReturnLineItemIfc[])saleReturnTransaction.getLineItems();
       int lengthOfLineItems = lineItemArray.length;
       Map<String, String> returnMessages = new HashMap<String, String>();
       Map<String, List<String>> itemIds = new HashMap<String, List<String>>();
       String msgID = null;
       List<String> itemList = null;
       List<String> msgIdlst = new ArrayList<String>(lengthOfLineItems);
       List<ReceiptFooterMessageDTO> rmFooterMsgs = null;
       ReceiptFooterMessageDTO footerMsg = null;

       if(lengthOfLineItems > 0)
       {
           saleReturnTransaction.setReturnTicketID(trans.getReturnTicket());
       }

       for (int ctr = 0; ctr < lengthOfLineItems; ctr++)
       {
           if (lineItemArray[ctr].getReturnMessage() != null && lineItemArray[ctr].getReturnMessage().getMessageID() != null)
           {
               msgID = (lineItemArray[ctr].getReturnMessage().getMessageID()).toString();
               if (msgIdlst.contains(msgID))
               {
                   itemList = itemIds.get(msgID);
                   if (!itemList.contains(lineItemArray[ctr].getItemID()))
                   {
                       itemList.add(lineItemArray[ctr].getItemID());
                   }
                   lineItemArray[ctr].getReturnMessage().setDuplicate(true);
               }
               else
               {
                   returnMessages.put(msgID, lineItemArray[ctr].getReturnMessage().getReturnMessage());
                   itemList = new ArrayList<String>();
                   itemList.add(lineItemArray[ctr].getItemID());
                   itemIds.put(msgID, itemList);
                   msgIdlst.add(lineItemArray[ctr].getReturnMessage().getMessageID().toString());
               }
           }
       }

       // Now returnMessages has unique Messages and itemIDs has unique item
       // IDs
       // both bound by message ID -- msgID and unique MessageIDs in the list
       rmFooterMsgs = new ArrayList<ReceiptFooterMessageDTO>(msgIdlst.size());

       for (int msgIdCtr = 0; msgIdCtr < msgIdlst.size(); msgIdCtr++)
       {
           footerMsg = new ReceiptFooterMessageDTO();
           itemList = itemIds.get(msgIdlst.get(msgIdCtr));
           footerMsg.setItemIds(getStringFromList(itemList));
           footerMsg.setItemMessage(returnMessages.get(msgIdlst.get(msgIdCtr)));
           rmFooterMsgs.add(footerMsg);
       }


       // cleanup just in case...
       {
           returnMessages = null;
           itemIds = null;
           msgID = null;
           itemList = null;
           msgIdlst = null;
       }

       return rmFooterMsgs;// This is an Array of Messages along with comma
                           // separated grouped Item IDs
   }

   private String getStringFromList(List<String> itemLst)
   {
       StringBuffer commaSeparatedStrBuf = new StringBuffer();
       if (itemLst != null)
       {
           for (int itemCtr = 0; itemCtr < itemLst.size(); itemCtr++)
           {
               commaSeparatedStrBuf.append(itemLst.get(itemCtr));
               if (itemCtr != itemLst.size() - 1)
               {
                   commaSeparatedStrBuf.append(",");
               }
           }
       }
       return commaSeparatedStrBuf.toString();
   }

   protected static void setPrinterStatus(boolean online, BusIfc bus)
   {
       POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);

       StatusBeanModel statusModel = new StatusBeanModel();
       statusModel.setStatus(POSUIManagerIfc.PRINTER_STATUS, online);
       POSBaseBeanModel baseModel = new POSBaseBeanModel();
       baseModel.setStatusBeanModel(statusModel);
       ui.setModel(POSUIManagerIfc.SHOW_STATUS_ONLY, baseModel);
   }
}
