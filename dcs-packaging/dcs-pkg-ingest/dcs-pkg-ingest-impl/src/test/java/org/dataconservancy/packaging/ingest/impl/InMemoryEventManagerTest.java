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

import junit.framework.Assert;
import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.impl.IdentifierImpl;
import org.dataconservancy.model.dcs.DcsEvent;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Unit tests for the InMemoryEventManager.
 */
public class InMemoryEventManagerTest {

    private DcsEvent event1 = new DcsEvent();
    private DcsEvent event2 = new DcsEvent();
    private DcsEvent event3 = new DcsEvent();

    private String id1;
    private String id2;
    private String id3;
    private String eventType1;
    private String eventType2;

    private int batchSize = 5;

    private InMemoryEventManager imem = new InMemoryEventManager();

    @Before
    public void setup(){
        String date1 = String.valueOf(DateTime.now().minusDays(1));
        eventType1 = "Test Event";
        id1 = "id:1";
        String detail1 = "detail1";
        String outcome1 = "success!";

        String date2 = String.valueOf(DateTime.now());
        eventType2 = "Other Event";
        id2 = "id:2";
        String detail2 = "detail2";
        String outcome2 = "fail!";

        id3 = "id:3";

        event1.setDate(date1);
        event1.setEventType(eventType1);
        event1.setId(id1);
        event1.setDetail(detail1);
        event1.setOutcome(outcome1);

        event2.setDate(date2);
        event2.setEventType(eventType2);
        event2.setId(id2);
        event2.setDetail(detail2);
        event2.setOutcome(outcome2);

        event3 = new DcsEvent(event1);
        event3.setId(id3);

        BulkIdCreationService bids = new BulkIdCreationService() {
            @Override
            public List<Identifier> create(int count, String type) {
                List<Identifier> ids = new ArrayList<Identifier>();

                for(Integer i=1; i<=count; i++){
                    ids.add(new IdentifierImpl("Event", "event:" + i.toString()));
                }
                return ids;
            }
        };
        imem.setIdService(bids);
        imem.setIdBatchSize(batchSize);
    }

    @Test
    public void testAddEvent(){
        Assert.assertTrue(isEventManagerClean());

        imem.addEvent(id1, event1);

        Collection<DcsEvent> events = imem.getEvents(id1, eventType1, eventType2);
        Assert.assertEquals(1,events.size());
        Assert.assertTrue(events.contains(event1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullEventThrowsException(){

        imem.addEvent(id1, null);
    }

    @Test
    public void testAddEvents(){
        Assert.assertTrue(isEventManagerClean());

        Collection<DcsEvent>events = new HashSet<DcsEvent>();
        events.add(event1);
        events.add(event2);
        events.add(event3);

        imem.addEvents(id2, events);

        events = imem.getEvents(id1, eventType1, eventType2);
        Assert.assertEquals(3 ,events.size());
        Assert.assertTrue(events.contains(event2));
        Assert.assertTrue(events.contains(event3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullEventsThrowsException(){

        imem.addEvents(id1, null);
    }

    @Test
    public void testGetEvents(){
        Assert.assertTrue(isEventManagerClean());

        Collection<DcsEvent>events = new HashSet<DcsEvent>();
        events.add(event1);
        events.add(event2);
        events.add(event3);

        imem.addEvents(id1, events);

        events = imem.getEvents(id1, eventType1);
        Assert.assertEquals(2, events.size());
        events = imem.getEvents(id1, eventType2);
        Assert.assertEquals(1, events.size());
        events = imem.getEvents(id1, eventType1, eventType2);
        Assert.assertEquals(3, events.size());
    }

    @Test
    public void testGetEventByType(){
        Assert.assertTrue(isEventManagerClean());

        Collection<DcsEvent>events = new HashSet<DcsEvent>();
        events.add(event1);
        events.add(event2);
        events.add(event3);

        imem.addEvents(id1, events);

        DcsEvent event = imem.getEventByType(id1, eventType1);
        //cannot guarantee order of the iterator in collection
        //make sure the first event is one of the expected ones
        if(!event.equals(event1)){
            Assert.assertEquals(event3, event);
        }

        DcsEvent firstEvent = new DcsEvent(event);

        event = imem.getEventByType(id1, eventType1);
        //and the second one is the other expected event
        if(!firstEvent.equals(event1)){
            Assert.assertEquals(event3, event);
        } else {
            Assert.assertEquals(event1, event);
        }

        event = imem.getEventByType(id1, eventType2);
        Assert.assertEquals(event2, event);
    }

    @Test
    public void testFindEventById(){
        Assert.assertTrue(isEventManagerClean());
        imem.addEvent(id1, event1);
        Collection<DcsEvent> events = imem.getEvents(id1, eventType1, eventType2);
        Assert.assertEquals(1,events.size());
        Assert.assertTrue(events.contains(event1));

        Assert.assertEquals(event1, imem.findEventById(id1));
        Assert.assertNull(imem.findEventById(id2));
        Assert.assertNull(imem.findEventById(id3));
    }

    @Test
    public void testNewEvent(){
        DcsEvent event = imem.newEvent(eventType1);

        Assert.assertEquals(eventType1, event.getEventType());
        Assert.assertNotNull(event.getId());
        Assert.assertTrue(event.getId().length() > 0);
        Assert.assertEquals(event.getId(), "http://dataconservancy.org/event:1");
        Assert.assertNotNull(event.getDate());
    }
    @Test
    public void testBatchSizeGetterAndSetter(){
        Assert.assertEquals(batchSize, imem.getIdBatchSize());
        Assert.assertTrue(batchSize != 10);
        imem.setIdBatchSize(10);
        Assert.assertEquals(10, imem.getIdBatchSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBadBatchSizeFails(){
        imem.setIdBatchSize(-1);
    }

    private boolean isEventManagerClean(){
       Collection<DcsEvent> events = imem.getEvents(id1, eventType1, eventType2);
        return(0 == events.size());
    }
}
