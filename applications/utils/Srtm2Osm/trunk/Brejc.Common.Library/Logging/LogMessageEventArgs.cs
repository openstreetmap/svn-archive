using System;
using System.Collections.Generic;
using System.Text;
using log4net.Core;

namespace Brejc.Common.Logging
{
    public class LoggingEventEventArgs : EventArgs
    {
        public LoggingEvent LoggingEvent
        {
            get { return loggingEvent; }
        }

        public LoggingEventEventArgs (LoggingEvent loggingEvent)
        {
            this.loggingEvent = loggingEvent;
        }

        private LoggingEvent loggingEvent;
    }
}
