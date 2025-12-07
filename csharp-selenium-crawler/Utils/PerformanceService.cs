using System;
using System.Diagnostics;

namespace Crawler.Utils
{
    public class PerformanceService
    {
        private Stopwatch _stopwatch;

        public PerformanceService()
        {
            _stopwatch = new Stopwatch();
        }

        public void StartTimer()
        {
            _stopwatch.Restart();
        }

        public void StopTimer()
        {
            _stopwatch.Stop();
        }

        public double GetDuration()
        {
            return _stopwatch.Elapsed.TotalMilliseconds;
        }

        public void ResetTimer()
        {
            _stopwatch.Reset();
        }
    }
}
