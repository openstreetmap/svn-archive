// AJAX wrapper - designed to make it easy to code AJAX
// Licence: LGPL
// You can use this in the assignment as long as you credit it.

function ajaxrequest(URL,method,information,callbackFunction,addData)
{

    var xmlHTTP;

	//URL = URL + "?" + information;

    // The if statement is necessary as Firefox and Internet Explorer have
    // different implementations of AJAX.
    if(window.XMLHttpRequest)
    {
        // Set up the AJAX variable on Firefox
        xmlHTTP = new XMLHttpRequest();
    }
    else
    {
        // Set up the AJAX variable on Internet Explorer 
        xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
    }

    // This line specifies we are POSTing the data (POST request)
    xmlHTTP.open('POST',URL, true);

    // Keep this line in - common to all ajax requests
    xmlHTTP.setRequestHeader('Content-Type',
                    'application/x-www-form-urlencoded');


    // This line specifies the callback function - the code which will run
    // when we receive the response from the server.
    xmlHTTP.onreadystatechange =  function()
        {
            if(xmlHTTP.readyState==4)
                callbackFunction(xmlHTTP,addData);
        }

    // Send the data.
    xmlHTTP.send(information);
}

// Function to easily extract the value in a unique child tag
function getTagValue(parentTag,childTagName)
{
    return parentTag.getElementsByTagName(childTagName)[0].firstChild.nodeValue;
}
