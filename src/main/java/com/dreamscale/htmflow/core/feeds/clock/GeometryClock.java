package com.dreamscale.htmflow.core.feeds.clock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class GeometryClock {

    private LocalDateTime clockTime;

    private Coords coords;


    public GeometryClock(LocalDateTime clockTime) {
        this.clockTime = clockTime;
        this.coords = createGeometryCoords(clockTime);
    }

    public Coords tick() {
        int minutesToTick = ZoomLevel.MIN.buckets();
        LocalDateTime nextClockTime = this.clockTime.plusMinutes(minutesToTick);

        this.coords = createGeometryCoords(nextClockTime);
        this.clockTime = nextClockTime;

        return this.coords;
    }

    public Coords getCoordinates() {
        return coords;
    }

    private static Coords createGeometryCoords(LocalDateTime nextClockTime) {

        int minuteBucketsIntoHour = calcMinuteBuckets(nextClockTime);
        int hoursIntoDay = nextClockTime.getHour();

        int daysIntoWeek = calcWeekdayOffset(nextClockTime);

        int firstMondayOffset = calcFirstMondayOfYear(nextClockTime);

        int weeksIntoYear = calcWeekOfYear(firstMondayOffset, nextClockTime);
        int currentYear = calcAdjustedYear(firstMondayOffset, nextClockTime);

        int weeksIntoBlock = calcWeeksIntoBlock(weeksIntoYear);

        int blocksIntoYear = calcBlocksIntoYear(weeksIntoYear);

        return new Coords(nextClockTime,
                minuteBucketsIntoHour,
                hoursIntoDay,
                daysIntoWeek,
                weeksIntoYear,
                weeksIntoBlock,
                blocksIntoYear,
                currentYear);
    }

    private static int calcBlocksIntoYear(int weeksIntoYear) {
        return Math.floorDiv(weeksIntoYear, ZoomLevel.BLOCK.buckets()) + 1;
    }

    private static int calcWeeksIntoBlock(int weeksIntoYear) {

        return Math.floorMod(weeksIntoYear, ZoomLevel.BLOCK.buckets());
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
                    ZoomLevel.WEEK.buckets());
        }

        return weekNumber + 1;
    }

    private static int findLastWeekOfPriorYear(LocalDateTime clock) {
        LocalDate startOfPriorYear = LocalDate.of(clock.getYear() - 1, 1, 1);
        LocalDate firstMondayOfPriorYear = startOfPriorYear.with(firstInMonth(DayOfWeek.MONDAY));

        LocalDate endOfPriorYear = LocalDate.of(clock.getYear() - 1, 12, 31);

        return Math.floorDiv((endOfPriorYear.getDayOfYear() - firstMondayOfPriorYear.getDayOfYear()),
                ZoomLevel.WEEK.buckets());

    }

    private static int calcWeekdayOffset(LocalDateTime clock) {
        DayOfWeek dayOfWeek = clock.getDayOfWeek();
        return dayOfWeek.getValue();
    }

    private static int calcMinuteBuckets(LocalDateTime clock) {
        return Math.floorDiv( clock.getMinute() , ZoomLevel.MIN.buckets() ) + 1;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Coords {

        final LocalDateTime clockTime;
        final int minuteBucketsIntoHour;
        final int hoursIntoDay;
        final int daysIntoWeek;
        final int weeksIntoYear;
        final int weeksIntoBlock;
        final int blocksIntoYear;
        final int currentYear;

        public String formatCoords() {
            return currentYear + "-" + blocksIntoYear + "-" + weeksIntoBlock + "-" + daysIntoWeek + "-" + hoursIntoDay + "-" + minuteBucketsIntoHour;
        }

        public Coords panLeft(ZoomLevel zoomLevel) {

                switch (zoomLevel) {
                    case MIN:
                        return minusMinutes();
                    case HOUR:
                        return minusHour();
                    case DAY:
                        return minusDay();
                    case WEEK:
                        return minusWeek();
                    case BLOCK:
                        return minusBlock();
                    case YEAR:
                        return minusYear();
                }
                return this;
        }

        public Coords panRight(ZoomLevel zoomLevel) {

            switch (zoomLevel) {
                case MIN:
                    return plusMinutes();
                case HOUR:
                    return plusHour();
                case DAY:
                    return plusDay();
                case WEEK:
                    return plusWeek();
                case BLOCK:
                    return plusBlock();
                case YEAR:
                    return plusYear();
            }
            return this;
        }

        //pan left functions

        public Coords minusMinutes() {
            return GeometryClock.createGeometryCoords(clockTime.minusMinutes(ZoomLevel.MIN.buckets()));
        }

        public Coords minusHour() {
            return GeometryClock.createGeometryCoords(clockTime.minusHours(1));
        }

        public Coords minusDay() {
            return GeometryClock.createGeometryCoords(clockTime.minusDays(1));
        }

        public Coords minusWeek() {
            return GeometryClock.createGeometryCoords(clockTime.minusWeeks(1));
        }

        public Coords minusBlock() {
            return GeometryClock.createGeometryCoords(clockTime.minusWeeks(ZoomLevel.BLOCK.buckets()));
        }

        public Coords minusYear() {
            return GeometryClock.createGeometryCoords(clockTime.minusYears(1));
        }

        // pan right functions

        public Coords plusMinutes() {
            return GeometryClock.createGeometryCoords(clockTime.plusMinutes(ZoomLevel.MIN.buckets()));
        }

        public Coords plusHour() {
            return GeometryClock.createGeometryCoords(clockTime.plusHours(1));
        }

        public Coords plusDay() {
            return GeometryClock.createGeometryCoords(clockTime.plusDays(1));
        }

        public Coords plusWeek() {
            return GeometryClock.createGeometryCoords(clockTime.plusWeeks(1));
        }

        public Coords plusBlock() {
            return GeometryClock.createGeometryCoords(clockTime.plusWeeks(ZoomLevel.BLOCK.buckets()));
        }

        public Coords plusYear() {
            return GeometryClock.createGeometryCoords(clockTime.plusYears(1));
        }


    }

}
