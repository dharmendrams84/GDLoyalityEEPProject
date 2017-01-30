/* ===========================================================================
* Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved. 
* ===========================================================================
* $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/ui/beans/StatusBean.java /rgbustores_13.4x_generic_branch/6 2011/10/27 16:12:04 cgreene Exp $
* ===========================================================================
* NOTES
* <other useful comments, qualifications, etc.>
*
* MODIFIED (MM/DD/YY)
*    cgreen 10/27/11 - implement popup dialog for customer info
*    cgreen 10/27/11 - refactored StackedStatusBean to extend StatusBean
*    icole  09/13/11 - Changes to better reflect correct Payment status.
*    icole  08/29/11 - Use a singleton for OnlineStatusContainer as multiple
*                      instances resulted in erroneous status.
*    blarse 06/30/11 - SIGNATURE_CAPTURE and PIN_PAD status were replaced with
*                      FINANCIAL_NETWORK status as part of the advance payment
*                      foundation feature.
*    rrkohl 05/25/11 - pos ui quickwin
*    cgreen 12/19/10 - add missing removeOnlineDeviceStatusListener that
*                      ConnectionSpec would use
*    cgreen 07/02/10 - use a URLLabel for statusField
*    cgreen 05/27/10 - convert to oracle packaging
*    cgreen 05/27/10 - convert to oracle packaging
*    cgreen 05/26/10 - convert to oracle packaging
*    abonda 01/03/10 - update header date
*    cgreen 04/01/09 - refactored cash under warning to animate from left side
*    acadar 02/09/09 - use default locale for display of date and time
*    nkgaut 09/24/08 - Check added for Cash UNDER Warning

* ===========================================================================
     $Log:
      6    360Commerce 1.5         5/15/2008 6:27:39 AM   Sishant Kumar
           Forward ported from V12x for CR 31357
      5    360Commerce 1.4         8/13/2007 12:25:34 PM  Mathews Kochummen use
            date service
      4    360Commerce 1.3         12/13/2005 4:42:46 PM  Barry A. Pape
           Base-lining of 7.1_LA
      3    360Commerce 1.2         3/31/2005 4:30:09 PM   Robert Pearse
      2    360Commerce 1.1         3/10/2005 10:25:28 AM  Robert Pearse
      1    360Commerce 1.0         2/11/2005 12:14:23 PM  Robert Pearse
     $
     Revision 1.10  2004/07/06 22:17:29  lzhao
     @scr 4019: make online green, offline red in status field of status bean

     Revision 1.9  2004/04/19 18:06:37  mweis
     @scr 4305 Sale: Negative balance indicators -- small enhance per related code review on 4410

     Revision 1.8  2004/04/09 19:46:30  mweis
     @scr 4305 Sale: Negative balance indicators -- code review updates

     Revision 1.7  2004/04/06 19:55:03  mweis
     @scr 4305  Sale:  indicators when till balance is negative

     Revision 1.6  2004/03/31 21:06:40  mweis
     @scr 4206 JavaDoc updates.

     Revision 1.5  2004/03/16 17:15:18  build
     Forcing head revision

     Revision 1.4  2004/03/01 23:11:41  rzurga
     @scr 0 Improve the testing so the code doesn't fail with null exception

     Revision 1.3  2004/02/26 16:47:10  rzurga
     @scr 0 Add optional and customizable date to the transaction id and its receipt barcode

     Revision 1.2  2004/02/11 20:56:26  rhafernik
     @scr 0 Log4J conversion and code cleanup

     Revision 1.1.1.1  2004/02/11 01:04:22  cschellenger
     updating to pvcs 360store-current


 *
 *    Rev 1.2   Feb 10 2004 08:34:12   Tim Fritz
 * Update status text for change of language.
 * Resolution for 3737: The online/offline status indicator is also held in the previous language when the user logs in for the first time until they return to the main menu.
 *
 *    Rev 1.1   Oct 21 2003 10:54:22   rsachdeva
 * initializeStatusContainer
 * Resolution for POS SCR-3411: Feature Enhancement:  Device and Database Status
 *
 *    Rev 1.0   Aug 29 2003 16:12:26   CSchellenger
 * Initial revision.
 *
 *    Rev 1.2   Aug 07 2002 19:34:26   baa
 * remove hard coded date formats
 * Resolution for POS SCR-1740: Code base Conversions
 *
 *    Rev 1.1   14 May 2002 18:30:14   baa
 * training mode enhancements
 * Resolution for POS SCR-1645: Training Mode Enhancements
 *
 *    Rev 1.0   Apr 29 2002 14:52:14   msg
 * Initial revision.
 *
 *    Rev 1.0   Mar 18 2002 11:52:30   msg
 * Initial revision.
 *
 *    Rev 1.3   Feb 27 2002 21:25:56   mpm
 * Continuing work on internationalization
 * Resolution for POS SCR-351: Internationalization
 *
 *    Rev 1.2   Feb 23 2002 15:04:18   mpm
 * Re-started internationalization initiative.
 * Resolution for POS SCR-351: Internationalization
 * ===================================================
 */
package oracle.retail.stores.pos.ui.beans;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import oracle.retail.stores.commerceservices.common.datetime.DateTimeServiceIfc;
import oracle.retail.stores.commerceservices.common.datetime.DateTimeServiceLocator;
import oracle.retail.stores.common.context.BeanLocator;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.foundation.manager.gui.UIModelIfc;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.pos.config.bundles.BundleConstantsIfc;
import oracle.retail.stores.pos.services.customer.common.DisplayCustomerAction;
import oracle.retail.stores.pos.services.manager.DisplayStatusAction;
import oracle.retail.stores.pos.ui.OnlineStatusContainer;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.UIUtilities;
import oracle.retail.stores.pos.ui.behavior.OnlineDeviceStatusListener;
import oracle.retail.stores.pos.ui.behavior.ScreenNameListener;
import oracle.retail.stores.pos.ui.popup.PopupMessageManagerIfc;

/**
 * This bean is the presentation of the status area; it contains information
 * such as the Cashier, Operator, and Customer Names; the online/offline state,
 * time and register number.
 */
public class StatusBean extends BaseBeanAdapter implements ScreenNameListener, ActionListener
{
    private static final long serialVersionUID = 4683294318354700480L;
    /** location of the indicators to use when the current till balance is negative. */
    protected static final String PROPERTIES_FILE = "application";   // "application.properties"
    /** Property name in application.properties that captures which marker/token to use when displaying a negative balance. */
    protected static final String NEGATIVE_INDICATOR_MARKER_FOR_REGISTER_NUMBER = "NegativeIndicatorMarkerForRegisterNumber";
    /** Property name in application.properties that captures which color to use to display a negative balance. */
    protected static final String NEGATIVE_INDICATOR_COLOR_FOR_REGISTER_NUMBER = "NegativeIndicatorColorForRegisterNumber";
    /** Number of milliseconds to wait between time/date displays */
    protected static final int WAIT_TIME           = 60000;

    /** Displays the cashier name */
    protected JLabel cashierField;
    /** Displays the customer name */
    protected JLabel customerField;
    /** Displays the date */
    protected JLabel dateField;
    /** Displays the time */
    protected JLabel registerField;
    /** Display the sales associate */
    protected JLabel salesAssociateField;
    /** Displays Online/Offline/Training Mode */
    protected JLabel statusField;
    /** Displays the screen name */
    protected JLabel screenNameField;
    /** Displays the time */
    protected JLabel timeField;
    /** Customer label */
    protected JLabel customerLabel = null;
    /** Cashier label */
    protected JLabel cashierLabel = null;
    /** Sales Associate label */
    protected JLabel salesAssociateLabel = null;
    /** Size of single field */
    protected Dimension cellDimension;
    /** Maintains the status of each output device and the overall status of the DB connection */
    protected OnlineStatusContainer statusContainer = OnlineStatusContainer.getSharedInstance();
    /** Timer for displaying time and date */
    protected Timer timer;
    /** Listener for the devices hashtable */
    protected OnlineDeviceStatusListener onlineDeviceStatusListener = null;
    /**
     * The standard foreground color we are using.
     * Will be re-initialized during the call to initComponents().
     */
    protected Color STD_FOREGROUND = Color.BLACK;

    /** screen name property tag */
    protected String screenNameTag = "";
    
    public boolean empDiscountApplied = false;
    
    
   

    /**
     * Constructor
     */
    public StatusBean()
    {
        this("StatusBean");
    }

    /**
     * Constructor
     * 
     * @param name
     */
    public StatusBean(String name)
    {
        super();
        setName(name);
        UI_PREFIX = "StatusBean";
        initialize();
    }

    /**
     * Initialize the bean.
     */
    protected void initialize()
    {
        uiFactory.configureUIComponent(this, UI_PREFIX);
        initComponents();
        initLayout();

        // initialize the device hashtable
        initializeStatusContainer();

        // Instantiate the timer, display the time and date, then
        // start the timer.
        timer = new Timer(WAIT_TIME, this);
        actionPerformed(null);
        timer.start();
    }

    /**
     * create all the fields and widgets
     */
    protected void initComponents()
    {
        String prefix = UI_PREFIX + ".field";

        registerField       = uiFactory.createLabel("register",  null, null, prefix);
        cashierField        = uiFactory.createLabel("cashier",   null, null, prefix);
        salesAssociateField = uiFactory.createLabel("associate", null, null, prefix);
        customerField       = uiFactory.createURLLabel("customer",  null, null, prefix, new DisplayCustomerAction());
        dateField           = uiFactory.createLabel("date",      null, null, prefix);
        timeField           = uiFactory.createLabel("time",      null, null, prefix);
        screenNameField     = uiFactory.createLabel("screen",    null, null, prefix);
        statusField         = uiFactory.createURLLabel("status", null, null, prefix, new DisplayStatusAction());
        cashierLabel  = uiFactory.createLabel("cashierLabel", "cashierLabel", null, prefix);
        customerLabel  = uiFactory.createLabel("customerLabel", "customerLabel", null, prefix);
        salesAssociateLabel  = uiFactory.createLabel("salesAssociateLabel", "salesAssociateLabel", null, prefix);

        // re-establish what is our standard foreground color
        STD_FOREGROUND = UIManager.getColor(prefix + ".foreground");
    }

    /**
     * Lays out this bean's components.
     */
    protected void initLayout()
    {
        setLayout(new GridBagLayout());
        GridBagConstraints  constraints = new GridBagConstraints();

        // set grid y-position
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.1;
        constraints.weighty = 0.5;
        constraints.insets = new Insets(5, 5, 1, 0);
        statusField.setHorizontalAlignment(JLabel.LEFT);
        add(statusField, constraints);

        constraints.insets.left = 0;
        constraints.insets.right = 1;
        registerField.setHorizontalAlignment(JLabel.RIGHT);
        add(registerField, constraints);

        constraints.insets.left = 1;
        constraints.insets.right = 0;
        cashierLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(cashierLabel, constraints);

        constraints.insets.left = 0;
        constraints.insets.right = 1;
        cashierField.setHorizontalAlignment(JLabel.LEFT);
        add(cashierField, constraints);

        constraints.insets.left = 1;
        constraints.insets.right = 0;
        constraints.weightx = 0.0;
        customerLabel.setHorizontalAlignment(JLabel.LEFT);
        add(customerLabel, constraints);

        constraints.insets.left = 0;
        constraints.insets.right = 5;
        constraints.weightx = 0.1;
        customerField.setHorizontalAlignment(JLabel.LEFT);
        add(customerField, constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(1, 5, 5, 0);
        dateField.setHorizontalAlignment(JLabel.LEFT);
        add(dateField, constraints);

        constraints.insets.left = 0;
        constraints.insets.right = 1;
        timeField.setHorizontalAlignment(JLabel.RIGHT);
        add(timeField, constraints);

        constraints.insets.left = 1;
        constraints.insets.right = 0;
        salesAssociateLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(salesAssociateLabel, constraints);

        constraints.insets.left = 0;
        constraints.insets.right = 1;
        salesAssociateField.setHorizontalAlignment(JLabel.LEFT);
        add(salesAssociateField, constraints);

        constraints.insets.left = 1;
        constraints.insets.right = 5;
        constraints.gridwidth = 2;
        screenNameField.setHorizontalAlignment(JLabel.LEFT);
        add(screenNameField, constraints);
    }

    /**
     * Sets the bean fields from the model
     *
     * @param model UIModelIfc
     */
    @Override
    public void setModel(UIModelIfc model)
    {
        if (model == null)
        {
            throw new NullPointerException("Attempt to set EmployeeMasterBeanModel to null");
        }

         if (model instanceof POSBaseBeanModel)
        {
            POSBaseBeanModel pModel = (POSBaseBeanModel)model;
            StatusBeanModel sModel = pModel.getStatusBeanModel();
           
            
            if (sModel != null)
            {
                if (sModel.getCashierName() != null)
                {
                    if (sModel.getCashierName().length() > 26)
                    {
                        FontMetrics fm = cashierField.getFontMetrics(cashierField.getFont());
                        cashierField.setText(clipString(sModel.getCashierName(), fm, cashierField.getWidth()));
                    }
                    else
                    {
                        cashierField.setText(sModel.getCashierName());
                    }
                }
                if (sModel.getCustomerName() != null)
                {
                    if (sModel.getCustomerName().length() > 26)
                    {
                        FontMetrics fm = customerField.getFontMetrics(customerField.getFont());
                        customerField.setText(clipString(sModel.getCustomerName(), fm, customerField.getWidth()));
                    }
                    else
                    {
                        customerField.setText(sModel.getCustomerName());
                    }
                }
                else
                {
                    customerField.setText("");
                }
                if (sModel.getRegisterId() != null)
                {
                    registerField.setText(sModel.getRegisterId());
                }
                if (sModel.getRegister() != null)
                {
                    displayCurrentTillStatus(sModel);
                }
                if (sModel.getSalesAssociateName() != null)
                {

                    if (sModel.getSalesAssociateName().length() > 26)
                    {
                        FontMetrics fm = salesAssociateField.getFontMetrics(salesAssociateField.getFont());
                        salesAssociateField.setText(clipString(sModel.getSalesAssociateName(), fm, salesAssociateField
                                .getWidth()));
                    }
                    else
                    {
                        salesAssociateField.setText(sModel.getSalesAssociateName());
                    }
                }
                if (sModel.getScreenName() != null)
                {
                    screenNameField.setText(sModel.getScreenName());
                }

                OnlineStatusContainer sContainer = sModel.getStatusContainer();
                if (sContainer != null)
                {
                    Hashtable<Integer,Boolean> statusHashTable = sContainer.getStatusHash();
                    if (!statusHashTable.isEmpty())
                    {
                        // clear the old status container to prevent incorrect status being set
                        sModel.setStatusContainer(null);
                        statusContainer.update(sContainer);
                        if (statusContainer.isTrainingMode())
                        {
                            statusField.setForeground(Color.red);
                            statusField.setFont(new Font(statusField.getFont().getFamily(), Font.BOLD, statusField
                                    .getFont().getSize()));
                        }
                        else if (!statusContainer.isOnline())
                        {
                            statusField.setForeground(Color.red);
                            statusField.setFont(new Font(statusField.getFont().getFamily(), Font.PLAIN, statusField
                                    .getFont().getSize()));
                        }
                        else
                        {
                            // is online, show green (the color is little bit darker then green.)
                            statusField.setForeground(new Color(0, 153, 0));
                            statusField.setFont(new Font(statusField.getFont().getFamily(), Font.PLAIN, statusField
                                    .getFont().getSize()));

                        }
                        statusField.setText(statusContainer.getStatusText());

                        // notify the listener
                        if (onlineDeviceStatusListener != null)
                        {
                            onlineDeviceStatusListener.onlineDeviceStatusChanged(statusContainer.getStatusHash());
                        }
                    }
                    
                    if(("SellItemScreenName".equalsIgnoreCase(screenNameTag) || "Sell Item".equalsIgnoreCase(retrieveText(screenNameTag, screenNameField.getText()))) 
                    		&& (sModel.isEmpDiscountApplied())){
                    	/*customerLabel.setText("Employee ");
                    	cashierLabel.setText("Employee ID "); 
                    	salesAssociateLabel.setText("Remaining Spend : ");*/
                    	/*code changes done by dharmendra on 08/09/2016 to fix issue POS-226*/
                    	customerLabel.setText(retrieveText("EEPLabel"));
                    	cashierLabel.setText(retrieveText("EEPIdLabel"));
                    	salesAssociateLabel.setText(retrieveText("EEPRemainingSpendLabel"));
                    }else{
                    	 customerLabel.setText(retrieveText("CustomerLabel", customerLabel));
                         cashierLabel.setText(retrieveText("CashierLabel", cashierLabel));
                         salesAssociateLabel.setText(retrieveText("SalesAssociateLabel", salesAssociateLabel));
                    }
                    
                }
                if (sModel.isBCashDrawerWarningReqd())
                {
                    PopupMessageManagerIfc popup = (PopupMessageManagerIfc)BeanLocator.getApplicationBean(PopupMessageManagerIfc.BEAN_KEY);
                    popup.setOwner(this);
                    String message = UIUtilities.retrieveText("StatusPanelSpec",
                            BundleConstantsIfc.POS_BUNDLE_NAME,
                            "CashDrawerUnderWarningMessage", "Warning");
                    popup.addMessage(message);
                    sModel.setBCashDrawerWarningReqd(false);
                }
            }

            // Update status text for change of language
            String currentStatus = statusField.getText();
            String newStatus = statusContainer.getStatusText();
            if (currentStatus == null || !currentStatus.equals(newStatus))
            {
                statusField.setText(newStatus);
            }
        }
    }

    /**
     * Sets the Screen name independently from the model. This allows the screen
     * to be set from the xml through a BEANPROPERTY and from another bean
     * through a connection.
     *
     * @param screenName String screen name to display in the status area.
     */
    public void setScreenName(String screenName)
    {
    	
        screenNameField.setText(screenName);
    }

    /**
     * Sets the Screen name independently from the model through a property tag.
     *
     * @param value screen name tag
     */
    public void setScreenNameTag(String value)
    {
    	
        screenNameTag = value;
        updatePropertyFields();
    }

    /**
     * Sets the properties object.
     *
     * @param props the properties object.
     */
    public void setProps(Properties props)
    {
        this.props = props;
        if (statusContainer != null)
        {
            statusContainer.setProps(props);
        }
        updatePropertyFields();
    }

    /**
     * Update property fields.
     */
    @Override
    protected void updatePropertyFields()
    {
    	screenNameField.setText(retrieveText(screenNameTag, screenNameField.getText()));
        if (customerField.getText() == null)
        {
            customerField.setText("");
        }
        customerLabel.setText(retrieveText("CustomerLabel", customerLabel));
        cashierLabel.setText(retrieveText("CashierLabel", cashierLabel));
        salesAssociateLabel.setText(retrieveText("SalesAssociateLabel", salesAssociateLabel));
            
    }

    /**
     * This method is required by the {@link ActionListener} interface. It will
     * be called by the {@link #timer} object at regular intervals to update the
     * time and date on the screen.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        DateTimeServiceIfc dateTimeService = DateTimeServiceLocator.getDateTimeService();
        Date now = new Date();
        Locale defaultLocale = LocaleMap.getLocale(LocaleMap.DEFAULT);
        String sDate = dateTimeService.formatDate(now, defaultLocale, DateFormat.SHORT);
        String sTime = dateTimeService.formatTime(now, defaultLocale, DateFormat.SHORT);
        dateField.setText(sDate);
        timeField.setText(sTime);
    }

    /**
     * Add the listener for the devices hashtable
     *
     * @param listener as a OnlineDeviceStatusListener.
     */
    public void addOnlineDeviceStatusListener(OnlineDeviceStatusListener listener)
    {
        onlineDeviceStatusListener = listener;

        Hashtable<Integer, Boolean> deviceHashTable = statusContainer.getStatusHash();
        if (deviceHashTable != null)
        {
            onlineDeviceStatusListener.onlineDeviceStatusChanged(deviceHashTable);
        }
    }

    /**
     * Remove the listener for the devices hashtable
     */
    public void removeOnlineDeviceStatusListener()
    {
        removeOnlineDeviceStatusListener(onlineDeviceStatusListener);
    }

    /**
     * Remove the listener for the devices hashtable if the parameter matches
     * the existing listener.
     */
    public void removeOnlineDeviceStatusListener(OnlineDeviceStatusListener listener)
    {
        if (listener == onlineDeviceStatusListener)
        {
            onlineDeviceStatusListener = null;
        }
    }

    /**
     * Initialize the Status Container using Default as Online
     */
    protected void initializeStatusContainer()
    {
        //These devices/authorizations are based on their Last Set Status.
        //By default, status for these is set to Online.

        // devices status
        statusContainer.setOnlineStatus(POSUIManagerIfc.PRINTER_STATUS,
                                        POSUIManagerIfc.ONLINE);
        statusContainer.setOnlineStatus(POSUIManagerIfc.CASHDRAWER_STATUS,
                                        POSUIManagerIfc.ONLINE);
        statusContainer.setOnlineStatus(POSUIManagerIfc.FINANCIAL_NETWORK_STATUS,
                                        POSUIManagerIfc.ONLINE);

        //authorization status
        statusContainer.setOnlineStatus(POSUIManagerIfc.CHECK_STATUS,
                                        POSUIManagerIfc.ONLINE);
        statusContainer.setOnlineStatus(POSUIManagerIfc.CREDIT_STATUS,
                                        POSUIManagerIfc.ONLINE);
        statusContainer.setOnlineStatus(POSUIManagerIfc.DEBIT_STATUS,
                                        POSUIManagerIfc.ONLINE);

    }

    /**
     * Displays the status of the current till.
     *
     * If the current till has a negative balance, the register ID might change
     * in appearance.  The details of how a negative balance are conveyed are
     * captured as property values in the POS <code>application.xml</code> file.
     * <ul>
     * <li>NegativeColorForRegisterNumber
     * <li>NegativeMarkerForRegisterNumber
     * </ul>
     *
     * @param sModel The model carrying the information about the current till
     *               (as accessed via the register object).
     */
    protected void displayCurrentTillStatus(StatusBeanModel sModel)
    {
        // Sanity.
        if (sModel == null || sModel.getRegister() == null)
        {
            return;  // we have nothing to work with.
        }

        // Attempt to get the actual register id.
        String registerId = sModel.getRegisterId();
        if (registerId == null)
        {
            // Cannot rely on text in the "registerField" since it could contain the negative marker.
            registerId = sModel.getRegister().getWorkstation().getWorkstationID();
        }

        // If we have an actual id, we can continue safely along.
        if (registerId != null)
        {
            // Assume all is normal.  These values, of course, become our default ones.
            String text = registerId;
            Color foreground = STD_FOREGROUND;

            // Check if we have a negative till balance.
            if (sModel.hasNegativeBalance())
            {
                // Retrieve how we are supposed to display a negative balance.
                String colorVal  = Gateway.getProperty(PROPERTIES_FILE, NEGATIVE_INDICATOR_COLOR_FOR_REGISTER_NUMBER,  "");
                String markerVal = Gateway.getProperty(PROPERTIES_FILE, NEGATIVE_INDICATOR_MARKER_FOR_REGISTER_NUMBER, "");

                // If applicable, transform ("decode") the color value into a color we will use for the foreground.
                try
                {
                    // If we have our default from above (""), then just stay with the STD_FOREGROUND color
                    // that we have already established as our actual default foreground color.
                    // However, if we have a value, let's attemp to decode it into an actual color.
                    if (!colorVal.equals(""))
                    {
                        foreground = Color.decode(colorVal);  // example:  colorVal = "#8b1c62"  /* maroon */
                    }
                }
                catch (Exception ignored)
                {
                    // Assume that we are here because no legitimate color value was supplied for us to decode.
                    // Maybe the reference is to a color already loaded in Swing's world, such as from tigerplaf.properties
                    foreground = UIManager.getColor(colorVal);
                    if (foreground == null)
                    {
                        foreground = STD_FOREGROUND;
                    }
                }

                // Use the marker value.
                text = text + markerVal;                  // now the registerId "number" will have more stuff tacked on
            }

            // Redisplay.
            registerField.setText(text);
            registerField.setForeground(foreground);
        }
    }

    /**
     * This method takes in the  displayName  and based on the font
     * size, width of the {@link JLabel} and  the characters in the displayName
     * calculates the number of characters that can be displayed.
     *
     * @param displayName
     * @param fm
     * @param labelWidth
     * @return String which is truncated and with ... appended
     */
    private String clipString(String displayName,FontMetrics fm,int labelWidth)
    {
        String clipString = "...";
        int totalWidth = SwingUtilities.computeStringWidth(fm,clipString);
        int nChars;

        for(nChars = 0; nChars <displayName.length() ; nChars++)
        {
            totalWidth += fm.charWidth(displayName.charAt(nChars));
            if (totalWidth > labelWidth)
            {
                break;
            }
        }
        displayName = displayName.substring(0, nChars) + clipString;
        return displayName;
    }

    
    /**
   	 * @return the isEmpDiscountApplied
   	 */
   	public boolean isEmpDiscountApplied() {
   		return empDiscountApplied;
   	}

   	/**
   	 * @param isEmpDiscountApplied the isEmpDiscountApplied to set
   	 */
   	public void setEmpDiscountApplied(boolean isEmpDiscountApplied) {
   		this.empDiscountApplied = isEmpDiscountApplied;
   	}

    /**
     * main entrypoint - starts the part when it is run as an application
     * @param args java.lang.String[]
     */
    public static void main(String[] args)
    {
        UIUtilities.setUpTest();
        StatusBean bean = new StatusBean();
        UIUtilities.doBeanTest(bean);
    }
}
