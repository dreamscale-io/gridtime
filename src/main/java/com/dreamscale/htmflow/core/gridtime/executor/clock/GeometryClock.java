package com.dreamscale.htmflow.core.gridtime.executor.clock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.*;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class GeometryClock {

    private Coords activeCoords;

    public GeometryClock(LocalDateTime clockTime) {

        LocalDateTime roundedClockTime = roundDownToNearestTwenty(clockTime);
        this.activeCoords = createCoords(ZoomLevel.TWENTY, roundedClockTime);
    }

    public static LocalDateTime roundDownToNearestTwenty(LocalDateTime middleOfNowhereTime) {
        int minutes = middleOfNowhereTime.getMinute();

        int twenties = Math.floorDiv(minutes, 20);

        LocalDateTime roundedTime = middleOfNowhereTime.withMinute(twenties * 20);
        return roundedTime.withSecond(0).withNano(0);
    }

    public Coords next() {
        Coords nextCoords = activeCoords;
        activeCoords = activeCoords.panRight();

        return nextCoords;
    }

    public Coords getActiveCoords() {
        return activeCoords;
    }

    public static Coords createCoords(ZoomLevel zoomLevel, LocalDateTime nextClockTime) {

        int firstMondayOffset = calcFirstMondayOfYear(nextClockTime);

        int weeksIntoYear = calcWeekOfYear(firstMondayOffset, nextClockTime);
        int year = calcAdjustedYear(firstMondayOffset, nextClockTime);

        int weeksIntoBlock = calcWeeksIntoBlock(weeksIntoYear);

        int block = calcBlocksIntoYear(weeksIntoYear);

        int daysIntoWeek = calcWeekdayOffset(nextClockTime);

        int dayPart = calc4HourSteps(nextClockTime);

        int twentyWithinDayPart = calc20MinutesWithinDayPart(nextClockTime);

        return new Coords(zoomLevel, nextClockTime, year, block, weeksIntoBlock, daysIntoWeek, dayPart, twentyWithinDayPart);

    }

    private static int calc4HourSteps(LocalDateTime nextClockTime) {
        return Math.floorDiv(nextClockTime.getHour(), 4) + 1;
    }

    private static int calcBlocksIntoYear(int weeksIntoYear) {
        return Math.floorDiv(weeksIntoYear, ZoomLevel.BLOCK_OF_SIX_WEEKS.getInnerBeats()) + 1;
    }

    private static int calcWeeksIntoBlock(int weeksIntoYear) {

        return Math.floorMod(weeksIntoYear, ZoomLevel.BLOCK_OF_SIX_WEEKS.getInnerBeats());
    }

    private static int calcAdjustedYear(int firstMondayOffset, LocalDateTime clock) {

        int yearNumber = clock.getYear();

        int dayNumber = clock.getDayOfYear();

        if (dayNumber < firstMondayOffset) { //first week of year starts monday
            yearNumber = yearNumber - 1;
        }
        return yearNumber;
    }

    private static int calcFirstMondayOfYear(LocalDateTime clock) {
        LocalDate sameYear = LocalDate.of(clock.getYear(), 1, 1);
        LocalDate firstMondayOfSameYear = sameYear.with(firstInMonth(DayOfWeek.MONDAY));

        return firstMondayOfSameYear.getDayOfYear();
    }

    private static int calcWeekOfYear(int firstMondayOffset, LocalDateTime clock) {
        int weekNumber = 0;

        int dayNumber = clock.getDayOfYear(); //1 to 365 or 366 in leap year

        if (dayNumber < firstMondayOffset) {
            weekNumber = findLastWeekOfPriorYear(clock);
        } else {
            weekNumber = Math.floorDiv((dayNumber - firstMondayOffset),
                    ZoomLevel.WORK_WEEK.getInnerBeats());
        }

        return weekNumber + 1;
    }

    private static int findLastWeekOfPriorYear(LocalDateTime clock) {
        LocalDate startOfPriorYear = LocalDate.of(clock.getYear() - 1, 1, 1);
        LocalDate firstMondayOfPriorYear = startOfPriorYear.with(firstInMonth(DayOfWeek.MONDAY));

        LocalDate endOfPriorYear = LocalDate.of(clock.getYear() - 1, 12, 31);

        return Math.floorDiv((endOfPriorYear.getDayOfYear() - firstMondayOfPriorYear.getDayOfYear()),
                ZoomLevel.WORK_WEEK.getInnerBeats());

    }

    private static int calcWeekdayOffset(LocalDateTime clock) {
        DayOfWeek dayOfWeek = clock.getDayOfWeek();
        return dayOfWeek.getValue();
    }

    private static int calc20MinutesWithinDayPart(LocalDateTime clock) {
        int hoursInto4Hour = Math.floorMod(clock.getHour(), 4);
        int completedHours = hoursInto4Hour * 3;
        int current20Mins = Math.floorDiv( clock.getMinute() , 20);

        return completedHours + current20Mins + 1;
    }

    public LocalDateTime getNextTickTime() {
        return activeCoords.clockTime.plusMinutes(20);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Coords {

        final LocalDateTime clockTime;
        final GridTime gridTime;

        public Coords(ZoomLevel zoomLevel, LocalDateTime clockTime, Integer ... gridCoords) {
             this.clockTime = clockTime;
             this.gridTime = new GridTime(zoomLevel, gridCoords);
        }

        public boolean equals(Coords o) {
            return gridTime.equals(o.gridTime);
        }

        public Duration getRelativeTime(LocalDateTime moment) {
            return Duration.between(clockTime, moment);
        }


        public Coords panLeft() {

                switch (gridTime.getZoomLevel()) {
                    case TWENTY:
                        return minus20Minutes();
                    case DAY_PART:
                        return minus4Hour();
                    case DAY:
                        return minusDay();
                    case WORK_WEEK:
                        return minusWeek();
                    case BLOCK_OF_SIX_WEEKS:
                        return minusBlock();
                    case YEAR:
                        return minusYear();
                }
                return this;
        }

        public Coords panRight() {
            switch (gridTime.getZoomLevel()) {
                case TWENTY:
                    return plus20Minutes();
                case DAY_PART:
                    return plus4Hour();
                case DAY:
                    return plusDay();
                case WORK_WEEK:
                    return plusWeek();
                case BLOCK_OF_SIX_WEEKS:
                    return plusBlock();
                case YEAR:
                    return plusYear();
            }
            return this;
        }

        //pan left functions

        public Coords minus20Minutes() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.minusMinutes(20));
        }

        public Coords minus4Hour() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.minusHours(4));
        }

        public Coords minusDay() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.minusDays(1));
        }

        public Coords minusWeek() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.minusWeeks(1));
        }

        public Coords minusBlock() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.minusWeeks(6));
        }

        public Coords minusYear() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.minusYears(1));
        }

        // pan right functions

        public Coords plus20Minutes() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.plusMinutes(20));
        }

        public Coords plus4Hour() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.plusHours(4));
        }

        public Coords plusDay() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.plusDays(1));
        }

        public Coords plusWeek() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.plusWeeks(1));
        }

        public Coords plusBlock() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.plusWeeks(6));
        }

        public Coords plusYear() {
            return GeometryClock.createCoords(gridTime.getZoomLevel(), clockTime.plusYears(1));
        }


        public String getFormattedGridTime() {
            return gridTime.getFormattedGridTime();
        }

        public ZoomLevel getZoomLevel() {
            return gridTime.getZoomLevel();
        }
    }

}
