package com.truegear;

import com.alibaba.fastjson.JSON;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;

public class TrueGearPlayerImpl implements TrueGearPlayer {
    private String url = "ws://127.0.0.1:18233/v1/tact/";
    private TruegearWsClient client;
    private String appId = "";
    private String apiKey = "";

    private HashMap<String, EffectObject> _effectSeek;
    public TrueGearPlayerImpl(String appid, String apikey)
    {
        appId = appid;
        apiKey = apikey;
        _effectSeek = new HashMap<String, EffectObject>();
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean GetStatus() {
        return client.isOpen();
    }

    @Override
    public void Start()  {
        try {
            client = new TruegearWsClient(new URI(url));
            client.connect();
            client.addPlayerConnectedCallback(connected -> {
                if (client.isOpen()) {
                    client.send("");
                    RpcRequest o = new RpcRequest();
                    String rpc = o.Create_RegisteApp(appId + ";" + apiKey);
                    client.send(rpc);
                }
            });

            client.addPlayerMessageReviCallback(msg -> {
                RpcResponse group = JSON.parseObject(msg, RpcResponse.class);
                if (group.Method.equals( "seek_by_uuid"))
                {
                    byte[] decodedBytes = Base64.getDecoder().decode(group.Result);
                    String body = new String(decodedBytes);
                    EffectObject obj = JSON.parseObject(body, EffectObject.class);
                    this._effectSeek.put(obj.uuid, obj);
                }

                if (group.Method.equals( "register_app"))
                {
                    for (String key : _effectSeek.keySet())
                    {
                        RpcRequest o1 = new RpcRequest();
                        String rpc1 = o1.Create_SeekEffectObject_byUUid(appId, key);
                        client.send(rpc1);
                    }
                }
                //    RpcRequest o = new RpcRequest();
//                    String rpc = o.Create_RegisteApp(appId + ";" + apiKey);
  //                  client.send(rpc);
            });
        } catch (URISyntaxException e) {
            //("HapticPlayerImpl() " + e.getMessage(), e);
        }
    }

    @Override
    public void SendPlay(String uuid) {
        if (!client.isOpen())
            return;
        RpcRequest o = new RpcRequest();
        String rpc = o.Create_PlayEffectByUuid(appId, uuid);
        client.send(rpc);
    }

    @Override
    public void PreSeekEffect(String uuid) {
        _effectSeek.put(uuid, null);
    }
    @Override
    public void SendSeekEffect(String uuid) {
        if (!client.isOpen())
            return;
        RpcRequest o1 = new RpcRequest();
        String rpc1 = o1.Create_SeekEffectObject_byUUid(appId, uuid);
        client.send(rpc1);
    }

    @Override
    public EffectObject FindEffectByUuid(String uuid){
        return _effectSeek.get(uuid);
    }

    @Override
    public void SendPlayEffectByContent(EffectObject effectObject) {
        if (!client.isOpen())
            return;
        RpcRequest o = new RpcRequest();
        String rpc = o.Create_PlayEffectByEffectObject(effectObject);
        client.send(rpc);
    }

    @Override
    public void TryConnect() {
        if (GetStatus())
        {
            return ;
        }
        client.connect();
    }

    @Override
    public void Destory() {
        client.close();
    }
}
