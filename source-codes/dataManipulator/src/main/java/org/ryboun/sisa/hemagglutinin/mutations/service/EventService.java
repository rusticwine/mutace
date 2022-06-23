package org.ryboun.sisa.hemagglutinin.mutations.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.Event;
import org.ryboun.sisa.hemagglutinin.mutations.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    EventRepository eventRepository;

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExchangeFilterFunction logSequenceDownloadRequest() {
        return Utils.logRequest(str -> logRequestEvent(Event.EventType.SEQUENCE_DOWNLOAD, str, "sequence downloaded"));
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent() {

    }


    //TODO - need new transaction
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void logRequestEvent(Event.EventType eventType, String loggableData, String description) {
        Event event = Event.builder()
                .eventType(eventType)
                .description(description)
                .created(LocalDateTime.now())
                .comment(loggableData)
                .build();

        eventRepository.save(event);
    }
}
