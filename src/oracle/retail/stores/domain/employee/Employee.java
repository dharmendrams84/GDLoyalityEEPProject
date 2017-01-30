/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  oracle.retail.stores.commerceservices.security.EmployeeComplianceIfc
 *  oracle.retail.stores.common.data.JdbcUtilities
 *  oracle.retail.stores.common.utility.Util
 *  oracle.retail.stores.domain.DomainGateway
 *  oracle.retail.stores.domain.employee.EmployeeTypeEnum
 *  oracle.retail.stores.domain.employee.Role
 *  oracle.retail.stores.domain.employee.RoleFunctionIfc
 *  oracle.retail.stores.domain.employee.RoleIfc
 *  oracle.retail.stores.domain.financial.HardTotalsBuilderIfc
 *  oracle.retail.stores.domain.financial.HardTotalsFormatException
 *  oracle.retail.stores.domain.utility.EYSDate
 *  oracle.retail.stores.domain.utility.PersonNameIfc
 *  oracle.retail.stores.foundation.utility.LocaleMap
 *  oracle.retail.stores.utility.I18NHelper
 *  org.apache.log4j.Logger
 */
package oracle.retail.stores.domain.employee;

import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import oracle.retail.stores.commerceservices.security.EmployeeComplianceIfc;
import oracle.retail.stores.common.data.JdbcUtilities;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.employee.EmployeeTypeEnum;
import oracle.retail.stores.domain.employee.Role;
import oracle.retail.stores.domain.employee.RoleFunctionIfc;
import oracle.retail.stores.domain.employee.RoleIfc;
import oracle.retail.stores.domain.financial.HardTotalsBuilderIfc;
import oracle.retail.stores.domain.financial.HardTotalsFormatException;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.PersonNameIfc;
import oracle.retail.stores.foundation.utility.LocaleMap;
import oracle.retail.stores.utility.I18NHelper;
import org.apache.log4j.Logger;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;

public class Employee
implements EmployeeIfc {
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/3 $";
    protected static final Logger logger = Logger.getLogger((Class)Employee.class);
    static final long serialVersionUID = 4769627265523328119L;
    protected String alternateID = "";
    protected int daysValid = 0;
    protected EmployeeComplianceIfc employeeCompliance = null;
    protected String employeeID = "";
    protected EYSDate expirationDate = null;
    protected String fullName = "";
    protected String loginID = "";
    protected int loginStatus = 2;
    protected PersonNameIfc name;
    protected int numberFailedPasswords = 0;
    protected byte[] password;
    protected String employeePasswordSalt;
    protected boolean passwordChangeRequired = true;
    protected Date passwordCreationDate;
    protected Locale preferredLocale = null;
    protected RoleIfc role;
    protected String storeID = "";
    protected EmployeeTypeEnum type = EmployeeTypeEnum.STANDARD;
    protected byte[] fingerprintBiometrics = new byte[0];
    protected Date lastLoginTime;
    
    
    protected GDYNEmployeeDiscResponseObject[] responseObject = null;

    /**
	 * @return the responseObject
	 */
	public GDYNEmployeeDiscResponseObject[] getResponseObject() {
		return responseObject;
	}

	/**
	 * @param responseObject the responseObject to set
	 */
	public void setResponseObject(GDYNEmployeeDiscResponseObject[] responseObject) {
		this.responseObject = responseObject;
	}

	public static void main(String[] args) {
        Employee clsEmployee = args.length == 5 ? new Employee(args[0], args[1], args[2], args[3], args[4]) : new Employee();
        System.out.println("Employee Object created " + clsEmployee.getLoginID());
        try {
            HardTotalsBuilderIfc builder = null;
            Serializable obj = null;
            EmployeeIfc a2 = null;
            Employee a1 = new Employee();
            builder = DomainGateway.getFactory().getHardTotalsBuilderInstance();
            a1.getHardTotalsData(builder);
            obj = builder.getHardTotalsOutput();
            builder.setHardTotalsInput(obj);
            a2 = (EmployeeIfc)builder.getFieldAsClass();
            a2.setHardTotalsData(builder);
            if (a1.equals(a2)) {
                System.out.println("Empty Employees are equal");
            } else {
                System.out.println("Empty Employees are NOT equal");
            }
            a1.setEmployeeID("1234");
            a1.setAlternateID("5678");
            a1.setPasswordBytes("jgs".getBytes("UTF-8"));
            a1.setLoginID("JGS");
            a1.setFullName("John Gray Swan");
            a1.setLoginStatus(3);
            Role role = new Role();
            role.setRoleID(10);
            role.setTitle("5678");
            RoleFunctionIfc[] funcs = new RoleFunctionIfc[5];
            for (int i = 0; i < 5; ++i) {
                RoleFunctionIfc rf = DomainGateway.getFactory().getRoleFunctionInstance();
                rf.setFunctionID(i);
                rf.setTitle("rolefunction" + i);
                funcs[i] = rf;
            }
            role.setFunctions(funcs);
            PersonNameIfc pn = DomainGateway.getFactory().getPersonNameInstance();
            pn.setFirstName("Wild");
            pn.setMiddleName("Bill");
            pn.setLastName("Hickock");
            pn.setSalutation("Mr.");
            pn.setSurname("None");
            pn.setTitle("Sir");
            a1.setName(pn);
            builder = DomainGateway.getFactory().getHardTotalsBuilderInstance();
            a1.getHardTotalsData(builder);
            obj = builder.getHardTotalsOutput();
            builder.setHardTotalsInput(obj);
            a2 = (EmployeeIfc)builder.getFieldAsClass();
            a2.setHardTotalsData(builder);
            if (a1.equals(a2)) {
                System.out.println("Full Employees are equal");
            } else {
                System.out.println("Full Employees are NOT equal");
            }
        }
        catch (UnsupportedEncodingException uee) {
            System.out.println("Password conversion failed:");
            uee.printStackTrace();
        }
        catch (HardTotalsFormatException iae) {
            System.out.println("Employee conversion failed:");
            iae.printStackTrace();
        }
    }

    public Employee() {
    }

    public Employee(String empID, String altID, String login, String pwd, String name) {
        this(empID, altID, login, pwd, (PersonNameIfc)null);
        this.fullName = name;
    }

    public Employee(String empID, String altID, String login, String pwd, PersonNameIfc pName) {
        this.employeeID = empID;
        this.alternateID = altID;
        this.setPassword(pwd);
        this.loginID = login;
        this.name = pName;
    }

    @Override
    public void assimilate(EmployeeIfc newEmployee) {
        Locale locale;
        RoleIfc role;
        this.setEmployeeID(newEmployee.getEmployeeID());
        this.setAlternateID(newEmployee.getAlternateID());
        this.setPasswordBytes(newEmployee.getPasswordBytes());
        this.setLoginID(newEmployee.getLoginID());
        this.setLoginStatus(newEmployee.getLoginStatus());
        PersonNameIfc name = newEmployee.getPersonName();
        if (name != null) {
            this.setPersonName((PersonNameIfc)name.clone());
            this.setFullName(name.getFullName());
        }
        if ((role = newEmployee.getRole()) != null) {
            this.setRole((RoleIfc)role.clone());
        }
        if ((locale = newEmployee.getPreferredLocale()) != null) {
            this.setPreferredLocale((Locale)locale.clone());
        }
        this.setDaysValid(newEmployee.getDaysValid());
        this.setExpirationDate(newEmployee.getExpirationDate());
        this.setStoreID(newEmployee.getStoreID());
        this.setType(newEmployee.getType());
        this.setPasswordChangeRequired(newEmployee.isPasswordChangeRequired());
        this.setNumberFailedPasswords(newEmployee.getNumberFailedPasswords());
        this.setPasswordCreationDate(newEmployee.getPasswordCreationDate());
        this.setFingerprintBiometrics(newEmployee.getFingerprintBiometrics());
        this.setLastLoginTime(newEmployee.getLastLoginTime());
    }

    public Object clone() {
        Employee c = new Employee();
        this.setCloneAttributes(c);
        return c;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        boolean isEqual = true;
        try {
            Employee c = (Employee)obj;
            isEqual = !Util.isObjectEqual((Object)this.employeeID, (Object)c.getEmployeeID()) ? false : (!Util.isObjectEqual((Object)this.alternateID, (Object)c.getAlternateID()) ? false : (!Arrays.equals(this.password, c.password) ? false : (!Util.isObjectEqual((Object)this.employeePasswordSalt, (Object)c.employeePasswordSalt) ? false : (!Util.isObjectEqual((Object)this.loginID, (Object)c.getLoginID()) ? false : (!Util.isObjectEqual((Object)this.name, (Object)c.getPersonName()) ? false : (this.loginStatus != c.getLoginStatus() ? false : (!Util.isObjectEqual((Object)this.role, (Object)c.getRole()) ? false : (!Util.isObjectEqual((Object)this.preferredLocale, (Object)c.getPreferredLocale()) ? false : (this.daysValid != c.getDaysValid() ? false : (!Util.isObjectEqual((Object)this.expirationDate, (Object)c.getExpirationDate()) ? false : (!Util.isObjectEqual((Object)this.storeID, (Object)c.getStoreID()) ? false : (!Util.isObjectEqual((Object)this.type, (Object)c.getType()) ? false : (this.passwordChangeRequired != c.isPasswordChangeRequired() ? false : (this.numberFailedPasswords != c.getNumberFailedPasswords() ? false : (!Util.isObjectEqual((Object)this.getPasswordCreationDate(), (Object)c.getPasswordCreationDate()) ? false : (!Util.isObjectEqual((Object)this.getFingerprintBiometrics(), (Object)c.getFingerprintBiometrics()) ? false : Util.isObjectEqual((Object)this.getLastLoginTime(), (Object)c.getLastLoginTime())))))))))))))))));
        }
        catch (Exception e) {
            isEqual = false;
        }
        return isEqual;
    }

    @Override
    public String getAlternateID() {
        return this.alternateID;
    }

    @Override
    public int getDaysValid() {
        return this.daysValid;
    }

    @Override
    public EmployeeComplianceIfc getEmployeeCompliance() {
        return this.employeeCompliance;
    }

    @Override
    public String getEmployeeID() {
        return this.employeeID;
    }

    @Override
    public EYSDate getExpirationDate() {
        return this.expirationDate;
    }

    @Override
    public String getFullName() {
        return this.fullName;
    }

    public void getHardTotalsData(HardTotalsBuilderIfc builder) {
        builder.appendStringObject(this.getClass().getName());
        builder.appendString(this.employeeID);
        builder.appendString(this.alternateID);
        builder.appendString(JdbcUtilities.base64encode((byte[])this.getPasswordBytes()));
        builder.appendString(this.loginID);
        builder.appendString(this.fullName);
        builder.appendInt(this.loginStatus);
        if (this.name == null) {
            builder.appendStringObject("null");
        } else {
            this.name.getHardTotalsData(builder);
        }
        if (this.role == null) {
            builder.appendStringObject("null");
        } else {
            this.role.getHardTotalsData(builder);
        }
    }

    @Override
    public String getLoginID() {
        return this.loginID;
    }

    @Override
    public int getLoginStatus() {
        return this.loginStatus;
    }

    @Override
    public PersonNameIfc getName() {
        return this.getPersonName();
    }

    @Override
    public int getNumberFailedPasswords() {
        return this.numberFailedPasswords;
    }

    @Override
    public String getPassword() {
        try {
            return new String(this.password, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            logger.error((Object)"Could not convert password to UTF-8 string", (Throwable)e);
            return new String(this.password);
        }
    }

    @Override
    public byte[] getPasswordBytes() {
        return this.password;
    }

    @Override
    public String getEmployeePasswordSalt() {
        return this.employeePasswordSalt;
    }

    @Override
    public Date getPasswordCreationDate() {
        return this.passwordCreationDate;
    }

    @Override
    public PersonNameIfc getPersonName() {
        return this.name;
    }

    @Override
    public Locale getPreferredLocale() {
        return this.preferredLocale;
    }

    public String getRevisionNumber() {
        return "$Revision: /rgbustores_13.4x_generic_branch/3 $";
    }

    @Override
    public RoleIfc getRole() {
        return this.role;
    }

    @Override
    public String getStoreID() {
        return this.storeID;
    }

    @Override
    public EmployeeTypeEnum getType() {
        return this.type;
    }

    @Override
    public byte[] getFingerprintBiometrics() {
        return this.fingerprintBiometrics;
    }

    @Override
    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    @Override
    public boolean hasAccessToFunction(int functionID) {
        return this.getRole().getFunctionAccess(functionID);
    }

    @Override
    public boolean isPasswordChangeRequired() {
        return this.passwordChangeRequired;
    }

    @Override
    public String loginStatusToString() {
        String strResult;
        try {
            strResult = EmployeeIfc.LOGIN_STATUS_DESCRIPTORS[this.loginStatus];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            strResult = "Invalid login status [" + this.loginStatus + "]";
        }
        return strResult;
    }

    @Override
    public void setAlternateID(String value) {
        this.alternateID = value;
    }

    @Override
    public void setDaysValid(int value) {
        this.daysValid = value;
    }

    @Override
    public void setEmployeeCompliance(EmployeeComplianceIfc employeeCompliance) {
        this.employeeCompliance = employeeCompliance;
    }

    @Override
    public void setEmployeeID(String value) {
        this.employeeID = value;
    }

    @Override
    public void setExpirationDate(EYSDate value) {
        this.expirationDate = value;
    }

    @Override
    public void setFullName(String value) {
        this.fullName = value;
    }

    public void setHardTotalsData(HardTotalsBuilderIfc builder) throws HardTotalsFormatException {
        this.employeeID = builder.getStringField();
        this.alternateID = builder.getStringField();
        this.setPasswordBytes(JdbcUtilities.base64decode((String)builder.getStringField()));
        this.loginID = builder.getStringField();
        this.fullName = builder.getStringField();
        this.loginStatus = builder.getIntField();
        this.name = (PersonNameIfc)builder.getFieldAsClass();
        if (this.name != null) {
            this.name.setHardTotalsData(builder);
        }
        this.role = (RoleIfc)builder.getFieldAsClass();
        if (this.role != null) {
            this.role.setHardTotalsData(builder);
        }
    }

    @Override
    public void setLoginID(String value) {
        this.loginID = value;
    }

    @Override
    public void setLoginStatus(int value) {
        this.loginStatus = value;
    }

    @Override
    public void setName(PersonNameIfc value) {
        this.setPersonName(value);
    }

    @Override
    public void setNumberFailedPasswords(int numberFailedPasswords) {
        this.numberFailedPasswords = numberFailedPasswords;
    }

    @Override
    public void setPassword(String value) {
        try {
            this.password = value.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            logger.error((Object)"Could not convert password from UTF-8 string", (Throwable)e);
        }
        this.password = value.getBytes();
    }

    @Override
    public void setPasswordBytes(byte[] value) {
        this.password = value;
    }

    @Override
    public void setEmployeePasswordSalt(String pwdSalt) {
        this.employeePasswordSalt = pwdSalt;
    }

    @Override
    public void setPasswordChangeRequired(boolean value) {
        this.passwordChangeRequired = value;
    }

    @Override
    public void setPasswordCreationDate(Date createDate) {
        this.passwordCreationDate = createDate;
    }

    @Override
    public void setPersonName(PersonNameIfc value) {
        this.name = value;
    }

    @Override
    public void setPreferredLocale(Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    @Override
    public void setRole(RoleIfc value) {
        this.role = value;
    }

    @Override
    public void setStoreID(String value) {
        this.storeID = value;
    }

    @Override
    public void setType(EmployeeTypeEnum type) {
        this.type = type;
    }

    @Override
    public void setFingerprintBiometrics(byte[] fingerprintBiometrics) {
        this.fingerprintBiometrics = fingerprintBiometrics;
    }

    @Override
    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public String toJournalString() {
        return this.toJournalString(LocaleMap.getLocale((String)"locale_Journaling"));
    }

    @Override
    public String toJournalString(Locale locale) {
        StringBuffer buff = new StringBuffer();
        buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.empId", (Object[])new Object[]{this.employeeID}, (Locale)locale));
        buff.append(Util.EOL);
        buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.alternateID", (Object[])new Object[]{this.alternateID}, (Locale)locale));
        buff.append(Util.EOL);
        buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.loginID", (Object[])new Object[]{this.loginID}, (Locale)locale));
        buff.append(Util.EOL);
        buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.fullName", (Object[])new Object[]{this.fullName}, (Locale)locale));
        buff.append(Util.EOL);
        buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.ssn", (Object[])new Object[]{"***-**-****"}, (Locale)locale));
        buff.append(Util.EOL);
        buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.loginStatus", (Object[])new Object[]{this.loginStatusToString()}, (Locale)locale));
        buff.append(Util.EOL);
        if (this.name == null) {
            buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.name", (Object[])new Object[]{null}, (Locale)locale));
            buff.append(Util.EOL);
        } else {
            buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.sub", (Object[])new Object[]{this.name}, (Locale)locale));
            buff.append(Util.EOL);
        }
        if (this.role == null) {
            buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.role", (Object[])new Object[]{null}, (Locale)locale));
            buff.append(Util.EOL);
        } else {
            buff.append(I18NHelper.getString((String)"EJournal", (String)"JournalEntry.employee.role", (Object[])new Object[]{this.role.hashCode()}, (Locale)locale));
            buff.append(Util.EOL);
        }
        return buff.toString();
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("Class:  Employee (Revision ");
        buff.append(this.getRevisionNumber());
        buff.append(") @");
        buff.append(this.hashCode());
        buff.append("\n");
        buff.append(this.toJournalString());
        return buff.toString();
    }

    protected void setCloneAttributes(Employee newClass) {
        if (this.employeeID != null) {
            newClass.setEmployeeID(new String(this.employeeID));
        }
        if (this.alternateID != null) {
            newClass.setAlternateID(new String(this.alternateID));
        }
        if (this.password != null) {
            newClass.setPasswordBytes(this.password);
        }
        if (this.employeePasswordSalt != null) {
            newClass.setEmployeePasswordSalt(this.employeePasswordSalt);
        }
        newClass.setNumberFailedPasswords(this.numberFailedPasswords);
        if (this.passwordCreationDate != null) {
            newClass.setPasswordCreationDate((Date)this.passwordCreationDate.clone());
        }
        if (this.loginID != null) {
            newClass.setLoginID(new String(this.loginID));
        }
        if (this.fullName != null) {
            newClass.setFullName(new String(this.fullName));
        }
        if (this.name != null) {
            newClass.setPersonName((PersonNameIfc)this.name.clone());
        }
        newClass.setLoginStatus(this.loginStatus);
        if (this.role != null) {
            newClass.setRole((RoleIfc)this.role.clone());
        }
        newClass.setDaysValid(this.daysValid);
        if (this.expirationDate != null) {
            newClass.setExpirationDate((EYSDate)this.expirationDate.clone());
        }
        newClass.setLoginStatus(this.loginStatus);
        newClass.setPasswordChangeRequired(this.passwordChangeRequired);
        if (this.storeID != null) {
            newClass.setStoreID(new String(this.storeID));
        }
        newClass.setType(this.type);
        if (this.preferredLocale != null) {
            newClass.setPreferredLocale((Locale)this.getPreferredLocale().clone());
        }
        if (this.fingerprintBiometrics != null) {
            newClass.setFingerprintBiometrics((byte[])this.fingerprintBiometrics.clone());
        }
        if (this.lastLoginTime != null) {
            newClass.setLastLoginTime((Date)this.lastLoginTime.clone());
        }
    }
}
