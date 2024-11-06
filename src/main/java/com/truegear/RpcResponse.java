package com.truegear;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.Base64;

public class RpcResponse {
    @JSONField(name = "Method")
    public String Method;

    @JSONField(name = "ReqId")
    public String ReqId;
    @JSONField(name = "Result")
    public String Result;
}
