package com.httpstat.kotlin;

interface RoundTripLogger {

    void log(RoundTripLog log);

    class DefaultLogger implements RoundTripLogger {
        static class Holder {
            static final DefaultLogger INSTANCE = new DefaultLogger();
        }

        public static DefaultLogger getInstance() {
            return Holder.INSTANCE;
        }

        private static final String TAG = "RoundTripLogger";

        private DefaultLogger() {}

        @Override
        public void log(RoundTripLog log) {
            if (log != null) {
                System.out.println(log);
            }
        }
    }

}
