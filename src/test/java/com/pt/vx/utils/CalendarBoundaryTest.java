package com.pt.vx.utils;
import com.pt.vx.OfflineTest;
import cn.hutool.core.date.ChineseDate;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import org.mockito.MockedStatic;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class CalendarBoundaryTest extends OfflineTest {
    private void lunar(LocalDate now,int lunarYear,int month,int day){
        ChineseDate target=new ChineseDate(lunarYear,month,day,false);
        LocalDate expected=LocalDate.of(target.getGregorianYear(),target.getGregorianMonthBase1(),target.getGregorianDay());
        try(MockedStatic<LocalDate> dates=mockStatic(LocalDate.class,CALLS_REAL_METHODS)){
            dates.when(LocalDate::now).thenReturn(now);
            assertEquals(String.valueOf(ChronoUnit.DAYS.between(now,expected)),DateUtil.getNextChineseBirthDay(month,day));
        }
    }
    @Test public void lunarDecemberKeepsConvertedGregorianYear(){lunar(LocalDate.of(2026,12,1),2026,12,1);}
    @Test public void januaryUsesCurrentLunarYearInsteadOfGregorianYear(){lunar(LocalDate.of(2027,1,1),2026,12,1);}
    @Test public void lunarBirthdayTodayIsZero(){ChineseDate day=new ChineseDate(2030,6,10,false);lunar(LocalDate.of(day.getGregorianYear(),day.getGregorianMonthBase1(),day.getGregorianDay()),2030,6,10);}
    @Test public void february29FindsNextActualLeapDay(){
        LocalDate now=LocalDate.of(2025,3,1),next=LocalDate.of(2028,2,29);
        try(MockedStatic<LocalDate> dates=mockStatic(LocalDate.class,CALLS_REAL_METHODS)){dates.when(LocalDate::now).thenReturn(now);assertEquals(String.valueOf(ChronoUnit.DAYS.between(now,next)),DateUtil.getNextBirthDay(2,29));}
    }
    @Test public void leapBirthdayTodayIsZero(){LocalDate now=LocalDate.of(2028,2,29);try(MockedStatic<LocalDate> dates=mockStatic(LocalDate.class,CALLS_REAL_METHODS)){dates.when(LocalDate::now).thenReturn(now);assertEquals("0",DateUtil.getNextBirthDay(2,29));}}
    @Test public void invalidMonthStillFails(){assertThrows(java.time.DateTimeException.class,()->DateUtil.getNextBirthDay(13,1));}
}