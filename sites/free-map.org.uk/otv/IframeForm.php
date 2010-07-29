<?php

/* iframe based pseudo-ajax upload:
   see
   http://ehsun7b.blogspot.com/2007/11/uploading-files-using-ajax_12.html
*/

session_start();
require_once('../lib/functionsnew.php');

class IframeForm
{
    protected $file, $errorDiv, $uploadMsg, $msg, $error, $filename;

    function __construct($file,$errorDiv,$uploadMsg)
    {
        $this->file = $file;
        $this->errorDiv = $errorDiv;
        $this->uploadMsg = $uploadMsg;
    }

    function createPage()
    {
        if (isset($_FILES[$this->file]))
        {
            // upload file
            $this->filename=$_FILES[$this->file]['tmp_name'];
            $file_name=$_FILES[$this->file]['name'];
            $file_size=$_FILES[$this->file]['size'];
            $file_type=$_FILES[$this->file]['type'];
            $file_error=$_FILES[$this->file]['error'];
    
            $this->msg=
                    "Successfully uploaded file ". $_FILES[$this->file]['name'];
            $this->error=false;

            if ($file_error>0)
            {
                $this->error=true;
                switch($file_error)
                {
                    case 1: $this->msg= 
                        "Exceeded upload max filesize (1MB)"; break;
                    case 2: $this->msg= "Exceeded max filesize (1MB)"; break;
                    case 3: $this->msg= "Partially uploaded"; break;
                    case 4: $this->msg= "Nothing uploaded"; break;
                }
            }


            if(!is_uploaded_file($this->filename))
            {
                $this->msg="There is something wrong with the uploaded file.";
                $this->error=true;
            }

            if (! $this->error)
            {
                $this->doProcessUpload();
            }
        }
        $this->writePage();
    }

    function doProcessUpload()
    {
    }

    function writePage()
    {
        ?>
        <html>
        <head>
           <script type='text/javascript'>
		 var pg;
        function loadingMsg()
        {
               pg = parent.content.document;
            pg.getElementById('<?php echo $this->errorDiv; ?>').innerHTML=
                   '<?php echo $this->uploadMsg;  ?>';
            return true;
        }
        </script>
        <?php
        if(isset($_FILES[$this->file]))
        {
            $errHTML = "<strong>".$this->msg."</strong>";
            ?>
            <script type='text/javascript'>
             var pg = parent.content.document;
            pg.getElementById('<?php echo $this->errorDiv; ?>').innerHTML = 
                '<?php echo $errHTML; ?>';
            </script>
            <?php
            if($this->error==false)
            {
                $this->doWriteOutput();
            }
        }
        echo "</head><body>";
        $this->doWriteForm();
        echo "</body></html>";
    }

    function doWriteOutput()
    {
        // write output from form processing here
    }

    function doWriteForm()
    {
        // write actual form here
    }
}
?>
