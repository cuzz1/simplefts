package com.cuzz;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author cuzz
 * @date 2020/8/15
 **/

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        String path = "/Users/cuzz/Downloads/axel/enwiki-latest-abstract1.xml.gz";
        List<Doc> docs = main.loadDocument(path);



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
            System.out.println(e.getStackTrace());
        }
        return list;
    }
}
