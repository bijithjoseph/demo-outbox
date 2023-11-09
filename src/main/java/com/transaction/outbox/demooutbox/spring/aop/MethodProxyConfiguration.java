package com.transaction.outbox.demooutbox.spring.aop;

import com.transaction.outbox.demooutbox.api.InvocationInstantiable;
import com.transaction.outbox.demooutbox.configuration.ApplicationConfiguration;
import com.transaction.outbox.demooutbox.core.Outbox;
import com.transaction.outbox.demooutbox.core.OutboxCleaner;
import com.transaction.outbox.demooutbox.core.dao.DataAccessor;
import com.transaction.outbox.demooutbox.spring.ImportAwareConfiguration;
import com.transaction.outbox.demooutbox.spring.SpringBeanProxyRipper;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.ExecutorService;

public class MethodProxyConfiguration extends ImportAwareConfiguration {
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public ApplicationConfiguration applicationConfiguration(){
        return new ApplicationConfiguration();
    }

    @Bean
    public DefaultJpaBasedDataAccessor dataAccessor(TransactionOutboxEntryRepository transactionOutboxEntryRepository,
                                                    ApplicationConfiguration applicationConfiguration){
        return new DefaultJpaBasedDataAccessor(transactionOutboxEntryRepository,applicationConfiguration);
    }

    @Bean
    public InvocationInstantiable proxyRipper(MethodProxyAdvisor methodProxyAdvisor){
        return new SpringBeanProxyRipper(methodProxyAdvisor);
    }

    @Bean
    int entryReattemptIntervalInSeconds(@Value("${outbox.clean.wait.interval:60}")int entryReattempIntervalInSeconds){
        return entryReattempIntervalInSeconds;
    }

    @Bean
    public OutboxCleaner outboxCleaner(DataAccessor dataAccessor, InvocationInstantiable invocationInstantiable){
        return new DefaultOutBoxCleaner(dataAccessor, invocationInstantiable);
    }

    @Bean
    public Outbox outbox(ExecutorService executorService, DataAccessor dataAccessor,OutboxCleaner outboxCleaner){
        return  new DefaultOubox(executorService, dataAccessor,outboxCleaner);
    }

    @Bean
    public MethodProxyAdvisor proxyScheduledLockAopBeanPostProcessor(@Lazy Outbox outbox,
                                                                     @Lazy ApplicationConfiguration applicationConfiguration){
        MethodProxyAdvisor advisor = new MethodProxyAdvisor(outbox, applicationConfiguration);
        advisor.setOrder(getOrder());
        return advisor;
    }

}
