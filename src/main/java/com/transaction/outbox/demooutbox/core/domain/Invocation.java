package com.transaction.outbox.demooutbox.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@NoArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Invocation {
    @JsonProperty("cn")
    private String className;
    @JsonProperty("m")
    private String methodName;
    @JsonProperty("p")
    private Class<?>[] parameterTypes;
    @JsonProperty("a")
    private List<ArgumentHolder> argumentHolders = new ArrayList<>();
    @JsonProperty("mdc")
    private Map<String, String> mdc;

    public Invocation(String className, String methodName, Class<?>[] parameterTypes, List<ArgumentHolder> argumentHolders) {
        this(className,methodName, parameterTypes,argumentHolders, null);
    }


    public Invocation(String className, String methodName, Class<?>[] parameterTypes, List<ArgumentHolder> argumentHolders, Map<String, String> mdc) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.argumentHolders = argumentHolders;
        this.mdc = mdc;
    }

    public void invoke(Object instance)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException{
        Class<?> clazz = Class.forName(className);
        log.debug("Retrieving method {} with parameterTypes {} from {}", methodName, argumentHolders,clazz);
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object[] args = argumentHolders.stream().map(ArgumentHolder::getArg).toArray();
        log.debug("Invoking method {} with args {}", method, Arrays.toString(args));
        if(mdc!= null && MDC.getMDCAdapter()!=null){
            Map<String ,String> oldMdc = MDC.getCopyOfContextMap();
            MDC.setContextMap(mdc);
            try{
                method.invoke(instance, args);
            } catch (InvocationTargetException e) {
                log.error("Error while invoking instance when MDC parameter is not null {} with given error {}" ,instance,e);
                throw new InvocationTargetException(e.getCause());
            }
            finally {
                if(oldMdc == null){
                    MDC.clear();
                }
                else {
                    MDC.setContextMap(oldMdc);
                }
            }
        }else{
            try{
                method.invoke(instance, args);
            } catch (InvocationTargetException e) {
                log.error("Error while invoking instance {} with given error {}" ,instance,e);
                throw new InvocationTargetException(e.getCause());
            }
        }

    }
}
