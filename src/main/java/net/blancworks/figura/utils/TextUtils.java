package net.blancworks.figura.utils;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    public static String noBadges4U(String string) {
        return string.replaceAll("([△▲★✯☆✭]|\\\\u(?i)(25B3|25B2|2605|272F|2606|272D))", "\uFFFD");
    }

    public static List<Text> splitText(Text text, String regex) {
        ArrayList<Text> textList = new ArrayList<>();

        MutableText currentText = new LiteralText("");
        for (Text entry : text.getWithStyle(text.getStyle())) {
            String entryString = entry.getString();
            String[] lines = entryString.split(regex);
            for (int i = 0; i < lines.length; i++) {
                if (i != 0) {
                    textList.add(currentText.shallowCopy());
                    currentText = new LiteralText("");
                }
                currentText.append(new LiteralText(lines[i]).setStyle(entry.getStyle()));
            }
            if (entryString.endsWith(regex)) {
                textList.add(currentText.shallowCopy());
                currentText = new LiteralText("");
            }
        }
        textList.add(currentText);

        return textList;
    }
}