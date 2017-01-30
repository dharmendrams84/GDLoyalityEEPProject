package oracle.retail.stores.domain.manager.datareplication;

public class CouponHierarchy 
{
	String applyTO="";
	String division="";
	String group="";
	String department="";
	String hierarchyclass = "";
	String subClass = "";
	
	
	public CouponHierarchy(String applyTO, String division, String group,
			String department, String hierarchyclass, String subClass) 
	{
		this.applyTO=applyTO;
		this.division=division;
		this.group=group;
		this.department=department;
		this.hierarchyclass=hierarchyclass;
		this.subClass=subClass;
	}
	
	public String getApplyTO() {
		return applyTO;
	}
	public void setApplyTO(String applyTO) {
		this.applyTO = applyTO;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getHierarchyclass() {
		return hierarchyclass;
	}
	public void setHierarchyclass(String hierarchyclass) {
		this.hierarchyclass = hierarchyclass;
	}
	public String getSubClass() {
		return subClass;
	}
	public void setSubClass(String subClass) {
		this.subClass = subClass;
	}

}
