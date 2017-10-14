import * as React from "react";
import { Form, Control } from "react-redux-form";
import { connect } from "react-redux";

import Button from "material-ui/Button";
import TextField from "material-ui/TextField";

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
}

export const ModelName = "userForm";

const ContactFormSFC: React.SFC<Props> = ({ handleSubmit }) => {
    return (
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
    );
};

const logValues = (form: UserForm) => (dispatch: any) => {
    console.log("logging: %O", form);
};

const mapStateToProps = (state: any) => ({});
const mapDispatchToProps = (dispatch: any) => ({
    handleSubmit: (form: UserForm) => dispatch(logValues(form))
});

export default connect(mapStateToProps, mapDispatchToProps)(ContactFormSFC);
