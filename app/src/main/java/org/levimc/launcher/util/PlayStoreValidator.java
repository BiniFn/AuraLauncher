package org.levimc.launcher.util;

import android.content.Context;
import android.content.pm.PackageManager;
import org.levimc.launcher.core.versions.VersionManager;

public class PlayStoreValidator {
    private static final String MINECRAFT_PACKAGE_NAME = "com.mojang.minecraftpe";

    /**
     * Play Store validation is bypassed to allow any legitimate or imported Minecraft package to run.
     */
    public static boolean isMinecraftFromPlayStore(Context context) {
        return true;
    }

    public static boolean isMinecraftInstalled(Context context) {
        if (context == null) return true;
        try {
            PackageManager packageManager = context.getPackageManager();
            packageManager.getPackageInfo(MINECRAFT_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            try {
                VersionManager vm = VersionManager.getIfInitialized();
                if (vm != null) {
                    return !vm.getInstalledVersions().isEmpty() || !vm.getCustomVersions().isEmpty();
                }
            } catch (Exception ignored) {
            }
            return true;
        }
    }

    public static boolean isLicenseVerified(Context context) {
        return true;
    }
}
