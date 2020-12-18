package com.dreamscale.gridtime.core.capability.query;


import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import lombok.Getter;
import lombok.Setter;
import org.dreamscale.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GridtimeExpression {

    private String formattedExpression;

    private Integer year;
    private Integer block;
    private Integer week;
    private Integer day;
    private Integer daypart;
    private Integer twenty;

    private ZoomLevel rangeZoomLevel;
    private Integer rangeExpression;

    private ZoomLevel zoomLevel;

    private List<Integer> coords;
    private List<Integer> rangeCoords;

    private GridtimeExpression() {}

    public static GridtimeExpression parse(String gridtimeExpressionStr) {

        char ch;
        StringBuilder token = new StringBuilder("");

        int coordinatePosition = 0;
        boolean isRangeCoordinate = false;

        String coordinateExp = gridtimeExpressionStr.substring(gridtimeExpressionStr.indexOf('[') + 1);

        GridtimeExpression exp = new GridtimeExpression();

        for (int i = 0; i < coordinateExp.length(); i++) {
            ch = coordinateExp.charAt(i);

            if (Character.isDigit(ch)) {
                token.append(ch);
            } else if (ch == ',' || ch == ']') {
                finishToken(exp, coordinatePosition, isRangeCoordinate, token.toString());
                token.setLength(0);
                isRangeCoordinate = false;
                coordinatePosition++;
            } else if (ch == '-') {
                finishToken(exp, coordinatePosition, isRangeCoordinate, token.toString());
                token.setLength(0);
                isRangeCoordinate = true;
            } else {
                throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Unable to parse " + gridtimeExpressionStr + " ch = "+ch);
            }
        }

        exp.setFormattedExpression(gridtimeExpressionStr);
        exp.setZoomLevel(determineZoomLevel(coordinatePosition - 1));
        exp.setCoords(createCoords(exp));

        if (exp.hasRangeExpression()) {
            exp.setRangeCoords(createRangeCoords(exp));
        }

        return exp;
    }


    public static GridtimeExpression createFrom(GeometryClock.GridTime gt) {

        GridtimeExpression gtExp = new GridtimeExpression();

        gtExp.setYear(gt.getYear());
        gtExp.setBlock(gt.getBlock());
        gtExp.setWeek(gt.getBlockWeek());
        gtExp.setDay(gt.getDay());
        gtExp.setDaypart(gt.getDayPart());
        gtExp.setTwenty(gt.getTwentyOfTwelve());
        gtExp.setZoomLevel(gt.getZoomLevel());
        gtExp.setFormattedExpression(gt.getFormattedCoords());
        gtExp.setCoords(DefaultCollections.toList(gt.getCoordinates()));

        return gtExp;
    }

    private static List<Integer> createCoords(GridtimeExpression exp) {
        List<Integer> coords = new ArrayList<>();

        addIfNotNull(coords, exp.getYear());
        addIfNotNull(coords, exp.getBlock());
        addIfNotNull(coords, exp.getWeek());
        addIfNotNull(coords, exp.getDay());
        addIfNotNull(coords, exp.getDaypart());
        addIfNotNull(coords, exp.getTwenty());

        return coords;
    }

    private static void addIfNotNull(List<Integer> coords, Integer coord) {
        if (coord != null) {
            coords.add(coord);
        }
    }

    private static List<Integer> createRangeCoords(GridtimeExpression exp) {
        List<Integer> coords = createCoords(exp);

        int coordIndexForRangeExp = getCoordIndexOfZoomLevel(exp.getRangeZoomLevel());

        coords.set(coordIndexForRangeExp, exp.getRangeExpression());

        return coords;
    }

    private static int getCoordIndexOfZoomLevel(ZoomLevel zoomLevel) {
        if (zoomLevel == ZoomLevel.YEAR) {
            return 0;
        }
        if (zoomLevel == ZoomLevel.BLOCK) {
            return 1;
        }
        if (zoomLevel == ZoomLevel.WEEK) {
            return 2;
        }
        if (zoomLevel == ZoomLevel.DAY) {
            return 3;
        }
        if (zoomLevel == ZoomLevel.DAY_PART) {
            return 4;
        }
        if (zoomLevel == ZoomLevel.TWENTY) {
            return 5;
        }
        throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Unknown zoom level for range expression " + zoomLevel);
    }

    private static ZoomLevel determineZoomLevel(int coordinatePosition) {
        if (coordinatePosition == 0) {
            return ZoomLevel.YEAR;
        }
        if (coordinatePosition == 1) {
            return ZoomLevel.BLOCK;
        }
        if (coordinatePosition == 2) {
            return ZoomLevel.WEEK;
        }
        if (coordinatePosition == 3) {
            return ZoomLevel.DAY;
        }
        if (coordinatePosition == 4) {
            return ZoomLevel.DAY_PART;
        }
        if (coordinatePosition == 5) {
            return ZoomLevel.TWENTY;
        }
        throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Unable to determine zoom level from coordinate :"+coordinatePosition);
    }


    private static void finishToken(GridtimeExpression exp, int coordinatePosition, boolean isRangeCoordinate, String token) {
        int number = Integer.parseInt(token);

        if (isRangeCoordinate && exp.getRangeExpression() == null) {
            exp.setRangeZoomLevel(determineZoomLevel(coordinatePosition));
            exp.setRangeExpression(number);
        } else if (isRangeCoordinate && exp.getRangeExpression() != null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Only one range expression allowed in gt[exp].");

        } else {
            setCoordinate(exp, coordinatePosition, number);
        }
    }

    private static void setCoordinate(GridtimeExpression exp, int coordinatePosition, int number) {
        if (coordinatePosition == 0) {
            exp.setYear(number);
        }

        if (coordinatePosition == 1) {
            exp.setBlock(number);
        }

        if (coordinatePosition == 2) {
            exp.setWeek(number);
        }

        if (coordinatePosition == 3) {
            exp.setDay(number);
        }

        if (coordinatePosition == 4) {
            exp.setDaypart(number);
        }

        if (coordinatePosition == 5) {
            exp.setTwenty(number);
        }
    }

    public boolean hasRangeExpression() {
        return rangeExpression != null;
    }


}
