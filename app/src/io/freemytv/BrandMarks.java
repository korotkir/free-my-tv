package io.freemytv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Crisp, transparent shelf marks for a white TV dock. The installed app's TV banner is kept in All Apps. */
final class BrandMarks {
    private static final int W = 960, H = 280;
    private static final int INK = 0xFF202124;
    private static final Map<String, Bitmap> CACHE = new HashMap<>();

    private BrandMarks() {}

    static synchronized Bitmap logo(Context context, String pkg, String label) {
        String key = pkg + ":" + label;
        Bitmap cached = CACHE.get(key);
        if (cached != null) return cached;
        Bitmap raw = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(raw);
        switch (pkg) {
            case "com.google.android.youtube.tv": youtube(c); break;
            case "mobi.zona": zona(c); break;
            case "ru.kinopoisk.tv": centered(c, "Кинопоиск", 162, Typeface.BOLD); break;
            case "com.vk.tv": vkVideo(c); break;
            case "rtb.mobile.android": rutube(c); break;
            case "ru.rt.video.app.tv": wink(c); break;
            case "org.amnezia.vpn":
            case "org.amnezia.awg": amnezia(context, c, pkg); break;
            default: centered(c, label, 150, Typeface.BOLD); break;
        }
        Bitmap result = trim(raw);
        result.setHasMipMap(true);
        CACHE.put(key, result);
        return result;
    }

    private static Paint paint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        p.setColor(color);
        return p;
    }

    private static Paint type(float size, int color, int weight) {
        Paint p = paint(color);
        p.setTypeface(Typeface.create("sans-serif", weight));
        p.setTextSize(size);
        return p;
    }

    private static float baseline(Paint p) {
        Paint.FontMetrics f = p.getFontMetrics();
        return H / 2f - (f.ascent + f.descent) / 2f;
    }

    private static void centered(Canvas c, String word, float size, int weight) {
        Paint p = type(size, INK, weight);
        p.setTextSize(Math.min(size, size * 820f / Math.max(1f, p.measureText(word))));
        c.drawText(word, (W - p.measureText(word)) / 2f, baseline(p), p);
    }

    private static void youtube(Canvas c) {
        Paint word = type(164, INK, Typeface.BOLD);
        float tw = word.measureText("YouTube"), iconW = 170, gap = 28;
        float x = (W - iconW - gap - tw) / 2f;
        Paint red = paint(0xFFFF0033);
        c.drawRoundRect(new RectF(x, 79, x + iconW, 201), 34, 34, red);
        Path play = new Path();
        play.moveTo(x + 70, 104); play.lineTo(x + 70, 176); play.lineTo(x + 122, 140); play.close();
        c.drawPath(play, paint(0xFFFFFFFF));
        c.drawText("YouTube", x + iconW + gap, baseline(word), word);
    }

    private static void zona(Canvas c) {
        // A smooth geometric Z, drawn as paths rather than enlarging a small app banner.
        Paint cyan = paint(0xFF00A9EA);
        Path z = new Path();
        z.moveTo(352, 75); z.quadTo(349, 54, 376, 54); z.lineTo(591, 54);
        z.quadTo(628, 54, 607, 88); z.lineTo(440, 212);
        z.lineTo(591, 212); z.quadTo(624, 212, 624, 239);
        z.quadTo(624, 264, 591, 264); z.lineTo(369, 264);
        z.quadTo(329, 264, 355, 224); z.lineTo(521, 109);
        z.lineTo(378, 109); z.quadTo(352, 109, 352, 75); z.close();
        c.drawPath(z, cyan);
    }

    private static void vkVideo(Canvas c) {
        Paint word = type(143, INK, Typeface.BOLD);
        float tw = word.measureText("VK Видео"), iconW = 132, gap = 27;
        float x = (W - iconW - gap - tw) / 2f;
        c.drawRoundRect(new RectF(x, 88, x + 124, 194), 24, 24, paint(0xFF0077FF));
        c.drawRoundRect(new RectF(x + 28, 88, x + 132, 194), 24, 24, paint(0xFFFF3158));
        Path play = new Path();
        play.moveTo(x + 66, 111); play.lineTo(x + 66, 171); play.lineTo(x + 111, 141); play.close();
        c.drawPath(play, paint(0xFFFFFFFF));
        c.drawText("VK Видео", x + iconW + gap, baseline(word), word);
    }

    private static void rutube(Canvas c) {
        Paint word = type(188, INK, Typeface.BOLD);
        word.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        String label = "RUTUBE";
        float width = word.measureText(label), x = (W - width) / 2f;
        c.drawText(label, x, baseline(word), word);
        c.drawCircle(x + width - 14, 55, 15, paint(0xFFED143B));
    }

    private static void wink(Canvas c) {
        Paint word = type(194, INK, Typeface.BOLD);
        word.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        float tw = word.measureText("WINK"), arrowW = 104, gap = 19;
        float x = (W - tw - gap - arrowW) / 2f;
        c.drawText("WINK", x, baseline(word), word);
        float ax = x + tw + gap;
        Path arrow = new Path();
        arrow.moveTo(ax + 6, 64); arrow.lineTo(ax + 55, 64); arrow.lineTo(ax + 109, 140);
        arrow.lineTo(ax + 55, 216); arrow.lineTo(ax + 6, 216);
        arrow.lineTo(ax + 61, 140); arrow.close();
        c.drawPath(arrow, paint(0xFFFF6B21));
    }

    private static void amnezia(Context context, Canvas c, String pkg) {
        if (pkg.equals("org.amnezia.awg")) { centered(c, "AmneziaWG", 160, Typeface.BOLD); return; }
        try (InputStream in = context.getAssets().open("logos/amnezia-icon.png")) {
            Bitmap icon = BitmapFactory.decodeStream(in);
            if (icon != null) {
                Bitmap cropped = trim(icon);
                c.drawBitmap(cropped, null, new RectF(70, 47, 266, 243), paint(0xFFFFFFFF));
            }
        } catch (IOException ignored) {}
        Paint word = type(142, INK, Typeface.BOLD);
        word.setTextSize(Math.min(142f, 620f / Math.max(1f, word.measureText("AmneziaVPN")) * 142f));
        c.drawText("AmneziaVPN", 290, baseline(word), word);
    }

    private static Bitmap trim(Bitmap b) {
        int left = b.getWidth(), top = b.getHeight(), right = -1, bottom = -1;
        int[] row = new int[b.getWidth()];
        for (int y = 0; y < b.getHeight(); y++) {
            b.getPixels(row, 0, b.getWidth(), 0, y, b.getWidth(), 1);
            for (int x = 0; x < row.length; x++) if ((row[x] >>> 24) >= 16) {
                left = Math.min(left, x); right = Math.max(right, x);
                top = Math.min(top, y); bottom = Math.max(bottom, y);
            }
        }
        if (right < left) return b;
        int pad = 18;
        Rect r = new Rect(Math.max(0, left - pad), Math.max(0, top - pad), Math.min(b.getWidth(), right + pad + 1), Math.min(b.getHeight(), bottom + pad + 1));
        return Bitmap.createBitmap(b, r.left, r.top, r.width(), r.height());
    }
}
