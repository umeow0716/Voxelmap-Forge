// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.awt.Graphics2D;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;

public class BlockModel
{
    ArrayList<BlockFace> faces;
    BlockVertex[] longestSide;
    float failedToLoadX;
    float failedToLoadY;
    
    public BlockModel(final List<BakedQuad> quads, final float failedToLoadX, final float failedToLoadY) {
        this.failedToLoadX = failedToLoadX;
        this.failedToLoadY = failedToLoadY;
        this.faces = new ArrayList<BlockFace>();
        BakedQuad quad = null;
        final Iterator<BakedQuad> iterator = quads.iterator();
        while (iterator.hasNext()) {
            quad = iterator.next();
            final BlockFace face = new BlockFace(quad.getVertices());
            if (face.isClockwise && !face.isVertical) {
                this.faces.add(face);
            }
        }
        Collections.sort(this.faces);
        this.longestSide = new BlockVertex[2];
        float greatestLength = 0.0f;
        BlockFace face = null;
        final Iterator<BlockFace> iterator2 = this.faces.iterator();
        while (iterator2.hasNext()) {
            face = iterator2.next();
            final float uDiff = face.longestSide[0].u - face.longestSide[1].u;
            final float vDiff = face.longestSide[0].v - face.longestSide[1].v;
            final float segmentLength = (float)Math.sqrt(uDiff * uDiff + vDiff * vDiff);
            if (segmentLength > greatestLength) {
                greatestLength = segmentLength;
                this.longestSide = face.longestSide;
            }
        }
    }
    
    public int numberOfFaces() {
        return this.faces.size();
    }
    
    public ArrayList<BlockFace> getFaces() {
        return this.faces;
    }
    
    public BufferedImage getImage(final BufferedImage terrainImage) {
        final float terrainImageAspectRatio = terrainImage.getWidth() / (float)terrainImage.getHeight();
        final float longestSideUV = Math.max(Math.abs(this.longestSide[0].u - this.longestSide[1].u), Math.abs(this.longestSide[0].v - this.longestSide[1].v) / terrainImageAspectRatio);
        final float modelImageWidthUV = longestSideUV / Math.max(Math.abs(this.longestSide[0].x - this.longestSide[1].x), Math.abs(this.longestSide[0].z - this.longestSide[1].z));
        final int modelImageWidth = Math.round(modelImageWidthUV * terrainImage.getWidth());
        final BufferedImage modelImage = new BufferedImage(modelImageWidth, modelImageWidth, 6);
        Graphics2D g2 = modelImage.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());
        g2.dispose();
        BlockFace face = null;
        final Iterator<BlockFace> iterator = this.faces.iterator();
        while (iterator.hasNext()) {
            face = iterator.next();
            final float minU = face.getMinU();
            final float maxU = face.getMaxU();
            final float minV = face.getMinV();
            final float maxV = face.getMaxV();
            final float minX = face.getMinX();
            final float maxX = face.getMaxX();
            final float minZ = face.getMinZ();
            final float maxZ = face.getMaxZ();
            if (this.similarEnough(minU, minV, this.failedToLoadX, this.failedToLoadY)) {
                return null;
            }
            int faceImageX = Math.round(minX * modelImage.getWidth());
            int faceImageY = Math.round(minZ * modelImage.getHeight());
            int faceImageWidth = Math.round(maxX * modelImage.getWidth()) - faceImageX;
            int faceImageHeight = Math.round(maxZ * modelImage.getHeight()) - faceImageY;
            if (faceImageWidth == 0) {
                if (faceImageX > modelImageWidth - 1) {
                    faceImageX = modelImageWidth - 1;
                }
                faceImageWidth = 1;
            }
            if (faceImageHeight == 0) {
                if (faceImageY > modelImageWidth - 1) {
                    faceImageY = modelImageWidth - 1;
                }
                faceImageHeight = 1;
            }
            final int faceImageU = Math.round(minU * terrainImage.getWidth());
            final int faceImageV = Math.round(minV * terrainImage.getHeight());
            int faceImageUVWidth = Math.round(maxU * terrainImage.getWidth()) - faceImageU;
            int faceImageUVHeight = Math.round(maxV * terrainImage.getHeight()) - faceImageV;
            if (faceImageUVWidth == 0) {
                faceImageUVWidth = 1;
            }
            if (faceImageUVHeight == 0) {
                faceImageUVHeight = 1;
            }
            BufferedImage faceImage = terrainImage.getSubimage(faceImageU, faceImageV, faceImageUVWidth, faceImageUVHeight);
            if (faceImageWidth != faceImageUVWidth || faceImageHeight != faceImageUVHeight) {
                if (faceImageWidth == faceImageUVHeight && faceImageHeight == faceImageUVWidth) {
                    final BufferedImage tmp = new BufferedImage(faceImageWidth, faceImageHeight, 6);
                    final AffineTransform transform = new AffineTransform();
                    transform.translate(faceImage.getHeight() / 2, faceImage.getWidth() / 2);
                    transform.rotate(1.5707963267948966);
                    transform.translate(-faceImage.getWidth() / 2, -faceImage.getHeight() / 2);
                    final AffineTransformOp op = new AffineTransformOp(transform, 1);
                    faceImage = op.filter(faceImage, tmp);
                }
                else {
                    final BufferedImage tmp = new BufferedImage(faceImageWidth, faceImageHeight, 6);
                    g2 = tmp.createGraphics();
                    g2.drawImage(faceImage, 0, 0, faceImageWidth, faceImageHeight, null);
                    g2.dispose();
                    faceImage = tmp;
                }
            }
            g2 = modelImage.createGraphics();
            g2.drawImage(faceImage, faceImageX, faceImageY, null);
            g2.dispose();
        }
        return modelImage;
    }
    
    private boolean similarEnough(final float a, final float b, final float one, final float two) {
        boolean similar = Math.abs(a - one) < 1.0E-4;
        similar = (similar && Math.abs(b - two) < 1.0E-4);
        return similar;
    }
    
    public class BlockFace implements Comparable<BlockFace>
    {
        BlockVertex[] vertices;
        boolean isHorizontal;
        boolean isVertical;
        boolean isClockwise;
        float yLevel;
        BlockVertex[] longestSide;
        
        BlockFace(final int[] values) {
            final int arraySize = values.length;
            final int intsPerVertex = arraySize / 4;
            this.vertices = new BlockVertex[4];
            for (int t = 0; t < 4; ++t) {
                final float x = Float.intBitsToFloat(values[t * intsPerVertex + 0]);
                final float y = Float.intBitsToFloat(values[t * intsPerVertex + 1]);
                final float z = Float.intBitsToFloat(values[t * intsPerVertex + 2]);
                final float u = Float.intBitsToFloat(values[t * intsPerVertex + 4]);
                final float v = Float.intBitsToFloat(values[t * intsPerVertex + 5]);
                this.vertices[t] = new BlockVertex(x, y, z, u, v);
            }
            this.isHorizontal = this.checkIfHorizontal();
            this.isVertical = this.checkIfVertical();
            this.isClockwise = this.checkIfClockwise();
            this.yLevel = this.calculateY();
            this.longestSide = this.getLongestSide();
        }
        
        private boolean checkIfHorizontal() {
            boolean isHorizontal = true;
            final float initialY = this.vertices[0].y;
            for (int t = 1; t < this.vertices.length; ++t) {
                if (this.vertices[t].y != initialY) {
                    isHorizontal = false;
                }
            }
            return isHorizontal;
        }
        
        private boolean checkIfVertical() {
            boolean allSameX = true;
            boolean allSameZ = true;
            final float initialX = this.vertices[0].x;
            final float initialZ = this.vertices[0].z;
            for (int t = 1; t < this.vertices.length; ++t) {
                if (this.vertices[t].x != initialX) {
                    allSameX = false;
                }
                if (this.vertices[t].z != initialZ) {
                    allSameZ = false;
                }
            }
            return allSameX || allSameZ;
        }
        
        private boolean checkIfClockwise() {
            float sum = 0.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                sum += (this.vertices[(t == this.vertices.length - 1) ? 0 : (t + 1)].x - this.vertices[t].x) * (this.vertices[(t == this.vertices.length - 1) ? 0 : (t + 1)].z + this.vertices[t].z);
            }
            return sum > 0.0f;
        }
        
        private float calculateY() {
            float sum = 0.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                sum += this.vertices[t].y;
            }
            sum /= this.vertices.length;
            return sum;
        }
        
        private BlockVertex[] getLongestSide() {
            float greatestLength = -1.0f;
            BlockVertex[] longestSide = new BlockVertex[0];
            for (int t = 0; t < this.vertices.length; ++t) {
                final float uDiff = this.vertices[t].u - this.vertices[(t == this.vertices.length - 1) ? 0 : (t + 1)].u;
                final float vDiff = this.vertices[t].v - this.vertices[(t == this.vertices.length - 1) ? 0 : (t + 1)].v;
                final float segmentLength = (float)Math.sqrt(uDiff * uDiff + vDiff * vDiff);
                if (segmentLength > greatestLength) {
                    greatestLength = segmentLength;
                    longestSide = new BlockVertex[] { this.vertices[t], this.vertices[(t == this.vertices.length - 1) ? 0 : (t + 1)] };
                }
            }
            return longestSide;
        }
        
        public float getMinX() {
            float minX = 1.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].x < minX) {
                    minX = this.vertices[t].x;
                }
            }
            return minX;
        }
        
        public float getMaxX() {
            float maxX = 0.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].x > maxX) {
                    maxX = this.vertices[t].x;
                }
            }
            return maxX;
        }
        
        public float getMinZ() {
            float minZ = 1.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].z < minZ) {
                    minZ = this.vertices[t].z;
                }
            }
            return minZ;
        }
        
        public float getMaxZ() {
            float maxZ = 0.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].z > maxZ) {
                    maxZ = this.vertices[t].z;
                }
            }
            return maxZ;
        }
        
        public float getMinU() {
            float minU = 1.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].u < minU) {
                    minU = this.vertices[t].u;
                }
            }
            return minU;
        }
        
        public float getMaxU() {
            float maxU = 0.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].u > maxU) {
                    maxU = this.vertices[t].u;
                }
            }
            return maxU;
        }
        
        public float getMinV() {
            float minV = 1.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].v < minV) {
                    minV = this.vertices[t].v;
                }
            }
            return minV;
        }
        
        public float getMaxV() {
            float maxV = 0.0f;
            for (int t = 0; t < this.vertices.length; ++t) {
                if (this.vertices[t].v > maxV) {
                    maxV = this.vertices[t].v;
                }
            }
            return maxV;
        }
        
        @Override
        public int compareTo(final BlockFace compareTo) {
            if (this.yLevel > compareTo.yLevel) {
                return 1;
            }
            if (this.yLevel < compareTo.yLevel) {
                return -1;
            }
            return 0;
        }
    }
    
    private class BlockVertex
    {
        float x;
        float y;
        float z;
        float u;
        float v;
        
        BlockVertex(final float x, final float y, final float z, final float u, final float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }
}
