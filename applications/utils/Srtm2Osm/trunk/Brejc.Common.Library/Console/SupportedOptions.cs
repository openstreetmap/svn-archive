using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.Common.Console
{
    public class SupportedOptions
    {
        public IList<ConsoleApplicationOption> UsedOptions { get { return usedOptions; } }

        public void AddOption (ConsoleApplicationOption option)
        {
            supportedOptions.Add (option);
        }

        public int ParseArgs (string[] args, int startFrom)
        {
            int i = startFrom;

            for (; i < args.Length; i++)
            {
                foreach (ConsoleApplicationOption option in supportedOptions)
                {
                    String optionString = String.Format (System.Globalization.CultureInfo.InvariantCulture,
                        "{0}{1}", optionMarker, option.OptionName);

                    if (args[i].Equals (optionString, StringComparison.InvariantCultureIgnoreCase))
                    {
                        usedOptions.Add (option);

                        for (int j = 0; j < option.ParametersCount; j++)
                        {
                            if (i + 1 + j >= args.Length)
                                throw new ArgumentException (String.Format (System.Globalization.CultureInfo.InvariantCulture,
                                    "Too few arguments ('{0}').", option.OptionName));

                            // NOTE (Igor) 15.10.07: this condition prevented negative values as parameters, so I had to remove it
                            //if (args [i + 1 + j].StartsWith (optionMarker))
                            //    throw new ArgumentException (String.Format (System.Globalization.CultureInfo.InvariantCulture,
                            //        "Too few arguments ('{0}').", option.OptionName));

                            option.AddParameter (args[i + 1 + j]);
                        }
                    }
                }
            }

            return i;
        }

        private List<ConsoleApplicationOption> supportedOptions = new List<ConsoleApplicationOption> ();
        private List<ConsoleApplicationOption> usedOptions = new List<ConsoleApplicationOption> ();
        private string optionMarker = @"-";
    }
}
