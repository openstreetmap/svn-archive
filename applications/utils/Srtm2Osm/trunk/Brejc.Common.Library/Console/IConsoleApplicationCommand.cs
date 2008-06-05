using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.Common.Console
{
    /// <summary>
    /// Interface defining the console command methods.
    /// </summary>
    public interface IConsoleApplicationCommand
    {
        /// <summary>
        /// Parse provided console command line arguments, starting from the specified argument.
        /// </summary>
        /// <param name="args">Console command line arguments.</param>
        /// <param name="startFrom">An index of the first argument to start parsing from.</param>
        /// <returns>An index of next argument which was not parsed.</returns>
        int ParseArgs (string[] args, int startFrom);

        /// <summary>
        /// Executes the console command.
        /// </summary>
        void Execute ();
    }
}
