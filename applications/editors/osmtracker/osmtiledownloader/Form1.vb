Imports System.Globalization
Imports System.xml
Imports System.IO
Imports System.Reflection
Imports System.Net

Public Class frmMain

    Public Shared USACultureInfo As New CultureInfo("en-US")

    Private m_LastGPXfolder As String
    Private m_TileTable As New Hashtable
    Private m_LastZoom As Long

    Public Sub New()

        ' This call is required by the Windows Form Designer.
        InitializeComponent()

        ' Add any initialization after the InitializeComponent() call.
        LoadSettings()

        ' fill tile server list
        With cmbTileServer.Items
            .Add("http://dev.openstreetmap.org/~ojw/Tiles/tile.php/")
            .Add("http://tile.openstreetmap.org/mapnik/")
            .Add("http://tah.dev.openstreetmap.org/Tiles/tile/")
            .Add("http://dev.openstreetmap.org/~ojw/Tiles/cycle.php/")
            .Add("http://dev.openstreetmap.org/~ojw/Tiles/maplint.php/")
        End With
        cmbTileServer.SelectedIndex = 1

        ' fill zoom combo
        With cmbZoom
            Dim i As Long
            For i = 13 To 17
                .Items.Add(i)
                If i = m_LastZoom Then .SelectedIndex = i - 13
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
            m_LastZoom = oConfig.GetSettingDefault("LastZoom", "13")
        End If
    End Sub

    Private Sub StoreSettings()

        m_LastZoom = cmbZoom.Text

        Dim oConfig As New cConfig(GetAppPath() & "\config.xml")
        If Not oConfig Is Nothing Then
            oConfig.SetSetting("StorageFolder", txtStorageFolder.Text)
            oConfig.SetSetting("LastGPXfolder", m_LastGPXfolder)
            oConfig.SetSetting("LastZoom", m_LastZoom)
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
            End If
        End With
    End Sub

    Private Sub frmMain_FormClosing(ByVal sender As Object, ByVal e As System.Windows.Forms.FormClosingEventArgs) Handles Me.FormClosing
        StoreSettings()
    End Sub

    Private Sub btnDownload_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnDownload.Click

        btnDownload.Enabled = False

        lblDownloadStatus.Text = "Analyzing track(s)..."
        Application.DoEvents()

        With DownloadProgressBar
            .Value = 0
            .Minimum = 0
        End With

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
            Dim MinLat As Single = 180, MaxLat As Single = -180
            Dim MinLon As Single = 180, MaxLon As Single = -180
            Dim BoundingBoxMethod As Boolean = rbutBoundingBox.Checked
            Dim zoom As Long = Long.Parse(cmbZoom.Text)
            Dim p As Point

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

                If BoundingBoxMethod Then
                    lblDownloadStatus.Text = "Calculating bouding box..."
                    Application.DoEvents()

                    Dim pLeftTop As Point = CalcTileXY(MinLat, MinLon, zoom)
                    Dim pRightBottom As Point = CalcTileXY(MaxLat, MaxLon, zoom)

                    Dim x As Long, y As Long
                    For x = pLeftTop.X To pRightBottom.X
                        For y = pRightBottom.Y To pLeftTop.Y
                            p = New Point(x, y)
                            If Not m_TileTable.Contains(p) Then
                                m_TileTable.Add(p, p)
                            End If
                        Next
                    Next
                End If

                DownloadProgressBar.Maximum = m_TileTable.Count

                Dim strFileName As String
                Dim strUrl As String
                Dim strFolder As String
                Dim forcedownload As Boolean = chkForceNewtiles.Checked
                Dim TileEnum As IDictionaryEnumerator = m_TileTable.GetEnumerator
                Dim TileNum As Long = 0

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
            Else
                MsgBox("Not track(s) in GPX file", MsgBoxStyle.Exclamation)
            End If
        End If

        lblDownloadStatus.Text = "Done"
        btnDownload.Enabled = True

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

        Catch ex As Exception
            ' MsgBox("error in DownloadFile (" & strUrl & ") reason : " & ex.ToString, MsgBoxStyle.Critical)
        End Try
        If input IsNot Nothing Then input.Close()
        If output IsNot Nothing Then output.Close()
        If response IsNot Nothing Then response.Close()

    End Function

End Class
