package com.mogujie.tt.utils.pinyin;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.pinyin.HanziToPinyin3.Token;

public class PinYin {
	public static class PinYinElement {
		public String pinyin;
		
		//every token's pinyin, so "你xx好" -> ["NI", "X", "Y", "HAO"]
		public List<String> tokenPinyinList = new ArrayList<String>();

		//every first char bonds together for all token's pinyin list
		public String tokenFirstChars = "";
		
		
		@Override
		public String toString() {
			StringBuilder part1 = new StringBuilder("PinYinElement [pinyin=" + pinyin  + ", firstChars=" + tokenFirstChars + "]");
			StringBuilder part2 = new StringBuilder("tokenPinyinList:");
			for (String tokenPinyin : tokenPinyinList) {
				part2.append(tokenPinyin).append(",");
			}
			
			return part1.append(part2).toString();
		}
	}

	// 汉字返回拼音，字母原样返回，都转换为小写
	public static void getPinYin(Logger logger, String input,
			PinYinElement pinyinElement) {
		ArrayList<Token> tokens = HanziToPinyin3.getInstance().get(input);

		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0) {
			for (Token token : tokens) {

				if (Token.PINYIN == token.type) {
					sb.append(token.target);

					if (!TextUtils.isEmpty(token.target)) {
						pinyinElement.tokenPinyinList.add(token.target);
						pinyinElement.tokenFirstChars += token.target.substring(0, 1);
					}

				} else {
					//你xyz好 -> token.source = xyz, so we should separate every original character here
					sb.append(token.source);
					for (int i = 0; i < token.source.length(); ++i) {
						String childString = token.source.substring(i, i + 1).toUpperCase();
						pinyinElement.tokenPinyinList.add(childString);
						pinyinElement.tokenFirstChars += childString;
					}
				}
			}
		}

		String ret = sb.toString().toUpperCase();
		if (!ret.isEmpty()) {
			int firstChar = ret.charAt(0);
			if (!(firstChar >= 'A' && firstChar <= 'Z')) {
				ret = "#" + ret;
			}
		}

		pinyinElement.pinyin = ret;
	}
}
