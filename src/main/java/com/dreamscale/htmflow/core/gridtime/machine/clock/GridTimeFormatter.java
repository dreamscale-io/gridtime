package com.dreamscale.htmflow.core.gridtime.machine.clock;

import org.apache.commons.lang3.StringUtils;

public class GridTimeFormatter {

    private static final String AM = "am";
    private static final String PM = "pm";


    public static Integer getYear(Integer [] coords) {
        if (coords.length > 0) {
            return coords[0];
        }
        return null;
    }

    public static Integer getBlock(Integer [] coords) {
        if (coords.length > 1) {
            return coords[1];
        }
        return null;
    }

    public static Integer getBlockWeek(Integer [] coords) {
        if (coords.length > 2) {
            return coords[2];
        }
        return null;
    }

    public static Integer getDay(Integer [] coords) {
        if (coords.length > 3) {
            return coords[3];
        }
        return null;
    }

    public static Integer getDayPart(Integer [] coords) {
        if (coords.length > 4) {
            return coords[4];
        }
        return null;
    }

    public static Integer getTwentyOfTwelve(Integer [] coords) {
        if (coords.length > 5) {
            return coords[5];
        }
        return null;
    }


    public static String formatGridTime(Integer[] coords) {
        String gridTime = "";

        gridTime = appendYear(getYear(coords), gridTime);

        gridTime = appendBlock(getBlock(coords), gridTime);

        gridTime = appendWeek(getBlockWeek(coords), gridTime);

        gridTime = appendDay(getDay(coords), gridTime);

        gridTime = appendDayPart(getDayPart(coords), gridTime);

        gridTime = appendHourTwentyAdjust(getTwentyOfTwelve(coords), gridTime);

        return gridTime;
    }

    private static String appendHourTwentyAdjust(Integer twenties, String gridTime) {
        if (twenties != null) {
            int hourAdjust = Math.floorDiv(twenties-1, 3);
            int twentiesRemainder = Math.floorMod(twenties -1, 3) * 20;
            String zeroPaddedTwenties = StringUtils.rightPad(Integer.toString(twentiesRemainder), 2, "0");

            gridTime += "+"+hourAdjust + ":" + zeroPaddedTwenties;
        }

        return gridTime;
    }

    private static String appendDayPart(Integer dayPart, String gridTime) {
        if (dayPart != null) {

            String dayPartInHours = "";

            switch (dayPart) {
                case 1:
                    dayPartInHours = "12am"; break;
                case 2:
                    dayPartInHours = "4am"; break;
                case 3:
                    dayPartInHours = "8am"; break;
                case 4:
                    dayPartInHours = "12pm"; break;
                case 5:
                    dayPartInHours = "4pm"; break;
                case 6:
                    dayPartInHours = "8pm"; break;
            }

            gridTime += "_"+dayPartInHours;
        }
        return gridTime;
    }

    private static String appendDay(Integer day, String gridTime) {
        if (day != null) {
            gridTime += "-D"+day;
        }
        return gridTime;
    }

    private static String appendWeek(Integer blockWeek, String gridTime) {
        if (blockWeek != null) {
            gridTime += "-W"+blockWeek;
        }
        return gridTime;
    }

    private static String appendBlock(Integer block, String gridTime) {
        if (block != null) {
            gridTime += "-B"+block;
        }
        return gridTime;
    }

    private static String appendYear(Integer year, String gridTime) {
        if (year != null) {
            gridTime += year;
        }
        return gridTime;
    }

    public static Integer [] parseGridTime(String gridtime) {
        //TODO implement me when we do URI mappings
        return null;
    }


//        public static Coords fromGridTime(String gridTime) {
//            AntPathMatcher pathMatcher = new AntPathMatcher();
//
//            Map<String, String> variables = pathMatcher.extractUriTemplateVariables("{year}_BWD{block}-{weeksIntoBlock}-{daysIntoWeek}_TT{twelves}-{twenties}", gridTime);
//            System.out.println(variables);
//
//            int year = Integer.valueOf(variables.get("year"));
//            int block = Integer.valueOf(variables.get("block"));
//            int weeksIntoBlock = Integer.valueOf(variables.get("weeksIntoBlock"));
//            int daysIntoWeek = Integer.valueOf(variables.get("daysIntoWeek"));
//            int twelves = Integer.valueOf(variables.get("twelves"));
//            int twenties = Integer.valueOf(variables.get("twenties"));
//
//            LocalDate sameYear = LocalDate.of(year, 1, 1);
//            LocalDate firstMondayOfSameYear = sameYear.with(firstInMonth(DayOfWeek.MONDAY));
//
//            int firstDayOfYear = firstMondayOfSameYear.getDayOfYear();
//
//            int blockDayTotal = (block - 1) * ZoomLevel.BLOCK_OF_SIX_WEEKS.getInnerBeats() * ZoomLevel.WORK_WEEK.getInnerBeats();
//            int weekDayTotal = (weeksIntoBlock - 1) * ZoomLevel.WORK_WEEK.getInnerBeats();
//            int dayIntoYear = firstDayOfYear + blockDayTotal + weekDayTotal + (daysIntoWeek - 1);
//
//            int partialTwelves = Math.floorDiv(twenties - 1 , 3);
//            int remainderInHour = Math.floorMod(twenties - 1, 3);
//            int hoursIntoDay = (twelves - 1) * 4 + partialTwelves;
//
//            int minutesIntoHour = remainderInHour * 20;
//
//            LocalDateTime translatedTime = LocalDateTime.of(year, 1, 1, hoursIntoDay, minutesIntoHour);
//            translatedTime = translatedTime.withDayOfYear(dayIntoYear);
//
//            return new Coords(translatedTime, year, block, weeksIntoBlock, block * 6 + weeksIntoBlock,
//                    daysIntoWeek, twelves, twenties);
//        }
//
//
//        public String formatGridTime() {
//            return year + "_BWD" + block + "-" + weeksIntoBlock + "-" + daysIntoWeek + "_TT" + twelves + "-" + twenties;
//        }

}
