import classNames from "classnames"
import { SideNavButton } from "./SideNavButton"
import { useLocation } from "react-router-dom";

type Props = {
    includeSchedules: boolean;
}

export const SideNav = (props: Props) => {
    const { includeSchedules } = props;
    const { pathname } = useLocation();

    return <>
        <div className={classNames(
            "mt-4",
            "h-[40px]",
            "text-2xl"
        )}>
            Navigate</div>
        {includeSchedules && <SideNavButton to="/schedules" text="Schedules" isActive={false} />}
        <SideNavButton to="/tasks" text="Add/Edit Tasks" isActive={pathname === "/tasks"} />
        <SideNavButton to="/people" text="Add/Edit People" isActive={pathname === "/people"} />
        <SideNavButton to="/about" text="About" isActive={pathname === "/about"} />
    </>

}