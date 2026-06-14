package org.levimc.launcher.util;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class GalleryUtils {
    private static final String TAG = "GalleryUtils";

    public static void protectAllDirectories() {
        File externalStorage = Environment.getExternalStorageDirectory();
        
        // App specific hidden folder
        protect(new File(externalStorage, ".games/org.levimc"));
        
        // Minecraft folders (often populated with random assets/screenshots)
        protect(new File(externalStorage, "games/com.mojang"));
        protect(new File(externalStorage, "games/com.mojang/Screenshots"));
        protect(new File(externalStorage, "games/com.mojang/minecraftpe"));
        
        // Protect any version isolated folders
        File appDataMinecraft = new File(externalStorage, ".games/org.levimc/minecraft");
        if (appDataMinecraft.exists() && appDataMinecraft.isDirectory()) {
            File[] versions = appDataMinecraft.listFiles(File::isDirectory);
            if (versions != null) {
                for (File version : versions) {
                    protect(new File(version, "games/com.mojang"));
                    protect(new File(version, "games/com.mojang/Screenshots"));
                }
            }
        }
    }

    public static void protect(File directory) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) return;
        }
        
        File nomedia = new File(directory, ".nomedia");
        if (!nomedia.exists()) {
            try {
                if (nomedia.createNewFile()) {
                    Log.d(TAG, "Created .nomedia in " + directory.getAbsolutePath());
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to create .nomedia in " + directory.getAbsolutePath(), e);
            }
        }
    }
}
