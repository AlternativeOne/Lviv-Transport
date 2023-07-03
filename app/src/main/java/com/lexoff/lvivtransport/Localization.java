package com.lexoff.lvivtransport;

public class Localization {

    public static String localizeString(String input, String langCode) {
        try {
            if (langCode.equals("en")) {
                if (input.contains("хв")) {
                    input = input.replaceAll("хв", "min");
                }
            }

            return input.trim();
        } catch (Exception e){
            return input;
        }
    }

    public static String localizeStopName(String input, String langCode) {
        try {
            String[] ukLower = new String[]{
                    "а",
                    "б",
                    "в",
                    "г",
                    "UNDEFINED",
                    "д",
                    "е",
                    "є",
                    "ж",
                    "з",
                    "и",
                    "і",
                    "ї",
                    "й",
                    "к",
                    "л",
                    "м",
                    "н",
                    "о",
                    "п",
                    "р",
                    "с",
                    "т",
                    "у",
                    "ф",
                    "х",
                    "ц",
                    "ч",
                    "ш",
                    "щ",
                    "ь",
                    "ю",
                    "я",
            };

            String[] ukUpper = new String[]{
                    "А",
                    "Б",
                    "В",
                    "Г",
                    "UNDEFINED",
                    "Д",
                    "Е",
                    "Є",
                    "Ж",
                    "З",
                    "И",
                    "І",
                    "Ї",
                    "Й",
                    "К",
                    "Л",
                    "М",
                    "Н",
                    "О",
                    "П",
                    "Р",
                    "С",
                    "Т",
                    "У",
                    "Ф",
                    "Х",
                    "Ц",
                    "Ч",
                    "Ш",
                    "Щ",
                    "Ь",
                    "Ю",
                    "Я",
            };

            String[] enLower = new String[]{
                    "a",
                    "b",
                    "v",
                    "h",
                    "",
                    "d",
                    "e",
                    "ye",
                    "zh",
                    "z",
                    "y",
                    "i",
                    "i",
                    "y",
                    "k",
                    "l",
                    "m",
                    "n",
                    "o",
                    "p",
                    "r",
                    "s",
                    "t",
                    "u",
                    "f",
                    "kh",
                    "ts",
                    "ch",
                    "sh",
                    "sh",
                    "",
                    "yu",
                    "ia",

            };

            String[] enUpper = new String[]{
                    "A",
                    "B",
                    "V",
                    "H",
                    "",
                    "D",
                    "E",
                    "Ye",
                    "Zh",
                    "Z",
                    "Y",
                    "I",
                    "I",
                    "Y",
                    "K",
                    "L",
                    "M",
                    "N",
                    "O",
                    "P",
                    "R",
                    "S",
                    "T",
                    "U",
                    "F",
                    "Kh",
                    "Ts",
                    "Ch",
                    "Sh",
                    "Sh",
                    "",
                    "Yu",
                    "Ia",
            };

            if (langCode.contains("en")) {
                if (input.contains("вул.")) {
                    input = input.replaceFirst("вул.", "") + " Street";
                }
                if (input.contains("Вул.")) {
                    input = input.replaceFirst("Вул.", "") + " Street";
                }
                if (input.contains("вулиця")) {
                    input = input.replaceFirst("вулиця", "") + " Street";
                }
                if (input.contains("Вулиця")) {
                    input = input.replaceFirst("Вулиця", "") + " Street";
                }

                if (input.contains("пл.")) {
                    input = input.replaceFirst("пл.", "") + " Square";
                }
                if (input.contains("Пл.")) {
                    input = input.replaceFirst("Пл.", "") + " Square";
                }
                if (input.contains("площа")) {
                    input = input.replaceFirst("площа", "") + " Square";
                }
                if (input.contains("Площа")) {
                    input = input.replaceFirst("Площа", "") + " Square";
                }

                if (input.contains("пр.")) {
                    input = input.replaceFirst("пр.", "") + " Avenue";
                }
                if (input.contains("Пр.")) {
                    input = input.replaceFirst("Пр.", "") + " Avenue";
                }
                if (input.contains("проспект")) {
                    input = input.replaceFirst("проспект", "") + " Avenue";
                }
                if (input.contains("Проспект")) {
                    input = input.replaceFirst("Проспект", "") + " Avenue";
                }

                for (int i = 0; i < ukLower.length; i++) {
                    input = input.replaceAll(ukLower[i], enLower[i]).replaceAll(ukUpper[i], enUpper[i]);
                }
            }

            return input.trim();
        } catch (Exception e){
            return input;
        }
    }

    public static String localizeRouteName(String input, String langCode) {
        try {
            input = input.replaceAll("- ", " - ").replaceAll(" -", " - ");
            String splits[] = input.split(" - ");
            return localizeStopName(splits[0], langCode) + " - " + localizeStopName(splits[1], langCode);
        } catch (Exception e){
            return input;
        }
    }

}
