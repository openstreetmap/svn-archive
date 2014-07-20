using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    public class ConsoleActivityLogger : IActivityLogger
    {
        public ActivityLogLevel LogLevel
        {
            get
            {
                return logLevel;
            }
            set
            {
                logLevel = value;
            }
        }

        public void Log (ActivityLogLevel activityLogLevel, string message)
        {
            if (activityLogLevel <= logLevel)
                System.Console.Out.WriteLine (message);
        }

        public void LogFormat (ActivityLogLevel activityLogLevel, string format,
            params object[] args)
        {
            if (activityLogLevel <= logLevel)
                System.Console.Out.WriteLine(format, args);
        }

        private ActivityLogLevel logLevel = ActivityLogLevel.Normal;
    }
}
