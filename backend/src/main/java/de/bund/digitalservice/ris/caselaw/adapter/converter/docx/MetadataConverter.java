package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.docx4j.wml.P;

/** Converter to convert docx4j metadata list to Metadata */
public class MetadataConverter {
  private MetadataConverter() {}

  /**
   * Convert a list of docx4j content elements to {@link ParagraphElement}
   *
   * @param content list of docx4j objects
   * @param converter converter to convert the objects to domain objects
   * @return a parent paragraph element with the content elements as children
   */
  public static ParagraphElement convert(List<Object> content, DocxConverter converter) {
    AtomicReference<ParagraphElement> paragraphElement = new AtomicReference<>();

    content.forEach(
        c -> {
          if (c instanceof P p) {
            paragraphElement.set(ParagraphConverter.convert(p, converter));
          }
        });

    return paragraphElement.get();
  }
}
