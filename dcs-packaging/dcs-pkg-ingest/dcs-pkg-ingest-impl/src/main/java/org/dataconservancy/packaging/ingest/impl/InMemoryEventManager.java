/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.ingest.impl;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcs.DcsEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of EventManager which stores all Events in memory.  Note that this implementation is <em>not</em>
 * thread-safe.
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIdService(BulkIdCreationService)}</dt>
 * <dd><b>Required</b>. An ID service is used to assign IDs to new events
 * created through {@link #newEvent(String)}.</dd>
 * <dt>{@link #setIdBatchSize(int)}</dt>
 * <dd><b>Optional</b>.  The number of identifiers to request from the {@code BulkIdCreationService} at a time.</dd>
 * </dl>
 */
public class InMemoryEventManager implements EventManager {

    /**
     * Data structure optimized to lookup event objects by their identifier.
     */
    private final Map<String, DcsEvent> eventsById = new HashMap<String, DcsEvent>();

    /**
     * Data structure optimized to lookup event objects by their event type.
     */
    private final Map<String, Set<DcsEvent>> eventsByType = new HashMap<String, Set<DcsEvent>>();

    /**
     * Used to create a bunch of identifiers at once.
     */
    private BulkIdCreationService idService;

    /**
     * The number of identifiers to create at a time.
     */
    private int idBatchSize = 1000;

    /**
     * Iterator over the Identifier objects produced by the {@link #idService bulk id creation service}
     */
    private Iterator<Identifier> idIterator;

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:
     * <br/>
     * The SIP identifier is ignored, the {@code event} must not be {@code null}, and the {@code event} will
     * be defensively copied before being persisted.
     *
     * @param id {@inheritDoc} (ignored by this implementation)
     * @param event {@inheritDoc} Must not be {@code null}, will be defensively copied prior to being persisted.
     */
    @Override
    public void addEvent(String id, DcsEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null.");
        }

        synchronized (id.intern()) {
            copyAndAddEvent(event);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:
     * <br/>
     * The SIP identifier is ignored, the {@code events} must not be {@code null}, and each {@code event} will
     * be defensively copied before being persisted.
     *
     * @param id {@inheritDoc} (ignored by this implementation)
     * @param events {@inheritDoc} Must not be {@code null}, each event will be defensively copied prior to being
     *                            persisted.
     */
    @Override
    public void addEvents(String id, Collection<DcsEvent> events) {
        if (events == null) {
            throw new IllegalArgumentException("Events must not be null.");
        }

        synchronized (id.intern()) {
            for (DcsEvent e : events) {
                if (e == null) {
                    continue;
                }

                copyAndAddEvent(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:
     * <br/>
     * The SIP identifier is ignored; events will be defensively copied before being returned.
     *
     * @param id     {@inheritDoc} (ignored by this implementation)
     * @param eventTypes {@inheritDoc}
     */
    @Override
    public Collection<DcsEvent> getEvents(String id, String... eventTypes) {
        final HashSet<DcsEvent> result = new HashSet<DcsEvent>();

        if (eventTypes == null || eventTypes.length == 0) {
            synchronized (id.intern()) {
                for (Map.Entry<String,DcsEvent> entry : eventsById.entrySet()) {
                    result.add(new DcsEvent(entry.getValue()));
                }
            }

            return result;
        }

        for (String eventType : eventTypes) {
            synchronized (id.intern()) {
                if (eventsByType.containsKey(eventType)) {
                    for (DcsEvent e : eventsByType.get(eventType)) {
                        result.add(new DcsEvent(e));
                    }
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:
     * <br/>
     * The SIP identifier is ignored; if {@code eventType} is {@code null}, {@code null} is returned; events will be
     * defensively copied prior to being returned.
     *
     * @param id         {@inheritDoc} (ignored by this implementation)
     * @param eventType {@inheritDoc} May be {@code null}
     */
    @Override
    public DcsEvent getEventByType(String id, String eventType) {
        if (eventType == null || eventType.trim().length() == 0) {
            return null;
        }

        if (!eventsByType.containsKey(eventType)) {
            return null;
        }

        return new DcsEvent(eventsByType.get(eventType).iterator().next());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:
     * <br/>
     * The SIP identifier is ignored; if {@code eventType} is {@code null}, {@code null} is returned; events will be
     * defensively copied prior to being returned.
     */
    @Override
    public DcsEvent findEventById(String eventId) {
        if (eventsById.containsKey(eventId)) {
            return new DcsEvent(eventsById.get(eventId));
        }

        return null;
    }

    @Override
    public DcsEvent newEvent(String eventType) {
        if (eventType == null || eventType.trim().length() == 0) {
            throw new IllegalArgumentException("Event type string must not be null or empty.");
        }

        if (idIterator == null || !idIterator.hasNext()) {
            refreshIterator();
        }

        DcsEvent event = new DcsEvent();
        event.setEventType(eventType);
        event.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        event.setId(idIterator.next().getUrl().toString());
        return event;
    }

    /**
     * The ID creation service.  The caller instantiating an instance of this event manager must set an ID creation
     * service.
     *
     * @param ids the id creation service
     */
    public void setIdService(BulkIdCreationService ids) {
        if (ids == null) {
            throw new IllegalArgumentException("BulkIdCreationService must not be null.");
        }
        idService = ids;
    }

    /**
     * Returns the ID creation service.  May be {@code null} if one hasn't been
     * {@link #setIdService(org.dataconservancy.dcs.id.api.BulkIdCreationService) set}.
     *
     * @return the id creation service
     */
    public BulkIdCreationService getIdService() {
        return idService;
    }

    /**
     * The number of identifiers to request from the bulk id creation service at a time.
     *
     * @return the number of identifiers to request from the bulk id creation service at a time.
     */
    public int getIdBatchSize() {
        return idBatchSize;
    }

    /**
     * The number of identifiers to request from the bulk id creation service at a time.
     *
     * @param idBatchSize the number of identifiers to request from the bulk id creation service at a time
     */
    public void setIdBatchSize(int idBatchSize) {
        if (idBatchSize < 1) {
            throw new IllegalArgumentException("Batch size must be a positive integer.");
        }
        this.idBatchSize = idBatchSize;
    }

    /**
     * Creates a copy of the supplied event and stores the copy in the backing data structure(s).
     *
     * @param e the event
     */
    private void copyAndAddEvent(DcsEvent e) {
        final DcsEvent copy = new DcsEvent(e);
        addEventById(copy);
        addEventByType(copy);
    }

    /**
     * Adds the supplied event to the data structure optimized for lookup of events by their type.
     *
     * @param e the event
     */
    private void addEventByType(DcsEvent e) {
        if (eventsByType.containsKey(e.getEventType())) {
            eventsByType.get(e.getEventType()).add(e);
        } else {
            HashSet<DcsEvent> events = new HashSet<DcsEvent>();
            events.add(e);
            eventsByType.put(e.getEventType(), events);
        }
    }

    /**
     * Adds the supplied event to the data structure optimized for lookup of events by their identifier.
     *
     * @param e the event
     */
    private void addEventById(DcsEvent e) {
        eventsById.put(e.getId(), e);
    }

    /**
     * Requests another batch of identifiers from the IdService, and resets the ID iterator so that it is positioned
     * at the start of the new batch.
     */
    private void refreshIterator() {
        this.idIterator = idService.create(idBatchSize, Types.EVENT.getTypeName()).iterator();
    }
}
