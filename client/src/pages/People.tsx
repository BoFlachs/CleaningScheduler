import classNames from "classnames";
import { Key, useEffect, useState } from "react";
import { AddButton } from "../components/AddButton";
import { Person } from "../components/Person";
import { PersonPrompt } from "../components/PersonPrompt";
import { SideNav } from "../components/SideNav";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { getAllPeople } from "../services/apiGet";
import * as types from "../types";

export const People = () => {
    const [loading, setLoading] = useState<boolean>(true);
    const [currentPersonName, setCurrentPersonName] = useState<string | undefined>(undefined)
    const [, setError] = useState<string | null>(null);
    const [people, setPeople] = useState<types.Person[] | undefined>(undefined)
    const { currentPerson } = useCleaningSchedulerContext()
    const [isPopupVisible, setIsPopupVisible] = useState(false);

    const togglePopup = () => {
        setIsPopupVisible((prev) => !prev);
    };

    useEffect(() => {
        const fetchPeople = async () => {
            try {
                setLoading(true)
                const people = await getAllPeople()
                setPeople(people)
                if (people?.length != 0) {
                    setCurrentPersonName(people![0].name)
                }
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchPeople();
    }, [currentPerson]);

    const showPerson = (personName: string) => {
        if (people != undefined) {
            var selectedPerson = people[0];
            for (const task of people) {
                if (task.name == personName) {
                    selectedPerson = task
                }
            }
            setCurrentPersonName(selectedPerson.name)
        }
    }

    return <>
        <aside className={classNames(
            "col-start-1",
            "bg-[#244788]",
            "flex",
            "flex-col",
            "items-center"
        )}>
            <SideNav includeSchedules={true} />
        </aside>
        <main className={classNames(
            "col-start-2",
            "bg-[#4393c9]",
            "flex",
            "justify-center"
        )}>
            {currentPersonName == undefined && <p className="m-4 mt-10">No person to show</p>}
            {currentPersonName != undefined && <Person personName={currentPersonName} />}
            {isPopupVisible && <PersonPrompt isPopupVisible={isPopupVisible}
                setIsPopupVisible={togglePopup} changePrompt={false} currentPerson={undefined} />}
        </main>
        <aside className={classNames(
            "col-start-3",
            "bg-[#244788]",
            "p-8",
            "font-medium",
        )}>
            <div className={
                classNames(
                    "bg-[#4393c9]/80",
                    "p-4",
                    "rounded-lg",
                    "text-[#082150]"
                )
            }>
                <p className="font-bold text-lg ">
                    People:
                </p>
                <p className="text-sm mb-2">Click to see more information:</p>
                <div className={classNames(
                    "text-sm",
                    "space-y-2",
                    "scrollable-container"
                )}>
                    {loading && <p>People loading</p>}
                    {!loading && people == undefined && <p>No people found</p>}
                    {people != undefined && people.map(person => (
                        <button
                            key={person.name as Key}
                            className={classNames(
                                "border-2 rounded-full",
                                "w-48",
                                "p-2",
                                "text-[#0f2057] ",
                                "border-[#0f2057]",
                                "bg-[#4393c9]",
                                { "!bg-[#0f2057]": currentPersonName == person.name },
                                { "text-[#4393c9]": currentPersonName == person.name },
                                "hover:text-[#4393c9] hover:bg-[#0f2057]",
                                "hover:border-[#0f2057] duration-300",
                            )}
                            disabled={currentPersonName == person.name}
                            onClick={() => showPerson(person.name)}
                        >
                            {person.name}
                        </button>
                    ))
                    }
                    <AddButton text={"Add New Person..."} addFunction={togglePopup} />
                </div>
            </div>
        </aside>
    </>
}