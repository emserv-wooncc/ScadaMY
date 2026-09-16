package com.serotonin.db.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class GenericTransactionTemplate extends TransactionTemplate {

    public GenericTransactionTemplate() {
        super();
    }

    public GenericTransactionTemplate(PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }
}
