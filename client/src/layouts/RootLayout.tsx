import { Outlet } from "react-router-dom";

export const RootLayout = () => {
    return <div className="flex flex-col h-screen ">
        <Outlet />
    </div>
};
