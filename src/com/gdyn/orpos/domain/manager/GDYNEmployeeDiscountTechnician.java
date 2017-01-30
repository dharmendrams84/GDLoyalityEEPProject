package com.gdyn.orpos.domain.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.tour.manager.Technician;

import org.apache.log4j.Logger;

import com.gdyn.co.employeediscount.ejb.GDYNEmployeeServiceHome;
import com.gdyn.co.employeediscount.ejb.GDYNEmployeeServiceRemote;
import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;


public class GDYNEmployeeDiscountTechnician extends Technician {

	
	protected static Logger logger = Logger
			.getLogger(com.gdyn.orpos.domain.manager.GDYNEmployeeDiscountTechnician.class);
	
	public static final String name = "GDYNEmployeeDiscountTechnician";
	String url = "";
	String initialFactory = "";
	String principal = "";
	String credentials = "";

	
	public GDYNEmployeeDiscountTechnician(){
		initialize();
		//System.out.println("inside GDYNEmployeeDiscountTechnician constructor");
		url = Gateway
				.getProperty("application", "java.naming.provider.url", "");
		initialFactory = Gateway.getProperty("application",
				"java.naming.factory.initial", "");
		principal = Gateway.getProperty("application",
				"java.naming.security.principal", "");
		credentials = Gateway.getProperty("application",
				"java.naming.security.credentials", "");
	}
	
	public void initialize() {
		//System.out.println("inside initialize of GDYNEmployeeDiscountTechnician");
		// Load the params
		// get url and userID & Password from application properties
	}
	
	
	public GDYNEmployeeDiscResponseObject[] getMaxSpendLimit(String employeeNumber) {

		
		logger.info("Requesting max spend limit of employee GDYNEmployeeDiscTechnician");
		//System.out.println("from getMaxSpendLimit() in GDYNEmployeeDiscTechnician for employee number "+employeeNumber);
		GDYNEmployeeDiscResponseObject[] responseObject = null;
		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(Context.INITIAL_CONTEXT_FACTORY, initialFactory);
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_PRINCIPAL, principal);
		environment.put(Context.SECURITY_CREDENTIALS, credentials);
		
		
		/*environment.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
		environment.put(Context.PROVIDER_URL, "t3s://sastoh81.corp.gdglobal.ca:7002");
		environment.put(Context.SECURITY_PRINCIPAL, "pos");
		environment.put(Context.SECURITY_CREDENTIALS, "Web_0801");*/
		//System.out.println(initialFactory+ " : "+url+ " : "+principal+" : "+credentials);
	try {
		//System.out.println("Before context");
			InitialContext context = new InitialContext(environment);
			//System.out.println("CO connected successfully"+context);
			/*Object objref =  context
					.lookup("employeeDiscount-ejb_EmployeeDiscountServiceSB");*/
			Object objref = context.lookup("EmployeeDiscountServiceSB");
			//Object objref =(GDYNEmployeeServiceHome)context.lookup("java:comp/env/ejb/EmployeeDiscountServiceSB");
        	logger.info("employeeDiscount-ejb_EmployeeDiscountServiceSB lookup Successfully!!!");			
			//System.out.println("employeeDiscount-ejb_EmployeeDiscountServiceSB lookup Successfully!!!");
			EJBHome home = (EJBHome) PortableRemoteObject.narrow(objref,
					GDYNEmployeeServiceHome.class);

			Method m = null;
			try {
				m = home.getClass().getMethod("create", new Class[0]);
			} catch (SecurityException e1) {
				logger.error("Security exception  : " + e1);
			} catch (NoSuchMethodException e1) {
				logger.error("NoSuchMethodException exception  : " + e1);
			}
			GDYNEmployeeServiceRemote employeeServiceRemote = null;
			try {
				employeeServiceRemote = (GDYNEmployeeServiceRemote) m.invoke(home, new Object[0]);
				//System.out.println("rechargeServiceRemote "+employeeServiceRemote);
			} catch (IllegalArgumentException e1) {
				logger.error("IllegalArgumentException exception  : " + e1);
			} catch (IllegalAccessException e1) {
				logger.error("IllegalAccessException exception  : " + e1);
			} catch (InvocationTargetException e1) {
				logger.error("InvocationTargetException exception  : " + e1);
			}
			try {
			
				responseObject = employeeServiceRemote.getEmployeeResponseObject(employeeNumber);
				
				
			} catch (Exception e) {
				logger.error("Exception exception  : " + e);
			}
		} catch (Exception exception) {
			logger.error("NamingException exception  : " + exception);
		}
	
		//logger.info("responseObject " + responseObject);
		/*if (responseObject != null&& responseObject.length !=0) {
			logger.info("responseObject is not null  && responseObject.length !=0 ");
			if (responseObject[0] != null) {
				logger.info("responseObject Details "
						+ responseObject[0].getDiscDivision()
						+ " :  responseObject[0].getEmpStatusCode() "
						+ responseObject[0].getEmpStatusCode());

			}
		}*/
		
		return responseObject;
	}

}
