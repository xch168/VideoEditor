package com.m4399.videoeditor.util;

public class TimeUtil
{
    public static String format(long time)
    {
        String str = "" ;
        time = time / 1000;
        int s = (int) (time % 60);
        int m = (int) (time / 60 % 60);
        int h = (int) (time / 3600);
        if(h > 0)
        {
            str += (h + ":");
        }
        if(m <= 9)
        {
            str = str + "0" + m + ":";
        }
        else
        {
            str += (m + ":");
        }
        if(s <= 9)
        {
            str = str + "0" + s;
        }
        else
        {
            str += s;
        }
        return str;
    }
}
