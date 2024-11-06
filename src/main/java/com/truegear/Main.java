package com.truegear;

public class Main {
    private static TrueGearPlayer hapticPlayer;
    public static void main(String[] args) {
        System.out.printf("Hello and welcome!");

        hapticPlayer = new TrueGearPlayerImpl("617830", "appName");
        hapticPlayer.PreSeekEffect("DropItemRight");
        hapticPlayer.Start();
        while(!hapticPlayer.GetStatus() )
        {
            continue;
        }
        //hapticPlayer.SendSeekEffect("DropItemRight");
        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
       // EffectObject ob =  hapticPlayer.FindEffectByUuid("DropItemRight");
        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
    }
}