/* ===========================================================================
* Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/services/pricing/CheckDiscountAllowedSite.java /rgbustores_13.4x_generic_branch/1 2011/05/05 14:06:00 mszekely Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    blarsen   12/21/10 - Fixed a few warnings. That is all.
 *    cgreene   05/26/10 - convert to oracle packaging
 *
 * ===========================================================================
 * $Log:
 *    4    360Commerce 1.3         11/15/2007 11:12:20 AM Christian Greene
 *         CR28668 - backed out changes for CR25850. When determing ifdiscount
 *          allowed at CheckDiscountAllowedSite, reset lineitems in cargo to
 *         only those that are allowable.
 *    3    360Commerce 1.2         3/31/2005 4:27:24 PM   Robert Pearse   
 *    2    360Commerce 1.1         3/10/2005 10:20:06 AM  Robert Pearse   
 *    1    360Commerce 1.0         2/11/2005 12:09:54 PM  Robert Pearse   
 *
 *   Revision 1.9  2004/03/22 03:49:27  cdb
 *   @scr 3588 Code Review Updates
 *
 *   Revision 1.8  2004/03/19 23:27:50  dcobb
 *   @scr 3911 Feature Enhancement: Markdown
 *   Code review cleanup.
 *
 *   Revision 1.7  2004/03/17 23:03:11  dcobb
 *   @scr 3911 Feature Enhancement: Markdown
 *   Code review modifications.
 *
 *   Revision 1.6  2004/03/02 16:03:19  dcobb
 *   @scr 3911 Feature Enhancement: Markdown
 *   Updated javadoc.
 *
 *   Revision 1.5  2004/02/24 23:43:16  cdb
 *   @scr 3588 Corrected problem with CheckDiscountAllowedSite,
 *   was disallowing all items that were not damage discountable.
 *   Made it abstract with CheckemployeeDiscountAllowedSite
 *   and CheckItemDiscountAllowedSite extending it.
 *
 *   Revision 1.4  2004/02/24 16:21:31  cdb
 *   @scr 0 Remove Deprecation warnings. Cleaned code.
 *
 *   Revision 1.3  2004/02/20 23:20:29  cdb
 *   @scr 3588 Updating Discount Options behavior to meet requirements.
 *
 *   Revision 1.2  2004/02/20 22:25:22  cdb
 *   @scr 3588 Added discount allowed checking for item discounts.
 *
 *   Revision 1.1  2004/02/16 15:49:58  cdb
 *   @scr 3588 Moved common file (CheckDiscountAllowedSite)
 *   to pricing package.
 *
 * ===========================================================================
 */
package oracle.retail.stores.pos.services.pricing;

import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.services.PosSiteActionAdapter;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

//--------------------------------------------------------------------------
/**
    This site checks the selected item(s) for the discount allowed flag and 
    displays the invalid discount dialog or the discount items error dialog.
    Sites which extend this site must supply the abstract methods:
        isDiscountAllowed(SaleReturnLineItemIfc srli)
        getMultipleInvalidDiscountDialogArg(UtilityManagerIfc utility)
        getInvalidDiscountDialogResourceID()
    @version $Revision: /rgbustores_13.4x_generic_branch/1 $
**/
//--------------------------------------------------------------------------
public abstract class CheckDiscountAllowedSite extends PosSiteActionAdapter
{
    /** revision number supplied by version control **/
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";
    
    //----------------------------------------------------------------------
    /**
        Checks the selected item(s) for the discount allowed flag and 
        displays the invalid discount dialog or the discount items error 
        dialog if there is a problem; otherwise mails the "Next" letter.
        @param  bus     Service Bus
    **/
    //----------------------------------------------------------------------
    public void arrive(BusIfc bus)
    {
    	logger.info("inside arrive method of CheckDiscountAllowedSite");
        PricingCargo cargo = (PricingCargo) bus.getCargo();
        SaleReturnLineItemIfc[] lineItems = cargo.getItems();
        if (lineItems == null)
        {
            lineItems = new SaleReturnLineItemIfc[1];
            lineItems[0] = cargo.getItem();
        }
        List<SaleReturnLineItemIfc> allowableLineItems = new ArrayList<SaleReturnLineItemIfc>();

        int disallowedItems = 0;
        for(int i=0; i < lineItems.length; i++)
        {
            if (!isDiscountAllowed(lineItems[i]))
            {
                disallowedItems++;
            }
            else
            {
                allowableLineItems.add(lineItems[i]);
            }
        }

        UtilityManagerIfc utility = (UtilityManagerIfc) bus.getManager(UtilityManagerIfc.TYPE);
        // Get required managers                                             
        POSUIManagerIfc ui= (POSUIManagerIfc) bus.getManager(UIManagerIfc.TYPE);
        
        /*if (disallowedItems == lineItems.length)
        {
        	logger.info("All items are not employee discount eligible");
        	System.out.println("All items are not employee discount eligible");
            // All items are not employee discount eligible
            showInvalidDiscountDialog(ui);
        }
        else if (disallowedItems > 0)
        {
            // Some items are disallowed
            String[] msg = new String[3];
            msg[0] = msg[1] = msg[2] = getMultipleInvalidDiscountDialogArg(utility);
            cargo.setItems(allowableLineItems.toArray(new SaleReturnLineItemIfc[0]));
            showMultipleInvalidDiscountDialog(ui, msg);
        }
        else
        {
 */           // Send the continue or success letter
            bus.mail(CommonLetterIfc.NEXT, BusIfc.CURRENT);
       // }
    }
        
    //----------------------------------------------------------------------
    /**
     *   Returns true if the discount is allowed on this line item. <P>
     *   @param  srli       The SaleReturnLineItemIfc to check
     *   @return true if the discount is allowed on this line item
     */
    //----------------------------------------------------------------------
    abstract public boolean isDiscountAllowed(SaleReturnLineItemIfc srli);

    //----------------------------------------------------------------------
    /**
     *   Returns ARG of the MultipleInvalidDiscountDialog. <P>
     *   @param  utility       The UtilityManagerIfc with the bundle resources
     *   @return The ARG of the MultipleInvalidDiscountDialog
     */
    //----------------------------------------------------------------------
    abstract public String getMultipleInvalidDiscountDialogArg(UtilityManagerIfc utility);

    //----------------------------------------------------------------------
    /**
     *   Returns the Invalid Discount Dialog resource ID. <P>
     *   @return The ARG of the MultipleInvalidDiscountDialog
     */
    //----------------------------------------------------------------------
    public abstract String getInvalidDiscountDialogResourceID();

    //----------------------------------------------------------------------
    /**
     *   Displays the discount not allowed error screen. <P>
     *   @param  ui       The POSUIManager
     */
    //----------------------------------------------------------------------
    protected void showInvalidDiscountDialog(POSUIManagerIfc ui)
    {
        // display the invalid discount error screen
        DialogBeanModel dialogModel = new DialogBeanModel();
        dialogModel.setResourceID(getInvalidDiscountDialogResourceID());
        dialogModel.setType(DialogScreensIfc.ERROR);
        dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK,CommonLetterIfc.CANCEL);
        ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
    }

    //----------------------------------------------------------------------
    /**
     *   Displays the multiple "not allowed" discount error screen. <P>
     *   @param  ui       The POSUIManager
     *   @param  msg      The string array representing the arguments for
     *                    the dialog
     */
    //----------------------------------------------------------------------
    protected void showMultipleInvalidDiscountDialog(POSUIManagerIfc ui, String[] msg)
    {
        // display the invalid discount error screen
        DialogBeanModel dialogModel = new DialogBeanModel();
        dialogModel.setResourceID(PricingCargo.MULTIPLE_DISCOUNT_NOT_ALLOWED);
        dialogModel.setType(DialogScreensIfc.ERROR);
        dialogModel.setArgs(msg);
        dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK, CommonLetterIfc.NEXT);
        ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);
    }

}
