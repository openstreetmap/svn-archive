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
            LogFormat(activityLogLevel, "{0}", message);
        }

        public void LogFormat (ActivityLogLevel activityLogLevel, string format,
            params object[] args)
        {
            if (activityLogLevel <= logLevel)
            {
                if (activityLogLevel == ActivityLogLevel.Warning)
                    format = "WARNING: " + format;
                else if (activityLogLevel == ActivityLogLevel.Error)
                    format = "ERROR: " + format;

                System.Console.Out.WriteLine(format, args);
            }
        }

        private ActivityLogLevel logLevel = ActivityLogLevel.Normal;
    }
}
