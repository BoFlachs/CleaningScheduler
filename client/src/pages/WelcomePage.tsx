import classNames from "classnames"
import { NavButton } from "../components/NavButton"
import { Footer } from "../components/Footer"
import mop from "../assets/mop.png"

export const WelcomePage = () => {
    return <div className="flex flex-col h-screen bg-[#082150]">
        <header
            className={classNames(
                "text-center",
                "text-[#4393c9]",
                "text-7xl",
                "font-semibold",
                "mt-40",
                "mb-16"
            )}
        >
            Cleaning Scheduler
        </header>
        <main className="h-full text-[#d2e8f5] flex-1 px-12 m-auto">
            <div className="h-[450px] w-[350px] bg-black/60 rounded-[20%]">
                <div className="flex flex-col h-full items-center justify-center">
                    <NavButton to="/schedules" text="Schedules" />
                    <NavButton to="/tasks" text="Tasks" />
                    <NavButton to="/people" text="People" />
                    <NavButton to="/about" text="About" />
                </div>
            </div>
            <img src={mop}
                className={classNames(
                    "absolute",
                    "top-10",
                    "-translate-y-[700px]",
                    "-translate-x-[400px]",
                    "scale-[20%]"
                )}  
            />
        </main>
        <Footer />
    </div>
}