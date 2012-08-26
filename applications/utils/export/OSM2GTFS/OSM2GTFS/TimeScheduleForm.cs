using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Forms;

namespace OSM2GTFS
{
    public partial class TimeScheduleForm : Form
    {
        private Calendar calendar;

        private Dictionary<Int64, Relation> routeMasters;
        private Dictionary<Int64, Relation> routes;
        private Dictionary<Int64, Way> ways;
        private Dictionary<Int64, Coord> nodes;
        private Dictionary<Int64, StopDistance> stopPositions;

        private Relation activeRoute = null;  // Route being edited
        private ServiceType activeService = null; // Service schedule being edited
        private string gtfsPath;

        private bool initializing = true;

        public TimeScheduleForm(string gtfsPath_,
                                Dictionary<Int64, Relation> routeMasters_,
                                Dictionary<Int64, Relation> routes_,
                                Dictionary<Int64, Way> ways_,
                                Dictionary<Int64, Coord> nodes_,
                                Dictionary<Int64, StopDistance> stopPositions_
            )
        {
            try
            {

                InitializeComponent();

                gtfsPath = gtfsPath_;
                routeMasters = routeMasters_;
                routes = routes_;
                ways = ways_;
                nodes = nodes_;
                stopPositions = stopPositions_;

                calendar = new Calendar(gtfsPath);

                ClearRouteTrips();

                foreach (ServiceType service in calendar.serviceTypes)
                {
                    lbServiceType.Items.Add(service.ServiceID);
                }
                if (calendar.serviceTypes.Count > 0)
                {
                    activeService = calendar.serviceTypes[0];
                    ShowActiveServiceDetail();
                    lbServiceType.SelectedIndex = 0;
                }
                LoadRoutesToListbox();

                ReadTripsAndStopTimes();

                this.WindowState = FormWindowState.Maximized;
                initializing = false;

                UpdateStatus();  // Actually display now
            }
            catch (Exception ex)
            {
                MessageBox.Show("Problem showing times: " + ex.Message + ex.StackTrace);
            }
        }


        /// <summary>
        /// Clear any possible trips from routes in a previous screen
        /// </summary>
        private void ClearRouteTrips()
        {
            foreach (Relation routeMaster in routeMasters.Values)
            {
                routeMaster.TripList = null;
            }
        }

        private void LoadRoutesToListbox()
        {
            foreach (Relation routeMaster in routeMasters.Values)
            {
                string reference = routeMaster.TagList["ref"];
                lbRoutes.Items.Add(reference);
            }
            if (routeMasters.Count > 0)
            {
                lbRoutes.SelectedIndex = 0;
            }
        }

        private void btAddService_Click(object sender, EventArgs e)
        {
            string newService = tbServiceID.Text.Trim();
            if (newService.Length > 0)
            {

                ServiceType service = new ServiceType();
                service.ServiceID = newService;
                service.StartDate = DateTime.Now;
                int thisYear = DateTime.Now.Year;
                service.EndDate = new DateTime(thisYear + 1, 12, 31);

                calendar.serviceTypes.Add(service);
                activeService = service;

                lbServiceType.Items.Add(newService);
                lbServiceType.SelectedItem = newService;

                ShowActiveServiceDetail();
                UpdateStatus();
            }
        }

        private void ShowActiveServiceDetail()
        {
            tbStartDate.Text = activeService.StartDate.ToShortDateString();
            tbEndDate.Text = activeService.EndDate.ToShortDateString();

            cbMon.Checked = activeService.WeekdayActive[0];
            cbTue.Checked = activeService.WeekdayActive[1];
            cbWed.Checked = activeService.WeekdayActive[2];
            cbThu.Checked = activeService.WeekdayActive[3];
            cbFri.Checked = activeService.WeekdayActive[4];
            cbSat.Checked = activeService.WeekdayActive[5];
            cbSun.Checked = activeService.WeekdayActive[6];

        }

        private void lbServiceType_SelectedIndexChanged(object sender, EventArgs e)
        {
            string serviceID = lbServiceType.SelectedItem.ToString();
            foreach (ServiceType service in calendar.serviceTypes)
            {
                if (service.ServiceID == serviceID)
                {
                    SaveSchedule();
                    activeService = service;
                    ShowActiveServiceDetail();
                    UpdateStatus();
                    return;
                }
            }
        }


        /// <summary>
        /// Save list of times for this route/service to route from grid
        /// Return true if no formatting errors
        /// </summary>
        private bool SaveSchedule()
        {
            try
            {

                if (initializing || activeRoute == null || activeService == null) return true;

                string serviceID = activeService.ServiceID;
                // Remove any existing trips of this service type as it is copied to new schedule
                var newSchedule = new List<Trip>();
                if (activeRoute.TripList != null)
                {
                    foreach (Trip trip in activeRoute.TripList)
                    {
                        if (trip.ServiceId.CompareTo(serviceID)!= 0)
                        {
                            newSchedule.Add(trip);
                        }
                    }
                }

                var stopIDList = ReadStopIDs();

                // Save current trips
                for (int row = 0; row < gridTimes.Rows.Count - 1; row++)
                {
                    var trip = new Trip(serviceID);
                    trip.StopIDList = stopIDList;
                    trip.StopTimeList = ParseTimeRow(gridTimes.Rows[row]);
                    if (trip.StopTimeList == null) return false;
                    newSchedule.Add(trip);
                }


                activeRoute.TripList = newSchedule;

            }
            catch (Exception ex)
            {
                MessageBox.Show("Problem saving schedule data: " + ex.Message + ex.StackTrace);
                return false;
            }

            return true;
        }


        /// <summary>
        /// Read stop ID tags from time grid column
        /// </summary>
        /// <returns></returns>
        private List<int> ReadStopIDs()
        {
            var stopIDs = new List<int>();
            for (int col = 0; col < gridTimes.Columns.Count; col++)
            {
                stopIDs.Add((int)gridTimes.Columns[col].Tag);
            }
            return stopIDs;
        }

        /// <summary>
        /// Not all vehicles stop at all stops along their route - be sure
        /// this stop is contained in the relation
        /// </summary>
        /// <param name="route"></param>
        /// <param name="stop"></param>
        /// <returns></returns>
        private bool RouteContainsStop(Relation route, Coord stopNode, out RelationMember stopMember)
        {
            stopMember = null;
            foreach (RelationMember relationMember in route.Members)
            {
                if (relationMember.RelationType == "node")
                {
                    var node = nodes[relationMember.RelationRef];
                    if (Form1.IsStop(node))
                    {
                        if (node.NodeID == stopNode.NodeID)
                        {
                            stopMember = relationMember;
                            return true;
                        }
                    }
                }
            }
            return false;
        }


        private void ShowStops()
        {

            // Show stops for activeRoute (which is a route_master relation)
            gridTimes.Columns.Clear();
            double distanceMeters = 0.0;
            Coord lastNode = null;
            Int64 lastNodeID = 0;
            activeRoute.StopDistances = new List<double>();

            foreach (RelationMember routeMember in activeRoute.Members)
            {
                var route = routes[routeMember.RelationRef];
                foreach (RelationMember wayMember in route.SortedWays)
                {
                    Way way = ways[wayMember.RelationRef];
                    int increment = 1;
                    int wayIndex = 0;
                    if (wayMember.Role == "backward")
                    {
                        increment = -1;
                        wayIndex = way.NodeList.Count - 1;
                    }

                    for (int i = 0; i < way.NodeList.Count; i++)
                    {
                        var nodeID = way.NodeList[wayIndex];

                        if (nodeID != lastNodeID)   // Don't process duplicate node (adjoining way)
                        {
                            var node = nodes[nodeID];
                            if (lastNode != null)
                            {
                                var currentSegment = node.GreatCircleDistance(lastNode);
                                distanceMeters += currentSegment;
                            }
                            lastNode = node;

                            if (nodeID < 0)
                            {
                                // This is a virtual stop position
                                var stopInfo = stopPositions[nodeID];
                                RelationMember stopMember = null;
                                if (RouteContainsStop(route, stopInfo.StopNode, out stopMember))
                                {
                                    if (stopInfo.StopNode.StopID < 0)
                                    {
                                        MessageBox.Show("Unassigned stop number");
                                    }
                                    AddStopTimeColumn(stopInfo.StopNode);
                                    activeRoute.StopDistances.Add(distanceMeters);
                                }
                            }
                        }

                        wayIndex += increment;
                        lastNodeID = nodeID;
                    }

                }

            }

            if (activeRoute.TripList != null)
            {
                // Show any trips
                int row = 0;
                foreach (Trip trip in activeRoute.TripList)
                {
                    if (trip.ServiceId == activeService.ServiceID)
                    {
                        gridTimes.Rows.Add();
                        var timeRow = gridTimes.Rows[row];
                        row++;
                        for (int col = 0; col < trip.StopTimeList.Count; col++)
                        {
                            if (trip.StopTimeList[col] != DateTime.MaxValue)
                            {
                                if (col < timeRow.Cells.Count)
                                {
                                    timeRow.Cells[col].Value = trip.StopTimeList[col].ToString("HH:mm:ss");
                                }
                            }
                        }
                    }
                }
            }

        }

        private void AddStopTimeColumn(Coord stopNode)
        {
            var newColumn = new DataGridViewTextBoxColumn();
            newColumn.Tag = stopNode.StopID;
            newColumn.SortMode = DataGridViewColumnSortMode.NotSortable;
            string stopName = stopNode.NodeID.ToString();
            if (stopNode.TagList.ContainsKey("name")) stopName = stopNode.TagList["name"];
            newColumn.HeaderText = stopName;
            gridTimes.Columns.Add(newColumn);
        }

        private void UpdateStatus()
        {
            try
            {
                string status = "No route selected";
                if (activeRoute != null)
                {
                    status = "Editing route " + activeRoute.TagList["ref"];
                    if (activeService != null)
                    {
                        status += " for service schedule " + activeService.ServiceID;
                        lbStatus.Text = status;
                        ShowStops();
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Problem showing route / service info: " + ex.Message + ex.StackTrace);
            }
        }

        private void lbRoutes_SelectedIndexChanged(object sender, EventArgs e)
        {
            string strRouteRef = lbRoutes.SelectedItem.ToString();
            foreach (Relation routeMaster in routeMasters.Values)
            {
                if (routeMaster.TagList["ref"] == strRouteRef)
                {
                    SaveSchedule();
                    activeRoute = routeMaster;
                    UpdateStatus();

                    return;
                }
            }
        }

        /// <summary>
        /// Given: Column of times in column 1 and some times in row 1,
        /// extend each column to match time span in column 1
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btFillRectangle_Click(object sender, EventArgs e)
        {
            try
            {
                if (gridTimes.Rows.Count < 2) return;  // not enough data entered

                var lastTime = ParseTimeRow(gridTimes.Rows[0]);
                var refTime = lastTime; // To record which of the header columns have time entries

                for (int row = 1; row < gridTimes.Rows.Count - 1; row++)
                {

                    var gridRow = gridTimes.Rows[row];
                    if (gridRow.Cells[0].Value == null) continue;
                    var curTime = DateTime.Parse(gridRow.Cells[0].Value.ToString());
                    var deltaTime = curTime - lastTime[0];
                    lastTime[0] = curTime;

                    // Increment other column times by the same amount
                    for (int col = 1; col < gridTimes.Columns.Count; col++)
                    {
                        if (refTime[col] != DateTime.MaxValue)
                        {
                            curTime = lastTime[col] + deltaTime;
                            lastTime[col] = curTime;
                            gridRow.Cells[col].Value = curTime.ToString("HH:mm:ss");
                        }

                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message + ex.StackTrace);
            }
        }

        // Parse and return array of all times in a row
        // Blank values are set to DateTime.MaxValue
        private List<DateTime> ParseTimeRow(DataGridViewRow timeRow)
        {
            var timeValue = new List<DateTime>();
            for (int col = 0; col < timeRow.Cells.Count; col++)
            {
                var strTime="";
                var cellValue = timeRow.Cells[col].Value;
                if (cellValue != null) strTime = cellValue.ToString();
                if (strTime != "")
                {
                    DateTime cellTime;
                    if (DateTime.TryParse(strTime, out cellTime))
                    {
                        timeValue.Add(cellTime);
                    }
                    else
                    {
                        timeRow.ErrorText = "Invalid time: " + strTime;
                        return null;
                    }
                }
                else
                {
                    timeValue.Add(DateTime.MaxValue);
                }

            }

            return timeValue;
        }

        // Called when the focus leaves current cell - check any entry for a valid time
        private void gridTimes_CellValidating(object sender, DataGridViewCellValidatingEventArgs e)
        {
            if ((e.RowIndex < 0) || (e.RowIndex >= gridTimes.Rows.Count-1) || (e.ColumnIndex < 0))
            {
                // Not on an editable cell
                return;
            }

            var strTime = "";
            var timeRow = gridTimes.Rows[e.RowIndex];
            var cellValue = e.FormattedValue.ToString();
            if (cellValue != null) strTime = cellValue;
            if (strTime != "")
            {
                DateTime cellTime;
                if (!DateTime.TryParse(strTime, out cellTime))
                {
                    timeRow.ErrorText = "Invalid time: " + strTime;
                }
                else
                {
                    gridTimes.EndEdit();
                    timeRow.ErrorText = "";
                }
            }

        }


        /// <summary>
        /// Allow pasting from Excel into grid, creating rows as needed
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void gridTimes_KeyDown(object sender, KeyEventArgs e)
        {
            try
            {

                if (e.Control && (e.KeyCode == Keys.V))
                {

                    int ridx = gridTimes.SelectedCells[0].RowIndex;
                    int cidx = gridTimes.SelectedCells[0].ColumnIndex;

                    if ((ridx >= 0) && (cidx >= 0) )
                    {
                        char[] lineDelim = { '\r', '\n' };
                        char[] colDelim = { '\t' };

                        IDataObject d = Clipboard.GetDataObject();
                        string s = (string)d.GetData(DataFormats.Text);
                        string[] lines = s.Split(lineDelim, StringSplitOptions.RemoveEmptyEntries);

                        int curRow = ridx;  // Start at upper left selected cell
                        // Loop through the lines and split those out  placing the values in the corresponding cell.
                        foreach (string line in lines)
                        {
                            if (curRow >= gridTimes.Rows.Count-1)
                            {
                                gridTimes.Rows.Add();
                            }
                            string[] CellVals = line.Split(colDelim);
                            int curCol = cidx;
                            foreach (string cellVal in CellVals)
                            {
                                gridTimes.Rows[curRow].Cells[curCol].Value = cellVal;
                                curCol++;
                            }
                            curRow++;
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message + ex.StackTrace);
            }
        }

        private const string TripFilename = "trips.txt";
        private const string StopsFilename = "stop_times.txt";

        private void WriteTrips()
        {
            TimeScheduleForm.BackupFile(gtfsPath, TripFilename); 
            string fileSpec = Path.Combine(gtfsPath, TripFilename);

            using (StreamWriter sw = new StreamWriter(fileSpec))
            {
                sw.WriteLine("route_id,service_id,trip_id,shape_id"); // ,trip_bikes_allowed");

                int tripID = 1;
                foreach (Relation routeMaster in routeMasters.Values)
                {
                    if (routeMaster.TripList != null)
                    {
                        foreach (Trip trip in routeMaster.TripList)
                        {
                            sw.WriteLine(string.Format("{0},{1},{2},{3}", 
                                routeMaster.RouteID, trip.ServiceId,tripID, routeMaster.ShapeID));
                            trip.TripID = tripID;
                            tripID++;
                        }
                    }
                }

                sw.Close();
            }

        }

        private void ReadTripsAndStopTimes()
        {

            string fileSpec = Path.Combine(gtfsPath, TripFilename);
            if (!File.Exists(fileSpec)) return;  // No file has been created yet

            // Create list of routes accessible by route ID
            var routeLookups = new Dictionary<string, Relation>();
            foreach (Relation routeMaster in routeMasters.Values)
            {
                routeLookups.Add(routeMaster.TagList["ref"], routeMaster);
            }

            var tripLookups = new Dictionary<string, Trip>();

            using (StreamReader sr = new StreamReader(fileSpec))
            {
                var line = sr.ReadLine(); // Discard header line

                // "route_id,service_id,trip_id,shape_id"

                while (!sr.EndOfStream)
                {
                    line = sr.ReadLine();
                    if (line.Length > 3) {
                        var fields = line.Split(',');
                        var routeMaster = routeLookups[fields[0]]; // route_id
                        if (routeMaster.TripList == null) routeMaster.TripList = new List<Trip>();

                        var trip = new Trip(fields[1]); // service_id
                        trip.StopIDList = new List<int>();
                        trip.StopTimeList = new List<DateTime>();
                        tripLookups.Add(fields[2], trip);  // Local lookup based on trip ID
                        routeMaster.TripList.Add(trip);
                    }
                }

                sr.Close();
            }


            // Read stops information while we have a route lookup table created

            fileSpec = Path.Combine(gtfsPath, StopsFilename);

            using (StreamReader sr = new StreamReader(fileSpec))
            {
                var line = sr.ReadLine(); // Discard header line

                // "trip_id,arrival_time,departure_time,stop_id,stop_sequence,shape_dist_traveled"

                while (!sr.EndOfStream)
                {
                    line = sr.ReadLine();
                    if (line.Length > 3)
                    {
                        var fields = line.Split(',');
                        var trip = tripLookups[fields[0]];  // trip_id
                        // Times may be blank
                        var strTime = fields[1];
                        var stopTime = DateTime.MaxValue;
                        if (strTime.Length > 0) stopTime = DateTime.Parse(strTime);
                        trip.StopTimeList.Add(stopTime); // *_time
                        trip.StopIDList.Add(Convert.ToInt32(fields[3])); // stop_id

                        // Note: Future shape_dist_traveled not loaded here

                    }
                }

                sr.Close();
            }



        }


        private void WriteStopTimes()
        {
            BackupFile(gtfsPath, StopsFilename);
            string fileSpec = Path.Combine(gtfsPath, StopsFilename);

            using (StreamWriter sw = new StreamWriter(fileSpec))
            {
                sw.WriteLine("trip_id,arrival_time,departure_time,stop_id,stop_sequence,shape_dist_traveled");

                int stopSequence = 10;
                foreach (Relation routeMaster in routeMasters.Values)
                {
                    if (routeMaster.TripList != null)
                    {
                        for (int i = 0; i < routeMaster.TripList.Count; i++)
                        {
                            var trip = routeMaster.TripList[i];

                            int stopDistanceIndex = 0;
                            for (int stop = 0; stop < trip.StopIDList.Count; stop++)
                            {
                                string strTime = "";
                                if (trip.StopTimeList[stop] != DateTime.MaxValue)
                                {
                                    strTime = trip.StopTimeList[stop].ToString("HH:mm:ss");
                                }

                                if (stopDistanceIndex >= routeMaster.StopDistances.Count)
                                {
                                    MessageBox.Show("Out of range debug");
                                }

                                if (trip.StopIDList[stop] < 0)
                                {
                                    MessageBox.Show("Uninitialized Stop debug");
                                }

                                double stopDistance = routeMaster.StopDistances[stopDistanceIndex];
                                stopDistanceIndex++;

                                sw.WriteLine(string.Format("{0},{1},{2},{3},{4},{5}",
                                    trip.TripID, strTime, strTime,
                                    trip.StopIDList[stop], stopSequence, stopDistance));
                                stopSequence += 10; // Possibly to allow stop insertion for some GTFS editors?
                            }

                        }
                    }
                }

                sw.Close();
            }

        }

        public static void BackupFile(string path, string filename)
        {
            string fileSpec = Path.Combine(path, filename);
            if (File.Exists(fileSpec))
            {
                var bakFolder = Path.Combine(path, "Backups");
                if (!Directory.Exists(bakFolder)) Directory.CreateDirectory(bakFolder);
                var bakFileSpec = Path.Combine(bakFolder, filename);
                var bakFile = bakFileSpec + DateTime.Now.ToString("_yyyyMMddHHmmss");
                Directory.Move(fileSpec, bakFile);
            }

        }


        private void btSave_Click(object sender, EventArgs e)
        {
            if (SaveSchedule())
            {
                try
                {
                    calendar.WriteCalendarFile();
                    WriteTrips();
                    WriteStopTimes();
                    MessageBox.Show("NOTE: Uploading GTFS schedules derived from OSM data to Google may violate the OSM license terms");
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Error while saving data: " + ex.Message + ex.StackTrace);
                }
            }
            else
            {
                MessageBox.Show("Formatting error - check left column for format error indicators");
            }
        }

    }
}
