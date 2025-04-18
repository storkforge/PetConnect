package se.storkforge.petconnect.service.storageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.storkforge.petconnect.config.AiConfig;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.repository.MeetUpRepository;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.service.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UserServiceCachingTest.TestAiConfig.class, MeetUpService.class})
@ActiveProfiles("test")
class UserServiceCachingTest {

    @Autowired
    private MeetUpService meetUpService;

    @MockitoBean
    private MeetUpRepository meetUpRepository;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private SmsService smsService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("nearbyMeetUpsCache").clear();
    }

    @TestConfiguration
    @EnableCaching
    static class TestAiConfig {

        @Bean
        public AiConfig aiConfig() {
            return new AiConfig() {
                @Override
                public String getOpenAiApiKey() {
                    return "dummy-key";
                }
            };
        }

        @Bean
        public ChatClient chatClient() {
            return mock(ChatClient.class);
        }

        @Bean
        public AiRecommendationExecutor aiRecommendationExecutor(ChatClient chatClient) {
            return new AiRecommendationExecutor(chatClient);
        }

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("nearbyMeetUpsCache");
        }
    }

    @Test
    void testMeetUpSearchCaching() {
        var longitude = 11.97;
        var latitude = 57.70;
        var radiusInKm = 10.0;
        var start = LocalDateTime.of(2025, 4, 18, 12, 0);
        var end = start.plusDays(1);

        var mockMeetUps = List.of(new MeetUp());
        when(meetUpRepository.findMeetUpsNearAndWithinTime(longitude, latitude, radiusInKm * 1000, start, end))
                .thenReturn(mockMeetUps);

        var firstCall = meetUpService.searchMeetUps(longitude, latitude, radiusInKm, start, end);
        var secondCall = meetUpService.searchMeetUps(longitude, latitude, radiusInKm, start, end);

        assertEquals(firstCall, secondCall);
        verify(meetUpRepository, times(1)).findMeetUpsNearAndWithinTime(anyDouble(), anyDouble(), anyDouble(), any(), any());
    }

}

