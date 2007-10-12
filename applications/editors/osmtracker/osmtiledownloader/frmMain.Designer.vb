<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class frmMain
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        If disposing AndAlso components IsNot Nothing Then
            components.Dispose()
        End If
        MyBase.Dispose(disposing)
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Dim resources As System.ComponentModel.ComponentResourceManager = New System.ComponentModel.ComponentResourceManager(GetType(frmMain))
        Me.gbSetup = New System.Windows.Forms.GroupBox
        Me.cmbZoom = New System.Windows.Forms.ComboBox
        Me.Label5 = New System.Windows.Forms.Label
        Me.Label3 = New System.Windows.Forms.Label
        Me.txtStorageFolder = New System.Windows.Forms.TextBox
        Me.btnSelectStorageFolder = New System.Windows.Forms.Button
        Me.Label1 = New System.Windows.Forms.Label
        Me.cmbTileServer = New System.Windows.Forms.ComboBox
        Me.Label2 = New System.Windows.Forms.Label
        Me.rbutBoundingBox = New System.Windows.Forms.RadioButton
        Me.rbutOnlyTrack = New System.Windows.Forms.RadioButton
        Me.OpenFileDialog1 = New System.Windows.Forms.OpenFileDialog
        Me.gbDownload = New System.Windows.Forms.GroupBox
        Me.chkForceNewtiles = New System.Windows.Forms.CheckBox
        Me.DownloadProgressBar = New System.Windows.Forms.ProgressBar
        Me.lblDownloadStatus = New System.Windows.Forms.Label
        Me.btnDownload = New System.Windows.Forms.Button
        Me.FolderBrowserDialog1 = New System.Windows.Forms.FolderBrowserDialog
        Me.gbMapCreator = New System.Windows.Forms.GroupBox
        Me.cmbCalibrationFile = New System.Windows.Forms.ComboBox
        Me.Label26 = New System.Windows.Forms.Label
        Me.pbMapCreation = New System.Windows.Forms.ProgressBar
        Me.btnBrowse = New System.Windows.Forms.Button
        Me.Label6 = New System.Windows.Forms.Label
        Me.txtMapFileName = New System.Windows.Forms.TextBox
        Me.btnCreate = New System.Windows.Forms.Button
        Me.SaveFileDialog1 = New System.Windows.Forms.SaveFileDialog
        Me.TabControl1 = New System.Windows.Forms.TabControl
        Me.TabPage1 = New System.Windows.Forms.TabPage
        Me.btnLoadGPX = New System.Windows.Forms.Button
        Me.ComboBox1 = New System.Windows.Forms.ComboBox
        Me.Label4 = New System.Windows.Forms.Label
        Me.lblGPXfilename = New System.Windows.Forms.Label
        Me.TabPage2 = New System.Windows.Forms.TabPage
        Me.NumericUpDown1 = New System.Windows.Forms.NumericUpDown
        Me.Label13 = New System.Windows.Forms.Label
        Me.Label12 = New System.Windows.Forms.Label
        Me.txtCntrLon = New System.Windows.Forms.TextBox
        Me.txtCntrLat = New System.Windows.Forms.TextBox
        Me.Label11 = New System.Windows.Forms.Label
        Me.txtUrlBox = New System.Windows.Forms.TextBox
        Me.Label10 = New System.Windows.Forms.Label
        Me.Label9 = New System.Windows.Forms.Label
        Me.GroupBox2 = New System.Windows.Forms.GroupBox
        Me.txtMaxY = New System.Windows.Forms.TextBox
        Me.txtMaxX = New System.Windows.Forms.TextBox
        Me.Label24 = New System.Windows.Forms.Label
        Me.Label25 = New System.Windows.Forms.Label
        Me.Label23 = New System.Windows.Forms.Label
        Me.txtMinY = New System.Windows.Forms.TextBox
        Me.Label22 = New System.Windows.Forms.Label
        Me.txtMinX = New System.Windows.Forms.TextBox
        Me.lblSize = New System.Windows.Forms.Label
        Me.Label20 = New System.Windows.Forms.Label
        Me.Label21 = New System.Windows.Forms.Label
        Me.Label8 = New System.Windows.Forms.Label
        Me.Label7 = New System.Windows.Forms.Label
        Me.txtMaxLon = New System.Windows.Forms.TextBox
        Me.txtMaxLat = New System.Windows.Forms.TextBox
        Me.txtMinLon = New System.Windows.Forms.TextBox
        Me.txtMinLat = New System.Windows.Forms.TextBox
        Me.Label14 = New System.Windows.Forms.Label
        Me.NumericUpDown2 = New System.Windows.Forms.NumericUpDown
        Me.Label15 = New System.Windows.Forms.Label
        Me.Label16 = New System.Windows.Forms.Label
        Me.TextBox1 = New System.Windows.Forms.TextBox
        Me.TextBox2 = New System.Windows.Forms.TextBox
        Me.Label17 = New System.Windows.Forms.Label
        Me.Label18 = New System.Windows.Forms.Label
        Me.TextBox3 = New System.Windows.Forms.TextBox
        Me.Label19 = New System.Windows.Forms.Label
        Me.gbSetup.SuspendLayout()
        Me.gbDownload.SuspendLayout()
        Me.gbMapCreator.SuspendLayout()
        Me.TabControl1.SuspendLayout()
        Me.TabPage1.SuspendLayout()
        Me.TabPage2.SuspendLayout()
        CType(Me.NumericUpDown1, System.ComponentModel.ISupportInitialize).BeginInit()
        Me.GroupBox2.SuspendLayout()
        CType(Me.NumericUpDown2, System.ComponentModel.ISupportInitialize).BeginInit()
        Me.SuspendLayout()
        '
        'gbSetup
        '
        Me.gbSetup.Controls.Add(Me.cmbZoom)
        Me.gbSetup.Controls.Add(Me.Label5)
        Me.gbSetup.Controls.Add(Me.Label3)
        Me.gbSetup.Controls.Add(Me.txtStorageFolder)
        Me.gbSetup.Controls.Add(Me.btnSelectStorageFolder)
        Me.gbSetup.Controls.Add(Me.Label1)
        Me.gbSetup.Controls.Add(Me.cmbTileServer)
        Me.gbSetup.Controls.Add(Me.Label2)
        Me.gbSetup.Controls.Add(Me.rbutBoundingBox)
        Me.gbSetup.Controls.Add(Me.rbutOnlyTrack)
        Me.gbSetup.Enabled = False
        Me.gbSetup.Location = New System.Drawing.Point(19, 179)
        Me.gbSetup.Name = "gbSetup"
        Me.gbSetup.Size = New System.Drawing.Size(498, 203)
        Me.gbSetup.TabIndex = 3
        Me.gbSetup.TabStop = False
        Me.gbSetup.Text = "Setup"
        '
        'cmbZoom
        '
        Me.cmbZoom.FormattingEnabled = True
        Me.cmbZoom.Location = New System.Drawing.Point(48, 85)
        Me.cmbZoom.Name = "cmbZoom"
        Me.cmbZoom.Size = New System.Drawing.Size(68, 21)
        Me.cmbZoom.TabIndex = 9
        '
        'Label5
        '
        Me.Label5.AutoSize = True
        Me.Label5.Location = New System.Drawing.Point(25, 64)
        Me.Label5.Name = "Label5"
        Me.Label5.Size = New System.Drawing.Size(34, 13)
        Me.Label5.TabIndex = 8
        Me.Label5.Text = "Zoom"
        '
        'Label3
        '
        Me.Label3.AutoSize = True
        Me.Label3.Location = New System.Drawing.Point(20, 149)
        Me.Label3.Name = "Label3"
        Me.Label3.Size = New System.Drawing.Size(91, 13)
        Me.Label3.TabIndex = 7
        Me.Label3.Text = "Tile storage folder"
        '
        'txtStorageFolder
        '
        Me.txtStorageFolder.Location = New System.Drawing.Point(47, 165)
        Me.txtStorageFolder.Name = "txtStorageFolder"
        Me.txtStorageFolder.Size = New System.Drawing.Size(286, 20)
        Me.txtStorageFolder.TabIndex = 6
        '
        'btnSelectStorageFolder
        '
        Me.btnSelectStorageFolder.Location = New System.Drawing.Point(339, 159)
        Me.btnSelectStorageFolder.Name = "btnSelectStorageFolder"
        Me.btnSelectStorageFolder.Size = New System.Drawing.Size(80, 30)
        Me.btnSelectStorageFolder.TabIndex = 5
        Me.btnSelectStorageFolder.Text = "Browse"
        Me.btnSelectStorageFolder.UseVisualStyleBackColor = True
        '
        'Label1
        '
        Me.Label1.AutoSize = True
        Me.Label1.Location = New System.Drawing.Point(19, 109)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(55, 13)
        Me.Label1.TabIndex = 4
        Me.Label1.Text = "TileServer"
        '
        'cmbTileServer
        '
        Me.cmbTileServer.FormattingEnabled = True
        Me.cmbTileServer.Location = New System.Drawing.Point(46, 125)
        Me.cmbTileServer.Name = "cmbTileServer"
        Me.cmbTileServer.Size = New System.Drawing.Size(286, 21)
        Me.cmbTileServer.TabIndex = 3
        '
        'Label2
        '
        Me.Label2.AutoSize = True
        Me.Label2.Location = New System.Drawing.Point(22, 21)
        Me.Label2.Name = "Label2"
        Me.Label2.Size = New System.Drawing.Size(29, 13)
        Me.Label2.TabIndex = 2
        Me.Label2.Text = "Area"
        '
        'rbutBoundingBox
        '
        Me.rbutBoundingBox.AutoSize = True
        Me.rbutBoundingBox.Checked = True
        Me.rbutBoundingBox.Location = New System.Drawing.Point(47, 37)
        Me.rbutBoundingBox.Name = "rbutBoundingBox"
        Me.rbutBoundingBox.Size = New System.Drawing.Size(87, 17)
        Me.rbutBoundingBox.TabIndex = 1
        Me.rbutBoundingBox.TabStop = True
        Me.rbutBoundingBox.Text = "Boundingbox"
        Me.rbutBoundingBox.UseVisualStyleBackColor = True
        '
        'rbutOnlyTrack
        '
        Me.rbutOnlyTrack.AutoSize = True
        Me.rbutOnlyTrack.Location = New System.Drawing.Point(149, 37)
        Me.rbutOnlyTrack.Name = "rbutOnlyTrack"
        Me.rbutOnlyTrack.Size = New System.Drawing.Size(96, 17)
        Me.rbutOnlyTrack.TabIndex = 0
        Me.rbutOnlyTrack.Text = "Track tiles only"
        Me.rbutOnlyTrack.UseVisualStyleBackColor = True
        '
        'OpenFileDialog1
        '
        Me.OpenFileDialog1.FileName = "OpenFileDialog1"
        '
        'gbDownload
        '
        Me.gbDownload.Controls.Add(Me.chkForceNewtiles)
        Me.gbDownload.Controls.Add(Me.DownloadProgressBar)
        Me.gbDownload.Controls.Add(Me.lblDownloadStatus)
        Me.gbDownload.Controls.Add(Me.btnDownload)
        Me.gbDownload.Enabled = False
        Me.gbDownload.Location = New System.Drawing.Point(19, 388)
        Me.gbDownload.Name = "gbDownload"
        Me.gbDownload.Size = New System.Drawing.Size(498, 88)
        Me.gbDownload.TabIndex = 4
        Me.gbDownload.TabStop = False
        Me.gbDownload.Text = "Download"
        '
        'chkForceNewtiles
        '
        Me.chkForceNewtiles.AutoSize = True
        Me.chkForceNewtiles.Location = New System.Drawing.Point(339, 19)
        Me.chkForceNewtiles.Name = "chkForceNewtiles"
        Me.chkForceNewtiles.Size = New System.Drawing.Size(89, 17)
        Me.chkForceNewtiles.TabIndex = 3
        Me.chkForceNewtiles.Text = "Force update"
        Me.chkForceNewtiles.UseVisualStyleBackColor = True
        '
        'DownloadProgressBar
        '
        Me.DownloadProgressBar.Location = New System.Drawing.Point(46, 43)
        Me.DownloadProgressBar.Name = "DownloadProgressBar"
        Me.DownloadProgressBar.Size = New System.Drawing.Size(285, 19)
        Me.DownloadProgressBar.TabIndex = 2
        '
        'lblDownloadStatus
        '
        Me.lblDownloadStatus.AutoSize = True
        Me.lblDownloadStatus.Location = New System.Drawing.Point(45, 27)
        Me.lblDownloadStatus.Name = "lblDownloadStatus"
        Me.lblDownloadStatus.Size = New System.Drawing.Size(35, 13)
        Me.lblDownloadStatus.TabIndex = 1
        Me.lblDownloadStatus.Text = "status"
        '
        'btnDownload
        '
        Me.btnDownload.Enabled = False
        Me.btnDownload.Location = New System.Drawing.Point(339, 42)
        Me.btnDownload.Name = "btnDownload"
        Me.btnDownload.Size = New System.Drawing.Size(80, 30)
        Me.btnDownload.TabIndex = 0
        Me.btnDownload.Text = "Download"
        Me.btnDownload.UseVisualStyleBackColor = True
        '
        'gbMapCreator
        '
        Me.gbMapCreator.Controls.Add(Me.cmbCalibrationFile)
        Me.gbMapCreator.Controls.Add(Me.Label26)
        Me.gbMapCreator.Controls.Add(Me.pbMapCreation)
        Me.gbMapCreator.Controls.Add(Me.btnBrowse)
        Me.gbMapCreator.Controls.Add(Me.Label6)
        Me.gbMapCreator.Controls.Add(Me.txtMapFileName)
        Me.gbMapCreator.Controls.Add(Me.btnCreate)
        Me.gbMapCreator.Enabled = False
        Me.gbMapCreator.Location = New System.Drawing.Point(19, 482)
        Me.gbMapCreator.Name = "gbMapCreator"
        Me.gbMapCreator.Size = New System.Drawing.Size(498, 152)
        Me.gbMapCreator.TabIndex = 5
        Me.gbMapCreator.TabStop = False
        Me.gbMapCreator.Text = "Map creator"
        '
        'cmbCalibrationFile
        '
        Me.cmbCalibrationFile.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList
        Me.cmbCalibrationFile.FormattingEnabled = True
        Me.cmbCalibrationFile.Items.AddRange(New Object() {"None", "Ozi Explorer (*.map)", "Georeference (*.pgw)"})
        Me.cmbCalibrationFile.Location = New System.Drawing.Point(106, 82)
        Me.cmbCalibrationFile.Name = "cmbCalibrationFile"
        Me.cmbCalibrationFile.Size = New System.Drawing.Size(226, 21)
        Me.cmbCalibrationFile.TabIndex = 7
        '
        'Label26
        '
        Me.Label26.AutoSize = True
        Me.Label26.Location = New System.Drawing.Point(28, 85)
        Me.Label26.Name = "Label26"
        Me.Label26.Size = New System.Drawing.Size(72, 13)
        Me.Label26.TabIndex = 6
        Me.Label26.Text = "Calibration file"
        '
        'pbMapCreation
        '
        Me.pbMapCreation.Location = New System.Drawing.Point(50, 112)
        Me.pbMapCreation.Name = "pbMapCreation"
        Me.pbMapCreation.Size = New System.Drawing.Size(285, 19)
        Me.pbMapCreation.TabIndex = 4
        '
        'btnBrowse
        '
        Me.btnBrowse.Location = New System.Drawing.Point(342, 47)
        Me.btnBrowse.Name = "btnBrowse"
        Me.btnBrowse.Size = New System.Drawing.Size(80, 30)
        Me.btnBrowse.TabIndex = 3
        Me.btnBrowse.Text = "Select"
        Me.btnBrowse.UseVisualStyleBackColor = True
        '
        'Label6
        '
        Me.Label6.AutoSize = True
        Me.Label6.Location = New System.Drawing.Point(28, 28)
        Me.Label6.Name = "Label6"
        Me.Label6.Size = New System.Drawing.Size(70, 13)
        Me.Label6.TabIndex = 2
        Me.Label6.Text = "Map filename"
        '
        'txtMapFileName
        '
        Me.txtMapFileName.Location = New System.Drawing.Point(50, 52)
        Me.txtMapFileName.Name = "txtMapFileName"
        Me.txtMapFileName.Size = New System.Drawing.Size(285, 20)
        Me.txtMapFileName.TabIndex = 1
        '
        'btnCreate
        '
        Me.btnCreate.Location = New System.Drawing.Point(341, 112)
        Me.btnCreate.Name = "btnCreate"
        Me.btnCreate.Size = New System.Drawing.Size(80, 30)
        Me.btnCreate.TabIndex = 0
        Me.btnCreate.Text = "Create"
        Me.btnCreate.UseVisualStyleBackColor = True
        '
        'TabControl1
        '
        Me.TabControl1.Controls.Add(Me.TabPage1)
        Me.TabControl1.Controls.Add(Me.TabPage2)
        Me.TabControl1.Location = New System.Drawing.Point(22, 5)
        Me.TabControl1.Name = "TabControl1"
        Me.TabControl1.SelectedIndex = 0
        Me.TabControl1.Size = New System.Drawing.Size(246, 168)
        Me.TabControl1.TabIndex = 6
        '
        'TabPage1
        '
        Me.TabPage1.Controls.Add(Me.btnLoadGPX)
        Me.TabPage1.Controls.Add(Me.ComboBox1)
        Me.TabPage1.Controls.Add(Me.Label4)
        Me.TabPage1.Controls.Add(Me.lblGPXfilename)
        Me.TabPage1.Location = New System.Drawing.Point(4, 22)
        Me.TabPage1.Name = "TabPage1"
        Me.TabPage1.Padding = New System.Windows.Forms.Padding(3)
        Me.TabPage1.Size = New System.Drawing.Size(238, 142)
        Me.TabPage1.TabIndex = 0
        Me.TabPage1.Text = "GPX file"
        Me.TabPage1.UseVisualStyleBackColor = True
        '
        'btnLoadGPX
        '
        Me.btnLoadGPX.Location = New System.Drawing.Point(18, 15)
        Me.btnLoadGPX.Name = "btnLoadGPX"
        Me.btnLoadGPX.Size = New System.Drawing.Size(80, 30)
        Me.btnLoadGPX.TabIndex = 10
        Me.btnLoadGPX.Text = "Load GPX"
        Me.btnLoadGPX.UseVisualStyleBackColor = True
        '
        'ComboBox1
        '
        Me.ComboBox1.Enabled = False
        Me.ComboBox1.FormattingEnabled = True
        Me.ComboBox1.Location = New System.Drawing.Point(72, 92)
        Me.ComboBox1.Name = "ComboBox1"
        Me.ComboBox1.Size = New System.Drawing.Size(75, 21)
        Me.ComboBox1.TabIndex = 9
        '
        'Label4
        '
        Me.Label4.AutoSize = True
        Me.Label4.Location = New System.Drawing.Point(16, 95)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(45, 13)
        Me.Label4.TabIndex = 8
        Me.Label4.Text = "Track #"
        '
        'lblGPXfilename
        '
        Me.lblGPXfilename.AutoSize = True
        Me.lblGPXfilename.Location = New System.Drawing.Point(16, 64)
        Me.lblGPXfilename.Name = "lblGPXfilename"
        Me.lblGPXfilename.Size = New System.Drawing.Size(45, 13)
        Me.lblGPXfilename.TabIndex = 7
        Me.lblGPXfilename.Text = "GPX file"
        '
        'TabPage2
        '
        Me.TabPage2.Controls.Add(Me.NumericUpDown1)
        Me.TabPage2.Controls.Add(Me.Label13)
        Me.TabPage2.Controls.Add(Me.Label12)
        Me.TabPage2.Controls.Add(Me.txtCntrLon)
        Me.TabPage2.Controls.Add(Me.txtCntrLat)
        Me.TabPage2.Controls.Add(Me.Label11)
        Me.TabPage2.Controls.Add(Me.txtUrlBox)
        Me.TabPage2.Controls.Add(Me.Label10)
        Me.TabPage2.Controls.Add(Me.Label9)
        Me.TabPage2.Location = New System.Drawing.Point(4, 22)
        Me.TabPage2.Name = "TabPage2"
        Me.TabPage2.Padding = New System.Windows.Forms.Padding(3)
        Me.TabPage2.Size = New System.Drawing.Size(238, 142)
        Me.TabPage2.TabIndex = 1
        Me.TabPage2.Text = "Manual"
        Me.TabPage2.UseVisualStyleBackColor = True
        '
        'NumericUpDown1
        '
        Me.NumericUpDown1.Location = New System.Drawing.Point(15, 112)
        Me.NumericUpDown1.Minimum = New Decimal(New Integer() {1, 0, 0, 0})
        Me.NumericUpDown1.Name = "NumericUpDown1"
        Me.NumericUpDown1.Size = New System.Drawing.Size(52, 20)
        Me.NumericUpDown1.TabIndex = 16
        Me.NumericUpDown1.TextAlign = System.Windows.Forms.HorizontalAlignment.Right
        Me.NumericUpDown1.Value = New Decimal(New Integer() {1, 0, 0, 0})
        '
        'Label13
        '
        Me.Label13.AutoSize = True
        Me.Label13.Location = New System.Drawing.Point(73, 114)
        Me.Label13.Name = "Label13"
        Me.Label13.Size = New System.Drawing.Size(21, 13)
        Me.Label13.TabIndex = 15
        Me.Label13.Text = "km"
        '
        'Label12
        '
        Me.Label12.AutoSize = True
        Me.Label12.Location = New System.Drawing.Point(15, 97)
        Me.Label12.Name = "Label12"
        Me.Label12.Size = New System.Drawing.Size(50, 13)
        Me.Label12.TabIndex = 13
        Me.Label12.Text = "Area size"
        '
        'txtCntrLon
        '
        Me.txtCntrLon.Location = New System.Drawing.Point(114, 68)
        Me.txtCntrLon.Name = "txtCntrLon"
        Me.txtCntrLon.Size = New System.Drawing.Size(93, 20)
        Me.txtCntrLon.TabIndex = 12
        '
        'txtCntrLat
        '
        Me.txtCntrLat.Location = New System.Drawing.Point(14, 68)
        Me.txtCntrLat.Name = "txtCntrLat"
        Me.txtCntrLat.Size = New System.Drawing.Size(93, 20)
        Me.txtCntrLat.TabIndex = 11
        '
        'Label11
        '
        Me.Label11.AutoSize = True
        Me.Label11.Location = New System.Drawing.Point(15, 17)
        Me.Label11.Name = "Label11"
        Me.Label11.Size = New System.Drawing.Size(78, 13)
        Me.Label11.TabIndex = 9
        Me.Label11.Text = "URL paste box"
        '
        'txtUrlBox
        '
        Me.txtUrlBox.Location = New System.Drawing.Point(14, 33)
        Me.txtUrlBox.Multiline = True
        Me.txtUrlBox.Name = "txtUrlBox"
        Me.txtUrlBox.Size = New System.Drawing.Size(161, 17)
        Me.txtUrlBox.TabIndex = 8
        '
        'Label10
        '
        Me.Label10.AutoSize = True
        Me.Label10.Location = New System.Drawing.Point(115, 53)
        Me.Label10.Name = "Label10"
        Me.Label10.Size = New System.Drawing.Size(25, 13)
        Me.Label10.TabIndex = 7
        Me.Label10.Text = "Lon"
        '
        'Label9
        '
        Me.Label9.AutoSize = True
        Me.Label9.Location = New System.Drawing.Point(15, 53)
        Me.Label9.Name = "Label9"
        Me.Label9.Size = New System.Drawing.Size(22, 13)
        Me.Label9.TabIndex = 6
        Me.Label9.Text = "Lat"
        '
        'GroupBox2
        '
        Me.GroupBox2.Controls.Add(Me.txtMaxY)
        Me.GroupBox2.Controls.Add(Me.txtMaxX)
        Me.GroupBox2.Controls.Add(Me.Label24)
        Me.GroupBox2.Controls.Add(Me.Label25)
        Me.GroupBox2.Controls.Add(Me.Label23)
        Me.GroupBox2.Controls.Add(Me.txtMinY)
        Me.GroupBox2.Controls.Add(Me.Label22)
        Me.GroupBox2.Controls.Add(Me.txtMinX)
        Me.GroupBox2.Controls.Add(Me.lblSize)
        Me.GroupBox2.Controls.Add(Me.Label20)
        Me.GroupBox2.Controls.Add(Me.Label21)
        Me.GroupBox2.Controls.Add(Me.Label8)
        Me.GroupBox2.Controls.Add(Me.Label7)
        Me.GroupBox2.Controls.Add(Me.txtMaxLon)
        Me.GroupBox2.Controls.Add(Me.txtMaxLat)
        Me.GroupBox2.Controls.Add(Me.txtMinLon)
        Me.GroupBox2.Controls.Add(Me.txtMinLat)
        Me.GroupBox2.Location = New System.Drawing.Point(274, 5)
        Me.GroupBox2.Name = "GroupBox2"
        Me.GroupBox2.Size = New System.Drawing.Size(243, 168)
        Me.GroupBox2.TabIndex = 7
        Me.GroupBox2.TabStop = False
        Me.GroupBox2.Text = "Overview"
        '
        'txtMaxY
        '
        Me.txtMaxY.Location = New System.Drawing.Point(142, 124)
        Me.txtMaxY.Name = "txtMaxY"
        Me.txtMaxY.ReadOnly = True
        Me.txtMaxY.Size = New System.Drawing.Size(93, 20)
        Me.txtMaxY.TabIndex = 24
        '
        'txtMaxX
        '
        Me.txtMaxX.Location = New System.Drawing.Point(41, 124)
        Me.txtMaxX.Name = "txtMaxX"
        Me.txtMaxX.ReadOnly = True
        Me.txtMaxX.Size = New System.Drawing.Size(93, 20)
        Me.txtMaxX.TabIndex = 23
        '
        'Label24
        '
        Me.Label24.AutoSize = True
        Me.Label24.Location = New System.Drawing.Point(14, 123)
        Me.Label24.Name = "Label24"
        Me.Label24.Size = New System.Drawing.Size(27, 13)
        Me.Label24.TabIndex = 22
        Me.Label24.Text = "Max"
        '
        'Label25
        '
        Me.Label25.AutoSize = True
        Me.Label25.Location = New System.Drawing.Point(14, 100)
        Me.Label25.Name = "Label25"
        Me.Label25.Size = New System.Drawing.Size(24, 13)
        Me.Label25.TabIndex = 21
        Me.Label25.Text = "Min"
        '
        'Label23
        '
        Me.Label23.AutoSize = True
        Me.Label23.Location = New System.Drawing.Point(138, 83)
        Me.Label23.Name = "Label23"
        Me.Label23.Size = New System.Drawing.Size(12, 13)
        Me.Label23.TabIndex = 20
        Me.Label23.Text = "y"
        '
        'txtMinY
        '
        Me.txtMinY.Location = New System.Drawing.Point(142, 98)
        Me.txtMinY.Name = "txtMinY"
        Me.txtMinY.ReadOnly = True
        Me.txtMinY.Size = New System.Drawing.Size(93, 20)
        Me.txtMinY.TabIndex = 19
        '
        'Label22
        '
        Me.Label22.AutoSize = True
        Me.Label22.Location = New System.Drawing.Point(38, 83)
        Me.Label22.Name = "Label22"
        Me.Label22.Size = New System.Drawing.Size(12, 13)
        Me.Label22.TabIndex = 18
        Me.Label22.Text = "x"
        '
        'txtMinX
        '
        Me.txtMinX.Location = New System.Drawing.Point(41, 98)
        Me.txtMinX.Name = "txtMinX"
        Me.txtMinX.ReadOnly = True
        Me.txtMinX.Size = New System.Drawing.Size(93, 20)
        Me.txtMinX.TabIndex = 17
        '
        'lblSize
        '
        Me.lblSize.AutoSize = True
        Me.lblSize.Location = New System.Drawing.Point(11, 151)
        Me.lblSize.Name = "lblSize"
        Me.lblSize.Size = New System.Drawing.Size(27, 13)
        Me.lblSize.TabIndex = 16
        Me.lblSize.Text = "Size"
        '
        'Label20
        '
        Me.Label20.AutoSize = True
        Me.Label20.Location = New System.Drawing.Point(138, 24)
        Me.Label20.Name = "Label20"
        Me.Label20.Size = New System.Drawing.Size(25, 13)
        Me.Label20.TabIndex = 13
        Me.Label20.Text = "Lon"
        '
        'Label21
        '
        Me.Label21.AutoSize = True
        Me.Label21.Location = New System.Drawing.Point(38, 24)
        Me.Label21.Name = "Label21"
        Me.Label21.Size = New System.Drawing.Size(22, 13)
        Me.Label21.TabIndex = 12
        Me.Label21.Text = "Lat"
        '
        'Label8
        '
        Me.Label8.AutoSize = True
        Me.Label8.Location = New System.Drawing.Point(11, 64)
        Me.Label8.Name = "Label8"
        Me.Label8.Size = New System.Drawing.Size(27, 13)
        Me.Label8.TabIndex = 11
        Me.Label8.Text = "Max"
        '
        'Label7
        '
        Me.Label7.AutoSize = True
        Me.Label7.Location = New System.Drawing.Point(11, 41)
        Me.Label7.Name = "Label7"
        Me.Label7.Size = New System.Drawing.Size(24, 13)
        Me.Label7.TabIndex = 10
        Me.Label7.Text = "Min"
        '
        'txtMaxLon
        '
        Me.txtMaxLon.Location = New System.Drawing.Point(141, 64)
        Me.txtMaxLon.Name = "txtMaxLon"
        Me.txtMaxLon.ReadOnly = True
        Me.txtMaxLon.Size = New System.Drawing.Size(93, 20)
        Me.txtMaxLon.TabIndex = 9
        '
        'txtMaxLat
        '
        Me.txtMaxLat.Location = New System.Drawing.Point(41, 64)
        Me.txtMaxLat.Name = "txtMaxLat"
        Me.txtMaxLat.ReadOnly = True
        Me.txtMaxLat.Size = New System.Drawing.Size(93, 20)
        Me.txtMaxLat.TabIndex = 8
        '
        'txtMinLon
        '
        Me.txtMinLon.Location = New System.Drawing.Point(142, 40)
        Me.txtMinLon.Name = "txtMinLon"
        Me.txtMinLon.ReadOnly = True
        Me.txtMinLon.Size = New System.Drawing.Size(93, 20)
        Me.txtMinLon.TabIndex = 7
        '
        'txtMinLat
        '
        Me.txtMinLat.Location = New System.Drawing.Point(41, 39)
        Me.txtMinLat.Name = "txtMinLat"
        Me.txtMinLat.ReadOnly = True
        Me.txtMinLat.Size = New System.Drawing.Size(93, 20)
        Me.txtMinLat.TabIndex = 6
        '
        'Label14
        '
        Me.Label14.AutoSize = True
        Me.Label14.Location = New System.Drawing.Point(332, 1)
        Me.Label14.Name = "Label14"
        Me.Label14.Size = New System.Drawing.Size(25, 13)
        Me.Label14.TabIndex = 7
        Me.Label14.Text = "Lon"
        '
        'NumericUpDown2
        '
        Me.NumericUpDown2.Location = New System.Drawing.Point(24, 61)
        Me.NumericUpDown2.Minimum = New Decimal(New Integer() {1, 0, 0, 0})
        Me.NumericUpDown2.Name = "NumericUpDown2"
        Me.NumericUpDown2.Size = New System.Drawing.Size(52, 20)
        Me.NumericUpDown2.TabIndex = 16
        Me.NumericUpDown2.TextAlign = System.Windows.Forms.HorizontalAlignment.Right
        Me.NumericUpDown2.Value = New Decimal(New Integer() {1, 0, 0, 0})
        '
        'Label15
        '
        Me.Label15.AutoSize = True
        Me.Label15.Location = New System.Drawing.Point(82, 63)
        Me.Label15.Name = "Label15"
        Me.Label15.Size = New System.Drawing.Size(21, 13)
        Me.Label15.TabIndex = 15
        Me.Label15.Text = "km"
        '
        'Label16
        '
        Me.Label16.AutoSize = True
        Me.Label16.Location = New System.Drawing.Point(24, 46)
        Me.Label16.Name = "Label16"
        Me.Label16.Size = New System.Drawing.Size(50, 13)
        Me.Label16.TabIndex = 13
        Me.Label16.Text = "Area size"
        '
        'TextBox1
        '
        Me.TextBox1.Location = New System.Drawing.Point(331, 16)
        Me.TextBox1.Name = "TextBox1"
        Me.TextBox1.Size = New System.Drawing.Size(93, 20)
        Me.TextBox1.TabIndex = 12
        '
        'TextBox2
        '
        Me.TextBox2.Location = New System.Drawing.Point(231, 16)
        Me.TextBox2.Name = "TextBox2"
        Me.TextBox2.Size = New System.Drawing.Size(93, 20)
        Me.TextBox2.TabIndex = 11
        '
        'Label17
        '
        Me.Label17.AutoSize = True
        Me.Label17.Location = New System.Drawing.Point(190, 16)
        Me.Label17.Name = "Label17"
        Me.Label17.Size = New System.Drawing.Size(38, 13)
        Me.Label17.TabIndex = 10
        Me.Label17.Text = "Center"
        '
        'Label18
        '
        Me.Label18.AutoSize = True
        Me.Label18.Location = New System.Drawing.Point(21, 3)
        Me.Label18.Name = "Label18"
        Me.Label18.Size = New System.Drawing.Size(78, 13)
        Me.Label18.TabIndex = 9
        Me.Label18.Text = "URL paste box"
        '
        'TextBox3
        '
        Me.TextBox3.Location = New System.Drawing.Point(23, 19)
        Me.TextBox3.Multiline = True
        Me.TextBox3.Name = "TextBox3"
        Me.TextBox3.Size = New System.Drawing.Size(161, 17)
        Me.TextBox3.TabIndex = 8
        '
        'Label19
        '
        Me.Label19.AutoSize = True
        Me.Label19.Location = New System.Drawing.Point(232, 1)
        Me.Label19.Name = "Label19"
        Me.Label19.Size = New System.Drawing.Size(22, 13)
        Me.Label19.TabIndex = 6
        Me.Label19.Text = "Lat"
        '
        'frmMain
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(541, 650)
        Me.Controls.Add(Me.GroupBox2)
        Me.Controls.Add(Me.TabControl1)
        Me.Controls.Add(Me.gbMapCreator)
        Me.Controls.Add(Me.gbDownload)
        Me.Controls.Add(Me.gbSetup)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.MinimizeBox = False
        Me.Name = "frmMain"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "OSM Tile Downloader"
        Me.gbSetup.ResumeLayout(False)
        Me.gbSetup.PerformLayout()
        Me.gbDownload.ResumeLayout(False)
        Me.gbDownload.PerformLayout()
        Me.gbMapCreator.ResumeLayout(False)
        Me.gbMapCreator.PerformLayout()
        Me.TabControl1.ResumeLayout(False)
        Me.TabPage1.ResumeLayout(False)
        Me.TabPage1.PerformLayout()
        Me.TabPage2.ResumeLayout(False)
        Me.TabPage2.PerformLayout()
        CType(Me.NumericUpDown1, System.ComponentModel.ISupportInitialize).EndInit()
        Me.GroupBox2.ResumeLayout(False)
        Me.GroupBox2.PerformLayout()
        CType(Me.NumericUpDown2, System.ComponentModel.ISupportInitialize).EndInit()
        Me.ResumeLayout(False)

    End Sub
    Friend WithEvents gbSetup As System.Windows.Forms.GroupBox
    Friend WithEvents rbutBoundingBox As System.Windows.Forms.RadioButton
    Friend WithEvents rbutOnlyTrack As System.Windows.Forms.RadioButton
    Friend WithEvents OpenFileDialog1 As System.Windows.Forms.OpenFileDialog
    Friend WithEvents Label3 As System.Windows.Forms.Label
    Friend WithEvents txtStorageFolder As System.Windows.Forms.TextBox
    Friend WithEvents btnSelectStorageFolder As System.Windows.Forms.Button
    Friend WithEvents Label1 As System.Windows.Forms.Label
    Friend WithEvents cmbTileServer As System.Windows.Forms.ComboBox
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents gbDownload As System.Windows.Forms.GroupBox
    Friend WithEvents btnDownload As System.Windows.Forms.Button
    Friend WithEvents FolderBrowserDialog1 As System.Windows.Forms.FolderBrowserDialog
    Friend WithEvents DownloadProgressBar As System.Windows.Forms.ProgressBar
    Friend WithEvents lblDownloadStatus As System.Windows.Forms.Label
    Friend WithEvents cmbZoom As System.Windows.Forms.ComboBox
    Friend WithEvents Label5 As System.Windows.Forms.Label
    Friend WithEvents chkForceNewtiles As System.Windows.Forms.CheckBox
    Friend WithEvents gbMapCreator As System.Windows.Forms.GroupBox
    Friend WithEvents Label6 As System.Windows.Forms.Label
    Friend WithEvents txtMapFileName As System.Windows.Forms.TextBox
    Friend WithEvents btnCreate As System.Windows.Forms.Button
    Friend WithEvents btnBrowse As System.Windows.Forms.Button
    Friend WithEvents SaveFileDialog1 As System.Windows.Forms.SaveFileDialog
    Friend WithEvents pbMapCreation As System.Windows.Forms.ProgressBar
    Friend WithEvents TabControl1 As System.Windows.Forms.TabControl
    Friend WithEvents TabPage1 As System.Windows.Forms.TabPage
    Friend WithEvents ComboBox1 As System.Windows.Forms.ComboBox
    Friend WithEvents Label4 As System.Windows.Forms.Label
    Friend WithEvents lblGPXfilename As System.Windows.Forms.Label
    Friend WithEvents TabPage2 As System.Windows.Forms.TabPage
    Friend WithEvents btnLoadGPX As System.Windows.Forms.Button
    Friend WithEvents Label11 As System.Windows.Forms.Label
    Friend WithEvents txtUrlBox As System.Windows.Forms.TextBox
    Friend WithEvents Label10 As System.Windows.Forms.Label
    Friend WithEvents Label9 As System.Windows.Forms.Label
    Friend WithEvents txtCntrLon As System.Windows.Forms.TextBox
    Friend WithEvents txtCntrLat As System.Windows.Forms.TextBox
    Friend WithEvents Label13 As System.Windows.Forms.Label
    Friend WithEvents Label12 As System.Windows.Forms.Label
    Friend WithEvents NumericUpDown1 As System.Windows.Forms.NumericUpDown
    Friend WithEvents GroupBox2 As System.Windows.Forms.GroupBox
    Friend WithEvents Label20 As System.Windows.Forms.Label
    Friend WithEvents Label21 As System.Windows.Forms.Label
    Friend WithEvents Label8 As System.Windows.Forms.Label
    Friend WithEvents Label7 As System.Windows.Forms.Label
    Friend WithEvents txtMaxLon As System.Windows.Forms.TextBox
    Friend WithEvents txtMaxLat As System.Windows.Forms.TextBox
    Friend WithEvents txtMinLon As System.Windows.Forms.TextBox
    Friend WithEvents txtMinLat As System.Windows.Forms.TextBox
    Friend WithEvents Label14 As System.Windows.Forms.Label
    Friend WithEvents NumericUpDown2 As System.Windows.Forms.NumericUpDown
    Friend WithEvents Label15 As System.Windows.Forms.Label
    Friend WithEvents Label16 As System.Windows.Forms.Label
    Friend WithEvents TextBox1 As System.Windows.Forms.TextBox
    Friend WithEvents TextBox2 As System.Windows.Forms.TextBox
    Friend WithEvents Label17 As System.Windows.Forms.Label
    Friend WithEvents Label18 As System.Windows.Forms.Label
    Friend WithEvents TextBox3 As System.Windows.Forms.TextBox
    Friend WithEvents Label19 As System.Windows.Forms.Label
    Friend WithEvents lblSize As System.Windows.Forms.Label
    Friend WithEvents txtMaxY As System.Windows.Forms.TextBox
    Friend WithEvents txtMaxX As System.Windows.Forms.TextBox
    Friend WithEvents Label24 As System.Windows.Forms.Label
    Friend WithEvents Label25 As System.Windows.Forms.Label
    Friend WithEvents Label23 As System.Windows.Forms.Label
    Friend WithEvents txtMinY As System.Windows.Forms.TextBox
    Friend WithEvents Label22 As System.Windows.Forms.Label
    Friend WithEvents txtMinX As System.Windows.Forms.TextBox
    Friend WithEvents Label26 As System.Windows.Forms.Label
    Friend WithEvents cmbCalibrationFile As System.Windows.Forms.ComboBox

End Class
