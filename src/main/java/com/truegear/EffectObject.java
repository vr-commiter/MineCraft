package com.truegear;

import com.alibaba.fastjson.annotation.JSONField;

public class EffectObject {
    @JSONField(name = "name")
    public String name ;

    @JSONField(name = "uuid")
    public String uuid ;
    @JSONField(name = "tracks")
    public TrackObject[] trackList ;
    @JSONField(name = "keep")
    public Boolean keep ;
}
