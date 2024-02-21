package de.bund.digitalservice.ris.caselaw.domain;

public class DocumentationUnitException extends RuntimeException {
  public DocumentationUnitException(String message) {
    super(message);
  }

  public DocumentationUnitException(String message, Exception ex) {
    super(message, ex);
  }
}
