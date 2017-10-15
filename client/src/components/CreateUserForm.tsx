import * as React from "react";
import { Form, Control } from "react-redux-form";
import { connect } from "react-redux";

import Button from "material-ui/Button";
import TextField from "material-ui/TextField";
import { doCreateUser } from "../actions";
import { StoreState } from "../store";
import { CircularProgress } from "material-ui/Progress";
import { paths } from "../routes";

export interface UserForm {
    firstName: string;
    lastName: string;
    email: string;
    repeatEmail: string;
    password: string;
    repeatPassword: string;
}

export const userFormInitialState: UserForm = {
    firstName: "",
    lastName: "",
    email: "",
    repeatEmail: "",
    password: "",
    repeatPassword: ""
};

interface Props {
    handleSubmit: (e: any) => any;
    isLoading: boolean;
}

const ModelName = "userForm";

const ContactFormSFC: React.SFC<Props> = ({ handleSubmit, isLoading }) => {
    console.log(isLoading);
    if (isLoading) {
        return <CircularProgress />;
    }

    return (
        <div>
            <h1> Create User </h1>
            <Form model={ModelName} onSubmit={form => handleSubmit(form)}>
                <div>
                    <Control.text
                        model=".firstName"
                        label="First Name"
                        component={TextField}
                    />
                    <Control.text
                        model=".lastName"
                        label="Last Name"
                        component={TextField}
                    />
                </div>
                <div>
                    <Control.text
                        model=".email"
                        label="e-mail"
                        component={TextField}
                    />
                    <Control.text
                        model=".repeatEmail"
                        label="repeat e-mail"
                        component={TextField}
                    />
                </div>
                <div>
                    <Control.text
                        model=".password"
                        type="password"
                        label="password"
                        component={TextField}
                    />
                    <Control.text
                        model=".repeatPassword"
                        type="password"
                        label="repeat password"
                        component={TextField}
                    />
                </div>
                <Button type="submit">Submit</Button>
            </Form>
        </div>
    );
};

const mapStateToProps = (state: { storeState: StoreState }) => ({
    isLoading: state.storeState.modalProgress
});
const mapDispatchToProps = (dispatch: any) => ({
    handleSubmit: (form: UserForm) =>
        dispatch(doCreateUser(paths.index)({ form: form }))
});

export default connect(mapStateToProps, mapDispatchToProps)(ContactFormSFC);
