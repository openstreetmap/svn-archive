using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.Common.Logging
{
    public interface ILogPublisher
    {
        EventHandler<LoggingEventEventArgs> LoggingEventArrived { get; set; }
    }
}
