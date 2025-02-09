package org.peter_lukas.shirtso.analytics;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@Aspect
@Component
public class ExecTimeDiagAsp {

    @Pointcut("@annotation(LogExecutionTime)")
    private void annotatedMethod() {
//        being used to define pointcut - used below
    }

    @Around("annotatedMethod()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object proceed = joinPoint.proceed();
        long stop = System.nanoTime();

        long executionTime = recalculateExecutionTimeUnit(stop - start, joinPoint);
        log.info("{} executed in {} {}", joinPoint.getStaticPart().getSignature(), executionTime, getChronoUnit(joinPoint));
        return proceed;
    }

    private long recalculateExecutionTimeUnit(long time, ProceedingJoinPoint joinPoint) {
        Duration duration = getChronoUnit(joinPoint).getDuration();
        return time / (1_000_000_000 / (1_000_000_000/ duration.getNano()));
    }

    private ChronoUnit getChronoUnit(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getStaticPart().getSignature();
        Method method = signature.getMethod();
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
        return annotation.timeUnit();
    }
}
