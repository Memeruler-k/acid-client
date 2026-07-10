package dev.abstr3act.addon.utils.luna;

public class ChineseUtils {
    private static final String[] CHINESE_NUMBS = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CHINESE_UNITS = new String[]{"", "十", "百", "千"};
    private static final String[] CHINESE_BIG_UNITS = new String[]{"", "万", "亿"};

    public static String convert(int number) {
        if (number == 0) {
            return CHINESE_NUMBS[0];
        } else {
            StringBuilder chinese = new StringBuilder();
            if (number < 0) {
                chinese.append("负");
                number = -number;
            }

            int unitPosition = 0;

            for (boolean needZero = false; number > 0; unitPosition++) {
                int part = number % 10000;
                if (part != 0) {
                    String partChinese = convertPart(part);
                    if (needZero) {
                        chinese.insert(0, "零");
                        needZero = false;
                    }

                    chinese.insert(0, partChinese + CHINESE_BIG_UNITS[unitPosition]);
                } else {
                    needZero = true;
                }

                number /= 10000;
            }

            String result = chinese.toString();
            return result.replaceAll("^一十", "十");
        }
    }

    private static String convertPart(int num) {
        StringBuilder partChinese = new StringBuilder();

        for (int unitPosition = 0; num > 0; unitPosition++) {
            int digit = num % 10;
            if (digit != 0) {
                partChinese.insert(0, CHINESE_NUMBS[digit] + CHINESE_UNITS[unitPosition]);
            } else if (partChinese.length() > 0 && partChinese.charAt(0) != '零') {
                partChinese.insert(0, "零");
            }

            num /= 10;
        }

        return partChinese.toString().replaceAll("零+", "零").replaceAll("零$", "");
    }
}
