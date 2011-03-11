package org.openstreetmap.josm.plugins.namemanager.utils;

import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.namemanager.NameManagerPlugin;
import org.openstreetmap.josm.plugins.namemanager.countryData.Country;
import org.openstreetmap.josm.plugins.namemanager.countryData.CountryDataMemory;
import org.openstreetmap.josm.tools.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NameManagerUtils {
    /**
     * @return the top {@link Window} of the JOSM application.
     */
    public static Window getTopWindow() {
        Component component = Main.panel;
        if (component != null) {
            while (component.getParent() != null) {
                component = component.getParent();
            }
            if (component instanceof Window)
                return (Window) component;
        }
        return null;
    }

    public static List<Way> getWaysInsideSelectedArea(Way areaBorder) {
        List<Way> waysInsideSelectedArea = new ArrayList<Way>();
        if (areaBorder != null) {
            Coordinate topLeftCorner = getTopLeftCorener(areaBorder);
            List<Pair<Node, Node>> areaBorderLines = areaBorder.getNodePairs(true);
            Collection<Way> ways = Main.main.getCurrentDataSet().getWays();
            ways: for (Way way : ways) {
                if (areaBorder == way) {
                    continue ways;
                }
                pairs: for (Pair<Node, Node> pair : way.getNodePairs(false)) {
                    double x1Pair = pair.a.getCoor().getX();
                    double y1Pair = pair.a.getCoor().getY();
                    double x2Pair = pair.b.getCoor().getX();
                    double y2Pair = pair.b.getCoor().getY();
                    double aPair = 0.0;
                    double bPair = 0.0;
                    double cPair = 0.0;
                    boolean pairPerpendicular = false;
                    if (x2Pair == x1Pair || y2Pair == y1Pair) {
                        pairPerpendicular = true;
                    } else {
                        aPair = 1 / (x2Pair - x1Pair);
                        bPair = -1 / (y2Pair - y1Pair);
                        cPair = y1Pair / (y2Pair - y1Pair) - x1Pair / (x2Pair - x1Pair);
                    }
                    int crossCount = 0;
                    areaLine: for (Pair<Node, Node> areaLine : areaBorderLines) {
                        double x1Line = areaLine.a.getCoor().getX();
                        double y1Line = areaLine.a.getCoor().getY();
                        double x2Line = areaLine.b.getCoor().getX();
                        double y2Line = areaLine.b.getCoor().getY();
                        boolean areaLinePerpendicular = false;
                        double aLine = 0.0;
                        double bLine = 0.0;
                        double cLine = 0.0;
                        if (x2Line == x1Line || y2Line == y1Line) {
                            areaLinePerpendicular = true;
                        } else {
                            aLine = 1 / (x2Line - x1Line);
                            bLine = -1 / (y2Line - y1Line);
                            cLine = y1Line / (y2Line - y1Line) - x1Line / (x2Line - x1Line);
                        }
                        if (pairPerpendicular && areaLinePerpendicular) {
                            if (x1Pair == x2Pair) {
                                if (x1Line == x2Line) {
                                    if (x1Pair == x1Line) {
                                        if ((y1Pair - y1Line) * (y1Pair - y2Line) < 0.0
                                                || (y2Pair - y1Line) * (y2Pair - y2Line) < 0.0
                                                || (y1Line - y1Pair) * (y1Line - y2Pair) < 0.0
                                                || (y2Line - y1Pair) * (y2Line - y2Pair) < 0.0) {
                                            waysInsideSelectedArea.add(way);
                                            continue ways;
                                        }
                                    }
                                } else if (y1Line == y2Line) {
                                    if ((x1Pair - x1Line) * (x1Pair - x2Line) < 0.0
                                            && (y1Line - y1Pair) * (y1Line - y2Pair) < 0.0) {
                                        waysInsideSelectedArea.add(way);
                                        continue ways;
                                    }
                                }
                            } else if (y1Pair == y2Pair) {
                                if (x1Line == x2Line) {
                                    if ((y1Pair - y1Line) * (y1Pair - y2Line) < 0.0
                                            && (x1Line - x1Pair) * (x1Line - x2Pair) < 0.0) {
                                        waysInsideSelectedArea.add(way);
                                        continue ways;
                                    }
                                } else if (y1Line == y2Line) {
                                    if (y1Pair == y1Line) {
                                        if ((x1Pair - x1Line) * (x1Pair - x2Line) < 0.0
                                                || (x2Pair - x1Line) * (x2Pair - x2Line) < 0.0
                                                || (x1Line - x1Pair) * (x1Line - x2Pair) < 0.0
                                                || (x2Line - x1Pair) * (x2Line - x2Pair) < 0.0) {
                                            waysInsideSelectedArea.add(way);
                                            continue ways;
                                        }
                                    }
                                }
                            }
                        } else if (pairPerpendicular) {
                            if (((aLine * x1Pair + bLine * y1Pair + cLine) * (aLine * x2Pair
                                    + bLine * y2Pair + cLine)) < 0.0) {
                                if (x1Pair == x2Pair) {
                                    if ((x1Pair - x1Line) * (x1Pair - x2Line) < 0.0) {
                                        waysInsideSelectedArea.add(way);
                                        continue ways;
                                    }
                                } else if (y1Pair == y2Pair) {
                                    if ((y1Pair - y1Line) * (y1Pair - y2Line) < 0.0) {
                                        waysInsideSelectedArea.add(way);
                                        continue ways;
                                    }
                                }
                            }
                        } else if (areaLinePerpendicular) {
                            if (((aPair * x1Line + bPair * y1Line + cPair) * (aPair * x2Line
                                    + bPair * y2Line + cPair)) < 0.0) {
                                if (x1Line == x2Line) {
                                    if ((x1Line - x1Pair) * (x1Line - x2Pair) < 0.0) {
                                        waysInsideSelectedArea.add(way);
                                        continue ways;
                                    }
                                } else if (y1Line == y2Line) {
                                    if ((y1Line - y1Pair) * (y1Line - y2Pair) < 0.0) {
                                        waysInsideSelectedArea.add(way);
                                        continue ways;
                                    }
                                }
                            }
                        } else if (((aLine * x1Pair + bLine * y1Pair + cLine) * (aLine * x2Pair
                                + bLine * y2Pair + cLine)) < 0.0
                                && ((aPair * x1Line + bPair * y1Line + cPair) * (aPair * x2Line
                                        + bPair * y2Line + cPair)) < 0.0) {
                            waysInsideSelectedArea.add(way);
                            continue ways;
                        }
                        Node raySource = null;
                        double tlcX = topLeftCorner.getLon();
                        double tlcY = topLeftCorner.getLat();
                        if (x1Pair != tlcX && y1Pair != tlcY) {
                            raySource = pair.a;
                        } else if (x2Pair != tlcX && y2Pair != tlcY) {
                            raySource = pair.b;
                        } else {
                            continue pairs;
                        }
                        if (raySource != null) {
                            double rsX = raySource.getCoor().getX();
                            double rsY = raySource.getCoor().getY();
                            if (areaLinePerpendicular) {
                                if (x1Line == x2Line) {
                                    if ((x1Line - rsX) * (x1Line - tlcX) > 0.0) {
                                        continue areaLine;
                                    }
                                } else if (y1Line == y2Line) {
                                    if ((y1Line - rsY) * (y1Line - tlcY) > 0.0) {
                                        continue areaLine;
                                    }
                                }
                            } else if (((aLine * rsX + bLine * rsY + cLine) * (aLine * tlcX + bLine
                                    * tlcY + cLine)) > 0.0) {
                                continue areaLine;
                            }
                            double aRay = 1 / (tlcX - rsX);
                            double bRay = -1 / (tlcY - rsY);
                            double cRay = rsY / (tlcY - rsY) - rsX / (tlcX - rsX);
                            if (((aRay * x1Line + bRay * y1Line + cRay) * (aRay * x2Line + bRay
                                    * y2Line + cRay)) < 0.0) {
                                crossCount++;
                            }
                        }
                    }
                    if (crossCount % 2 == 1) {
                        waysInsideSelectedArea.add(way);
                        continue ways;
                    }
                }
            }
        }
        return waysInsideSelectedArea;
    }

    private static Coordinate getTopLeftCorener(Way way) {
        double x = Double.POSITIVE_INFINITY;
        double y = Double.NEGATIVE_INFINITY;
        List<Node> nodes = way.getNodes();
        for (Node node : nodes) {
            if (node.getCoor().getX() < x) {
                x = node.getCoor().getX();
            }
            if (node.getCoor().getY() > y) {
                y = node.getCoor().getY();
            }
        }
        x--;
        y++;
        return new Coordinate(y, x);
    }

    public static Document parseCountries() {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            InputStream xml = NameManagerPlugin.class
                    .getResourceAsStream("/resources/administrative-levels.xml");
            doc = db.parse(xml);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static void prepareCountryDataMemoryCache(Document doc) {
        CountryDataMemory.instantiateCountryCache();
        if (!CountryDataMemory.isEmpty()) {
            CountryDataMemory.clearCache();
        }
        NodeList nodeList = doc.getElementsByTagName("country");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element countryNode = (Element) nodeList.item(i);
            String countryName = countryNode.getAttributes().item(0).getNodeValue();
            NodeList level1List = countryNode.getElementsByTagName("level1");
            String level1 = "n/a";
            if (level1List.getLength() == 1) {
                level1 = level1List.item(0).getTextContent();
            }
            NodeList level2List = countryNode.getElementsByTagName("level2");
            String level2 = "n/a";
            if (level2List.getLength() == 1) {
                level2 = level2List.item(0).getTextContent();
            }
            NodeList level3List = countryNode.getElementsByTagName("level3");
            String level3 = "n/a";
            if (level3List.getLength() == 1) {
                level3 = level3List.item(0).getTextContent();
            }
            NodeList level4List = countryNode.getElementsByTagName("level4");
            String level4 = "n/a";
            if (level4List.getLength() == 1) {
                level4 = level4List.item(0).getTextContent();
            }
            NodeList level5List = countryNode.getElementsByTagName("level5");
            String level5 = "n/a";
            if (level5List.getLength() == 1) {
                level5 = level5List.item(0).getTextContent();
            }
            NodeList level6List = countryNode.getElementsByTagName("level6");
            String level6 = "n/a";
            if (level6List.getLength() == 1) {
                level6 = level6List.item(0).getTextContent();
            }
            CountryDataMemory.addCountry(new Country(countryName, level1, level2, level3, level4,
                    level5, level6));
        }
    }
}
