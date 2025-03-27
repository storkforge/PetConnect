package se.storkforge.petconnect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleMockTest {

    @Mock
    private MyService myService; // Replace with a simple interface

    @Test
    void testSimpleMock() {
        // Create a dummy ChatResponse (or any other simple object)
        ChatResponse dummyResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("test"))));

        when(myService.someMethod()).thenReturn(dummyResponse); // Replace with a simple method call

        // Add assertions if needed
    }

    // Define a simple interface for testing
    interface MyService {
        ChatResponse someMethod();
    }
}