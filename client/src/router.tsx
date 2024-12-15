import { createBrowserRouter } from "react-router-dom";
import { PageLayout } from "./layouts/PageLayout";
import { RootLayout } from "./layouts/RootLayout";
import { ErrorPage } from "./pages/ErrorPage";
import { WelcomePage } from "./pages/WelcomePage";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <WelcomePage />,
        errorElement: <ErrorPage /> ,
    },
    {
        path: "/",
        element: <RootLayout />,
        children: [
            {
                path: "schedules",
                element: <PageLayout subject="Schedules"/>
            },
            {
                path: "tasks",
                element: <PageLayout subject="Tasks"/>
            },
            {
                path: "people",
                element: <PageLayout subject="People"/>
            },
            {
                path: "about",
                element: <PageLayout subject="About"/>
            }
        ]
    }
]);