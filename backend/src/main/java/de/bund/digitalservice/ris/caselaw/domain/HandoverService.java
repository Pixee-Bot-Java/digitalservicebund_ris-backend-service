package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Service
@Slf4j
public class HandoverService {

  private final DocumentationUnitRepository repository;
  private final HandoverReportRepository handoverReportRepository;
  private final MailService mailService;
  private final DeltaMigrationRepository deltaMigrationRepository;

  @Value("${mail.exporter.recipientAddress:neuris@example.com}")
  private String recipientAddress;

  public HandoverService(
      DocumentationUnitRepository repository,
      MailService mailService,
      DeltaMigrationRepository migrationService,
      HandoverReportRepository handoverReportRepository) {

    this.repository = repository;
    this.mailService = mailService;
    this.deltaMigrationRepository = migrationService;
    this.handoverReportRepository = handoverReportRepository;
  }

  /**
   * Handover a documentation unit to the email service.
   *
   * @param documentationUnitId the UUID of the documentation unit
   * @param issuerAddress the email address of the issuer
   * @return the handover result
   * @throws DocumentationUnitNotExistsException if the documentation unit does not exist
   */
  public HandoverMail handoverAsMail(UUID documentationUnitId, String issuerAddress)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        repository
            .findByUuid(documentationUnitId)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentationUnitId));

    HandoverMail handoverMail =
        mailService.handOver(documentationUnit, recipientAddress, issuerAddress);
    if (!handoverMail.success()) {
      log.warn("Failed to send mail for documentation unit {}", documentationUnitId);
    }

    return handoverMail;
  }

  /**
   * Create a preview juris xml for a documentation unit.
   *
   * @param documentUuid the UUID of the documentation unit
   * @return the export result, containing the juris xml and export metadata
   */
  public XmlTransformationResult createPreviewXml(UUID documentUuid)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        repository
            .findByUuid(documentUuid)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUuid));
    return mailService.getXmlPreview(documentationUnit);
  }

  /**
   * Get the event log for a documentation unit, containing jDV email handover operations, handover
   * reports (response emails from the jDV) and migrations/import events.
   *
   * @param documentUuid the UUID of the documentation unit
   * @return the event log
   */
  public List<EventRecord> getEventLog(UUID documentUuid) {
    List<EventRecord> list =
        ListUtils.union(
            mailService.getHandoverResult(documentUuid),
            handoverReportRepository.getAllByDocumentationUnitUuid(documentUuid));
    var migration = deltaMigrationRepository.getLatestMigration(documentUuid);
    if (migration != null) {
      list.add(
          migration.xml() != null
              ? migration.toBuilder().xml(prettifyXml(migration.xml())).build()
              : migration);
    }
    list.sort(Comparator.comparing(EventRecord::getDate).reversed());
    return list;
  }

  /**
   * Prettify an XML string.
   *
   * @param xml the XML string
   * @return the prettified XML string
   */
  public static String prettifyXml(String xml) {
    try {
      Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      Node node =
          factory
              .newDocumentBuilder()
              .parse(new ByteArrayInputStream(xml.getBytes()))
              .getDocumentElement();

      StreamResult result = new StreamResult(new StringWriter());
      transformer.transform(new DOMSource(node), result);
      return result.getWriter().toString();

    } catch (TransformerException | IOException | ParserConfigurationException | SAXException e) {
      return "Could not prettify XML";
    }
  }
}
