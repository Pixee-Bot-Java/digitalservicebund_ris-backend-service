package de.bund.digitalservice.ris.caselaw.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("deviating_decisiondate")
public class DeviatingDecisionDateDTO {
  @Id Long id;
  Long documentUnitId;
  String decisiondate;
}
