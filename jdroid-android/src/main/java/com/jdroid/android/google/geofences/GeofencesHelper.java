package com.jdroid.android.google.geofences;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.jdroid.android.R;
import com.jdroid.android.activity.ActivityIf;
import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.fragment.FragmentIf;
import com.jdroid.android.google.SafeResultCallback;
import com.jdroid.android.permission.PermissionHelper;
import com.jdroid.java.collections.Lists;
import com.jdroid.java.utils.LoggerUtils;

import org.slf4j.Logger;

import java.util.List;

public class GeofencesHelper {

	private final static Logger LOGGER = LoggerUtils.getLogger(GeofencesHelper.class);

	public static void addGeofences(final FragmentIf fragmentIf, final int intialTrigger, final List<Geofence> geofences) {

		PermissionHelper locationPermissionHelper = PermissionHelper.createLocationPermissionHelper((Fragment)fragmentIf);
		// TODO
		locationPermissionHelper.setAppInfoDialogMessageResId(R.string.jdroid_appInviteMessage);
		locationPermissionHelper.setOnRequestPermissionsResultListener(new PermissionHelper.OnRequestPermissionsResultListener() {
			@Override
			public void onRequestPermissionsGranted() {
				addGeofencesInternal(fragmentIf, intialTrigger, geofences);
			}

			@Override
			public void onRequestPermissionsDenied() {
				// Nothing to do
			}
		});
		Boolean locationPermissionGranted = locationPermissionHelper.checkPermission(false);
		if (locationPermissionGranted) {
			addGeofencesInternal(fragmentIf, intialTrigger, geofences);
		}
	}

	@SuppressWarnings("MissingPermission")
	private static void addGeofencesInternal(FragmentIf fragmentIf, final int intialTrigger, final List<Geofence> geofences) {
		GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
		geofencingRequestBuilder.setInitialTrigger(intialTrigger);
		geofencingRequestBuilder.addGeofences(geofences);
		GeofencingRequest geofencingRequest = geofencingRequestBuilder.build();

		Intent intent = new Intent(AbstractApplication.get(), GeofenceTransitionsIntentService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
		// calling addGeofences() and removeGeofences().
		PendingIntent geofencePendingIntent = PendingIntent.getService(AbstractApplication.get(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		LocationServices.GeofencingApi.addGeofences(fragmentIf.getActivityIf().getGoogleApiClient(), geofencingRequest, geofencePendingIntent).setResultCallback(new GeofenceResultCallback() {

			@Override
			public void onSuccessResult(@NonNull Status result) {
				LOGGER.info("Geofence added.");
			}

			@Override
			protected void onGeofenceNotAvailable() {

			}
		});
	}

	public static void removeGeofence(ActivityIf activityIf, String geoFenceId) {
		LocationServices.GeofencingApi.removeGeofences(activityIf.getGoogleApiClient(), Lists.newArrayList(geoFenceId)).setResultCallback(new GeofenceResultCallback() {
			@Override
			public void onSuccessResult(@NonNull Status result) {
				LOGGER.info("Geofence removed.");
			}

			@Override
			protected void onGeofenceNotAvailable() {

			}
		});
	}

	public static abstract class GeofenceResultCallback extends SafeResultCallback<Status> {

		@Override
		public final void onFailedResult(@NonNull Status result) {
			switch (result.getStatusCode()) {
				case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
					// Geofence service is not available now. Typically this is because the user turned off location access in settings > location access.
					LOGGER.warn("Geofence service not available.");
					onGeofenceNotAvailable();
				case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
					// Your app has registered more than 100 geofences. Remove unused ones before adding new geofences.
					AbstractApplication.get().getExceptionHandler().logHandledException("Too many geofences");
					onUnexpectedError(result);
				case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
					// You have provided more than 5 different PendingIntents to the addGeofences(GoogleApiClient, GeofencingRequest, PendingIntent) call.
					AbstractApplication.get().getExceptionHandler().logHandledException("Too many geofences pending intents");
					onUnexpectedError(result);
				default:
					AbstractApplication.get().getExceptionHandler().logHandledException("Unknown geofences error");
					onUnexpectedError(result);
			}
		}

		protected abstract void onGeofenceNotAvailable();

		protected void onUnexpectedError(@NonNull Status result) {
			// Do nothing
		}
	}
}