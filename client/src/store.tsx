import { UserForm, userFormInitialState } from "./components/CreateUserForm";
import { LoginForm, initialLoginFormState } from "./components/Login";
import { createStore, applyMiddleware } from "redux";
import { createForms } from "react-redux-form";
import thunk from "redux-thunk";
import { composeWithDevTools } from "redux-devtools-extension";
import createHistory from "history/createBrowserHistory";
import { routerMiddleware } from "react-router-redux";
import { combineReducers } from "redux";
import { reducer } from "./reducers";

export interface StoreState {
    modalProgress: boolean;
}

export interface FormState {
    userForm: UserForm;
    loginForm: LoginForm;
}

const initialFormState: FormState = {
    userForm: userFormInitialState,
    loginForm: initialLoginFormState
};

const initialStoreState: StoreState = {
    modalProgress: false
};

export const history = createHistory();
const historyMiddleWare = routerMiddleware(history);
const middleWare = applyMiddleware(thunk, historyMiddleWare);

const initializedReducer = reducer(initialStoreState);
// const formsReducer = combineForms(storeState);
const combinedReducer = combineReducers({
    storeState: initializedReducer,
    ...createForms(initialFormState)
});
const store = createStore(combinedReducer, composeWithDevTools(middleWare));
export default store;
