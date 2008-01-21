using System;
using System.Collections.Generic;
using System.Text;
using OsmUtils.Framework;
using Brejc.DemLibrary;

namespace Srtm2Osm
{
    public interface IContourMarker
    {
        void Configure (double elevationUnits);

        void MarkContour (OsmWay isohypseWay, Isohypse isohypse);
        void MarkContour (OsmUtils.OsmSchema.osmWay isohypseWay, Isohypse isohypse);
    }
}
