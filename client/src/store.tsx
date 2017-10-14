import {
    ModelName as CreateUserModelName,
    userFormInitialState
} from "./components/CreateUserForm";
import {
    ModelName as LoginModelName,
    initialLoginFormState
} from "./components/Login";
import { createStore, applyMiddleware } from "redux";
import { combineForms } from "react-redux-form";
import thunk from "redux-thunk";
import { composeWithDevTools } from "redux-devtools-extension";
import createHistory from "history/createBrowserHistory";
import { routerMiddleware } from "react-router-redux";

// export interface StoreState {
//     contact: UserForm;
// }

const storeState = {};
storeState[CreateUserModelName] = userFormInitialState;
storeState[LoginModelName] = initialLoginFormState;

export const history = createHistory();
const historyMiddleWare = routerMiddleware(history);
const middleWare = applyMiddleware(thunk, historyMiddleWare);

const store = createStore(
    combineForms(storeState),
    composeWithDevTools(middleWare)
);
export default store;
