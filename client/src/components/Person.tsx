import { useEffect, useState } from "react";
import { getPerson } from "../services/apiGet";
import * as types from "../types";
import classNames from "classnames";
import { DeleteButton } from "./DeleteButton";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { deletePerson } from "../services/apiDelete";
import { Alert } from "./Alert";
import { ChangeButton } from "./ChangeButton";
import { PersonPrompt } from "./PersonPrompt";

type Props = {
    personName: string
}

export const Person = (props: Props) => {
    const { personName } = props;
    const [loading, setLoading] = useState<boolean>(true);
    const [alert, setAlert] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [person, setPerson] = useState<types.Person | undefined>(undefined)
    const { setCurrentPerson } = useCleaningSchedulerContext();
    const [isPopupVisible, setIsPopupVisible] = useState(false);

    const togglePopup = () => {
        setIsPopupVisible((prev) => !prev);
    };

    useEffect(() => {
        const fetchPerson = async () => {
            try {
                setLoading(true)
                const person = await getPerson(personName)
                setPerson(person)
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchPerson();
    }, [personName]);

    const deleteCurrentPerson = async () => {
        const result = await deletePerson(personName)

        if (result == 204) {
            const fakePerson: types.Person = { name: "fakePerson", availabilityAssignment: new Map<number, number>() }
            setCurrentPerson(fakePerson);
            setPerson(undefined);
        } else if (result == 409) {
            setAlert("At least one task or schedule is dependent on this person. Delete these first.")
        } else {
            console.log("Something unforeseen went wrong...")
        }
    }

    if (loading) return <p>Loading</p>
    if (error) return <p>{error}</p>

    return <div className="w-full">
        {isPopupVisible && <PersonPrompt isPopupVisible={isPopupVisible} setIsPopupVisible={togglePopup}
            changePrompt={true} currentPerson={person} />}
        {person ? (
            <div className={classNames(
                "rounded-[50px]",
                "mt-10",
                "p-10",
                "px-[10%]",
                "w-[70%]",
                "mx-auto",
                "bg-black/30",
                "text-[#082150]",
                "flex",
                "flex-col",
            )}>
                <h1 className="font-semibold mb-4 text-3xl">
                    {person.name}
                </h1>
                <h2 className="font-semibold mb-1">Availability:</h2>
                <div className="scrollable-container">
                    <table className={classNames("border border-collapse",
                        "border-[#082150]",
                        "divide-y divide-black",
                    )}>
                        <tbody>
                            {Array.from(person.availabilityAssignment.entries()).flatMap(([weekNumber, availability]) => [
                                <tr key={`week-${weekNumber}`} className="border border-[#082150]">
                                    <td className="border border-[#082150] px-2 py-2 w-20">Week {weekNumber}</td>
                                    <td className="border border-[#082150] px-6 py-2 w-40 text-center">{availability} minutes</td>
                                </tr>,
                            ])}
                        </tbody>
                    </table>
                    {alert && <Alert text={alert} onClick={() => setAlert(null)} />}
                    <ChangeButton text={"Change Person"} changeFunction={togglePopup} />
                    <DeleteButton text={"Delete Person"} deleteFunction={deleteCurrentPerson} />
                </div>
            </div>
        ) : (
            <p className="p-10">No person found.</p>
        )}
    </div>
}