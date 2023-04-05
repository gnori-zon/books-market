package org.gnori.booksmarket.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DelegatedAuthenticationEntryPoint implements AuthenticationEntryPoint {

  HandlerExceptionResolver resolver;

  public DelegatedAuthenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {

    this.resolver = resolver;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) {

    resolver.resolveException(request,response, null, authException);
  }
}
