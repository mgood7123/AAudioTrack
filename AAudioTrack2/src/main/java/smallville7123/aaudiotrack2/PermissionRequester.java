package smallville7123.aaudiotrack2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionRequester {
    private static final String TAG = "PermissionRequester";

    private Activity savedActivity;
    String savedPermission;
    private int savedRequestCode;

    private boolean permissionGranted(Activity activity, String permission) {
        return activity.checkSelfPermission(permission) == PERMISSION_GRANTED;
    }

    private boolean permissionDenied(Activity activity, String permission) {
        return activity.checkSelfPermission(permission) == PERMISSION_DENIED;
    }

    public static class DialogSet {
        final String message;
        final String grantedButtonText;
        final Runnable onGranted;
        final String deniedButtonText;
        final Runnable onDeny;

        public DialogSet(
                String message,
                String grantedButtonText,
                Runnable onGranted,
                String deniedButtonText,
                Runnable onDeny
        ) {
            this.message = message;
            this.grantedButtonText = grantedButtonText;
            this.onGranted = onGranted;
            this.deniedButtonText = deniedButtonText;
            this.onDeny = onDeny;
        }
    }

    public static class DialogSet2 extends DialogSet {
        final String additionalMessage;

        public DialogSet2(
                String message,
                String additionalMessage,
                String grantedButtonText,
                Runnable onGranted,
                String deniedButtonText,
                Runnable onDeny
        ) {
            super(message, grantedButtonText, onGranted, deniedButtonText, onDeny);
            this.additionalMessage = additionalMessage;
        }
    }

    DialogSet onFirstRequest;
    DialogSet2 onAlreadyGranted;
    DialogSet onPreviouslyDenied;

    public abstract static class HandleRequest {
        abstract void run(int requestCode);
    }

    HandleRequest handleOtherRequestsOn_PERMISSION_GRANTED = null;
    HandleRequest handleOtherRequestsOn_PERMISSION_DENIED = null;
    
    public static class PermissionSet {
        final DialogSet onFirstRequest;
        final DialogSet2 onAlreadyGranted;
        final DialogSet onPreviouslyDenied;

        PermissionSet(DialogSet onFirstRequest, DialogSet onPreviouslyDenied) {
            this.onFirstRequest = onFirstRequest;
            this.onAlreadyGranted = null;
            this.onPreviouslyDenied = onPreviouslyDenied;
        }

        PermissionSet(DialogSet onFirstRequest, DialogSet2 onAlreadyGranted, DialogSet onPreviouslyDenied) {
            this.onFirstRequest = onFirstRequest;
            this.onAlreadyGranted = onAlreadyGranted;
            this.onPreviouslyDenied = onPreviouslyDenied;
        }

        public PermissionSet(
                String messageOnFirstRequest,
                String grantedButtonTextOnFirstRequest,
                Runnable onGrantedOnFirstRequest,
                String deniedButtonTextOnFirstRequest,
                Runnable onDenyOnFirstRequest,

                String messageOnPreviouslyDenied,
                String grantedButtonTextOnPreviouslyDenied,
                Runnable onGrantedOnPreviouslyDenied,
                String deniedButtonTextOnPreviouslyDenied,
                Runnable onDenyOnPreviouslyDenied
        ) {
            this.onFirstRequest = new DialogSet (
                    messageOnFirstRequest,
                    grantedButtonTextOnFirstRequest,
                    onGrantedOnFirstRequest,
                    deniedButtonTextOnFirstRequest,
                    onDenyOnFirstRequest
            );
            this.onAlreadyGranted = null;
            this.onPreviouslyDenied = new DialogSet (
                    messageOnPreviouslyDenied,
                    grantedButtonTextOnPreviouslyDenied,
                    onGrantedOnPreviouslyDenied,
                    deniedButtonTextOnPreviouslyDenied,
                    onDenyOnPreviouslyDenied
            );
        }

        public PermissionSet(
                String messageOnFirstRequest,
                String grantedButtonTextOnFirstRequest,
                Runnable onGrantedOnFirstRequest,
                String deniedButtonTextOnFirstRequest,
                Runnable onDenyOnFirstRequest,

                String messageOnAlreadyGranted,
                String additionalMessageOnAlreadyGranted,
                String grantedButtonTextOnAlreadyGranted,
                Runnable onGrantedOnAlreadyGranted,
                String deniedButtonTextOnAlreadyGranted,
                Runnable onDenyOnAlreadyGranted,

                String messageOnPreviouslyDenied,
                String grantedButtonTextOnPreviouslyDenied,
                Runnable onGrantedOnPreviouslyDenied,
                String deniedButtonTextOnPreviouslyDenied,
                Runnable onDenyOnPreviouslyDenied
        ) {
            this.onFirstRequest = new DialogSet (
                    messageOnFirstRequest,
                    grantedButtonTextOnFirstRequest,
                    onGrantedOnFirstRequest,
                    deniedButtonTextOnFirstRequest,
                    onDenyOnFirstRequest
            );
            this.onAlreadyGranted = new DialogSet2 (
                    messageOnAlreadyGranted,
                    additionalMessageOnAlreadyGranted,
                    grantedButtonTextOnAlreadyGranted,
                    onGrantedOnAlreadyGranted,
                    deniedButtonTextOnAlreadyGranted,
                    onDenyOnAlreadyGranted
            );
            this.onPreviouslyDenied = new DialogSet (
                    messageOnPreviouslyDenied,
                    grantedButtonTextOnPreviouslyDenied,
                    onGrantedOnPreviouslyDenied,
                    deniedButtonTextOnPreviouslyDenied,
                    onDenyOnPreviouslyDenied
            );
        }
    }

    public void requestPermission(
            Activity activity,
            String permission,
            int requestCode,
            PermissionSet permissionSet,
            HandleRequest handleOtherRequestsOn_PERMISSION_GRANTED,
            HandleRequest handleOtherRequestsOn_PERMISSION_DENIED
    ) {
        onFirstRequest = permissionSet.onFirstRequest;
        onAlreadyGranted = permissionSet.onAlreadyGranted;
        onPreviouslyDenied = permissionSet.onPreviouslyDenied;
        this.handleOtherRequestsOn_PERMISSION_GRANTED = handleOtherRequestsOn_PERMISSION_GRANTED;
        this.handleOtherRequestsOn_PERMISSION_DENIED = handleOtherRequestsOn_PERMISSION_DENIED;

        if (permissionGranted(activity, permission)) {
            if (onAlreadyGranted != null) {
                showDialog(
                        activity,
                        onAlreadyGranted.message + ".\n\n" +
                                "However this permission has already been granted.\n\n" +
                                onAlreadyGranted.additionalMessage,
                        onAlreadyGranted.grantedButtonText,
                        onAlreadyGranted.onGranted,
                        onAlreadyGranted.deniedButtonText,
                        onAlreadyGranted.onDeny
                );
            }
        } else {
            showDialog(
                    activity,
                    onFirstRequest.message + ".\n\n" +
                            "do you want to grant this permission?",
                    onFirstRequest.grantedButtonText,
                    () -> {
                        if (permissionGranted(activity, permission)) {
                            if (onFirstRequest.onGranted != null) {
                                onFirstRequest.onGranted.run();
                            }
                        } else {
                            savedActivity = activity;
                            savedPermission = permission;
                            savedRequestCode = requestCode;
                            activity.requestPermissions(new String[]{permission}, requestCode);
                        }
                    },
                    onFirstRequest.deniedButtonText,
                    onFirstRequest.onDeny
            );
        }
    }

    private void showDialog(Context context, String message, String positiveButtonText, Runnable onPositiveButtonClicked, String negativeButtonText, Runnable onNegativeButtonClicked) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    if (onPositiveButtonClicked != null) {
                        onPositiveButtonClicked.run();
                    } else {
                        dialog.dismiss();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    if (onNegativeButtonClicked != null) {
                        onNegativeButtonClicked.run();
                    } else {
                        dialog.dismiss();
                    }
                    break;
            }
        };

        if (positiveButtonText == null || positiveButtonText.isEmpty()) {
            positiveButtonText = "Positive Button";
        }

        if (negativeButtonText == null || negativeButtonText.isEmpty()) {
            negativeButtonText = "Negative Button";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setMessage(message)
                .setPositiveButton(positiveButtonText, dialogClickListener)
                .setNegativeButton(negativeButtonText, dialogClickListener)
                .show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults[0] == PERMISSION_GRANTED) {
            if (requestCode == savedRequestCode) {
                if (onFirstRequest.onGranted != null) {
                    onFirstRequest.onGranted.run();
                }
            } else {
                if (handleOtherRequestsOn_PERMISSION_GRANTED != null) {
                    handleOtherRequestsOn_PERMISSION_GRANTED.run(requestCode);
                }
            }
        } else if (grantResults[0] == PERMISSION_DENIED) {
            if (requestCode == savedRequestCode) {
                showDialog(
                        savedActivity,
                        onPreviouslyDenied.message + ", and you have denied this permission.\n\n" +
                                "Do you want to request it again?",
                        onPreviouslyDenied.grantedButtonText,
                        () -> {
                            if (permissionGranted(savedActivity, savedPermission)) {
                                if (onFirstRequest.onGranted != null) {
                                    onPreviouslyDenied.onGranted.run();
                                }
                            } else {
                                savedActivity.requestPermissions(new String[]{savedPermission}, requestCode);
                            }
                        },
                        onPreviouslyDenied.deniedButtonText,
                        onPreviouslyDenied.onDeny
                );
            } else {
                if (handleOtherRequestsOn_PERMISSION_DENIED != null) {
                    handleOtherRequestsOn_PERMISSION_DENIED.run(requestCode);
                }
            }
        }
    }
}
