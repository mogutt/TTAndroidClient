package com.mogujie.tt.utils.pinyin;

import java.util.ArrayList;
import java.util.List;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.pinyin.HanziToPinyin3.Token;

public class PinYin {
	public static class PinYinArea {
		public int startIndex = 1;
		public int endIndex = -1;
	}

	public static class PinYinElement {
		@Override
		public String toString() {
			String part1 = "PinYinElement [pinyin=" + pinyin + "]";

			String part2 = "(";
			for (int i = 0; i < pinyinArea.size(); ++i) {
				PinYinArea area = pinyinArea.get(i);

				part2 += String.format("area%d(%d, %d)", i, area.startIndex, area.endIndex);
			}

			return part1 + part2 + ")";
		}

		public String pinyin;

		public List<PinYinArea> pinyinArea = new ArrayList<PinYinArea>();
	}

	// 汉字返回拼音，字母原样返回，都转换为小写
	public static void getPinYin(Logger logger, String input,
			PinYinElement pinyinElement) {
		ArrayList<Token> tokens = HanziToPinyin3.getInstance().get(input);

		int index = 0;
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0) {
			for (Token token : tokens) {

				if (Token.PINYIN == token.type) {
					sb.append(token.target);

					index = addPinyinArea(pinyinElement.pinyinArea, index, token.target);

				} else {
					//你xyz好 -> token.source = xyz, so we should seperate every original character here
					sb.append(token.source);
					for (int i = 0; i < token.source.length(); ++i) {
						String childString = token.source.substring(i, i + 1);
						index = addPinyinArea(pinyinElement.pinyinArea, index, childString);
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

	private static int addPinyinArea(List<PinYinArea> pinYinAreas,
			int curIndex, String content) {
		PinYinArea area = new PinYinArea();
		area.startIndex = curIndex;
		area.endIndex = area.startIndex + content.length();
		
		pinYinAreas.add(area);

		return area.endIndex;
	}
}
