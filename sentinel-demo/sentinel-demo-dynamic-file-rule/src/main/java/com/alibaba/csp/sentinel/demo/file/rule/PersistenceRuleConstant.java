package com.alibaba.csp.sentinel.demo.file.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.File;
import java.util.List;

/**
 * @Description:
 * @Date : 2024/05/20 15:49
 * @Auther : tiankun
 */
public class PersistenceRuleConstant {
    public static String storePath = System.getProperty("user.home") + File.separator + "sentinel" + File.separator + "rules";
    public static final String FLOW_RULE_PATH = "FlowRule.json";
    public static final String DEGRAGE_RULE_PATH = "DegrageRule.json";
    public static final String SYSTEM_RULE_PATH = "SystemRule.json";
    public static final String HOT_PARAM_RULE = "HotParamRule.json";
    public static final String AUTH_RULE_PATH = "AuthRule.json";

    public static CacheMap<String, String> rulesMap;


    public static Converter<List<ParamFlowRule>, String> paramRuleEnCoding = PersistenceRuleConstant::encodeJson;
    public static Converter<List<AuthorityRule>, String> authorityEncoding = PersistenceRuleConstant::encodeJson;
    public static Converter<List<SystemRule>, String> sysRuleEnCoding = PersistenceRuleConstant::encodeJson;
    public static Converter<List<DegradeRule>, String> degradeRuleEnCoding = PersistenceRuleConstant::encodeJson;
    public static Converter<List<FlowRule>, String> flowFuleEnCoding = PersistenceRuleConstant::encodeJson;
    public static Converter<String, List<ParamFlowRule>> paramFlowRuleListParse = source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {});
    public static Converter<String, List<AuthorityRule>> authorityRuleParse = source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {});
    public static Converter<String, List<SystemRule>> sysRuleListParse = source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {});
    public static Converter<String, List<DegradeRule>> degradeRuleListParse = source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {});
    public static Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});

    static {
        rulesMap.put(FLOW_RULE_PATH,  storePath + File.separator + FLOW_RULE_PATH);
        rulesMap.put(DEGRAGE_RULE_PATH,  storePath + File.separator + FLOW_RULE_PATH);
        rulesMap.put(SYSTEM_RULE_PATH,  storePath + File.separator + SYSTEM_RULE_PATH);
        rulesMap.put(HOT_PARAM_RULE,  storePath + File.separator + HOT_PARAM_RULE);
        rulesMap.put(AUTH_RULE_PATH,  storePath + File.separator + AUTH_RULE_PATH);
    }

    private static  <T> String encodeJson(T t) {
        return JSON.toJSONString(t);
    }
}
