import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { unregister } from './registerServiceWorker';
import './index.css';

import CreateUserForm from './components/CreateUserForm';

import { Provider } from 'react-redux';

import store from './store';

ReactDOM.render(
	<Provider store={store}>
		<CreateUserForm />
	</Provider>,
	document.getElementById('root') as HTMLElement
);
unregister();
