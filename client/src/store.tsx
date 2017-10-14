import { UserForm, ModelName } from "./components/CreateUserForm";
import { createStore, applyMiddleware } from "redux";
import { combineForms } from "react-redux-form";
import thunk from "redux-thunk";
import { composeWithDevTools } from "redux-devtools-extension";
import createHistory from "history/createBrowserHistory";
import { routerMiddleware } from "react-router-redux";

const initialFormState: UserForm = {
          firstName: "",
          lastName: "",
          email: "",
          repeatEmail: "",
          password: "",
          repeatPassword: ""
};

export interface StoreState {
          contact: UserForm;
}

const storeState = {};
storeState[ModelName] = initialFormState;

export const history = createHistory();
const historyMiddleWare = routerMiddleware(history);
const middleWare = applyMiddleware(thunk, historyMiddleWare);

const store = createStore(
          combineForms(storeState),
          composeWithDevTools(middleWare)
);
export default store;
