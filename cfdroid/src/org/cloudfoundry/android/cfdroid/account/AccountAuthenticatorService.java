/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.android.cfdroid.account;

import static android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Authenticator service that returns our particular subclass of
 * AbstractAccountAuthenticator in onBind().
 */
public class AccountAuthenticatorService extends Service {

	private static CloudFoundryAccountAuthenticator singleton = null;

	@Override
	public IBinder onBind(Intent intent) {
		return intent.getAction().equals(ACTION_AUTHENTICATOR_INTENT) ? getAuthenticator()
				.getIBinder() : null;
	}

	private CloudFoundryAccountAuthenticator getAuthenticator() {
		if (singleton == null) {
			singleton = new CloudFoundryAccountAuthenticator(this);
		}
		return singleton;
	}

}
