// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import java.util.Optional;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import java.util.ArrayList;

public class WaypointContainer
{
    private ArrayList<Waypoint> wayPts;
    private Waypoint highlightedWaypoint;
    private Minecraft mc;
    public MapSettingsManager options;
    
    public WaypointContainer(final MapSettingsManager options) {
        this.wayPts = new ArrayList<Waypoint>();
        this.highlightedWaypoint = null;
        this.options = null;
        this.mc = Minecraft.getInstance();
        this.options = options;
    }
    
    public void addWaypoint(final Waypoint newWaypoint) {
        this.wayPts.add(newWaypoint);
    }
    
    public void removeWaypoint(final Waypoint waypoint) {
        this.wayPts.remove(waypoint);
    }
    
    public void setHighlightedWaypoint(final Waypoint highlightedWaypoint) {
        this.highlightedWaypoint = highlightedWaypoint;
    }
    
    private void sortWaypoints() {
        Collections.sort(this.wayPts, Collections.reverseOrder());
    }
    
    public void renderWaypoints(final float partialTicks, final PoseStack matrixStack, final boolean beacons, final boolean signs, final boolean withDepth, final boolean withoutDepth) {
        this.sortWaypoints();
        final Entity cameraEntity = this.options.game.getCameraEntity();
        final double renderPosX = GameVariableAccessShim.xCoordDouble();
        final double renderPosY = GameVariableAccessShim.yCoordDouble();
        final double renderPosZ = GameVariableAccessShim.zCoordDouble();
        GLShim.glEnable(2884);
        if (this.options.showBeacons && beacons) {
            GLShim.glDisable(3553);
            GLShim.glDisable(2896);
            GLShim.glEnable(2929);
            GLShim.glDepthMask(false);
            GLShim.glEnable(3042);
            GLShim.glBlendFunc(770, 1);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            final Matrix4f matrix4f = matrixStack.last().pose();
            for (final Waypoint pt : this.wayPts) {
                if (pt.isActive() || pt == this.highlightedWaypoint) {
                    final int x = pt.getX();
                    final int z = pt.getZ();
                    final LevelChunk chunk = this.mc.level.getChunk(x >> 4, z >> 4);
                    if (chunk == null || chunk.isEmpty() || !this.mc.level.hasChunk(x >> 4, z >> 4)) {
                        continue;
                    }
                    final double bottomOfWorld = 0.0 - renderPosY;
                    this.renderBeam(pt, x - renderPosX, bottomOfWorld, z - renderPosZ, 64.0f, matrix4f);
                }
            }
            GLShim.glDisable(3042);
            GLShim.glEnable(2896);
            GLShim.glEnable(3553);
            GLShim.glDepthMask(true);
        }
        if (this.options.showWaypoints && signs) {
            GLShim.glDisable(2896);
            GLShim.glEnable(3042);
            GLShim.glBlendFuncSeparate(770, 771, 1, 771);
            for (final Waypoint pt2 : this.wayPts) {
                if (pt2.isActive() || pt2 == this.highlightedWaypoint) {
                    final int x2 = pt2.getX();
                    final int z2 = pt2.getZ();
                    final int y = pt2.getY();
                    final double distance = Math.sqrt(pt2.getDistanceSqToEntity(cameraEntity));
                    if ((distance >= this.options.maxWaypointDisplayDistance && this.options.maxWaypointDisplayDistance >= 0 && pt2 != this.highlightedWaypoint) || this.options.game.options.hideGui) {
                        continue;
                    }
                    final boolean isPointedAt = this.isPointedAt(pt2, distance, cameraEntity, partialTicks);
                    final String label = pt2.name;
                    this.renderLabel(matrixStack, pt2, distance, isPointedAt, label, x2 - renderPosX, y - renderPosY - 0.5, z2 - renderPosZ, 64, withDepth, withoutDepth);
                }
            }
            if (this.highlightedWaypoint != null && !this.options.game.options.hideGui) {
                final int x3 = this.highlightedWaypoint.getX();
                final int z3 = this.highlightedWaypoint.getZ();
                final int y2 = this.highlightedWaypoint.getY();
                final double distance2 = Math.sqrt(this.highlightedWaypoint.getDistanceSqToEntity(cameraEntity));
                final boolean isPointedAt2 = this.isPointedAt(this.highlightedWaypoint, distance2, cameraEntity, partialTicks);
                this.renderLabel(matrixStack, this.highlightedWaypoint, distance2, isPointedAt2, "*&^TARget%$^", x3 - renderPosX, y2 - renderPosY - 0.5, z3 - renderPosZ, 64, withDepth, withoutDepth);
            }
            GLShim.glEnable(2929);
            GLShim.glDepthMask(true);
            GLShim.glDisable(3042);
        }
    }
    
    private boolean isPointedAt(final Waypoint waypoint, final double distance, final Entity cameraEntity, final Float partialTicks) {
        final Vec3 cameraPos = cameraEntity.getEyePosition((float)partialTicks);
        final double degrees = 5.0 + Math.min(5.0 / distance, 5.0);
        final double angle = degrees * 0.0174533;
        final double size = Math.sin(angle) * distance;
        final Vec3 cameraPosPlusDirection = cameraEntity.getViewVector((float)partialTicks);
        final Vec3 cameraPosPlusDirectionTimesDistance = cameraPos.add(cameraPosPlusDirection.x * distance, cameraPosPlusDirection.y * distance, cameraPosPlusDirection.z * distance);
        final AABB axisalignedbb = new AABB(waypoint.getX() + 0.5f - size, waypoint.getY() + 1.5f - size, waypoint.getZ() + 0.5f - size, waypoint.getX() + 0.5f + size, waypoint.getY() + 1.5f + size, waypoint.getZ() + 0.5f + size);
        final Optional<Vec3> raytraceresult = axisalignedbb.clip(cameraPos, cameraPosPlusDirectionTimesDistance);
        if (axisalignedbb.contains(cameraPos)) {
            if (distance >= 1.0) {
                return true;
            }
        }
        else if (raytraceresult.isPresent()) {
            return true;
        }
        return false;
    }
    
    private void renderBeam(final Waypoint par1EntityWaypoint, final double baseX, final double baseY, final double baseZ, final float par8, final Matrix4f matrix4f) {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        final int height = 256;
        final float brightness = 0.06f;
        final double topWidthFactor = 1.05;
        final double bottomWidthFactor = 1.05;
        final float r = par1EntityWaypoint.red;
        final float b = par1EntityWaypoint.blue;
        final float g = par1EntityWaypoint.green;
        for (int width = 0; width < 4; ++width) {
            vertexBuffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            double d6 = 0.1 + width * 0.2;
            d6 *= topWidthFactor;
            double d7 = 0.1 + width * 0.2;
            d7 *= bottomWidthFactor;
            for (int side = 0; side < 5; ++side) {
                float vertX2 = (float)(baseX + 0.5 - d6);
                float vertZ2 = (float)(baseZ + 0.5 - d6);
                if (side == 1 || side == 2) {
                    vertX2 += (float)(d6 * 2.0);
                }
                if (side == 2 || side == 3) {
                    vertZ2 += (float)(d6 * 2.0);
                }
                float vertX3 = (float)(baseX + 0.5 - d7);
                float vertZ3 = (float)(baseZ + 0.5 - d7);
                if (side == 1 || side == 2) {
                    vertX3 += (float)(d7 * 2.0);
                }
                if (side == 2 || side == 3) {
                    vertZ3 += (float)(d7 * 2.0);
                }
                vertexBuffer.vertex(matrix4f, vertX3, (float)baseY + 0.0f, vertZ3).color(r * brightness, g * brightness, b * brightness, 0.8f).endVertex();
                vertexBuffer.vertex(matrix4f, vertX2, (float)baseY + height, vertZ2).color(r * brightness, g * brightness, b * brightness, 0.8f).endVertex();
            }
            tessellator.end();
        }
    }
    
    private void renderLabel(final PoseStack matrixStack, final Waypoint pt, final double distance, boolean isPointedAt, String name, double baseX, double baseY, double baseZ, final int par9, final boolean withDepth, final boolean withoutDepth) {
        final boolean target = name == "*&^TARget%$^";
        if (target) {
            if (pt.red == 2.0f && pt.green == 0.0f && pt.blue == 0.0f) {
                name = "X:" + pt.getX() + ", Y:" + pt.getY() + ", Z:" + pt.getZ();
            }
            else {
                isPointedAt = false;
            }
        }
        name = name + " (" + (int)distance + "m)";
        final double maxDistance = Option.RENDER_DISTANCE.get(this.options.game.options) * 16.0 * 0.99;
        double adjustedDistance = distance;
        if (distance > maxDistance) {
            baseX = baseX / distance * maxDistance;
            baseY = baseY / distance * maxDistance;
            baseZ = baseZ / distance * maxDistance;
            adjustedDistance = maxDistance;
        }
        final float var14 = ((float)adjustedDistance * 0.1f + 1.0f) * 0.0266f;
        matrixStack.pushPose();
        matrixStack.translate((double)((float)baseX + 0.5f), (double)((float)baseY + 0.5f), (double)((float)baseZ + 0.5f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-this.mc.getEntityRenderDispatcher().camera.getYRot()));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(this.mc.getEntityRenderDispatcher().camera.getXRot()));
        matrixStack.scale(-var14, -var14, -var14);
        final Matrix4f matrix4f = matrixStack.last().pose();
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        float fade = (distance > 5.0) ? 1.0f : ((float)distance / 5.0f);
        fade = Math.min(fade, (pt.enabled || target) ? 1.0f : 0.3f);
        final float width = 10.0f;
        final float r = target ? 1.0f : pt.red;
        final float g = target ? 0.0f : pt.green;
        final float b = target ? 0.0f : pt.blue;
        final TextureAtlas textureAtlas = AbstractVoxelMap.getInstance().getWaypointManager().getTextureAtlas();
        Sprite icon = target ? textureAtlas.getAtlasSprite("voxelmap:images/waypoints/target.png") : textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint" + pt.imageSuffix + ".png");
        if (icon == textureAtlas.getMissingImage()) {
            icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint.png");
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        GLUtils.disp2(textureAtlas.getId());
        GLShim.glEnable(3553);
        if (withDepth) {
            GLShim.glDepthMask(distance < maxDistance);
            GLShim.glEnable(2929);
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            vertexBuffer.vertex(matrix4f, -width, -width, 0.0f).uv(icon.getMinU(), icon.getMinV()).color(r, g, b, 1.0f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, -width, width, 0.0f).uv(icon.getMinU(), icon.getMaxV()).color(r, g, b, 1.0f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, width, width, 0.0f).uv(icon.getMaxU(), icon.getMaxV()).color(r, g, b, 1.0f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, width, -width, 0.0f).uv(icon.getMaxU(), icon.getMinV()).color(r, g, b, 1.0f * fade).endVertex();
            tessellator.end();
        }
        if (withoutDepth) {
            GLShim.glDisable(2929);
            GLShim.glDepthMask(false);
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            vertexBuffer.vertex(matrix4f, -width, -width, 0.0f).uv(icon.getMinU(), icon.getMinV()).color(r, g, b, 0.3f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, -width, width, 0.0f).uv(icon.getMinU(), icon.getMaxV()).color(r, g, b, 0.3f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, width, width, 0.0f).uv(icon.getMaxU(), icon.getMaxV()).color(r, g, b, 0.3f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, width, -width, 0.0f).uv(icon.getMaxU(), icon.getMinV()).color(r, g, b, 0.3f * fade).endVertex();
            tessellator.end();
        }
        final Font fontRenderer = this.mc.font;
        if (isPointedAt && fontRenderer != null) {
            final byte elevateBy = -19;
            GLShim.glDisable(3553);
            GLShim.glEnable(32823);
            final int halfStringWidth = fontRenderer.width(name) / 2;
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            if (withDepth) {
                GLShim.glEnable(2929);
                GLShim.glDepthMask(distance < maxDistance);
                GLShim.glPolygonOffset(1.0f, 7.0f);
                vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 2), (float)(-2 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.6f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 2), (float)(9 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.6f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 2), (float)(9 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.6f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 2), (float)(-2 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.6f * fade).endVertex();
                tessellator.end();
                GLShim.glPolygonOffset(1.0f, 5.0f);
                vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 1), (float)(-1 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 1), (float)(8 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 1), (float)(8 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 1), (float)(-1 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                tessellator.end();
            }
            if (withoutDepth) {
                GLShim.glDisable(2929);
                GLShim.glDepthMask(false);
                GLShim.glPolygonOffset(1.0f, 11.0f);
                vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 2), (float)(-2 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 2), (float)(9 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 2), (float)(9 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 2), (float)(-2 + elevateBy), 0.0f).color(pt.red, pt.green, pt.blue, 0.15f * fade).endVertex();
                tessellator.end();
                GLShim.glPolygonOffset(1.0f, 9.0f);
                vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 1), (float)(-1 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(-halfStringWidth - 1), (float)(8 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 1), (float)(8 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                vertexBuffer.vertex(matrix4f, (float)(halfStringWidth + 1), (float)(-1 + elevateBy), 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
                tessellator.end();
            }
            GLShim.glDisable(32823);
            GLShim.glDepthMask(false);
            GLShim.glEnable(3553);
            final MultiBufferSource.BufferSource vertexConsumerProvider = this.mc.renderBuffers().bufferSource();
            if (withoutDepth) {
                int textColor = (int)(255.0f * fade) << 24 | 0xCCCCCC;
                GLShim.glDisable(2929);
                fontRenderer.drawInBatch((Component)new TextComponent(name), (float)(-fontRenderer.width(name) / 2), (float)elevateBy, textColor, false, matrix4f, (MultiBufferSource)vertexConsumerProvider, true, 0, 15728880);
                vertexConsumerProvider.endBatch();
                GLShim.glEnable(2929);
                textColor = ((int)(255.0f * fade) << 24 | 0xFFFFFF);
                fontRenderer.draw(matrixStack, name, (float)(-fontRenderer.width(name) / 2), (float)elevateBy, textColor);
            }
            GLShim.glEnable(3042);
        }
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.popPose();
    }
}
