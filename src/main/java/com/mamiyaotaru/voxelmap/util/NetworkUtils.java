// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InterfaceAddress;
import java.util.ArrayList;

public class NetworkUtils
{
    private static ArrayList<InterfaceAddress> interfaceAddresses;
    
    public static void enumerateInterfaces() throws SocketException {
        NetworkUtils.interfaceAddresses = new ArrayList<InterfaceAddress>();
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            try {
                final NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface == null || networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                final Enumeration<NetworkInterface> subInterfaces = networkInterface.getSubInterfaces();
                while (subInterfaces.hasMoreElements()) {
                    try {
                        final NetworkInterface subNetworkInterface = subInterfaces.nextElement();
                        for (final InterfaceAddress interfaceAddress : subNetworkInterface.getInterfaceAddresses()) {
                            if (interfaceAddress != null) {
                                NetworkUtils.interfaceAddresses.add(interfaceAddress);
                            }
                        }
                    }
                    catch (final Exception ex) {}
                }
                for (final InterfaceAddress interfaceAddress2 : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress2 != null) {
                        NetworkUtils.interfaceAddresses.add(interfaceAddress2);
                    }
                }
            }
            catch (final Exception ex2) {}
        }
    }
    
    public static boolean isOnLan(final InetAddress serverAddress) {
        for (int t = 0; t < NetworkUtils.interfaceAddresses.size(); ++t) {
            try {
                final InterfaceAddress interfaceAddress = NetworkUtils.interfaceAddresses.get(t);
                if (onSameNetwork(serverAddress, interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength())) {
                    return true;
                }
            }
            catch (final Exception ex) {}
        }
        return false;
    }
    
    private static boolean onSameNetwork(final InetAddress a, final InetAddress b, final int mask) {
        return onSameNetwork(a.getAddress(), b.getAddress(), mask);
    }
    
    private static boolean onSameNetwork(final byte[] x, final byte[] y, final int mask) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        if (x.length != y.length) {
            return false;
        }
        final int bits = mask & 0x7;
        final int bytes = mask >>> 3;
        for (int i = 0; i < bytes; ++i) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        final int shift = 8 - bits;
        return bits == 0 || x[bytes] >>> shift == y[bytes] >>> shift;
    }
}
