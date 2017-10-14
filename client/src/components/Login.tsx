import * as React from "react";
import { connect } from "react-redux";

interface Props {}

export const ModelName = "userForm";

const Login: React.SFC<Props> = () => {
    return <h1>Welcome to WishR</h1>;
};

const mapStateToProps = (state: any) => ({});
const mapDispatchToProps = (dispatch: any) => ({});

export default connect(mapStateToProps, mapDispatchToProps)(Login);
