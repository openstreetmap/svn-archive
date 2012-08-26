using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace OSM2GTFS
{
    public class Calendar
    {
        private const string CalendarFilename = "calendar.txt";

        public List<ServiceType> serviceTypes;

        private string gtfsPath;

        /// <summary>
        /// Try to read any existing calendar.txt file
        /// </summary>
        /// <param name="path"></param>
        public Calendar(string path)
        {
            gtfsPath = path;
            serviceTypes = new List<ServiceType>();
            ReadCalendarFile();
        }

        /// <summary>
        /// Parse date string format YYYYMMDD 
        /// </summary>
        /// <param name="strDate"></param>
        /// <returns></returns>
        private DateTime ParseGTFSDate(string strDate)
        {
            int yyyy = Convert.ToInt32(strDate.Substring(0, 4));
            int mm = Convert.ToInt32(strDate.Substring(4, 2));
            int dd = Convert.ToInt32(strDate.Substring(6, 2));
            return new DateTime(yyyy, mm, dd);
        }

        private void ReadCalendarFile()
        {
            string fileSpec = Path.Combine(gtfsPath, CalendarFilename);
            if (File.Exists(fileSpec))
            {
                using (StreamReader sr = new StreamReader(fileSpec))
                {
                    sr.ReadLine();// Discard header (Assumed column order**)
                    while (!sr.EndOfStream)
                    {
                        string line = sr.ReadLine();
                        if (line.Length > 7)
                        {
                            var fields = line.Split(',');
                            var service = new ServiceType();
                            service.ServiceID = fields[0];
                            for (int i = 0; i < 7; i++)
                            {
                                service.WeekdayActive[i] = fields[i + 1] == "1";
                            }

                            service.StartDate = ParseGTFSDate(fields[8]);
                            service.EndDate = ParseGTFSDate(fields[9]);
                            serviceTypes.Add(service);
                        }
                    }

                    sr.Close();
                }
            }
            
        }

        /// <summary>
        /// Create date string format YYYYMMDD
        /// </summary>
        /// <param name="date"></param>
        private string FormatGTFSDate(DateTime date)
        {
            return date.ToString("yyyyMMdd");
        }


        public void WriteCalendarFile()
        {
            TimeScheduleForm.BackupFile(gtfsPath, CalendarFilename);
            string fileSpec = Path.Combine(gtfsPath, CalendarFilename);

            using (StreamWriter sw = new StreamWriter(fileSpec))
            {
                sw.WriteLine("service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date");
                foreach (ServiceType serviceType in serviceTypes)
                {
                    string outLine = serviceType.ServiceID + ",";

                    for (int i = 0; i < 7; i++)
                    {
                        if (serviceType.WeekdayActive[i])
                        {
                            outLine += "1,";
                        }
                        else
                        {
                            outLine += "0,";
                        }
                    }

                    outLine += FormatGTFSDate(serviceType.StartDate) + "," + FormatGTFSDate(serviceType.EndDate);

                    sw.WriteLine(outLine);
                }

                sw.Close();
            }

        }

    }

    public class ServiceType
    {
        public string ServiceID { get; set; }

        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }

        public bool[] WeekdayActive;

        public ServiceType()
        {
            WeekdayActive = new bool[7];
        }

    }
}
