package com.alibaba.csp.sentinel.demo.file.rule;

import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;

import java.io.File;
import java.io.IOException;

/**
 * @Description:
 * @Date : 2024/05/20 16:07
 * @Auther : tiankun
 */
public class RuleFileUtils {

    public static void mkdirIfNotExits(String storePath) {
        File file = new File(storePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void createFileIfNotExits(CacheMap<String, String> rulesMap) {
        rulesMap.keySet(true).forEach(filename -> {
            File file = new File(PersistenceRuleConstant.storePath + File.separator + filename);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
