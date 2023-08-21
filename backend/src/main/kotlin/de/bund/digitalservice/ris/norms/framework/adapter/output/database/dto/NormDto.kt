package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "norms")
data class NormDto(
    @Id val guid: UUID,
    @Column("e_gesetzgebung") val eGesetzgebung: Boolean = false
) : Persistable<UUID> {

  @Transient var newEntry: Boolean = true

  override fun getId(): UUID = guid

  override fun isNew(): Boolean = newEntry
}
