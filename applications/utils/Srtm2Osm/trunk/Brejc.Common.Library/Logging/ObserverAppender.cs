using System;
using System.Collections.Generic;
using System.Text;
using log4net.Appender;
using log4net.Core;

namespace Brejc.Common.Logging
{
    public class ObserverAppender : AppenderSkeleton, ILogPublisher
    {
        public EventHandler<LoggingEventEventArgs> LoggingEventArrived { get; set; }

        protected override void Append (log4net.Core.LoggingEvent loggingEvent)
        {
            lock (events)
            {
                events.Add (loggingEvent);

                if (LoggingEventArrived != null)
                    LoggingEventArrived.Invoke (this, new LoggingEventEventArgs (loggingEvent));
            }
        }

        private List<LoggingEvent> events = new List<LoggingEvent> ();
    }
}
