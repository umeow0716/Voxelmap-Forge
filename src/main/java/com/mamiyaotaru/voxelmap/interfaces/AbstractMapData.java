// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import java.util.Collection;
import java.util.ArrayList;

public abstract class AbstractMapData implements IMapData
{
    protected int width;
    protected int height;
    protected Object dataLock;
    private Object labelLock;
    public Point[][] points;
    public ArrayList<Segment> segments;
    private ArrayList<BiomeLabel> labels;
    
    public AbstractMapData() {
        this.dataLock = new Object();
        this.labelLock = new Object();
        this.labels = new ArrayList<BiomeLabel>();
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    public void segmentBiomes() {
        this.points = new Point[this.width][this.height];
        this.segments = new ArrayList<Segment>();
        for (int x = 0; x < this.width; ++x) {
            for (int z = 0; z < this.height; ++z) {
                this.points[x][z] = new Point(x, z, this.getBiomeID(x, z));
            }
        }
        synchronized (this.dataLock) {
            for (int x2 = 0; x2 < this.width; ++x2) {
                for (int z2 = 0; z2 < this.height; ++z2) {
                    if (!this.points[x2][z2].inSegment) {
                        final long startTime = System.nanoTime();
                        if (this.points[x2][z2].biomeID == -1) {
                            System.out.println("no biome segment!");
                        }
                        final Segment segment = new Segment(this.points[x2][z2]);
                        this.segments.add(segment);
                        segment.flood();
                        if (this.points[x2][z2].biomeID == -1) {
                            System.out.println("created in " + (System.nanoTime() - startTime));
                        }
                    }
                }
            }
        }
    }
    
    public void findCenterOfSegments(final boolean horizontalBias) {
        if (this.segments != null) {
            for (final Segment segment : this.segments) {
                if (segment.biomeID != -1) {
                    segment.calculateCenter(horizontalBias);
                }
            }
        }
        synchronized (this.labelLock) {
            this.labels.clear();
            if (this.segments != null) {
                for (final Segment segment2 : this.segments) {
                    if (segment2.biomeID != -1) {
                        final BiomeLabel label = new BiomeLabel();
                        label.biomeID = segment2.biomeID;
                        label.name = segment2.name;
                        label.segmentSize = segment2.memberPoints.size();
                        label.x = segment2.centerX;
                        label.z = segment2.centerZ;
                        this.labels.add(label);
                    }
                }
            }
        }
    }
    
    public ArrayList<BiomeLabel> getBiomeLabels() {
        final ArrayList<BiomeLabel> labelsToReturn = new ArrayList<BiomeLabel>();
        synchronized (this.labelLock) {
            labelsToReturn.addAll(this.labels);
        }
        return labelsToReturn;
    }
    
    private class Point
    {
        public int x;
        public int z;
        public boolean inSegment;
        public boolean isCandidate;
        public int layer;
        public int biomeID;
        
        public Point(final int x, final int z, int biomeID) {
            this.inSegment = false;
            this.isCandidate = false;
            this.layer = -1;
            this.biomeID = -1;
            this.x = x;
            this.z = z;
            if (biomeID == 255 || biomeID == -1) {
                biomeID = -1;
                this.inSegment = true;
            }
            this.biomeID = biomeID;
        }
    }
    
    public class Segment
    {
        public ArrayList<Point> memberPoints;
        ArrayList<Point> currentShell;
        public int biomeID;
        public String name;
        public int centerX;
        public int centerZ;
        
        public Segment(final Point point) {
            this.name = null;
            this.centerX = 0;
            this.centerZ = 0;
            this.biomeID = point.biomeID;
            if (this.biomeID != -1) {
                this.name = BiomeRepository.getName(this.biomeID);
            }
            (this.memberPoints = new ArrayList<Point>()).add(point);
            this.currentShell = new ArrayList<Point>();
        }
        
        public void flood() {
            final ArrayList<Point> candidatePoints = new ArrayList<Point>();
            candidatePoints.add(this.memberPoints.remove(0));
            while (candidatePoints.size() > 0) {
                final Point point = candidatePoints.remove(0);
                point.isCandidate = false;
                if (point.biomeID == this.biomeID) {
                    this.memberPoints.add(point);
                    point.inSegment = true;
                    boolean edge = false;
                    if (point.x < AbstractMapData.this.width - 1) {
                        final Point neighbor = AbstractMapData.this.points[point.x + 1][point.z];
                        if (!neighbor.inSegment && !neighbor.isCandidate) {
                            candidatePoints.add(neighbor);
                            neighbor.isCandidate = true;
                        }
                        if (neighbor.biomeID != point.biomeID) {
                            edge = true;
                        }
                    }
                    else {
                        edge = true;
                    }
                    if (point.x > 0) {
                        final Point neighbor = AbstractMapData.this.points[point.x - 1][point.z];
                        if (!neighbor.inSegment && !neighbor.isCandidate) {
                            candidatePoints.add(neighbor);
                            neighbor.isCandidate = true;
                        }
                        if (neighbor.biomeID != point.biomeID) {
                            edge = true;
                        }
                    }
                    else {
                        edge = true;
                    }
                    if (point.z < AbstractMapData.this.height - 1) {
                        final Point neighbor = AbstractMapData.this.points[point.x][point.z + 1];
                        if (!neighbor.inSegment && !neighbor.isCandidate) {
                            candidatePoints.add(neighbor);
                            neighbor.isCandidate = true;
                        }
                        if (neighbor.biomeID != point.biomeID) {
                            edge = true;
                        }
                    }
                    else {
                        edge = true;
                    }
                    if (point.z > 0) {
                        final Point neighbor = AbstractMapData.this.points[point.x][point.z - 1];
                        if (!neighbor.inSegment && !neighbor.isCandidate) {
                            candidatePoints.add(neighbor);
                            neighbor.isCandidate = true;
                        }
                        if (neighbor.biomeID != point.biomeID) {
                            edge = true;
                        }
                    }
                    else {
                        edge = true;
                    }
                    if (!edge) {
                        continue;
                    }
                    point.layer = 0;
                    this.currentShell.add(point);
                }
            }
        }
        
        public void calculateCenter(final boolean horizontalBias) {
            this.calculateCenterOfMass();
            this.morphologicallyErode(horizontalBias);
        }
        
        public void calculateCenterOfMass() {
            this.calculateCenterOfMass(this.memberPoints);
        }
        
        public void calculateCenterOfMass(final Collection<Point> points) {
            this.centerX = 0;
            this.centerZ = 0;
            for (final Point point : points) {
                this.centerX += point.x;
                this.centerZ += point.z;
            }
            this.centerX /= points.size();
            this.centerZ /= points.size();
        }
        
        public void calculateClosestPointToCenter(final Collection<Point> points) {
            int distanceSquared = AbstractMapData.this.width * AbstractMapData.this.width + AbstractMapData.this.height * AbstractMapData.this.height;
            Point centerPoint = null;
            for (final Point point : points) {
                final int pointDistanceSquared = (point.x - this.centerX) * (point.x - this.centerX) + (point.z - this.centerZ) * (point.z - this.centerZ);
                if (pointDistanceSquared < distanceSquared) {
                    distanceSquared = pointDistanceSquared;
                    centerPoint = point;
                }
            }
            this.centerX = centerPoint.x;
            this.centerZ = centerPoint.z;
        }
        
        @SuppressWarnings("resource")
		public void morphologicallyErode(final boolean horizontalBias) {
            final float labelWidth = (float)(Minecraft.getInstance().font.width(this.name) + 8);
            final float multi = (float)(AbstractMapData.this.width / 32);
            final float shellWidth = 2.0f;
            float labelPadding;
            int layer;
            for (labelPadding = labelWidth / 16.0f * multi / shellWidth, layer = 0; this.currentShell.size() > 0 && layer < labelPadding; ++layer, this.currentShell = this.getNextShell(this.currentShell, layer, horizontalBias)) {}
            if (this.currentShell.size() > 0) {
                final ArrayList<Point> remainingPoints = new ArrayList<Point>();
                for (final Point point : this.memberPoints) {
                    if (point.layer < 0 || point.layer == layer) {
                        remainingPoints.add(point);
                    }
                }
                this.calculateClosestPointToCenter(remainingPoints);
            }
        }
        
        public ArrayList<Point> getNextShell(final Collection<Point> pointsToCheck, final int layer, final boolean horizontalBias) {
            final int layerWidth = horizontalBias ? 2 : 1;
            final int layerHeight = horizontalBias ? 1 : 2;
            final ArrayList<Point> nextShell = new ArrayList<Point>();
            for (final Point point : pointsToCheck) {
                if (point.x < AbstractMapData.this.width - layerWidth) {
                    boolean foundEdge = false;
                    for (int t = layerWidth; t > 0; --t) {
                        final Point neighbor = AbstractMapData.this.points[point.x + t][point.z];
                        if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
                            neighbor.layer = layer;
                            if (!foundEdge) {
                                foundEdge = true;
                                nextShell.add(neighbor);
                            }
                        }
                    }
                }
                if (point.x >= layerWidth) {
                    boolean foundEdge = false;
                    for (int t = layerWidth; t > 0; --t) {
                        final Point neighbor = AbstractMapData.this.points[point.x - t][point.z];
                        if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
                            neighbor.layer = layer;
                            if (!foundEdge) {
                                foundEdge = true;
                                nextShell.add(neighbor);
                            }
                        }
                    }
                }
                if (point.z < AbstractMapData.this.height - layerHeight) {
                    boolean foundEdge = false;
                    for (int t = layerHeight; t > 0; --t) {
                        final Point neighbor = AbstractMapData.this.points[point.x][point.z + t];
                        if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
                            neighbor.layer = layer;
                            if (!foundEdge) {
                                foundEdge = true;
                                nextShell.add(neighbor);
                            }
                        }
                    }
                }
                if (point.z >= layerHeight) {
                    boolean foundEdge = false;
                    for (int t = layerHeight; t > 0; --t) {
                        final Point neighbor = AbstractMapData.this.points[point.x][point.z - t];
                        if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
                            neighbor.layer = layer;
                            if (!foundEdge) {
                                foundEdge = true;
                                nextShell.add(neighbor);
                            }
                        }
                    }
                }
            }
            if (nextShell.size() > 0) {
                return nextShell;
            }
            this.calculateCenterOfMass(pointsToCheck);
            this.calculateClosestPointToCenter(pointsToCheck);
            return nextShell;
        }
    }
    
    public class BiomeLabel
    {
        public int biomeID;
        public String name;
        public int segmentSize;
        public int x;
        public int z;
        
        public BiomeLabel() {
            this.biomeID = -1;
            this.name = "";
            this.segmentSize = 0;
            this.x = 0;
            this.z = 0;
        }
    }
}
