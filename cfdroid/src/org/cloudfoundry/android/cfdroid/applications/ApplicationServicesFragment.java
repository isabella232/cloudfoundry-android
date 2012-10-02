package org.cloudfoundry.android.cfdroid.applications;

import java.util.List;

import org.cloudfoundry.android.cfdroid.CloudFoundry;
import org.cloudfoundry.android.cfdroid.R;
import org.cloudfoundry.android.cfdroid.services.ServiceView;
import org.cloudfoundry.android.cfdroid.services.ServicesListLoader;
import org.cloudfoundry.android.cfdroid.support.ItemListAdapter;
import org.cloudfoundry.android.cfdroid.support.ListLoadingFragment;
import org.cloudfoundry.android.cfdroid.support.Result;
import org.cloudfoundry.android.cfdroid.support.TaskWithDialog;
import org.cloudfoundry.android.cfdroid.support.masterdetail.DetailPaneEventsCallback;
import org.cloudfoundry.client.lib.CloudApplication;
import org.cloudfoundry.client.lib.CloudService;

import roboguice.util.Ln;
import android.database.ContentObserver;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ImageView;

import com.google.inject.Inject;

/**
 * A fragment that shows all services in the current cloud, with information
 * about which service is bound to the current app and ability to bind/unbind
 * them.
 * 
 * @author Eric Bottard
 * 
 */
public class ApplicationServicesFragment extends
		ListLoadingFragment<CloudService> 
		implements DetailPaneEventsCallback {
	/**
	 * A view that shows info about a service as well as toggle button to
	 * bind/unbind the service to a given app.
	 * 
	 * @author Eric Bottard
	 */
	private class BindableServiceView extends ServiceView {

		private ImageView bindButton;

		public BindableServiceView(View container) {
			super(container);
			bindButton = (ImageView) container.findViewById(R.id.link_unlink);
		}

		@Override
		public void bind(final CloudService service) {
			super.bind(service);

			boolean bound = getCloudApplication().getServices().contains(
					service.getName());
			bindButton.setImageLevel(bound ? 0 : 1);

			bindButton.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					toggleService(service.getName());
					return true;
				}

			});
		}

	}

	private int position;

	@Inject
	private CloudFoundry client;

	private ContentObserver contentObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});
		}
	};	
	
	@Override
	protected ItemListAdapter<CloudService, ?> adapterFor(
			List<CloudService> items) {

		return new ItemListAdapter<CloudService, BindableServiceView>(
				R.layout.application_service_list_item, getActivity()
						.getLayoutInflater(), items) {

			@Override
			protected BindableServiceView createView(View view) {
				return ApplicationServicesFragment.this.new BindableServiceView(
						view);
			}
		};
	}

	private CloudApplication getCloudApplication() {
		return client.getApplications(false).get(position);
	}

	@Override
	public Loader<Result<List<CloudService>>> onCreateLoader(int arg0, Bundle arg1) {
		return new ServicesListLoader(getActivity(), client);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position", position);
	}

	public void onStart() {
		super.onStart();
		client.listenForApplicationsUpdates(contentObserver);
	}

	@Override
	public void onStop() {
		client.stopListeningForApplicationUpdates(contentObserver);
		super.onStop();
	}

	public void selectionChanged(int position) {
		this.position = position;
		if (isResumed()) {
			refresh();
		}
	}

	private void toggleService(final String serviceName) {
		new TaskWithDialog<CloudApplication>(getActivity(), R.string.working) {
			@Override
			public CloudApplication call() throws Exception {
				if (getCloudApplication().getServices().contains(serviceName)) {
					return client.unbindService(
							getCloudApplication().getName(), serviceName);
				} else {
					return client.bindService(getCloudApplication().getName(),
							serviceName);
				}
			}

			protected void onSuccess(CloudApplication t) throws Exception {
				ApplicationServicesFragment.this.refresh();
			}

		}.execute();

	}

	@Override
	protected int loaderId() {
		return R.id.application_services_loader;
	}
}
