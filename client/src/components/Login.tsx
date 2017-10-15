import * as React from "react";
import { connect } from "react-redux";
import Button from "material-ui/Button";

import TextField from "material-ui/TextField";
import { Form, Control } from "react-redux-form";
import { push } from "react-router-redux";
import { PathType, paths } from "../routes";

interface Props {
    handleSubmit: (e: any) => any;
    navigate: (path: PathType) => void;
}

export const ModelName = "loginForm";

export interface LoginForm {
    userName: string;
    password: string;
}

export const initialLoginFormState: LoginForm = {
    userName: "",
    password: ""
};

const Login: React.SFC<Props> = ({ handleSubmit, navigate }) => {
    return (
        <div>
            <h1>Welcome to WishR</h1>
            <Form model={ModelName} onSubmit={form => handleSubmit(form)}>
                <div>
                    <Control.text
                        model=".userName"
                        label="User name"
                        component={TextField}
                    />
                    <Control.text
                        model=".password"
                        label="Password"
                        type="password"
                        component={TextField}
                    />
                </div>
                <Button type="submit"> Log in </Button>
                or
                <Button
                    onClick={(_: React.MouseEvent<any>) =>
                        navigate(paths.createUser)}
                >
                    Create User
                </Button>
            </Form>
        </div>
    );
};

const logValues = (form: LoginForm) => (dispatch: any) => {
    console.log("logging: %O", form);
};

const mapStateToProps = (state: any) => ({});
const mapDispatchToProps = (dispatch: any) => ({
    handleSubmit: (form: LoginForm) => dispatch(logValues(form)),
    navigate: (path: PathType) => dispatch(push(path))
});

export default connect(mapStateToProps, mapDispatchToProps)(Login);
