package com.dreamscale.htmflow.core.gridtime.executor.clock;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

public class GridTime {
    private final ZoomLevel zoomLevel;
    private final Integer[] coords;

    private static final String AM = "am";
    private static final String PM = "pm";
    private final String gridTime;

    GridTime(ZoomLevel zoomLevel, Integer ... coords) {
        this.zoomLevel = zoomLevel;
        this.coords = coords;
        this.gridTime = formatGridTime(coords);
    }

    public boolean equals(GridTime gridTime) {
        if (coords.length != gridTime.coords.length) {
            return false;
        }

        for (int i = 0; i < coords.length; i++) {
            if (!coords[i].equals(gridTime.coords[i])) {
                return false;
            }
        }
        return true;
    }


    public String getFormattedGridTime() {
        return gridTime;
    }

    public Integer getYear() {
        if (coords.length > 0) {
            return coords[0];
        }
        return null;
    }

    public Integer getBlock() {
        if (coords.length > 1) {
            return coords[1];
        }
        return null;
    }

    public Integer getBlockWeek() {
        if (coords.length > 2) {
            return coords[2];
        }
        return null;
    }

    public Integer getDay() {
        if (coords.length > 3) {
            return coords[3];
        }
        return null;
    }

    public Integer getDayPart() {
        if (coords.length > 4) {
            return coords[4];
        }
        return null;
    }

    public Integer getTwentyOfTwelve() {
        if (coords.length > 5) {
            return coords[5];
        }
        return null;
    }


    public GridTime panLeft() {
        Integer [] newCoords = coords.clone();

        switch (zoomLevel) {
            case TWENTY:
                minus(newCoords, zoomLevel, 5);
                break;
            case DAY_PART:
                minus(newCoords, zoomLevel, 4);
                break;
            case DAY:
                minus(newCoords, zoomLevel, 3);
                break;
            case WORK_WEEK:
                minus(newCoords, zoomLevel, 2);
                break;
            case BLOCK_OF_SIX_WEEKS:
                minus(newCoords, zoomLevel, 1);
                break;
            case YEAR:
                minus(newCoords, zoomLevel, 0);
        }
        return new GridTime(zoomLevel, newCoords);
    }

    public GridTime panRight() {
        Integer [] newCoords = coords.clone();

        switch (zoomLevel) {
            case TWENTY:
                plus(newCoords, zoomLevel, 5);
                break;
            case DAY_PART:
                plus(newCoords, zoomLevel, 4);
                break;
            case DAY:
                plus(newCoords, zoomLevel, 3);
                break;
            case WORK_WEEK:
                plus(newCoords, zoomLevel, 2);
                break;
            case BLOCK_OF_SIX_WEEKS:
                plus(newCoords, zoomLevel, 1);
                break;
            case YEAR:
                newCoords[0] = coords[0]+1;
        }
        return new GridTime(zoomLevel, newCoords);
    }

    private void minus(Integer[] newCoords, ZoomLevel zoomLevel, int index) {
        newCoords[index] = coords[index] - 1;
        if (newCoords[index] < 0) {
            newCoords[index] = zoomLevel.getParentBeats() - 1;
            minus(newCoords, zoomLevel.zoomOut(), index - 1);
        }
    }

    private void plus(Integer[] newCoords, ZoomLevel zoomLevel, int index) {
        newCoords[index] = coords[index] + 1;
        if (newCoords[index] > zoomLevel.getParentBeats() - 1) {
            newCoords[index] = 0;
            plus(newCoords, zoomLevel.zoomOut(), index - 1);
        }
    }

    private String formatGridTime(Integer[] coords) {
        String gridTime = "";

        gridTime = appendYear(getYear(), gridTime);

        gridTime = appendBlock(getBlock(), gridTime);

        gridTime = appendWeek(getBlockWeek(), gridTime);

        gridTime = appendDay(getDay(), gridTime);

        gridTime = appendDayPart(getDayPart(), gridTime);

        gridTime = appendHourTwentyAdjust(getTwentyOfTwelve(), gridTime);


        return gridTime;
    }

    private String appendHourTwentyAdjust(Integer twenties, String gridTime) {
        if (twenties != null) {
            int hourAdjust = Math.floorDiv(twenties , 4);
            int twentiesRemainder = Math.floorMod(twenties, 4) * 20;
            String zeroPaddedTwenties = StringUtils.rightPad(Integer.toString(twentiesRemainder), 2, "0");

            gridTime += "+"+hourAdjust + ":" + zeroPaddedTwenties;
        }

        return gridTime;
    }

    private String appendDayPart(Integer dayPart, String gridTime) {
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

    private String appendDay(Integer day, String gridTime) {
        if (day != null) {
            gridTime += "-D"+day;
        }
        return gridTime;
    }

    private String appendWeek(Integer blockWeek, String gridTime) {
        if (blockWeek != null) {
            gridTime += "-W"+blockWeek;
        }
        return gridTime;
    }

    private String appendBlock(Integer block, String gridTime) {
        if (block != null) {
            gridTime += "B"+block;
        }
        return gridTime;
    }

    private String appendYear(Integer year, String gridTime) {
        if (year != null) {
            gridTime += year;
        }
        return gridTime;
    }

    private String getAMorPM(Integer fourHours) {
        if (fourHours <= 3) {
            return AM;
        } else {
            return PM;
        }
    }

    public ZoomLevel getZoomLevel() {
        return zoomLevel;
    }

    public static GridTime fromGridTime(String gridtime) {
        //TODO implement me when we do URI mappings
        return null;
    }

    public LocalDateTime toClockTime() {
        //TODO implement me when we do URI mappings and controller stuff
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
