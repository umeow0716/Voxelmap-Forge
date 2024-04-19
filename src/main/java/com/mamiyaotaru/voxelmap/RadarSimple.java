// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;

import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import java.util.Collections;
import java.util.Comparator;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import java.awt.image.BufferedImage;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import java.util.UUID;
import com.mamiyaotaru.voxelmap.util.Contact;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import com.mamiyaotaru.voxelmap.interfaces.IRadar;

public class RadarSimple implements IRadar
{
    private Minecraft game;
    private LayoutVariables layoutVariables;
    public MapSettingsManager minimapOptions;
    public RadarSettingsManager options;
    private TextureAtlas textureAtlas;
    private boolean enabled;
    private boolean completedLoading;
    private int timer;
    private float direction;
    private ArrayList<Contact> contacts;
    UUID devUUID;
    
    public RadarSimple(final IVoxelMap master) {
        this.layoutVariables = null;
        this.minimapOptions = null;
        this.options = null;
        this.enabled = true;
        this.completedLoading = false;
        this.timer = 500;
        this.direction = 0.0f;
        this.contacts = new ArrayList<Contact>(40);
        this.devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
        this.minimapOptions = master.getMapOptions();
        this.options = master.getRadarOptions();
        this.game = Minecraft.getInstance();
        (this.textureAtlas = new TextureAtlas("pings")).setFilter(false, false);
    }
    
    @Override
    public void onResourceManagerReload(final ResourceManager resourceManager) {
        this.loadTexturePackIcons();
    }
    
    private void loadTexturePackIcons() {
        this.completedLoading = false;
        try {
            this.textureAtlas.reset();
            BufferedImage contact = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/contact.png"), 0, 0, 32, 32, 32, 32);
            contact = ImageUtils.fillOutline(contact, false, true, 32.0f, 32.0f, 0);
            this.textureAtlas.registerIconForBufferedImage("contact", contact);
            BufferedImage facing = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/contact_facing.png"), 0, 0, 32, 32, 32, 32);
            facing = ImageUtils.fillOutline(facing, false, true, 32.0f, 32.0f, 0);
            this.textureAtlas.registerIconForBufferedImage("facing", facing);
            BufferedImage glow = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/glow.png"), 0, 0, 16, 16, 16, 16);
            glow = ImageUtils.fillOutline(glow, false, true, 16.0f, 16.0f, 0);
            this.textureAtlas.registerIconForBufferedImage("glow", glow);
            this.textureAtlas.stitch();
            this.completedLoading = true;
        }
        catch (final Exception e) {
            System.err.println("Failed getting mobs " + e.getLocalizedMessage());
            e.printStackTrace();
        }
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
    
    public void calculateMobs() {
        this.contacts.clear();
        final Iterable<Entity> entities = this.game.level.entitiesForRendering();
        for (final Entity entity : entities) {
            try {
                if (entity == null || entity.isInvisibleTo((Player)this.game.player) || ((!this.options.showHostiles || (!this.options.radarAllowed && !this.options.radarMobsAllowed) || !this.isHostile(entity)) && (!this.options.showPlayers || (!this.options.radarAllowed && !this.options.radarPlayersAllowed) || !this.isPlayer(entity)) && (!this.options.showNeutrals || !this.options.radarMobsAllowed || !this.isNeutral(entity)))) {
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
                final Contact contact = new Contact(entity, this.getUnknownMobNeutrality(entity));
                final String unscrubbedName = contact.entity.getDisplayName().getString();
                contact.setName(unscrubbedName);
                contact.updateLocation();
                this.contacts.add(contact);
            }
            catch (final Exception e) {
                System.err.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        Collections.sort(this.contacts, new Comparator<Contact>() {
            @Override
            public int compare(final Contact contact1, final Contact contact2) {
                return contact1.y - contact2.y;
            }
        });
    }
    
    private EnumMobs getUnknownMobNeutrality(final Entity entity) {
        if (this.isHostile(entity)) {
            return EnumMobs.GENERICHOSTILE;
        }
        if (entity instanceof TamableAnimal && ((TamableAnimal)entity).isTame() && (this.game.hasSingleplayerServer() || ((TamableAnimal)entity).getOwner().equals(this.game.player))) {
            return EnumMobs.GENERICTAME;
        }
        return EnumMobs.GENERICNEUTRAL;
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
            for (final PolarBear polarBear : polarBearEntity.level.getEntitiesOfClass(PolarBear.class, polarBearEntity.getBoundingBox().inflate(8.0, 4.0, 8.0))) {
                if (polarBear.isBaby()) {
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
    
    public void renderMapMobs(final PoseStack matrixStack, final int x, final int y) {
        final double max = this.layoutVariables.zoomScaleAdjusted * 32.0;
        GLUtils.disp2(this.textureAtlas.getId());
        for (Contact contact : this.contacts) {
            contact.updateLocation();
            final double contactX = contact.x;
            final double contactZ = contact.z;
            final int contactY = contact.y;
            final double wayX = GameVariableAccessShim.xCoordDouble() - contactX;
            final double wayZ = GameVariableAccessShim.zCoordDouble() - contactZ;
            final int wayY = GameVariableAccessShim.yCoord() - contactY;
            final double adjustedDiff = max - Math.max(Math.abs(wayY) - 0, 0);
            contact.brightness = (float)Math.max(adjustedDiff / max, 0.0);
            final Contact contact2 = contact;
            contact2.brightness *= contact.brightness;
            contact.angle = (float)Math.toDegrees(Math.atan2(wayX, wayZ));
            contact.distance = Math.sqrt(wayX * wayX + wayZ * wayZ) / this.layoutVariables.zoomScaleAdjusted;
            GLShim.glBlendFunc(770, 771);
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
                    float contactFacing = contact.entity.getYHeadRot();
                    if (this.minimapOptions.rotates) {
                        contactFacing -= this.direction;
                    }
                    else if (this.minimapOptions.oldNorth) {
                        contactFacing += 90.0f;
                    }
                    matrixStack.translate((double)x, (double)y, 0.0);
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-contact.angle));
                    matrixStack.translate(0.0, -contact.distance, 0.0);
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(contact.angle + contactFacing));
                    matrixStack.translate((double)(-x), (double)(-y), 0.0);
                    RenderSystem.applyModelViewMatrix();
                    if (contact.uuid != null && contact.uuid.equals(this.devUUID)) {
                        final Sprite icon = this.textureAtlas.getAtlasSprite("glow");
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, (float)x, (float)y, (float)(int)(icon.getIconWidth() / 2.0f));
                        GLUtils.drawPost();
                    }
                    this.applyFilteringParameters();
                    GLUtils.drawPre();
                    GLUtils.setMap(this.textureAtlas.getAtlasSprite("contact"), (float)x, (float)y, 16.0f);
                    GLUtils.drawPost();
                    if (!this.options.showFacing) {
                        continue;
                    }
                    this.applyFilteringParameters();
                    GLUtils.drawPre();
                    GLUtils.setMap(this.textureAtlas.getAtlasSprite("facing"), (float)x, (float)y, 16.0f);
                    GLUtils.drawPost();
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
}
