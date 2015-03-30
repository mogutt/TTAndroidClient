
package com.mogujie.tt.ui.tools;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.mogujie.tt.R;
import com.mogujie.tt.utils.CommonUtil;

/**
 * @Description 表情解析
 * @author Nana
 * @date 2014-4-16
 */
@SuppressLint("UseSparseArrays")
public class Emoparser {
    private Context context;
    private String[] emoList;
    private Pattern mPattern;
    private static HashMap<String, Integer> phraseIdMap;
    private static HashMap<Integer, String> idPhraseMap;
    private static Emoparser instance = null;
    private final int DEFAULT_SMILEY_TEXTS = R.array.default_emo_phrase;
    private final int[] DEFAULT_EMO_RES_IDS = {
            R.drawable.tt_e0, R.drawable.tt_e1,
            R.drawable.tt_e2, R.drawable.tt_e3, R.drawable.tt_e4, R.drawable.tt_e5,
            R.drawable.tt_e6, R.drawable.tt_e7, R.drawable.tt_e8, R.drawable.tt_e9,
            R.drawable.tt_e10, R.drawable.tt_e11, R.drawable.tt_e12, R.drawable.tt_e13,
            R.drawable.tt_e14, R.drawable.tt_e15, R.drawable.tt_e16, R.drawable.tt_e17,
            R.drawable.tt_e18, R.drawable.tt_e19, R.drawable.tt_e20, R.drawable.tt_e21,
            R.drawable.tt_e22, R.drawable.tt_e23, R.drawable.tt_e24, R.drawable.tt_e25,
            R.drawable.tt_e26, R.drawable.tt_e27, R.drawable.tt_e28, R.drawable.tt_e29,
            R.drawable.tt_e30, R.drawable.tt_e31, R.drawable.tt_e32, R.drawable.tt_e33,
            R.drawable.tt_e34, R.drawable.tt_e35, R.drawable.tt_e36, R.drawable.tt_e37,
            R.drawable.tt_e38, R.drawable.tt_e39, R.drawable.tt_e40, R.drawable.tt_e41,
            R.drawable.tt_e42, R.drawable.tt_e43, R.drawable.tt_e44, R.drawable.tt_e45
    };

    public int[] getResIdList() {
        return DEFAULT_EMO_RES_IDS;
    }

    public static synchronized Emoparser getInstance(Context cxt) {
        if (null == instance && null != cxt) {
            instance = new Emoparser(cxt);
        }
        return instance;
    }

    private Emoparser(Context cxt) {
        context = cxt;
        emoList = context.getResources().getStringArray(DEFAULT_SMILEY_TEXTS);
        buildMap();
        mPattern = buildPattern();
    }

    private void buildMap() {
        if (DEFAULT_EMO_RES_IDS.length != emoList.length) {
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        phraseIdMap = new HashMap<String, Integer>(emoList.length);
        idPhraseMap = new HashMap<Integer, String>(emoList.length);
        for (int i = 0; i < emoList.length; i++) {
            phraseIdMap.put(emoList[i], DEFAULT_EMO_RES_IDS[i]);
            idPhraseMap.put(DEFAULT_EMO_RES_IDS[i], emoList[i]);
        }
    }

    public HashMap<String, Integer> getPhraseIdMap() {
        return phraseIdMap;
    }

    public HashMap<Integer, String> getIdPhraseMap() {
        return idPhraseMap;
    }

    private Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(emoList.length * 3);
        patternString.append('(');
        for (String s : emoList) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1,
                patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    public CharSequence emoCharsequence(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = phraseIdMap.get(matcher.group());
            Drawable drawable = context.getResources().getDrawable(resId);
            int size = (int) (CommonUtil.getElementSzie(context) * 0.8);
            drawable.setBounds(0, 0, size, size);
            ImageSpan imageSpan = new ImageSpan(drawable,
                    ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
}
