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
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public final class FindMeetingQuery {

  /* runs in O(n*m+p), where n is the number of events, m is the maximum number of attendees for any given event, and p is
   * the number of attendees in the request
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // find all the viable meeting times for the mandatory attendees
    Collection<TimeRange> mandatoryViableMeetingTimes = getViableMeetingTimes(events, duration, attendees);
    // find all the viable meeting times for the optional attendees
    Collection<TimeRange> optionalViableMeetingTimes = getViableMeetingTimes(events, duration, optionalAttendees);
    // see if there are any overlapping meeting times between what the mandatory and optional attendees can attend
    Collection<TimeRange> intersectionMeetingTimes = intersectionMeetingTimes(duration, mandatoryViableMeetingTimes, optionalViableMeetingTimes);

    // if there is no meeting time where all of the mandatory and optional attendees can attend, return only the times when the mandatory employees are available
    if(intersectionMeetingTimes.size() == 0) {
        // case where there are only optional attendees, and there is not a time where all of the optional attendees can attend
        if(attendees.size() == 0) {
            return Arrays.asList();
        }
        return mandatoryViableMeetingTimes;
    }
    else {
        return intersectionMeetingTimes;
    }
  }

  private static Collection<TimeRange> getViableMeetingTimes(Collection<Event> events, long duration, Collection<String> attendees) {
    // used in the function intersection
    HashMap<String, Boolean> attendeesMap = new HashMap<String, Boolean>();
    Iterator iterator = attendees.iterator(); 
    while(iterator.hasNext()) {
        attendeesMap.put((String) iterator.next(), true);
    }

    // all of the minutes when meetings can be held will be marked true
    boolean[] viableTimes = new boolean[TimeRange.END_OF_DAY+1];

    // initially, all of the minutes are viable
    for(int i=0;i<viableTimes.length;i++) {
        viableTimes[i] = true;
    }

    // consider each event independently
    iterator = events.iterator(); 
    while(iterator.hasNext()) {
        Event event = (Event) iterator.next();
        Collection<String> eventAttendees = event.getAttendees();
        TimeRange when = event.getWhen();
        int eventStart = when.start();
        int eventEnd = when.end();

        // check if any of the attendees are attending the event
        if(attending(attendeesMap, eventAttendees)) {
            // mark all of the minutes from eventStart to eventEnd as false
            for(int i=eventStart;i<eventEnd;i++) {
                viableTimes[i] = false;
            }
        }
    }
    
    return linearSearch(viableTimes, duration);
  }

  private static Collection<TimeRange> intersectionMeetingTimes(long duration, Collection<TimeRange> mandatoryViableMeetingTimes, Collection<TimeRange> optionalViableMeetingTimes) {
    // all of the minutes when mandatory meetings can be held will be marked true
    boolean[] mandatoryTimes = new boolean[TimeRange.END_OF_DAY+1];
    Iterator iterator = mandatoryViableMeetingTimes.iterator(); 
    while(iterator.hasNext()) {
        TimeRange timeRange = (TimeRange) iterator.next();
        int start = timeRange.start();
        int end = timeRange.end();
        
        for(int i=start;i<end;i++) {
            mandatoryTimes[i] = true;
        }
    }
    
    // all of the minutes when mandatory and optional meetings can be held will be marked true
    boolean[] intersectionTimes = new boolean[TimeRange.END_OF_DAY+1];
    iterator = optionalViableMeetingTimes.iterator(); 
    while(iterator.hasNext()) {
        TimeRange timeRange = (TimeRange) iterator.next();
        int start = timeRange.start();
        int end = timeRange.end();
        
        for(int i=start;i<end;i++) {
            if(mandatoryTimes[i]) {
                intersectionTimes[i] = true;
            }
        }
    }

    return linearSearch(intersectionTimes, duration);
  }

  private static boolean attending(HashMap<String, Boolean> attendeesMap, Collection<String> eventAttendees) {
    Iterator iterator = eventAttendees.iterator();
    while(iterator.hasNext()) {
        if(attendeesMap.get(iterator.next()) != null)
            return true;
    }
    return false;
  }

  /* Performs a linear search over the viable start times.
   * For each potential start time, finds the largest contiguous viable time range beginning at that start time.
   * If it's a viable meeting time (i.e. it lasts longer than the required duration), adds it to the output.
  */
  private static Collection<TimeRange> linearSearch(boolean[] viableTimes, long duration) {
    ArrayList<TimeRange> viableMeetingTimes = new ArrayList<TimeRange>();
    int start = 0;
    int currentRun = 0;
    for(int min=0;min<=TimeRange.END_OF_DAY;min++) {
        if(viableTimes[min]) {
            currentRun++;
        }
        else {
            if(currentRun >= duration) {
                viableMeetingTimes.add(TimeRange.fromStartDuration(start, currentRun));
            }
            start = min+1;
            currentRun = 0;
        }
    }
    if(currentRun >= duration) {
        viableMeetingTimes.add(TimeRange.fromStartDuration(start, currentRun));
    }

    return viableMeetingTimes;
  }
}
