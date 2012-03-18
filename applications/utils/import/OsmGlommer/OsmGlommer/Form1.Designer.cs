namespace OsmGlommer
{
    partial class Form1
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
            this.tbFilename = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.btBrowseFolder = new System.Windows.Forms.Button();
            this.btConvert = new System.Windows.Forms.Button();
            this.richTextLogBox = new System.Windows.Forms.RichTextBox();
            this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
            this.SuspendLayout();
            // 
            // tbFilename
            // 
            this.tbFilename.Location = new System.Drawing.Point(126, 137);
            this.tbFilename.Name = "tbFilename";
            this.tbFilename.Size = new System.Drawing.Size(346, 20);
            this.tbFilename.TabIndex = 4;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(52, 144);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(53, 13);
            this.label3.TabIndex = 5;
            this.label3.Text = "OSM File:";
            // 
            // btBrowseFolder
            // 
            this.btBrowseFolder.Location = new System.Drawing.Point(478, 130);
            this.btBrowseFolder.Name = "btBrowseFolder";
            this.btBrowseFolder.Size = new System.Drawing.Size(78, 33);
            this.btBrowseFolder.TabIndex = 6;
            this.btBrowseFolder.Text = "Browse...";
            this.btBrowseFolder.UseVisualStyleBackColor = true;
            this.btBrowseFolder.Click += new System.EventHandler(this.btBrowseFolder_Click);
            // 
            // btConvert
            // 
            this.btConvert.Location = new System.Drawing.Point(126, 324);
            this.btConvert.Name = "btConvert";
            this.btConvert.Size = new System.Drawing.Size(78, 33);
            this.btConvert.TabIndex = 9;
            this.btConvert.Text = "Glom";
            this.btConvert.UseVisualStyleBackColor = true;
            this.btConvert.Click += new System.EventHandler(this.btConvert_Click);
            // 
            // richTextLogBox
            // 
            this.richTextLogBox.Location = new System.Drawing.Point(19, 172);
            this.richTextLogBox.Name = "richTextLogBox";
            this.richTextLogBox.Size = new System.Drawing.Size(555, 140);
            this.richTextLogBox.TabIndex = 10;
            this.richTextLogBox.Text = "";
            // 
            // openFileDialog1
            // 
            this.openFileDialog1.FileName = "openFileDialog1";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(609, 425);
            this.Controls.Add(this.richTextLogBox);
            this.Controls.Add(this.btConvert);
            this.Controls.Add(this.btBrowseFolder);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.tbFilename);
            this.Name = "Form1";
            this.Text = "Way Glommer - combine like ways";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox tbFilename;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button btBrowseFolder;
        private System.Windows.Forms.Button btConvert;
        private System.Windows.Forms.RichTextBox richTextLogBox;
        private System.Windows.Forms.OpenFileDialog openFileDialog1;
    }
}

