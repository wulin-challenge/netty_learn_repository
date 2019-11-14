/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.wulin.nio.test.charbuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wulin.nio.test.charbuffer.TestCharBuffer.ChineseString;

/**
 * TelnetUtils
 *
 * @author william.liangf
 */
public class TelnetUtils {

    public static String toList(List<List<String>> table) {
        int[] widths = new int[table.get(0).size()];
        for (int j = 0; j < widths.length; j++) {
            for (List<String> row : table) {
                widths[j] = Math.max(widths[j], row.get(j).length());
            }
        }
        StringBuilder buf = new StringBuilder();
        for (List<String> row : table) {
            if (buf.length() > 0) {
                buf.append("\r\n");
            }
            for (int j = 0; j < widths.length; j++) {
                if (j > 0) {
                    buf.append(" - ");
                }
                String value = row.get(j);
                buf.append(value);
                if (j < widths.length - 1) {
                    int pad = widths[j] - value.length();
                    if (pad > 0) {
                        for (int k = 0; k < pad; k++) {
                            buf.append(" ");
                        }
                    }
                }
            }
        }
        return buf.toString();
    }

    public static String toTable(String[] header, List<List<String>> table) {
        return toTable(new ArrayList<>(Arrays.asList(header)), new ArrayList<>(table));
    }

    public static String toTable(List<String> header, List<List<String>> table) {
        int totalWidth = 0;
        int[] widths = new int[header.size()];
        int maxwidth = 70;
        int maxcountbefore = 0;
        for (int j = 0; j < widths.length; j++) {
//            widths[j] = Math.max(widths[j], header.get(j).length());
        	
        	ChineseString charLength = TestCharBuffer.getCharLength(header.get(j));
        	header.set(j, charLength.getFullString());
//        	header.get
            widths[j] = Math.max(widths[j], charLength.getFullLocationLength());
        }
        for (List<String> row : table) {
            int countbefore = 0;
            for (int j = 0; j < widths.length; j++) {
//                widths[j] = Math.max(widths[j], row.get(j).length());
            	ChineseString charLength = TestCharBuffer.getCharLength(row.get(j));
            	row.set(j, charLength.getFullString());
            	
                widths[j] = Math.max(widths[j], charLength.getFullLocationLength());
                totalWidth = (totalWidth + widths[j]) > maxwidth ? maxwidth : (totalWidth + widths[j]);
                if (j < widths.length - 1) {
                    countbefore = countbefore + widths[j];
                }
            }
            maxcountbefore = Math.max(countbefore, maxcountbefore);
        }
        widths[widths.length - 1] = Math.min(widths[widths.length - 1], maxwidth - maxcountbefore);
        StringBuilder buf = new StringBuilder();
        //line
        buf.append("+");
        for (int j = 0; j < widths.length; j++) {
            for (int k = 0; k < widths[j] + 2; k++) {
                buf.append("-");
            }
            buf.append("+");
        }
        buf.append("\r\n");
        //header
        buf.append("|");
        for (int j = 0; j < widths.length; j++) {
            String cell = header.get(j);
            buf.append(" ");
            buf.append(cell);
            int pad = widths[j] - cell.length();
            if (pad > 0) {
                for (int k = 0; k < pad; k++) {
                    buf.append(" ");
                }
            }
            buf.append(" |");
        }
        buf.append("\r\n");
        //line
        buf.append("+");
        for (int j = 0; j < widths.length; j++) {
            for (int k = 0; k < widths[j] + 2; k++) {
                buf.append("-");
            }
            buf.append("+");
        }
        buf.append("\r\n");
        //content
        for (List<String> row : table) {
            StringBuffer rowbuf = new StringBuffer();
            rowbuf.append("|");
            for (int j = 0; j < widths.length; j++) {
                String cell = row.get(j);
                rowbuf.append(" ");
                int remaing = cell.length();
                while (remaing > 0) {

                    if (rowbuf.length() >= totalWidth) {
                        buf.append(rowbuf.toString());
                        rowbuf = new StringBuffer();
//                        for(int m = 0;m < maxcountbefore && maxcountbefore < totalWidth ; m++){
//                            rowbuf.append(" ");
//                        }
                    }

                    rowbuf.append(cell.substring(cell.length() - remaing, cell.length() - remaing + 1));
                    remaing--;
                }
                int pad = widths[j] - cell.length();
                if (pad > 0) {
                    for (int k = 0; k < pad; k++) {
                        rowbuf.append(" ");
                    }
                }
                rowbuf.append(" |");
            }
            buf.append(rowbuf).append("\r\n");
        }
        //line
        buf.append("+");
        for (int j = 0; j < widths.length; j++) {
            for (int k = 0; k < widths[j] + 2; k++) {
                buf.append("-");
            }
            buf.append("+");
        }
        buf.append("\r\n");
        return buf.toString();
    }

}