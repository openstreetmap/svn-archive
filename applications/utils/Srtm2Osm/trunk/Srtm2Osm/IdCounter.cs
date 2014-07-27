using System;
using System.Collections.Generic;
using System.Text;

namespace Srtm2Osm
{
    /// <summary>
    /// Provides OSM elements with a valid and unique ID number.
    /// </summary>
    class IdCounter
    {
        /// <summary>
        /// Gets a value indicating whether the ID numbers should be incremented or not.
        /// </summary>
        public bool Increment
        {
            get { return this.increment; }
        }

        private bool increment;
        private long currentId;
        private bool calledBefore;

        public IdCounter(bool increment, long startingId)
        {
            this.increment = increment;
            currentId = startingId;
        }

        /// <summary>
        /// Calculates the next ID number.
        /// </summary>
        /// <param name="valid">TRUE if the calculation succeeded. FALSE if the available number space is exhausted.</param>
        /// <returns>The next ID.</returns>
        public long GetNextId(out bool valid)
        {
            // Do nothing on the first call of this method.
            if (!calledBefore)
            {
                calledBefore = true;
                valid = true;
                return currentId;
            }

            valid = doCalculation(1);

            // Negative IDs are not supported.
            if (currentId <= 0)
                valid = false;

            return currentId;
        }

        /// <summary>
        /// Does the actual ID calculation.
        /// </summary>
        /// <remarks>An overflow is enforced and intercepted.</remarks>
        /// <param name="amount">Amount, which should be de-/incremented</param>
        /// <returns>TRUE if the calculation was successful, FALSE if it overflowed.</returns>
        private bool doCalculation(long amount)
        {
            if (amount < 1)
                throw new ArgumentOutOfRangeException("amount", "Negative or zero amounts are not valid.");

            try
            {
                checked
                {
                    currentId += Increment ? amount : amount * -1;
                }

                return true;
            }
            catch (OverflowException)
            {
                return false;
            }
        }

        public static implicit operator long (IdCounter counter)
        {
            bool valid = false;
            long result = counter.GetNextId(out valid);

            if (!valid)
                throw new OverflowException();

            return result;
        }
    }
}
