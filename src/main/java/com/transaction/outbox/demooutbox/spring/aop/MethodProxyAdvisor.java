package com.transaction.outbox.demooutbox.spring.aop;

import com.sun.istack.NotNull;
import com.transaction.outbox.demooutbox.annotation.TransactionOutbox;
import com.transaction.outbox.demooutbox.configuration.ApplicationConfiguration;
import com.transaction.outbox.demooutbox.core.Outbox;
import com.transaction.outbox.demooutbox.core.domain.ArgumentHolder;
import com.transaction.outbox.demooutbox.core.domain.Invocation;
import com.transaction.outbox.demooutbox.core.domain.TransactionOutBoxEntry;
import com.transaction.outbox.demooutbox.core.exception.TransactionOutBoxException;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MethodProxyAdvisor extends AbstractPointcutAdvisor {
    private static Logger logger= LoggerFactory.getLogger(MethodProxyAdvisor.class);
    private final transient Pointcut pointcut= methodPointCutFor(TransactionOutbox.class);
    private final transient Advice advice;

    public MethodProxyAdvisor(Outbox taskExecutor, ApplicationConfiguration applicationConfiguration) {
        this.advice = new LockingInterceptor(taskExecutor,applicationConfiguration, outbox, applicationConfiguration1);
    }

    @NotNull
    private static AnnotationMatchingPointcut methodPointCutFor(Class<? extends Annotation> methodAnnotationType) {
   return new AnnotationMatchingPointcut(null ,methodAnnotationType, true);
    }
    @NotNull
    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @NotNull
    @Override
    public Advice getAdvice() {
        return advice;
    }

    private static class LockingInterceptor implements MethodInterceptor {
        private final Outbox outbox;
        private final ApplicationConfiguration applicationConfiguration;
        public LockingInterceptor(Outbox taskExecutor, ApplicationConfiguration applicationConfiguration) {
            this.outbox = taskExecutor;
            this.applicationConfiguration = applicationConfiguration;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            logger.info(">>> Method interception by Transaction out box started");
            logger.debug("invocation ->{}", invocation);
            Class<?> returnType = invocation.getMethod().getReturnType();
            if(!void.class.equals(returnType)){
                throw new TransactionOutBoxException("cannot apply TransactionOutBox on method returning value");
            }
            outbox.scheduleImmediately(newEntry(invocation));
            logger.info(">>>Method Interception by TransactionOutbox ended<<<");
            return Optional.empty();
        }

        private TransactionOutBoxEntry newEntry(MethodInvocation methodInvocation) {
            TransactionOutbox annotation = AnnotationUtils.findAnnotation(methodInvocation.getMethod(),TransactionOutbox.class);
            if(annotation == null){
                throw new TransactionOutBoxException("TransactionOutbox annotation missing");
            }

            logger.debug("Annotation: {}",annotation);
            final String flushMethodName = annotation.nonTransactionalMethod();
            List<ArgumentHolder> argumentHolders = null;
            argumentHolders= new ArrayList<>(methodInvocation.getArguments().length);
            for(int i=0;i< methodInvocation.getArguments().length; i++){
                ArgumentHolder argumentHolder= ArgumentHolder.builder()
                        .argType(methodInvocation.getArguments()[i].getClass().getCanonicalName())
                        .arg(methodInvocation.getArguments()[i])
                        .build();
                argumentHolders.add(argumentHolder);
            }
            Invocation invocation = Invocation.builder()
                    .argumentHolders(argumentHolders)
                    .className(ClassUtils.getUserClass(methodInvocation.getMethod().getDeclaringClass()).getName())
                    .methodName(flushMethodName)
                    .mdc((MDC.getMDCAdapter()!=null)? MDC.getCopyOfContextMap():null)
                    .parameterTypes(methodInvocation.getMethod().getParameterTypes())
                    .build();
            return TransactionOutBoxEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .uniqueRequestId(UUID.randomUUID().toString())
                    .invocation(invocation).application(applicationConfiguration.getTranOutboxClient())
                    .build();

        }
    }
}
