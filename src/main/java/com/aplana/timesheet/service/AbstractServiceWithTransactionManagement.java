package com.aplana.timesheet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

import java.util.Arrays;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractServiceWithTransactionManagement {

    private static final RuleBasedTransactionAttribute TRANSACTION_DEFINITION = new RuleBasedTransactionAttribute();

    static {
        TRANSACTION_DEFINITION.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TRANSACTION_DEFINITION.setRollbackRules(
                Arrays.<RollbackRuleAttribute>asList(new NoRollbackRuleAttribute(DataAccessException.class))
        );
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    protected final TransactionStatus getNewTransaction() {
        return transactionManager.getTransaction(TRANSACTION_DEFINITION);
    }

    protected final void commit(TransactionStatus transactionStatus) {
        transactionManager.commit(transactionStatus);
    }

}
