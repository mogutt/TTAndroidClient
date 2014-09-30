package com.mogujie.tt.utils.pinyin;

import java.util.ArrayList;

import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.pinyin.HanziToPinyin3.Token;


public class PinYin
{
	// 汉字返回拼音，字母原样返回，都转换为小写
	public static String getPinYin(String input)
	{
		ArrayList<Token> tokens = HanziToPinyin3.getInstance().get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0)
		{
			for (Token token : tokens)
			{
				if (Token.PINYIN == token.type)
				{
					sb.append(token.target);
				} else
				{
					sb.append(token.source);
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
		
		return ret;
	}
}
