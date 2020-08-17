package com.cuzz;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author cuzz
 * @date 2020/8/15
 **/

public class Main {


    /**
     * 索引
     */
    private static final Map<String, List<Integer>> index = new HashMap<>();

    /**
     * 停顿词去掉
     */
    private static final Set<String> stopWords = new HashSet<>();

    static {
        List<String> list = Arrays.asList("a", "and", "be", "have", "i", "in", "of", "that", "the", "to");
        stopWords.addAll(list);
    }

    public static void main(String[] args) {

        Main main = new Main();
        main.test();
    }

    private void test() {
        String path = "/Users/cuzz/Downloads/axel/enwiki-latest-abstract1.xml.gz";
        List<Doc> docs = loadDocument(path);
        makeIndex(docs);

        String search = "Small wild cat ";
        List<Integer> ids = search(search);
        for (Integer id : ids) {
            Doc doc = docs.get(id);
            System.out.println(doc);
        }

    }

    /**
     * 查询 hello world
     *
     * @param search 查询字符串
     * @return list
     */
    private List<Integer> search(String search) {
        List<List<Integer>> list = new ArrayList<>();
        List<String> tokens = analyze(search);
        for (String token : tokens) {
            if (index.containsKey(token)) {
                list.add(index.get(token));
            }
        }

        return compose(list);
    }

    /**
     * 把多个合并成一个list，保证都存在
     *
     * @param list 多个list
     * @return list
     */
    private List<Integer> compose(List<List<Integer>> list) {
        List<Integer> res = new ArrayList<>();
        if (list.size() <= 1) {
            return res;
        }

        res = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            res = intersect(res, list.get(i));
        }
        return res;
    }

    private List<Integer> intersect(List<Integer> l1, List<Integer> l2) {
        int minLen = l1.size();
        if (l1.size() > l2.size()) {
            minLen = l2.size();
        }
        List<Integer> res = new ArrayList<>(minLen);
        int i = 0;
        int j = 0;
        while (i < l1.size() && j < l2.size()) {
            if (l1.get(i) > l2.get(j)) {
                j++;
            } else if (l1.get(i) < l2.get(j)) {
                i++;
            } else {
                res.add(l1.get(i));
                i++;
                j++;
            }
        }
        return res;
    }

    /**
     * 创建索引  word:[1, 2]
     *
     * @param docs list
     */
    private void makeIndex(List<Doc> docs) {
        for (Doc doc : docs) {
            List<String> tokens = analyze(doc.getText());
            for (String token : tokens) {
                // 如果index不存在token -> 把这个token加上
                if (!index.containsKey(token)) {
                    index.put(token, new ArrayList<>(Arrays.asList(doc.getId())));
                } else {
                    // 如果index存在token -> 如果这次已经添加就不加了
                    List<Integer> ids = index.get(token);
                    if (!Objects.equals(ids.get(ids.size() - 1), doc.getId())) {
                        ids.add(doc.getId());
                    }
                }
            }
        }
    }

    /**
     * 语意分析
     *
     * @param text 文本
     * @return list
     */
    private List<String> analyze(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return tokens;
        }
        tokens = tokenize(text);
        tokens = lowerCaseFilter(tokens);
        tokens = stopWordFilter(tokens);
        tokens = stemmerFilter(tokens);

        return tokens;
    }

    /**
     * 把单词复杂形式化为简单形式，比如去掉复数，形态等
     *
     * @param tokens
     * @return
     */
    private List<String> stemmerFilter(List<String> tokens) {
        return tokens.stream().map(
                token -> {
                    SnowballStemmer stemmer = new porterStemmer();
                    stemmer.setCurrent(token);
                    stemmer.stem();
                    return stemmer.getCurrent();
                }
        ).collect(Collectors.toList());

    }

    /**
     * 把常见单词去掉
     *
     * @param tokens
     * @return
     */
    private List<String> stopWordFilter(List<String> tokens) {
        return tokens.stream().filter(token -> !stopWords.contains(token)).collect(Collectors.toList());
    }

    /**
     * 把大写转换为小写
     *
     * @param tokens
     * @return
     */
    private List<String> lowerCaseFilter(List<String> tokens) {
        return tokens.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    /**
     * 把句子转换为单词
     *
     * @param text
     * @return list
     */
    private List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        BreakIterator breakIterator = BreakIterator.getWordInstance();
        breakIterator.setText(text);
        int lastIndex = breakIterator.first();
        while (BreakIterator.DONE != lastIndex) {
            int firstIndex = lastIndex;
            lastIndex = breakIterator.next();
            if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(text.charAt(firstIndex))) {
                words.add(text.substring(firstIndex, lastIndex));
            }
        }
        return words;
    }

    /**
     * 加载文件
     *
     * @param path 路径
     * @return list
     */
    private List<Doc> loadDocument(String path) {
        List<Doc> list = new ArrayList<>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputStream in = new GZIPInputStream(new FileInputStream(path));
            MyHandler myHandler = new MyHandler();
            saxParser.parse(in, myHandler);
            list = myHandler.getList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // For Test
        // Doc doc = new Doc();
        // doc.setId(1);
        // doc.setUrl("http://www.baidu.com");
        // doc.setTitle("hello world");
        // doc.setText("A donut on a glass plate. Only the donuts.");
        // list.add(doc);

        return list;
    }
}
