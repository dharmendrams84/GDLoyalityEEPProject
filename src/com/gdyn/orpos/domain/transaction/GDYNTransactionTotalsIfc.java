package com.gdyn.orpos.domain.transaction;

import java.math.BigDecimal;

import oracle.retail.stores.domain.transaction.TransactionTotalsIfc;

public interface GDYNTransactionTotalsIfc extends TransactionTotalsIfc {

	public  Boolean getIsLoyalityDiscountApplied();
    
    public  void setIsLoyalityDiscountApplied(Boolean b);
    
    public  void setTotalLoyalityDiscountAmt(BigDecimal d);
    
    public  BigDecimal getTotalLoyalityDiscountAmt();
}
