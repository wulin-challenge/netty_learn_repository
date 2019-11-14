package cn.wulin.nio.test.charbuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestCharBuffer {
	String empty = "　";
	
	@Test
	public void testPrintCharLength() {
		String c1 = "123456789012345678902222223";
		String c2 = "你好啊放放就是杰弗!!!里斯来省局冯是是是是是";
		ChineseString charLength1 = getCharLength(c1);
		ChineseString charLength2 = getCharLength(c2);
		
		System.out.println(charLength1.getFullLocationLength());
		System.out.println(charLength2.getFullLocationLength());
		System.out.println(charLength1.fullString);
		System.out.println(charLength2.fullString);
		
	}
	
	@Test
	public void testPrintList() {
		String[] header = new String[] {"用户名","密码","性别","电话","邮件"};
		
		List<List<String>> table = new ArrayList<>();
		
		table.add(new ArrayList<>(Arrays.asList("张三","2113slfsf23","男","12345678901","1168637654@qq.com")));
		table.add(new ArrayList<>(Arrays.asList("李思","123","男","12345678901","1147837654@qq.com")));
		
		String table2 = TelnetUtils.toTable(header, table);
		System.out.println(table2);
	}
	
	/**
	 * <p> 1234
	 * <p> 你好啊
	 * <p> 原因就是 3个汉字所占的字符位置与4个英文字符所占的位置长度一致
	 * <p> 实际测试并不绝对
	 * @param chineseString
	 * @param chineseNumber
	 */
	public static ChineseString getCharLength(String s) {
		ChineseString chineseString = new ChineseString();
		
		if(s == null || s.length() == 0) {
			return chineseString;
		}
		
		int[] nums = getBothNumber(s);
		int chineseNumber = nums[0];
		
		int remaider = (chineseNumber%3); //得到余数
		int value = (chineseNumber/3); //得到除以3后的值
		if(remaider != 0) {
			for (int i = 0; i < (3-remaider); i++) {
				s += ChineseString.blankChinese;
			}
			value ++;
		}
		
		int locationLen = value * 4+nums[1];
		chineseString.setFullString(s);
		chineseString.setFullLocationLength(locationLen);
		return chineseString;
	}
	
	
//	private void getChineseLength(ChineseString chineseString,int chineseNumber,String s) {
//		
//	}
	
	/**
	 * 返回一个长度为2的int数组,[0]=中文个数,[1]=英文个数
	 * @return
	 */
	private static int[] getBothNumber(String s){
		int[] nums = new int[2];
		char[] charArray = s.toCharArray();
		for (char c : charArray) {
			if(isChineseChar(c))
				nums[0]++;
			else
				nums[1]++;
		}
		return nums;
	}
	
	/**
     * 判断一个字符是否是汉字
     * PS：中文汉字的编码范围：[\u4e00-\u9fa5]
     *
     * @param c 需要判断的字符
     * @return 是汉字(true), 不是汉字(false)
     */
    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }
    
    public static class ChineseString{
    	/**
    	 * 这是一个全角的中文空格
    	 */
    	public static final String blankChinese = "　";
    	
    	/**
    	 * 补齐后的中文字符串
    	 */
    	private String fullString = "";
    	
    	/**
    	 * 补齐后的所占位置长度
    	 */
    	private int fullLocationLength = 0;
    	
    	public ChineseString() {}
		public ChineseString(String fullString, int fullLocationLength) {
			super();
			this.fullString = fullString;
			this.fullLocationLength = fullLocationLength;
		}

		public String getFullString() {
			return fullString;
		}

		public void setFullString(String fullString) {
			this.fullString = fullString;
		}

		public int getFullLocationLength() {
			return fullLocationLength;
		}

		public void setFullLocationLength(int fullLocationLength) {
			this.fullLocationLength = fullLocationLength;
		}

		public static String getBlankchinese() {
			return blankChinese;
		}
    }
	
}
