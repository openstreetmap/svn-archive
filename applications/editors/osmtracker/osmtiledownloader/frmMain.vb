Imports System.Globalization
Imports System.xml
Imports System.IO
Imports System.Reflection
Imports System.Net

Public Class frmMain

    Private Const earthRadius As Double = 6378137
    'The radius of the earth - should never change!
    Private Const earthCircum As Double = earthRadius * 2.0 * Math.PI
    'calulated circumference of the earth
    Private Const earthHalfCirc As Double = earthCircum / 2
    'calulated half circumference of the earth

    Public Shared USACultureInfo As New CultureInfo("en-US")

    Private m_LastGPXfolder As String
    Private m_TileTable As New Hashtable
    Private m_Zoom As Long
    Private m_TileServerIndex As Long
    Private MinLat As Single, MaxLat As Single
    Private MinLon As Single, MaxLon As Single
    Private pTopLeft As Point
    Private pBottomRight As Point
    Private m_BoundingBoxCalculated As Boolean

    Public Sub New()

        ' This call is required by the Windows Form Designer.
        InitializeComponent()

        ' Add any initialization after the InitializeComponent() call.
        Me.Text = Application.ProductName & " v" & Application.ProductVersion

        LoadSettings()

        ' fill tile server list
        With cmbTileServer.Items
            .Add("http://dev.openstreetmap.org/~ojw/Tiles/tile.php/")
            .Add("http://tile.openstreetmap.org/mapnik/")
            .Add("http://tah.dev.openstreetmap.org/Tiles/tile/")
            .Add("http://dev.openstreetmap.org/~ojw/Tiles/cycle.php/")
            .Add("http://dev.openstreetmap.org/~ojw/Tiles/maplint.php/")
        End With
        If m_TileServerIndex < 1 And m_TileServerIndex > cmbTileServer.Items.Count Then m_TileServerIndex = 1
        cmbTileServer.SelectedIndex = m_TileServerIndex

        ' fill zoom combo
        With cmbZoom
            Dim i As Long
            For i = 13 To 17
                .Items.Add(i)
                If i = m_Zoom Then .SelectedIndex = (i - 13)
            Next
        End With

    End Sub

    Public Function GetAppPath() As String
        GetAppPath = Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly.Location()) 'Path.GetDirectoryName([Assembly].GetExecutingAssembly().GetModules(0).ToString)
    End Function

    Private Sub LoadSettings()
        Dim oConfig As New cConfig(GetAppPath() & "\config.xml")
        If Not oConfig Is Nothing Then
            txtStorageFolder.Text = oConfig.GetSettingDefault("StorageFolder", "c:\")
            m_LastGPXfolder = oConfig.GetSettingDefault("LastGPXfolder", "c:\")
            m_Zoom = oConfig.GetSettingDefault("LastZoom", "13")
            m_TileServerIndex = oConfig.GetSettingDefault("TileServerIndex", "1")
            txtMapFileName.Text = oConfig.GetSettingDefault("MapStorageFolder", "c:\")
        End If
    End Sub

    Private Sub StoreSettings()

        m_Zoom = cmbZoom.Text
        Dim oConfig As New cConfig(GetAppPath() & "\config.xml")
        If Not oConfig Is Nothing Then
            oConfig.SetSetting("StorageFolder", txtStorageFolder.Text)
            oConfig.SetSetting("LastGPXfolder", m_LastGPXfolder)
            oConfig.SetSetting("LastZoom", m_Zoom)
            oConfig.SetSetting("TileServerIndex", m_TileServerIndex)
            oConfig.SetSetting("MapStorageFolder", txtMapFileName.Text)
        End If
    End Sub

    Private Sub btnSelectStorageFolder_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnSelectStorageFolder.Click
        With FolderBrowserDialog1
            .SelectedPath = txtStorageFolder.Text
            If .ShowDialog = Windows.Forms.DialogResult.OK Then
                txtStorageFolder.Text = .SelectedPath
            End If
        End With
    End Sub

    Private Sub butLoadGPX_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnLoadGPX.Click
        With OpenFileDialog1
            .InitialDirectory = m_LastGPXfolder
            .FileName = ""
            .Filter = "GPX files (*.gpx)|*.gpx|All files (*.*)|*.*"
            If .ShowDialog = Windows.Forms.DialogResult.OK Then
                lblGPXfilename.Text = .FileName
                m_LastGPXfolder = Path.GetDirectoryName(.FileName)
                btnDownload.Enabled = True
                m_BoundingBoxCalculated = False
                Analyze()
            End If
        End With
        UpdateGUI()
    End Sub

    Private Sub frmMain_FormClosing(ByVal sender As Object, ByVal e As System.Windows.Forms.FormClosingEventArgs) Handles Me.FormClosing
        StoreSettings()
    End Sub

    Private Sub Analyze()

        m_TileTable.Clear()

        ' process GPX
        Dim oGPX As New XmlDocument()
        If File.Exists(lblGPXfilename.Text) Then
            oGPX.Load(lblGPXfilename.Text)
            Dim nsmgr As New Xml.XmlNamespaceManager(oGPX.NameTable)

            nsmgr.AddNamespace("gpx", "http://www.topografix.com/GPX/1/0")
            nsmgr.AddNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
            nsmgr.AddNamespace("xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd")

            Dim oTrack As XmlNode
            Dim oTrkPnt As XmlNode
            Dim oTrkSeg As XmlNode
            Dim Lat As Single, Lon As Single
            Dim BoundingBoxMethod As Boolean = rbutBoundingBox.Checked
            Dim zoom As Long = Long.Parse(cmbZoom.Text)
            Dim p As Point

            MaxLat = -180 : MinLat = 180
            MaxLon = -180 : MinLon = 180

            oTrack = oGPX.SelectSingleNode("//gpx:trk", nsmgr)
            If oTrack IsNot Nothing Then
                For Each oTrkSeg In oTrack.SelectNodes("./gpx:trkseg", nsmgr)
                    For Each oTrkPnt In oTrkSeg.SelectNodes("./gpx:trkpt", nsmgr)

                        Lat = Single.Parse(oTrkPnt.Attributes("lat").InnerText, USACultureInfo)
                        Lon = Single.Parse(oTrkPnt.Attributes("lon").InnerText, USACultureInfo)
                        If Lat > MaxLat Then MaxLat = Lat
                        If Lat < MinLat Then MinLat = Lat
                        If Lon > MaxLon Then MaxLon = Lon
                        If Lon < MinLon Then MinLon = Lon

                        p = CalcTileXY(Lat, Lon, zoom)
                        'txtDebug.AppendText(p.ToString & vbCrLf)

                        If Not m_TileTable.Contains(p) Then
                            m_TileTable.Add(p, p)
                        End If
                    Next
                Next

                txtMinLat.Text = MinLat.ToString(USACultureInfo)
                txtMaxLat.Text = MaxLat.ToString(USACultureInfo)
                txtMinLon.Text = MinLon.ToString(USACultureInfo)
                txtMaxLon.Text = MaxLon.ToString(USACultureInfo)

                pTopLeft = CalcTileXY(MaxLat, MinLon, zoom)
                pBottomRight = CalcTileXY(MinLat, MaxLon, zoom)
                m_BoundingBoxCalculated = True

                If BoundingBoxMethod Then

                    Dim x As Long, y As Long
                    For x = pTopLeft.X To pBottomRight.X
                        For y = pTopLeft.Y To pBottomRight.Y
                            p = New Point(x, y)
                            If Not m_TileTable.Contains(p) Then
                                m_TileTable.Add(p, p)
                            End If
                        Next
                    Next
                End If

                Dim w As Long = pBottomRight.X - pTopLeft.X + 1
                Dim h As Long = pBottomRight.Y - pTopLeft.Y + 1

                txtMinX.Text = pTopLeft.X.ToString(USACultureInfo)
                txtMinY.Text = pTopLeft.Y.ToString(USACultureInfo)
                txtMaxX.Text = pBottomRight.X.ToString(USACultureInfo)
                txtMaxY.Text = pBottomRight.Y.ToString(USACultureInfo)

                lblSize.Text = "tiles (" & w & "," & h & ") img (" & w * 256 & "x" & h * 256 & ") pixels"

            Else
                MsgBox("Not track(s) in GPX file", MsgBoxStyle.Exclamation)
            End If
        End If

    End Sub

    Private Sub btnDownload_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnDownload.Click

        btnDownload.Enabled = False

        With DownloadProgressBar
            .Value = 0
            .Minimum = 0
            .Maximum = m_TileTable.Count
        End With

        Dim strFileName As String
        Dim strUrl As String
        Dim strFolder As String
        Dim forcedownload As Boolean = chkForceNewtiles.Checked
        Dim TileEnum As IDictionaryEnumerator = m_TileTable.GetEnumerator
        Dim TileNum As Long = 0
        Dim p As Point
        Dim zoom As Long = Long.Parse(cmbZoom.Text)

        While (TileEnum.MoveNext())

            lblDownloadStatus.Text = "Downloading tile " & TileNum + 1 & " of " & m_TileTable.Count

            p = TileEnum.Value
            strFolder = txtStorageFolder.Text & "\" & zoom & "\" & p.X & "\"
            strFileName = strFolder & p.Y & ".png"
            strUrl = cmbTileServer.Text & zoom & "/" & p.X & "/" & p.Y & ".png"
            'Console.WriteLine(strUrl)

            If Not Directory.Exists(strFolder) Then
                Dim di As DirectoryInfo = Directory.CreateDirectory(strFolder)
                If Not di.Exists Then MsgBox("Could not create folder : " & strFolder, MsgBoxStyle.Exclamation)
            End If

            If Directory.Exists(strFolder) Then
                If Not File.Exists(strFileName) Or forcedownload Then
                    DownloadFile(strUrl, strFileName)
                End If
            End If

            TileNum += 1
            DownloadProgressBar.Value = TileNum
            Application.DoEvents()

        End While

        MsgBox("Tile download done", MsgBoxStyle.Information)

        UpdateGUI()
        lblDownloadStatus.Text = "Done"
        btnDownload.Enabled = True

    End Sub

    Private Function DegToRad(ByVal d As Double) As Double
        Return d * Math.PI / 180.0
    End Function

    Private Function ProjectF(ByVal lat As Single) As Single
        lat = DegToRad(lat)
        ProjectF = Math.Log(Math.Tan(lat) + (1 / Math.Cos(lat)))
    End Function

    Private Function ProjectMercToLat(ByVal v As Single) As Single
        ProjectMercToLat = 180 / Math.PI * Math.Atan(Math.Sinh(v))
    End Function

    Private Function MetersPerPixel(ByVal zoom As Integer) As Double
        MetersPerPixel = earthCircum / ((1 << zoom) * 256)
    End Function

    'helper function - converts a latitude at a certain zoom into a y pixel
    Private Function LatitudeToYAtZoom(ByVal lat As Double, ByVal zoom As Integer) As Integer
        Dim arc As Double = earthCircum / ((1 << zoom) * 256)
        Dim sinLat As Double = Math.Sin(DegToRad(lat))
        Dim metersY As Double = earthRadius / 2 * Math.Log((1 + sinLat) / (1 - sinLat))
        LatitudeToYAtZoom = CInt(Math.Round((earthHalfCirc - metersY) / arc))
    End Function

    'helper function - converts a longitude at a certain zoom into a x pixel
    Private Function LongitudeToXAtZoom(ByVal lon As Double, ByVal zoom As Integer) As Integer
        Dim arc As Double = earthCircum / ((1 << zoom) * 256)
        Dim metersX As Double = earthRadius * DegToRad(lon)
        LongitudeToXAtZoom = CInt(Math.Round((earthHalfCirc + metersX) / arc))
    End Function

    Private Sub CalcLatLonFromTileXY(ByVal p As Point, ByVal zoom As Long, ByRef p1 As PointF, ByRef p2 As PointF)  ' lon,lat
        Dim unit As Single = 1 / (2 ^ zoom)
        Dim relY1 = p.Y * unit
        Dim relY2 = relY1 + unit
        Dim LimitY As Single = ProjectF(85.0511)
        Dim RangeY As Single = 2 * LimitY
        relY1 = LimitY - RangeY * relY1
        relY2 = LimitY - RangeY * relY2
        Dim Lat1 As Single = ProjectMercToLat(relY1)
        Dim Lat2 As Single = ProjectMercToLat(relY2)
        unit = 360 / (2 ^ zoom)
        Dim Lon1 = -180 + p.X * unit
        Dim Lon2 = Lon1 + unit
        'Lat2, Long1, Lat1, Long1 + $Unit)); # S,W,N,E
        p1.Y = Lat1 ' top,left
        p1.X = Lon1
        p2.Y = Lat2 ' bottom, right
        p2.X = Lon2
    End Sub

    Private Function CalcTileXY(ByVal lat As Single, ByVal lon As Single, ByVal zoom As Long) As Point

        ' http://dev.openstreetmap.org/~ojw/Tiles/tile.php/14/8452/5496.png

        Dim xf As Single = (lon + 180) / 360 * 2 ^ zoom
        Dim yf As Single = (1 - Math.Log(Math.Tan(lat * Math.PI / 180) + 1 / Math.Cos(lat * Math.PI / 180)) / Math.PI) / 2 * 2 ^ zoom
        CalcTileXY.X = CLng(Math.Floor(xf))
        CalcTileXY.Y = CLng(Math.Floor(yf))

    End Function

    Private Function DownloadFile(ByVal strUrl As String, ByVal strFileName As String) As Boolean

        Dim request As WebRequest
        Dim response As WebResponse
        Dim input As IO.Stream
        Dim output As IO.FileStream
        Dim count As Long = 128 * 1024 ' 128k at a time
        Dim buffer(count - 1) As Byte

        Try
            request = WebRequest.Create(strUrl)
            If request IsNot Nothing Then
                response = request.GetResponse()
                If response IsNot Nothing Then
                    input = response.GetResponseStream()
                    If input IsNot Nothing Then
                        If response.ContentLength > 0 Then
                            output = New IO.FileStream(strFileName, IO.FileMode.Create)
                            If output IsNot Nothing Then
                                Do
                                    count = input.Read(buffer, 0, count)
                                    If count = 0 Then Exit Do ' ready
                                    output.Write(buffer, 0, count)
                                Loop
                            End If
                            DownloadFile = True
                        End If
                    End If
                End If
            End If

        Catch ex As Exception
            ' MsgBox("error in DownloadFile (" & strUrl & ") reason : " & ex.ToString, MsgBoxStyle.Critical)
        End Try
        If input IsNot Nothing Then input.Close()
        If output IsNot Nothing Then output.Close()
        If response IsNot Nothing Then response.Close()

    End Function

    Private Sub btnBrowse_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnBrowse.Click

        With SaveFileDialog1
            .InitialDirectory = txtMapFileName.Text
            .Filter = "PNG image file (*.png)|*.png"
            If .ShowDialog = Windows.Forms.DialogResult.OK Then
                txtMapFileName.Text = .FileName
            End If
        End With
    End Sub

    Private Sub btnCreate_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnCreate.Click

        btnCreate.Enabled = False
        Dim mapsize As Size = CreatePNG(txtMapFileName.Text)

        Select Case cmbCalibrationFile.SelectedIndex
            Case 1 ' ozi
                Dim strOziMapFileName As String = txtMapFileName.Text & ".map"
                CreateOziMapFile(strOziMapFileName, txtMapFileName.Text, mapsize)
            Case 2 ' georeference
                Dim strGeoRefFileName As String = txtMapFileName.Text & ".pgw"
                CreateGeoReferenceFile(strGeoRefFileName, mapsize)
        End Select
        

        MsgBox("Done")
        btnCreate.Enabled = True

    End Sub

    Private Function CreatePNG(ByVal strFileName As String) As Size

        ' determine size
        Dim w As Long = pBottomRight.X - pTopLeft.X + 1
        Dim h As Long = pBottomRight.Y - pTopLeft.Y + 1

        With pbMapCreation
            .Minimum = 0
            .Maximum = w * h
            .Value = 0
        End With

        If w > 0 And h > 0 Then

            Dim mapsize As Size = New Size(256 * w, 256 * h)
            Dim map As New Bitmap(256 * w, 256 * h, Imaging.PixelFormat.Format24bppRgb)
            If map IsNot Nothing Then
                Dim g As Graphics = Graphics.FromImage(map)

                Dim TileEnum As IDictionaryEnumerator = m_TileTable.GetEnumerator
                Dim tilepoint As Point ' tile x y coords
                Dim offset As Point
                Dim TileImage As Bitmap
                Dim strTileFileName As String
                Dim x As Long, y As Long
                Dim i As Long

                g.Clear(Color.White) ' white background

                ' paste tiles in big map
                While (TileEnum.MoveNext())
                    tilepoint = TileEnum.Value
                    strTileFileName = txtStorageFolder.Text & "\" & m_Zoom & "\" & tilepoint.X & "\" & tilepoint.Y & ".png"
                    If File.Exists(strTileFileName) Then
                        TileImage = New Bitmap(strTileFileName)
                        If TileImage IsNot Nothing Then
                            x = (tilepoint.X - pTopLeft.X)
                            y = (tilepoint.Y - pTopLeft.Y)
                            Console.WriteLine(tilepoint.ToString & " -> " & x & "-" & y)
                            offset.X = x * 256
                            offset.Y = y * 256
                            g.DrawImage(TileImage, offset)
                            'g.DrawString(tilepoint.ToString, New Font("Arial", 12, FontStyle.Bold), New SolidBrush(Color.Black), offset)
                        End If
                    End If
                    i += 1
                    pbMapCreation.Value = i
                End While
            End If

            ' store map
            Try
                map.Save(strFileName)
                CreatePNG = mapsize
            Catch ex As Exception
                MsgBox("Error saving file", MsgBoxStyle.Exclamation)
            End Try

        End If

    End Function

    Private Function CreateOziMapFile(ByVal strFileName As String, ByVal strImageFileName As String, ByVal mapsize As Size) As Boolean

        ' Format : http://www.rus-roads.ru/gps/help_ozi/map_file_format.html

        Dim pTileTopLeft As PointF, pTileTopLeft2 As PointF
        Dim pTileBottomRight As PointF
        Dim pDummy As PointF
        CalcLatLonFromTileXY(pTopLeft, m_Zoom, pTileTopLeft, pTileTopLeft2)
        CalcLatLonFromTileXY(pBottomRight, m_Zoom, pDummy, pTileBottomRight)

        Dim strWidth As String = mapsize.Width.ToString(USACultureInfo)
        Dim strHeight As String = mapsize.Height.ToString(USACultureInfo)
        Dim strWidth2 As String = (mapsize.Width / 2).ToString(USACultureInfo)
        Dim strHeight2 As String = (mapsize.Height / 2).ToString(USACultureInfo)
        Dim strPixelsPerMeter As String = MetersPerPixel(m_Zoom).ToString(USACultureInfo)

        Dim ts As New StreamWriter(strFileName)
        If ts IsNot Nothing Then
            ts.WriteLine("OziExplorer Map Data File Version 2.2")
            ts.WriteLine(strImageFileName) ' description
            ts.WriteLine(strImageFileName) ' filename
            ts.WriteLine("1 ,Map Code,")
            ts.WriteLine("WGS 84,,   0.0000,   0.0000,WGS 84")
            ts.WriteLine("Reserved(1)")
            ts.WriteLine("Reserved(2)")
            ts.WriteLine("Magnetic(Variation, , , W)")
            'Map Projection,Mercator,PolyCal,No,AutoCalOnly,No,BSBUseWPX,No
            ts.WriteLine("Map(Projection, Mercator, PolyCal, No, AutoCalOnly, No, BSBUseWPX, No)")
            ts.WriteLine("Point01,xy,     0,     0,in, deg, " & pTileTopLeft.Y.ToString(USACultureInfo) & ",0.0,N," & pTileTopLeft.X.ToString(USACultureInfo) & ",0.0,E, grid,,,,N")
            ts.WriteLine("Point02,xy,     " & strWidth & "," & strHeight & ",in, deg," & pTileBottomRight.Y.ToString(USACultureInfo) & ",0.0,N," & pTileBottomRight.X.ToString(USACultureInfo) & ",0.0,E, grid,,,,N")
            ts.WriteLine("Point03,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point04,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point05,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point06,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point07,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point08,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point09,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point10,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point11,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point12,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point13,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point14,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point15,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point16,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point17,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point18,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point19,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point20,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point21,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point22,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point23,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point24,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point25,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point26,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point27,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point28,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point29,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Point30,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N")
            ts.WriteLine("Projection Setup,,,,,,,,,,")
            ts.WriteLine("Map Feature = MF ; Map Comment = MC     These follow if they exist")
            ts.WriteLine("Track File = TF      These follow if they exist")
            ts.WriteLine("Moving Map Parameters = MM?    These follow if they exist")
            ts.WriteLine("MM0, Yes")
            ts.WriteLine("MMPNUM, 4")
            ts.WriteLine("MMPXY, 1, 0, 0") ' top left
            ts.WriteLine("MMPXY, 2, " & strWidth & ", 0") ' top right
            ts.WriteLine("MMPXY, 3, " & strWidth & "," & strHeight) ' bottom right
            ts.WriteLine("MMPXY, 4, 0, " & strHeight) ' bottom left
            ts.WriteLine("MMPLL, 1, " & pTileTopLeft.X.ToString(USACultureInfo) & ", " & pTileTopLeft.Y.ToString(USACultureInfo)) ' top left
            ts.WriteLine("MMPLL, 2, " & pTileBottomRight.X.ToString(USACultureInfo) & ", " & pTileTopLeft.Y.ToString(USACultureInfo)) ' top right
            ts.WriteLine("MMPLL, 3, " & pTileBottomRight.X.ToString(USACultureInfo) & ", " & pTileBottomRight.Y.ToString(USACultureInfo)) ' bottom right
            ts.WriteLine("MMPLL, 4, " & pTileTopLeft.X.ToString(USACultureInfo) & ", " & pTileBottomRight.Y.ToString(USACultureInfo)) ' bottom left
            ' my $MM1B = 36000/360*1000*($N-$S)/$Width  ; 
            ts.WriteLine("MM1B, " & strPixelsPerMeter)
            'LL Grid Setup
            'LLGRID,No,No Grid,Yes,255,16711680,0,No Labels,0,16777215,7,1,Yes,x
            'Other Grid Setup
            'GRGRID,No,No Grid,Yes,255,16711680,No Labels,0,16777215,8,1,Yes,No,No,x
            ts.WriteLine("MOP,Map Open Position," & strWidth2 & "," & strHeight2)
            ts.WriteLine("IWH,Map Image Width/Height," & strWidth & "," & strHeight)
            ts.Close()
            CreateOziMapFile = True
        End If
    End Function

    Private Function CreateGeoReferenceFile(ByVal strFileName As String, ByVal mapsize As Size) As Boolean

        ' format : http://www.cadforum.cz/cadforum_en/qaID.asp?tip=3515
        ' and thanks to Bostjan for the idea
        Dim PixelsPerMeter As Double = MetersPerPixel(m_Zoom)
        Dim strPixelsPerMeterX As String = PixelsPerMeter.ToString(USACultureInfo)
        Dim strPixelsPerMeterY As String = (PixelsPerMeter * -1).ToString(USACultureInfo)
        Dim pTileTopLeft As PointF, pTileTopLeft2 As PointF
        CalcLatLonFromTileXY(pTopLeft, m_Zoom, pTileTopLeft, pTileTopLeft2)
        Dim ts As New StreamWriter(strFileName)
        If ts IsNot Nothing Then
            ts.WriteLine(strPixelsPerMeterX) ' size o pixel in x direction (must be the same as line 4 - this means that pixel is square) 
            ts.WriteLine("0.00000000000000000") ' rotation - must be 0
            ts.WriteLine("0.00000000000000000") ' rotation - must be 0
            ts.WriteLine(strPixelsPerMeterY) ' size o pixel in y direction (must be the same as line 1, but negative)
            ts.WriteLine(pTileTopLeft.X.ToString(USACultureInfo)) ' position of pixel - longitude (values from -180 to 180)
            ts.WriteLine(pTileTopLeft.Y.ToString(USACultureInfo)) ' position of pixel - latitude (values from -90 to 90)
            ts.Close()
            CreateGeoReferenceFile = True
        End If
    End Function

    Private Sub UpdateGUI()
        gbSetup.Enabled = lblGPXfilename.Text.Length > 0
        gbDownload.Enabled = lblGPXfilename.Text.Length > 0
        gbMapCreator.Enabled = m_BoundingBoxCalculated
    End Sub

    Private Sub cmbZoom_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles cmbZoom.SelectedIndexChanged
        m_Zoom = cmbZoom.Text
        Analyze()
    End Sub

    Private Sub cmbTileServer_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles cmbTileServer.SelectedIndexChanged
        m_TileServerIndex = cmbTileServer.SelectedIndex
    End Sub

    Private Sub rbutBoundingBox_CheckedChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles rbutBoundingBox.CheckedChanged
        Analyze()
    End Sub

    Private Sub rbutOnlyTrack_CheckedChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles rbutOnlyTrack.CheckedChanged
        Analyze()
    End Sub

    Private Sub txtUrlBox_TextChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles txtUrlBox.TextChanged

        Dim strContents As String = txtUrlBox.Text.ToLower
        Dim lat As Single
        Dim lon As Single
        Dim zoom As Integer

        Dim LatStartIndex As Integer = strContents.IndexOf("lat=")
        If LatStartIndex > -1 Then
            Dim LatEndIndex As Integer = strContents.IndexOf("&", LatStartIndex)
            If LatEndIndex > -1 Then
                Dim strLat As String = strContents.Substring(LatStartIndex + 4, LatEndIndex - (LatStartIndex + 4))
                lat = Single.Parse(strLat, USACultureInfo)
            End If
        End If
        Dim LonStartIndex As Integer = strContents.IndexOf("lon=")
        If LonStartIndex > -1 Then
            Dim LonEndIndex As Integer = strContents.IndexOf("&", LonStartIndex)
            If LonEndIndex > -1 Then
                Dim strLon = strContents.Substring(LonStartIndex + 4, LonEndIndex - (LonStartIndex + 4))
                lon = Single.Parse(strLon, USACultureInfo)
            End If
        End If
        Dim ZoomStartIndex As Integer = strContents.IndexOf("zoom=")
        If ZoomStartIndex > -1 Then
            Dim ZoomEndIndex As Integer = strContents.IndexOf("&", ZoomStartIndex)
            If ZoomEndIndex > -1 Then
                Dim strzoom As String = strContents.Substring(ZoomStartIndex + 5, ZoomEndIndex - (ZoomStartIndex + 5))
                zoom = Single.Parse(strZoom, USACultureInfo)
            End If
        End If

        txtCntrLat.Text = lat.ToString(USACultureInfo)
        txtCntrLon.Text = lon.ToString(USACultureInfo)

        btnDownload.Enabled = True
        m_BoundingBoxCalculated = False

    End Sub

    Private Sub NumericUpDown1_ValueChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles NumericUpDown1.ValueChanged

    End Sub
End Class
