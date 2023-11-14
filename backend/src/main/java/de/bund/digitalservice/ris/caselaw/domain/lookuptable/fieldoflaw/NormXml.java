package de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NormXml {
  @JsonProperty(value = "normabk")
  String abbreviation;

  @JsonProperty(value = "enbez")
  String singleNormDescription;
}
