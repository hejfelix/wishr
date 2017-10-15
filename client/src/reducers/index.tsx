import { createUser } from "../actions";
import { StoreState } from "../store";

export const reducer = (initialState: StoreState) => (
    state: StoreState = initialState,
    action: any
) => {
    console.log("Calling reducer wit action: %o", action);
    console.log("Calling reducer wit action: %o", action.type);
    console.log("Calling reducer wit action: %o", createUser.started.type);
    console.log("%o", initialState);
    switch (action.type) {
        case createUser.started.type:
            console.log("Turning on modal");
            return Object.assign({}, state, { modalProgress: true });
        case createUser.done.type:
            console.log("Success, turning off modal");
            return Object.assign({}, state, { modalProgress: false });
        case createUser.failed.type:
            console.log("Error, turning off modal");
            return Object.assign({}, state, { modalProgress: false });
        default:
            return state;
    }
};
