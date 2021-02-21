package org.folio.ed.error;

/**
 * Specific exception for handlig edge-authorization process
 */
public class AuthorizationException extends RuntimeException {
  public AuthorizationException(String message) {
    super(message);
  }
}
