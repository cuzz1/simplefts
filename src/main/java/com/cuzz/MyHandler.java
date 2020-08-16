package com.cuzz;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class MyHandler extends DefaultHandler {

    private Integer id = 0;
    /**
     * 存放解析结果
     */
    List<Doc> list = null;

    /**
     * 存放当前
     */
    private Doc doc;

    /**
     * 用来存放每次遍历后的元素名称(节点名称)
     */
    private String tagName;

    public List<Doc> getList() {
        return list;
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    public void startDocument() throws SAXException {
        list = new ArrayList<>();
        System.out.println("===start===");
    }

    @Override
    public void endDocument() throws SAXException {
        System.out.println("===end===");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // 初始化
        if (Objects.equals(qName, "doc")) {
            doc = new Doc();
            doc.setId(this.id);
            this.id ++;
        }
        this.tagName = qName;

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("doc")) {
            this.list.add(this.doc);
        }
        this.tagName = null;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tagName == null) {
            return;
        }
        String date = new String(ch, start, length);
        if (this.tagName.equals("title")) {
            this.doc.setTitle(date);
        } else if (this.getTagName().equals("url")) {
            this.doc.setUrl(date);
        } else if (this.getTagName().equals("abstract")) {
            this.doc.setText(date);
        }

    }

    @Override
    public void error(SAXParseException e) throws SAXException {
    }


}