// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import java.util.AbstractList;
import java.util.Collection;

public class XPath implements javax.xml.xpath.XPath {
    private final javax.xml.xpath.XPath xPath;
    private static XPath INSTANCE = new XPath(XPathFactory.newInstance().newXPath());

    private XPath(javax.xml.xpath.XPath xPath) {
        this.xPath = xPath;
    }

    public static XPath getInstance() {
        return INSTANCE;
    }

    public static class UncheckedXPathExpressionException extends RuntimeException {
        public UncheckedXPathExpressionException(Throwable cause) {
            super(cause);
        }
    }

    public String evaluateString(String expression, Object item) throws UncheckedXPathExpressionException {
        try {
            return (String) xPath.evaluate(expression, item, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new UncheckedXPathExpressionException(e);
        }
    }

    public double evaluateDouble(String expression, Object item) throws UncheckedXPathExpressionException {
        try {
            return ((Number) xPath.evaluate(expression, item, XPathConstants.NUMBER)).doubleValue();
        } catch (XPathExpressionException e) {
            throw new UncheckedXPathExpressionException(e);
        }
    }

    public Node evaluateNode(String expression, Object item) throws UncheckedXPathExpressionException {
        try {
            return (Node) evaluate(expression, item, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new UncheckedXPathExpressionException(e);
        }
    }

    public Collection<Node> evaluateNodes(String expression, Object item) throws UncheckedXPathExpressionException {
        try {
            final NodeList nodes = (NodeList) evaluate(expression, item, XPathConstants.NODESET);
            return new AbstractList<Node>() {
                @Override
                public Node get(int index) {
                    return nodes.item(index);
                }

                @Override
                public int size() {
                    return nodes.getLength();
                }
            };
        } catch (XPathExpressionException e) {
            throw new UncheckedXPathExpressionException(e);
        }
    }

    @Override
    public void reset() {
        xPath.reset();
    }

    @Override
    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        xPath.setXPathVariableResolver(resolver);
    }

    @Override
    public XPathVariableResolver getXPathVariableResolver() {
        return xPath.getXPathVariableResolver();
    }

    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        xPath.setXPathFunctionResolver(resolver);
    }

    @Override
    public XPathFunctionResolver getXPathFunctionResolver() {
        return xPath.getXPathFunctionResolver();
    }

    @Override
    public void setNamespaceContext(NamespaceContext nsContext) {
        xPath.setNamespaceContext(nsContext);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return xPath.getNamespaceContext();
    }

    @Override
    public XPathExpression compile(String expression) throws XPathExpressionException {
        return xPath.compile(expression);
    }

    @Override
    public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
        return xPath.evaluate(expression, source, returnType);
    }

    @Override
    public String evaluate(String expression, InputSource source) throws XPathExpressionException {
        return xPath.evaluate(expression, source);
    }

    @Override
    public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        return xPath.evaluate(expression, item, returnType);
    }

    @Override
    public String evaluate(String expression, Object item) throws XPathExpressionException {
        return xPath.evaluate(expression, item);
    }
}
