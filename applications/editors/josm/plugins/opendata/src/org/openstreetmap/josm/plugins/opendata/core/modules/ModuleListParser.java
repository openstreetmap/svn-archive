//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * A parser for the module list provided by an opendata Module Download Site.
 *
 * See <a href="http://josm.openstreetmap.de/module">http://josm.openstreetmap.de/module</a>
 * for a sample of the document. The format is a custom format, kind of mix of CSV and RFC822 style
 * name/value-pairs.
 *
 */
public class ModuleListParser {

    /**
     * Creates the module information object
     *
     * @param name the module name
     * @param url the module download url
     * @param manifest the module manifest
     * @return a module information object
     * @throws ModuleListParseException
     */
    protected ModuleInformation createInfo(String name, String url, String manifest) throws ModuleListParseException{
        try {
            return new ModuleInformation(
                    new ByteArrayInputStream(manifest.getBytes("utf-8")),
                    name.substring(0, name.length() - 4),
                    url
            );
        } catch(UnsupportedEncodingException e) {
            throw new ModuleListParseException(tr("Failed to create module information from manifest for module ''{0}''", name), e);
        } catch (ModuleException e) {
            throw new ModuleListParseException(tr("Failed to create module information from manifest for module ''{0}''", name), e);
        }
    }

    /**
     * Parses a module information document and replies a list of module information objects.
     *
     * See <a href="http://josm.openstreetmap.de/module">http://josm.openstreetmap.de/module</a>
     * for a sample of the document. The format is a custom format, kind of mix of CSV and RFC822 style
     * name/value-pairs.
     *
     * @param in the input stream from which to parse
     * @return the list of module information objects
     * @throws ModuleListParseException thrown if something goes wrong while parsing
     */
    public List<ModuleInformation> parse(InputStream in) throws ModuleListParseException{
        List<ModuleInformation> ret = new LinkedList<ModuleInformation>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String name = null;
            String url = null;
            StringBuilder manifest = new StringBuilder();
            /*
            code structure:
                for () {
                    A;
                    B;
                    C;
                }
                B;
            */
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                if (line.startsWith("\t")) {
                    line = line.substring(1);
                    if (line.length() > 70) {
                        manifest.append(line.substring(0, 70)).append("\n");
                        line = " " + line.substring(70);
                    }
                    manifest.append(line).append("\n");
                    continue;
                }
                if (name != null) {
                    ModuleInformation info = createInfo(name, url, manifest.toString());
                    if (info != null) {
                        for (Module module : ModuleHandler.moduleList) {
                            if (module.getModuleInformation().name.equals(info.getName())) {
                                info.localversion = module.getModuleInformation().localversion;
                            }
                        }
                        ret.add(info);
                    }
                }
                String x[] = line.split(";");
                if(x.length != 2)
                  throw new IOException(tr("Illegal entry in module list."));
                name = x[0];
                url = x[1];
                manifest = new StringBuilder();

            }
            if (name != null) {
                ModuleInformation info = createInfo(name, url, manifest.toString());
                if (info != null) {
                    for (Module module : ModuleHandler.moduleList) {
                        if (module.getModuleInformation().name.equals(info.getName())) {
                            info.localversion = module.getModuleInformation().localversion;
                        }
                    }
                    ret.add(info);
                }
            }
            return ret;
        } catch (IOException e) {
            throw new ModuleListParseException(e);
        }
    }
}
