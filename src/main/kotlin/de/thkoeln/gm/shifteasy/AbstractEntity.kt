package de.thkoeln.gm.shifteasy

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import lombok.EqualsAndHashCode
import lombok.Getter

import java.util.UUID;

@EqualsAndHashCode
@MappedSuperclass
abstract class AbstractEntity protected constructor() {
    @Id
    @Getter
    protected var id: UUID = UUID.randomUUID()
}