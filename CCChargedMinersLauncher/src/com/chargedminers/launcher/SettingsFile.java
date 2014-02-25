package com.chargedminers.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SettingsFile {

    private static final String COMMENT_PATTERN = "^\\s*#",
            SETTING_PATTERN = "^\\s*(\\S+?)\\s*=\\s*(\\S*)$",
            SETTING_LITERAL_PATTERN = "^\\s*(\\S+?)\\s*=?\\s*:(.*)$";

    private static final Pattern settingPattern = Pattern.compile(SETTING_PATTERN),
            settingLiteralPattern = Pattern.compile(SETTING_LITERAL_PATTERN);

    private final HashMap<String, String> store = new HashMap<>();

    public boolean load(File file) throws IOException {
        store.clear();
        if (!file.exists()) {
            return false;
        }
        try (FileReader fr = new FileReader(file)) {
            try (BufferedReader br = new BufferedReader(fr)) {
                for (String line; (line = br.readLine()) != null;) {
                    if (line.length() == 0 || line.matches(COMMENT_PATTERN)) {
                        continue;
                    }
                    Matcher settingMatch = settingPattern.matcher(line);
                    if (settingMatch.matches()) {
                        store.put(settingMatch.group(1), settingMatch.group(2));
                        continue;
                    }
                    Matcher settingLiteralMatch = settingLiteralPattern.matcher(line);
                    if (settingLiteralMatch.matches()) {
                        store.put(settingLiteralMatch.group(1), settingLiteralMatch.group(2));
                    }
                }
            }
        }
        return true;
    }

    public void save(File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                for (Map.Entry<String, String> entry : store.entrySet()) {
                    bw.write(entry.getKey() + ":" + entry.getValue() + "\n");
                }
            }
        }
    }

    public String getString(String key, String defaultValue) {
        String value = store.get(key);
        return (value == null) ? defaultValue : value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, null);
        if (value == null) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = getString(key, null);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(value);
        }
    }

    public void setString(String key, String value) {
        store.put(key, value);
    }

    public void setBoolean(String key, boolean value) {
        setString(key, Boolean.toString(value));
    }

    public void setInt(String key, int value) {
        setString(key, Integer.toString(value));
    }
}
