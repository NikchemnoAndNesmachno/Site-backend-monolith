package ua.nin.views.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.model.ViewCount;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ua.nin.views.model.ViewCount.ViewCountId;

class ViewCountsResponseMapperTest {

    private final ViewCountsResponseMapper mapper = Mappers.getMapper(ViewCountsResponseMapper.class);

    @Test
    void mapsViewCountToDto() {
        var updatedAt = Instant.now();
        ViewCount count = ViewCount.builder()
                .id(new ViewCountId("VIDEO", 1L))
                .totalViews(10L)
                .uniqueViews(5L)
                .updatedAt(updatedAt)
                .build();

        ViewCountsResponse response = mapper.toDto(count);
        assertEquals("VIDEO", response.targetType());
        assertEquals(1L, response.targetId());
        assertEquals(10L, response.totalViews());
        assertEquals(5L, response.uniqueViews());
        assertEquals(updatedAt, response.updatedAt());
    }
}
