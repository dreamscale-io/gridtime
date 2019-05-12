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
        this.coords = createStoryCoords(clockTime);
    }

    public Coords tick() {
        int minutesToTick = ZoomLevel.TWENTY_MINS.buckets();
        LocalDateTime nextClockTime = this.clockTime.plusMinutes(minutesToTick);

        this.coords = createStoryCoords(nextClockTime);
        this.clockTime = nextClockTime;

        return this.coords;
    }

    public LocalDateTime getNextTickTime() {
        int minutesToTick = ZoomLevel.TWENTY_MINS.buckets();
        return this.clockTime.plusMinutes(minutesToTick);
    }

    public Coords getCoordinates() {
        return coords;
    }

    private static Coords createStoryCoords(LocalDateTime nextClockTime) {

        int fours = calc4HourSteps(nextClockTime);
        int twenties = calc20MinuteSteps(nextClockTime);

        int daysIntoWeek = calcWeekdayOffset(nextClockTime);

        int firstMondayOffset = calcFirstMondayOfYear(nextClockTime);

        int weeksIntoYear = calcWeekOfYear(firstMondayOffset, nextClockTime);
        int year = calcAdjustedYear(firstMondayOffset, nextClockTime);

        int weeksIntoBlock = calcWeeksIntoBlock(weeksIntoYear);

        int block = calcBlocksIntoYear(weeksIntoYear);

        return new Coords(nextClockTime,
                year,
                block,
                weeksIntoBlock,
                weeksIntoYear,
                daysIntoWeek,
                fours,
                twenties
                );
    }

    private static int calc4HourSteps(LocalDateTime nextClockTime) {
        return Math.floorDiv(nextClockTime.getHour(), 4) + 1;
    }

    private static int calcBlocksIntoYear(int weeksIntoYear) {
        return Math.floorDiv(weeksIntoYear, ZoomLevel.BLOCKS.buckets()) + 1;
    }

    private static int calcWeeksIntoBlock(int weeksIntoYear) {

        return Math.floorMod(weeksIntoYear, ZoomLevel.BLOCKS.buckets());
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
                    ZoomLevel.WEEKS.buckets());
        }

        return weekNumber + 1;
    }

    private static int findLastWeekOfPriorYear(LocalDateTime clock) {
        LocalDate startOfPriorYear = LocalDate.of(clock.getYear() - 1, 1, 1);
        LocalDate firstMondayOfPriorYear = startOfPriorYear.with(firstInMonth(DayOfWeek.MONDAY));

        LocalDate endOfPriorYear = LocalDate.of(clock.getYear() - 1, 12, 31);

        return Math.floorDiv((endOfPriorYear.getDayOfYear() - firstMondayOfPriorYear.getDayOfYear()),
                ZoomLevel.WEEKS.buckets());

    }

    private static int calcWeekdayOffset(LocalDateTime clock) {
        DayOfWeek dayOfWeek = clock.getDayOfWeek();
        return dayOfWeek.getValue();
    }

    private static int calc20MinuteSteps(LocalDateTime clock) {
        int hoursInto4Hour = Math.floorMod(clock.getHour(), 4);
        int completedHours = hoursInto4Hour * 3;
        int current20Mins = Math.floorDiv( clock.getMinute() , 20);

        return completedHours + current20Mins + 1;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Coords {

        final LocalDateTime clockTime;
        final int year;
        final int block;
        final int weeksIntoBlock;
        final int weeksIntoYear;
        final int daysIntoWeek;
        final int fourhours;
        final int twenties;



        public String formatDreamTime() {
            return year + "_BW" + block + "-" + weeksIntoBlock + "-" + daysIntoWeek + "_TT" + fourhours + ":" + twenties;
        }

        public Coords panLeft(ZoomLevel zoomLevel) {

                switch (zoomLevel) {
                    case TWENTY_MINS:
                        return minus20Minutes();
                    case FOUR_HOURS:
                        return minus4Hour();
                    case DAYS:
                        return minusDay();
                    case WEEKS:
                        return minusWeek();
                    case BLOCKS:
                        return minusBlock();
                    case YEAR:
                        return minusYear();
                }
                return this;
        }

        public Coords panRight(ZoomLevel zoomLevel) {

            switch (zoomLevel) {
                case TWENTY_MINS:
                    return plus20Minutes();
                case FOUR_HOURS:
                    return plus4Hour();
                case DAYS:
                    return plusDay();
                case WEEKS:
                    return plusWeek();
                case BLOCKS:
                    return plusBlock();
                case YEAR:
                    return plusYear();
            }
            return this;
        }

        //pan left functions

        public Coords minus20Minutes() {
            return GeometryClock.createStoryCoords(clockTime.minusMinutes(20));
        }

        public Coords minus4Hour() {
            return GeometryClock.createStoryCoords(clockTime.minusHours(4));
        }

        public Coords minusDay() {
            return GeometryClock.createStoryCoords(clockTime.minusDays(1));
        }

        public Coords minusWeek() {
            return GeometryClock.createStoryCoords(clockTime.minusWeeks(1));
        }

        public Coords minusBlock() {
            return GeometryClock.createStoryCoords(clockTime.minusWeeks(ZoomLevel.BLOCKS.buckets()));
        }

        public Coords minusYear() {
            return GeometryClock.createStoryCoords(clockTime.minusYears(1));
        }

        // pan right functions

        public Coords plus20Minutes() {
            return GeometryClock.createStoryCoords(clockTime.plusMinutes(20));
        }

        public Coords plus4Hour() {
            return GeometryClock.createStoryCoords(clockTime.plusHours(4));
        }

        public Coords plusDay() {
            return GeometryClock.createStoryCoords(clockTime.plusDays(1));
        }

        public Coords plusWeek() {
            return GeometryClock.createStoryCoords(clockTime.plusWeeks(1));
        }

        public Coords plusBlock() {
            return GeometryClock.createStoryCoords(clockTime.plusWeeks(ZoomLevel.BLOCKS.buckets()));
        }

        public Coords plusYear() {
            return GeometryClock.createStoryCoords(clockTime.plusYears(1));
        }


    }

}
