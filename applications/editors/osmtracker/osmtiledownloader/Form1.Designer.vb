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
        Me.GroupBox1 = New System.Windows.Forms.GroupBox
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
        Me.GroupBox2 = New System.Windows.Forms.GroupBox
        Me.ComboBox1 = New System.Windows.Forms.ComboBox
        Me.Label4 = New System.Windows.Forms.Label
        Me.lblGPXfilename = New System.Windows.Forms.Label
        Me.btnLoadGPX = New System.Windows.Forms.Button
        Me.GroupBox3 = New System.Windows.Forms.GroupBox
        Me.chkForceNewtiles = New System.Windows.Forms.CheckBox
        Me.DownloadProgressBar = New System.Windows.Forms.ProgressBar
        Me.lblDownloadStatus = New System.Windows.Forms.Label
        Me.btnDownload = New System.Windows.Forms.Button
        Me.FolderBrowserDialog1 = New System.Windows.Forms.FolderBrowserDialog
        Me.GroupBox1.SuspendLayout()
        Me.GroupBox2.SuspendLayout()
        Me.GroupBox3.SuspendLayout()
        Me.SuspendLayout()
        '
        'GroupBox1
        '
        Me.GroupBox1.Controls.Add(Me.cmbZoom)
        Me.GroupBox1.Controls.Add(Me.Label5)
        Me.GroupBox1.Controls.Add(Me.Label3)
        Me.GroupBox1.Controls.Add(Me.txtStorageFolder)
        Me.GroupBox1.Controls.Add(Me.btnSelectStorageFolder)
        Me.GroupBox1.Controls.Add(Me.Label1)
        Me.GroupBox1.Controls.Add(Me.cmbTileServer)
        Me.GroupBox1.Controls.Add(Me.Label2)
        Me.GroupBox1.Controls.Add(Me.rbutBoundingBox)
        Me.GroupBox1.Controls.Add(Me.rbutOnlyTrack)
        Me.GroupBox1.Location = New System.Drawing.Point(22, 108)
        Me.GroupBox1.Name = "GroupBox1"
        Me.GroupBox1.Size = New System.Drawing.Size(440, 225)
        Me.GroupBox1.TabIndex = 3
        Me.GroupBox1.TabStop = False
        Me.GroupBox1.Text = "Setup"
        '
        'cmbZoom
        '
        Me.cmbZoom.FormattingEnabled = True
        Me.cmbZoom.Location = New System.Drawing.Point(96, 141)
        Me.cmbZoom.Name = "cmbZoom"
        Me.cmbZoom.Size = New System.Drawing.Size(68, 21)
        Me.cmbZoom.TabIndex = 9
        '
        'Label5
        '
        Me.Label5.AutoSize = True
        Me.Label5.Location = New System.Drawing.Point(44, 141)
        Me.Label5.Name = "Label5"
        Me.Label5.Size = New System.Drawing.Size(34, 13)
        Me.Label5.TabIndex = 8
        Me.Label5.Text = "Zoom"
        '
        'Label3
        '
        Me.Label3.AutoSize = True
        Me.Label3.Location = New System.Drawing.Point(20, 165)
        Me.Label3.Name = "Label3"
        Me.Label3.Size = New System.Drawing.Size(91, 13)
        Me.Label3.TabIndex = 7
        Me.Label3.Text = "Tile storage folder"
        '
        'txtStorageFolder
        '
        Me.txtStorageFolder.Location = New System.Drawing.Point(47, 181)
        Me.txtStorageFolder.Name = "txtStorageFolder"
        Me.txtStorageFolder.Size = New System.Drawing.Size(286, 20)
        Me.txtStorageFolder.TabIndex = 6
        '
        'btnSelectStorageFolder
        '
        Me.btnSelectStorageFolder.Location = New System.Drawing.Point(339, 181)
        Me.btnSelectStorageFolder.Name = "btnSelectStorageFolder"
        Me.btnSelectStorageFolder.Size = New System.Drawing.Size(80, 30)
        Me.btnSelectStorageFolder.TabIndex = 5
        Me.btnSelectStorageFolder.Text = "Browse"
        Me.btnSelectStorageFolder.UseVisualStyleBackColor = True
        '
        'Label1
        '
        Me.Label1.AutoSize = True
        Me.Label1.Location = New System.Drawing.Point(21, 90)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(55, 13)
        Me.Label1.TabIndex = 4
        Me.Label1.Text = "TileServer"
        '
        'cmbTileServer
        '
        Me.cmbTileServer.FormattingEnabled = True
        Me.cmbTileServer.Location = New System.Drawing.Point(48, 106)
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
        Me.rbutBoundingBox.Location = New System.Drawing.Point(48, 60)
        Me.rbutBoundingBox.Name = "rbutBoundingBox"
        Me.rbutBoundingBox.Size = New System.Drawing.Size(87, 17)
        Me.rbutBoundingBox.TabIndex = 1
        Me.rbutBoundingBox.Text = "Boundingbox"
        Me.rbutBoundingBox.UseVisualStyleBackColor = True
        '
        'rbutOnlyTrack
        '
        Me.rbutOnlyTrack.AutoSize = True
        Me.rbutOnlyTrack.Checked = True
        Me.rbutOnlyTrack.Location = New System.Drawing.Point(48, 37)
        Me.rbutOnlyTrack.Name = "rbutOnlyTrack"
        Me.rbutOnlyTrack.Size = New System.Drawing.Size(96, 17)
        Me.rbutOnlyTrack.TabIndex = 0
        Me.rbutOnlyTrack.TabStop = True
        Me.rbutOnlyTrack.Text = "Track tiles only"
        Me.rbutOnlyTrack.UseVisualStyleBackColor = True
        '
        'OpenFileDialog1
        '
        Me.OpenFileDialog1.FileName = "OpenFileDialog1"
        '
        'GroupBox2
        '
        Me.GroupBox2.Controls.Add(Me.ComboBox1)
        Me.GroupBox2.Controls.Add(Me.Label4)
        Me.GroupBox2.Controls.Add(Me.lblGPXfilename)
        Me.GroupBox2.Controls.Add(Me.btnLoadGPX)
        Me.GroupBox2.Location = New System.Drawing.Point(22, 12)
        Me.GroupBox2.Name = "GroupBox2"
        Me.GroupBox2.Size = New System.Drawing.Size(440, 90)
        Me.GroupBox2.TabIndex = 0
        Me.GroupBox2.TabStop = False
        Me.GroupBox2.Text = "Select Track"
        '
        'ComboBox1
        '
        Me.ComboBox1.Enabled = False
        Me.ComboBox1.FormattingEnabled = True
        Me.ComboBox1.Location = New System.Drawing.Point(96, 55)
        Me.ComboBox1.Name = "ComboBox1"
        Me.ComboBox1.Size = New System.Drawing.Size(75, 21)
        Me.ComboBox1.TabIndex = 6
        '
        'Label4
        '
        Me.Label4.AutoSize = True
        Me.Label4.Location = New System.Drawing.Point(45, 58)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(45, 13)
        Me.Label4.TabIndex = 5
        Me.Label4.Text = "Track #"
        '
        'lblGPXfilename
        '
        Me.lblGPXfilename.AutoSize = True
        Me.lblGPXfilename.Location = New System.Drawing.Point(21, 34)
        Me.lblGPXfilename.Name = "lblGPXfilename"
        Me.lblGPXfilename.Size = New System.Drawing.Size(45, 13)
        Me.lblGPXfilename.TabIndex = 4
        Me.lblGPXfilename.Text = "GPX file"
        '
        'btnLoadGPX
        '
        Me.btnLoadGPX.Location = New System.Drawing.Point(340, 25)
        Me.btnLoadGPX.Name = "btnLoadGPX"
        Me.btnLoadGPX.Size = New System.Drawing.Size(80, 30)
        Me.btnLoadGPX.TabIndex = 3
        Me.btnLoadGPX.Text = "Load GPX"
        Me.btnLoadGPX.UseVisualStyleBackColor = True
        '
        'GroupBox3
        '
        Me.GroupBox3.Controls.Add(Me.chkForceNewtiles)
        Me.GroupBox3.Controls.Add(Me.DownloadProgressBar)
        Me.GroupBox3.Controls.Add(Me.lblDownloadStatus)
        Me.GroupBox3.Controls.Add(Me.btnDownload)
        Me.GroupBox3.Location = New System.Drawing.Point(22, 352)
        Me.GroupBox3.Name = "GroupBox3"
        Me.GroupBox3.Size = New System.Drawing.Size(440, 108)
        Me.GroupBox3.TabIndex = 4
        Me.GroupBox3.TabStop = False
        Me.GroupBox3.Text = "Download"
        '
        'chkForceNewtiles
        '
        Me.chkForceNewtiles.AutoSize = True
        Me.chkForceNewtiles.Location = New System.Drawing.Point(340, 71)
        Me.chkForceNewtiles.Name = "chkForceNewtiles"
        Me.chkForceNewtiles.Size = New System.Drawing.Size(89, 17)
        Me.chkForceNewtiles.TabIndex = 3
        Me.chkForceNewtiles.Text = "Force update"
        Me.chkForceNewtiles.UseVisualStyleBackColor = True
        '
        'DownloadProgressBar
        '
        Me.DownloadProgressBar.Location = New System.Drawing.Point(48, 69)
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
        Me.btnDownload.Location = New System.Drawing.Point(340, 27)
        Me.btnDownload.Name = "btnDownload"
        Me.btnDownload.Size = New System.Drawing.Size(80, 30)
        Me.btnDownload.TabIndex = 0
        Me.btnDownload.Text = "Download"
        Me.btnDownload.UseVisualStyleBackColor = True
        '
        'frmMain
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(484, 477)
        Me.Controls.Add(Me.GroupBox3)
        Me.Controls.Add(Me.GroupBox2)
        Me.Controls.Add(Me.GroupBox1)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog
        Me.MaximizeBox = False
        Me.MinimizeBox = False
        Me.Name = "frmMain"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "OSM Tile Downloader"
        Me.GroupBox1.ResumeLayout(False)
        Me.GroupBox1.PerformLayout()
        Me.GroupBox2.ResumeLayout(False)
        Me.GroupBox2.PerformLayout()
        Me.GroupBox3.ResumeLayout(False)
        Me.GroupBox3.PerformLayout()
        Me.ResumeLayout(False)

    End Sub
    Friend WithEvents GroupBox1 As System.Windows.Forms.GroupBox
    Friend WithEvents rbutBoundingBox As System.Windows.Forms.RadioButton
    Friend WithEvents rbutOnlyTrack As System.Windows.Forms.RadioButton
    Friend WithEvents OpenFileDialog1 As System.Windows.Forms.OpenFileDialog
    Friend WithEvents GroupBox2 As System.Windows.Forms.GroupBox
    Friend WithEvents lblGPXfilename As System.Windows.Forms.Label
    Friend WithEvents btnLoadGPX As System.Windows.Forms.Button
    Friend WithEvents Label3 As System.Windows.Forms.Label
    Friend WithEvents txtStorageFolder As System.Windows.Forms.TextBox
    Friend WithEvents btnSelectStorageFolder As System.Windows.Forms.Button
    Friend WithEvents Label1 As System.Windows.Forms.Label
    Friend WithEvents cmbTileServer As System.Windows.Forms.ComboBox
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents GroupBox3 As System.Windows.Forms.GroupBox
    Friend WithEvents btnDownload As System.Windows.Forms.Button
    Friend WithEvents FolderBrowserDialog1 As System.Windows.Forms.FolderBrowserDialog
    Friend WithEvents DownloadProgressBar As System.Windows.Forms.ProgressBar
    Friend WithEvents lblDownloadStatus As System.Windows.Forms.Label
    Friend WithEvents ComboBox1 As System.Windows.Forms.ComboBox
    Friend WithEvents Label4 As System.Windows.Forms.Label
    Friend WithEvents cmbZoom As System.Windows.Forms.ComboBox
    Friend WithEvents Label5 As System.Windows.Forms.Label
    Friend WithEvents chkForceNewtiles As System.Windows.Forms.CheckBox

End Class
