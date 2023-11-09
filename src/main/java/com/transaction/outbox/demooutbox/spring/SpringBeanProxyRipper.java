package com.transaction.outbox.demooutbox.spring;

import com.transaction.outbox.demooutbox.api.InvocationInstantiable;
import com.transaction.outbox.demooutbox.core.domain.Invocation;
import com.transaction.outbox.demooutbox.spring.aop.MethodProxyAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
@Slf4j
public class SpringBeanProxyRipper implements InvocationInstantiable {
    private ApplicationContext applicationContext;
    private MethodProxyAdvisor methodProxyAdvisor;
    public SpringBeanProxyRipper(MethodProxyAdvisor methodProxyAdvisor) {
        this.methodProxyAdvisor = methodProxyAdvisor;
    }

    @Override
    public Optional<Object> instantiate(Invocation invocation) {
        try{
            log.debug("ripping off the proxy, for invocation: {}", invocation);
            Class<?> clazz = Class.forName(invocation.getClassName());
            Object springManagedBean= applicationContext.getBean(clazz);
            Object objectToReturn = ripMethodProxyAdvisor(springManagedBean);
            log.debug("Ripped target : {}",objectToReturn);
            return Optional.ofNullable(objectToReturn);
        } catch (ClassNotFoundException e) {
            log.error(String.format("Error while recreating proxy for invocation :%s", invocation.toString()),e);
        }
        return Optional.empty();
    }

    private Object ripMethodProxyAdvisor(Object proxy) {
        if(AopUtils.isCglibProxy(proxy)){
            ((Advised) proxy).removeAdvisor(methodProxyAdvisor);
        }
        return proxy;
    }
}
