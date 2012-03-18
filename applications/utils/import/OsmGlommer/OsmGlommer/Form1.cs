///
/// Glommer - Given an OpenStreetMap XML file, combine ways with identical tags
/// and save to a new OpenStreetMap XML file.
/// *
/// Author: Mike Nice November 2010
///  Josh Doe - Add 1-way handling  December 2011
///  MN add threading  March 2012
/// License: Open Source
///


using System;
using System.Collections.Generic;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Xml;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.IO;
using System.Windows.Forms;
using System.Threading;

namespace OsmGlommer
{

    /// <summary>
    /// Process a .OSM XML file
    /// Connected ways of exactly the same name, identical attributes and end node, 
    /// will be linked at the common node and one of the ways will be deleted
    /// </summary>
    public partial class Form1 : Form
    {
        public delegate void LogMessageDelegate(string status, bool error);

        private List<LinkedWay> wayList;
        private Hashtable nodeHash;
        private XmlDocument osmXMLDoc;
        private XmlNode rootNode;

        bool terminating = false;
        private Thread glomThread = null;


        public Form1()
        {
            InitializeComponent();
            // openFileDialog1.InitialDirectory = @"C:\mike\Projects\Openstreetmap";
        }







        private void LoadOSMFile(string xmlFilename)
        {
            wayList = new List<LinkedWay>();

            osmXMLDoc = new XmlDocument();
            osmXMLDoc.Load(xmlFilename);

            rootNode = osmXMLDoc.SelectSingleNode("/osm");

            XmlNodeList xmlNodes = osmXMLDoc.SelectNodes("/osm/way");
            foreach (XmlNode xmlNode in xmlNodes)
            {
                Int64 wayID = Convert.ToInt64(xmlNode.Attributes["id"].InnerText);

                var wayNodeList = new List<Int64>();
                var osmNodeIDs = xmlNode.SelectNodes("nd");
                foreach(XmlNode osmNodeID in osmNodeIDs) {
                    wayNodeList.Add(Convert.ToInt64(osmNodeID.Attributes["ref"].InnerText));
                }

                if (wayNodeList.Count > 0)
                {
                    var tagList = new Dictionary<string, string>();
                    var osmTags = xmlNode.SelectNodes("tag");
                    foreach (XmlNode osmTag in osmTags)
                    {
                        tagList.Add(osmTag.Attributes["k"].InnerText, osmTag.Attributes["v"].InnerText);
                    }

                    var way = new LinkedWay(wayID, wayNodeList, xmlNode, tagList);
                    wayList.Add(way);
                }
            }
            
            LogMessage("Finished Reading: " + wayList.Count.ToString() + " ways", false);

            nodeHash = new Hashtable();
            xmlNodes = osmXMLDoc.SelectNodes("/osm/node");
            foreach (XmlNode xmlNode in xmlNodes)
            {
                Int64 nodeID = Convert.ToInt64(xmlNode.Attributes["id"].InnerText);
                nodeHash.Add(nodeID, new Coord(xmlNode.Attributes["lat"].InnerText, xmlNode.Attributes["lon"].InnerText));
            }
            LogMessage("Finished Reading: " + nodeHash.Count.ToString() + " nodes", false);
        }



        /// <summary>
        /// Check if 2 ways have identical tags
        /// </summary>
        /// <param name="way"></param>
        /// <param name="followWay"></param>
        /// <returns>true if identical</returns>
        private bool IdenticalTags(LinkedWay way, LinkedWay followWay) {
            bool isSame = false;

            if (way.TagList.Count == followWay.TagList.Count)
            {
                foreach (string tagName in way.TagList.Keys)
                {
                    string tagValue = way.TagList[tagName];
                    if (!followWay.TagList.ContainsKey(tagName) ||
                        !followWay.TagList[tagName].Equals(tagValue))
                    {
                        return false;
                    }
                }
                isSame = true;
            }

            return isSame;
        }

        private Coord GetCoord(Int64 nodeID)
        {
            return (Coord)nodeHash[nodeID];
            //XmlNode xmlNode = osmXMLDoc.SelectSingleNode("/osm/node[@id = '" + nodeID + "']");
            //return new Coord(xmlNode.Attributes["lat"].InnerText, xmlNode.Attributes["lon"].InnerText);
        }

        private double GetAngle(LinkedWay way, LinkedWay followWay)
        {
            Coord first = GetCoord(way.NodeList[way.NodeList.Count - 2]);
            Coord middle = GetCoord(way.LastNode);
            Coord last = GetCoord(followWay.NodeList[1]);

            double firstAngle = Math.Atan2(middle.Lat - first.Lat, middle.Lon - first.Lon) * 180/Math.PI;
            double secondAngle = Math.Atan2(last.Lat - middle.Lat, last.Lon - middle.Lon) * 180 / Math.PI;

            return firstAngle - secondAngle;
        }

        private bool GlomFound()
        {
            bool foundGlom = false;
            foreach (LinkedWay way in wayList)
            {

                if (way.WayChecked) continue;

                // Search for a way whose first node is the same as last node of current way
                foreach (LinkedWay followWay in wayList)
                {
                    if (way != followWay)
                    {
                        if (way.LastNode == followWay.FirstNode ||
                            ((way.FirstNode == followWay.FirstNode || way.LastNode == followWay.LastNode) &&
                                 !way.TagList.ContainsKey("oneway")))
                        {
                            if (IdenticalTags(way, followWay))
                            {
                                if (way.LastNode == followWay.LastNode)
                                {
                                    followWay.NodeList.Reverse();
                                }
                                else if (way.FirstNode == followWay.FirstNode)
                                {
                                    way.NodeList.Reverse();
                                }

                                // check if there's an acute angle between segments (possible dual carriageway)
                                if (Math.Abs(GetAngle(way, followWay)) > 90)
                                    continue;

                                // Glom ways
                                //
                                // Add nodes to current way
                                bool firstNode = true;
                                foreach (long newNode in followWay.NodeList)
                                {
                                    // First node of following way is last node of current way (already present)
                                    if (!firstNode)
                                    {
                                        way.NodeList.Add(newNode);
                                    }
                                    firstNode = false;
                                }
                                rootNode.RemoveChild(followWay.WayXMLNode);  // Remove way from XML document - combined way node list will be updated later
                                wayList.Remove(followWay); // Invalidate foreach loop, must exit
                                return true;
                            }

                        }
                    }
                }

                way.WayChecked = true;
            }

            return foundGlom;

        }


        private void LinkWays()
        {
            while (GlomFound())
            {
            }

        }



        /// <summary>
        /// Update node list in XML to match glommed node list of the remaining way
        /// </summary>
        /// <param name="way"></param>
        private void UpdateNodeList(LinkedWay way)
        {
            XmlNode xmlNode = way.WayXMLNode;

            var osmNodeIDs = xmlNode.SelectNodes("nd");
            // Remove all existing nodes
            foreach (XmlNode osmXMLNodeID in osmNodeIDs)
            {
                xmlNode.RemoveChild(osmXMLNodeID);
            }

            // Add new nodes
            foreach (long nodeID in way.NodeList)
            {
                var newElement = osmXMLDoc.CreateElement("nd");
                newElement.SetAttribute("ref", nodeID.ToString());
                xmlNode.AppendChild(newElement);
            }
        }


        private void UpdateOSMWays()
        {
            foreach (LinkedWay way in wayList)
            {
                UpdateNodeList(way);
            }

        }


        private void SaveOSM(string inFilename)
        {
            var outFilename = inFilename;
            var dir = Path.GetDirectoryName(outFilename);

            var baseFileName = Path.GetFileNameWithoutExtension(outFilename);
            var ext = Path.GetExtension(outFilename);
            var fileName = baseFileName + "_glommed" + ext;
            var saveFilePath = Path.Combine(dir, fileName);


            UpdateOSMWays();

            osmXMLDoc.Save(saveFilePath);

            LogMessage("Wrote " + wayList.Count + " OSM Ways to " + fileName, false);
        }


        private void GlomThread(object objOSMFilename)
        {
            try
            {
                string osmFilename = (string)objOSMFilename;
                LogMessage("Glom started for " + osmFilename, false);
                DateTime startTime = DateTime.Now;
                LoadOSMFile(osmFilename);
                LogMessage("Linking ways ... this may take some time", false);
                LinkWays();
                SaveOSM(osmFilename);

                DateTime endTime = DateTime.Now;
                var elapsed = endTime - startTime;
                LogMessage("Glom complete.  Elapsed time: " + elapsed.ToString(), false);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error during glom operation: " + ex.Message);
            }
            EndGlomThread();

        }

        private void EndGlomThread()
        {
            if (InvokeRequired)
            {
                Invoke(new MethodInvoker(EndGlomThread));
                return;
            }
            btConvert.Enabled = true;
        }



        private void btConvert_Click(object sender, EventArgs e)
        {
            try
            {
                string osmFilename = tbFilename.Text;
                glomThread = new Thread(GlomThread);
                glomThread.Name = "Glom Thread";
                glomThread.IsBackground = true;
                glomThread.Start(osmFilename);
                btConvert.Enabled = false;
                
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error starting glom operation: " + ex.Message);
                btConvert.Enabled = true;
            }

        }

        private void btBrowseFolder_Click(object sender, EventArgs e)
        {
            openFileDialog1.FileName = tbFilename.Text;
            DialogResult result = openFileDialog1.ShowDialog();
            openFileDialog1.Filter = "OSM Map Vector Files(*.osm;*.xml)|*.osm;*.xml|All files (*.*)|*.*";
            openFileDialog1.FilterIndex = 2;

            if (result == DialogResult.OK)
            {
                tbFilename.Text = openFileDialog1.FileName;
            }

        }



        public void LogMessage(string msg, bool errorStatus)
        {

            const int MaxLogLines = 1000;

            if (terminating)
            {
                return;
            }
            if (InvokeRequired)
            {

                object[] arglist = new object[2];
                arglist[0] = msg;
                arglist[1] = errorStatus;
                Invoke(new LogMessageDelegate(LogMessage), arglist);
                return;
            }


            if (richTextLogBox.Lines.GetLength(0) > MaxLogLines)
            {
                // Delete the first line and reposition cursor to end
                richTextLogBox.Select(0, richTextLogBox.Lines[0].Length + 1);
                richTextLogBox.SelectedText = "";
                richTextLogBox.Select(richTextLogBox.TextLength, 0);
            }

            msg = DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss") + ": " + msg + "\n";


            if (errorStatus)
            {

                richTextLogBox.SelectionColor = Color.White;
                richTextLogBox.SelectionBackColor = Color.Red;
            }
            else
            {
                richTextLogBox.SelectionColor = Color.Black;
                richTextLogBox.SelectionBackColor = Color.White;
            }

            richTextLogBox.AppendText(msg);
            richTextLogBox.ScrollToCaret();

            Application.DoEvents();

        }


    }

    public class Coord
    {
        public double Lat {get; set; }
        public double Lon { get; set; }
        public Coord(double lat, double lon)
        {
            Lat = lat;
            Lon = lon;
        }
        public Coord(string lat, string lon)
        {
            Lat = Convert.ToDouble(lat);
            Lon = Convert.ToDouble(lon);
        }
    }

    public class LinkedWay
    {
        public List<Int64> NodeList { get; set; }

        public Dictionary<string, string> TagList { get; set; }

        public LinkedWay NextWay { get; set; }
        public LinkedWay PreviousWay { get; set; }

        public Int64 FirstNode
        {
            get { return NodeList[0]; }
        }
        public Int64 LastNode
        {
            get { return NodeList.Last(); }
        }

        public bool WayChecked { get; set; }
        public Int64 WayID { get; set; }

        public XmlNode WayXMLNode { get; set; }

        public LinkedWay(Int64 wayID, List<Int64> nodeListToUse, XmlNode wayXMLNode, Dictionary<string, string> tagList)
        {
            NodeList = nodeListToUse;
            WayID = wayID;
            WayXMLNode = wayXMLNode;
            TagList = tagList;
            NextWay = null;
            PreviousWay = null;
            WayChecked = false;
        }

    }



}
