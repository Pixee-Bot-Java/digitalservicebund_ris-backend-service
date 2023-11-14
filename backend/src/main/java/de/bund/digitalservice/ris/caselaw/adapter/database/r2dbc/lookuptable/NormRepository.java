package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NormRepository extends R2dbcRepository<NormDTO, Long> {

  Flux<NormDTO> findAllByOrderByFieldOfLawIdAscAbbreviationAsc();

  Flux<NormDTO> findAllByFieldOfLawIdOrderByAbbreviationAscSingleNormDescriptionAsc(
      Long fieldOfLawId);
}
