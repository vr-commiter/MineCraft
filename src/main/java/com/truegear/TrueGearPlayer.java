package com.truegear;

import java.net.URISyntaxException;
import java.util.HashMap;

public interface TrueGearPlayer {
    void dispose();

    boolean GetStatus();
    void Start();

    void SendPlay(String key);
    void SendPlayEffectByContent(EffectObject key);
    void SendSeekEffect(String uuid);
    void PreSeekEffect(String uuid);

    EffectObject FindEffectByUuid(String uuid);
    void TryConnect();
    void Destory();
}

