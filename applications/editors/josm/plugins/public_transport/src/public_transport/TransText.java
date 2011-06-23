package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

public class TransText
{
    public String text;
    public TransText(String t)
    {
        text = t;
    }
    public String toString()
    {
        return text == null ? "" : tr(text);
    }
}
