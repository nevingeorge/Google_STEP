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
import java.util.HashMap;
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
    * The algorithm up to the optional coding challenge runs in O(n*m+p), where n is the number of events, m is the maximum number of attendees for any given event, 
    * and p is the number of attendees in the request.
    */
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        long duration = request.getDuration();
        Collection<String> mandatoryAttendees = request.getAttendees();
        Collection<String> optionalAttendees = request.getOptionalAttendees();

        // find all the viable meeting times for the mandatory attendees
        Collection<TimeRange> mandatoryViableMeetingTimes = getViableMeetingTimes(events, duration, mandatoryAttendees);

        // optional coding challenge
        // find the times within mandatoryViableMeetingTimes when the greatest number of optional attendees can attend
        return optionalAttendees(mandatoryViableMeetingTimes, events, duration, optionalAttendees);
    }
    
    // returns a list the meeting times when all the attendees can attend.
    private static Collection<TimeRange> getViableMeetingTimes(Collection<Event> events, long duration, Collection<String> attendees) {
        // used in the function attending
        Set<String> attendeesSet = new HashSet<String>();
        for(String attendee : attendees) {
            attendeesSet.add(attendee);
        }

        // all of the minutes when meetings can be held will be marked true
        boolean[] viableTimes = new boolean[TimeRange.END_OF_DAY+1];
        for(int i=0;i<viableTimes.length;i++) {
            viableTimes[i] = true;
        }

        for(Event event : events) {
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
        
        /* 
         * Run a linear search over the viable start times.
         * For each potential start time, find the largest contiguous viable time range beginning at that start time.
         * If the time range lasts longer than the required duration, add it to the output.
         */
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

    // returns true if any of the attendees in attendeesSet are in eventAttendees
    private static boolean attending(Collection<String> attendeesSet, Collection<String> eventAttendees) {
        for(String eventAttendee : eventAttendees) {
            if(attendeesSet.contains(eventAttendee)) {
                return true;
            }
        }
        return false;
    }

    // finds the time slot(s) that allow all the mandatory attendees and the greatest possible number of optional attendees to attend
    private static Collection<TimeRange> optionalAttendees(Collection<TimeRange> mandatoryViableMeetingTimes, Collection<Event> events, long duration, Collection<String> optionalAttendees) {
        // for every timeRange in mandatoryViableMeetingTimes, eventOverlap contains a corresponding arraylist of all the events that optional attendees are attending and that overlap with the meeting time
        HashMap<TimeRange, ArrayList<Event>> eventOverlap = new HashMap<TimeRange, ArrayList<Event>>();
        for(TimeRange timeRange : mandatoryViableMeetingTimes) {
            eventOverlap.put(timeRange, new ArrayList<Event>());
        }

        for(Event event : events) {
            // check if any of the optional attendees are attending the event
            if(attending(optionalAttendees, event.getAttendees())) {
                TimeRange eventTimeRange = event.getWhen();

                // if the event overlaps with any of the time ranges in mandatoryViableMeetingTimes, add the event to the meeting time's arraylist in eventOverlap
                for(TimeRange meetingTimeRange : mandatoryViableMeetingTimes) {
                    if(eventTimeRange.overlaps(meetingTimeRange)) {
                        eventOverlap.get(meetingTimeRange).add(event);
                    }
                }
            }
        }

        ArrayList<TimeRange> maxOptionalViableMeetingTimes = new ArrayList<TimeRange>();
        int maxOptionalAttendees = 0;
        for(TimeRange timeRange : mandatoryViableMeetingTimes) {
            // get the maximum number of optional attendees that are available during timeRange
            // maxAttendanceTimeRanges will contain all the subsets of timeRange when the maximum number of optional attendees can attend
            ArrayList<TimeRange> maxAttendanceTimeRanges = new ArrayList<TimeRange>();
            int maxAttendance = getMaxAttendance(timeRange, eventOverlap.get(timeRange), maxAttendanceTimeRanges, optionalAttendees, (int) duration);
            if(maxAttendance > maxOptionalAttendees) {
                maxOptionalViableMeetingTimes = maxAttendanceTimeRanges;
                maxOptionalAttendees = maxAttendance;
            }
            else if(maxAttendance == maxOptionalAttendees) {
                maxOptionalViableMeetingTimes.addAll(maxAttendanceTimeRanges);
            }
        }

        // no times when any optional attendees can attend
        if(maxOptionalAttendees == 0) {
            return mandatoryViableMeetingTimes;
        }
        else {
            return maxOptionalViableMeetingTimes;
        }
    }

    // returns the maximum number of optional attendees that are free during a subset of timeRange longer than the required duration.
    // maxAttendanceTimeRanges is updated with all the subsets of timeRange where the maximal number of optional attendees can attend.
    @SuppressWarnings("unchecked")
    private static int getMaxAttendance(TimeRange timeRange, ArrayList<Event> overlap, ArrayList<TimeRange> maxAttendanceTimeRanges, Collection<String> optionalAttendees, int meetingDuration) {
        int start = timeRange.start();
        int end = timeRange.end();
        int duration = timeRange.duration();
        Set<String>[] attendeesEveryMinute = new HashSet[duration];
        for(int i=0;i<duration;i++) {
            attendeesEveryMinute[i] = new HashSet<String>();
        }

        for(Event event : overlap) {
            Set<String> attendees = event.getAttendees();

            TimeRange eventTimeRange = event.getWhen();
            int eventStart = eventTimeRange.start();
            int eventEnd = eventTimeRange.end();
            int startInInterval = Math.max(start, eventStart);
            int endInInterval = Math.min(end, eventEnd);

            // for every minute within eventTimeRange (that's also within timeRange), add to its corresponding HashSet the optional attendees that are attending the event
            for(int min=startInInterval; min<endInInterval; min++) {
                for(String attendee : attendees) {
                    if(optionalAttendees.contains(attendee))
                        attendeesEveryMinute[min-start].add(attendee);
                }
            }
        }
        
        // find the maximum number of optional attendees that can attend, beginning with the case where no one is unavailable
        for(int numUnavailable=0; numUnavailable<optionalAttendees.size(); numUnavailable++) {
            // test every potential start time
            for(int startMin=start; startMin<=end-meetingDuration; startMin++) {
                Set<String> unavailable = new HashSet<String>();

                // see if you can have a meeting of the smallest possible duration beginning at startMin
                for(int min=startMin; min<startMin+meetingDuration; min++) {
                    for(String attendee : attendeesEveryMinute[min-start]) {
                        unavailable.add(attendee);
                    }
                }

                // a meeting of the smallest possible duration is possible
                if(unavailable.size() <= numUnavailable) {
                    int checkNextMin;

                    for(checkNextMin=startMin+meetingDuration; checkNextMin<end; checkNextMin++) {
                        // check if you can enlarge the duration of the meeting
                        if(reachedLimit(attendeesEveryMinute[checkNextMin-start], unavailable, numUnavailable)) {
                            break;
                        }
                    }

                    // add to maxAttendanceTimeRanges the time range of the meeting of the largest possible duration beginning at startMin
                    maxAttendanceTimeRanges.add(TimeRange.fromStartEnd(startMin, checkNextMin, false));
                    startMin = checkNextMin-1;
                }
            }
            
            // found a time range when only numUnavailable people are unavailable
            if(maxAttendanceTimeRanges.size() > 0) {
                return optionalAttendees.size()-numUnavailable;
            }
        }

        // there is no time range of the necessary duration that any attendee can attend
        return 0;
    }
    
    // checks if you can add the people in attendees to unavailable without exceeding the maximum size
    private static boolean reachedLimit(Set<String> attendees, Set<String> unavailable, int numUnavailable) {
        for(String attendee : attendees) {
            unavailable.add(attendee);
            if(unavailable.size() > numUnavailable) {
                return true;
            }
        }
        return false;
    }
}