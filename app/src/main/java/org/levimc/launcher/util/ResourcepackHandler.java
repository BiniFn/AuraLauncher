package org.levimc.launcher.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.ProgressBar;

import org.levimc.launcher.R;
import org.levimc.launcher.core.minecraft.MinecraftImportIntents;
import org.levimc.launcher.core.minecraft.MinecraftLauncher;
import org.levimc.launcher.core.versions.GameVersion;
import org.levimc.launcher.core.versions.VersionManager;
import org.levimc.launcher.ui.dialogs.CustomAlertDialog;

import java.util.concurrent.ExecutorService;

import org.levimc.launcher.core.content.ContentImporter;
import org.levimc.launcher.core.content.ContentManager;
import org.levimc.launcher.settings.FeatureSettings;
import android.widget.Toast;
import java.io.File;
import java.util.Collections;

public class ResourcepackHandler {

    private final Activity activity;
    private final MinecraftLauncher minecraftLauncher;
    private final ExecutorService executor;

    public ResourcepackHandler(Activity activity, MinecraftLauncher minecraftLauncher,
                               ExecutorService executor) {
        this.activity = activity;
        this.minecraftLauncher = minecraftLauncher;
        this.executor = executor;
    }

    public void checkIntentForResourcepack() {
        Intent intent = activity.getIntent();
        if (!MinecraftImportIntents.isMinecraftResourceIntent(activity, intent)) {
            return;
        }
        if (MinecraftImportIntents.forwardToRunningMinecraft(activity, intent)) {
            return;
        }

        Uri data = intent.getData();
        if (data != null) {
            String displayPath = data.getPath() != null ? data.getPath() : data.toString();
            new CustomAlertDialog(activity)
                    .setTitleText(activity.getString(R.string.content_detected_title))
                    .setMessage(activity.getString(R.string.content_detected_message, displayPath))
                    .setPositiveButton(activity.getString(R.string.launch_now), (d) -> launchMinecraft(intent))
                    .setNegativeButton(activity.getString(R.string.import_content), (d) -> importContent(data))
                    .show();
        }
    }

    private void importContent(Uri uri) {
        ContentImporter importer = new ContentImporter(activity);
        
        android.content.SharedPreferences prefs = activity.getSharedPreferences("content_management", android.content.Context.MODE_PRIVATE);
        String savedType = prefs.getString("storage_type", "INTERNAL");
        FeatureSettings.StorageType currentStorageType = FeatureSettings.StorageType.valueOf(savedType);

        VersionManager versionManager = VersionManager.get(activity);
        GameVersion currentVersion = versionManager.getSelectedVersion();

        File baseDir;
        switch (currentStorageType) {
            case VERSION_ISOLATION:
                if (currentVersion != null && currentVersion.versionDir != null) {
                    baseDir = new File(currentVersion.versionDir, "games/com.mojang");
                } else {
                    baseDir = new File(activity.getDataDir(), "games/com.mojang");
                }
                break;
            case EXTERNAL:
                File externalDir = activity.getExternalFilesDir(null);
                if (externalDir != null) {
                    baseDir = new File(externalDir, "games/com.mojang");
                } else {
                    baseDir = new File(activity.getDataDir(), "games/com.mojang");
                }
                break;
            case INTERNAL:
            default:
                baseDir = new File(activity.getDataDir(), "games/com.mojang");
                break;
        }

        File worldsDir = new File(baseDir, "minecraftWorlds");
        File resourcePacksDir = new File(baseDir, "resource_packs");
        File behaviorPacksDir = new File(baseDir, "behavior_packs");
        File skinPacksDir = new File(baseDir, "skin_packs");

        importer.importContent(Collections.singletonList(uri), resourcePacksDir, behaviorPacksDir, skinPacksDir, worldsDir,
            new ContentImporter.ImportCallback() {
                @Override
                public void onSuccess(String message) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        ContentManager.getInstance(activity).refreshContent();
                    });
                }

                @Override
                public void onError(String error) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, error, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onProgress(int progress) {}
            });
    }

    private void launchMinecraft(Intent intent) {
        executor.execute(() -> {
            VersionManager versionManager = VersionManager.get(activity);
            GameVersion currentVersion = versionManager.getSelectedVersion();

            minecraftLauncher.launch(intent, currentVersion);
            activity.runOnUiThread(() -> {
            });
        });
    }
}
