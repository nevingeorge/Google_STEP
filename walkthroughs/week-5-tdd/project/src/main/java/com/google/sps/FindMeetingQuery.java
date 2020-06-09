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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {

  /* 
   * This function simulates the "find a meeting" feature on Google calendar.
   * A meeting request contains a 1) name, 2) duration in minutes, and 3) collection of attendees.
   * Each event in the collection of events contains a 1) name, 2) time range, and 3) collection of attendees.
   * Given a meeting request and a list of all known events, the function returns a collection of all the possible time ranges when the meeting can take place.
   * These returned meeting times must last at least as long as the specified duration, and every mandatory attendee must have no other conflicts during 
   * that time range (i.e. there cannot exist an event that a mandatory attendee is attending at the same time of the meeting).
   * 
   * The function also provides support for optional attendees.
   * - If one or more time slots exists so that both mandatory and optional attendees can attend, the function returns those time slots.
   * - Otherwise, the function returns the time slots that fit just the mandatory attendees.
   *
   * The algorithm runs in O(n*m+p), where n is the number of events, m is the maximum number of attendees for any given event, and p is
   * the number of attendees in the request.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // find all the viable meeting times for the mandatory attendees
    Collection<TimeRange> mandatoryViableMeetingTimes = getViableMeetingTimes(events, duration, mandatoryAttendees);
    // find all the viable meeting times for the optional attendees
    Collection<TimeRange> optionalViableMeetingTimes = getViableMeetingTimes(events, duration, optionalAttendees);
    // see if there are any overlapping meeting times between what the mandatory and optional attendees can attend
    Collection<TimeRange> intersectionMeetingTimes = intersectionMeetingTimes(duration, mandatoryViableMeetingTimes, optionalViableMeetingTimes);

    // if there is no meeting time where all of the mandatory and optional attendees can attend, return only the times when the mandatory employees are available
    if(intersectionMeetingTimes.size() == 0) {
        // case where there are only optional attendees, and there is not a time where all of the optional attendees can attend
        if(mandatoryAttendees.size() == 0) {
            return Arrays.asList();
        }

        // optional coding challenge
        // finds the time slot(s) that allow mandatory attendees and the greatest possible number of optional attendees to attend
        return optimize(mandatoryViableMeetingTimes, events, duration, optionalAttendees);
    }
    else {
        return intersectionMeetingTimes;
    }
  }
  
  /*
   * The function returns a list of all the meeting times when all the attendees can attend.
   * It uses a boolean array containing values for every minute of the day, where a minute is marked as false if an attendee has an event during that time.
   * It then runs a linear search on the boolean array to determine if there are any contiguous time ranges longer than the specified duration.
   */
  private static Collection<TimeRange> getViableMeetingTimes(Collection<Event> events, long duration, Collection<String> attendees) {
    // used in the function attending
    Set<String> attendeesSet = new HashSet<String>();
    for(String attendee: attendees) {
        attendeesSet.add(attendee);
    }

    // all of the minutes when meetings can be held will be marked true
    boolean[] viableTimes = new boolean[TimeRange.END_OF_DAY+1];
    for(int i=0;i<viableTimes.length;i++) {
        viableTimes[i] = true;
    }

    for(Event event: events) {
        Collection<String> eventAttendees = event.getAttendees();
        TimeRange when = event.getWhen();
        int eventStart = when.start();
        int eventEnd = when.end();

        // check if any of the attendees are attending the event
        if(attending(attendeesSet, eventAttendees)) {
            // mark all of the minutes from eventStart to eventEnd as false
            for(int i=eventStart;i<eventEnd;i++) {
                viableTimes[i] = false;
            }
        }
    }
    
    // runs a linear search on the boolean array to determine if there are any contiguous time ranges longer than the specified duration
    return getViableTimeRanges(viableTimes, duration);
  }

  /*
   * The function returns a list of all the time ranges that are common to both mandatoryViableMeetingTimes and optionalViableMeetingTimes.
   * It uses a boolean array containing values for every minute of the day, where a minute is marked as true if it is in a time range 
   * found in both mandatoryViableMeetingTimes and optionalViableMeetingTimes.
   * It then runs a linear search on the boolean array to determine if there are any contiguous time ranges longer than the specified duration.
   */
  private static Collection<TimeRange> intersectionMeetingTimes(long duration, Collection<TimeRange> mandatoryViableMeetingTimes, Collection<TimeRange> optionalViableMeetingTimes) {
    // all of the minutes when mandatory meetings can be held will be marked true
    boolean[] mandatoryTimes = new boolean[TimeRange.END_OF_DAY+1];
    for(TimeRange timeRange: mandatoryViableMeetingTimes) {
        int start = timeRange.start();
        int end = timeRange.end();
        
        for(int i=start;i<end;i++) {
            mandatoryTimes[i] = true;
        }
    }
    
    // all of the minutes when mandatory and optional meetings can be held will be marked true
    boolean[] intersectionTimes = new boolean[TimeRange.END_OF_DAY+1];
    for(TimeRange timeRange: optionalViableMeetingTimes) {
        int start = timeRange.start();
        int end = timeRange.end();
        
        for(int i=start;i<end;i++) {
            if(mandatoryTimes[i]) {
                intersectionTimes[i] = true;
            }
        }
    }

    // runs a linear search on the boolean array to determine if there are any contiguous time ranges longer than the specified duration
    return getViableTimeRanges(intersectionTimes, duration);
  }

  // returns true if any of the attendees in attendeesSet are in eventAttendees
  private static boolean attending(Set<String> attendeesSet, Collection<String> eventAttendees) {
    for(String eventAttendee: eventAttendees) {
        if(attendeesSet.contains(eventAttendee)) {
            return true;
        }
    }
    return false;
  }

  /* The function runs a linear search over the viable start times.
   * For each potential start time, it finds the largest contiguous viable time range beginning at that start time.
   * If it's a viable time range (lasts longer than the required duration), the function adds it to the output.
  */
  private static Collection<TimeRange> getViableTimeRanges(boolean[] viableTimes, long duration) {
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

/*
 * This is the optional coding challenge.
 * The function is called in the case where there is no time range when all of the mandatory and optional attendees can attend.
 * It finds the time slot(s) that allow mandatory attendees and the greatest possible number of optional attendees to attend.
 */
private static Collection<TimeRange> optimize() {

}