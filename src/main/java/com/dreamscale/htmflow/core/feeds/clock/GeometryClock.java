package com.dreamscale.htmflow.core.feeds.clock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.AntPathMatcher;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class GeometryClock {

    private LocalDateTime clockTime;

    private Coords coords;


    public GeometryClock(LocalDateTime clockTime) {
        this.clockTime = clockTime;
        this.coords = createCoords(clockTime);
    }

    public static LocalDateTime roundDownToNearestTwenty(LocalDateTime middleOfNowhereTime) {
        int minutes = middleOfNowhereTime.getMinute();

        int twenties = Math.floorDiv(minutes, 20);

        LocalDateTime roundedTime = middleOfNowhereTime.withMinute(twenties * 20);
        return roundedTime.withSecond(0).withNano(0);
    }

    public Coords tick() {
        int minutesToTick = ZoomLevel.TWENTIES.buckets();
        LocalDateTime nextClockTime = this.clockTime.plusMinutes(minutesToTick);

        this.coords = createCoords(nextClockTime);
        this.clockTime = nextClockTime;

        return this.coords;
    }

    public LocalDateTime getNextTickTime() {
        int minutesToTick = ZoomLevel.TWENTIES.buckets();
        return this.clockTime.plusMinutes(minutesToTick);
    }

    public Coords getCoordinates() {
        return coords;
    }

    public static Coords createCoords(LocalDateTime nextClockTime) {

        int twelveTwenties = calc4HourSteps(nextClockTime);
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
                twelveTwenties,
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
        final int twelves;
        final int twenties;

        public static Coords fromDreamTime(String dreamtime) {
            AntPathMatcher pathMatcher = new AntPathMatcher();

            Map<String, String> variables = pathMatcher.extractUriTemplateVariables("{year}_BWD{block}-{weeksIntoBlock}-{daysIntoWeek}_TT{twelves}-{twenties}", dreamtime);
            System.out.println(variables);

            int year = Integer.valueOf(variables.get("year"));
            int block = Integer.valueOf(variables.get("block"));
            int weeksIntoBlock = Integer.valueOf(variables.get("weeksIntoBlock"));
            int daysIntoWeek = Integer.valueOf(variables.get("daysIntoWeek"));
            int twelves = Integer.valueOf(variables.get("twelves"));
            int twenties = Integer.valueOf(variables.get("twenties"));

            LocalDate sameYear = LocalDate.of(year, 1, 1);
            LocalDate firstMondayOfSameYear = sameYear.with(firstInMonth(DayOfWeek.MONDAY));

            int firstDayOfYear = firstMondayOfSameYear.getDayOfYear();

            int blockDayTotal = (block - 1) * ZoomLevel.BLOCKS.buckets() * ZoomLevel.WEEKS.buckets();
            int weekDayTotal = (weeksIntoBlock - 1) * ZoomLevel.WEEKS.buckets();
            int dayIntoYear = firstDayOfYear + blockDayTotal + weekDayTotal + (daysIntoWeek - 1);

            int partialTwelves = Math.floorDiv(twenties - 1 , 3);
            int remainderInHour = Math.floorMod(twenties - 1, 3);
            int hoursIntoDay = (twelves - 1) * 4 + partialTwelves;

            int minutesIntoHour = remainderInHour * 20;

            LocalDateTime translatedTime = LocalDateTime.of(year, 1, 1, hoursIntoDay, minutesIntoHour);
            translatedTime = translatedTime.withDayOfYear(dayIntoYear);

            return new Coords(translatedTime, year, block, weeksIntoBlock, block * 6 + weeksIntoBlock,
                    daysIntoWeek, twelves, twenties);
        }


        public String formatDreamTime() {
            return year + "_BWD" + block + "-" + weeksIntoBlock + "-" + daysIntoWeek + "_TT" + twelves + "-" + twenties;
        }

        public Coords panLeft(ZoomLevel zoomLevel) {

                switch (zoomLevel) {
                    case TWENTIES:
                        return minus20Minutes();
                    case TWELVE_TWENTIES:
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
                case TWENTIES:
                    return plus20Minutes();
                case TWELVE_TWENTIES:
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
            return GeometryClock.createCoords(clockTime.minusMinutes(20));
        }

        public Coords minus4Hour() {
            return GeometryClock.createCoords(clockTime.minusHours(4));
        }

        public Coords minusDay() {
            return GeometryClock.createCoords(clockTime.minusDays(1));
        }

        public Coords minusWeek() {
            return GeometryClock.createCoords(clockTime.minusWeeks(1));
        }

        public Coords minusBlock() {
            return GeometryClock.createCoords(clockTime.minusWeeks(ZoomLevel.BLOCKS.buckets()));
        }

        public Coords minusYear() {
            return GeometryClock.createCoords(clockTime.minusYears(1));
        }

        // pan right functions

        public Coords plus20Minutes() {
            return GeometryClock.createCoords(clockTime.plusMinutes(20));
        }

        public Coords plus4Hour() {
            return GeometryClock.createCoords(clockTime.plusHours(4));
        }

        public Coords plusDay() {
            return GeometryClock.createCoords(clockTime.plusDays(1));
        }

        public Coords plusWeek() {
            return GeometryClock.createCoords(clockTime.plusWeeks(1));
        }

        public Coords plusBlock() {
            return GeometryClock.createCoords(clockTime.plusWeeks(ZoomLevel.BLOCKS.buckets()));
        }

        public Coords plusYear() {
            return GeometryClock.createCoords(clockTime.plusYears(1));
        }


    }

}
