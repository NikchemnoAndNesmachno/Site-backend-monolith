package ua.nin.views.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.mapper.ViewCountsResponseMapper;
import ua.nin.views.model.ViewCount;
import ua.nin.views.repository.ViewCountRepository;
import ua.nin.views.repository.ViewUniqueRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewServiceTest {

    @Mock
    private ViewUniqueRepository uniqueRepo;
    @Mock
    private ViewCountRepository countRepo;
    @Mock
    private ViewCountsResponseMapper mapper;

    @InjectMocks
    private ViewService viewService;

    @Test
    void recordView_insertsUniqueAndIncrements() {
        when(uniqueRepo.insertUniqueIfAbsent(anyString(), anyLong(), anyString(), any(), any())).thenReturn(1);

        viewService.recordView("video", 1L, 2L, "UA", "127.0.0.1");

        verify(uniqueRepo).insertUniqueIfAbsent(eq("VIDEO"), eq(1L), anyString(), any(), any());
        verify(countRepo).upsertIncrement("VIDEO", 1L, 1, 1);
    }

    @Test
    void recordView_duplicateUniqueOnlyIncrementsTotal() {
        when(uniqueRepo.insertUniqueIfAbsent(anyString(), anyLong(), anyString(), any(), any())).thenReturn(0);

        viewService.recordView("video", 1L, null, "UA", "127.0.0.1");

        verify(countRepo).upsertIncrement("VIDEO", 1L, 1, 0);
    }

    @Test
    void getCounts_mapsResponse() {
        ViewCount count = ViewCount.builder().id(new ViewCount.ViewCountId("VIDEO", 1L)).totalViews(10L).uniqueViews(5L).build();
        ViewCountsResponse response = new ViewCountsResponse("VIDEO", 1L, 10L, 5L, null);
        when(countRepo.findCountsByTarget("VIDEO", 1L)).thenReturn(count);
        when(mapper.toDto(count)).thenReturn(response);

        ViewCountsResponse result = viewService.getCounts("video", 1L);

        assertEquals(10L, result.totalViews());
    }

    @Test
    void recordView_usesPepper() {
        ReflectionTestUtils.setField(viewService, "pepper", "pepper");
        when(uniqueRepo.insertUniqueIfAbsent(anyString(), anyLong(), anyString(), any(), any())).thenReturn(1);

        viewService.recordView("video", 1L, null, "UA", "127.0.0.1");

        verify(uniqueRepo).insertUniqueIfAbsent(eq("VIDEO"), eq(1L), argThat(hash -> hash.length() == 64), any(), any());
    }
}
