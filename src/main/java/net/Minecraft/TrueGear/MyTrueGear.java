package net.Minecraft.TrueGear;

import com.truegear.*;

import java.util.Arrays;
import java.util.Random;

public class MyTrueGear {

    private static TrueGearPlayer _Player = null;

    private static String appID = "-10001";
    private static String apikey = "MineCraft";


    public MyTrueGear() {
//        LeftBow leftBowThread = new LeftBow();
//        leftBowThread.start();
        _Player = new TrueGearPlayerImpl(appID,apikey);
        PreSeek();
        _Player.Start();

        Minecraft_TrueGear.LOGGER.info("---------------------------------------");
        Minecraft_TrueGear.LOGGER.info("MyTrueGear is Started");
    }

    private void PreSeek()
    {
        _Player.PreSeekEffect("Rain");
        _Player.PreSeekEffect("DefaultDamage");
        _Player.PreSeekEffect("ShieldDefaultDamage");
    }

    public void Play(String event)
    {
        _Player.SendPlay(event);
    }

    public void PlayAngle(String event,double tmpAngle,double tmpVertical)
    {
        try {
            var rootObject = _Player.FindEffectByUuid(event);

            double angle = (tmpAngle - 22.5f) > 0 ? tmpAngle - 22.5 : 360 - tmpAngle;
            int horCount = (int) (angle / 45) + 1;

            int verCount = tmpVertical > 0.1 ? -4 : tmpVertical < 0 ? 8 : 0;


            for (TrackObject track : rootObject.trackList) {
                if (track.action_type == ActionType.Shake) {
                    for (int i = 0; i < track.index.length; i++) {
                        if (verCount != 0) {
                            track.index[i] += verCount;
                        }
                        if (horCount < 8) {
                            if (track.index[i] < 50) {
                                int remainder = track.index[i] % 4;
                                if (horCount <= remainder) {
                                    track.index[i] = track.index[i] - horCount;
                                } else if (horCount <= (remainder + 4)) {
                                    int num1 = horCount - remainder;
                                    track.index[i] = track.index[i] - remainder + 99 + num1;
                                } else {
                                    track.index[i] = track.index[i] + 2;
                                }
                            } else {
                                int remainder = 3 - (track.index[i] % 4);
                                if (horCount <= remainder) {
                                    track.index[i] = track.index[i] + horCount;
                                } else if (horCount <= (remainder + 4)) {
                                    int num1 = horCount - remainder;
                                    track.index[i] = track.index[i] + remainder - 99 - num1;
                                } else {
                                    track.index[i] = track.index[i] - 2;
                                }
                            }
                        }
                    }
                    if (track.index != null) {
                        track.index = Arrays.stream(track.index)
                                .filter(i -> !(i < 0 || (i > 19 && i < 100) || i > 119))
                                .toArray();
                    }
                } else if (track.action_type == ActionType.Electrical) {
                    for (int i = 0; i < track.index.length; i++) {
                        if (horCount <= 4) {
                            track.index[i] = 0;
                        } else {
                            track.index[i] = 100;
                        }
                        if (horCount == 1 || horCount == 8 || horCount == 4 || horCount == 5) {
                            track.index = new int[]{0, 100};
                        }
                    }
                }
            }
            _Player.SendPlayEffectByContent(rootObject);
        } catch (Exception ex) {
            System.out.println("TrueGear Mod PlayAngle Error: " + ex.getMessage());
            _Player.SendPlay(event);
        }
    }


    public void PlayRandom(String event, int[][] tmpElectricals, int[] tmpRandomCounts)
    {
        try{
            int[] randomCounts =  tmpRandomCounts.clone();
            int[][] electricals = new int[tmpElectricals.length][];
            for (int i = 0; i < tmpElectricals.length; i++) {
                electricals[i] = tmpElectricals[i].clone();
            }
            Random rand = new Random();

            int randomSum = 0;
            for (int random : randomCounts) {
                randomSum += random;
            }

            var rootObject = _Player.FindEffectByUuid(event);
            int columns = electricals[0].length;

            for (TrackObject track : rootObject.trackList) {
                if (track.action_type == ActionType.Shake) {
                    if (track.index.length < randomSum) {
                        track.index = new int[randomSum];
                    }
                    int j = 0;
                    for (int i = 0; i < track.index.length; i++) {
                        if (randomCounts[j] > 1) {
                            int randomCol = rand.nextInt(columns);
                            while (electricals[j][randomCol] == -1) {
                                randomCol = rand.nextInt(columns);
                            }
                            track.index[i] = electricals[j][randomCol];
                            electricals[j][randomCol] = -1;
                            randomCounts[j]--;
                        } else {
                            int randomCol = rand.nextInt(columns);
                            while (electricals[j][randomCol] == -1) {
                                randomCol = rand.nextInt(columns);
                            }
                            track.index[i] = electricals[j][randomCol];
                            electricals[j][randomCol] = -1;
                            randomCounts[j]--;
                            j++;
                        }
                    }
                }else if (track.action_type == ActionType.Electrical) {
                    for (int i = 0; i < track.index.length; i++) {
                        int index = rand.nextInt(2);
                        if (index == 0) {
                            track.index[i] = index;
                        } else {
                            track.index[i] = 100;
                        }
                    }
                }
            }
            _Player.SendPlayEffectByContent(rootObject);
        } catch (Exception ex) {
            System.out.println("TrueGear Mod PlayAngle Error: " + ex.getMessage());
            _Player.SendPlay(event);
        }


    }




}
