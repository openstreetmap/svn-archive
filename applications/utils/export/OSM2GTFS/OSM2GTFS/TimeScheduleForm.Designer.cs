namespace OSM2GTFS
{
    partial class TimeScheduleForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.lbServiceType = new System.Windows.Forms.ListBox();
            this.label1 = new System.Windows.Forms.Label();
            this.cbMon = new System.Windows.Forms.CheckBox();
            this.cbTue = new System.Windows.Forms.CheckBox();
            this.cbWed = new System.Windows.Forms.CheckBox();
            this.cbThu = new System.Windows.Forms.CheckBox();
            this.cbFri = new System.Windows.Forms.CheckBox();
            this.cbSat = new System.Windows.Forms.CheckBox();
            this.cbSun = new System.Windows.Forms.CheckBox();
            this.gridTimes = new System.Windows.Forms.DataGridView();
            this.label2 = new System.Windows.Forms.Label();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.panel1 = new System.Windows.Forms.Panel();
            this.label4 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.tbEndDate = new System.Windows.Forms.TextBox();
            this.tbStartDate = new System.Windows.Forms.TextBox();
            this.btAddService = new System.Windows.Forms.Button();
            this.tbServiceID = new System.Windows.Forms.TextBox();
            this.panel2 = new System.Windows.Forms.Panel();
            this.label5 = new System.Windows.Forms.Label();
            this.lbRoutes = new System.Windows.Forms.ListBox();
            this.panel3 = new System.Windows.Forms.Panel();
            this.cbInexactSchedule = new System.Windows.Forms.CheckBox();
            this.lbStatus = new System.Windows.Forms.Label();
            this.panel4 = new System.Windows.Forms.Panel();
            this.btFillRectangle = new System.Windows.Forms.Button();
            this.btSave = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.gridTimes)).BeginInit();
            this.tableLayoutPanel1.SuspendLayout();
            this.panel1.SuspendLayout();
            this.panel2.SuspendLayout();
            this.panel3.SuspendLayout();
            this.panel4.SuspendLayout();
            this.SuspendLayout();
            // 
            // lbServiceType
            // 
            this.lbServiceType.FormattingEnabled = true;
            this.lbServiceType.Location = new System.Drawing.Point(0, 3);
            this.lbServiceType.Name = "lbServiceType";
            this.lbServiceType.Size = new System.Drawing.Size(110, 173);
            this.lbServiceType.TabIndex = 0;
            this.lbServiceType.SelectedIndexChanged += new System.EventHandler(this.lbServiceType_SelectedIndexChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(123, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(125, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "Service ID (Calendar,txt):";
            // 
            // cbMon
            // 
            this.cbMon.AutoSize = true;
            this.cbMon.Location = new System.Drawing.Point(142, 4);
            this.cbMon.Name = "cbMon";
            this.cbMon.Size = new System.Drawing.Size(47, 17);
            this.cbMon.TabIndex = 2;
            this.cbMon.Text = "Mon";
            this.cbMon.UseVisualStyleBackColor = true;
            // 
            // cbTue
            // 
            this.cbTue.AutoSize = true;
            this.cbTue.Location = new System.Drawing.Point(142, 35);
            this.cbTue.Name = "cbTue";
            this.cbTue.Size = new System.Drawing.Size(45, 17);
            this.cbTue.TabIndex = 3;
            this.cbTue.Text = "Tue";
            this.cbTue.UseVisualStyleBackColor = true;
            // 
            // cbWed
            // 
            this.cbWed.AutoSize = true;
            this.cbWed.Location = new System.Drawing.Point(142, 60);
            this.cbWed.Name = "cbWed";
            this.cbWed.Size = new System.Drawing.Size(49, 17);
            this.cbWed.TabIndex = 4;
            this.cbWed.Text = "Wed";
            this.cbWed.UseVisualStyleBackColor = true;
            // 
            // cbThu
            // 
            this.cbThu.AutoSize = true;
            this.cbThu.Location = new System.Drawing.Point(142, 83);
            this.cbThu.Name = "cbThu";
            this.cbThu.Size = new System.Drawing.Size(45, 17);
            this.cbThu.TabIndex = 5;
            this.cbThu.Text = "Thu";
            this.cbThu.UseVisualStyleBackColor = true;
            // 
            // cbFri
            // 
            this.cbFri.AutoSize = true;
            this.cbFri.Location = new System.Drawing.Point(142, 106);
            this.cbFri.Name = "cbFri";
            this.cbFri.Size = new System.Drawing.Size(37, 17);
            this.cbFri.TabIndex = 6;
            this.cbFri.Text = "Fri";
            this.cbFri.UseVisualStyleBackColor = true;
            // 
            // cbSat
            // 
            this.cbSat.AutoSize = true;
            this.cbSat.Location = new System.Drawing.Point(142, 131);
            this.cbSat.Name = "cbSat";
            this.cbSat.Size = new System.Drawing.Size(42, 17);
            this.cbSat.TabIndex = 7;
            this.cbSat.Text = "Sat";
            this.cbSat.UseVisualStyleBackColor = true;
            // 
            // cbSun
            // 
            this.cbSun.AutoSize = true;
            this.cbSun.Location = new System.Drawing.Point(142, 159);
            this.cbSun.Name = "cbSun";
            this.cbSun.Size = new System.Drawing.Size(45, 17);
            this.cbSun.TabIndex = 8;
            this.cbSun.Text = "Sun";
            this.cbSun.UseVisualStyleBackColor = true;
            // 
            // gridTimes
            // 
            this.gridTimes.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.gridTimes.Dock = System.Windows.Forms.DockStyle.Fill;
            this.gridTimes.Location = new System.Drawing.Point(0, 0);
            this.gridTimes.Name = "gridTimes";
            this.gridTimes.Size = new System.Drawing.Size(727, 430);
            this.gridTimes.TabIndex = 9;
            this.gridTimes.CellValidating += new System.Windows.Forms.DataGridViewCellValidatingEventHandler(this.gridTimes_CellValidating);
            this.gridTimes.KeyDown += new System.Windows.Forms.KeyEventHandler(this.gridTimes_KeyDown);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(3, 0);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(33, 13);
            this.label2.TabIndex = 10;
            this.label2.Text = "Trips:";
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 5;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 200F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel1.Controls.Add(this.panel1, 2, 1);
            this.tableLayoutPanel1.Controls.Add(this.label1, 2, 0);
            this.tableLayoutPanel1.Controls.Add(this.panel2, 3, 1);
            this.tableLayoutPanel1.Controls.Add(this.label5, 1, 0);
            this.tableLayoutPanel1.Controls.Add(this.lbRoutes, 1, 1);
            this.tableLayoutPanel1.Controls.Add(this.panel3, 3, 0);
            this.tableLayoutPanel1.Controls.Add(this.panel4, 3, 2);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.RowCount = 3;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 30F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 40F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(1073, 506);
            this.tableLayoutPanel1.TabIndex = 11;
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.label4);
            this.panel1.Controls.Add(this.label3);
            this.panel1.Controls.Add(this.tbEndDate);
            this.panel1.Controls.Add(this.tbStartDate);
            this.panel1.Controls.Add(this.btAddService);
            this.panel1.Controls.Add(this.tbServiceID);
            this.panel1.Controls.Add(this.cbMon);
            this.panel1.Controls.Add(this.lbServiceType);
            this.panel1.Controls.Add(this.cbSun);
            this.panel1.Controls.Add(this.cbTue);
            this.panel1.Controls.Add(this.cbSat);
            this.panel1.Controls.Add(this.cbWed);
            this.panel1.Controls.Add(this.cbFri);
            this.panel1.Controls.Add(this.cbThu);
            this.panel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel1.Location = new System.Drawing.Point(123, 33);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(194, 430);
            this.panel1.TabIndex = 0;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(3, 237);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(55, 13);
            this.label4.TabIndex = 13;
            this.label4.Text = "End Date:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(3, 188);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(58, 13);
            this.label3.TabIndex = 11;
            this.label3.Text = "Start Date:";
            // 
            // tbEndDate
            // 
            this.tbEndDate.Location = new System.Drawing.Point(3, 253);
            this.tbEndDate.Name = "tbEndDate";
            this.tbEndDate.Size = new System.Drawing.Size(105, 20);
            this.tbEndDate.TabIndex = 12;
            // 
            // tbStartDate
            // 
            this.tbStartDate.Location = new System.Drawing.Point(3, 211);
            this.tbStartDate.Name = "tbStartDate";
            this.tbStartDate.Size = new System.Drawing.Size(105, 20);
            this.tbStartDate.TabIndex = 11;
            // 
            // btAddService
            // 
            this.btAddService.Location = new System.Drawing.Point(6, 376);
            this.btAddService.Name = "btAddService";
            this.btAddService.Size = new System.Drawing.Size(183, 31);
            this.btAddService.TabIndex = 10;
            this.btAddService.Text = "Add Service";
            this.btAddService.UseVisualStyleBackColor = true;
            this.btAddService.Click += new System.EventHandler(this.btAddService_Click);
            // 
            // tbServiceID
            // 
            this.tbServiceID.Location = new System.Drawing.Point(5, 350);
            this.tbServiceID.Name = "tbServiceID";
            this.tbServiceID.Size = new System.Drawing.Size(184, 20);
            this.tbServiceID.TabIndex = 9;
            // 
            // panel2
            // 
            this.panel2.Controls.Add(this.gridTimes);
            this.panel2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel2.Location = new System.Drawing.Point(323, 33);
            this.panel2.Name = "panel2";
            this.panel2.Size = new System.Drawing.Size(727, 430);
            this.panel2.TabIndex = 1;
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(23, 0);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(44, 13);
            this.label5.TabIndex = 11;
            this.label5.Text = "Routes:";
            // 
            // lbRoutes
            // 
            this.lbRoutes.Dock = System.Windows.Forms.DockStyle.Fill;
            this.lbRoutes.FormattingEnabled = true;
            this.lbRoutes.Location = new System.Drawing.Point(23, 33);
            this.lbRoutes.Name = "lbRoutes";
            this.lbRoutes.Size = new System.Drawing.Size(94, 420);
            this.lbRoutes.Sorted = true;
            this.lbRoutes.TabIndex = 12;
            this.lbRoutes.SelectedIndexChanged += new System.EventHandler(this.lbRoutes_SelectedIndexChanged);
            // 
            // panel3
            // 
            this.panel3.Controls.Add(this.cbInexactSchedule);
            this.panel3.Controls.Add(this.label2);
            this.panel3.Controls.Add(this.lbStatus);
            this.panel3.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel3.Location = new System.Drawing.Point(323, 3);
            this.panel3.Name = "panel3";
            this.panel3.Size = new System.Drawing.Size(727, 24);
            this.panel3.TabIndex = 14;
            // 
            // cbInexactSchedule
            // 
            this.cbInexactSchedule.AutoSize = true;
            this.cbInexactSchedule.Location = new System.Drawing.Point(63, 3);
            this.cbInexactSchedule.Name = "cbInexactSchedule";
            this.cbInexactSchedule.Size = new System.Drawing.Size(214, 17);
            this.cbInexactSchedule.TabIndex = 11;
            this.cbInexactSchedule.Text = "Stop times are not fixed (frequencies.txt)";
            this.cbInexactSchedule.UseVisualStyleBackColor = true;
            // 
            // lbStatus
            // 
            this.lbStatus.AutoSize = true;
            this.lbStatus.BackColor = System.Drawing.Color.Yellow;
            this.lbStatus.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lbStatus.Location = new System.Drawing.Point(283, 3);
            this.lbStatus.Name = "lbStatus";
            this.lbStatus.Size = new System.Drawing.Size(285, 17);
            this.lbStatus.TabIndex = 13;
            this.lbStatus.Text = "Editing route X for service Schedule Y";
            // 
            // panel4
            // 
            this.panel4.Controls.Add(this.btFillRectangle);
            this.panel4.Controls.Add(this.btSave);
            this.panel4.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel4.Location = new System.Drawing.Point(323, 469);
            this.panel4.Name = "panel4";
            this.panel4.Size = new System.Drawing.Size(727, 34);
            this.panel4.TabIndex = 15;
            // 
            // btFillRectangle
            // 
            this.btFillRectangle.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btFillRectangle.Location = new System.Drawing.Point(335, 1);
            this.btFillRectangle.Name = "btFillRectangle";
            this.btFillRectangle.Size = new System.Drawing.Size(222, 30);
            this.btFillRectangle.TabIndex = 15;
            this.btFillRectangle.Text = "Fill Time Rectangle";
            this.btFillRectangle.UseVisualStyleBackColor = true;
            this.btFillRectangle.Click += new System.EventHandler(this.btFillRectangle_Click);
            // 
            // btSave
            // 
            this.btSave.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btSave.Location = new System.Drawing.Point(608, 0);
            this.btSave.Name = "btSave";
            this.btSave.Size = new System.Drawing.Size(119, 30);
            this.btSave.TabIndex = 14;
            this.btSave.Text = "Save All";
            this.btSave.UseVisualStyleBackColor = true;
            this.btSave.Click += new System.EventHandler(this.btSave_Click);
            // 
            // TimeScheduleForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1073, 506);
            this.Controls.Add(this.tableLayoutPanel1);
            this.Name = "TimeScheduleForm";
            this.Text = "Route Time Schedules";
            ((System.ComponentModel.ISupportInitialize)(this.gridTimes)).EndInit();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            this.panel2.ResumeLayout(false);
            this.panel3.ResumeLayout(false);
            this.panel3.PerformLayout();
            this.panel4.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListBox lbServiceType;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.CheckBox cbMon;
        private System.Windows.Forms.CheckBox cbTue;
        private System.Windows.Forms.CheckBox cbWed;
        private System.Windows.Forms.CheckBox cbThu;
        private System.Windows.Forms.CheckBox cbFri;
        private System.Windows.Forms.CheckBox cbSat;
        private System.Windows.Forms.CheckBox cbSun;
        private System.Windows.Forms.DataGridView gridTimes;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Panel panel2;
        private System.Windows.Forms.Button btAddService;
        private System.Windows.Forms.TextBox tbServiceID;
        private System.Windows.Forms.TextBox tbStartDate;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox tbEndDate;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.ListBox lbRoutes;
        private System.Windows.Forms.Label lbStatus;
        private System.Windows.Forms.Panel panel3;
        private System.Windows.Forms.CheckBox cbInexactSchedule;
        private System.Windows.Forms.Panel panel4;
        private System.Windows.Forms.Button btSave;
        private System.Windows.Forms.Button btFillRectangle;
    }
}