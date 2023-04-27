package org.gnori.booksmarket.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  @Value("${jwt.secret}")
  private String SECRET_KEY;

  @LogExecutionTime
  public String extractUsername(String token) {

    return extractClaim(token, Claims::getSubject);
  }

  @LogExecutionTime
  public String generateToken(UserDetails userDetails) {

    return generateToken(new HashMap<>(),userDetails);
  }

  private String generateToken(
      Map<String, Object> extractClaims, // additional claims
      UserDetails userDetails) {

    return Jwts.builder()
        .setClaims(extractClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 24 * 30))) // 24 hours
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  @LogExecutionTime
  public boolean isTokenValid(String token, UserDetails userDetails){

     final String username = extractUsername(token);
     return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {

    return extractExpiration(token).before(new Date(System.currentTimeMillis()));
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private  <T> T extractClaim(String token, Function<Claims,T> claimsResolver){

    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token){

    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {

    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
