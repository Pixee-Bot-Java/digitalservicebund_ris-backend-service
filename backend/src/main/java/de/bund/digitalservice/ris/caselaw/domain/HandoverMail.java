package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a mail to handover a documentation unit to jDV
 *
 * @param documentationUnitId the UUID of the documentation unit
 * @param receiverAddress the address of the receiver (jDV mail interface)
 * @param mailSubject the subject of the mail containing handover options (e.g. desired operation)
 * @param xml the xml as string
 * @param success true if the XML export operation was successful (200)
 * @param statusMessages a list of issues found during the export operation
 * @param fileName the name of the attached file (<documentNumber>.xml)
 * @param handoverDate the date the mail was sent
 * @param issuerAddress the address of the user issuing the handover, used to redirect replies by
 *     the mail interface
 */
@Builder(toBuilder = true)
public record HandoverMail(
    UUID documentationUnitId,
    String receiverAddress,
    String mailSubject,
    String xml,
    @Getter boolean success,
    List<String> statusMessages,
    String fileName,
    @Getter @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "date")
        Instant handoverDate,
    @Getter String issuerAddress)
    implements EventRecord {
  @Override
  public EventType getType() {
    return EventType.HANDOVER;
  }

  @Override
  public Instant getDate() {
    return getHandoverDate();
  }
}
