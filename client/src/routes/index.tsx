type Path = "/" | "createUser";
export type PathType = Path;

interface Paths {
    index: PathType;
    createUser: PathType;
}
export const paths: Paths = {
    index: "/",
    createUser: "createUser"
};
