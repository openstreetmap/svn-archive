using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Naming", "CA1711:IdentifiersShouldNotHaveIncorrectSuffix")]
    [Serializable]
    public class IsohypseCollection
    {
        public SortedList<double, Isohypse> Isohypses
        {
            get { return isohypses; }
        }

        public void AddIsohypse (Isohypse isohypse)
        {
            isohypses.Add (isohypse.Elevation, isohypse);
        }

        public Isohypse GetIsohypseForElevation (double elevation)
        {
            return isohypses[elevation];
        }

        private SortedList<double, Isohypse> isohypses = new SortedList<double, Isohypse> ();
    }
}
