package org.peter_lukas.shirtso.analytics;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecTimeDiagAsp {

    @Pointcut("@annotation(LogExecutionTime)")
    private void annotatedMethod() {
//
    }

    @Around("annotatedMethod()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long stop = System.currentTimeMillis();

        log.info("{} executed in {}", joinPoint.getStaticPart().getSignature(), stop - start);
        return proceed;
    }
}
