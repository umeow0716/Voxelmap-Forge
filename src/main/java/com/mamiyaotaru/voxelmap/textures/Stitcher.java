// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.textures;

import java.text.Collator;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import java.util.ArrayList;

import net.minecraft.util.Mth;
import java.util.Arrays;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;

public class Stitcher
{
    private final Set<Holder> setStitchHolders;
    private final List<Slot> stitchSlots;
    private int currentWidth;
    private int currentHeight;
    private int currentWidthToPowerOfTwo;
    private int currentHeightToPowerOfTwo;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxTileDimension;
    
    public Stitcher(final int maxWidth, final int maxHeight, final int maxTileDimension) {
        this.setStitchHolders = Sets.newHashSetWithExpectedSize(256);
        this.stitchSlots = Lists.newArrayListWithCapacity(256);
        this.currentWidth = 0;
        this.currentHeight = 0;
        this.currentWidthToPowerOfTwo = 0;
        this.currentHeightToPowerOfTwo = 0;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxTileDimension = maxTileDimension;
    }
    
    public int getCurrentImageWidth() {
        return this.currentWidthToPowerOfTwo;
    }
    
    public int getCurrentImageHeight() {
        return this.currentHeightToPowerOfTwo;
    }
    
    public int getCurrentWidth() {
        return this.currentWidth;
    }
    
    public int getCurrentHeight() {
        return this.currentHeight;
    }
    
    public void addSprite(final Sprite icon) {
        final Holder holder = new Holder(icon);
        if (this.maxTileDimension > 0) {
            holder.setNewDimension(this.maxTileDimension);
        }
        this.setStitchHolders.add(holder);
    }
    
    public void doStitch() {
        final Holder[] stitchHoldersArray = this.setStitchHolders.toArray(new Holder[this.setStitchHolders.size()]);
        Arrays.sort(stitchHoldersArray);
        final Holder[] tempStitchHoldersArray = stitchHoldersArray;
        final int stitcherHoldersArrayLength = stitchHoldersArray.length;
        if (stitcherHoldersArrayLength > 0) {
            Holder holder = tempStitchHoldersArray[0];
            final int iconWidth = holder.width;
            final int iconHeight = holder.height;
            boolean allSameSize = true;
            for (int stitcherHolderIndex = 1; stitcherHolderIndex < stitcherHoldersArrayLength && allSameSize; allSameSize = (allSameSize && holder.width == iconWidth && holder.height == iconHeight), ++stitcherHolderIndex) {
                holder = tempStitchHoldersArray[stitcherHolderIndex];
            }
            if (allSameSize) {
                final int nextPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(stitcherHoldersArrayLength);
                final int power = Integer.numberOfTrailingZeros(nextPowerOfTwo);
                final int width = (int)Math.pow(2.0, Math.ceil(power / 2.0)) * iconWidth;
                final int height = (int)Math.pow(2.0, Math.floor(power / 2.0)) * iconHeight;
                this.currentWidth = width;
                this.currentHeight = height;
                this.currentWidthToPowerOfTwo = width;
                this.currentHeightToPowerOfTwo = height;
                final Slot slot = new Slot(0, 0, this.currentWidth, this.currentHeight);
                this.stitchSlots.add(slot);
            }
        }
        for (final Holder holder2 : tempStitchHoldersArray) {
            if (!this.allocateSlot(holder2)) {
                final String errorString = String.format("Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", holder2.getAtlasSprite().getIconName(), holder2.getAtlasSprite().getIconWidth(), holder2.getAtlasSprite().getIconHeight());
                throw new StitcherException(holder2, errorString);
            }
        }
        this.currentWidthToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentWidth);
        this.currentHeightToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentHeight);
        this.setStitchHolders.clear();
    }
    
    public void doStitchNew() {
        final Holder[] stitchHoldersArray = this.setStitchHolders.toArray(new Holder[this.setStitchHolders.size()]);
        Arrays.sort(stitchHoldersArray);
        final Holder[] tempStitchHoldersArray = stitchHoldersArray;
        for (int stitcherHoldersArrayLength = stitchHoldersArray.length, stitcherHolderIndex = 0; stitcherHolderIndex < stitcherHoldersArrayLength; ++stitcherHolderIndex) {
            final Holder holder = tempStitchHoldersArray[stitcherHolderIndex];
            if (!this.allocateSlot(holder)) {
                final String errorString = String.format("Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", holder.getAtlasSprite().getIconName(), holder.getAtlasSprite().getIconWidth(), holder.getAtlasSprite().getIconHeight());
                throw new StitcherException(holder, errorString);
            }
        }
        this.currentWidthToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentWidth);
        this.currentHeightToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentHeight);
        this.setStitchHolders.clear();
    }
    
    public List<Sprite> getStitchSlots() {
        final ArrayList<Slot> listOfStitchSlots = Lists.newArrayList();
        for (final Slot slot : this.stitchSlots) {
            slot.getAllStitchSlots(listOfStitchSlots);
        }
        final ArrayList<Sprite> spritesList = Lists.newArrayList();
        for (final Slot stitcherSlot : listOfStitchSlots) {
            final Holder stitcherHolder = stitcherSlot.getStitchHolder();
            final Sprite icon = stitcherHolder.getAtlasSprite();
            icon.initSprite(this.currentWidthToPowerOfTwo, this.currentHeightToPowerOfTwo, stitcherSlot.getOriginX(), stitcherSlot.getOriginY());
            spritesList.add(icon);
        }
        return spritesList;
    }
    
    private boolean allocateSlot(final Holder holder) {
        for (int stitcherSlotsIndex = 0; stitcherSlotsIndex < this.stitchSlots.size(); ++stitcherSlotsIndex) {
            if (this.stitchSlots.get(stitcherSlotsIndex).addSlot(holder)) {
                return true;
            }
        }
        return this.expandAndAllocateSlot(holder);
    }
    
    private boolean expandAndAllocateSlot(final Holder holder) {
        final int expandBy = holder.getWidth();
        final int currentWidthToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentWidth);
        final int currentHeightToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentHeight);
        final int possibleNewWidthToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentWidth + expandBy);
        final int possibleNewHeightToPowerOfTwo = Mth.smallestEncompassingPowerOfTwo(this.currentHeight + expandBy);
        final boolean isRoomToExpandRight = possibleNewWidthToPowerOfTwo <= this.maxWidth;
        final boolean isRoomToExpandDown = possibleNewHeightToPowerOfTwo <= this.maxHeight;
        if (!isRoomToExpandRight && !isRoomToExpandDown) {
            return false;
        }
        final boolean widthWouldChange = currentWidthToPowerOfTwo != possibleNewWidthToPowerOfTwo;
        final boolean heightWouldChange = currentHeightToPowerOfTwo != possibleNewHeightToPowerOfTwo;
        boolean shouldExpandRight;
        if (widthWouldChange ^ heightWouldChange) {
            shouldExpandRight = !widthWouldChange;
        }
        else {
            shouldExpandRight = (isRoomToExpandRight && currentWidthToPowerOfTwo <= currentHeightToPowerOfTwo);
        }
        if (Mth.smallestEncompassingPowerOfTwo((shouldExpandRight ? this.currentWidth : this.currentHeight) + expandBy) > (shouldExpandRight ? this.maxWidth : this.maxHeight)) {
            return false;
        }
        Slot slot;
        if (shouldExpandRight) {
            if (this.currentHeight == 0) {
                this.currentHeight = holder.getHeight();
            }
            slot = new Slot(this.currentWidth, 0, holder.getWidth(), this.currentHeight);
            this.currentWidth += holder.getWidth();
        }
        else {
            slot = new Slot(0, this.currentHeight, this.currentWidth, holder.getHeight());
            this.currentHeight += holder.getHeight();
        }
        if (!slot.addSlot(holder)) {
            final String errorString = String.format("Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", holder.getAtlasSprite().getIconName(), holder.getAtlasSprite().getIconWidth(), holder.getAtlasSprite().getIconHeight());
            System.err.println(errorString);
        }
        this.stitchSlots.add(slot);
        return true;
    }
    
    public class Holder implements Comparable<Holder>
    {
        private final Sprite icon;
        private final int width;
        private final int height;
        private float scaleFactor;
        
        public Holder(final Sprite icon) {
            this.scaleFactor = 1.0f;
            this.icon = icon;
            this.width = icon.getIconWidth();
            this.height = icon.getIconHeight();
        }
        
        public Sprite getAtlasSprite() {
            return this.icon;
        }
        
        public int getWidth() {
            return (int)(this.width * this.scaleFactor);
        }
        
        public int getHeight() {
            return (int)(this.height * this.scaleFactor);
        }
        
        public void setNewDimension(final int newDimension) {
            if (this.width > newDimension && this.height > newDimension) {
                this.scaleFactor = newDimension / (float)Math.min(this.width, this.height);
            }
        }
        
        @Override
        public int compareTo(final Holder compareTo) {
            int var2;
            if (this.getHeight() == compareTo.getHeight()) {
                if (this.getWidth() == compareTo.getWidth()) {
                    if (this.icon.getIconName() == null) {
                        return (compareTo.icon.getIconName() == null) ? 0 : -1;
                    }
                    final Collator collator = I18nUtils.getLocaleAwareCollator();
                    return collator.compare(this.icon.getIconName(), compareTo.icon.getIconName());
                }
                else {
                    var2 = ((this.getWidth() < compareTo.getWidth()) ? 1 : -1);
                }
            }
            else {
                var2 = ((this.getHeight() < compareTo.getHeight()) ? 1 : -1);
            }
            return var2;
        }
    }
    
    public class Slot
    {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private int failsAt;
        private List<Slot> subSlots;
        private Holder holder;
        
        public Slot(final int originX, final int originY, final int width, final int height) {
            this.failsAt = Stitcher.this.maxWidth;
            this.originX = originX;
            this.originY = originY;
            this.width = width;
            this.height = height;
        }
        
        public Holder getStitchHolder() {
            return this.holder;
        }
        
        public int getOriginX() {
            return this.originX;
        }
        
        public int getOriginY() {
            return this.originY;
        }
        
        public boolean addSlot(final Holder holder) {
            if (holder.width >= this.failsAt) {
                return false;
            }
            if (this.holder != null) {
                this.failsAt = 0;
                return false;
            }
            final int holderWidth = holder.getWidth();
            final int holderHeight = holder.getHeight();
            if (holderWidth > this.width || holderHeight > this.height) {
                this.failsAt = holder.width;
                return false;
            }
            if (holderWidth == this.width && holderHeight == this.height) {
                this.holder = holder;
                return true;
            }
            if (this.subSlots == null) {
                (this.subSlots = Lists.newArrayListWithCapacity(1)).add(new Slot(this.originX, this.originY, holderWidth, holderHeight));
                final int excessWidth = this.width - holderWidth;
                final int excessHeight = this.height - holderHeight;
                if (excessHeight > 0 && excessWidth > 0) {
                    final int var6 = Math.max(this.height, excessWidth);
                    final int var7 = Math.max(this.width, excessHeight);
                    if (var6 > var7) {
                        this.subSlots.add(new Slot(this.originX, this.originY + holderHeight, holderWidth, excessHeight));
                        this.subSlots.add(new Slot(this.originX + holderWidth, this.originY, excessWidth, this.height));
                    }
                    else {
                        this.subSlots.add(new Slot(this.originX + holderWidth, this.originY, excessWidth, holderHeight));
                        this.subSlots.add(new Slot(this.originX, this.originY + holderHeight, this.width, excessHeight));
                    }
                }
                else if (excessWidth == 0) {
                    this.subSlots.add(new Slot(this.originX, this.originY + holderHeight, holderWidth, excessHeight));
                }
                else if (excessHeight == 0) {
                    this.subSlots.add(new Slot(this.originX + holderWidth, this.originY, excessWidth, holderHeight));
                }
            }
            for (final Slot slot : this.subSlots) {
                if (slot.addSlot(holder)) {
                    return true;
                }
            }
            this.failsAt = holder.width;
            return false;
        }
        
        public void getAllStitchSlots(final List<Slot> listOfStitchSlots) {
            if (this.holder != null) {
                listOfStitchSlots.add(this);
            }
            else if (this.subSlots != null) {
                for (final Slot slot : this.subSlots) {
                    slot.getAllStitchSlots(listOfStitchSlots);
                }
            }
        }
    }
}
