package com.example.xinli.util;

import java.util.*;

/**
 * 内容安全检测：社区帖子、评论、AI用户输入高危检测
 * 重点检测：攻击辱骂他人、教唆自伤、教唆伤害他人、暴力言论
 */
public class ContentSafetyUtil {

    /**
     * 辱骂攻击他人关键词
     */
    private static final Set<String> ATTACK_OTHER = new HashSet<>(Arrays.asList(
            "猪","狗","傻","死","傻逼","脑残","废物","去死吧","弄死你","打你","报复你","揍死你","骂死你",
            "诋毁","人身攻击","弄死别人","伤害别人","教唆打人","找人打他",
            "垃圾","贱人","杂种","滚蛋","干掉他"
    ));

    /**
     * 教唆 / 鼓励伤害自己、轻生
     */
    private static final Set<String> INJURE_SELF = new HashSet<>(Arrays.asList(
            "自杀","自残","割腕","跳楼","不要活","死了算了","毁灭自己",
            "伤害自己","快去死","了结生命","想死","去死","轻生","了结自己",
            "不想活了","活着没意思","了结我自己"
    ));

    /**
     * 教唆伤害他人
     */
    private static final Set<String> INJURE_OTHER = new HashSet<>(Arrays.asList(
            "杀了他","弄死他","伤害他","报复他","找人打他","教唆伤人",
            "暴力报复","把他干掉","毁掉别人","捅死他","砍死他"
    ));

    /**
     * 检测结果对象
     */
    public static class CheckResult{
        private boolean unsafe; //true=不安全，拦截
        private String msg;     //拦截提示信息

        public CheckResult(boolean unsafe, String msg) {
            this.unsafe = unsafe;
            this.msg = msg;
        }
        public boolean isUnsafe() { return unsafe; }
        public String getMsg() { return msg; }
    }

    /**
     * 文本预处理：清除所有非汉字、字母数字字符，全部转为小写
     * 用来防御：想 死、想！死、想#死 这种插入符号绕过检测
     */
    private static String preProcess(String text){
        if(text == null) return "";
        // 保留中文、英文、数字，其余全部删掉
        return text.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]","").toLowerCase();
    }

    /**
     * 对外调用入口
     * @param text 需要检测的原始文本
     * @return CheckResult
     */
    public static CheckResult check(String text){
        if(text == null || text.isBlank()){
            return new CheckResult(false,"");
        }
        //预处理文本，去除符号空格干扰
        String cleanText = preProcess(text);
        if(cleanText.isBlank()){
            return new CheckResult(false,"");
        }

        //1.攻击辱骂他人
        for(String word : ATTACK_OTHER){
            if(cleanText.contains(word)){
                return new CheckResult(true,"检测到存在攻击辱骂他人言论，内容已拒绝发布，请文明发言。");
            }
        }
        //2.教唆伤害自己/轻生
        for(String word : INJURE_SELF){
            if(cleanText.contains(word)){
                return new CheckResult(true,"检测到涉及伤害自身相关言论，请珍惜生命，有困扰可以联系心理老师寻求帮助。");
            }
        }
        //3.教唆伤害他人
        for(String word : INJURE_OTHER){
            if(cleanText.contains(word)){
                return new CheckResult(true,"检测到教唆/伤害他人的高危言论，禁止发布。");
            }
        }
        return new CheckResult(false,"");
    }
}
