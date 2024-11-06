package com.truegear;

import com.alibaba.fastjson.annotation.JSONField;

public class TrackObject {
    @JSONField(name = "action_type")
    public ActionType action_type;

    @JSONField(name = "intensity_mode")
    public IntensityMode intensity_mode;

    @JSONField(name = "once")
    public boolean once;
    @JSONField(name = "stop_name")
    public String stopName;
    //间隔
    @JSONField(name = "interval")
    public int interval ;
    @JSONField(name = "start_time")
    public int start_time ;
    @JSONField(name = "end_time")
    public int end_time ;


    @JSONField(name = "start_intensity")
    public int start_intensity ;

    @JSONField(name = "end_intensity")
    public int end_intensity ;
    @JSONField(name = "index")
    public int[] index ;
}
