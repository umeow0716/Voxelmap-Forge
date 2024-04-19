// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.animal.MushroomCow;

import java.util.stream.StreamSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Properties;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.io.IOException;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import com.mamiyaotaru.voxelmap.textures.StitcherException;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import java.awt.image.BufferedImage;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.UUID;
import java.lang.reflect.Method;
import java.util.Map;
import java.lang.reflect.Field;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import java.util.HashMap;
import com.mamiyaotaru.voxelmap.util.Contact;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.textures.FontRendererWithAtlas;
import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IRadar;

public class Radar implements IRadar
{
    private Minecraft game;
    private IVoxelMap master;
    private LayoutVariables layoutVariables;
    public MapSettingsManager minimapOptions;
    public RadarSettingsManager options;
    private FontRendererWithAtlas fontRenderer;
    private TextureAtlas textureAtlas;
    private boolean newMobs;
    private boolean enabled;
    private boolean completedLoading;
    private int timer;
    private float direction;
    private ArrayList<Contact> contacts;
    public HashMap<String, Integer> mpContactsSkinGetTries;
    public HashMap<String, Integer> contactsSkinGetTries;
    private Sprite clothIcon;
    private static final int UNKNOWN;
    private String[] armorNames;
    private boolean randomobsOptifine;
    private Class<?> randomEntitiesClass;
    private Field mapPropertiesField;
    private Map<String, ?> mapProperties;
    private Field randomEntityField;
    private Object randomEntity;
    private Class<?> iRandomEntityClass;
    private Class<?> randomEntityClass;
    private Method setEntityMethod;
    private Class<?> randomEntitiesPropertiesClass;
    private Method getEntityTextureMethod;
    private boolean hasCustomNPCs;
    private Class<?> entityCustomNpcClass;
    private Class<?> modelDataClass;
    private Class<?> entityNPCInterfaceClass;
    private Field modelDataField;
    private Method getEntityMethod;
    private boolean lastOutlines;
    UUID devUUID;
    private SkullModel playerSkullModel;
    private HumanoidModel<? extends LivingEntity> bipedArmorModel;
    private SkeletonModel<? extends Skeleton> strayOverlayModel;
    private DrownedModel<? extends Zombie> drownedOverlayModel;
    private HumanoidModel<? extends LivingEntity> piglinArmorModel;
    private DynamicTexture nativeBackedTexture;
    private final ResourceLocation nativeBackedTextureLocation;
    private final Vector3f fullbright;
    private static final Int2ObjectMap<ResourceLocation> LEVEL_TO_ID;
    private static final Map<Markings, ResourceLocation> TEXTURES;
    
    public Radar(final IVoxelMap master) {
        this.master = null;
        this.layoutVariables = null;
        this.minimapOptions = null;
        this.options = null;
        this.newMobs = false;
        this.enabled = true;
        this.completedLoading = false;
        this.timer = 500;
        this.direction = 0.0f;
        this.contacts = new ArrayList<Contact>(40);
        this.mpContactsSkinGetTries = new HashMap<String, Integer>();
        this.contactsSkinGetTries = new HashMap<String, Integer>();
        this.clothIcon = null;
        this.armorNames = new String[] { "cloth", "clothOverlay", "clothOuter", "clothOverlayOuter", "chain", "iron", "gold", "diamond", "netherite", "turtle" };
        this.randomobsOptifine = false;
        this.randomEntitiesClass = null;
        this.mapPropertiesField = null;
        this.mapProperties = null;
        this.randomEntityField = null;
        this.randomEntity = null;
        this.iRandomEntityClass = null;
        this.randomEntityClass = null;
        this.setEntityMethod = null;
        this.randomEntitiesPropertiesClass = null;
        this.getEntityTextureMethod = null;
        this.hasCustomNPCs = false;
        this.entityCustomNpcClass = null;
        this.modelDataClass = null;
        this.entityNPCInterfaceClass = null;
        this.modelDataField = null;
        this.getEntityMethod = null;
        this.lastOutlines = true;
        this.devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
        this.nativeBackedTexture = new DynamicTexture(2, 2, false);
        this.nativeBackedTextureLocation = new ResourceLocation("voxelmap", "tempimage");
        this.fullbright = new Vector3f(1.0f, 1.0f, 1.0f);
        this.master = master;
        this.minimapOptions = master.getMapOptions();
        this.options = master.getRadarOptions();
        this.game = Minecraft.getInstance();
        this.fontRenderer = new FontRendererWithAtlas(this.game.getTextureManager(), new ResourceLocation("textures/font/ascii.png"));
        (this.textureAtlas = new TextureAtlas("mobs")).setFilter(false, false);
        try {
            this.randomEntitiesClass = Class.forName("net.optifine.RandomEntities");
            (this.mapPropertiesField = this.randomEntitiesClass.getDeclaredField("mapProperties")).setAccessible(true);
            this.mapProperties = (Map)this.mapPropertiesField.get(null);
            (this.randomEntityField = this.randomEntitiesClass.getDeclaredField("randomEntity")).setAccessible(true);
            this.randomEntity = this.randomEntityField.get(null);
            this.iRandomEntityClass = Class.forName("net.optifine.IRandomEntity");
            this.randomEntityClass = Class.forName("net.optifine.RandomEntity");
            final Class<?>[] argClasses1 = { Entity.class };
            this.setEntityMethod = this.randomEntityClass.getDeclaredMethod("setEntity", argClasses1);
            this.randomEntitiesPropertiesClass = Class.forName("net.optifine.RandomEntityProperties");
            final Class<?>[] argClasses2 = { ResourceLocation.class, this.iRandomEntityClass };
            this.getEntityTextureMethod = this.randomEntitiesPropertiesClass.getDeclaredMethod("getTextureLocation", argClasses2);
            this.randomobsOptifine = true;
        }
        catch (final ClassNotFoundException e) {
            this.randomobsOptifine = false;
        }
        catch (final NoSuchMethodException e2) {
            this.randomobsOptifine = false;
        }
        catch (final NoSuchFieldException e3) {
            this.randomobsOptifine = false;
        }
        catch (final SecurityException e4) {
            this.randomobsOptifine = false;
        }
        catch (final IllegalArgumentException e5) {
            this.randomobsOptifine = false;
        }
        catch (final IllegalAccessException e6) {
            this.randomobsOptifine = false;
        }
        try {
            this.entityCustomNpcClass = Class.forName("noppes.npcs.entity.EntityCustomNpc");
            this.modelDataClass = Class.forName("noppes.npcs.ModelData");
            this.modelDataField = this.entityCustomNpcClass.getField("modelData");
            this.entityNPCInterfaceClass = Class.forName("noppes.npcs.entity.EntityNPCInterface");
            this.getEntityMethod = this.modelDataClass.getMethod("getEntity", this.entityNPCInterfaceClass);
            this.hasCustomNPCs = true;
        }
        catch (final ClassNotFoundException e) {
            this.hasCustomNPCs = false;
        }
        catch (final NoSuchFieldException e3) {
            this.hasCustomNPCs = false;
        }
        catch (final NoSuchMethodException e2) {
            this.hasCustomNPCs = false;
        }
    }
    
    @Override
    public void onResourceManagerReload(final ResourceManager resourceManager) {
        this.loadTexturePackIcons();
        this.fontRenderer.onResourceManagerReload(resourceManager);
    }
    
    private void loadTexturePackIcons() {
        this.completedLoading = false;
        try {
            this.mpContactsSkinGetTries.clear();
            this.contactsSkinGetTries.clear();
            this.textureAtlas.reset();
            final LayerDefinition texturedModelData12 = SkullModel.createHumanoidHeadLayer();
            final ModelPart skullModelPart = texturedModelData12.bakeRoot();
            this.playerSkullModel = new SkullModel(skullModelPart);
            final CubeDeformation ARMOR_DILATION = new CubeDeformation(1.0f);
            final LayerDefinition texturedModelData13 = LayerDefinition.create(HumanoidModel.createMesh(ARMOR_DILATION, 0.0f), 64, 32);
            final ModelPart bipedArmorModelPart = texturedModelData13.bakeRoot();
            this.bipedArmorModel = (HumanoidModel<? extends LivingEntity>)new HumanoidModel(bipedArmorModelPart);
            final LayerDefinition strayModelData = LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25f), 0.0f), 64, 32);
            final ModelPart strayOverlayModelPart = strayModelData.bakeRoot();
            this.strayOverlayModel = (SkeletonModel<? extends Skeleton>)new SkeletonModel(strayOverlayModelPart);
            final LayerDefinition drownedModelData = DrownedModel.createBodyLayer(new CubeDeformation(0.25f));
            final ModelPart drownedOverlayModelPart = drownedModelData.bakeRoot();
            this.drownedOverlayModel = (DrownedModel<? extends Zombie>)new DrownedModel(drownedOverlayModelPart);
            final LayerDefinition texturedModelData14 = LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(1.02f), 0.0f), 64, 32);
            final ModelPart piglinArmorModelPart = texturedModelData14.bakeRoot();
            this.piglinArmorModel = (HumanoidModel<? extends LivingEntity>)new HumanoidModel(piglinArmorModelPart);
            if (ReflectionUtils.classExists("com.prupe.mcpatcher.mob.MobOverlay") && ImageUtils.loadImage(new ResourceLocation("mcpatcher/mob/cow/mooshroom_overlay.png"), 0, 0, 1, 1) != null) {
                EnumMobs.MOOSHROOM.secondaryResourceLocation = new ResourceLocation("mcpatcher/mob/cow/mooshroom_overlay.png");
            }
            else {
                EnumMobs.MOOSHROOM.secondaryResourceLocation = new ResourceLocation("textures/block/red_mushroom.png");
            }
            for (int t = 0; t < EnumMobs.values().length - 1; ++t) {
                final String identifier = "minecraft." + EnumMobs.values()[t].id;
                final String identifierSimple = EnumMobs.values()[t].id;
                String spriteName = identifier + EnumMobs.values()[t].resourceLocation.toString();
                spriteName += ((EnumMobs.values()[t].secondaryResourceLocation != null) ? EnumMobs.values()[t].secondaryResourceLocation.toString() : "");
                BufferedImage mobImage = this.getCustomMobImage(identifier, identifierSimple);
                if (mobImage != null) {
                    final Sprite sprite = this.textureAtlas.registerIconForBufferedImage(identifier + "custom", mobImage);
                    this.textureAtlas.registerMaskedIcon(spriteName, sprite);
                }
                else {
                    this.textureAtlas.registerFailedIcon(identifier + "custom");
                    if (EnumMobs.values()[t].expectedWidth > 0.5) {
                        mobImage = this.createImageFromTypeAndResourceLocations(EnumMobs.values()[t], EnumMobs.values()[t].resourceLocation, EnumMobs.values()[t].secondaryResourceLocation, null);
                        if (mobImage != null) {
                            final float scale = mobImage.getWidth() / EnumMobs.values()[t].expectedWidth;
                            mobImage = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(mobImage, 4.0f / scale)), this.options.outlines, 2);
                            this.textureAtlas.registerIconForBufferedImage(spriteName, mobImage);
                        }
                    }
                }
            }
            final BufferedImage[] armorImages = { ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1.png"), 8, 8, 8, 8), ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1.png"), 40, 8, 8, 8), ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1_overlay.png"), 8, 8, 8, 8), ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1_overlay.png"), 40, 8, 8, 8) };
            for (int t2 = 0; t2 < armorImages.length; ++t2) {
                final float scale2 = armorImages[t2].getWidth() / 8.0f;
                armorImages[t2] = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(armorImages[t2], 4.0f / scale2 * 47.0f / 38.0f)), this.options.outlines && t2 != 2 && t2 != 3, true, 37.6f, 37.6f, 2);
                final Sprite icon = this.textureAtlas.registerIconForBufferedImage("armor " + this.armorNames[t2], armorImages[t2]);
                if (t2 == 0) {
                    this.clothIcon = icon;
                }
            }
            BufferedImage zombie = ImageUtils.loadImage(EnumMobs.ZOMBIE.resourceLocation, 8, 8, 8, 8, 64, 64);
            float scale2 = zombie.getWidth() / 8.0f;
            zombie = ImageUtils.scaleImage(zombie, 4.0f / scale2 * 47.0f / 38.0f);
            BufferedImage zombieHat = ImageUtils.loadImage(EnumMobs.ZOMBIE.resourceLocation, 40, 8, 8, 8, 64, 64);
            zombieHat = ImageUtils.scaleImage(zombieHat, 4.0f / scale2 * 47.0f / 35.0f);
            zombie = ImageUtils.addImages(ImageUtils.addImages(new BufferedImage(zombieHat.getWidth(), zombieHat.getHeight() + 8, 6), zombie, (zombieHat.getWidth() - zombie.getWidth()) / 2.0f, (zombieHat.getHeight() - zombie.getHeight()) / 2.0f, zombieHat.getWidth(), zombieHat.getHeight() + 8), zombieHat, 0.0f, 0.0f, zombieHat.getWidth(), zombieHat.getHeight() + 8);
            zombieHat.flush();
            zombie = ImageUtils.fillOutline(ImageUtils.pad(zombie), this.options.outlines, true, 37.6f, 37.6f, 2);
            this.textureAtlas.registerIconForBufferedImage("minecraft." + EnumMobs.ZOMBIE.id + EnumMobs.ZOMBIE.resourceLocation.toString() + "head", zombie);
            BufferedImage skeleton = ImageUtils.loadImage(EnumMobs.SKELETON.resourceLocation, 8, 8, 8, 8, 64, 32);
            scale2 = skeleton.getWidth() / 8.0f;
            skeleton = ImageUtils.scaleImage(skeleton, 4.0f / scale2 * 47.0f / 38.0f);
            skeleton = ImageUtils.addImages(new BufferedImage(skeleton.getWidth(), skeleton.getHeight() + 8, 6), skeleton, 0.0f, 0.0f, skeleton.getWidth(), skeleton.getHeight() + 8);
            skeleton = ImageUtils.fillOutline(ImageUtils.pad(skeleton), this.options.outlines, true, 37.6f, 37.6f, 2);
            this.textureAtlas.registerIconForBufferedImage("minecraft." + EnumMobs.SKELETON.id + EnumMobs.SKELETON.resourceLocation.toString() + "head", skeleton);
            BufferedImage witherSkeleton = ImageUtils.loadImage(EnumMobs.SKELETONWITHER.resourceLocation, 8, 8, 8, 8, 64, 32);
            scale2 = witherSkeleton.getWidth() / 8.0f;
            witherSkeleton = ImageUtils.scaleImage(witherSkeleton, 4.0f / scale2 * 47.0f / 38.0f);
            witherSkeleton = ImageUtils.addImages(new BufferedImage(witherSkeleton.getWidth(), witherSkeleton.getHeight() + 8, 6), witherSkeleton, 0.0f, 0.0f, witherSkeleton.getWidth(), witherSkeleton.getHeight() + 8);
            witherSkeleton = ImageUtils.fillOutline(ImageUtils.pad(witherSkeleton), this.options.outlines, true, 37.6f, 37.6f, 2);
            this.textureAtlas.registerIconForBufferedImage("minecraft." + EnumMobs.SKELETONWITHER.id + EnumMobs.SKELETONWITHER.resourceLocation.toString() + "head", witherSkeleton);
            BufferedImage creeper = ImageUtils.addImages(ImageUtils.blankImage(EnumMobs.CREEPER.resourceLocation, 8, 10), ImageUtils.loadImage(EnumMobs.CREEPER.resourceLocation, 8, 8, 8, 8), 0.0f, 0.0f, 8, 10);
            scale2 = creeper.getWidth() / EnumMobs.CREEPER.expectedWidth;
            creeper = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(creeper, 4.0f / scale2 * 47.0f / 38.0f)), this.options.outlines, true, 37.6f, 37.6f, 2);
            this.textureAtlas.registerIconForBufferedImage("minecraft." + EnumMobs.CREEPER.id + EnumMobs.CREEPER.resourceLocation.toString() + "head", creeper);
            BufferedImage dragon = this.createImageFromTypeAndResourceLocations(EnumMobs.ENDERDRAGON, EnumMobs.ENDERDRAGON.resourceLocation, null, null);
            scale2 = dragon.getWidth() / EnumMobs.ENDERDRAGON.expectedWidth;
            dragon = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(dragon, 4.0f / scale2)), this.options.outlines, true, 32.0f, 32.0f, 2);
            this.textureAtlas.registerIconForBufferedImage("minecraft." + EnumMobs.ENDERDRAGON.id + EnumMobs.ENDERDRAGON.resourceLocation.toString() + "head", dragon);
            BufferedImage sheepFur = ImageUtils.loadImage(new ResourceLocation("textures/entity/sheep/sheep_fur.png"), 6, 6, 6, 6);
            scale2 = sheepFur.getWidth() / 6.0f;
            sheepFur = ImageUtils.scaleImage(sheepFur, 4.0f / scale2 * 1.0625f);
            final int chop = (int)Math.max(1.0f, 2.0f);
            sheepFur = ImageUtils.eraseArea(sheepFur, chop, chop, sheepFur.getWidth() - chop * 2, sheepFur.getHeight() - chop * 2, sheepFur.getWidth(), sheepFur.getHeight());
            sheepFur = ImageUtils.fillOutline(ImageUtils.pad(sheepFur), this.options.outlines, true, 27.5f, 27.5f, (int)Math.max(1.0f, 2.0f));
            this.textureAtlas.registerIconForBufferedImage("sheepfur", sheepFur);
            BufferedImage crown = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/crown.png"), 0, 0, 16, 16, 16, 16);
            crown = ImageUtils.fillOutline(ImageUtils.scaleImage(crown, 2.0f), this.options.outlines, true, 32.0f, 32.0f, 2);
            this.textureAtlas.registerIconForBufferedImage("crown", crown);
            BufferedImage glow = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/glow.png"), 0, 0, 16, 16, 16, 16);
            glow = ImageUtils.fillOutline(glow, this.options.outlines, true, 32.0f, 32.0f, 2);
            this.textureAtlas.registerIconForBufferedImage("glow", glow);
            final ResourceLocation fontResourceLocation = new ResourceLocation("textures/font/ascii.png");
            BufferedImage fontImage = ImageUtils.loadImage(fontResourceLocation, 0, 0, 128, 128, 128, 128);
            if (fontImage.getWidth() > 512 || fontImage.getHeight() > 512) {
                final int maxDim = Math.max(fontImage.getWidth(), fontImage.getHeight());
                final float scaleBy = 512.0f / maxDim;
                fontImage = ImageUtils.scaleImage(fontImage, scaleBy);
            }
            fontImage = ImageUtils.addImages(new BufferedImage(fontImage.getWidth() + 2, fontImage.getHeight() + 2, fontImage.getType()), fontImage, 1.0f, 1.0f, fontImage.getWidth() + 2, fontImage.getHeight() + 2);
            final Sprite fontSprite = this.textureAtlas.registerIconForBufferedImage(fontResourceLocation.toString(), fontImage);
            final ResourceLocation blankResourceLocation = new ResourceLocation("voxelmap", "images/radar/solid.png");
            final BufferedImage blankImage = ImageUtils.loadImage(blankResourceLocation, 0, 0, 8, 8, 8, 8);
            final Sprite blankSprite = this.textureAtlas.registerIconForBufferedImage(blankResourceLocation.toString(), blankImage);
            this.fontRenderer.setSprites(fontSprite, blankSprite);
            this.fontRenderer.setFontRef(this.textureAtlas.getId());
            this.textureAtlas.stitch();
            this.completedLoading = true;
        }
        catch (final Exception e) {
            System.err.println("Failed getting mobs " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    
    private BufferedImage createImageFromTypeAndResourceLocations(final EnumMobs type, final ResourceLocation resourceLocation, final ResourceLocation resourceLocationSecondary, final Entity entity) {
        final BufferedImage mobImage = ImageUtils.createBufferedImageFromResourceLocation(resourceLocation);
        BufferedImage mobImageSecondary = null;
        if (resourceLocationSecondary != null) {
            mobImageSecondary = ImageUtils.createBufferedImageFromResourceLocation(resourceLocationSecondary);
        }
        try {
            return this.createImageFromTypeAndImages(type, mobImage, mobImageSecondary, entity);
        }
        catch (final Exception e) {
            return null;
        }
    }
    
    private BufferedImage createImageFromTypeAndImages(final EnumMobs type, final BufferedImage mobImage, final BufferedImage mobImageSecondary, final Entity entity) {
        BufferedImage image = null;
        switch (type) {
            case GENERICHOSTILE: {
                image = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/hostile.png"), 0, 0, 16, 16, 16, 16);
                break;
            }
            case GENERICNEUTRAL: {
                image = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/neutral.png"), 0, 0, 16, 16, 16, 16);
                break;
            }
            case GENERICTAME: {
                image = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/tame.png"), 0, 0, 16, 16, 16, 16);
                break;
            }
            case BAT: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12, 64, 64), ImageUtils.loadImage(mobImage, 25, 1, 3, 4), 0.0f, 0.0f, 8, 12), ImageUtils.flipHorizontal(ImageUtils.loadImage(mobImage, 25, 1, 3, 4)), 5.0f, 0.0f, 8, 12), ImageUtils.loadImage(mobImage, 6, 6, 6, 6), 1.0f, 3.0f, 8, 12);
                break;
            }
            case CHICKEN: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.loadImage(mobImage, 2, 3, 6, 6), ImageUtils.loadImage(mobImage, 16, 2, 4, 2), 1.0f, 2.0f, 6, 6), ImageUtils.loadImage(mobImage, 16, 6, 2, 2), 2.0f, 4.0f, 6, 6);
                break;
            }
            case COD: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 16, 5, 32, 32), ImageUtils.loadImage(mobImage, 15, 3, 1, 3, 32, 32), 1.0f, 1.0f, 16, 5), ImageUtils.loadImage(mobImage, 16, 3, 3, 4, 32, 32), 2.0f, 1.0f, 16, 5), ImageUtils.loadImage(mobImage, 9, 7, 7, 4, 32, 32), 5.0f, 1.0f, 16, 5), ImageUtils.loadImage(mobImage, 26, 7, 4, 4, 32, 32), 12.0f, 1.0f, 16, 5), ImageUtils.loadImage(mobImage, 26, 0, 6, 1, 32, 32), 4.0f, 0.0f, 16, 5);
                break;
            }
            case ENDERDRAGON: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 16, 20, 256, 256), ImageUtils.loadImage(mobImage, 128, 46, 16, 16, 256, 256), 0.0f, 4.0f, 16, 16), ImageUtils.loadImage(mobImage, 192, 60, 12, 5, 256, 256), 2.0f, 11.0f, 16, 16), ImageUtils.loadImage(mobImage, 192, 81, 12, 4, 256, 256), 2.0f, 16.0f, 16, 16), ImageUtils.loadImage(mobImage, 6, 6, 2, 4, 256, 256), 3.0f, 0.0f, 16, 16), ImageUtils.flipHorizontal(ImageUtils.loadImage(mobImage, 6, 6, 2, 4, 256, 256)), 11.0f, 0.0f, 16, 16);
                break;
            }
            case GHAST: {
                image = ImageUtils.loadImage(mobImage, 16, 16, 16, 16);
                break;
            }
            case GHASTATTACKING: {
                image = ImageUtils.loadImage(mobImage, 16, 16, 16, 16);
                break;
            }
            case GUARDIAN: {
                image = ImageUtils.scaleImage(ImageUtils.addImages(ImageUtils.loadImage(mobImage, 16, 16, 12, 12), ImageUtils.loadImage(mobImage, 9, 1, 2, 2), 5.0f, 5.5f, 12, 12), 0.5f);
                break;
            }
            case GUARDIANELDER: {
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 16, 16, 12, 12), ImageUtils.loadImage(mobImage, 9, 1, 2, 2), 5.0f, 5.5f, 12, 12);
                break;
            }
            case HORSE: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 16, 24, 64, 64), ImageUtils.loadImage(mobImage, 56, 38, 2, 16, 64, 64), 1.0f, 7.0f, 16, 24), ImageUtils.loadImage(mobImage, 0, 42, 7, 12, 64, 64), 3.0f, 12.0f, 16, 24), ImageUtils.loadImage(mobImage, 0, 20, 7, 5, 64, 64), 3.0f, 7.0f, 16, 24), ImageUtils.loadImage(mobImage, 0, 30, 5, 5, 64, 64), 10.0f, 7.0f, 16, 24), ImageUtils.loadImage(mobImage, 19, 17, 1, 3, 64, 64), 3.0f, 4.0f, 16, 24), ImageUtils.loadImage(mobImage, 0, 13, 1, 7, 64, 64), 3.0f, 0.0f, 16, 24);
                break;
            }
            case IRONGOLEM: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12, 128, 128), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 128, 128), 0.0f, 1.0f, 8, 12), ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 128, 128), 3.0f, 8.0f, 8, 12);
                break;
            }
            case LLAMA: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 14, 128, 64), ImageUtils.loadImage(mobImage, 6, 20, 8, 8, 128, 64), 0.0f, 3.0f, 8, 14), ImageUtils.loadImage(mobImage, 9, 9, 4, 4, 128, 64), 2.0f, 5.0f, 8, 14), ImageUtils.loadImage(mobImage, 19, 2, 3, 3, 128, 64), 0.0f, 0.0f, 8, 14), ImageUtils.loadImage(mobImage, 19, 2, 3, 3, 128, 64), 5.0f, 0.0f, 8, 14);
                break;
            }
            case LLAMATRADER: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 14, 128, 64), ImageUtils.loadImage(mobImage, 6, 20, 8, 8, 128, 64), 0.0f, 3.0f, 8, 14), ImageUtils.loadImage(mobImage, 9, 9, 4, 4, 128, 64), 2.0f, 5.0f, 8, 14), ImageUtils.loadImage(mobImage, 19, 2, 3, 3, 128, 64), 0.0f, 0.0f, 8, 14), ImageUtils.loadImage(mobImage, 19, 2, 3, 3, 128, 64), 5.0f, 0.0f, 8, 14);
                break;
            }
            case MAGMA: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 32, 18, 8, 1), 0.0f, 3.0f, 8, 8), ImageUtils.loadImage(mobImage, 32, 27, 8, 1), 0.0f, 4.0f, 8, 8);
                break;
            }
            case MOOSHROOM: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 40, 40), ImageUtils.loadImage(mobImage, 6, 6, 8, 8), 16.0f, 16.0f, 40, 40), ImageUtils.loadImage(mobImage, 23, 1, 1, 3), 15.0f, 15.0f, 40, 40), ImageUtils.loadImage(mobImage, 23, 1, 1, 3), 24.0f, 15.0f, 40, 40);
                if (mobImageSecondary != null) {
                    BufferedImage mushroomImage;
                    if (mobImageSecondary.getWidth() != mobImageSecondary.getHeight()) {
                        mushroomImage = ImageUtils.loadImage(mobImageSecondary, 32, 0, 16, 16, 48, 16);
                    }
                    else {
                        mushroomImage = ImageUtils.loadImage(mobImageSecondary, 0, 0, 16, 16, 16, 16);
                    }
                    final float ratio = image.getWidth() / (float)mushroomImage.getWidth();
                    if (ratio < 2.5) {
                        image = ImageUtils.scaleImage(image, 2.5f / ratio);
                    }
                    else if (ratio > 2.5) {
                        mushroomImage = ImageUtils.scaleImage(mushroomImage, ratio / 2.5f);
                    }
                    image = ImageUtils.addImages(image, mushroomImage, 12.0f, 0.0f, 40, 40);
                    break;
                }
                break;
            }
            case PARROT: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 8, 32, 32), ImageUtils.loadImage(mobImage, 2, 22, 3, 5, 32, 32), 1.0f, 0.0f, 8, 8), ImageUtils.loadImage(mobImage, 10, 4, 4, 1, 32, 32), 2.0f, 4.0f, 8, 8), ImageUtils.loadImage(mobImage, 2, 4, 2, 3, 32, 32), 2.0f, 5.0f, 8, 8), ImageUtils.loadImage(mobImage, 11, 8, 1, 2, 32, 32), 4.0f, 5.0f, 8, 8), ImageUtils.loadImage(mobImage, 16, 8, 1, 2, 32, 32), 5.0f, 5.0f, 8, 8);
                break;
            }
            case PHANTOM: {
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 5, 5, 7, 3, 64, 64), ImageUtils.loadImage(mobImageSecondary, 5, 5, 7, 3, 64, 64), 0.0f, 0.0f, 7, 3);
                break;
            }
            case PUFFERFISH: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 3, 3, 32, 32), ImageUtils.loadImage(mobImage, 3, 30, 3, 2, 32, 32), 0.0f, 1.0f, 3, 3), ImageUtils.loadImage(mobImage, 3, 29, 1, 1, 32, 32), 0.0f, 0.0f, 3, 3), ImageUtils.loadImage(mobImage, 5, 29, 1, 1, 32, 32), 2.0f, 0.0f, 3, 3);
                break;
            }
            case PUFFERFISHHALF: {
                image = ImageUtils.loadImage(mobImage, 17, 27, 5, 5, 32, 32);
                break;
            }
            case PUFFERFISHFULL: {
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 32, 32);
                break;
            }
            case SALMON: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 26, 7, 32, 32), ImageUtils.loadImage(mobImage, 27, 3, 3, 4, 32, 32), 1.0f, 2.5f, 26, 7), ImageUtils.loadImage(mobImage, 11, 8, 8, 5, 32, 32), 4.0f, 2.0f, 26, 7), ImageUtils.loadImage(mobImage, 11, 21, 8, 5, 32, 32), 12.0f, 2.0f, 26, 7), ImageUtils.loadImage(mobImage, 26, 16, 6, 5, 32, 32), 20.0f, 2.0f, 26, 7), ImageUtils.loadImage(mobImage, 0, 0, 2, 2, 32, 32), 10.0f, 0.0f, 26, 7), ImageUtils.loadImage(mobImage, 5, 6, 3, 2, 32, 32), 12.0f, 0.0f, 26, 7);
                break;
            }
            case SLIME: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 8), ImageUtils.loadImage(mobImage, 6, 22, 6, 6), 1.0f, 1.0f, 8, 8), ImageUtils.loadImage(mobImage, 34, 6, 2, 2), 5.0f, 2.0f, 8, 8), ImageUtils.loadImage(mobImage, 34, 2, 2, 2), 1.0f, 2.0f, 8, 8), ImageUtils.loadImage(mobImage, 33, 9, 1, 1), 4.0f, 5.0f, 8, 8), ImageUtils.loadImage(mobImage, 8, 8, 8, 8), 0.0f, 0.0f, 8, 8);
                break;
            }
            case TROPICALFISHA: {
                float[] primaryColorsA = { 0.9765f, 0.502f, 0.1137f };
                float[] secondaryColorsA = { 0.9765f, 1.0f, 0.9961f };
                if (entity != null && entity instanceof TropicalFish) {
                    final TropicalFish fish = (TropicalFish)entity;
                    primaryColorsA = fish.getBaseColor();
                    secondaryColorsA = fish.getPatternColor();
                }
                final BufferedImage baseA = ImageUtils.colorify(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 10, 6, 32, 32), ImageUtils.loadImage(mobImage, 8, 6, 6, 3, 32, 32), 0.0f, 3.0f, 10, 6), ImageUtils.loadImage(mobImage, 17, 1, 5, 3, 32, 32), 1.0f, 0.0f, 10, 6), ImageUtils.loadImage(mobImage, 28, 0, 4, 3, 32, 32), 6.0f, 3.0f, 10, 6), primaryColorsA[0], primaryColorsA[1], primaryColorsA[2]);
                final BufferedImage patternA = ImageUtils.colorify(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImageSecondary, 10, 6, 32, 32), ImageUtils.loadImage(mobImageSecondary, 8, 6, 6, 3, 32, 32), 0.0f, 3.0f, 10, 6), ImageUtils.loadImage(mobImageSecondary, 17, 1, 5, 3, 32, 32), 1.0f, 0.0f, 10, 6), ImageUtils.loadImage(mobImageSecondary, 28, 0, 4, 3, 32, 32), 6.0f, 3.0f, 10, 6), secondaryColorsA[0], secondaryColorsA[1], secondaryColorsA[2]);
                image = ImageUtils.addImages(baseA, patternA, 0.0f, 0.0f, 10, 6);
                baseA.flush();
                patternA.flush();
                break;
            }
            case TROPICALFISHB: {
                float[] primaryColorsB = { 0.5373f, 0.1961f, 0.7216f };
                float[] secondaryColorsB = { 0.9961f, 0.8471f, 0.2392f };
                if (entity != null && entity instanceof TropicalFish) {
                    final TropicalFish fish2 = (TropicalFish)entity;
                    primaryColorsB = fish2.getBaseColor();
                    secondaryColorsB = fish2.getPatternColor();
                }
                final BufferedImage baseB = ImageUtils.colorify(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 12, 12, 32, 32), ImageUtils.loadImage(mobImage, 0, 26, 6, 6, 32, 32), 6.0f, 3.0f, 12, 12), ImageUtils.loadImage(mobImage, 20, 21, 6, 6, 32, 32), 0.0f, 3.0f, 12, 12), ImageUtils.loadImage(mobImage, 20, 18, 5, 3, 32, 32), 6.0f, 0.0f, 12, 12), ImageUtils.loadImage(mobImage, 20, 27, 5, 3, 32, 32), 6.0f, 9.0f, 12, 12), primaryColorsB[0], primaryColorsB[1], primaryColorsB[2]);
                final BufferedImage patternB = ImageUtils.colorify(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImageSecondary, 12, 12, 32, 32), ImageUtils.loadImage(mobImageSecondary, 0, 26, 6, 6, 32, 32), 6.0f, 3.0f, 12, 12), ImageUtils.loadImage(mobImageSecondary, 20, 21, 6, 6, 32, 32), 0.0f, 3.0f, 12, 12), ImageUtils.loadImage(mobImageSecondary, 20, 18, 5, 3, 32, 32), 6.0f, 0.0f, 12, 12), ImageUtils.loadImage(mobImageSecondary, 20, 27, 5, 3, 32, 32), 6.0f, 9.0f, 12, 12), secondaryColorsB[0], secondaryColorsB[1], secondaryColorsB[2]);
                image = ImageUtils.addImages(baseB, patternB, 0.0f, 0.0f, 12, 12);
                baseB.flush();
                patternB.flush();
                break;
            }
            case WITHER: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 24, 10, 64, 64), ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64), 8.0f, 0.0f, 24, 10), ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64), 0.0f, 2.0f, 24, 10), ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64), 18.0f, 2.0f, 24, 10);
                break;
            }
            case WITHERINVULNERABLE: {
                image = ImageUtils.addImages(ImageUtils.addImages(ImageUtils.addImages(ImageUtils.blankImage(mobImage, 24, 10, 64, 64), ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64), 8.0f, 0.0f, 24, 10), ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64), 0.0f, 2.0f, 24, 10), ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64), 18.0f, 2.0f, 24, 10);
                break;
            }
            default: {
                image = null;
                break;
            }
        }
        mobImage.flush();
        if (mobImageSecondary != null) {
            mobImageSecondary.flush();
        }
        return image;
    }
    
    @Override
    public void onTickInGame(final PoseStack matrixStack, final Minecraft mc, final LayoutVariables layoutVariables) {
        if (!this.options.radarAllowed && !this.options.radarMobsAllowed && !this.options.radarPlayersAllowed) {
            return;
        }
        if (this.game == null) {
            this.game = mc;
        }
        this.layoutVariables = layoutVariables;
        if (this.options.isChanged()) {
            this.timer = 500;
            if (this.options.outlines != this.lastOutlines) {
                this.lastOutlines = this.options.outlines;
                this.loadTexturePackIcons();
            }
        }
        this.direction = GameVariableAccessShim.rotationYaw() + 180.0f;
        while (this.direction >= 360.0f) {
            this.direction -= 360.0f;
        }
        while (this.direction < 0.0f) {
            this.direction += 360.0f;
        }
        if (this.enabled) {
            if (this.completedLoading && this.timer > 95) {
                this.calculateMobs();
                this.timer = 0;
            }
            ++this.timer;
            if (this.completedLoading) {
                this.renderMapMobs(matrixStack, this.layoutVariables.mapX, this.layoutVariables.mapY);
            }
            GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
    
    private int chkLen(final String paramStr) {
        return this.fontRenderer.getStringWidth(paramStr);
    }
    
    private void write(final String paramStr, final float x, final float y, final int color) {
        GLShim.glTexParameteri(3553, 10241, 9728);
        GLShim.glTexParameteri(3553, 10240, 9728);
        this.fontRenderer.drawStringWithShadow(paramStr, x, y, color);
    }
    
    private boolean isEntityShown(final Entity entity) {
        return entity != null && !entity.isInvisibleTo((Player)this.game.player) && ((this.options.showHostiles && (this.options.radarAllowed || this.options.radarMobsAllowed) && this.isHostile(entity)) || (this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed) && this.isPlayer(entity)) || (this.options.showNeutrals && this.options.radarMobsAllowed && this.isNeutral(entity)));
    }
    
    public void calculateMobs() {
        this.contacts.clear();
        final Iterable<Entity> entities = this.game.level.entitiesForRendering();
        for (Entity entity : entities) {
            try {
                if (!this.isEntityShown(entity)) {
                    continue;
                }
                final int wayX = GameVariableAccessShim.xCoord() - (int)entity.position().x();
                final int wayZ = GameVariableAccessShim.zCoord() - (int)entity.position().z();
                final int wayY = GameVariableAccessShim.yCoord() - (int)entity.position().y();
                double hypot = wayX * wayX + wayZ * wayZ + wayY * wayY;
                hypot /= this.layoutVariables.zoomScaleAdjusted * this.layoutVariables.zoomScaleAdjusted;
                if (hypot >= 961.0) {
                    continue;
                }
                if (this.hasCustomNPCs) {
                    try {
                        if (this.entityCustomNpcClass.isInstance(entity)) {
                            final Object modelData = this.modelDataField.get(entity);
                            final LivingEntity wrappedEntity = (LivingEntity)this.getEntityMethod.invoke(modelData, entity);
                            if (wrappedEntity != null) {
                                entity = (Entity)wrappedEntity;
                            }
                        }
                    }
                    catch (final Exception ex) {}
                }
                final Contact contact = new Contact(entity, EnumMobs.getMobTypeByEntity(entity));
                final String unscrubbedName = TextUtils.asFormattedString(contact.entity.getDisplayName());
                contact.setName(unscrubbedName);
                if (contact.entity.getVehicle() != null && this.isEntityShown(contact.entity.getVehicle())) {
                    contact.yFudge = 1;
                }
                contact.updateLocation();
                boolean enabled = false;
                if (!contact.vanillaType) {
                    final String type = entity.getType().getDescriptionId();
                    final CustomMob customMob = CustomMobsManager.getCustomMobByType(type);
                    if (customMob == null || customMob.enabled) {
                        enabled = true;
                    }
                }
                else if (contact.type.enabled) {
                    enabled = true;
                }
                if (!enabled) {
                    continue;
                }
                if (contact.type == EnumMobs.PLAYER) {
                    this.handleMPplayer(contact);
                }
                if (contact.icon == null) {
                    this.tryCustomIcon(contact);
                }
                if (contact.icon == null) {
                    this.tryAutoIcon(contact);
                }
                if (contact.icon == null) {
                    this.getGenericIcon(contact);
                }
                if (contact.type == EnumMobs.HORSE) {
                    contact.setRotationFactor(45);
                }
                final String scrubbedName = TextUtils.scrubCodes(contact.entity.getName().getString());
                if (scrubbedName != null && (scrubbedName.equals("Dinnerbone") || scrubbedName.equals("Grumm")) && (!(contact.entity instanceof Player) || ((Player)contact.entity).isModelPartShown(PlayerModelPart.CAPE))) {
                    contact.setRotationFactor(contact.rotationFactor + 180);
                }
                if ((this.options.showHelmetsPlayers && contact.type == EnumMobs.PLAYER) || (this.options.showHelmetsMobs && contact.type != EnumMobs.PLAYER) || contact.type == EnumMobs.SHEEP) {
                    this.getArmor(contact, entity);
                }
                this.contacts.add(contact);
            }
            catch (final Exception e) {
                System.err.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        if (this.newMobs) {
            try {
                this.textureAtlas.stitchNew();
            }
            catch (final StitcherException e2) {
                System.err.println("Stitcher exception!  Resetting mobs texture atlas.");
                this.loadTexturePackIcons();
            }
        }
        this.newMobs = false;
        Collections.sort(this.contacts, new Comparator<Contact>() {
            @Override
            public int compare(final Contact contact1, final Contact contact2) {
                return contact1.y - contact2.y;
            }
        });
    }
    
    private void tryCustomIcon(final Contact contact) {
        final String identifier = contact.vanillaType ? ("minecraft." + contact.type.id) : contact.entity.getClass().getName();
        final String identifierSimple = contact.vanillaType ? contact.type.id : contact.entity.getClass().getSimpleName();
        Sprite icon = this.textureAtlas.getAtlasSprite(identifier + "custom");
        if (icon == this.textureAtlas.getMissingImage()) {
            final boolean isHostile = this.isHostile(contact.entity);
            CustomMobsManager.add(contact.entity.getType().getDescriptionId(), isHostile, !isHostile);
            final BufferedImage mobSkin = this.getCustomMobImage(identifier, identifierSimple);
            if (mobSkin != null) {
                icon = this.textureAtlas.registerIconForBufferedImage(identifier + "custom", mobSkin);
                this.newMobs = true;
                contact.icon = icon;
                contact.custom = true;
            }
            else {
                this.textureAtlas.registerFailedIcon(identifier + "custom");
            }
        }
        else if (icon != this.textureAtlas.getFailedImage()) {
            contact.custom = true;
            contact.icon = icon;
        }
    }
    
    private BufferedImage getCustomMobImage(final String identifier, final String identifierSimple) {
        BufferedImage mobSkin = null;
        try {
            int intendedSize = 8;
            String fullPath = ("textures/icons/" + identifier + ".png").toLowerCase();
            InputStream is = null;
            try {
                is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
            }
            catch (final IOException e) {
                is = null;
            }
            if (is == null) {
                fullPath = ("textures/icons/" + identifierSimple + ".png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is == null) {
                fullPath = ("textures/icons/" + identifier + "8.png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is == null) {
                fullPath = ("textures/icons/" + identifierSimple + "8.png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is == null) {
                intendedSize = 16;
                fullPath = ("textures/icons/" + identifier + "16.png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is == null) {
                fullPath = ("textures/icons/" + identifierSimple + "16.png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is == null) {
                intendedSize = 32;
                fullPath = ("textures/icons/" + identifier + "32.png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is == null) {
                fullPath = ("textures/icons/" + identifierSimple + "32.png").toLowerCase();
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
            }
            if (is != null) {
                mobSkin = ImageIO.read(is);
                is.close();
                mobSkin = ImageUtils.validateImage(mobSkin);
                final float scale = mobSkin.getWidth() / (float)intendedSize;
                mobSkin = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(mobSkin, 4.0f / scale)), this.options.outlines, 2);
            }
        }
        catch (final Exception e2) {
            mobSkin = null;
        }
        return mobSkin;
    }
    
    private <T extends Entity> void tryAutoIcon(final Contact contact) {
        final EntityRenderer<? super Entity> render = (EntityRenderer<? super Entity>)this.game.getEntityRenderDispatcher().getRenderer(contact.entity);
        ResourceLocation resourceLocation = render.getTextureLocation(contact.entity);
        resourceLocation = this.getRandomizedResourceLocationForEntity(resourceLocation, contact.entity);
        ResourceLocation resourceLocationSecondary = null;
        ResourceLocation resourceLocationTertiary = null;
        ResourceLocation resourceLocationQuaternary = null;
        String color = "";
        if (contact.type.secondaryResourceLocation != null) {
            Label_0575: {
                if (contact.type == EnumMobs.MOOSHROOM) {
                    if (!((MushroomCow)contact.entity).isBaby()) {
                        resourceLocationSecondary = EnumMobs.MOOSHROOM.secondaryResourceLocation;
                    }
                    else {
                        resourceLocationSecondary = null;
                    }
                }
                else if (contact.type == EnumMobs.TROPICALFISHA || contact.type == EnumMobs.TROPICALFISHB) {
                    final TropicalFish fish = (TropicalFish)contact.entity;
                    resourceLocationSecondary = fish.getPatternTextureLocation();
                    color = fish.getBaseColor() + " " + fish.getPatternColor();
                }
                else {
                    if (contact.type == EnumMobs.HORSE) {
                        final Entity entity = contact.entity;
                        if (entity instanceof final Horse horse) {
                            resourceLocationSecondary = Radar.TEXTURES.get(horse.getMarkings());
                            final ItemStack itemStack = horse.getArmor();
                            if (this.options.showHelmetsMobs) {
                                final Item getItem = itemStack.getItem();
                                if (getItem instanceof final HorseArmorItem horseArmorItem) {
                                    resourceLocationTertiary = horseArmorItem.getTexture();
                                    final HorseArmorItem HorseArmorItem = horseArmorItem;
                                    if (HorseArmorItem instanceof final DyeableHorseArmorItem dyableHorseArmorItem) {
                                        contact.armorColor = dyableHorseArmorItem.getColor(itemStack);
                                    }
                                }
                            }
                            break Label_0575;
                        }
                    }
                    if (contact.type == EnumMobs.VILLAGER || contact.type == EnumMobs.ZOMBIEVILLAGER) {
                        final String zombie = (contact.type == EnumMobs.ZOMBIEVILLAGER) ? "zombie_" : "";
                        final VillagerData villagerData = ((VillagerDataHolder) contact.entity).getVillagerData();
                        final VillagerType villagerType = villagerData.getType();
                        final VillagerProfession villagerProfession = villagerData.getProfession();
                        resourceLocationSecondary = Registry.VILLAGER_TYPE.getKey(villagerType);
                        resourceLocationSecondary = new ResourceLocation(resourceLocationSecondary.getNamespace(), "textures/entity/" + zombie + "villager/type/" + resourceLocationSecondary.getPath() + ".png");
                        if (villagerProfession != VillagerProfession.NONE && !((LivingEntity)contact.entity).isBaby()) {
                            resourceLocationTertiary = Registry.VILLAGER_PROFESSION.getKey(villagerProfession);
                            resourceLocationTertiary = new ResourceLocation(resourceLocationTertiary.getNamespace(), "textures/entity/" + zombie + "villager/profession/" + resourceLocationTertiary.getPath() + ".png");
                            if (villagerProfession != VillagerProfession.NITWIT) {
                                resourceLocationQuaternary = (ResourceLocation)Radar.LEVEL_TO_ID.get(Mth.clamp(villagerData.getLevel(), 1, Radar.LEVEL_TO_ID.size()));
                                resourceLocationQuaternary = new ResourceLocation(resourceLocationQuaternary.getNamespace(), "textures/entity/" + zombie + "villager/profession_level/" + resourceLocationQuaternary.getPath() + ".png");
                            }
                        }
                        final VillagerMetaDataSection.Hat biomeHatType = this.getHatType(resourceLocationSecondary);
                        final VillagerMetaDataSection.Hat professionHatType = this.getHatType(resourceLocationTertiary);
                        final boolean showBiomeHat = professionHatType == VillagerMetaDataSection.Hat.NONE || (professionHatType == VillagerMetaDataSection.Hat.PARTIAL && biomeHatType != VillagerMetaDataSection.Hat.FULL);
                        if (!showBiomeHat) {
                            resourceLocationSecondary = null;
                        }
                    }
                    else {
                        resourceLocationSecondary = contact.type.secondaryResourceLocation;
                    }
                }
            }
            if (resourceLocationSecondary != null) {
                resourceLocationSecondary = this.getRandomizedResourceLocationForEntity(resourceLocationSecondary, contact.entity);
            }
            if (resourceLocationTertiary != null) {
                resourceLocationTertiary = this.getRandomizedResourceLocationForEntity(resourceLocationTertiary, contact.entity);
            }
            if (resourceLocationQuaternary != null) {
                resourceLocationQuaternary = this.getRandomizedResourceLocationForEntity(resourceLocationQuaternary, contact.entity);
            }
        }
        final String entityName = contact.vanillaType ? ("minecraft." + contact.type.id) : contact.entity.getClass().getName();
        String resourceLocationString = ((resourceLocation != null) ? resourceLocation.toString() : "") + ((resourceLocationSecondary != null) ? resourceLocationSecondary.toString() : "");
        resourceLocationString = resourceLocationString + ((resourceLocationTertiary != null) ? resourceLocationTertiary.toString() : "") + ((resourceLocationQuaternary != null) ? resourceLocationQuaternary.toString() : "");
        resourceLocationString += ((contact.armorColor != -1) ? Integer.valueOf(contact.armorColor) : "");
        final String name = entityName + color + resourceLocationString;
        Sprite icon = this.textureAtlas.getAtlasSprite(name);
        if (icon == this.textureAtlas.getMissingImage()) {
            Integer checkCount = this.contactsSkinGetTries.get(name);
            if (checkCount == null) {
                checkCount = 0;
            }
            BufferedImage mobImage = null;
            if (contact.type == EnumMobs.HORSE) {
                BufferedImage base = ImageUtils.createBufferedImageFromResourceLocation(resourceLocation);
                if (resourceLocationSecondary != null) {
                    BufferedImage pattern = ImageUtils.createBufferedImageFromResourceLocation(resourceLocationSecondary);
                    pattern = ImageUtils.scaleImage(pattern, base.getWidth() / (float)pattern.getWidth(), base.getHeight() / (float)pattern.getHeight());
                    base = ImageUtils.addImages(base, pattern, 0.0f, 0.0f, base.getWidth(), base.getHeight());
                    pattern.flush();
                }
                if (resourceLocationTertiary != null) {
                    BufferedImage armor = ImageUtils.createBufferedImageFromResourceLocation(resourceLocationTertiary);
                    armor = ImageUtils.scaleImage(armor, base.getWidth() / (float)armor.getWidth(), base.getHeight() / (float)armor.getHeight());
                    armor = ImageUtils.colorify(armor, contact.armorColor);
                    base = ImageUtils.addImages(base, armor, 0.0f, 0.0f, base.getWidth(), base.getHeight());
                    armor.flush();
                }
                mobImage = this.createImageFromTypeAndImages(contact.type, base, null, contact.entity);
                base.flush();
            }
            else if (contact.type.expectedWidth > 0.5) {
                mobImage = this.createImageFromTypeAndResourceLocations(contact.type, resourceLocation, resourceLocationSecondary, contact.entity);
            }
            if (mobImage != null) {
                mobImage = this.trimAndOutlineImage(contact, mobImage, false, true);
            }
            else {
                mobImage = this.createAutoIconImageFromResourceLocations(contact, (EntityRenderer<? extends Entity>)render, resourceLocation, resourceLocationSecondary, resourceLocationTertiary, resourceLocationQuaternary);
            }
            if (mobImage != null) {
                try {
                    icon = this.textureAtlas.registerIconForBufferedImage(name, mobImage);
                    contact.icon = icon;
                    this.newMobs = true;
                    this.contactsSkinGetTries.remove(name);
                }
                catch (final Exception e) {
                    ++checkCount;
                    if (checkCount > 4) {
                        this.textureAtlas.registerFailedIcon(name);
                        this.contactsSkinGetTries.remove(name);
                    }
                    else {
                        this.contactsSkinGetTries.put(name, checkCount);
                    }
                }
            }
            else {
                ++checkCount;
                if (checkCount > 4) {
                    this.textureAtlas.registerFailedIcon(name);
                    this.contactsSkinGetTries.remove(name);
                }
                else {
                    this.contactsSkinGetTries.put(name, checkCount);
                }
            }
        }
        else if (icon != this.textureAtlas.getFailedImage()) {
            contact.icon = icon;
        }
    }
    
    public VillagerMetaDataSection.Hat getHatType(final ResourceLocation resourceLocation) {
        VillagerMetaDataSection.Hat hatType = VillagerMetaDataSection.Hat.NONE;
        if (resourceLocation != null) {
            try {
                final Resource resource = this.game.getResourceManager().getResource(resourceLocation);
                if (resource != null) {
                    final VillagerMetaDataSection villagerResourceMetadata = (VillagerMetaDataSection)resource.getMetadata(VillagerMetaDataSection.SERIALIZER);
                    if (villagerResourceMetadata != null) {
                        hatType = villagerResourceMetadata.getHat();
                    }
                    resource.close();
                }
            }
            catch (final IOException ex) {}
        }
        return hatType;
    }
    
    private BufferedImage createAutoIconImageFromResourceLocations(final Contact contact, final EntityRenderer<? extends Entity> render, final ResourceLocation... resourceLocations) {
        BufferedImage headImage = null;
        EntityModel<? extends LivingEntity> model = null;
        if (render instanceof final LivingEntityRenderer class_605) {
            try {
                model = (EntityModel<? extends LivingEntity>) class_605.getModel();
                final ArrayList<Field> submodels = ReflectionUtils.getFieldsByType(model, Model.class, ModelPart.class);
                final ArrayList<Field> submodelArrays = ReflectionUtils.getFieldsByType(model, Model.class, ModelPart[].class);
                ModelPart[] headBits = null;
                final ArrayList<ModelPartWithResourceLocation> headPartsWithResourceLocationList = new ArrayList<ModelPartWithResourceLocation>();
                final Properties properties = new Properties();
                final String fullName = contact.vanillaType ? ("minecraft." + contact.type.id) : contact.entity.getClass().getName();
                final String simpleName = contact.vanillaType ? contact.type.id : contact.entity.getClass().getSimpleName();
                String fullPath = ("textures/icons/" + fullName + ".properties").toLowerCase();
                InputStream is = null;
                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                }
                catch (final IOException e) {
                    is = null;
                }
                if (is == null) {
                    fullPath = ("textures/icons/" + simpleName + ".properties").toLowerCase();
                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    }
                    catch (final IOException e) {
                        is = null;
                    }
                }
                if (is != null) {
                    properties.load(is);
                    is.close();
                    final String subModelNames = properties.getProperty("models", "").toLowerCase();
                    final String[] submodelNamesArray = subModelNames.split(",");
                    final List<String> subModelNamesList = Arrays.asList(submodelNamesArray);
                    final HashSet<String> subModelNamesSet = new HashSet<String>();
                    subModelNamesSet.addAll((Collection<? extends String>)subModelNamesList);
                    final ArrayList<ModelPart> headPartsArrayList = new ArrayList<ModelPart>();
                    for (final Field submodelArray : submodelArrays) {
                        final String name = submodelArray.getName().toLowerCase();
                        if (subModelNamesSet.contains(name) || subModelNames.equals("all")) {
                            final ModelPart[] submodelArrayValue = (ModelPart[])submodelArray.get(model);
                            if (submodelArrayValue == null) {
                                continue;
                            }
                            for (int t = 0; t < submodelArrayValue.length; ++t) {
                                headPartsArrayList.add(submodelArrayValue[t]);
                            }
                        }
                    }
                    for (final Field submodel : submodels) {
                        final String name = submodel.getName().toLowerCase();
                        if ((subModelNamesSet.contains(name) || subModelNames.equals("all")) && submodel.get(model) != null) {
                            headPartsArrayList.add((ModelPart)submodel.get(model));
                        }
                    }
                    if (headPartsArrayList.size() > 0) {
                        headBits = headPartsArrayList.toArray(new ModelPart[headPartsArrayList.size()]);
                    }
                }
                if (headBits == null) {
                    if (model instanceof PlayerModel) {
                        boolean showHat = true;
                        final Entity entity = contact.entity;
                        if (entity instanceof final Player player) {
                            showHat = player.isModelPartShown(PlayerModelPart.HAT);
                        }
                        if (showHat) {
                            headBits = new ModelPart[] { ((PlayerModel)model).head, ((PlayerModel)model).hat };
                        }
                        else {
                            headBits = new ModelPart[] { ((PlayerModel)model).head };
                        }
                    }
                    else if (contact.type == EnumMobs.STRAY) {
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(((SkeletonModel)model).head, resourceLocations[0]));
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(((SkeletonModel)model).hat, resourceLocations[0]));
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(this.strayOverlayModel.head, resourceLocations[1]));
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(this.strayOverlayModel.hat, resourceLocations[1]));
                    }
                    else if (contact.type == EnumMobs.DROWNED) {
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(((DrownedModel)model).head, resourceLocations[0]));
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(((DrownedModel)model).hat, resourceLocations[0]));
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(this.drownedOverlayModel.head, resourceLocations[1]));
                        headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(this.drownedOverlayModel.hat, resourceLocations[1]));
                    }
                    else if (model instanceof AxolotlModel) {
                        headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, AxolotlModel.class, ModelPart.class, 6) };
                    }
                    else {
                        final EntityModel<? extends LivingEntity> class_583 = model;
                        if (class_583 instanceof final BatModel batEntityModel) {
                            headBits = new ModelPart[] { batEntityModel.root().getChild("head") };
                        }
                        else if (model instanceof BeeModel) {
                            headBits = new ModelPart[] { ((ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, BeeModel.class, ModelPart.class, 0)).getChild("body") };
                        }
                        else {
                            final EntityModel<? extends LivingEntity> class_584 = model;
                            if (class_584 instanceof final HumanoidModel bipedEntityModel) {
                                headBits = new ModelPart[] { bipedEntityModel.head, bipedEntityModel.hat };
                            }
                            else {
                                final EntityModel<? extends LivingEntity> class_585 = model;
                                if (class_585 instanceof final BlazeModel blazeEntityModel) {
                                    headBits = new ModelPart[] { blazeEntityModel.root().getChild("head") };
                                }
                                else if (model instanceof ChickenModel) {
                                    headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, ChickenModel.class, ModelPart.class) };
                                }
                                else {
                                    final EntityModel<? extends LivingEntity> class_586 = model;
                                    if (class_586 instanceof final CreeperModel creeperEntityModel) {
                                        headBits = new ModelPart[] { creeperEntityModel.root().getChild("head") };
                                    }
                                    else {
                                        final EntityModel<? extends LivingEntity> class_587 = model;
                                        if (class_587 instanceof final DolphinModel dolphinEntityModel) {
                                            headBits = new ModelPart[] { dolphinEntityModel.root().getChild("body").getChild("head") };
                                        }
                                        else {
                                            final EntityModel<? extends LivingEntity> class_588 = model;
                                            if (class_588 instanceof final EndermiteModel endermiteEntityModel) {
                                                headBits = new ModelPart[] { endermiteEntityModel.root().getChild("segment0"), endermiteEntityModel.root().getChild("segment1") };
                                            }
                                            else {
                                                final EntityModel<? extends LivingEntity> class_589 = model;
                                                if (class_589 instanceof final GhastModel ghastEntityModel) {
                                                    headBits = new ModelPart[] { ghastEntityModel.root() };
                                                }
                                                else {
                                                    final EntityModel<? extends LivingEntity> class_590 = model;
                                                    if (class_590 instanceof final GuardianModel guardianEntityModel) {
                                                        headBits = new ModelPart[] { guardianEntityModel.root().getChild("head") };
                                                    }
                                                    else if (model instanceof HoglinModel) {
                                                        headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, HoglinModel.class, ModelPart.class) };
                                                    }
                                                    else {
                                                        final EntityModel<? extends LivingEntity> class_591 = model;
                                                        if (class_591 instanceof HorseModel) {
                                                            final HorseModel<? extends Entity> horseEntityModel = (HorseModel<? extends Entity>)class_591;
                                                            headBits = StreamSupport.stream(horseEntityModel.headParts().spliterator(), false).toArray(ModelPart[]::new);
                                                        }
                                                        else {
                                                            final EntityModel<? extends LivingEntity> class_592 = model;
                                                            if (class_592 instanceof final IllagerModel illagerEntityModel) {
                                                                headBits = new ModelPart[] { illagerEntityModel.root().getChild("head") };
                                                            }
                                                            else {
                                                                final EntityModel<? extends LivingEntity> class_593 = model;
                                                                if (class_593 instanceof final IronGolemModel ironGolemEntityModel) {
                                                                    headBits = new ModelPart[] { ironGolemEntityModel.root().getChild("head") };
                                                                }
                                                                else if (model instanceof LavaSlimeModel) {
                                                                    headBits = (ModelPart[])ReflectionUtils.getPrivateFieldValueByType(model, LavaSlimeModel.class, ModelPart[].class);
                                                                }
                                                                else if (model instanceof OcelotModel) {
                                                                    headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, OcelotModel.class, ModelPart.class, 6) };
                                                                }
                                                                else {
                                                                    final EntityModel<? extends LivingEntity> class_594 = model;
                                                                    if (class_594 instanceof final PhantomModel phantomEntityModel) {
                                                                        headBits = new ModelPart[] { phantomEntityModel.root().getChild("body") };
                                                                    }
                                                                    else if (model instanceof RabbitModel) {
                                                                        headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, RabbitModel.class, ModelPart.class, 7), (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, RabbitModel.class, ModelPart.class, 8), (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, RabbitModel.class, ModelPart.class, 9), (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, RabbitModel.class, ModelPart.class, 11) };
                                                                    }
                                                                    else {
                                                                        final EntityModel<? extends LivingEntity> class_595 = model;
                                                                        if (class_595 instanceof final RavagerModel ravagerEntityModel) {
                                                                            headBits = new ModelPart[] { ravagerEntityModel.root().getChild("neck").getChild("head") };
                                                                        }
                                                                        else {
                                                                            final EntityModel<? extends LivingEntity> class_596 = model;
                                                                            if (class_596 instanceof final ShulkerModel shulkerEntityModel) {
                                                                                headBits = new ModelPart[] { shulkerEntityModel.getHead() };
                                                                            }
                                                                            else {
                                                                                final EntityModel<? extends LivingEntity> class_597 = model;
                                                                                if (class_597 instanceof final SilverfishModel silverFishEntityModel) {
                                                                                    headBits = new ModelPart[] { silverFishEntityModel.root().getChild("segment0"), silverFishEntityModel.root().getChild("segment1") };
                                                                                }
                                                                                else {
                                                                                    final EntityModel<? extends LivingEntity> class_598 = model;
                                                                                    if (class_598 instanceof final SlimeModel slimeEntityModel) {
                                                                                        headBits = new ModelPart[] { slimeEntityModel.root() };
                                                                                    }
                                                                                    else {
                                                                                        final EntityModel<? extends LivingEntity> class_599 = model;
                                                                                        if (class_599 instanceof final SnowGolemModel snowGolemEntityModel) {
                                                                                            headBits = new ModelPart[] { snowGolemEntityModel.root().getChild("head") };
                                                                                        }
                                                                                        else {
                                                                                            final EntityModel<? extends LivingEntity> class_600 = model;
                                                                                            if (class_600 instanceof final SpiderModel spiderEntityModel) {
                                                                                                headBits = new ModelPart[] { spiderEntityModel.root().getChild("head"), spiderEntityModel.root().getChild("body0") };
                                                                                            }
                                                                                            else {
                                                                                                final EntityModel<? extends LivingEntity> class_601 = model;
                                                                                                if (class_601 instanceof final SquidModel squidEntityModel) {
                                                                                                    headBits = new ModelPart[] { squidEntityModel.root().getChild("body") };
                                                                                                }
                                                                                                else {
                                                                                                    final EntityModel<? extends LivingEntity> class_602 = model;
                                                                                                    if (class_602 instanceof final StriderModel striderEntityModel) {
                                                                                                        headBits = new ModelPart[] { striderEntityModel.root().getChild("body") };
                                                                                                    }
                                                                                                    else {
                                                                                                        final EntityModel<? extends LivingEntity> class_603 = model;
                                                                                                        if (class_603 instanceof final VillagerModel villagerResemblingModel) {
                                                                                                            headBits = new ModelPart[] { villagerResemblingModel.getHead() };
                                                                                                        }
                                                                                                        else if (model instanceof WolfModel) {
                                                                                                            headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, WolfModel.class, ModelPart.class) };
                                                                                                        }
                                                                                                        else if (model instanceof QuadrupedModel) {
                                                                                                            headBits = new ModelPart[] { (ModelPart)ReflectionUtils.getPrivateFieldValueByType(model, QuadrupedModel.class, ModelPart.class) };
                                                                                                        }
                                                                                                        else {
                                                                                                            final EntityModel<? extends LivingEntity> class_604 = model;
                                                                                                            if (class_604 instanceof final HierarchicalModel singlePartEntityModel) {
                                                                                                                try {
                                                                                                                    headBits = new ModelPart[] { singlePartEntityModel.root().getChild("head") };
                                                                                                                }
                                                                                                                catch (final Exception ex) {}
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (headBits == null) {
                    final ArrayList<ModelPart> headPartsArrayList2 = new ArrayList<ModelPart>();
                    final ArrayList<ModelPart> purge = new ArrayList<ModelPart>();
                    for (final Field submodelArray2 : submodelArrays) {
                        final String name2 = submodelArray2.getName().toLowerCase();
                        if (name2.contains("head") | name2.contains("eye") | name2.contains("mouth") | name2.contains("teeth") | name2.contains("tooth") | name2.contains("tusk") | name2.contains("jaw") | name2.contains("mand") | name2.contains("nose") | name2.contains("beak") | name2.contains("snout") | name2.contains("muzzle") | (!name2.contains("rear") && name2.contains("ear")) | name2.contains("trunk") | name2.contains("mane") | name2.contains("horn") | name2.contains("antler")) {
                            final ModelPart[] submodelArrayValue2 = (ModelPart[])submodelArray2.get(model);
                            if (submodelArrayValue2 == null || submodelArrayValue2.length < 0) {
                                continue;
                            }
                            headPartsArrayList2.add(submodelArrayValue2[0]);
                        }
                    }
                    for (final Field submodel2 : submodels) {
                        final String name2 = submodel2.getName().toLowerCase();
                        final String nameS = submodel2.getName();
                        if ((name2.contains("head") | name2.contains("eye") | name2.contains("mouth") | name2.contains("teeth") | name2.contains("tooth") | name2.contains("tusk") | name2.contains("jaw") | name2.contains("mand") | name2.contains("nose") | name2.contains("beak") | name2.contains("snout") | name2.contains("muzzle") | (!name2.contains("rear") && name2.contains("ear")) | name2.contains("trunk") | name2.contains("mane") | name2.contains("horn") | name2.contains("antler") | nameS.equals("REar") | nameS.equals("Trout")) && (!nameS.equals("LeftSmallEar") & !nameS.equals("RightSmallEar") & !nameS.equals("BHead") & !nameS.equals("BSnout") & !nameS.equals("BMouth") & !nameS.equals("BMouthOpen") & !nameS.equals("BLEar") & !nameS.equals("BREar") & !nameS.equals("CHead") & !nameS.equals("CSnout") & !nameS.equals("CMouth") & !nameS.equals("CMouthOpen") & !nameS.equals("CLEar") & !nameS.equals("CREar")) && submodel2.get(model) != null) {
                            headPartsArrayList2.add((ModelPart)submodel2.get(model));
                        }
                    }
                    if (headPartsArrayList2.size() == 0) {
                        final int pos = (model instanceof HierarchicalModel) ? 1 : 0;
                        if (submodels.size() > pos) {
                            if (submodels.get(pos).get(model) != null) {
                                headPartsArrayList2.add((ModelPart)submodels.get(pos).get(model));
                            }
                        }
                        else if (submodelArrays.size() > 0 && submodelArrays.get(0).get(model) != null) {
                            final ModelPart[] submodelArrayValue3 = (ModelPart[])submodelArrays.get(0).get(model);
                            if (submodelArrayValue3.length > 0) {
                                headPartsArrayList2.add(submodelArrayValue3[0]);
                            }
                        }
                    }
                    for (final ModelPart bit : headPartsArrayList2) {
                        try {
                            final Object childrenObj = ReflectionUtils.getPrivateFieldValueByType(bit, ModelPart.class, ObjectList.class, 1);
                            if (childrenObj == null) {
                                continue;
                            }
                            final List<ModelPart> children = (List<ModelPart>)childrenObj;
                            purge.addAll(children);
                        }
                        catch (final Exception ex2) {}
                    }
                    headPartsArrayList2.removeAll(purge);
                    headBits = headPartsArrayList2.toArray(new ModelPart[headPartsArrayList2.size()]);
                }
                if (contact.entity != null && model != null && ((headBits != null && headBits.length > 0) || headPartsWithResourceLocationList.size() > 0) && resourceLocations[0] != null) {
                    final String scaleString = properties.getProperty("scale", "1");
                    final float scale = Float.parseFloat(scaleString);
                    Direction facing = Direction.NORTH;
                    final String facingString = properties.getProperty("facing", "front");
                    if (facingString.equals("top")) {
                        facing = Direction.UP;
                    }
                    else if (facingString.equals("side")) {
                        facing = Direction.EAST;
                    }
                    final ResourceLocation resourceLocation = this.combineResourceLocations(resourceLocations);
                    if (headBits != null) {
                        for (int t2 = 0; t2 < headBits.length; ++t2) {
                            headPartsWithResourceLocationList.add(new ModelPartWithResourceLocation(headBits[t2], resourceLocation));
                        }
                    }
                    final ModelPartWithResourceLocation[] headBitsWithLocations = headPartsWithResourceLocationList.toArray(new ModelPartWithResourceLocation[headPartsWithResourceLocationList.size()]);
                    final boolean success = this.drawModel(scale, 1000, (LivingEntity)contact.entity, facing, (Model)model, headBitsWithLocations);
                    ImageUtils.saveImage(contact.type.id, GLUtils.fboTextureID, 0, 512, 512);
                    if (success) {
                        headImage = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
                    }
                }
            }
            catch (final Exception e2) {
                headImage = null;
                e2.printStackTrace();
            }
        }
        if (headImage != null) {
            headImage = this.trimAndOutlineImage(contact, headImage, true, model != null && model instanceof HumanoidModel);
        }
        return headImage;
    }
    
    private ResourceLocation combineResourceLocations(final ResourceLocation... resourceLocations) {
        ResourceLocation resourceLocation = resourceLocations[0];
        if (resourceLocations.length > 1) {
            boolean hasAdditional = false;
            try {
                BufferedImage base = null;
                for (int t = 1; t < resourceLocations.length; ++t) {
                    if (resourceLocations[t] != null) {
                        if (!hasAdditional) {
                            base = ImageUtils.createBufferedImageFromResourceLocation(resourceLocation);
                        }
                        hasAdditional = true;
                        BufferedImage overlay = ImageUtils.createBufferedImageFromResourceLocation(resourceLocations[t]);
                        final float xScale = (float)(base.getWidth() / overlay.getWidth());
                        final float yScale = (float)(base.getHeight() / overlay.getHeight());
                        if (xScale != 1.0f || yScale != 1.0f) {
                            overlay = ImageUtils.scaleImage(overlay, xScale, yScale);
                        }
                        base = ImageUtils.addImages(base, overlay, 0.0f, 0.0f, base.getWidth(), base.getHeight());
                        overlay.flush();
                    }
                }
                if (hasAdditional) {
                    final NativeImage nativeImage = GLUtils.nativeImageFromBufferedImage(base);
                    base.flush();
                    this.nativeBackedTexture.close();
                    this.nativeBackedTexture = new DynamicTexture(nativeImage);
                    GLUtils.register(this.nativeBackedTextureLocation, (AbstractTexture)this.nativeBackedTexture);
                    resourceLocation = this.nativeBackedTextureLocation;
                }
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return resourceLocation;
    }
    
    private boolean drawModel(final float scale, final int captureDepth, final LivingEntity livingEntity, final Direction facing, final Model model, final ModelPartWithResourceLocation[] headBits) {
        boolean failed = false;
        final float size = 64.0f * scale;
        GLShim.glBindTexture(3553, GLUtils.fboTextureID);
        final int width = GLShim.glGetTexLevelParameteri(3553, 0, 4096);
        final int height = GLShim.glGetTexLevelParameteri(3553, 0, 4097);
        GLShim.glBindTexture(3553, 0);
        GLShim.glViewport(0, 0, width, height);
        final Matrix4f minimapProjectionMatrix = RenderSystem.getProjectionMatrix();
        final Matrix4f matrix4f = Matrix4f.orthographic(0.0f, (float)width, 0.0f, (float)height, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f);
        final PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.setIdentity();
        matrixStack.translate(0.0, 0.0, -3000.0 + captureDepth);
        RenderSystem.applyModelViewMatrix();
        GLUtils.bindFrameBuffer();
        GLShim.glDepthMask(true);
        GLShim.glEnable(2929);
        GLShim.glEnable(3553);
        GLShim.glEnable(3042);
        GLShim.glDisable(2884);
        GLShim.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        GLShim.glClearDepth(1.0);
        GLShim.glClear(16640);
        GLShim.glBlendFunc(770, 771);
        matrixStack.pushPose();
        matrixStack.translate((double)(width / 2), (double)(height / 2), 0.0);
        matrixStack.scale(size, size, size);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        if (facing == Direction.EAST) {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90.0f));
        }
        else if (facing == Direction.UP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        }
        RenderSystem.applyModelViewMatrix();
        final Vector4f fullbright2 = new Vector4f(this.fullbright);
        fullbright2.transform(matrixStack.last().pose());
        final Vector3f fullbright3 = new Vector3f(fullbright2);
        RenderSystem.setShaderLights(fullbright3, fullbright3);
        try {
            final PoseStack newMatrixStack = new PoseStack();
            final MultiBufferSource.BufferSource immediate = this.game.renderBuffers().bufferSource();
            float offsetByY = (model instanceof EndermanModel) ? 8.0f : ((model instanceof HumanoidModel || model instanceof SkullModel) ? 4.0f : 0.0f);
            float maxY = 0.0f;
            float minY = 0.0f;
            for (int t = 0; t < headBits.length; ++t) {
                if (headBits[t].modelPart.y < minY) {
                    minY = headBits[t].modelPart.y;
                }
                if (headBits[t].modelPart.y > maxY) {
                    maxY = headBits[t].modelPart.y;
                }
            }
            if (minY < -25.0f) {
                offsetByY = -25.0f - minY;
            }
            else if (maxY > 25.0f) {
                offsetByY = 25.0f - maxY;
            }
            for (int t = 0; t < headBits.length; ++t) {
                final VertexConsumer vertexConsumer = immediate.getBuffer(model.renderType(headBits[t].resourceLocation));
                if (model instanceof final EntityModel entityModel) {
                    entityModel.setupAnim((Entity)livingEntity, 0.0f, 0.0f, 163.0f, 360.0f, 0.0f);
                }
                final float y = headBits[t].modelPart.y;
                headBits[t].modelPart.y += offsetByY;
                headBits[t].modelPart.render(newMatrixStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY);
                headBits[t].modelPart.y = y;
                immediate.endBatch();
            }
        }
        catch (final Exception e) {
            System.out.println("Error attempting to render head bits for " + livingEntity.getClass().getSimpleName());
            e.printStackTrace();
            failed = true;
        }
        matrixStack.popPose();
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
        GLShim.glEnable(2884);
        GLShim.glDisable(2929);
        GLShim.glDepthMask(false);
        GLUtils.unbindFrameBuffer();
        RenderSystem.setProjectionMatrix(minimapProjectionMatrix);
        GLShim.glViewport(0, 0, this.game.getWindow().getWidth(), this.game.getWindow().getHeight());
        return !failed;
    }
    
    private void getGenericIcon(final Contact contact) {
        contact.type = this.getUnknownMobNeutrality(contact.entity);
        final String name = "minecraft." + contact.type.id + contact.type.resourceLocation.toString();
        contact.icon = this.textureAtlas.getAtlasSprite(name);
    }
    
    private ResourceLocation getRandomizedResourceLocationForEntity(ResourceLocation resourceLocation, final Entity entity) {
        try {
            if (this.randomobsOptifine) {
                final Object randomEntitiesProperties = this.mapProperties.get(resourceLocation.getPath());
                if (randomEntitiesProperties != null) {
                    this.setEntityMethod.invoke(this.randomEntityClass.cast(this.randomEntity), entity);
                    resourceLocation = (ResourceLocation)this.getEntityTextureMethod.invoke(this.randomEntitiesPropertiesClass.cast(randomEntitiesProperties), resourceLocation, this.randomEntityClass.cast(this.randomEntity));
                }
            }
        }
        catch (final Exception ex) {}
        return resourceLocation;
    }
    
    private BufferedImage trimAndOutlineImage(final Contact contact, BufferedImage image, final boolean auto, final boolean centered) {
        if (auto) {
            image = (centered ? ImageUtils.trimCentered(image) : ImageUtils.trim(image));
            double acceptableMax = 64.0;
            if (ImageUtils.percentageOfEdgePixelsThatAreSolid(image) < 30.0f) {
                acceptableMax = 128.0;
            }
            final int maxDimension = Math.max(image.getWidth(), image.getHeight());
            final float scale = (float)Math.ceil(maxDimension / acceptableMax);
            image = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, 1.0f / scale)), this.options.outlines, 2);
            return image;
        }
        final float scale2 = image.getWidth() / contact.type.expectedWidth;
        image = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, 4.0f / scale2)), this.options.outlines, 2);
        return image;
    }
    
    private void handleMPplayer(final Contact contact) {
        final AbstractClientPlayer player = (AbstractClientPlayer)contact.entity;
        final GameProfile gameProfile = player.getGameProfile();
        final UUID uuid = gameProfile.getId();
        contact.setUUID(uuid);
        final String playerName = this.scrubCodes(gameProfile.getName());
        Sprite icon = this.textureAtlas.getAtlasSprite(playerName);
        Integer checkCount = 0;
        if (icon == this.textureAtlas.getMissingImage()) {
            checkCount = this.mpContactsSkinGetTries.get(playerName);
            if (checkCount == null) {
                checkCount = 0;
            }
            if (checkCount < 5) {
                HttpTexture imageData = null;
                try {
                    if (player.getSkinTextureLocation() == DefaultPlayerSkin.getDefaultSkin(player.getUUID())) {
                        throw new Exception("failed to get skin: skin is default");
                    }
                    AbstractClientPlayer.registerSkinTexture(player.getSkinTextureLocation(), player.getName().getString());
                    imageData = (HttpTexture)Minecraft.getInstance().getTextureManager().getTexture(player.getSkinTextureLocation());
                    if (imageData == null) {
                        throw new Exception("failed to get skin: image data was null");
                    }
                    final EntityRenderer<? extends Entity> render = (EntityRenderer<? extends Entity>)this.game.getEntityRenderDispatcher().getRenderer(contact.entity);
                    final BufferedImage skinImage = this.createAutoIconImageFromResourceLocations(contact, render, player.getSkinTextureLocation(), null);
                    icon = this.textureAtlas.registerIconForBufferedImage(playerName, skinImage);
                    this.newMobs = true;
                    this.mpContactsSkinGetTries.remove(playerName);
                }
                catch (final Exception e) {
                    icon = this.textureAtlas.getAtlasSprite("minecraft." + EnumMobs.PLAYER.id + EnumMobs.PLAYER.resourceLocation.toString());
                    ++checkCount;
                    this.mpContactsSkinGetTries.put(playerName, checkCount);
                }
                contact.icon = icon;
            }
        }
        else {
            contact.icon = icon;
        }
    }
    
    private void getArmor(final Contact contact, final Entity entity) {
        Sprite icon = null;
        final ItemStack stack = ((LivingEntity)entity).getItemBySlot(EquipmentSlot.HEAD);
        Item helmet = null;
        if (stack != null && stack.getCount() > 0) {
            helmet = stack.getItem();
        }
        if (contact.type == EnumMobs.SHEEP) {
            final Sheep sheepEntity = (Sheep)contact.entity;
            if (!sheepEntity.isSheared()) {
                icon = this.textureAtlas.getAtlasSprite("sheepfur");
                final float[] sheepColors = Sheep.getColorArray(sheepEntity.getColor());
                contact.setArmorColor((int)(sheepColors[0] * 255.0f) << 16 | (int)(sheepColors[1] * 255.0f) << 8 | (int)(sheepColors[2] * 255.0f));
            }
        }
        else if (helmet != null) {
            if (helmet == Items.SKELETON_SKULL) {
                icon = this.textureAtlas.getAtlasSprite("minecraft." + EnumMobs.SKELETON.id + EnumMobs.SKELETON.resourceLocation.toString() + "head");
            }
            else if (helmet == Items.WITHER_SKELETON_SKULL) {
                icon = this.textureAtlas.getAtlasSprite("minecraft." + EnumMobs.SKELETONWITHER.id + EnumMobs.SKELETONWITHER.resourceLocation.toString() + "head");
            }
            else if (helmet == Items.ZOMBIE_HEAD) {
                icon = this.textureAtlas.getAtlasSprite("minecraft." + EnumMobs.ZOMBIE.id + EnumMobs.ZOMBIE.resourceLocation.toString() + "head");
            }
            else if (helmet == Items.CREEPER_HEAD) {
                icon = this.textureAtlas.getAtlasSprite("minecraft." + EnumMobs.CREEPER.id + EnumMobs.CREEPER.resourceLocation.toString() + "head");
            }
            else if (helmet == Items.DRAGON_HEAD) {
                icon = this.textureAtlas.getAtlasSprite("minecraft." + EnumMobs.ENDERDRAGON.id + EnumMobs.ENDERDRAGON.resourceLocation.toString() + "head");
            }
            else if (helmet == Items.PLAYER_HEAD) {
                GameProfile gameProfile = null;
                if (stack.hasTag()) {
                    final CompoundTag nbttagcompound = stack.getTag();
                    if (nbttagcompound.contains("SkullOwner", 10)) {
                        gameProfile = NbtUtils.readGameProfile(nbttagcompound.getCompound("SkullOwner"));
                    }
                    else if (nbttagcompound.contains("SkullOwner", 8)) {
                        final String name = nbttagcompound.getString("SkullOwner");
                        if (name != null && !name.equals("")) {
                            gameProfile = new GameProfile((UUID)null, name);
                            nbttagcompound.getString("SkullOwner");
                            SkullBlockEntity.updateGameprofile(gameProfile, gameProfilex -> nbttagcompound.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfilex)));
                        }
                    }
                }
                ResourceLocation resourceLocation = DefaultPlayerSkin.getDefaultSkin();
                if (gameProfile != null) {
                    final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = this.game.getSkinManager().getInsecureSkinInformation(gameProfile);
                    if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                        resourceLocation = this.game.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                    }
                }
                icon = this.textureAtlas.getAtlasSpriteIncludingYetToBeStitched("minecraft." + EnumMobs.PLAYER.id + resourceLocation.toString() + "head");
                if (icon == this.textureAtlas.getMissingImage()) {
                    final ModelPart inner = (ModelPart)ReflectionUtils.getPrivateFieldValueByType(this.playerSkullModel, SkullModel.class, ModelPart.class, 0);
                    final ModelPart outer = (ModelPart)ReflectionUtils.getPrivateFieldValueByType(this.playerSkullModel, SkullModel.class, ModelPart.class, 1);
                    final ModelPartWithResourceLocation[] headBits = { new ModelPartWithResourceLocation(inner, resourceLocation), new ModelPartWithResourceLocation(outer, resourceLocation) };
                    final boolean success = this.drawModel(1.1875f, 1000, (LivingEntity)contact.entity, Direction.NORTH, (Model)this.playerSkullModel, headBits);
                    if (success) {
                        BufferedImage headImage = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
                        headImage = this.trimAndOutlineImage(new Contact((Entity)this.game.player, EnumMobs.PLAYER), headImage, true, true);
                        icon = this.textureAtlas.registerIconForBufferedImage("minecraft." + EnumMobs.PLAYER.id + resourceLocation.toString() + "head", headImage);
                        this.newMobs = true;
                    }
                }
            }
            else {
                final Item Item = helmet;
                if (Item instanceof final ArmorItem helmetArmor) {
                    final int armorType = this.getArmorType(helmetArmor);
                    if (armorType == Radar.UNKNOWN) {
                        final boolean isPiglin = contact.type == EnumMobs.PIGLIN || contact.type == EnumMobs.PIGLINZOMBIE;
                        icon = this.textureAtlas.getAtlasSprite("armor " + helmet.getDescriptionId() + (isPiglin ? "_piglin" : ""));
                        if (icon == this.textureAtlas.getMissingImage()) {
                            icon = this.createUnknownArmorIcons(contact, stack, helmet);
                        }
                        else if (icon == this.textureAtlas.getFailedImage()) {
                            icon = null;
                        }
                    }
                    else {
                        icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[armorType]);
                    }
                    final ArmorItem class_1793 = helmetArmor;
                    if (class_1793 instanceof final DyeableArmorItem dyeableHelmetArmor) {
                        contact.setArmorColor(dyeableHelmetArmor.getColor(stack));
                    }
                }
                else {
                    final Item class_1794 = helmet;
                    if (class_1794 instanceof final BlockItem blockItem) {
                        final Block block = blockItem.getBlock();
                        final BlockState blockState = block.defaultBlockState();
                        final int stateID = Block.getId(blockState);
                        icon = this.textureAtlas.getAtlasSprite("blockArmor " + stateID);
                        if (icon == this.textureAtlas.getMissingImage()) {
                            BufferedImage blockImage = this.master.getColorManager().getBlockImage(blockState, stack, entity.level, 4.9473686f, -8.0f);
                            if (blockImage != null) {
                                final int width = blockImage.getWidth();
                                final int height = blockImage.getHeight();
                                blockImage = ImageUtils.eraseArea(blockImage, width / 2 - 15, height / 2 - 15, 30, 30, width, height);
                                BufferedImage blockImageFront = this.master.getColorManager().getBlockImage(blockState, stack, entity.level, 4.9473686f, 7.25f);
                                blockImageFront = blockImageFront.getSubimage(width / 2 - 15, height / 2 - 15, 30, 30);
                                blockImage = ImageUtils.addImages(blockImage, blockImageFront, (float)(width / 2 - 15), (float)(height / 2 - 15), width, height);
                                blockImageFront.flush();
                                blockImage = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.trimCentered(blockImage)), this.options.outlines, true, 37.6f, 37.6f, 2);
                                icon = this.textureAtlas.registerIconForBufferedImage("blockArmor " + stateID, blockImage);
                                this.newMobs = true;
                            }
                        }
                    }
                }
            }
        }
        contact.armorIcon = icon;
    }
    
    private Sprite createUnknownArmorIcons(final Contact contact, final ItemStack stack, final Item helmet) {
        Sprite icon = null;
        final boolean isPiglin = contact.type == EnumMobs.PIGLIN || contact.type == EnumMobs.PIGLINZOMBIE;
        Method m = null;
        try {
            final Class<?> c = Class.forName("net.minecraftforge.client.ForgeHooksClient");
            m = c.getMethod("getArmorTexture", Entity.class, ItemStack.class, String.class, EquipmentSlot.class, String.class);
        }
        catch (final Exception ex) {}
        final Method getResourceLocation = m;
        ResourceLocation resourceLocation = null;
        try {
            String materialName = ((ArmorItem)helmet).getMaterial().getName();
            String domain = "minecraft";
            final int sep = materialName.indexOf(58);
            if (sep != -1) {
                domain = materialName.substring(0, sep);
                materialName = materialName.substring(sep + 1);
            }
            String suffix = null;
            suffix = ((suffix == null) ? "" : ("_" + suffix));
            String resourcePath = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, materialName, 1, suffix);
            if (getResourceLocation != null) {
                resourcePath = (String)getResourceLocation.invoke(null, contact.entity, stack, resourcePath, EquipmentSlot.HEAD, null);
            }
            resourceLocation = new ResourceLocation(resourcePath);
        }
        catch (final Exception ex2) {}
        m = null;
        try {
            final Class<?> c2 = Class.forName("net.minecraftforge.client.ForgeHooksClient");
            m = c2.getMethod("getArmorModel", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class);
        }
        catch (final Exception ex3) {}
        final Method getModel = m;
        HumanoidModel<? extends LivingEntity> modelBiped = null;
        try {
            if (getModel != null) {
                modelBiped = (HumanoidModel<? extends LivingEntity>)getModel.invoke(null, contact.entity, stack, EquipmentSlot.HEAD, null);
            }
        }
        catch (final Exception ex4) {}
        float intendedWidth = 9.0f;
        final float intendedHeight = 9.0f;
        if (modelBiped == null) {
            if (!isPiglin) {
                modelBiped = this.bipedArmorModel;
            }
            else {
                modelBiped = this.piglinArmorModel;
                intendedWidth = 11.5f;
            }
        }
        if (modelBiped != null && resourceLocation != null) {
            final ModelPartWithResourceLocation[] headBitsWithResourceLocation = { new ModelPartWithResourceLocation(modelBiped.head, resourceLocation), new ModelPartWithResourceLocation(modelBiped.hat, resourceLocation) };
            this.drawModel(1.0f, 2, (LivingEntity)contact.entity, Direction.NORTH, (Model)modelBiped, headBitsWithResourceLocation);
            BufferedImage armorImage = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
            armorImage = armorImage.getSubimage(200, 200, 112, 112);
            armorImage = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.trimCentered(armorImage)), this.options.outlines, true, intendedWidth * 4.0f, intendedHeight * 4.0f, 2);
            icon = this.textureAtlas.registerIconForBufferedImage("armor " + helmet.getDescriptionId() + (isPiglin ? "_piglin" : ""), armorImage);
            this.newMobs = true;
        }
        if (icon == null && resourceLocation != null) {
            BufferedImage armorTexture = ImageUtils.createBufferedImageFromResourceLocation(resourceLocation);
            if (armorTexture != null) {
                if (!isPiglin) {
                    armorTexture = ImageUtils.addImages(ImageUtils.loadImage(armorTexture, 8, 8, 8, 8), ImageUtils.loadImage(armorTexture, 40, 8, 8, 8), 0.0f, 0.0f, 8, 8);
                    final float scale = armorTexture.getWidth() / 8.0f;
                    final BufferedImage armorImage2 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(armorTexture, 4.0f / scale * 47.0f / 38.0f)), this.options.outlines, true, 37.6f, 37.6f, 2);
                    icon = this.textureAtlas.registerIconForBufferedImage("armor " + resourceLocation.toString(), armorImage2);
                }
                else {
                    armorTexture = ImageUtils.addImages(ImageUtils.loadImage(armorTexture, 8, 8, 8, 8), ImageUtils.loadImage(armorTexture, 40, 8, 8, 8), 0.0f, 0.0f, 8, 8);
                    final float scale = armorTexture.getWidth() / 8.0f;
                    final BufferedImage armorImage2 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(armorTexture, 4.0f / scale * 47.0f / 38.0f)), this.options.outlines, true, 47.0f, 37.6f, 2);
                    icon = this.textureAtlas.registerIconForBufferedImage("armor " + resourceLocation.toString() + "_piglin", armorImage2);
                }
                this.newMobs = true;
            }
        }
        if (icon == null) {
            System.out.println("can't get texture for custom armor type: " + helmet.getClass());
            this.textureAtlas.registerFailedIcon("armor " + helmet.getDescriptionId() + helmet.getClass().getName());
        }
        return icon;
    }
    
    private String scrubCodes(String string) {
        string = string.replaceAll("(\\xA7.)", "");
        return string;
    }
    
    private EnumMobs getUnknownMobNeutrality(final Entity entity) {
        if (this.isHostile(entity)) {
            return EnumMobs.GENERICHOSTILE;
        }
        if (entity instanceof final TamableAnimal tameableEntity) {
            if (tameableEntity.isTame() && (this.game.hasSingleplayerServer() || tameableEntity.getOwner().equals(this.game.player))) {
                return EnumMobs.GENERICTAME;
            }
        }
        return EnumMobs.GENERICNEUTRAL;
    }
    
    private int getArmorType(final ArmorItem helmet) {
        if (helmet.getDescriptionId().equals("item.minecraft.leather_helmet")) {
            return 0;
        }
        return Radar.UNKNOWN;
    }
    
    public <T extends Entity> void renderMapMobs(final PoseStack matrixStack, final int x, final int y) {
        final double max = this.layoutVariables.zoomScaleAdjusted * 32.0;
        final double lastX = GameVariableAccessShim.xCoordDouble();
        final double lastZ = GameVariableAccessShim.zCoordDouble();
        final int lastY = GameVariableAccessShim.yCoord();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GLUtils.disp2(this.textureAtlas.getId());
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        for (Contact contact : this.contacts) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            contact.updateLocation();
            final double contactX = contact.x;
            final double contactZ = contact.z;
            final int contactY = contact.y;
            double wayX = lastX - contactX;
            double wayZ = lastZ - contactZ;
            final int wayY = lastY - contactY;
            final double adjustedDiff = max - Math.max(Math.abs(wayY) - 0, 0);
            contact.brightness = (float)Math.max(adjustedDiff / max, 0.0);
            final Contact contact2 = contact;
            contact2.brightness *= contact.brightness;
            contact.angle = (float)Math.toDegrees(Math.atan2(wayX, wayZ));
            contact.distance = Math.sqrt(wayX * wayX + wayZ * wayZ) / this.layoutVariables.zoomScaleAdjusted;
            if (wayY < 0) {
                GLShim.glColor4f(1.0f, 1.0f, 1.0f, contact.brightness);
            }
            else {
                GLShim.glColor3f(1.0f * contact.brightness, 1.0f * contact.brightness, 1.0f * contact.brightness);
            }
            if (this.minimapOptions.rotates) {
                contact.angle += this.direction;
            }
            else if (this.minimapOptions.oldNorth) {
                contact.angle -= 90.0f;
            }
            boolean inRange = false;
            if (this.minimapOptions.squareMap) {
                final double radLocate = Math.toRadians(contact.angle);
                final double dispX = contact.distance * Math.cos(radLocate);
                final double dispY = contact.distance * Math.sin(radLocate);
                inRange = (Math.abs(dispX) <= 28.5 && Math.abs(dispY) <= 28.5);
            }
            else {
                inRange = (contact.distance < 31.0);
            }
            if (inRange) {
                try {
                    matrixStack.pushPose();
                    if (this.options.filtering) {
                        matrixStack.translate((double)x, (double)y, 0.0);
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-contact.angle));
                        matrixStack.translate(0.0, -contact.distance, 0.0);
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(contact.angle + contact.rotationFactor));
                        matrixStack.translate((double)(-x), (double)(-y), 0.0);
                    }
                    else {
                        wayX = Math.sin(Math.toRadians(contact.angle)) * contact.distance;
                        wayZ = Math.cos(Math.toRadians(contact.angle)) * contact.distance;
                        if (this.options.filtering) {
                            matrixStack.translate(-wayX, -wayZ, 0.0);
                        }
                        else {
                            matrixStack.translate(Math.round(-wayX * this.layoutVariables.scScale) / (double)this.layoutVariables.scScale, Math.round(-wayZ * this.layoutVariables.scScale) / (double)this.layoutVariables.scScale, 0.0);
                        }
                    }
                    RenderSystem.applyModelViewMatrix();
                    float yOffset = 0.0f;
                    if (contact.entity.getVehicle() != null && this.isEntityShown(contact.entity.getVehicle())) {
                        yOffset = -4.0f;
                    }
                    if (contact.type == EnumMobs.GHAST || contact.type == EnumMobs.GHASTATTACKING || contact.type == EnumMobs.WITHER || contact.type == EnumMobs.WITHERINVULNERABLE || contact.type == EnumMobs.VEX || contact.type == EnumMobs.VEXCHARGING || contact.type == EnumMobs.PUFFERFISH || contact.type == EnumMobs.PUFFERFISHHALF || contact.type == EnumMobs.PUFFERFISHFULL) {
                        if (contact.type == EnumMobs.GHAST || contact.type == EnumMobs.GHASTATTACKING) {
                            final EntityRenderer<? super Entity> render = (EntityRenderer<? super Entity>)this.game.getEntityRenderDispatcher().getRenderer(contact.entity);
                            final String path = render.getTextureLocation(contact.entity).getPath();
                            contact.type = (path.endsWith("ghast_fire.png") ? EnumMobs.GHASTATTACKING : EnumMobs.GHAST);
                        }
                        else if (contact.type == EnumMobs.WITHER || contact.type == EnumMobs.WITHERINVULNERABLE) {
                            final EntityRenderer<? super Entity> render = (EntityRenderer<? super Entity>)this.game.getEntityRenderDispatcher().getRenderer(contact.entity);
                            final String path = render.getTextureLocation(contact.entity).getPath();
                            contact.type = (path.endsWith("wither_invulnerable.png") ? EnumMobs.WITHERINVULNERABLE : EnumMobs.WITHER);
                        }
                        else if (contact.type == EnumMobs.VEX || contact.type == EnumMobs.VEXCHARGING) {
                            final EntityRenderer<? super Entity> render = (EntityRenderer<? super Entity>)this.game.getEntityRenderDispatcher().getRenderer(contact.entity);
                            final String path = render.getTextureLocation(contact.entity).getPath();
                            contact.type = (path.endsWith("vex_charging.png") ? EnumMobs.VEXCHARGING : EnumMobs.VEX);
                        }
                        else if (contact.type == EnumMobs.PUFFERFISH || contact.type == EnumMobs.PUFFERFISHHALF || contact.type == EnumMobs.PUFFERFISHFULL) {
                            final int size = ((Pufferfish) contact.entity).getPuffState();
                            switch (size) {
                                case 0: {
                                    contact.type = EnumMobs.PUFFERFISH;
                                    break;
                                }
                                case 1: {
                                    contact.type = EnumMobs.PUFFERFISHHALF;
                                    break;
                                }
                                case 2: {
                                    contact.type = EnumMobs.PUFFERFISHFULL;
                                    break;
                                }
                            }
                        }
                        this.tryAutoIcon(contact);
                        this.tryCustomIcon(contact);
                        if (this.newMobs) {
                            try {
                                this.textureAtlas.stitchNew();
                            }
                            catch (final StitcherException e) {
                                System.err.println("Stitcher exception in render method!  Resetting mobs texture atlas.");
                                this.loadTexturePackIcons();
                            }
                            GLUtils.disp2(this.textureAtlas.getId());
                        }
                        this.newMobs = false;
                    }
                    if (contact.uuid != null && contact.uuid.equals(this.devUUID)) {
                        final Sprite icon = this.textureAtlas.getAtlasSprite("glow");
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, (float)x, y + yOffset, (float)(int)(icon.getIconWidth() / 2.0f));
                        GLUtils.drawPost();
                    }
                    this.applyFilteringParameters();
                    GLUtils.drawPre();
                    GLUtils.setMap(contact.icon, (float)x, y + yOffset, (float)(int)(contact.icon.getIconWidth() / 4.0f));
                    GLUtils.drawPost();
                    if (((this.options.showHelmetsPlayers && contact.type == EnumMobs.PLAYER) || (this.options.showHelmetsMobs && contact.type != EnumMobs.PLAYER) || contact.type == EnumMobs.SHEEP) && contact.armorIcon != null) {
                        Sprite icon = contact.armorIcon;
                        float armorOffset = 0.0f;
                        if (contact.type == EnumMobs.ZOMBIEVILLAGER) {
                            armorOffset = -0.5f;
                        }
                        float armorScale = 1.0f;
                        float red = 1.0f;
                        float green = 1.0f;
                        float blue = 1.0f;
                        if (contact.armorColor != -1) {
                            red = (contact.armorColor >> 16 & 0xFF) / 255.0f;
                            green = (contact.armorColor >> 8 & 0xFF) / 255.0f;
                            blue = (contact.armorColor >> 0 & 0xFF) / 255.0f;
                            if (contact.type == EnumMobs.SHEEP) {
                                final Sheep sheepEntity = (Sheep)contact.entity;
                                if (sheepEntity.hasCustomName() && "jeb_".equals(sheepEntity.getName().getContents())) {
                                    final int semiRandom = sheepEntity.getAge() / 25 + sheepEntity.getId();
                                    final int numDyeColors = DyeColor.values().length;
                                    final int colorID1 = semiRandom % numDyeColors;
                                    final int colorID2 = (semiRandom + 1) % numDyeColors;
                                    final float lerpVal = (sheepEntity.getAge() % 25 + this.game.getFrameTime()) / 25.0f;
                                    final float[] sheepColors1 = Sheep.getColorArray(DyeColor.byId(colorID1));
                                    final float[] sheepColors2 = Sheep.getColorArray(DyeColor.byId(colorID2));
                                    red = sheepColors1[0] * (1.0f - lerpVal) + sheepColors2[0] * lerpVal;
                                    green = sheepColors1[1] * (1.0f - lerpVal) + sheepColors2[1] * lerpVal;
                                    blue = sheepColors1[2] * (1.0f - lerpVal) + sheepColors2[2] * lerpVal;
                                }
                                armorScale = 1.04f;
                            }
                            if (wayY < 0) {
                                GLShim.glColor4f(red, green, blue, contact.brightness);
                            }
                            else {
                                GLShim.glColor3f(red * contact.brightness, green * contact.brightness, blue * contact.brightness);
                            }
                        }
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, (float)x, y + yOffset + armorOffset, (float)(int)(icon.getIconWidth() / 4.0f * armorScale));
                        GLUtils.drawPost();
                        if (icon == this.clothIcon) {
                            if (wayY < 0) {
                                GLShim.glColor4f(1.0f, 1.0f, 1.0f, contact.brightness);
                            }
                            else {
                                GLShim.glColor3f(1.0f * contact.brightness, 1.0f * contact.brightness, 1.0f * contact.brightness);
                            }
                            icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[2]);
                            this.applyFilteringParameters();
                            GLUtils.drawPre();
                            GLUtils.setMap(icon, (float)x, y + yOffset + armorOffset, icon.getIconWidth() / 4.0f * armorScale);
                            GLUtils.drawPost();
                            if (wayY < 0) {
                                GLShim.glColor4f(red, green, blue, contact.brightness);
                            }
                            else {
                                GLShim.glColor3f(red * contact.brightness, green * contact.brightness, blue * contact.brightness);
                            }
                            icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[1]);
                            this.applyFilteringParameters();
                            GLUtils.drawPre();
                            GLUtils.setMap(icon, (float)x, y + yOffset + armorOffset, icon.getIconWidth() / 4.0f * armorScale * 40.0f / 37.0f);
                            GLUtils.drawPost();
                            GLShim.glColor3f(1.0f, 1.0f, 1.0f);
                            icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[3]);
                            this.applyFilteringParameters();
                            GLUtils.drawPre();
                            GLUtils.setMap(icon, (float)x, y + yOffset + armorOffset, icon.getIconWidth() / 4.0f * armorScale * 40.0f / 37.0f);
                            GLUtils.drawPost();
                        }
                    }
                    else if (contact.uuid != null && contact.uuid.equals(this.devUUID)) {
                        final Sprite icon = this.textureAtlas.getAtlasSprite("crown");
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, (float)x, y + yOffset, icon.getIconWidth() / 4.0f);
                        GLUtils.drawPost();
                    }
                    if (contact.name == null || ((this.options.showPlayerNames || contact.type != EnumMobs.PLAYER) && (!this.options.showMobNames || contact.type == EnumMobs.PLAYER))) {
                        continue;
                    }
                    final float scaleFactor = this.layoutVariables.scScale / this.options.fontScale;
                    matrixStack.scale(1.0f / scaleFactor, 1.0f / scaleFactor, 1.0f);
                    RenderSystem.applyModelViewMatrix();
                    final int m = this.chkLen(contact.name) / 2;
                    this.write(contact.name, x * scaleFactor - m, (y + 3) * scaleFactor, 16777215);
                }
                catch (final Exception localException) {
                    System.err.println("Error rendering mob icon! " + localException.getLocalizedMessage() + " contact type " + contact.type);
                }
                finally {
                    matrixStack.popPose();
                    RenderSystem.applyModelViewMatrix();
                }
            }
        }
    }
    
    private void applyFilteringParameters() {
        if (this.options.filtering) {
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTexParameteri(3553, 10242, 10496);
            GLShim.glTexParameteri(3553, 10243, 10496);
        }
        else {
            GLShim.glTexParameteri(3553, 10241, 9728);
            GLShim.glTexParameteri(3553, 10240, 9728);
        }
    }
    
    private boolean isHostile(final Entity entity) {
        if (entity instanceof final ZombifiedPiglin zombifiedPiglinEntity) {
            return zombifiedPiglinEntity.isPreventingPlayerRest(this.game.player);
        }
        if (entity instanceof Enemy) {
            return true;
        }
        if (entity instanceof final Bee beeEntity) {
            return beeEntity.isAngry();
        }
        if (entity instanceof final PolarBear polarBearEntity) {
            for (final Object object : polarBearEntity.level.getEntitiesOfClass(PolarBear.class, polarBearEntity.getBoundingBox().inflate(8.0, 4.0, 8.0))) {
                if (((PolarBear)object).isBaby()) {
                    return true;
                }
            }
        }
        if (entity instanceof final Rabbit rabbitEntity) {
            return rabbitEntity.getRabbitType() == 99;
        }
        if (entity instanceof final Wolf wolfEntity) {
            return wolfEntity.isAngry();
        }
        return false;
    }
    
    private boolean isPlayer(final Entity entity) {
        return entity instanceof RemotePlayer;
    }
    
    private boolean isNeutral(final Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof Player) && !this.isHostile(entity);
    }
    
    static {
        UNKNOWN = EnumMobs.UNKNOWN.ordinal();
        LEVEL_TO_ID = Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
            int2ObjectOpenHashMap.put(1, new ResourceLocation("stone"));
            int2ObjectOpenHashMap.put(2, new ResourceLocation("iron"));
            int2ObjectOpenHashMap.put(3, new ResourceLocation("gold"));
            int2ObjectOpenHashMap.put(4, new ResourceLocation("emerald"));
            int2ObjectOpenHashMap.put(5, new ResourceLocation("diamond"));
            return;
        });
        TEXTURES = Util.make(Maps.newEnumMap(Markings.class), enumMap -> {
            enumMap.put(Markings.NONE, null);
            enumMap.put(Markings.WHITE, new ResourceLocation("textures/entity/horse/horse_markings_white.png"));
            enumMap.put(Markings.WHITE_FIELD, new ResourceLocation("textures/entity/horse/horse_markings_whitefield.png"));
            enumMap.put(Markings.WHITE_DOTS, new ResourceLocation("textures/entity/horse/horse_markings_whitedots.png"));
            enumMap.put(Markings.BLACK_DOTS, new ResourceLocation("textures/entity/horse/horse_markings_blackdots.png"));
        });
    }
    
    private class ModelPartWithResourceLocation
    {
        ModelPart modelPart;
        ResourceLocation resourceLocation;
        
        public ModelPartWithResourceLocation(final ModelPart modelPart, final ResourceLocation resourceLocation) {
            this.modelPart = modelPart;
            this.resourceLocation = resourceLocation;
        }
    }
}
