package cn.iba8.module.generator.common.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

public abstract class Properties2Yml {

    public static String convert(String ps) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(ps));
        } catch (Exception e) {
            e.printStackTrace();
        }
        LinkedHashMap hashMap = new LinkedHashMap();
        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("", hashMap);
        properties.forEach((o, o2) -> {
            join("", o.toString().split("\\."), o2, hashMap1);
        });
        Yaml yaml = new Yaml();
        return yaml.dumpAs(hashMap, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
    }

    public static String convert(Properties properties) {
        LinkedHashMap hashMap = new LinkedHashMap();
        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("", hashMap);
        properties.forEach((o, o2) -> {
            join("", o.toString().split("\\."), o2, hashMap1);
        });
        Yaml yaml = new Yaml();
        return yaml.dumpAs(hashMap, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
    }

    private static void join(String parentUrl, String[] keyList, Object value, HashMap<String, Object> pool) {
        if (keyList.length == 0) return;

        String myKey = keyList[0];
        String myUrl = parentUrl + "." + myKey;
        Object myParent = pool.get(parentUrl);
        Object myvalue;

        List<String> stringList = new ArrayList<>();
        for (int i = 1; i < keyList.length; i++) {
            stringList.add(keyList[i]);
        }
        String[] last = stringList.toArray(new String[((ArrayList) stringList).size()]);

        if (!pool.containsKey(myUrl)) {
            if (last.length == 0) {
                myvalue = value;
            } else if (isArray(last[0])) {
                myvalue = new ArrayList<>();
            } else {
                myvalue = new LinkedHashMap<>();
            }
            if (myParent instanceof ArrayList) {
                ((ArrayList) myParent).add(myvalue);
            } else if (myParent instanceof LinkedHashMap) {
                ((LinkedHashMap) myParent).put(myKey, myvalue);
            }
            pool.put(myUrl, myvalue);
        }
        join(myUrl, last, value, pool);
    }

    private static boolean isArray(String k) {
        String s = "";
        if (!k.endsWith("]")) return false;
        char[] chars = k.toCharArray();
        for (int i = chars.length - 2; i >= 0; i--) {
            char c = chars[i];
            if (c != '[') {
                s = c + s;
            } else {
                Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
                return pattern.matcher(s).matches();
            }
        }
        return false;
    }

}
