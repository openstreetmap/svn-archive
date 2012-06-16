package josmrestartplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.PlatformHookWindows;
import org.openstreetmap.josm.tools.Shortcut;

public class RestartJosmAction extends JosmAction {

    public RestartJosmAction() {
        super(tr("Restart JOSM"), null, tr("Restart JOSM"),
                Shortcut.registerShortcut("file:restart",
                tr("File: {0}", tr("Restart JOSM")),
                KeyEvent.VK_J, Shortcut.ALT_CTRL_SHIFT),
                false);
        putValue("toolbar", "action/restart");
        Main.toolbar.register(this);
    }

    public void actionPerformed(ActionEvent arg0) {
        if (!Main.exitJosm(false)) return;
        try {
            File jarfile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String javacmd = "java";
            if (Main.platform instanceof PlatformHookWindows) javacmd = "javaw";
            LinkedList<String> cmds = new LinkedList<String>();
            cmds.add(javacmd);
            cmds.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
            cmds.add("-jar");
            cmds.add(jarfile.getAbsolutePath());
            for (String s : cmds)
                System.out.print(s + " ");
            System.out.println();

            Runtime.getRuntime().exec(cmds.toArray(new String[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}