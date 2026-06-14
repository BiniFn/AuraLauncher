package org.levimc.launcher.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.levimc.launcher.R;
import org.levimc.launcher.ui.animation.DynamicAnim;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutActivity extends BaseActivity {

    private static final String AVATAR_URL = "https://github.com/BiniFn.png";
    private static final String URL_TAPLINK = "https://taplink.cc/binifn";
    private static final String URL_GUNS = "https://guns.lol/binifn";
    private static final String URL_GITHUB = "https://github.com/BiniFn";
    private static final String URL_PATREON = "https://patreon.com/BiniFn";
    private static final String URL_YT_MAIN = "https://youtube.com/@BiniFn_Yt";
    private static final String URL_YT_RBX = "https://youtube.com/@BiniRBX";
    private static final String URL_YT_FX = "https://youtube.com/@BiniFx";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupNavBar();

        loadAvatars();
        setupLinks();
        styleBadges();

        DynamicAnim.applyPressScaleRecursively(findViewById(android.R.id.content));
    }

    private void styleBadges() {
        TextView authorBadge = findViewById(R.id.author_badge);

        org.levimc.launcher.util.PersonalizationManager pm = new org.levimc.launcher.util.PersonalizationManager(this);
        int accent = pm.getAccentColor();
        if (accent != 0) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            gd.setColor(accent);
            gd.setCornerRadius(4 * getResources().getDisplayMetrics().density);
            if (authorBadge != null) {
                authorBadge.setBackground(gd);
                authorBadge.setTextColor(android.graphics.Color.WHITE);
            }
        }
    }

    private void setupNavBar() {
        setActiveNavTab(R.id.nav_tab_about);
        findViewById(R.id.nav_tab_about).setOnClickListener(v -> {});
    }

    private void loadAvatars() {
        com.microsoft.xbox.idp.toolkit.CircleImageView avatar = findViewById(R.id.author_avatar);

        executor.execute(() -> {
            try {
                if (avatar != null) {
                    Response resp = client.newCall(new Request.Builder().url(AVATAR_URL).build()).execute();
                    if (resp.isSuccessful() && resp.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(resp.body().byteStream());
                        runOnUiThread(() -> {
                            if (bmp != null) avatar.setImageBitmap(bmp);
                        });
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    private void setupLinks() {
        setupLinkButton(R.id.btn_taplink, URL_TAPLINK);
        setupLinkButton(R.id.btn_guns, URL_GUNS);
        setupLinkButton(R.id.btn_github_repo, URL_GITHUB);
        setupLinkButton(R.id.btn_patreon, URL_PATREON);
        setupLinkButton(R.id.btn_youtube_main, URL_YT_MAIN);
        setupLinkButton(R.id.btn_youtube_rbx, URL_YT_RBX);
        setupLinkButton(R.id.btn_youtube_fx, URL_YT_FX);
        
        View btnOpenStorage = findViewById(R.id.btn_open_storage);
        if (btnOpenStorage != null) {
            btnOpenStorage.setOnClickListener(v -> openDataFolder());
            DynamicAnim.applyPressScale(btnOpenStorage);
        }
    }

    private void setupLinkButton(int viewId, String url) {
        TextView btn = findViewById(viewId);
        if (btn == null) return;
        btn.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ignored) {}
        });
        DynamicAnim.applyPressScale(btn);
    }

    private void openDataFolder() {
        try {
            File gamesDir = new File(android.os.Environment.getExternalStorageDirectory(), "games/com.mojang");
            if (!gamesDir.exists()) gamesDir.mkdirs();
            
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", gamesDir);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Use standard directory MIME type
            intent.setDataAndType(uri, "vnd.android.document/directory");
            
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback for file managers that don't support the above
                intent.setDataAndType(uri, "*/*");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Intent fallback = new Intent(Intent.ACTION_GET_CONTENT);
                    fallback.addCategory(Intent.CATEGORY_OPENABLE);
                    fallback.setDataAndType(uri, "*/*");
                    startActivity(Intent.createChooser(fallback, "Open Folder"));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            android.util.Log.e("AboutActivity", "Folder open error", e);
        }
    }
}
