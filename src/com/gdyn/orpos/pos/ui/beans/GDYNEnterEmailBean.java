/* ===========================================================================
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/applications/pos/src/oracle/retail/stores/pos/ui/beans/EnterEmailBean.java /rgbustores_13.4x_generic_branch/1 2011/05/05 14:06:57 mszekely Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    abondala  01/03/10 - update header date
 *    cgreene   11/09/09 - resize email entry fields based upon screen size
 *    ohorne    03/30/09 - regex pattern now obtained from domain.properties
 *
 * ===========================================================================
 */
package com.gdyn.orpos.pos.ui.beans;

import java.awt.Component;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.JTextComponent;

import oracle.retail.stores.domain.utility.LocaleUtilities;
import oracle.retail.stores.foundation.tour.conduit.Dispatcher;
import oracle.retail.stores.pos.config.bundles.BundleConstantsIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.ui.UIUtilities;
import oracle.retail.stores.pos.ui.beans.DataInputBean;
import oracle.retail.stores.pos.ui.beans.ValidatingFieldIfc;

public class GDYNEnterEmailBean extends DataInputBean {
	private static final long serialVersionUID = -1451300025784392803L;

	/**
	 * Determines if all the required fields have non-null, valid data; and
	 * determines if all the non-null optional fields have valid data; if so, it
	 * fires a "validated" event, otherwise it fires an "invalidated" event.
	 *
	 * @return True if no errors
	 */
	@Override
	protected boolean validateFields() {
		// call super to do all basic validations.
		boolean valid = super.validateFields();
	
		// if no error returns from super, do the validation specific to
		// eReceiptEmailAddress screen.
		if (valid) {
			int errorCount = 0;
			// get all requirefield list.
			Iterator<ValidatingFieldIfc> requiredEnum = requiredFields
					.iterator();
			String emailFieldValue = "";

			while (requiredEnum.hasNext() && errorCount < MAX_ERROR_MESSAGES) {
				ValidatingFieldIfc field = requiredEnum.next();
				String name = ((Component) field).getName();
				if ("email".equals(name)) {
					emailFieldValue = ((JTextComponent) field).getText();
					// Display error dialog if email address format is not
					// correct
					if (!isValidEmailId(emailFieldValue)) {
						String msg = UIUtilities.retrieveText("DialogSpec",
								BundleConstantsIfc.DIALOG_BUNDLE_NAME,
								"InvalidData.emailFormat",
								"{0} Invalid Email Address Format.");
						Object[] data = new Object[1];
						data[0] = getFieldName(field);
						errorMessage[errorCount] = LocaleUtilities
								.formatComplexMessage(msg, data, getLocale());
						errorCount++;
					}
				} 
			}			
			if (errorCount > 0) {
				// There were errors, show the error screen.
				showErrorScreen();
				valid = false;
			}
		}
		return valid;
	}
	
	/**
     * This method validate the email address format.
     * @param email
     * @return true if valid email address format otherwise false.
     */
    protected boolean isValidEmailId(String email)
    {
        UtilityManagerIfc util = (UtilityManagerIfc) Dispatcher.getDispatcher().getManager(UtilityManagerIfc.TYPE);
        String regex = util.getEmailValidationRegexp();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        return m.matches();
    }

}
