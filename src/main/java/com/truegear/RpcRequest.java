package com.truegear;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.Base64;

public class RpcRequest {
    public class RpcRequestObj{
        @JSONField(name = "Method")
        public String Method;
        @JSONField(name = "ReqId")
        public  String ReqId;
        @JSONField(name = "Body")
        public  String Body;
        RpcRequestObj() {
            // no-args constructor
        }
    }

    public String Create_RegisteApp(String body1) {
        String b_base64 = Base64.getEncoder().encodeToString(body1.getBytes());
        RpcRequestObj obj = new RpcRequestObj();
        obj.Body = b_base64;
        obj.Method = "register_app";
        return GetContent(obj);
    }

    public String Create_PlayEffectByUuid(String appId, String uuid) {
        String b_base64 = Base64.getEncoder().encodeToString((appId + ";" + uuid).getBytes());
        RpcRequestObj obj = new RpcRequestObj();
        obj.Body = b_base64;
        obj.Method = "play_effect_by_uuid";
        return GetContent(obj);
    }

    public  String Create_PlayEffectByEffectObject(EffectObject effectObject) {
        String jsonString = JSON.toJSONString(effectObject);
        String b_base64 = Base64.getEncoder().encodeToString(jsonString.getBytes());
        RpcRequestObj obj = new RpcRequestObj();
        obj.Body = b_base64;
        obj.Method = "play_effect_by_content";
        return GetContent(obj);
    }

    public String Create_SeekEffectObject_byUUid(String appId, String uuid) {
        String b_base64 = Base64.getEncoder().encodeToString((appId + ";" + uuid).getBytes());
        RpcRequestObj obj = new RpcRequestObj();
        obj.Body = b_base64;
        obj.Method = "seek_by_uuid";
        return GetContent(obj);
    }

    public static String GetContent(RpcRequestObj obj) {
        String jsonString = JSON.toJSONString(obj);
        return  jsonString;
    }
}
