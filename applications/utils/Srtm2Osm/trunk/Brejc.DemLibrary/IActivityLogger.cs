using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    public enum ActivityLogLevel
    {
        Normal,
        Verbose,
    }

    public interface IActivityLogger
    {
        ActivityLogLevel LogLevel { get; set;}

        void Log (ActivityLogLevel activityLogLevel, string message);
    }
}
