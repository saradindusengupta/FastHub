package com.fastaccess.helper;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.fastaccess.R;

import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by Kosh on 12/12/15 10:51 PM
 */
public class ActivityHelper {

    @Nullable public static Activity getActivity(@Nullable Context cont) {
        if (cont == null) return null;
        else if (cont instanceof Activity) return (Activity) cont;
        else if (cont instanceof ContextWrapper) return getActivity(((ContextWrapper) cont).getBaseContext());
        return null;
    }

    public static void startCustomTab(@NonNull Activity context, @NonNull Uri url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ViewHelper.getPrimaryColor(context));
        builder.setShowTitle(false);
        CustomTabsIntent tabsIntent = builder.build();
        tabsIntent.launchUrl(context, url);
    }

    public static void startCustomTab(@NonNull Activity context, @NonNull String url) {
        startCustomTab(context, Uri.parse(url));
    }

    public static void forceOpenInBrowser(@NonNull Context context, @NonNull Uri url) {
        try {
            Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Intent i = new Intent(Intent.ACTION_VIEW, url);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public static void forceOpenInBrowser(@NonNull Context context, @NonNull String url) {
        forceOpenInBrowser(context, Uri.parse(url));
    }

    @SafeVarargs public static void start(Activity activity, Class cl, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(activity, cl);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements);
        activity.startActivity(intent, options.toBundle());
    }

    public static void start(Activity activity, Intent intent, View sharedElement) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                sharedElement, ViewHelper.getTransitionName(sharedElement));
        activity.startActivity(intent, options.toBundle());
    }

    public static void startReveal(Activity activity, Intent intent, View sharedElement) {
        Rect rect = ViewHelper.getLayoutPosition(sharedElement);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeClipRevealAnimation(sharedElement, rect.centerX(), rect.centerY(), sharedElement
                .getWidth(), sharedElement.getHeight());
        activity.startActivity(intent, options.toBundle());
    }

    @SafeVarargs public static void start(Activity activity, Intent intent, Pair<View, String>... sharedElements) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements);
        activity.startActivity(intent, options.toBundle());

    }

    public static void shareUrl(@NonNull Context context, @NonNull String url) {
        Activity activity = getActivity(context);
        if (activity == null) throw new IllegalArgumentException("Context given is not an instance of activity " + context.getClass().getName());
        try {
            ShareCompat.IntentBuilder.from(activity)
                    .setChooserTitle(context.getString(R.string.share))
                    .setType("text/*")
                    .setText(url)
                    .startChooser();
        } catch (ActivityNotFoundException e) {
            Toasty.error(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Nullable public static Fragment getVisibleFragment(@NonNull FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();
        if (fragments != null && !fragments.isEmpty()) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible()) {
                    return fragment;
                }
            }
        }
        return null;
    }

    private static boolean isPermissionGranted(@NonNull Context context, @NonNull String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isExplanationNeeded(@NonNull Activity context, @NonNull String permissionName) {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permissionName);
    }

    private static boolean isReadWritePermissionIsGranted(@NonNull Context context) {
        return isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                && isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private static void requestReadWritePermission(@NonNull Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 1);
    }

    public static boolean checkAndRequestReadWritePermission(@NonNull Activity activity) {
        if (!isReadWritePermissionIsGranted(activity)) {
            requestReadWritePermission(activity);
            return false;
        } else if (isExplanationNeeded(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                || isExplanationNeeded(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toasty.error(activity, activity.getString(R.string.read_write_permission_explanation), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
