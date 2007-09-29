Imports System.Xml
Imports System.Xml.XPath
Imports System.io

Public Class cConfig

    Private _StrFile As String
    Private _Doc As XmlDocument = New XmlDocument

    Public Sub New(ByVal strFile As String)
        _StrFile = strFile
        If File.Exists(strFile) Then
            _Doc.Load(_StrFile)
        End If

    End Sub


    Public Function GetSettingDefault(ByVal StrKey As String, ByVal StrDefault As String) As String
        Dim Node As XmlNode = _Doc.SelectSingleNode("configuration/appSettings/add[@key='" + StrKey + "']")
        If (Node Is Nothing) Then
            Return StrDefault
        End If
        Return ReadWithDefault(Node.Attributes("value").Value, StrDefault)
    End Function


    Public Sub SetSetting(ByVal StrKey As String, ByVal StrValue As String)
        Dim Node As XmlNode = _Doc.SelectSingleNode("//configuration/appSettings/add[@key='" + StrKey + "']")
        If Node Is Nothing Then
            If _Doc.SelectSingleNode("//configuration") Is Nothing Then
                _Doc.AppendChild(_Doc.CreateElement("configuration"))
            End If
            If _Doc.SelectSingleNode("//configuration/appSettings") Is Nothing Then
                _Doc.SelectSingleNode("//configuration").AppendChild(_Doc.CreateElement("appSettings"))
            End If
            'If _Doc.SelectSingleNode("//configuration/appSettings/add") Is Nothing Then
            Dim oNode As XmlElement = _Doc.SelectSingleNode("//configuration/appSettings")
            Dim oAdd As XmlElement = _Doc.CreateElement("add")
            Dim oAttrKey As XmlAttribute = _Doc.CreateAttribute("key")
            Dim oAttrValue As XmlAttribute = _Doc.CreateAttribute("value")
            oAttrKey.Value = StrKey
            oAdd.Attributes.Append(oAttrKey)
            oAdd.Attributes.Append(oAttrValue)
            oNode.AppendChild(oAdd)
            'End If
            Node = _Doc.SelectSingleNode("//configuration/appSettings/add[@key='" + StrKey + "']")
        End If
        Node.Attributes("value").Value = StrValue
        _Doc.Save(_StrFile)
    End Sub


    Private Function ReadWithDefault(ByVal StrValue As String, ByVal StrDefault As String) As String
        Return IIf(StrValue Is Nothing, StrDefault, StrValue)
    End Function

End Class

