using System;
using System.Globalization;
using System.Text;

namespace OsmUtils.Framework
{
    [Serializable]
    public class OsmDuplicatedObjectIdException : Exception
    {
        public OsmDuplicatedObjectIdException(OsmObjectBase osmObject)
            : base(GetMessage(osmObject))
        { }

        public OsmDuplicatedObjectIdException()
            : base("An object with the same ObjectId is already in the database.")
        { }

        public OsmDuplicatedObjectIdException(string message) : base(message) { }
        public OsmDuplicatedObjectIdException(string message, Exception innerException) : base(message, innerException) { }
        protected OsmDuplicatedObjectIdException(System.Runtime.Serialization.SerializationInfo serializationInfo,
            System.Runtime.Serialization.StreamingContext context)
            : base(serializationInfo, context) { }

        private static string GetMessage(OsmObjectBase osmObject)
        {
            string msg = "A {0} object with ObjectId {1} is already in the database.";
            return String.Format(CultureInfo.CurrentCulture, msg, osmObject.GetType().Name, osmObject.ObjectId);
        }
    }
}
