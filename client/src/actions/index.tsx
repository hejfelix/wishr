import actionCreatorFactory from "typescript-fsa";
import { bindThunkAction } from "typescript-fsa-redux-thunk";
import { UserForm } from "../components/CreateUserForm";
import { PathType } from "../routes";
import { push } from "react-router-redux";

const actionCreator = actionCreatorFactory();

export const createUser = actionCreator.async<
    { form: UserForm },
    { success: string },
    { error: any }
>("CREATE_USER");

function sleep(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export const doCreateUser = (navigateOnSuccess: PathType) =>
    bindThunkAction(createUser, async (userForm, dispatch) => {
        await sleep(2000);
        const res = { success: "Ok" };

        dispatch(push(navigateOnSuccess));
        // const res = await fetch(`/api/foo/${params.foo}`);
        return res;
    });
