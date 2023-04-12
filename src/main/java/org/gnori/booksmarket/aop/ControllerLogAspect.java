package org.gnori.booksmarket.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class ControllerLogAspect {

  private static final String ERROR_TEXT_CONTROLLER = "::ASPECT:: Controller threw exception:";
  private static final String DEBUG_TEXT_ENTRY_CONTROLLER = "::ASPECT:: entry into method {} of class {} with {}";
  private static final String DEBUG_TEXT_EXIT_CONTROLLER = "::ASPECT:: exit from class {} method {}";

  @AfterThrowing(pointcut = "execution(* org.gnori.booksmarket.api.controller.*.*(..))", throwing = "ex")
  public void afterThrowingControllersMethods(Exception ex) {

    log.error(ERROR_TEXT_CONTROLLER + ex.getMessage());
  }

  @Around(value = "execution(* org.gnori.booksmarket.api.controller.*.*(..))")
  public Object entryAndExitFromControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {

    return entryAndExitFromController(joinPoint);
  }


  @AfterThrowing(pointcut = "execution(* org.gnori.booksmarket.security.auth.AuthenticationController.*(..))", throwing = "ex")
  public void afterThrowingAuthenticationControllerMethods(Exception ex) {

    log.error(ERROR_TEXT_CONTROLLER,ex);
  }

  @Around(value = "execution(* org.gnori.booksmarket.security.auth.AuthenticationController.*(..))")
  public Object entryAndExitFromAuthenticationControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {

    return entryAndExitFromController(joinPoint);
  }

  private Object entryAndExitFromController(ProceedingJoinPoint joinPoint) throws Throwable {
    var methodName = joinPoint.getSignature().getName();
    var className = joinPoint.getSignature().getDeclaringTypeName();
    var paramsName = joinPoint.getArgs();

    log.debug(DEBUG_TEXT_ENTRY_CONTROLLER, methodName, className , paramsName);

    Object proceed = joinPoint.proceed();

    log.debug(DEBUG_TEXT_EXIT_CONTROLLER, className, methodName);

    return proceed;
  }
}
