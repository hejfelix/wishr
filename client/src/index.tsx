import * as React from "react";
import * as ReactDOM from "react-dom";
import { unregister } from "./registerServiceWorker";
import "./index.css";

import CreateUserForm from "./components/CreateUserForm";
import Login from "./components/Login";

import { Provider } from "react-redux";

import store from "./store";
import { history } from "./store";
import { ConnectedRouter } from "react-router-redux";
import { Route } from "react-router";

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <div>
                <Route exact path="/" component={Login} />
                <Route path="/createUser" component={CreateUserForm} />
            </div>
        </ConnectedRouter>
    </Provider>,
    document.getElementById("root") as HTMLElement
);
unregister();
