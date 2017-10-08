import { UserForm, ModelName } from './components/CreateUserForm';
import { createStore, applyMiddleware } from 'redux';
import { combineForms } from 'react-redux-form';
import thunk from 'redux-thunk';
import { composeWithDevTools } from 'redux-devtools-extension';

const initialFormState: UserForm = {
	firstName: '',
	lastName: '',
	email: '',
	repeatEmail: '',
	password: '',
	repeatPassword: ''
};

export interface StoreState {
	contact: UserForm;
}

const storeState = {};
storeState[ModelName] = initialFormState;

const store = createStore(combineForms(storeState), composeWithDevTools(applyMiddleware(thunk)));
export default store;
