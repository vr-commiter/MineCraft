package net.Minecraft.TrueGear;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.extensions.IForgeDimensionSpecialEffects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import net.minecraftforge.client.event.sound.PlaySoundEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Minecraft_TrueGear.MODID)
public class Minecraft_TrueGear
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "minecrafttruegear";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    private static UUID localUUid = null;
    private static MyTrueGear _TrueGear = null;
    private static Integer useCount = (Integer) 0;
    private static InteractionHand entityHand = InteractionHand.MAIN_HAND;
    private static InteractionHand lastHand = InteractionHand.MAIN_HAND;
    private static boolean canCreateFluid = false;
    private static InteractionHand useItemHand = InteractionHand.MAIN_HAND;
    private static boolean canUseItem = false;
    private static useItemType tickUseItemType = null;
    private static boolean canRightHandInteraction = false;
    private static boolean isFallDamage = false;
    private static boolean canMelee = true;
    private static double lastFallDamage = 0;
    private static InteractionHand meleeHand = InteractionHand.MAIN_HAND;

    private static ItemStack mainHandItem = null;
    private static ItemStack offHandItem = null;

    private static boolean isShield = false;

    private enum useItemType
    {
        LeftHandPullBow,
        RightHandPullBow,
        LeftHandPullCrossBow,
        RightHandPullCrossBow,
        LeftHandPullTrident,
        RightHandPullTrident,
        GoatHorn,
        LeftShield,
        RightShield,
        Food
    }

    private List<String> itemList = Arrays.asList("splash_potion", "lingering_potion", "fishing_rod");;

    BlockPos playerBlockPos = null;
    Integer rainCount = (Integer) 0;

    int[][] rainRandomEle = {
            {0,1},
            {2,3},
            {100,101},
            {102,103}
    } ;
    int[] rainRandomCount = {1,1,1,1};

    boolean canBreak = false;
    long leftClickTime = 0;



    public Minecraft_TrueGear()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        _TrueGear = new MyTrueGear();

        LOGGER.info("---------------------------------------");
        LOGGER.info("TrueGear Mod Is Loaded");
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        _TrueGear.Play("HeartBeat");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    @SubscribeEvent
    public void onEntityItemPickup(EntityItemPickupEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(event.getEntity().getInventory().getFreeSlot() == -1 && event.getEntity().getInventory().getSlotWithRemainingSpace(event.getItem().getItem()) == -1)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("PlayerPickupItem");
        _TrueGear.Play("PlayerPickupItem");
    }

    @SubscribeEvent
    public void onArrowLoose(ArrowLooseEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }

        LOGGER.info("---------------------------------------");

        if(tickUseItemType == useItemType.LeftHandPullBow)
        {
            LOGGER.info("LeftHandBowShoot");
            _TrueGear.Play("LeftHandBowShoot");
        }
        else if(tickUseItemType == useItemType.RightHandPullBow)
        {
            LOGGER.info("RightHandBowShoot");
            _TrueGear.Play("RightHandBowShoot");
        }
        else if(tickUseItemType == useItemType.LeftHandPullCrossBow)
        {
            LOGGER.info("LeftHandCrossBowShoot");
            _TrueGear.Play("LeftHandCrossBowShoot");
        }
        else if(tickUseItemType == useItemType.RightHandPullCrossBow)
        {
            LOGGER.info("RightHandCrossBowShoot");
            _TrueGear.Play("RightHandCrossBowShoot");
        }
    }

//    @SubscribeEvent
//    public void onArrowNock(ArrowNockEvent event)
//    {
//        if(event.getEntity().getUUID() != localUUid)
//        {
//            return;
//        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info("ArrowNockEvent");
//        LOGGER.info(String.valueOf(event.getHand()));
//    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(!canMelee)
        {
            canMelee = true;
            return;
        }
        canMelee = false;
        canRightHandInteraction = false;
        if(isRightHandAttack)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandMeleeHit");
            _TrueGear.Play("RightHandMeleeHit");
        }
        else
        {
            if(String.valueOf(event.getEntity().getMainHandItem()).contains("sword") && String.valueOf(event.getEntity().getOffhandItem()).contains("sword"))
            {
                LOGGER.info("RightHandMeleeHit");
                _TrueGear.Play("RightHandMeleeHit");
                LOGGER.info("LeftHandMeleeHit");
                _TrueGear.Play("LeftHandMeleeHit");
            }
            else if(String.valueOf(event.getEntity().getMainHandItem()).contains("sword"))
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("RightHandMeleeHit");
                _TrueGear.Play("RightHandMeleeHit");
            }
            else if(String.valueOf(event.getEntity().getOffhandItem()).contains("sword"))
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("LeftHandMeleeHit");
                _TrueGear.Play("LeftHandMeleeHit");
            }
        }
        isRightHandAttack = false;


        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem().getUseDuration()));
        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem().getCount()));
        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem().getItem().getBarColor(event.getEntity().getMainHandItem())));
        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem().getItem().getBarWidth(event.getEntity().getMainHandItem())));

    }

    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }

        if(tickUseItemType == useItemType.Food)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("Deglutition");
            _TrueGear.Play("Deglutition");
        }
    }


    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("WakeUp");
        _TrueGear.Play("WakeUp");
    }

//    @SubscribeEvent
//    public void onPlayerSleepInBed(PlayerSleepInBedEvent event)
//    {
//        if(event.getEntity().getUUID() != localUUid)
//        {
//            return;
//        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info("PlayerSleepInBedEvent");
//    }

    @SubscribeEvent
    public void onSleepingTimeCheck(SleepingTimeCheckEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(event.getEntity().getSleepTimer() == 1)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("Sleeping");
            _TrueGear.Play("Sleeping");
            LOGGER.info(String.valueOf(event.getResult()));
            LOGGER.info(String.valueOf(event.getEntity().getSleepTimer()));
        }



    }

    @SubscribeEvent
    public void onPlayerXp(PlayerXpEvent.PickupXp event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("PlayerPickupXp");
        _TrueGear.Play("PlayerPickupXp");
    }


    long lastTimeMillis = 0;

    @SubscribeEvent
    public void PlayerFlyableFallEvent(PlayerFlyableFallEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(System.currentTimeMillis() - lastTimeMillis < 110)
        {
            return;
        }
        lastTimeMillis = System.currentTimeMillis();
//        if(lastFallDamage == event.getDistance())
//        {
//            lastFallDamage = 0;
//            return;
//        }



        lastFallDamage = event.getDistance();
        if(event.getDistance() < 4.0)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("Fall");
            _TrueGear.Play("Fall");
            LOGGER.info(String.valueOf(event.getDistance()));
            return;
        }
        isFallDamage = true;
        LOGGER.info("---------------------------------------");
        LOGGER.info("FallDamage");
        _TrueGear.Play("FallDamage");
        LOGGER.info(String.valueOf(event.getDistance()));
        LOGGER.info(String.valueOf(event.getEntity().getUUID()));
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(System.currentTimeMillis() - lastTimeMillis < 110)
        {
            return;
        }
        lastTimeMillis = System.currentTimeMillis();
//        if(lastFallDamage == event.getDistance())
//        {
//            lastFallDamage = 0;
//            return;
//        }

        lastFallDamage = event.getDistance();
        if(event.getDistance() < 4.0)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("Fall");
            _TrueGear.Play("Fall");
            LOGGER.info(String.valueOf(event.getDistance()));
            return;
        }
        isFallDamage = true;
        LOGGER.info("---------------------------------------");
        LOGGER.info("FallDamage");
        _TrueGear.Play("FallDamage");
        LOGGER.info(String.valueOf(event.getDistance()));
        LOGGER.info(String.valueOf(event.getEntity().shouldRiderSit()));
    }

    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event)
    {
        LOGGER.info("---------------------------------------");
        LOGGER.info("ShieldBlockEvent");
        _TrueGear.Play("ShieldBlock");
    }
    private static boolean isRightHandAttack = false;
    @SubscribeEvent
    public void InteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event)
    {
        LOGGER.info("---------------------------------------");
        LOGGER.info("InteractionKeyMappingTriggered");
        LOGGER.info("IsUseItem :" + event.isUseItem());
        LOGGER.info("isAttack :" + event.isAttack());
        LOGGER.info("isPickBlock :" + event.isPickBlock());;
        LOGGER.info("Hand :" + event.getHand());

        useItemHand = event.getHand();

        if(event.isAttack())
        {
            if(event.getHand() == InteractionHand.MAIN_HAND)
            {
                isRightHandAttack = true;
            }
            meleeHand = event.getHand();
        }
        if(!event.isUseItem())
        {
            return;
        }

        if(event.getHand() == InteractionHand.MAIN_HAND)
        {
            canRightHandInteraction = true;
        }
//        else if(event.getHand() == InteractionHand.OFF_HAND)
//        {
//            LOGGER.info("---------------------------------------");
//            LOGGER.info("LeftHandPickupItem");
//            _TrueGear.Play("LeftHandPickupItem");
//        }

    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event)
    {

        String soundName = event.getName();
//        if(canRightHandInteraction && (soundName.contains(".open") || soundName.contains(".close") || soundName.contains("click")))
//        {
//            LOGGER.info("---------------------------------------");
//            LOGGER.info("RightHandInteraction");
//            _TrueGear.Play("RightHandInteraction");
//            LOGGER.info(event.getName());
//            canRightHandInteraction = false;
//        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info(soundName);
    }


//    @SubscribeEvent
//    public void LivingAttackEvent(LivingAttackEvent event)
//    {
//        if(event.getEntity().getUUID() != localUUid)
//        {
//            return;
//        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info("LivingAttackEvent");
//    }



    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickItem event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("RightClickItem");
        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem()));;
        LOGGER.info(String.valueOf(event.getEntity().getOffhandItem()));
        canRightHandInteraction = false;

        if(useItemHand == InteractionHand.MAIN_HAND)
        {
            for (String item :itemList)
            {
                if(String.valueOf(event.getEntity().getMainHandItem()).contains(item))
                {
                    LOGGER.info("---------------------------------------");
                    LOGGER.info("RightHandThrowItem");
                    _TrueGear.Play("RightHandThrowItem");
                }
            }
        }
        else if (useItemHand == InteractionHand.OFF_HAND)
        {
            for (String item :itemList)
            {
                if(String.valueOf(event.getEntity().getOffhandItem()).contains(item))
                {
                    LOGGER.info("---------------------------------------");
                    LOGGER.info("LeftHandThrowItem");
                    _TrueGear.Play("LeftHandThrowItem");
                }
            }
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info(String.valueOf(event.getHand()));
        LOGGER.info(String.valueOf(event.getItemStack()));
        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem()));
        LOGGER.info(String.valueOf(event.getEntity().getOffhandItem()));
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }

        entityHand = event.getHand();
        LOGGER.info("---------------------------------------");
        LOGGER.info("RightClickBolck");
        LOGGER.info(String.valueOf(event));

        LOGGER.info(String.valueOf(event.getUseItem()));
        if(entityHand == InteractionHand.MAIN_HAND)
        {
            if (String.valueOf(event.getUseItem()) == String.valueOf(event.getEntity().getMainHandItem()))
            {
                canCreateFluid = true;
            }
        }
        else
        {
            if (String.valueOf(event.getUseItem()) == String.valueOf(event.getEntity().getOffhandItem()))
            {
                canCreateFluid = true;
            }
        }
        if(lastHand == event.getHand() && lastHand == InteractionHand.MAIN_HAND)
        {
            lastHand = InteractionHand.OFF_HAND;
            return;
        }
        lastHand = event.getHand();
    }

//    @SubscribeEvent
//    public void onPlayerInteract(PlayerInteractEvent.RightClickEmpty event)
//    {
//        if(event.getEntity().getUUID() != localUUid)
//        {
//            return;
//        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info("RightClickEmpty");
//        LOGGER.info(String.valueOf(event.getHand()));
//        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem()));
//        LOGGER.info(String.valueOf(event.getEntity().getOffhandItem()));
//    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
//        LOGGER.info("---------------------------------------");
//        LOGGER.info("currentTimeMillis :" + String.valueOf(currentTimeMillis));
        if(currentTimeMillis - leftClickTime < 120)
        {
            return;
        }
        leftClickTime = currentTimeMillis;
//        LOGGER.info("leftClickTime :" + String.valueOf(leftClickTime));
        canRightHandInteraction = false;
        if(isRightHandAttack)
        {
            canBreak = true;
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandPickupItem1");
            _TrueGear.Play("RightHandHitBlock");
        }
        else
        {
            if((String.valueOf(event.getEntity().getMainHandItem()).contains("axe") || String.valueOf(event.getEntity().getMainHandItem()).contains("shovel")) && ( String.valueOf(event.getEntity().getOffhandItem()).contains("axe") || String.valueOf(event.getEntity().getOffhandItem()).contains("shovel")))
            {
                LOGGER.info("RightHandPickupItem");
                _TrueGear.Play("RightHandHitBlock");
                LOGGER.info("LeftHandPickupItem");
                _TrueGear.Play("LeftHandHitBlock");
            }
            else if(String.valueOf(event.getEntity().getMainHandItem()).contains("axe") || String.valueOf(event.getEntity().getMainHandItem()).contains("shovel"))
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("RightHandPickupItem");
                _TrueGear.Play("RightHandHitBlock");
            }
            else if(String.valueOf(event.getEntity().getOffhandItem()).contains("axe") || String.valueOf(event.getEntity().getOffhandItem()).contains("shovel"))
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("LeftHandPickupItem");
                _TrueGear.Play("LeftHandHitBlock");
            }
        }

        isRightHandAttack = false;
//        LOGGER.info(String.valueOf(event.getHand()));
//        LOGGER.info(String.valueOf(event.getEntity().getUsedItemHand()));
//        LOGGER.info(String.valueOf(event.getSide()));
//        LOGGER.info(String.valueOf(event.getItemStack()));
//        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem()));
//        LOGGER.info(String.valueOf(event.getEntity().getOffhandItem()));
    }

    @SubscribeEvent
    public void onBlock(ExplosionEvent.Detonate event)
    {
        var entities = event.getAffectedEntities();
        for(var entity : entities)
        {
            if(entity.getUUID() != localUUid)
            {
                return;
            }
            LOGGER.info("---------------------------------------");
            LOGGER.info("Explosion");
            _TrueGear.Play("Explosion");
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            if(event.getSource().getEntity() != null)
            {
                if(event.getSource().getEntity().getUUID() == localUUid)
                {
                    LOGGER.info(event.getSource().toString());
                    LOGGER.info(event.getSource().getMsgId());
                }
            }
            return;
        }
        if(isFallDamage)
        {
            isFallDamage = false;
            return;
        }
        if(event.getSource() == null || event.getSource().getSourcePosition() == null)
        {
            if(isShield)
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("ShieldPoisonDamage");
                _TrueGear.Play("ShieldPoisonDamage");
                return;
            }
            LOGGER.info("---------------------------------------");
            LOGGER.info("PoisonDamage");
            _TrueGear.Play("PoisonDamage");
            return;
        }

        double angle =  GetAngle(event.getEntity(),event.getSource());
        if(isShield)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("ShieldDefaultDamage," + angle + ",0");
            _TrueGear.PlayAngle("ShieldDefaultDamage",angle,0);
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("DefaultDamage," + angle + ",0");
        _TrueGear.PlayAngle("DefaultDamage",angle,0);
        LOGGER.info(String.valueOf(event.getEntity().isControlledByLocalInstance()));
        LOGGER.info(String.valueOf(event.getEntity().getUUID()));
        LOGGER.info(String.valueOf(angle));
        LOGGER.info(String.valueOf(event.getEntity()));
        LOGGER.info(String.valueOf(event.getSource().getEntity()));
    }

    public double GetAngle(LivingEntity player, DamageSource source)
    {
        Vec3 playerToDamageSource = source.getSourcePosition().subtract(player.position()).normalize();
        Vec3 playerForward = player.getForward().normalize();


        double dotProduct = playerForward.dot(playerToDamageSource);
        double angle = Math.toDegrees(Math.acos(dotProduct));

        Vec3 crossProduct = playerForward.cross(playerToDamageSource);

        if (crossProduct.y < 0) {
            angle = 360 - angle;
        }

        return angle;
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("PlayerDeath");
        _TrueGear.Play("PlayerDeath");
        isShield = false;
        LOGGER.info(String.valueOf(event.getEntity()));
    }

    public useItemType CheckType(ItemStack nowItem,ItemStack rightItem,ItemStack leftItem)
    {
        useItemType useType = null;

        if (String.valueOf(nowItem).contains("crossbow"))
        {
            if (nowItem == rightItem)
            {
                useType = useItemType.RightHandPullCrossBow;
            }
            else if (nowItem == leftItem)
            {
                useType = useItemType.LeftHandPullCrossBow;
            }
        }
        else if (String.valueOf(nowItem).contains("bow"))
        {
            if (nowItem == rightItem)
            {
                useType = useItemType.RightHandPullBow;
            }
            else if (nowItem == leftItem)
            {
                useType = useItemType.LeftHandPullBow;
            }
        }
        else if(String.valueOf(nowItem).contains("trident"))
        {
            if (nowItem == rightItem)
            {
                useType = useItemType.RightHandPullTrident;
            }
            else if (nowItem == leftItem)
            {
                useType = useItemType.LeftHandPullTrident;
            }
        }
        else if(String.valueOf(nowItem).contains("shield"))
        {
            if (nowItem == rightItem)
            {
                useType = useItemType.RightShield;
            }
            else if (nowItem == leftItem)
            {
                useType = useItemType.LeftShield;
            }
        }
        else if(String.valueOf(nowItem).contains("goathorn"))
        {
            useType = useItemType.GoatHorn;
        }
        else
        {
            useType = useItemType.Food;
        }

        return useType;
    }

    @SubscribeEvent
    public void onStartUse(LivingEntityUseItemEvent.Start event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        tickUseItemType = CheckType(event.getItem(),event.getEntity().getMainHandItem(),event.getEntity().getOffhandItem());
        if (tickUseItemType == useItemType.GoatHorn)
        {
            LOGGER.info("GoatHorn");
            _TrueGear.Play("GoatHorn");
        }
        else if (tickUseItemType == useItemType.LeftShield)
        {
            isShield = true;
            LOGGER.info("LeftHandShield");
            _TrueGear.Play("LeftHandShield");
        }
        else if(tickUseItemType == useItemType.RightShield)
        {
            isShield = true;
            if(canRightHandInteraction)
            {
                LOGGER.info("RightHandShield");
                _TrueGear.Play("RightHandShield");
            }
            else
            {
                if(String.valueOf(event.getEntity().getMainHandItem()).contains("shield") && String.valueOf(event.getEntity().getOffhandItem()).contains("shield"))
                {
                    LOGGER.info("RightHandShield");
                    _TrueGear.Play("RightHandShield");
                    LOGGER.info("LeftHandShield");
                    _TrueGear.Play("LeftHandShield");
                }
                else if(String.valueOf(event.getEntity().getMainHandItem()).contains("shield"))
                {
                    LOGGER.info("RightHandShield");
                    _TrueGear.Play("RightHandShield");
                }
                else
                {
                    LOGGER.info("LeftHandShield");
                    _TrueGear.Play("LeftHandShield");
                }
            }
        }

        canRightHandInteraction = false;
        useCount = (Integer) 0;
//        LOGGER.info("StartUsed");
//        LOGGER.info(String.valueOf(event.getItem()));
//        LOGGER.info(String.valueOf(event.getEntity().getMainHandItem()));
//        LOGGER.info(String.valueOf(event.getEntity().getOffhandItem()));
    }

    @SubscribeEvent
    public void onStopUse(LivingEntityUseItemEvent.Stop event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if (tickUseItemType == useItemType.LeftShield)
        {
            isShield = false;
        }
        else if (tickUseItemType == useItemType.RightShield)
        {
            isShield = false;
        }
        if(useCount > 9)
        {
            if(tickUseItemType == useItemType.LeftHandPullTrident)
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("LeftHandThrowItem");
                _TrueGear.Play("LeftHandThrowItem");
            }
            else if (tickUseItemType == useItemType.RightHandPullTrident)
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("RightHandThrowItem");
                _TrueGear.Play("RightHandThrowItem");
            }
        }
        useCount = (Integer) 0;
    }

    @SubscribeEvent
    public void onFinishUse(LivingEntityUseItemEvent.Finish event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        useCount = (Integer) 0;
        if(tickUseItemType == useItemType.Food)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("Deglutition");
            _TrueGear.Play("Deglutition");
        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info(String.valueOf(event.getItem()));
    }

    @SubscribeEvent
    public void onTickUse(LivingEntityUseItemEvent.Tick event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(useCount % 10 == 0)
        {
            tickUseItemType = CheckType(event.getItem(),event.getEntity().getMainHandItem(),event.getEntity().getOffhandItem());
            if(tickUseItemType == useItemType.Food)
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("Eating");
                _TrueGear.Play("Eating");
            }
            else
            {
                Integer eventNum = (Integer) (useCount / 10 + 1);
                if(eventNum > 3)
                {
                    eventNum = (Integer) 3;
                }
                else if(eventNum == 3)
                {
                    eventNum = (Integer) 2;
                }
                LOGGER.info("---------------------------------------");
                LOGGER.info(String.valueOf(tickUseItemType) + eventNum);
                _TrueGear.Play(String.valueOf(tickUseItemType)  + eventNum);
                LOGGER.info(String.valueOf(event.getItem()));
                LOGGER.info(String.valueOf(useCount));
            }
        }
        useCount++;
    }

    @SubscribeEvent
    public void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event)
    {
        LOGGER.info("---------------------------------------");
        LOGGER.info("PlayerLoggingIn");
        localUUid = event.getPlayer().getUUID();
        LOGGER.info(String.valueOf(event.getPlayer().getUUID()));
    }




    @SubscribeEvent
    public void onLoggingIn(BlockEvent.BreakEvent event)
    {
        if(event.getPlayer().getUUID() != localUUid)
        {
            return;
        }
        canRightHandInteraction = false;
        LOGGER.info("---------------------------------------");
        LOGGER.info("RightHandBreakItem2");
        if(canBreak)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandBreakItem1");
            _TrueGear.Play("RightHandBreakItem");
            canBreak = false;
            isRightHandAttack = false;
            LOGGER.info(String.valueOf(event.getPlayer().getUUID()));
            return;
        }
        if(isRightHandAttack)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandBreakItem");
            _TrueGear.Play("RightHandBreakItem");
        }
        else
        {
            if((String.valueOf(event.getPlayer().getMainHandItem()).contains("axe") || String.valueOf(event.getPlayer().getMainHandItem()).contains("shovel")) && ( String.valueOf(event.getPlayer().getOffhandItem()).contains("axe") || String.valueOf(event.getPlayer().getOffhandItem()).contains("shovel")))
            {
                LOGGER.info("RightHandBreakItem");
                _TrueGear.Play("RightHandBreakItem");
                LOGGER.info("LeftHandBreakItem");
                _TrueGear.Play("LeftHandBreakItem");
            }
            else if(String.valueOf(event.getPlayer().getMainHandItem()).contains("axe") || String.valueOf(event.getPlayer().getMainHandItem()).contains("shovel"))
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("RightHandBreakItem");
                _TrueGear.Play("RightHandBreakItem");
            }
            else if(String.valueOf(event.getPlayer().getOffhandItem()).contains("axe") || String.valueOf(event.getPlayer().getOffhandItem()).contains("shovel"))
            {
                LOGGER.info("---------------------------------------");
                LOGGER.info("LeftHandBreakItem");
                _TrueGear.Play("LeftHandBreakItem");
            }
        }

        isRightHandAttack = false;
        LOGGER.info(String.valueOf(event.getPlayer().getUUID()));
    }

    @SubscribeEvent
    public void onLoggingIn(BlockEvent.EntityPlaceEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        canRightHandInteraction = false;
        if(entityHand == InteractionHand.MAIN_HAND)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandCreateItem");
            _TrueGear.Play("RightHandPickupItem");
        }
        else
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("LeftHandCreateItem");
            _TrueGear.Play("LeftHandPickupItem");
        }
    }

    @SubscribeEvent
    public void Opening(ScreenEvent.Opening event)
    {
        if(event.getNewScreen().isPauseScreen() || !event.getNewScreen().toString().contains(".inventory"))
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("Opening");
        _TrueGear.Play("RightHandInteraction");
        LOGGER.info( event.getNewScreen().toString());
        LOGGER.info(String.valueOf(event.getNewScreen().isPauseScreen()));
        canRightHandInteraction = false;
    }

    @SubscribeEvent
    public void CreateFluidSourceEvent(BlockEvent.CreateFluidSourceEvent event)
    {
        if (!canCreateFluid)
        {
            return;
        }
        canCreateFluid = false;
        canRightHandInteraction = false;
        if(entityHand == InteractionHand.MAIN_HAND)
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandCreateFluid");
            _TrueGear.Play("RightHandCreateFluid");
        }
        else
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("LeftHandCreateFluid");
            _TrueGear.Play("LeftHandCreateFluid");
        }
    }

    @SubscribeEvent
    public void ItemTossEvent(ItemTossEvent event)
    {
        if(event.getPlayer().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info("PlayerTossItem");
        _TrueGear.Play("PlayerTossItem");
    }

    @SubscribeEvent
    public void FillBucketEvent(FillBucketEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        if(String.valueOf(event.getEntity().getMainHandItem()).contains("bucket"))
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandFillBucket");
            _TrueGear.Play("RightHandFillBucket");
        }
        else
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("LeftHandFillBucket");
            _TrueGear.Play("LeftHandFillBucket");
        }
    }

    @SubscribeEvent
    public void TickEvent(TickEvent.LevelTickEvent event)
    {
        if(playerBlockPos == null)
        {
            return;
        }
        if(event.level.isRainingAt(playerBlockPos))
        {
            rainCount++;
            if(rainCount == 40)
            {
                rainCount = (Integer) 0;
                _TrueGear.PlayRandom("Rain",rainRandomEle,rainRandomCount);
            }
        }
    }


    @SubscribeEvent
    public void TickEvent(TickEvent.PlayerTickEvent event)
    {
        if(event.player.getUUID() != localUUid)
        {
            return;
        }
//        LOGGER.info("---------------------------------------");
//        LOGGER.info("LevelTickEvent");
//        LOGGER.info(String.valueOf(event.player.getUUID()));
//        LOGGER.info(String.valueOf(event.player.blockPosition()));
        playerBlockPos = event.player.blockPosition();
    }


    @SubscribeEvent
    public void LivingEquipmentChangeEvent(LivingEquipmentChangeEvent event)
    {
        if(event.getEntity().getUUID() != localUUid)
        {
            return;
        }
        LOGGER.info("---------------------------------------");
        LOGGER.info(String.valueOf(event.getSlot()));
        if(String.valueOf(event.getSlot()).contains("CHEST") || String.valueOf(event.getSlot()).contains("HEAD") || String.valueOf(event.getSlot()).contains("LEGS") || String.valueOf(event.getSlot()).contains("FEET"))
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("ChangeEquipment");
            _TrueGear.Play("ChangeEquipment");
        }
        if(String.valueOf(event.getSlot()).contains("MAINHAND"))
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("RightHandPickupItem");
            _TrueGear.Play("RightHandPickupItem");
        }
        else if(String.valueOf(event.getSlot()).contains("OFFHAND"))
        {
            LOGGER.info("---------------------------------------");
            LOGGER.info("LeftHandPickupItem");
            _TrueGear.Play("LeftHandPickupItem");
        }
    }

}
