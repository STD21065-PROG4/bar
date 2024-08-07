package com.company.base2.unit;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.company.base2.PojaGenerated;
import com.company.base2.conf.FacadeIT;
import com.company.base2.endpoint.event.consumer.model.ConsumableEvent;
import com.company.base2.endpoint.event.consumer.model.ConsumableEventTyper;
import com.company.base2.endpoint.event.consumer.model.TypedEvent;
import com.company.base2.endpoint.event.model.UuidCreated;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

@PojaGenerated
public class ConsumableEventTyperTest extends FacadeIT {
  public static final String UNKNOWN_TYPENAME = "unknown_typename";
  @Autowired ConsumableEventTyper subject;
  @Autowired ObjectMapper om;
  @MockBean SqsClient sqsClient;

  private SQSEvent.SQSMessage sqsMessageFrom(TypedEvent typedEvent) throws JsonProcessingException {
    var message = new SQSEvent.SQSMessage();
    message.setBody(
        "{\"detail-type\":\""
            + typedEvent.typeName()
            + "\", \"detail\":"
            + om.writeValueAsString(typedEvent.payload())
            + "}");
    return message;
  }

  private ConsumableEvent ackTypedEventfrom(TypedEvent typedEvent) {
    return new ConsumableEvent(typedEvent, () -> {}, () -> {});
  }

  @Test
  void to_acknowledgeable_typed_event_ok() throws JsonProcessingException {
    var uuid = randomUUID().toString();
    var uuidCreated = UuidCreated.builder().uuid(uuid).build();
    var payload = om.readValue(om.writeValueAsString(uuidCreated), UuidCreated.class);
    var typedEvent = new TypedEvent("com.company.base2.endpoint.event.model.UuidCreated", payload);

    var actualAcknowledgeableEvents = subject.apply(List.of(sqsMessageFrom(typedEvent)));
    var actualAcknowledgeableEvent = actualAcknowledgeableEvents.get(0);
    actualAcknowledgeableEvent.ack();

    verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    assertEquals(ackTypedEventfrom(typedEvent).getEvent(), actualAcknowledgeableEvent.getEvent());
  }

  @Test
  void to_acknowledgeable_typed_event_ko() throws JsonProcessingException {
    var uuid = randomUUID().toString();
    var uuidCreated = UuidCreated.builder().uuid(uuid).build();
    var payload = om.readValue(om.writeValueAsString(uuidCreated), UuidCreated.class);
    var unknownTypenameTypedEvent = new TypedEvent(UNKNOWN_TYPENAME, payload);
    var validTypedEvent =
        new TypedEvent("com.company.base2.endpoint.event.model.UuidCreated", payload);

    var actualAcknowledgeableEvents =
        subject.apply(
            List.of(sqsMessageFrom(unknownTypenameTypedEvent), sqsMessageFrom(validTypedEvent)));

    assertTrue(
        actualAcknowledgeableEvents.stream()
            .allMatch(ackTypedEvent -> ackTypedEvent.getEvent().equals(validTypedEvent)));
  }
}
