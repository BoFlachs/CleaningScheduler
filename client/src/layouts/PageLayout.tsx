import classNames from "classnames";
import { About } from "../pages/About";
import { People } from "../pages/People";
import { Schedules } from "../pages/Schedules";
import { Tasks } from "../pages/Tasks";
import { Footer } from "../components/Footer";
import mop from "../assets/mop.png";

type Props = {
    subject: string
}

export const PageLayout = (props: Props) => {
    const { subject } = props;

    return <>
        <header className={classNames(
            "bg-[#082150]",
            "py-4",
            "text-[#4393c9]",
            "text-5xl",
            "text-center",
            "pr-[300px]",
            "pl-[200px]"
        )}>
            <span className="flex items-center justify-center">
                <img src={mop}
                    className={classNames(
                        "h-20",
                    )}
                />
                {subject}</span>
        </header>
        <div className={classNames(
            "grid",
            "grid-cols-[200px_minmax(0,_1fr)_300px]",
            "h-full"
        )}>
            {subject == "Schedules" && <Schedules />}
            {subject == "Tasks" && <Tasks />}
            {subject == "People" && <People />}
            {subject == "About" && <About />}
        </div>
        <Footer />
    </>
}