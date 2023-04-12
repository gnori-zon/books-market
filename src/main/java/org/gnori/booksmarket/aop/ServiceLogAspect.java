package org.gnori.booksmarket.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class ServiceLogAspect {

  private static final String ERROR_TEXT_FILE_SERVICE = "::ASPECT:: FileServiceImpl threw exception: ";
  private static final String ERROR_TEXT_JWT_SERVICE = "::ASPECT:: JwtService threw exception: ";

  @AfterThrowing(pointcut = "execution(* org.gnori.booksmarket.service.impl.FileServiceImpl.*(..))", throwing = "ex")
  public void afterThrowingFileServiceImplMethods(Exception ex) {

    log.error(ERROR_TEXT_FILE_SERVICE + ex.getMessage());
  }

  @AfterThrowing(pointcut = "execution(* org.gnori.booksmarket.service.utils.TempFileCleanerThread.*(..))", throwing = "ex")
  public void afterThrowingTempFileCleanerThreadMethods(Exception ex) {

    log.error(ERROR_TEXT_FILE_SERVICE + ex.getMessage());
  }

  @AfterThrowing(pointcut = "execution(public * org.gnori.booksmarket.security.config.JwtService.*(..))", throwing = "ex")
  public void afterThrowingJwtServiceMethods(Exception ex) {

    log.error(ERROR_TEXT_JWT_SERVICE + ex.getMessage());
  }
}
