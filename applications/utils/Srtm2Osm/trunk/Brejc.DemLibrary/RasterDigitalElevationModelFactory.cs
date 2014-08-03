using System;
using System.Collections.Generic;
using System.IO;

namespace Brejc.DemLibrary
{
    /// <summary>
    /// Defines a factory API to obtain a DEM storage that is supported on the current executing system.
    /// </summary>
    class RasterDigitalElevationModelFactory
    {
        /// <summary>
        /// Gets or sets the activity logger.
        /// </summary>
        /// <value>The activity logger.</value>
        public IActivityLogger ActivityLogger { get { return activityLogger; } set { activityLogger = value; } }

        /// <summary>
        /// Gets the size of the DEM in bytes.
        /// </summary>
        private long RequiredBytes
        {
            get { return this.lonLength * this.latLength * 2L; }
        }

        /// <summary>
        /// Gets the size of the DEM in mebibytes.
        /// </summary>
        private long RequiredMebibytes
        {
            get { return RequiredBytes / 1024L / 1024L; }
        }

        public RasterDigitalElevationModelFactory(int lonResolution,
            int latResolution,
            int lonOffset,
            int latOffset,
            int lonLength,
            int latLength)
        {
            this.lonResolution = lonResolution;
            this.latResolution = latResolution;
            this.lonOffset = lonOffset;
            this.latOffset = latOffset;
            this.lonLength = lonLength;
            this.latLength = latLength;

            this.ActivityLogger = new ConsoleActivityLogger();
        }

        /// <summary>
        /// Returns a suitable DEM storage which is supported by the executing system.
        /// </summary>
        /// <returns>NULL if no model is available.</returns>
        public RasterDigitalElevationModelBase CreateSupportedModel()
        {
            RasterDigitalElevationModelBase result = null;

            // First of all try to save the DEM in memory
            result = TryMemory();

            // If this fails try the filesystem
            if (result == null)
                result = TryFilesystem();

            if (result is FileBasedRasterDigitalElevationModel)
            {
                string msg = "Using the filesystem as cache. This will take a huge amount of time, be patient.";
                msg += " It will also create a {0} MiB file in your location for temporary files.";

                this.ActivityLogger.LogFormat(ActivityLogLevel.Warning, msg, RequiredMebibytes);
            }

            return result;
        }

        /// <summary>
        /// Tries to use the DEM stored in memory.
        /// </summary>
        /// <returns>NULL if the in-memory DEM is not available.</returns>
        private RasterDigitalElevationModelBase TryMemory()
        {
            RasterDigitalElevationModelBase result = null;
            long maxBytes = 1024 * 1024 * 1024 * 2L;         // 2 GiB

            // 2 GiB is the .NET limit for allocating RAM, at least in 2.0.
            // See https://stackoverflow.com/questions/1087982/single-objects-still-limited-to-2-gb-in-size-in-clr-4-0
            if (RequiredBytes > maxBytes)
            {
                this.ActivityLogger.LogFormat(ActivityLogLevel.Warning,
                    "In-memory cache not available due to DEM size of {0} MiB bigger than 2 GiB.", RequiredMebibytes);
                return null;
            }

            try
            {
                result = new MemoryBasedRasterDigitalElevationModel(
                    lonResolution, latResolution, lonOffset, latOffset, lonLength, latLength);
            }
            catch (OverflowException)
            {
                return null;
            }
            catch (OutOfMemoryException)
            {
                this.ActivityLogger.LogFormat(ActivityLogLevel.Warning,
                    "In-memory cache not available due to not enough free memory. DEM needs {0} MiB.", RequiredMebibytes);
                return null;
            }

            return result;
        }

        /// <summary>
        /// Tries to use the DEM stored in filesystem.
        /// </summary>
        /// <returns>NULL if the filesystem does not support the DEM.</returns>
        private RasterDigitalElevationModelBase TryFilesystem()
        {
            long available = GetAvailableFreeSpace(Path.GetTempPath());
            long maxFilesize = GetMaxFileSize(Path.GetTempPath());

            if (available != -1 && RequiredBytes > available)
            {
                this.ActivityLogger.LogFormat(ActivityLogLevel.Warning,
                    "Not enough free space on the location for temporary files.", RequiredMebibytes);

                return null;
            }

            if (maxFilesize != -1 && RequiredBytes > maxFilesize)
            {
                this.ActivityLogger.LogFormat(ActivityLogLevel.Warning,
                    "Not enough free space on the location for temporary files.", RequiredMebibytes);

                return null;
            }

            return new FileBasedRasterDigitalElevationModel(
                    lonResolution, latResolution, lonOffset, latOffset, lonLength, latLength);
        }

        /// <summary>
        /// Returns whether this assembly is running on a Mono runtime.
        /// </summary>
        /// <returns>TRUE if this assembly is running on Mono.</returns>
        private static bool IsRunningOnMono()
        {
            return Type.GetType("Mono.Runtime") != null;
        }

        /// <summary>
        /// Returns the maximum filesize supported by the filesystem used by the path in <paramref name="path"/>.
        /// </summary>
        /// <param name="path">Path on which the max. filesize should be determined.</param>
        /// <returns>The max. filesize or -1 on error.</returns>
        private static long GetMaxFileSize(string path)
        {
            // The DriveInfo class ctor is buggy as hell on Mono.
            if (IsRunningOnMono())
                return -1;

            DriveInfo drive = new DriveInfo(path);

            if (!drive.IsReady)
                return -1;

            switch (drive.DriveFormat)
            {
                case "exFAT":
                    return 1024L * 1024L * 1024L * 1024L * 1024L * 36L;
                case "FAT": // FAT16
                    return 1024L * 1024L * 1024L * 2L;
                case "FAT32":
                    return 1024L * 1024L * 1024L * 2L;
                case "NTFS":
                    return 1024L * 1024L * 1024L * 1024L * 16L;
                default:
                    return -1;
            }
        }

        /// <summary>
        /// Returns the free space available on the location in <paramref name="path"/>.
        /// </summary>
        /// <param name="path">Path on which the free space should be determined.</param>
        /// <returns>The free space or -1 on error.</returns>
        private static long GetAvailableFreeSpace(string path)
        {
            // The DriveInfo class ctor is buggy as hell on Mono.
            if (IsRunningOnMono())
                return -1;

            DriveInfo drive = new DriveInfo(path);

            if (!drive.IsReady)
                return -1;

            return drive.AvailableFreeSpace;

        }

        private int lonResolution, latResolution;
        private int lonOffset, latOffset;
        private int lonLength, latLength;

        private IActivityLogger activityLogger;
    }
}
