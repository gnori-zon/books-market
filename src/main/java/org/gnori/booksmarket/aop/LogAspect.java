package org.gnori.booksmarket.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class LogAspect {

  private static final String DEBUG_TEXT_EXECUTION_TIME = "::ASPECT:: {} executed in {} ms";
  private static final String DEBUG_TEXT_HANDLE_EXCEPTION = "::ASPECT:: CustomExceptionHandler handle exception during execution of application {}";


  @Around("@annotation(LogExecutionTime)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
    long startTime = System.currentTimeMillis();

    Object proceed = joinPoint.proceed();

    long executionTime = System.currentTimeMillis() - startTime;

    log.debug(DEBUG_TEXT_EXECUTION_TIME, joinPoint.getSignature(),  executionTime);

    return proceed;
  }

  @Before(value = "execution(* org.gnori.booksmarket.api.exception.CustomExceptionHandler.handleAuthenticationException(..))")
  public void beforeHandleException(JoinPoint joinPoint) {

    log.debug(DEBUG_TEXT_HANDLE_EXCEPTION, joinPoint.getArgs());
  }

}
