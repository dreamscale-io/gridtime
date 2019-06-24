package com.dreamscale.htmflow.core.gridtime.machine.clock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.*;
import java.util.Arrays;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class GeometryClock {

    private GridTime activeGridTime;

    public GeometryClock(LocalDateTime clockTime) {

        LocalDateTime roundedClockTime = roundDownToNearestTwenty(clockTime);
        this.activeGridTime = createGridTime(ZoomLevel.TWENTY, roundedClockTime);
    }

    public static LocalDateTime roundDownToNearestTwenty(LocalDateTime middleOfNowhereTime) {
        int minutes = middleOfNowhereTime.getMinute();

        int twenties = Math.floorDiv(minutes, 20);

        LocalDateTime roundedTime = middleOfNowhereTime.withMinute(twenties * 20);
        return roundedTime.withSecond(0).withNano(0);
    }

    public static LocalDateTime getFirstMomentOfYear(int year) {
        LocalDate sameYear = LocalDate.of(year, 1, 1);
        LocalDate firstMondayOfSameYear = sameYear.with(firstInMonth(DayOfWeek.MONDAY));
        return firstMondayOfSameYear.atStartOfDay();
    }

    public GridTime next() {
        activeGridTime = activeGridTime.panRight();
        return activeGridTime;
    }

    public GridTime getActiveGridTime() {
        return activeGridTime;
    }

    public static Sequence createSequencedGridTime(ZoomLevel zoomLevel, LocalDateTime clockTime, long sequenceNumber) {
        GridTime gridTime = createGridTime(zoomLevel, clockTime);
        return new Sequence(sequenceNumber, gridTime);
    }

    public static GridTime createGridTime(ZoomLevel zoomLevel, LocalDateTime clockTime) {

        int firstMondayOffset = calcFirstMondayOfYear(clockTime);

        int weeksIntoYear = calcWeekOfYear(firstMondayOffset, clockTime);
        int year = calcAdjustedYear(firstMondayOffset, clockTime);

        int weeksIntoBlock = calcWeeksIntoBlock(weeksIntoYear);

        int block = calcBlocksIntoYear(weeksIntoYear);

        int daysIntoWeek = calcWeekdayOffset(clockTime);

        int dayPart = calc4HourSteps(clockTime);

        int twentyWithinDayPart = calc20MinutesWithinDayPart(clockTime);

        return new GridTime(zoomLevel, clockTime, year, block, weeksIntoBlock, daysIntoWeek, dayPart, twentyWithinDayPart);
    }

    private static GridTime createGridTimeFromCoordinates(ZoomLevel zoomLevel, Integer[] coords) {
        LocalDateTime moment = getFirstMomentOfYear(coords[0]);

        if (coords.length >= 2) {
            moment = moment.plus(ZoomLevel.BLOCK.getDuration().multipliedBy(coords[1] - 1));
        }
        if (coords.length >= 3) {
            moment = moment.plus(ZoomLevel.WEEK.getDuration().multipliedBy(coords[2] - 1));
        }
        if (coords.length >= 4) {
            moment = moment.plus(ZoomLevel.DAY.getDuration().multipliedBy(coords[3] - 1));
        }
        if (coords.length >= 5) {
            moment = moment.plus(ZoomLevel.DAY_PART.getDuration().multipliedBy(coords[4] - 1));
        }
        if (coords.length >= 6) {
            moment = moment.plus(ZoomLevel.TWENTY.getDuration().multipliedBy(coords[5] - 1));
        }

        return new GridTime(zoomLevel, moment, coords);
    }


    private static int calc4HourSteps(LocalDateTime nextClockTime) {
        return Math.floorDiv(nextClockTime.getHour(), 4) + 1;
    }

    private static int calcBlocksIntoYear(int weeksIntoYear) {
        return Math.floorDiv(weeksIntoYear, ZoomLevel.BLOCK.getInnerBeats()) + 1;
    }

    private static int calcWeeksIntoBlock(int weeksIntoYear) {

        return Math.floorMod(weeksIntoYear, ZoomLevel.BLOCK.getInnerBeats());
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
                    ZoomLevel.WEEK.getInnerBeats());
        }

        return weekNumber + 1;
    }

    private static int findLastWeekOfPriorYear(LocalDateTime clock) {
        LocalDate startOfPriorYear = LocalDate.of(clock.getYear() - 1, 1, 1);
        LocalDate firstMondayOfPriorYear = startOfPriorYear.with(firstInMonth(DayOfWeek.MONDAY));

        LocalDate endOfPriorYear = LocalDate.of(clock.getYear() - 1, 12, 31);

        return Math.floorDiv((endOfPriorYear.getDayOfYear() - firstMondayOfPriorYear.getDayOfYear()),
                ZoomLevel.WEEK.getInnerBeats());

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
        return activeGridTime.clockTime.plusMinutes(20);
    }



    @AllArgsConstructor
    @Getter
    public static class Sequence {
        long sequenceNumber;
        GridTime gridTime;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class GridTime {

        private final LocalDateTime clockTime;
        private final ZoomLevel zoomLevel;
        private final Integer[] coords;

        private final String formattedGridTime;


        public GridTime(ZoomLevel zoomLevel, LocalDateTime clockTime, Integer ... fullCoordinates) {
            this.zoomLevel = zoomLevel;
             this.clockTime = clockTime;
             this.coords = truncateCoordinates(zoomLevel, fullCoordinates);
             this.formattedGridTime = GridTimeFormatter.formatGridTime(coords);
        }

        private static Integer[] truncateCoordinates(ZoomLevel zoomLevel, Integer[] fullCoordinates) {
            switch (zoomLevel) {
                case TWENTY:
                    return fullCoordinates;
                case DAY_PART:
                    return Arrays.copyOf(fullCoordinates, 5);
                case DAY:
                    return Arrays.copyOf(fullCoordinates, 4);
                case WEEK:
                    return Arrays.copyOf(fullCoordinates, 3);
                case BLOCK:
                    return Arrays.copyOf(fullCoordinates, 2);
                case YEAR:
                    return Arrays.copyOf(fullCoordinates, 1);
            }

            return new Integer[0];
        }

        public boolean equals(GridTime o) {
            return formattedGridTime.equals(o.formattedGridTime);
        }

        public Duration getRelativeTime(LocalDateTime moment) {
            return Duration.between(clockTime, moment);
        }

        public GridTime toZoomLevel(ZoomLevel newZoomLevel) {
            Integer[] newCoords = truncateCoordinates(newZoomLevel, coords);
            return GeometryClock.createGridTimeFromCoordinates(newZoomLevel, newCoords);
        }

        public GridTime zoomOut() {
            return GeometryClock.createGridTimeFromCoordinates(zoomLevel.zoomOut(), Arrays.copyOf(coords, coords.length - 1));
        }

        public GridTime zoomIn() {
            return GeometryClock.createGridTime(zoomLevel.zoomIn(), clockTime);
        }

        public GridTime panLeft() {

                switch (zoomLevel) {
                    case TWENTY:
                        return minus20Minutes();
                    case DAY_PART:
                        return minus4Hour();
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

        public GridTime panRight() {
            switch (zoomLevel) {
                case TWENTY:
                    return plus20Minutes();
                case DAY_PART:
                    return plus4Hour();
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

        private GridTime minus20Minutes() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.minusMinutes(20));
        }

        private GridTime minus4Hour() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.minusHours(4));
        }

        private GridTime minusDay() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.minusDays(1));
        }

        private GridTime minusWeek() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.minusWeeks(1));
        }

        private GridTime minusBlock() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.minusWeeks(6));
        }

        private GridTime minusYear() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.minusYears(1));
        }

        // pan right functions

        private GridTime plus20Minutes() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.plusMinutes(20));
        }

        private GridTime plus4Hour() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.plusHours(4));
        }

        private GridTime plusDay() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.plusDays(1));
        }

        private GridTime plusWeek() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.plusWeeks(1));
        }

        private GridTime plusBlock() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.plusWeeks(6));
        }

        private GridTime plusYear() {
            return GeometryClock.createGridTime(zoomLevel, clockTime.plusYears(1));
        }

        public Integer getYear() {
            return GridTimeFormatter.getYear(coords);
        }

        public Integer getBlock() {
            return GridTimeFormatter.getBlock(coords);
        }

        public Integer getBlockWeek() {
            return GridTimeFormatter.getBlockWeek(coords);
        }

        public Integer getDay() {
            return GridTimeFormatter.getDay(coords);
        }

        public Integer getDayPart() {
            return GridTimeFormatter.getDayPart(coords);
        }

        public Integer getTwentyOfTwelve() {
            return GridTimeFormatter.getTwentyOfTwelve(coords);
        }


    }
}
