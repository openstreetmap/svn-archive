using System;
using System.Collections.Generic;
using System.IO;
using System.Security;
using System.Security.Permissions;
using Brejc.DemLibrary;

namespace Srtm2Osm
{
    /// <summary>
    /// Callback used to get the next available OSM element ID.
    /// </summary>
    /// <returns></returns>
    public delegate long NextIdCallback ();

    abstract class OutputBase
    {
        public OutputBase (FileInfo file, OutputSettings settings)
        {
            if (file == null)
                throw new ArgumentNullException ("file");
            if (settings == null)
                throw new ArgumentNullException ("settings");

            this.file = file;
            this.settings = settings;
        }

        abstract public void Begin ();
        abstract public void ProcessIsohypse (Isohypse isohypse, NextIdCallback nodeCallback, NextIdCallback wayCallback);
        abstract public void End ();

        abstract public void Merge (string mergeFile);

        protected readonly FileInfo file;
        protected readonly OutputSettings settings;
    }
}
