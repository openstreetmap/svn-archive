using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.Common.Console
{
    public class ConsoleApplicationOption
    {
        public int OptionId
        {
            get { return optionId; }
        }

        public string OptionName
        {
            get { return optionName; }
        }

        public int ParametersCount
        {
            get { return parametersCount; }
        }

        public IList<string> Parameters {get {return parameters;}}

        public ConsoleApplicationOption (int optionId, string optionName, int parametersCount)
        {
            this.optionId = optionId;
            this.optionName = optionName;
            this.parametersCount = parametersCount;
        }

        public ConsoleApplicationOption (int optionId, string optionName) : this (optionId, optionName, 0) { }

        public void AddParameter (string parameter)
        {
            parameters.Add (parameter);
        }

        private int optionId;
        private string optionName;
        private int parametersCount;
        private List<string> parameters = new List<string> ();
    }
}
