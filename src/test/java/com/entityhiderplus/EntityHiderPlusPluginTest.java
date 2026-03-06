package com.entityhiderplus;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EntityHiderPlusPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(
            EntityHiderPlusPlugin.class
        );
        RuneLite.main(args);
    }
}
