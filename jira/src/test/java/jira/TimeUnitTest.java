package jira;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import se.metro.jira.util.TimeUtil;

public class TimeUnitTest {
    @Test
    public void test_2h(){
        DateTime start = new DateTime(2015, 11, 2, 10, 30);
        DateTime end = new DateTime(2015, 11, 2, 12, 30);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        Assert.assertThat(time, is(0.25));
        
    }

    @Test
    public void test_8h_dayChange(){
        DateTime start = new DateTime(2015, 11, 1, 13, 30);
        DateTime end = new DateTime(2015, 11, 2, 12, 30);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        Assert.assertThat(time, is(1.));
        
    }
    @Test
    public void test_8h_overWeekend(){
        DateTime start = new DateTime(2015, 11, 6, 13, 30);
        DateTime end = new DateTime(2015, 11, 9, 12, 30);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        Assert.assertThat(time, is(1.));
        
    }
    @Test
    public void test_6days_overWeekend(){
        DateTime start = new DateTime(2015, 11, 6, 13, 30);
        DateTime end = new DateTime(2015, 11, 16, 12, 30);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        Assert.assertThat(time, is(6.));
        
    }
    
    @Test
    public void test_specific(){
        DateTime start = new DateTime(2015, 9, 30, 17, 22);
        DateTime end = new DateTime(2015, 10, 1, 7, 51);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        Assert.assertThat(time, is(0.));
        
    }

    @Test
    public void test_specific2(){
        DateTime start = new DateTime(2015, 11, 24, 12, 18);
        DateTime end = new DateTime(2015, 11, 30, 14, 51);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        Assert.assertThat(time, is(4.25));
    }

    @Test
    public void testParse(){
        DateTime start = DateTime.parse("2017-11-06T13:49:45.990+01:00");
        DateTime end = DateTime.parse("2017-11-07T07:55:50.529+01:00");
        System.out.println(start);
        System.out.println(end);
        double time = TimeUtil.getWorkdaysBetween(start, end);
        System.out.println(time);
        Assert.assertThat(time, is(0.375));
    }
}
