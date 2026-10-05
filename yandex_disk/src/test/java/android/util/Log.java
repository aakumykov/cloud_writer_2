package android.util;

// Source - https://stackoverflow.com/a/46793567
// Posted by Paglian, modified by community. See post 'Timeline' for change history
// Retrieved 2026-09-23, License - CC BY-SA 4.0

import androidx.annotation.NonNull;

public class Log {
    public static int d(String tag, String msg) {
        System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println("ERROR: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg, @NonNull Throwable t) {
        System.out.println("ERROR: " + tag + ": " + msg);
        t.printStackTrace();
        return 0;
    }

    // add other methods if required...
}

