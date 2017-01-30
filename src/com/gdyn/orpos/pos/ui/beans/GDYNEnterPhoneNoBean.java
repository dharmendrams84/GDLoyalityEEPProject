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


import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;


import org.apache.log4j.Logger;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityConstants;


import oracle.retail.stores.common.utility.LocaleMap;

import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.domain.utility.LocaleUtilities;

import oracle.retail.stores.foundation.tour.conduit.Dispatcher;
import oracle.retail.stores.pos.config.bundles.BundleConstantsIfc;
import oracle.retail.stores.pos.manager.ifc.UtilityManagerIfc;
import oracle.retail.stores.pos.manager.utility.UtilityManager;
import oracle.retail.stores.pos.ui.UIUtilities;

import oracle.retail.stores.pos.ui.beans.GDYNPhoneNoBeanModel;

import oracle.retail.stores.pos.ui.beans.ValidatingBean;
import oracle.retail.stores.pos.ui.beans.ValidatingFieldIfc;
import oracle.retail.stores.pos.ui.beans.ValidatingFormattedTextField;


public class GDYNEnterPhoneNoBean extends ValidatingBean {
	private static final long serialVersionUID = -1451300025784392803L;

	protected static final Logger logger = Logger
			.getLogger(GDYNEnterPhoneNoBean.class);
	protected ValidatingFormattedTextField phoneNo = null;
	protected JLabel phoneNoLabel = null;

	// The bean model

	/**
	 * Determines if all the required fields have non-null, valid data; and
	 * determines if all the non-null optional fields have valid data; if so, it
	 * fires a "validated" event, otherwise it fires an "invalidated" event.
	 * 
	 * @return True if no errors
	 */

	public GDYNEnterPhoneNoBean() {
		super();
		initialize();

	}

	protected void initialize() {
		uiFactory.configureUIComponent(this, UI_PREFIX);

		initializeFields();
		initializeLabels();
		initLayout();
	}

	protected void initLayout() {
		UIUtilities.layoutComponent(this, phoneNoLabel, phoneNo, 0, 0, false);
	}

	protected void initializeFields() {

		phoneNo = uiFactory.createValidatingFormattedTextField("phoneNo", "",
				"30", "20");
		

	}

	protected void initializeLabels() {
		String EnterPhoneLabel = UIUtilities.retrieveText("Common",
				BundleConstantsIfc.CUSTOMER_BUNDLE_NAME, "EnterPhoneLabel");
		phoneNoLabel = uiFactory.createLabel("phoneNoLabel", EnterPhoneLabel,
				null, UI_LABEL);
	}

	@Override
	public void updateModel() {
		super.updateModel();

	}

	@Override
	protected void updateBean() {
		super.updateBean();
		phoneNo.setText("");
		phoneNo.setHorizontalAlignment(JTextField.LEFT);

		Locale defLocale = LocaleMap
				.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);
		String countryCode = defLocale.getCountry();

		UtilityManager util = (UtilityManager) Dispatcher.getDispatcher()
				.getManager(UtilityManagerIfc.TYPE);
		String phoneFormat = util.getPhoneFormat(countryCode);
		phoneNo.setFormat(phoneFormat);

		phoneNo.setVisible(true);
		phoneNo.setEditable(true);
		phoneNo.setEnabled(true);

	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		phoneNo.addFocusListener(this);
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		super.deactivate();
		phoneNo.removeFocusListener(this);
	}

	@Override
	protected boolean validateFields() {

		// call super to do all basic validations.
		boolean valid = super.validateFields();
		String phoneNoValue = "";
		UtilityManager util = (UtilityManager) Dispatcher.getDispatcher()
				.getManager(UtilityManagerIfc.TYPE);

		Locale defLocale = LocaleMap
				.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);
		String countryCode = defLocale.getCountry();

		ValidatingFieldIfc field = (ValidatingFieldIfc) requiredFields.get(0);

		if (valid) {

			phoneNoValue = ((JTextComponent) field).getText();

			String phoneValidationRegexp = util
					.getPhoneValidationRegexp(countryCode);
			
			Pattern p = Pattern.compile(phoneValidationRegexp);
			Matcher m = p.matcher(phoneNoValue);
			valid = m.matches();

			if (beanModel instanceof GDYNPhoneNoBeanModel) {
				GDYNPhoneNoBeanModel model = (GDYNPhoneNoBeanModel) beanModel;
				model.setFieldPhoneNo(phoneNoValue);
			}

		}
		//new if block added by Ashwinee on 18/01/2017 to fix issue POS-379
		if (valid) {
			String formattedPhoneNoValue = GDYNLoyalityConstants
					.getFormattedPhoneNo(phoneNoValue);
			String firstCharacter = formattedPhoneNoValue.substring(0, 1);
			if (firstCharacter.equalsIgnoreCase("1")
					|| firstCharacter.equalsIgnoreCase("0")) {
				valid = false;
				String msg = UIUtilities.retrieveText("DialogSpec",
						BundleConstantsIfc.DIALOG_BUNDLE_NAME,
						"InvalidData.validatePhoneNoStart");
				errorMessage[0] = LocaleUtilities.formatComplexMessage(msg,
						null, getLocale());
				showErrorScreen();
				
			}

		} else if (!valid) {
			logger.info("phoneNoValue " + phoneNoValue
					+ " fails validation for  locale " + defLocale.getCountry());

			String msg = UIUtilities.retrieveText("DialogSpec",
					BundleConstantsIfc.DIALOG_BUNDLE_NAME,
					"InvalidData.phoneNoFormat");
			errorMessage[0] = LocaleUtilities.formatComplexMessage(msg, null,
					getLocale());
			showErrorScreen();
		}
		return valid;
	}

}
