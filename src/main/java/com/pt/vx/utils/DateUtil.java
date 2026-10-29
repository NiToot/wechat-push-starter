package com.pt.vx.utils;

import cn.hutool.core.date.ChineseDate;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static java.time.DayOfWeek.MONDAY;

public class DateUtil {




    /**
     * 获取今天离目标的距离天数
     * @param year 目标年
     * @param month  目标月
     * @param day 目标天
     * @return 天数
     */
    public static String passDay(int year,int month,int day){
        LocalDate start = LocalDate.of(year, month, day);
        LocalDate now = LocalDate.now();
        return passDay(now,start);
    }

    /**
     *
     * @param chineseYear 农历年
     * @param chineseMonth 农历月
     * @param chineseDay 农历日
     * @return 距离今天的天数
     */
    public static String passDayChinese(int chineseYear,int chineseMonth,int chineseDay){
        ChineseDate chineseDate = new ChineseDate(chineseYear,chineseMonth,chineseDay,false);
        int gregorianYear = chineseDate.getGregorianYear();
        int gregorianMonthBase1 = chineseDate.getGregorianMonthBase1();
        int gregorianDay = chineseDate.getGregorianDay();
        LocalDate start = LocalDate.of(gregorianYear, gregorianMonthBase1, gregorianDay);
        LocalDate now = LocalDate.now();
        return passDay(now,start);
    }

    public static String passDayOfNow(LocalDate source){
        int year = source.getYear();
        int month = source.getMonthValue();
        int day = source.getDayOfMonth();
        LocalDate start = LocalDate.of(year, month, day);
        LocalDate now = LocalDate.now();
        return passDay(now,start);
    }

    /**
     *  获取资源离目标的距离天数
     * @param source 资源日期
     * @param target 目标容器
     * @return 天数
     */
    public static String passDay(LocalDate source,LocalDate target){
        return source.toEpochDay() - target.toEpochDay() + "";
    }


    /**
     *
     * 获取距离阳历生日的天数
     * @param month 阳历生日的月份
     * @param day 阳历生日的日期
     * @return 天数
     */
    public static String getNextBirthDay(int month,int day){
        LocalDate now = LocalDate.now();
        java.time.MonthDay birthday = java.time.MonthDay.of(month, day);
        for (int year = now.getYear(); year <= now.getYear() + 8; year++) {
            if (!birthday.isValidYear(year)) continue;
            LocalDate next = birthday.atYear(year);
            if (!next.isBefore(now)) return passDay(next, now);
        }
        throw new IllegalArgumentException("未找到下一次有效的公历生日");
    }
    public static String getNextBirthDay(LocalDate birthday){
        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();
        return getNextBirthDay(month,day);
    }

    public static String getNextChineseBirthDay(LocalDate birthday){
        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();
        return getNextChineseBirthDay(month,day);
    }

    /**
     * 获取距离农历生日的天数
     * @param chineseMonth 农历生日的月份
     * @param chineseDay 农历生日的日期
     * @return 天数
     */
    public static String getNextChineseBirthDay(int chineseMonth,int chineseDay){
        if (chineseMonth < 1 || chineseMonth > 12 || chineseDay < 1 || chineseDay > 30)
            throw new IllegalArgumentException("农历月份或日期无效");
        LocalDate now = LocalDate.now();
        int lunarYear = new ChineseDate(now).getChineseYear();
        for (int year = lunarYear; year <= 2099; year++) {
            ChineseDate date = new ChineseDate(year, chineseMonth, chineseDay, false);
            LocalDate next = LocalDate.of(date.getGregorianYear(), date.getGregorianMonthBase1(), date.getGregorianDay());
            ChineseDate checked = new ChineseDate(next);
            // A lunar month may not have day 30; skip that year instead of silently shifting the date.
            if (checked.getMonth() != chineseMonth || checked.getDay() != chineseDay || checked.isLeapMonth()) continue;
            if (!next.isBefore(now)) return passDay(next, now);
        }
        throw new IllegalArgumentException("农历转换超出支持范围，未找到2099年内的有效生日");
    }
    /**
     *
     * @param date 日期
     * @param isChinese 中文还是数字
     * @return 星期
     */
    public static String getWeek(LocalDate date,boolean isChinese){
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if(isChinese){
            String week = "";
            switch (dayOfWeek) {
                case MONDAY:
                    week = "一";
                    break;
                case TUESDAY:
                    week = "二";
                    break;
                case WEDNESDAY:
                    week = "三";
                    break;
                case THURSDAY:
                    week = "四";
                    break;
                case FRIDAY:
                    week = "五";
                    break;
                case SATURDAY:
                    week = "六";
                    break;
                case SUNDAY:
                    week = "日";
                    break;
                default:
                    week = "未知";
            }

            return week;
        }else {
            return ""+dayOfWeek.getValue();
        }



    }


}
