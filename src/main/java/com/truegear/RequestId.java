package com.truegear;

public class RequestId
{
    private static int cur;
    public static String Generate()
    {
        return String.format("%d", cur++);
    }
}
