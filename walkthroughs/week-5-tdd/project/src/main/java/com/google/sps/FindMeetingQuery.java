// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long requestDuration = request.getDuration();
    Collection<String> requestAttendees = request.getAttendees();

    // used in the function intersection
    HashMap<String, Boolean> requestAttendeesMap = new HashMap<String, Boolean>();
    Iterator iterator = requestAttendees.iterator(); 
    while(iterator.hasNext()) {
        requestAttendeesMap.put((String) iterator.next(), true);
    }

    // viableTimes contains all of the minutes where meetings can be held
    boolean[] viableTimes = new boolean[TimeRange.END_OF_DAY+1];

    // initially, all of the minutes are viable
    for(int i=0;i<viableTimes.length;i++) {
        viableTimes[i] = true;
    }

    // consider each event independently
    iterator = events.iterator(); 
    while(iterator.hasNext()) {
        Event event = (Event) iterator.next();
        TimeRange when = event.getWhen();
        int eventStart = when.start();
        int eventDuration = when.duration();

        Collection<String> eventAttendees = event.getAttendees();

        // check if any of the attendees in the request are attending the event
        if(intersection(requestAttendeesMap, eventAttendees)) {
            // mark all of the minutes from eventStart to eventStart+eventDuration as false
            for(int j=eventStart;j<eventStart+eventDuration;j++) {
                viableTimes[j] = false;
            }
        }
    }

    // perform a linear search over the viable start times
    // for each potential start time, find the largest contiguous viable time range beginning at that start time
    // if it's a viable meeting time (i.e. it lasts longer than requestDuration), add it to the output
    ArrayList<TimeRange> viableMeetingTimes = new ArrayList<TimeRange>();
    int start = 0;
    int currentRun = 0;
    for(int min=0;min<=TimeRange.END_OF_DAY;min++) {
        if(viableTimes[min]) {
            currentRun++;
        }
        else {
            if(currentRun >= requestDuration) {
                viableMeetingTimes.add(TimeRange.fromStartDuration(start, currentRun));
            }
            start = min+1;
            currentRun = 0;
        }
    }
    if(currentRun >= requestDuration) {
        viableMeetingTimes.add(TimeRange.fromStartDuration(start, currentRun));
    }

    return viableMeetingTimes;
  }

  public static boolean intersection(HashMap<String, Boolean> requestAttendeesMap, Collection<String> eventAttendees) {
    Iterator iterator = eventAttendees.iterator();
    while(iterator.hasNext()) {
        if(requestAttendeesMap.get(iterator.next()) != null)
            return true;
    }
    return false;
  }
}
