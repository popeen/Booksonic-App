package github.popeen.dsub.util.compat;

import android.content.Context;
import androidx.mediarouter.media.MediaRouter;

import github.popeen.dsub.service.DownloadService;
import github.popeen.dsub.service.RemoteController;


// Provides stubs for Google-related functionality
public final class GoogleCompat {

    public static boolean playServicesAvailable(Context context) {
        return false;
    }

    public static void installProvider(Context context) throws Exception {
    }

    public static boolean castAvailable() {
        return false;
    }

    public static RemoteController getController(DownloadService downloadService, MediaRouter.RouteInfo info) {
        return null;
    }

    public static String getCastControlCategory() {
        return null;
    }
}
