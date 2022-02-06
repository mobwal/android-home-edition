/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:11
 *
 */

package pw.appcode.mimic;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 * Подражатель разметки
 */
class MimicUIParser {

    private final Hashtable<String, String> mProperties;

    public OnContainer getInstance(Context context, String xtype) {
        switch (xtype) {
            case "displayfield":
                return new UIDisplayField(context, this);

            case "textfield":
                return new UIEditField(context, this);

            case "textview":
                return new UITextView(context, this);

            case "numberfield":
                return new UINumberField(context, this);

            case "checkbox":
                return new UICheckBoxField(context, this);

            case "combo":
                return new UIComboBoxField(context, this);

            case "datefield":
                return new UIDateField(context, this);

            case "switchfield":
                return new UISwitchField(context, this);

            case "radiogroup":
                return new UIRadioGroupField(context, this);

            case "button":
                return new UIButtonField(context, this);
        }

        if(RegistryFields.exists(xtype)) {
            return RegistryFields.pull(xtype);
        }

        return null;
    }

    /**
     * Является простой разметкой
     * @param layout разметка
     * @return true - простая разметка
     */
    public static boolean isSimpleLayout(String layout) {
        String[] lines = layout.split("\n");
        List<String> results = new ArrayList<>();

        for (String line: lines) {
            if(!line.startsWith("//")) {
                results.add(line.replaceAll("\r", "").replaceAll("\t", ""));
            }
        }

        return results.size() > 0
                && results.get(0).startsWith("layout")
                && results.get(0).endsWith("'");
    }

    public static String convertToMimicUIParser(String layout) {
        String[] lines = layout.split("\n");
        List<String> results = new ArrayList<>();

        for (String line: lines) {
            if(!line.startsWith("//")) {
                results.add(line.replaceAll("\r", "").replaceAll("\t", "").trim());
            }
        }

        StringBuilder items = new StringBuilder();

        for (String s : results) {
            String line = s.trim();
            if (!line.isEmpty()) {
                String[] data = splitSmart(line, ' '); // line.splitSmart(separator: " ").map { String($0).replacingOccurrences(of: "\r", with: "").replacingOccurrences(of: "\t", with: "") }
                switch (data[0].trim()) {
                    case "layout":
                        items.append("xtype 'container'\n");
                        items.append("layout " + data[1] + "\n");
                        items.append("items [\n");
                        break;

                    case "textfield":
                        items.append("\t{\n");
                        items.append("\t\txtype 'textfield'\n");
                        items.append("\t\tname " + data[1] + "\n");
                        items.append("\t\tlabel " + getLabel(data) + "\n");
                        items.append("\t}\n");
                        break;

                    case "textview":
                        items.append("\t{\n");
                        items.append("\t\txtype 'textview'\n");
                        items.append("\t\tname " + data[1] + "\n");
                        items.append("\t\tlabel " + getLabel(data) + "\n");
                        items.append("\t}\n");
                        break;

                    case "numberfield":
                        items.append("\t{\n");
                        items.append("\t\txtype 'numberfield'\n");
                        items.append("\t\tname " + data[1] + "\n");
                        items.append("\t\tlabel " + getLabel(data) + "\n");
                        items.append("\t\tdecimalPrecision 6\n");
                        items.append("\t}\n");
                        break;

                    case "switchfield":
                        items.append("\t{\n");
                        items.append("\t\txtype 'switchfield'\n");
                        items.append("\t\tname " + data[1] + "\n");
                        items.append("\t\tlabel " + getLabel(data) + "\n");
                        //items.append("\t\talign 'right'\n")
                        items.append("\t\tlayout 'vbox'");
                        items.append("\t}\n");
                        break;

                    case "datefield":
                        items.append("\t{\n");
                        items.append("\t\txtype 'datefield'\n");
                        items.append("\t\tname " + data[1] + "\n");
                        items.append("\t\tlabel " + getLabel(data) + "\n");
                        items.append("\t\tlayout 'vbox'\n");
                        items.append("\t\tvalue 'NOW'\n");
                        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
                        String pattern = ((SimpleDateFormat) format).toLocalizedPattern();

                        items.append("\t\tformat '" + pattern + "'\n");
                        items.append("\t}\n");
                        break;

                    default:
                        items.append("\t{\n");
                        items.append("\t\txtype 'displayfield'\n");
                        items.append("\t\tvalue " + data[1] + "\n");
                        items.append("\t}\n");
                        break;
                }
            }
        }

        items.append("]");

        return items.toString();
    }

    private static String getLabel(String[] array) {
        if (array.length > 2) {
            return array[2];
        } else {
            return array[1];
        }
    }

    public static String[] splitSmart(String line, char separator) {
        List<String> results = new ArrayList<>();

        StringBuilder word = new StringBuilder();
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char s = line.charAt(i);

            if (s == '\'') {
                if (count == 0) {
                    count = 1;
                } else {
                    count = 0;
                }
            }

            if (s == separator && count == 0) {
                results.add(word.toString().replaceAll("\r", "").replaceAll("\t", ""));
                word = new StringBuilder();
                continue;
            }
            word.append(s);
        }

        if (!word.toString().isEmpty()) {
            results.add(word.toString());
        }

        return results.toArray(new String[0]);
    }

    public MimicUIParser(String layout) {
        mProperties = new Hashtable<>();

        // делим на строки, разделитель перенос строки
        String[] lines = layout.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String formatLine = line.trim();
            if(!formatLine.isEmpty()) {
                // сложное описание
                if(formatLine.endsWith("{")) {
                    // нужно найти весь блок
                    SearchIndex searchIndex = getSearchIndex(i, lines, formatLine, "{", "}");
                    if(searchIndex != null) {
                        mProperties.put(searchIndex.key, "{" + searchIndex.value);
                        i = searchIndex.i;
                    }
                    continue;
                }
                // вложения
                if(formatLine.endsWith("[")) {
                    // нужно найти весь блок
                    SearchIndex searchIndex = getSearchIndex(i, lines, formatLine, "[", "]");
                    if(searchIndex != null) {
                        mProperties.put(searchIndex.key, "[" + searchIndex.value);
                        i = searchIndex.i;
                    }
                    continue;
                }

                String[] data = formatLine.split(" ");
                mProperties.put(data[0], formatLine.replace(data[0] + " ", ""));
            }
        }
    }

    public String getStringValue(String key) {
        if(mProperties.containsKey(key)) {
            String str = mProperties.get(key);
            assert str != null;
            return trimString(str, Names.TRIM_STRING_SYMBOL);
        }
        return null;
    }

    public void setProperty(String key, String value) {
        mProperties.put(key, value);
    }

    public Boolean getBooleanValue(String key) {
        if(mProperties.containsKey(key)) {
            return Boolean.parseBoolean(mProperties.get(key));
        }
        return null;
    }

    public Integer getIntegerValue(String key) {
        if(mProperties.containsKey(key)) {
            String str = mProperties.get(key);
            assert str != null;
            return Integer.parseInt(str);
        }
        return null;
    }

    private SearchIndex getSearchIndex(int i, String[] lines, String formatLine, String startSymbol, String endSymbol) {
        // нужно найти весь блок
        int open = 1;
        List<String> propertyLines = new ArrayList<>();
        for(int j = i + 1; j < lines.length; j++) {
            String childLine = lines[j];
            String formatChildLine = childLine.trim();
            propertyLines.add(formatChildLine);

            if(formatChildLine.contains(startSymbol)) {
                open++;
            } else if(formatChildLine.contains(endSymbol)) {
                open--;
            }

            if(open == 0) {
                String[] childData = formatLine.split(" ");
                SearchIndex searchIndex = new SearchIndex();
                searchIndex.i = j;
                searchIndex.key = childData[0];
                searchIndex.value = join(propertyLines, "\n");

                return searchIndex;
            }
        }

        return null;
    }

    /**
     * Доступно свойство
     * @param key ключ
     * @return является свойством
     */
    public boolean isProperty(String key) {
        return mProperties.containsKey(key);
    }

    /**
     * Является свойством
     * @param key ключ
     * @return является свойством
     */
    public boolean isEvent(String key) {
        return isProperty(key) && key.startsWith("on");
    }

    /**
     * Является объектом
     * @param key ключ
     * @return является объектом
     */
    public boolean isObject(String key) {
        return getStringValue(key).startsWith("{");
    }

    /**
     * Является массивом
     * @param key ключ
     * @return является массивом
     */
    public boolean isArray(String key) {
        return getStringValue(key).startsWith("[");
    }

    /**
     * Получение объекта
     * @param key ключ
     * @return объект
     */
    public MimicUIParser getObject(String key) {
        return new MimicUIParser(trimString(trimString(getStringValue(key), "}"), "{"));
    }

    /**
     * Получение скрипта
     * @param key ключ
     * @return cкрипт
     */
    public String getEvent(String key) {
        return trimString(trimString(getStringValue(key), "}"), "{");
    }

    /**
     * Получение массивов
     * @param key ключ
     * @return массив
     */
    public MimicUIParser[] getArray(String key) {
        List<MimicUIParser> mimicUIParsers = new ArrayList<>();
        String formatArray = trimString(trimString(getStringValue(key), "["), "]");
        // делим на строки, разделитель перенос строки
        String[] lines = formatArray.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String formatLine = line.trim();
            if(!formatLine.isEmpty()) {
                // сложное описание
                if(formatLine.endsWith("{")) {
                    // нужно найти весь блок
                    SearchIndex searchIndex = getSearchIndex(i, lines, formatLine, "{", "}");
                    if(searchIndex != null) {
                        mimicUIParsers.add(new MimicUIParser(trimString(searchIndex.value, "}")));
                        i = searchIndex.i;
                    }
                }
            }
        }
        return mimicUIParsers.toArray(new MimicUIParser[0]);
    }

    public String toUIString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key: mProperties.keySet()) {
            stringBuilder.append(key).append(" ").append(mProperties.get(key)).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Объединений массива в одну строку с разделителем
     * @param col коллекция строк
     * @param delim разделитель
     * @return объединенная строка
     */
    public static String join(Collection<?> col, String delim) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = col.iterator();
        if (iter.hasNext())
            sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(delim);
            sb.append(iter.next().toString());
        }
        return sb.toString();
    }

    /**
     * Очистка символов
     * @param text текст
     * @param trimBy символ
     * @return форматированная строка
     */
    public static String trimString(String text, String trimBy) {
        int beginIndex = 0;
        int endIndex = text.length();

        while (text.substring(beginIndex, endIndex).startsWith(trimBy)) {
            beginIndex += trimBy.length();
        }

        while (text.substring(beginIndex, endIndex).endsWith(trimBy)) {
            endIndex -= trimBy.length();
        }

        return text.substring(beginIndex, endIndex);
    }

    static class SearchIndex {
        public int i;
        public String key;
        public String value;
    }
 }
