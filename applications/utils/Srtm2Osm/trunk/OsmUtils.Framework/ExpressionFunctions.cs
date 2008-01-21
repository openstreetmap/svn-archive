using System;
using System.Collections.Generic;
using System.Text;
using System.Diagnostics.CodeAnalysis;

namespace OsmUtils.Framework
{
    public sealed class ExpressionFunctions
    {
        [SuppressMessage ("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        static public bool IsTaggedWith (OsmWay osmElement, string key)
        {
            return osmElement.HasTag (key);
        }

        [SuppressMessage ("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        static public string ValueString (OsmWay osmElement, string key)
        {
            return osmElement.GetTagValue (key);
        }

        [SuppressMessage ("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        [SuppressMessage ("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "Num")]
        static public double ValueNum (OsmWay osmElement, string key)
        {
            return Double.Parse (osmElement.GetTagValue (key), System.Globalization.CultureInfo.InvariantCulture);
        }

        [SuppressMessage ("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        static public bool IsTaggedWith (OsmNode osmElement, string key)
        {
            return osmElement.HasTag (key);
        }

        [SuppressMessage ("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        static public string ValueString (OsmNode osmElement, string key)
        {
            return osmElement.GetTagValue (key);
        }

        [SuppressMessage ("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        [SuppressMessage ("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "Num")]
        static public double ValueNum (OsmNode osmElement, string key)
        {
            return Double.Parse (osmElement.GetTagValue (key), System.Globalization.CultureInfo.InvariantCulture);
        }

        private ExpressionFunctions () { }
    }
}
