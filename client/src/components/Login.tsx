import * as React from "react";
import { connect } from "react-redux";
import Button from "material-ui/Button";

import TextField from "material-ui/TextField";
import { Form, Control } from "react-redux-form";
import { push } from "react-router-redux";
import Path from "../routes";

interface Props {
    handleSubmit: (e: any) => any;
    navigate: (path: Path) => void;
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
            </Form>
            <Button onClick={(e: any) => navigate("createUser")}>
                Create User
            </Button>
        </div>
    );
};

const logValues = (form: LoginForm) => (dispatch: any) => {
    console.log("logging: %O", form);
};

const mapStateToProps = (state: any) => ({});
const mapDispatchToProps = (dispatch: any) => ({
    handleSubmit: (form: LoginForm) => dispatch(logValues(form)),
    navigate: (path: Path) => dispatch(push(path))
});

export default connect(mapStateToProps, mapDispatchToProps)(Login);
