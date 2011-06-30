package org.openstreetmap.josm.plugins.ohe.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.plugins.ohe.ClockSystem;
import org.openstreetmap.josm.plugins.ohe.OhePlugin;
import org.openstreetmap.josm.plugins.ohe.OpeningTimeUtils;
import org.openstreetmap.josm.plugins.ohe.parser.OpeningTimeCompiler;
import org.openstreetmap.josm.plugins.ohe.parser.ParseException;
import org.openstreetmap.josm.plugins.ohe.parser.SyntaxException;
import org.openstreetmap.josm.plugins.ohe.parser.TokenMgrError;
import org.openstreetmap.josm.tools.GBC;

public class OheDialogPanel extends JPanel {

    private final JTextField keyField;

    // The Component for showing the Time as a Text
    private final JTextField valueField;

    private final JButton twentyfourSevenButton;
    private final JLabel actualPostionLabel;

    // The important Panel for showing/editing the Time graphical
    private final OheEditor editorPanel;

    private final String oldkey;

    private ClockSystem clockSystem;

    /**
     * The Panel for editing the time-values.
     * 
     * @param plugin
     * @param key
     * @param valuesToEdit
     *            can be a String or a Map<String, Integer> which contains
     *            multiple values and their number of occurences
     */
    public OheDialogPanel(OhePlugin plugin, String key, Object valuesToEdit, ClockSystem clockSystem) {
        this.clockSystem = clockSystem;

        oldkey = key;
        keyField = new JTextField(key);

        String value = "";
        if (valuesToEdit instanceof String)
            value = (String) valuesToEdit;
        else if (valuesToEdit instanceof Map<?, ?>) {
            Map<String, Integer> valuesMap = (Map<String, Integer>) valuesToEdit;
            if (valuesMap.size() == 1)
                value = valuesMap.keySet().iterator().next();
            else if (valuesMap.size() > 1) {
                // TODO let the user choose which value he wants to edit (e.g.
                // with a combobox)
                int mostOccurences = 0;
                for (String v : valuesMap.keySet())
                    if (valuesMap.get(v) > mostOccurences) {
                        value = v;
                        mostOccurences = valuesMap.get(v);
                    }
            }
        }
        valueField = new JTextField(value);
        valueField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // on every action in the textfield the timeRects are reloaded
                editorPanel.initTimeRects();
            }
        });

        twentyfourSevenButton = new JButton(tr("apply {0}", "24/7"));
        twentyfourSevenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                valueField.setText("24/7");
                editorPanel.initTimeRects();
            }
        });

        actualPostionLabel = new JLabel("-");
        JPanel toolsPanel = new JPanel(new GridBagLayout());
        toolsPanel.add(twentyfourSevenButton, GBC.std());
        toolsPanel.add(Box.createGlue(), GBC.std().fill(GBC.HORIZONTAL));
        toolsPanel.add(actualPostionLabel, GBC.eop());

        editorPanel = new OheEditor(this);

        // adding all Components in a Gridbaglayout
        setLayout(new GridBagLayout());
        add(new JLabel(tr("Key")), GBC.std());
        add(Box.createHorizontalStrut(10), GBC.std());
        add(keyField, GBC.eol().fill(GBC.HORIZONTAL));
        add(new JLabel(tr("Value")), GBC.std());
        add(Box.createHorizontalStrut(10), GBC.std());
        add(valueField, GBC.eop().fill(GBC.HORIZONTAL));
        add(toolsPanel, GBC.eol().fill(GBC.HORIZONTAL));
        add(editorPanel, GBC.eol().fill());

        setPreferredSize(new Dimension(480, 520));
    }

    public String[] getChangedKeyValuePair() {
        return new String[] { oldkey, keyField.getText(), valueField.getText() };
    }

    // returns the compiled Time from the valueField
    public ArrayList<int[]> getTime() throws Exception {
        String value = valueField.getText();
        ArrayList<int[]> time = null;
        if (value.length() > 0) {
            OpeningTimeCompiler compiler = new OpeningTimeCompiler(value);
            try {
                time = OpeningTimeUtils.convert(compiler.startCompile());
            } catch (Throwable t) {
                int tColumns[] = null;
                String info = null;

                if (t instanceof ParseException) {
                    ParseException parserExc = (ParseException) t;
                    tColumns = new int[] { parserExc.currentToken.beginColumn - 1, parserExc.currentToken.endColumn + 1 };
                } else if (t instanceof SyntaxException) {
                    SyntaxException syntaxError = (SyntaxException) t;
                    tColumns = new int[] { syntaxError.getStartColumn(), syntaxError.getEndColumn() };
                    info = syntaxError.getInfo();
                } else if (t instanceof TokenMgrError) {
                    TokenMgrError tokenMgrError = (TokenMgrError) t;
                    tColumns = new int[] { tokenMgrError.errorColumn - 1, tokenMgrError.errorColumn + 1 };
                } else {
                    t.printStackTrace();
                }

                // shows a Information Dialog, where the Error occurred
                if (tColumns != null) {
                    int first = Math.max(0, tColumns[0]);
                    int last = Math.min(value.length(), tColumns[1]);
                    String begin = value.substring(0, first);
                    String middle = value.substring(first, last);
                    String end = value.substring(last);
                    valueField.setCaretPosition(first);
                    // TODO focus on the valueField
                    String message = "<html>" + tr("There is something wrong in the value near:") + "<br>" + begin
                            + "<span style='background-color:red;'>" + middle + "</span>" + end;
                    if (info != null)
                        message += "<br>" + tr("Info: {0}", tr(info));
                    message += "<br>" + tr("Correct the value manually and than press Enter.");
                    message += "</html>";
                    JOptionPane.showMessageDialog(this, message, tr("Error in timeformat"),
                            JOptionPane.INFORMATION_MESSAGE);
                }

                throw new Exception("Error in the TimeValue");
            }
        }

        return time;
    }

    // updates the valueField with the given timeRects
    public void updateValueField(ArrayList<TimeRect> timeRects) {
        if (valueField != null && timeRects != null)
            valueField.setText(OpeningTimeUtils.makeStringFromRects(timeRects));
    }

    public void setMousePositionText(String positionText) {
        actualPostionLabel.setText(positionText);
    }

    /**
     * @return the hourMode
     */
    public ClockSystem getHourMode() {
        return clockSystem;
    }
}