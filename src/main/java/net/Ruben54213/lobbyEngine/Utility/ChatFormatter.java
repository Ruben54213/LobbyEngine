package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unterstützt:
 *  - Legacy '&' Codes (&a, &l, &r, …)
 *  - HEX: "#RRGGBB" und "&#RRGGBB"
 *  - MiniMessage-Farben: "<#RRGGBB>Text</#RRGGBB>"
 *  - MiniMessage-Gradient: "<gradient:#AA11BB:#22CC33(:#... optional)>Text</gradient>"
 *  - Bereits vorhandene '§'-Codes bleiben erhalten
 */
public class ChatFormatter {

    private static final Pattern AMP_OR_PLAIN_HEX = Pattern.compile("&?#([A-Fa-f0-9]{6})");
    private static final Pattern OPEN_HEX_TAG     = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern CLOSE_HEX_TAG    = Pattern.compile("</#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_TAG     = Pattern.compile("<gradient:((?:#[A-Fa-f0-9]{6})(?::#[A-Fa-f0-9]{6})+?)>(.*?)</gradient>", Pattern.DOTALL);

    public static String format(String input) {
        if (input == null || input.isEmpty()) return input;

        String text = input;

        // 1) MiniMessage: <gradient:#..:#..(:#..)*>text</gradient>
        text = replaceGradients(text);

        // 2) MiniMessage: <#RRGGBB>text</#RRGGBB>
        //    -> ersetze den Opener in HEX (§x…); der Closertag fliegt einfach raus.
        text = replaceOpenHexTags(text);
        text = CLOSE_HEX_TAG.matcher(text).replaceAll(""); // closers entfernen

        // 3) Rohe HEXs: "#RRGGBB" oder "&#RRGGBB"
        text = replaceHexShortcuts(text);

        // 4) Legacy '&' Farbcodes/Format (&a &l &r etc.) übersetzen
        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }

    /* ===================== Helpers ===================== */

    private static String replaceHexShortcuts(String text) {
        Matcher m = AMP_OR_PLAIN_HEX.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            m.appendReplacement(sb, toSectionHex(hex));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String replaceOpenHexTags(String text) {
        Matcher m = OPEN_HEX_TAG.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            m.appendReplacement(sb, toSectionHex(hex));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String replaceGradients(String text) {
        Matcher m = GRADIENT_TAG.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String colorList = m.group(1); // "#AA11BB:#22CC33(:#....)"
            String content   = m.group(2);

            List<int[]> colors = parseHexList(colorList);
            String gradient = applyGradient(content, colors);

            // Backslashes in Replacement escapen
            m.appendReplacement(sb, Matcher.quoteReplacement(gradient));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static List<int[]> parseHexList(String list) {
        String[] parts = list.split(":");
        List<int[]> out = new ArrayList<>(parts.length);
        for (String p : parts) out.add(hexToRgb(p.replace("#","")));
        return out;
    }

    /**
     * Wendet einen n-Segment-Gradient über den gesamten Text an.
     * Bei 2 Farben: linear von c0 -> c1; bei mehr werden die Segmente gleichmäßig verteilt.
     */
    private static String applyGradient(String text, List<int[]> colors) {
        if (text.isEmpty() || colors.isEmpty()) return text;
        int len = text.length();
        if (len == 1) return toSectionHex(rgbToHex(colors.get(0))) + text;

        StringBuilder out = new StringBuilder(text.length() * 14); // grob
        // total Steps = len - 1, Segmente = colors.size()-1
        int segments = Math.max(1, colors.size() - 1);
        for (int i = 0; i < len; i++) {
            double tGlobal = (double) i / Math.max(1, len - 1);
            // Segment index bestimmen
            double segPos = tGlobal * segments;
            int segIdx = Math.min(segments - 1, (int) Math.floor(segPos));
            double t = segPos - segIdx;

            int[] c0 = colors.get(segIdx);
            int[] c1 = colors.get(segIdx + 1);
            int r = (int) Math.round(lerp(c0[0], c1[0], t));
            int g = (int) Math.round(lerp(c0[1], c1[1], t));
            int b = (int) Math.round(lerp(c0[2], c1[2], t));

            out.append(toSectionHex(rgbToHex(new int[]{r,g,b})));
            out.append(text.charAt(i));
        }
        return out.toString();
    }

    private static double lerp(int a, int b, double t) {
        return a + (b - a) * t;
    }

    private static int[] hexToRgb(String hex6) {
        return new int[] {
                Integer.parseInt(hex6.substring(0,2), 16),
                Integer.parseInt(hex6.substring(2,4), 16),
                Integer.parseInt(hex6.substring(4,6), 16)
        };
    }

    private static String rgbToHex(int[] rgb) {
        return String.format("%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
    }

    /** Wandelt "RRGGBB" in §x§R§R§G§G§B§B um */
    private static String toSectionHex(String hex6) {
        StringBuilder rep = new StringBuilder("§x");
        for (char c : hex6.toCharArray()) rep.append('§').append(c);
        return rep.toString();
    }
}
