package org.example.ash.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Optional;

/**
 * Generic MapStruct contract for bidirectional mapping between
 * DTO (D), Entity (E), and Request (R).
 *
 * <pre>
 *   Request ──toEntity──► Entity ──toDto──► DTO
 *   DTO     ──fromDto──► Entity
 * </pre>
 *
 * <p>Note: {@code toEntity(R)} and a hypothetical {@code toEntity(D)} would share
 * the same type-erasure signature at runtime.  The DTO→Entity direction is
 * therefore named {@code fromDto} to keep the interface compilable.
 */
public interface EntityMapper<D, E, R> {

    // ── Single-object mappings ─────────────────────────────────────────────────

    /** Entity → DTO */
    D toDto(E entity);

    /** Request → Entity (create / update payloads) */
    E toEntity(R request);

    /** DTO → Entity */
    E fromDto(D dto);

    // ── List mappings ──────────────────────────────────────────────────────────

    List<D> toListDto(List<E> entities);

    List<E> toListEntity(List<R> requests);

    // ── Optional wrappers ──────────────────────────────────────────────────────

    /** Optional&lt;Entity&gt; → Optional&lt;DTO&gt; */
    default Optional<D> toDto(Optional<E> entity) {
        return entity.map(this::toDto);
    }

    /** Optional&lt;Request&gt; → Optional&lt;Entity&gt; */
    default Optional<E> toEntity(Optional<R> request) {
        return request.map(this::toEntity);
    }

    /** Optional&lt;DTO&gt; → Optional&lt;Entity&gt; */
    default Optional<E> fromDto(Optional<D> dto) {
        return dto.map(this::fromDto);
    }

    // ── Partial update ─────────────────────────────────────────────────────────

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}