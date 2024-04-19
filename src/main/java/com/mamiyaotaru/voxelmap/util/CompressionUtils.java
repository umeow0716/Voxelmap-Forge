// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class CompressionUtils
{
    public static byte[] compress(final byte[] dataToCompress) throws IOException {
        final Deflater deflater = new Deflater();
        deflater.setLevel(1);
        deflater.setInput(dataToCompress);
        deflater.finish();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataToCompress.length);
        final byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            final int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        deflater.end();
        final byte[] output = outputStream.toByteArray();
        return output;
    }
    
    public static byte[] decompress(final byte[] dataToDecompress) throws IOException, DataFormatException {
        final Inflater inflater = new Inflater();
        inflater.setInput(dataToDecompress);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataToDecompress.length);
        final byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            final int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        inflater.end();
        final byte[] output = outputStream.toByteArray();
        return output;
    }
}
